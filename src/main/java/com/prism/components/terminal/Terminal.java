package com.prism.components.terminal;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.definition.PrismFile;
import com.prism.components.definition.Shell;
import com.prism.components.definition.Tool;
import com.prism.components.extended.JDefaultKineticScrollPane;
import com.prism.components.extended.JExtendedTextField;
import com.prism.managers.FileManager;
import com.prism.managers.ToolsManager;
import com.prism.utils.FontLoader;
import com.prism.utils.Theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Terminal extends JPanel {
	private static final Color DEFAULT_FOREGROUND = Theme.invertColorIfDarkThemeSet(Color.BLACK);
	private static final Color DEFAULT_BACKGROUND = Color.decode("#1E1E1E");
	private static final Color PROMPT_COLOR = Color.decode("#808080");
	private static final int MAX_LINES = 8_000;

	private static final Pattern ANSI_PATTERN = Pattern.compile("\\x1B\\[[0-9;]*[a-zA-Z]");
	private final Prism prism = Prism.getInstance();
	private final BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final List<String> commandHistory = Collections.synchronizedList(new ArrayList<>());
	private final Shell shell;

	private final Map<String, Style> styleCache = new ConcurrentHashMap<>();
	private JTextField dirPathLabel;
	private volatile String currentDirectory;
	private volatile Process currentProcess;
	private volatile BufferedWriter processWriter;
	private volatile Future<?> processOutputTask;
	private volatile boolean ignoreComboEvents = false;
	private volatile int historyIndex = -1;
	private volatile boolean isProcessing = false;
	private JTextPane terminalArea;
	private JExtendedTextField commandTextField;
	private JComboBox<String> comboBoxCommands;
	private JPopupMenu toolMenu;
	private JList<String> toolList;
	private Style currentStyle;

	public Terminal(File directory, Shell shell) {
		this.shell = shell;
		this.currentDirectory = directory != null ? directory.getAbsolutePath() : System.getProperty("user.dir");

		initializeUI();
		setupInputHandling();
		initializeTerminal();
		initializeToolMenu();
	}

	private void initializeUI() {
		setLayout(new BorderLayout());
		setBackground(DEFAULT_BACKGROUND);

		terminalArea = new JTextPane() {
			@Override
			public boolean getScrollableTracksViewportWidth() {
				return getUI().getPreferredSize(this).width <= getParent().getSize().width;
			}
		};

		String fontName = prism.getConfig().getString(ConfigKey.TERMINAL_FONT_NAME, "Prism: JetBrains Mono");
		boolean isIntelliJMono = fontName.equals("Prism: JetBrains Mono");

		if (isIntelliJMono) {
			terminalArea.setFont(FontLoader.getIntelliJFont(14));
		} else {
			terminalArea.setFont(new Font(fontName, Font.PLAIN, 14));
		}

		terminalArea.setEditable(false);
		terminalArea.setBorder(new EmptyBorder(5, 5, 5, 5));

		JDefaultKineticScrollPane scrollPane = new JDefaultKineticScrollPane(terminalArea);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		add(scrollPane, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new BorderLayout(5, 0));
		bottomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		dirPathLabel = new JTextField();
		updateDirPathLabel();
		dirPathLabel.setEnabled(false);
		dirPathLabel.setEditable(false);
		dirPathLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), new EmptyBorder(2, 5, 2, 5)));

		dirPathLabel.setForeground(PROMPT_COLOR);


		commandTextField = new JExtendedTextField(20);
		commandTextField.setPlaceholder(prism.getLanguage().get(36));
		commandTextField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), new EmptyBorder(2, 5, 2, 5)));

		comboBoxCommands = new JComboBox<>(new String[]{prism.getLanguage().get(37)});
		comboBoxCommands.setEnabled(false);
		comboBoxCommands.setMaximumRowCount(10);

		bottomPanel.add(dirPathLabel, BorderLayout.WEST);
		bottomPanel.add(commandTextField, BorderLayout.CENTER);
		bottomPanel.add(comboBoxCommands, BorderLayout.EAST);

		add(bottomPanel, BorderLayout.SOUTH);
	}

	private void setupInputHandling() {
		commandTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				String text = commandTextField.getText();

				if (toolMenu.isVisible() && text.startsWith("/")) {
					switch (e.getKeyCode()) {
						case KeyEvent.VK_UP:
							e.consume();
							int prev = toolList.getSelectedIndex() - 1;
							if (prev >= 0) toolList.setSelectedIndex(prev);
							return;
						case KeyEvent.VK_DOWN:
							e.consume();
							int next = toolList.getSelectedIndex() + 1;
							if (next < toolList.getModel().getSize()) toolList.setSelectedIndex(next);
							return;
						case KeyEvent.VK_ENTER:
							e.consume();
							executeSelectedTool();
							return;
						case KeyEvent.VK_ESCAPE:
							e.consume();
							toolMenu.setVisible(false);
							return;
					}
				}

				switch (e.getKeyCode()) {
					case KeyEvent.VK_ENTER:
						e.consume();
						processInput();
						break;
					case KeyEvent.VK_UP:
						e.consume();
						navigateHistory(-1);
						break;
					case KeyEvent.VK_DOWN:
						e.consume();
						navigateHistory(1);
						break;
					case KeyEvent.VK_TAB:
						e.consume();
						autoComplete();
						break;
					case KeyEvent.VK_C:
						if (e.isControlDown()) {
							e.consume();
							handleCtrlC();
						}
						break;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				String text = commandTextField.getText();

				if (text.startsWith("/")) {
					showToolMenu();

					setCommandText("");
				} else {
					toolMenu.setVisible(false);
				}
			}
		});

		// Mouse wheel history navigation
		commandTextField.addMouseWheelListener(e -> {
			navigateHistory(e.getWheelRotation() > 0 ? 1 : -1);
		});

		comboBoxCommands.addActionListener(e -> {
			String selected = (String) comboBoxCommands.getSelectedItem();
			if (selected != null && !selected.equals(prism.getLanguage().get(37))) {
				if (!ignoreComboEvents) setCommandText(selected);
				commandTextField.requestFocus();
			}
		});

		// Terminal area context menu
		terminalArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showContextMenu(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showContextMenu(e);
				}
			}
		});
	}

	private void initializeTerminal() {
		initializeStyles();

		String shellName = getShellName();
		appendToTerminal(String.format("%s - Path: %s\n", shellName, currentDirectory), PROMPT_COLOR, false);
		appendPrompt();
	}

	private void initializeToolMenu() {
		toolMenu = new JPopupMenu();
		toolList = new JList<>(new DefaultListModel<>());
		toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		toolList.setVisibleRowCount(5);

		JDefaultKineticScrollPane scrollPane = new JDefaultKineticScrollPane(toolList);
		toolMenu.add(scrollPane);

		toolList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					executeSelectedTool();
					e.consume();
				}
			}
		});

		toolList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {
					executeSelectedTool();
				}
			}
		});
	}

	private void initializeStyles() {
		currentStyle = terminalArea.addStyle("default", null);
		StyleConstants.setBold(currentStyle, false);

		for (int i = 30; i <= 37; i++) {
			Style style = terminalArea.addStyle("ansi_" + i, null);
			StyleConstants.setForeground(style, getAnsiColor(i));
			styleCache.put(String.valueOf(i), style);
		}
	}

	private void processInput() {
		String input = commandTextField.getText().trim();
		if (input.isEmpty()) {
			appendToTerminal("\n", Color.BLACK, false);
			appendPrompt();
			return;
		}

		historyIndex = -1;

		if (isAlive()) {
			try {
				processWriter.write(input + "\n");
				processWriter.flush();
				appendToTerminal(input + "\n", DEFAULT_FOREGROUND, false);
			} catch (IOException ex) {
				appendToTerminal("Error: Failed to send input\n", Color.RED, true);
			}
		} else {
			executeCommand(input, true);
		}

		setCommandText("");
	}

	private void executeCommand(String command, boolean output) {
		if (command.isEmpty()) return;

		isProcessing = true;
		if (output) {
			addToHistory(command);
			appendToTerminal(command + "\n", DEFAULT_FOREGROUND, false);
		}

		if (handleInternalCommand(command, output)) {
			isProcessing = false;
			return;
		}

		executor.submit(() -> executeExternalCommand(command, output));
	}

	private boolean handleInternalCommand(String command, boolean output) {
		String cmd = command.toLowerCase().trim();

		if (cmd.equals("cls") || cmd.equals("clear")) {
			clearTerminal();
			return true;
		} else if (cmd.startsWith("cd ")) {
			changeDirectory(command, output);
			return true;
		} else if (cmd.equals("path")) {
			appendToTerminal("Current directory: " + currentDirectory + "\n", DEFAULT_FOREGROUND, false);
			appendPrompt();
			return true;
		} else if (cmd.equals("exit")) {
			if (isAlive()) {
				closeProcess();
			} else {
				appendPrompt();
			}
			return true;
		} else if (cmd.equals("history")) {
			showHistory();
			return true;
		} else if (cmd.startsWith("!")) {
			executeHistoryCommand(cmd);
			return true;
		}

		return false;
	}

	private void executeExternalCommand(String command, boolean output) {
		try {
			ProcessBuilder pb = createProcessBuilder(command);
			currentProcess = pb.start();

			processWriter = new BufferedWriter(new OutputStreamWriter(currentProcess.getOutputStream(), getCharset()));

			processOutputTask = executor.submit(this::readProcessOutput);

			int exitCode = currentProcess.waitFor();

			SwingUtilities.invokeLater(() -> {
				if (output && prism.getConfig().getBoolean(ConfigKey.SHOW_PROCESS_TERMINATION_CODE_OUTPUT, true)) {
					String message = exitCode == 0 ? ("\n" + prism.getLanguage().get(38) + "\n") : ("\n" + prism.getLanguage().get(39, exitCode) + "\n");
					Color color = exitCode == 0 ? new Color(0, 150, 0) : Color.RED;
					appendToTerminal(message, color, true);
				}

				if (output) {
					appendPrompt();
				}

				isProcessing = false;
			});

		} catch (IOException | InterruptedException ex) {
			SwingUtilities.invokeLater(() -> {
				appendToTerminal("Error: " + ex.getMessage() + "\n", Color.RED, true);
				appendPrompt();
				isProcessing = false;
			});
		} finally {
			cleanupProcess();
		}
	}

	private ProcessBuilder createProcessBuilder(String command) {
		ProcessBuilder pb = new ProcessBuilder();
		pb.directory(new File(currentDirectory));
		pb.redirectErrorStream(true);

		List<String> commands = new ArrayList<>();
		switch (shell) {
			case COMMAND_PROMPT:
				commands.addAll(Arrays.asList("cmd.exe", "/c", command));
				break;
			case POWERSHELL:
				commands.addAll(Arrays.asList("powershell.exe", "-Command", command));
				break;
			default:
				commands.addAll(Arrays.asList("cmd.exe", "/c", command));
		}

		pb.command(commands);
		return pb;
	}

	private void readProcessOutput() {
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(currentProcess.getInputStream(), getCharset()))) {

			String line;
			int lines = 0;
			final String ELLIPSIS = "\n... (output truncated at " + MAX_LINES + " lines) ...\n";

			while ((line = br.readLine()) != null) {
				if (lines < MAX_LINES) {
					final String l = line + "\n";
					SwingUtilities.invokeLater(() -> parseAndAppendAnsi(l));
				} else if (lines == MAX_LINES) {
					SwingUtilities.invokeLater(() -> parseAndAppendAnsi(ELLIPSIS));
				}
				lines++;
			}
		} catch (IOException ignored) {
		}
	}

	private void parseAndAppendAnsi(String text) {
		StyledDocument doc = terminalArea.getStyledDocument();
		Matcher matcher = ANSI_PATTERN.matcher(text);
		int lastEnd = 0;

		try {
			while (matcher.find()) {
				if (matcher.start() > lastEnd) {
					doc.insertString(doc.getLength(), text.substring(lastEnd, matcher.start()), currentStyle);
				}

				String ansiCode = matcher.group();
				processAnsiCode(ansiCode);
				lastEnd = matcher.end();
			}

			if (lastEnd < text.length()) {
				doc.insertString(doc.getLength(), text.substring(lastEnd), currentStyle);
			}
		} catch (BadLocationException ex) {

		}

		terminalArea.setCaretPosition(doc.getLength());

		trimTerminalBuffer();
	}

	private void processAnsiCode(String ansiCode) {
		String code = ansiCode.replaceAll("\\x1B\\[|\\]", "");

		if (code.endsWith("m")) {
			code = code.substring(0, code.length() - 1);
			applySgrCodes(code.split(";"));
		}
	}

	private void applySgrCodes(String[] codes) {
		for (String code : codes) {
			try {
				int num = Integer.parseInt(code);

				switch (num) {
					case 0:
						currentStyle = styleCache.get("default");
						break;
					case 1:
						StyleConstants.setBold(currentStyle, true);
						break;
					case 22:
						StyleConstants.setBold(currentStyle, false);
						break;
					case 30:
					case 31:
					case 32:
					case 33:
					case 34:
					case 35:
					case 36:
					case 37:
						currentStyle = styleCache.getOrDefault(String.valueOf(num), currentStyle);
						break;
					case 40:
					case 41:
					case 42:
					case 43:
					case 44:
					case 45:
					case 46:
					case 47:
						StyleConstants.setBackground(currentStyle, getAnsiColor(num - 10));
						break;
				}
			} catch (NumberFormatException ex) {

			}
		}
	}

	private Color getAnsiColor(int code) {
		switch (code) {
			case 30:
				return Color.BLACK;
			case 31:
				return Color.RED;
			case 32:
				return Color.decode("#006400");
			case 33:
				return Color.YELLOW;
			case 34:
				return Color.BLUE;
			case 35:
				return Color.MAGENTA;
			case 36:
				return Color.CYAN;
			case 37:
				return Color.WHITE;
			default:
				return DEFAULT_FOREGROUND;
		}
	}

	private void addToHistory(String command) {
		commandHistory.add(command);
		updateHistoryCombo();
		historyIndex = -1;
	}

	private void updateHistoryCombo() {
		SwingUtilities.invokeLater(() -> {
			if (!commandHistory.isEmpty()) {
				ignoreComboEvents = true;
				comboBoxCommands.setEnabled(true);
				comboBoxCommands.setModel(new DefaultComboBoxModel<>(commandHistory.toArray(new String[0])));
				comboBoxCommands.setSelectedIndex(commandHistory.size() - 1);
				ignoreComboEvents = false;
			}
		});
	}

	private void navigateHistory(int direction) {
		if (commandHistory.isEmpty()) return;

		historyIndex += direction;

		if (historyIndex < 0) {
			historyIndex = -1;
			setCommandText("");
		} else if (historyIndex >= commandHistory.size()) {
			historyIndex = commandHistory.size() - 1;
		} else {
			setCommandText(commandHistory.get(historyIndex));
		}
	}

	private void autoComplete() {
		String text = commandTextField.getText();
		if (text.isEmpty()) return;

		List<String> matches = commandHistory.stream().filter(cmd -> cmd.startsWith(text)).collect(ArrayList::new, (list, cmd) -> {
			if (!list.contains(cmd)) list.add(cmd);
		}, ArrayList::addAll);

		if (matches.size() == 1) {
			setCommandText(matches.get(0));
			commandTextField.setCaretPosition(matches.get(0).length());
		} else if (matches.size() > 1) {
			appendToTerminal("\n" + String.join("  ", matches) + "\n", Color.YELLOW, false);
			appendPrompt();
		}
	}

	private void handleCtrlC() {
		if (isAlive()) {
			currentProcess.destroyForcibly();
			appendToTerminal("^C\n", Color.YELLOW, false);
		} else {
			setCommandText("");
		}
	}

	private void showContextMenu(MouseEvent e) {
		JPopupMenu menu = new JPopupMenu();

		JMenuItem copy = new JMenuItem(prism.getLanguage().get(40));
		copy.addActionListener(ev -> terminalArea.copy());
		menu.add(copy);

		JMenuItem paste = new JMenuItem(prism.getLanguage().get(41));
		paste.addActionListener(ev -> {
			String text = null;
			try {
				text = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor).toString();
			} catch (Exception ex) {
				text = "";
			}

			setCommandText(commandTextField.getText() + text);
		});
		menu.add(paste);

		menu.addSeparator();

		JMenuItem clear = new JMenuItem(prism.getLanguage().get(42));
		clear.addActionListener(ev -> clearTerminal());
		menu.add(clear);

		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	private void showToolMenu() {
		java.util.List<Tool> tools = ToolsManager.getAllTools();
		DefaultListModel<String> model = new DefaultListModel<>();

		for (Tool tool : tools) {
			if (!tool.getShortcut().isEmpty()) {
				model.addElement(tool.getShortcut() + " - " + (tool.getDescription() != null ? tool.getDescription() : "No description"));
			}
		}

		toolList.setModel(model);

		if (model.getSize() > 0) {
			toolList.setSelectedIndex(0);
		}

		if (!toolMenu.isVisible()) {
			toolMenu.show(commandTextField, 0, -toolMenu.getPreferredSize().height);
		}
	}

	private void showHistory() {
		if (commandHistory.isEmpty()) {
			appendToTerminal(prism.getLanguage().get(43) + "\n", Color.YELLOW, false);
		} else {
			appendToTerminal("\n" + prism.getLanguage().get(44) + "\n", Color.CYAN, false);
			for (int i = 0; i < commandHistory.size(); i++) {
				appendToTerminal(String.format("%3d: %s\n", i + 1, commandHistory.get(i)), DEFAULT_FOREGROUND, false);
			}
		}
		appendPrompt();
	}

	private void executeHistoryCommand(String cmd) {
		try {
			int index = Integer.parseInt(cmd.substring(1)) - 1;
			if (index >= 0 && index < commandHistory.size()) {
				executeCommand(commandHistory.get(index), true);
			} else {
				appendPrompt();
			}
		} catch (NumberFormatException ex) {
			appendPrompt();
		}
	}

	private void appendToTerminal(String text, Color color, boolean bold) {
		SwingUtilities.invokeLater(() -> {
			try {
				StyledDocument doc = terminalArea.getStyledDocument();
				Style style = terminalArea.addStyle("temp", null);
				StyleConstants.setForeground(style, color);
				StyleConstants.setBold(style, bold);
				doc.insertString(doc.getLength(), text, style);
				terminalArea.setCaretPosition(doc.getLength());
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			}
		});
	}

	private void trimTerminalBuffer() {
		SwingUtilities.invokeLater(() -> {
			try {
				StyledDocument doc = terminalArea.getStyledDocument();
				int max = 10000;
				if (doc.getLength() > max) {
					doc.remove(0, doc.getLength() - max);
				}
			} catch (BadLocationException ignored) {
			}
		});
	}

	private void appendPrompt() {
		appendToTerminal("terminal@prism:~$ ", PROMPT_COLOR, false);
	}

	private void cleanupProcess() {
		if (currentProcess != null) {
			currentProcess.destroyForcibly();
			currentProcess = null;
		}
		if (processWriter != null) {
			try {
				processWriter.close();
			} catch (IOException ex) {

			}

			processWriter = null;
		}
		if (processOutputTask != null) {
			processOutputTask.cancel(true);
			processOutputTask = null;
		}
	}

	private Charset getCharset() {
		return System.getProperty("os.name").toLowerCase().contains("windows") ? Charset.forName("Cp850") : StandardCharsets.UTF_8;
	}

	private String getShellName() {
		switch (shell) {
			case COMMAND_PROMPT:
				return "Command Prompt";
			case POWERSHELL:
				return "PowerShell";
			default:
				return "Terminal";
		}
	}

	public boolean isAlive() {
		return currentProcess != null && currentProcess.isAlive();
	}

	public void clearTerminal() {
		SwingUtilities.invokeLater(() -> {
			terminalArea.setText("");
			appendPrompt();
		});
	}

	public void closeProcess() {
		cleanupProcess();
		appendToTerminal("\n" + prism.getLanguage().get(45) + "\n", Color.RED, true);
		appendPrompt();
	}

	public void restartProcess() {
		if (commandHistory.isEmpty()) {
			return;
		}

		closeProcess();

		executeCommand(commandHistory.get(commandHistory.size() - 1), true);
	}

	public void executeCommandSync(String command) {
		executeCommandSync(command, true);
	}

	public void executeCommandSync(String command, boolean output) {
		executeCommand(command, output);

		if (isAlive()) {
			try {
				currentProcess.waitFor();
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void executeTool(Tool tool) {
		closeProcess();
		PrismFile pfile = prism.getTextAreaTabbedPane().getCurrentFile();
		File file = pfile != null ? pfile.getFile() : null;

		prism.getLowerSidebar().setSelectedIndex(1);

		for (String argument : tool.getArguments()) {
			argument = argument.replace("$(FILE_NAME)", file != null ? file.getName() : "null");
			argument = argument.replace("$(FILE_NAME_NO_EXS)", file != null ? file.getName().replaceFirst("[.][^.]+$", "") : "null");
			argument = argument.replace("$(FILE_PATH)", file != null ? file.getAbsolutePath() : "null");
			argument = argument.replace("$(DIR_PATH)", FileManager.getRootDirectory().getAbsolutePath());

			executeCommandSync(argument);
		}
	}

	private void executeSelectedTool() {
		String shortcut = toolList.getSelectedValue();

		if (shortcut != null) {
			for (Tool tool : ToolsManager.getAllTools()) {
				if (!tool.getShortcut().isEmpty() && shortcut.startsWith(tool.getShortcut())) {
					toolMenu.setVisible(false);

					executeTool(tool);

					break;
				}
			}
		}
	}

	public boolean isProcessing() {
		return isProcessing;
	}

	public JTextPane getTerminalArea() {
		return terminalArea;
	}

	public Shell getShell() {
		return shell;
	}

	public List<String> getCommands() {
		return new ArrayList<>(commandHistory);
	}

	public String getDirectoryPath() {
		return currentDirectory;
	}

	public File getDirectory() {
		return new File(currentDirectory);
	}

	public void changeDirectory(String path) {
		changeDirectory("cd " + path, true);
	}

	public void changeDirectory(String command, boolean output) {
		String[] parts = command.trim().split("\\s+", 2);
		if (parts.length < 2) {
			if (output) {
				appendToTerminal("Usage: cd <directory>\n", Color.RED, true);
				appendPrompt();
			}
			return;
		}

		String newPath = parts[1].trim();
		File dir;

		if (newPath.equals("..")) {
			dir = new File(currentDirectory).getParentFile();
		} else if (newPath.equals(".")) {
			dir = new File(currentDirectory);
		} else if (new File(newPath).isAbsolute()) {
			dir = new File(newPath);
		} else {
			dir = new File(currentDirectory, newPath);
		}

		if (dir != null && dir.exists() && dir.isDirectory()) {
			currentDirectory = dir.getAbsolutePath();
			updateDirPathLabel();

			if (output) {
				appendToTerminal("Changed directory to: " + currentDirectory + "\n", Theme.invertColorIfDarkThemeSet(Color.decode("#006400")), false);
				appendPrompt();
			}
		} else {
			if (output) {
				appendToTerminal("Directory not found: " + newPath + "\n", Color.RED, true);
				appendPrompt();
			}
		}
	}

	private void setCommandText(String text) {
		commandTextField.setText(text);
	}

	private void updateDirPathLabel() {
		SwingUtilities.invokeLater(() -> {
			try {
				File dir = new File(currentDirectory);
				String name = dir.getName();
				if (name.isEmpty()) name = dir.getAbsolutePath();
				dirPathLabel.setText(name + " >>");
			} catch (Exception ex) {
				dirPathLabel.setText(currentDirectory + " >>");
			}
		});
	}
}