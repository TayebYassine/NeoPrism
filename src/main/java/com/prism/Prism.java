package com.prism;

import com.prism.components.definition.ComponentType;
import com.prism.components.definition.ConfigKey;
import com.prism.components.definition.Language;
import com.prism.components.definition.PrismFile;
import com.prism.components.extended.JClosableComponent;
import com.prism.components.frames.ErrorDialog;
import com.prism.components.frames.LoadingFrame;
import com.prism.components.frames.WarningDialog;
import com.prism.components.menus.PrismMenuBar;
import com.prism.components.menus.SearchAndReplace;
import com.prism.components.sidebar.LowerSidebar;
import com.prism.components.sidebar.Sidebar;
import com.prism.components.sidebar.panels.*;
import com.prism.components.terminal.TerminalTabbedPane;
import com.prism.components.textarea.TextArea;
import com.prism.components.textarea.TextAreaTabbedPane;
import com.prism.components.toolbar.*;
import com.prism.config.Config;
import com.prism.managers.*;
import com.prism.plugins.PluginLoader;
import com.prism.services.Service;
import com.prism.utils.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Prism extends JFrame {
	private static final String VERSION = "1.0.2";
	private static final String BUILD_INFO = "November 24th, 2025 11:17 PM";
	private static final boolean SNAPSHOT = false;

	private static final String WINDOW_TITLE = "Prism";
	private static final String CONFIG_FILE = "config.properties";
	private static final String PLUGINS_DIR = "plugins";
	private static final int DEFAULT_WINDOW_WIDTH = 900;
	private static final int DEFAULT_WINDOW_HEIGHT = 600;
	private static final int DEFAULT_PRIMARY_DIVIDER = 250;
	private static final int DEFAULT_SECONDARY_DIVIDER = 350;
	private static final int LOADING_FRAME_DELAY_MS = 3000;
	private static final int HORIZONTAL_STRUT_SIZE = 8;
	private static final int STATUS_BAR_BORDER = 3;
	private static final int STATUS_BAR_BORDER_SIDE = 5;
	private static final int DIVIDER_SIZE = 5;
	private static final int ICON_SIZE = 16;
	private static final int DEFAULT_TEXTAREA_ZOOM = 12;
	private static final double PRIMARY_SPLIT_PANE_RESIZE_WEIGHT = 0.3;
	private static final double SECONDARY_SPLIT_PANE_RESIZE_WEIGHT = 0.75;
	public static Prism INSTANCE;
	public final List<JClosableComponent> REMOVED_COMPONENTS = new ArrayList<>();
	private final LoadingFrame loadingFrame;
	private final PrismMenuBar menuBar;
	private final PrimaryToolbar primaryToolbar;
	private final Map<String, JLabel> statusWidgets = new HashMap<>();   // <id, label>
	private Config config;
	private LanguageIntf languageInterface;
	private PluginLoader pluginLoader;
	private TextAreaTabbedPane textAreaTabbedPane;
	private TerminalTabbedPane terminalTabbedPane;
	private TerminalToolbar terminalToolbar;
	private FileExplorer fileExplorer;
	private CodeFoldingPanel codeFoldingPanel;
	private Bookmarks bookmarks;
	private BookmarksToolbar bookmarksToolbar;
	private TasksList tasksList;
	private TasksToolbar tasksToolbar;
	private Database database;
	private DatabaseToolbar databaseToolbar;
	private Problems problems;
	private Sidebar sidebar;
	private JLabel sidebarHeader;
	private LowerSidebar lowerSidebar;
	private JLabel lowerSidebarHeader;
	private PluginsPanel pluginsPanel;
	private SymbolsPanel symbolsPanel;
	private JSplitPane primarySplitPane;
	private JSplitPane secondarySplitPane;
	private JClosableComponent sidebarClosableComponent;
	private JClosableComponent lowerSidebarClosableComponent;
	private SearchAndReplace searchAndReplace;
	private JPanel searchAndReplaceAndStatusBarPanel;
	private JPanel statusBarPanel;
	private JLabel sbLanguage = new JLabel();
	private JLabel sbLength = new JLabel();
	private JLabel sbLines = new JLabel();
	private JLabel sbPosition = new JLabel();
	private JLabel sbZoom = new JLabel();
	private JLabel sbEncoding = new JLabel();

	private JProgressBar heapProgressBar;
	private Timer heapTimer;
	private boolean isHeapUpdateInProgress = false;

	public Prism(String[] args) {
		Prism.INSTANCE = this;
		initializeConfiguration();
		initializeLanguageInterface();

		initializeFrame();

		initializePlugins();

		loadingFrame = new LoadingFrame();

		if (isSnapshot()) {
			Windows.sendNotification(languageInterface.get(223), languageInterface.get(224), TrayIcon.MessageType.WARNING);
		}

		loadingFrame.setVisible(true);

		configureWindowState();
		configureRootDirectory();
		addWindowCloseListener();

		menuBar = new PrismMenuBar();
		setJMenuBar(menuBar);

		primaryToolbar = new PrimaryToolbar();
		add(primaryToolbar, BorderLayout.NORTH);

		initializeComponents();
		setupStatusBar();
		startHeapTimer();
		setupSplitPanes();
		loadDefaultResources();
	}

	public static void main(String[] args) {
		try {
			String lafClass = EarlyThemeSniffer.getLafClassName();
			UIManager.setLookAndFeel(lafClass);
		} catch (Exception _) {

		}

		System.setProperty("sun.java2d.d3d", "true");

		SwingUtilities.invokeLater(() -> {
			try {
				Prism prism = new Prism(args);

				setTimeout(() -> {
					if (prism.loadingFrame.isDisplayable()) {
						prism.loadingFrame.dispose();
					}

					prism.setVisible(true);

					prism.updateStatusBar();

					SwingUtilities.invokeLater(() -> {
						prism.initializeSplitPaneLocations();
						prism.initializeSidebarsView();
						prism.initializeLastModifications();
					});
				}, LOADING_FRAME_DELAY_MS);
			} catch (Exception e) {
				ErrorDialog.showErrorDialog(getInstance(), e);
			}
		});
	}

	public static Prism getInstance() {
		return INSTANCE;
	}

	public static String getVersion() {
		return VERSION;
	}

	public static String getBuildInfo() {
		return BUILD_INFO;
	}

	public static boolean isSnapshot() {
		return SNAPSHOT;
	}

	public static void setTimeout(Runnable runnable, int delay) {
		new Thread(() -> {
			try {
				Thread.sleep(delay);
				runnable.run();
			} catch (Exception e) {
				WarningDialog.showWarningDialog(getInstance(), e);
			}
		}).start();
	}

	private void initializeFrame() {
		setTitle(WINDOW_TITLE);
		setSize(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setIconImages(ResourceUtil.getAppIcon());
		Theme.setupFromConfig();
	}

	private void initializeConfiguration() {
		config = new Config(new File(CONFIG_FILE));
		try {
			config.load();
		} catch (Exception e) {
			ErrorDialog.showErrorDialog(this, e);
		}
	}

	private void initializeLanguageInterface() {
		try {
			Language lang = Language.fromId(config.getInt(ConfigKey.LANGUAGE, 0));

			if (lang == null) {
				throw new Exception("Language not found");
			}

			languageInterface = new LanguageIntf(lang.getPath());
		} catch (Exception e) {
			ErrorDialog.showErrorDialog(this, e);
		}
	}

	private void initializePlugins() {
		pluginLoader = new PluginLoader(new File(PLUGINS_DIR));
		pluginLoader.loadPlugins();
	}

	private void configureWindowState() {
		if (config.getBoolean(ConfigKey.WINDOW_EXTENDED, true)) {
			setExtendedState(MAXIMIZED_BOTH);
		} else {
			setExtendedState(NORMAL);
			setSize(new Dimension(
					config.getInt(ConfigKey.WINDOW_WIDTH, DEFAULT_WINDOW_WIDTH),
					config.getInt(ConfigKey.WINDOW_HEIGHT, DEFAULT_WINDOW_HEIGHT)));
			setLocation(
					config.getInt(ConfigKey.WINDOW_POSITION_X, (int) getLocation().getX()),
					config.getInt(ConfigKey.WINDOW_POSITION_Y, (int) getLocation().getY()));
		}
	}

	private void configureRootDirectory() {
		if (config.getString(ConfigKey.ROOT_DIRECTORY_PATH) == null) {
			config.set(ConfigKey.ROOT_DIRECTORY_PATH, System.getProperty("user.home"));
		}
		File rootDir = new File(config.getString(ConfigKey.ROOT_DIRECTORY_PATH));
		if (!rootDir.exists() || !rootDir.isDirectory()) {
			rootDir = new File(System.getProperty("user.home"));
			config.set(ConfigKey.ROOT_DIRECTORY_PATH, rootDir.getAbsolutePath());
		}
		FileManager.setRootDirectory(rootDir);
	}

	private void addWindowCloseListener() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				prepareClosing();
				System.exit(0);
			}
		});
	}

	public void prepareClosing() {
		saveWindowState();
		saveRecentFiles();
		saveSplitPaneLocations();
		saveComponentStates();
		saveDatabaseState();
	}

	private void saveWindowState() {
		config.set(ConfigKey.WINDOW_EXTENDED, (getExtendedState() & MAXIMIZED_BOTH) == MAXIMIZED_BOTH);
		if (!config.getBoolean(ConfigKey.WINDOW_EXTENDED, true)) {
			Dimension size = getSize();
			config.set(ConfigKey.WINDOW_WIDTH, size.width);
			config.set(ConfigKey.WINDOW_HEIGHT, size.height);
			config.set(ConfigKey.WINDOW_POSITION_X, (int) getLocation().getX());
			config.set(ConfigKey.WINDOW_POSITION_Y, (int) getLocation().getY());
		}
	}

	private void saveRecentFiles() {
		List<String> recentFiles = new ArrayList<>();
		for (PrismFile pf : FileManager.files) {
			if (pf.getAbsolutePath() != null) {
				recentFiles.add(pf.getAbsolutePath());
			}
		}

		config.set(ConfigKey.RECENT_OPENED_FILES, recentFiles.toArray(new String[0]));
	}

	private void saveSplitPaneLocations() {
		config.set(ConfigKey.PRIMARY_SPLITPANE_DIVIDER_LOCATION, primarySplitPane.getDividerLocation());
		config.set(ConfigKey.SECONDARY_SPLITPANE_DIVIDER_LOCATION, secondarySplitPane.getDividerLocation());
	}

	private void saveComponentStates() {
		config.set(ConfigKey.SIDEBAR_CLOSABLE_COMPONENT_CLOSED, sidebarClosableComponent.isClosed());
		config.set(ConfigKey.LOWER_SIDEBAR_CLOSABLE_COMPONENT_CLOSED, lowerSidebarClosableComponent.isClosed());

		config.set(ConfigKey.LAST_SELECTED_TEXT_AREA_TAB_INDEX, textAreaTabbedPane.getSelectedIndex());
	}

	private void saveDatabaseState() {
		config.set(ConfigKey.DATABASE_SQL_EDITOR_TEXT, database.getSqlEditor().getText());
	}

	private void initializeComponents() {
		textAreaTabbedPane = new TextAreaTabbedPane();
		File directory = new File(config.getString(ConfigKey.ROOT_DIRECTORY_PATH));

		JPanel terminalArea = initializeTerminalPanel();
		JPanel bookmarksArea = initializeBookmarksPanel();
		JPanel tasksArea = initializeTasksPanel();
		JPanel databaseArea = initializeDatabasePanel();
		JPanel problemsArea = initializeProblemsPanel();

		initializeSidebars(directory, terminalArea, bookmarksArea, tasksArea, databaseArea, problemsArea);
		initializeCentralArea();
	}

	private JPanel initializeTerminalPanel() {
		JPanel terminalArea = new JPanel(new BorderLayout());
		terminalToolbar = new TerminalToolbar(this);
		terminalTabbedPane = new TerminalTabbedPane();
		terminalArea.add(terminalToolbar, BorderLayout.NORTH);
		terminalArea.add(terminalTabbedPane, BorderLayout.CENTER);
		return terminalArea;
	}

	private JPanel initializeBookmarksPanel() {
		JPanel bookmarksArea = new JPanel(new BorderLayout());
		bookmarksToolbar = new BookmarksToolbar(this);
		bookmarks = new Bookmarks();
		bookmarksArea.add(bookmarksToolbar, BorderLayout.NORTH);
		bookmarksArea.add(bookmarks, BorderLayout.CENTER);
		return bookmarksArea;
	}

	private JPanel initializeTasksPanel() {
		JPanel tasksArea = new JPanel(new BorderLayout());
		tasksToolbar = new TasksToolbar(this);
		tasksList = new TasksList();
		tasksArea.add(tasksToolbar, BorderLayout.NORTH);
		tasksArea.add(tasksList, BorderLayout.CENTER);
		return tasksArea;
	}

	private JPanel initializeDatabasePanel() {
		JPanel databaseArea = new JPanel(new BorderLayout());
		databaseToolbar = new DatabaseToolbar(this);
		database = new Database();
		databaseArea.add(databaseToolbar, BorderLayout.NORTH);
		databaseArea.add(database, BorderLayout.CENTER);
		return databaseArea;
	}

	private JPanel initializeProblemsPanel() {
		JPanel problemsArea = new JPanel(new BorderLayout());
		problems = new Problems();
		problemsArea.add(problems, BorderLayout.CENTER);
		return problemsArea;
	}

	private void initializeSidebars(File directory, JPanel terminalArea, JPanel bookmarksArea,
									JPanel tasksArea, JPanel databaseArea, JPanel problemsArea) {
		fileExplorer = new FileExplorer(directory);
		codeFoldingPanel = new CodeFoldingPanel();
		pluginsPanel = new PluginsPanel(pluginLoader.getPlugins());
		symbolsPanel = new SymbolsPanel();

		lowerSidebarHeader = new JLabel(languageInterface.get(209));
		lowerSidebar = new LowerSidebar(lowerSidebarHeader, terminalArea, bookmarksArea, tasksArea, databaseArea, problemsArea);
		lowerSidebarClosableComponent = new JClosableComponent(ComponentType.LOWER_SIDEBAR, lowerSidebarHeader, lowerSidebar);

		sidebarHeader = new JLabel(languageInterface.get(33));
		sidebar = new Sidebar(sidebarHeader, fileExplorer, codeFoldingPanel, symbolsPanel, pluginsPanel);
		sidebarClosableComponent = new JClosableComponent(ComponentType.SIDEBAR, sidebarHeader, sidebar);
	}

	private void initializeSidebarsView() {
		if (config.getBoolean(ConfigKey.LOWER_SIDEBAR_CLOSABLE_COMPONENT_CLOSED, false)) {
			lowerSidebarClosableComponent.closeComponent();
		}

		if (config.getBoolean(ConfigKey.SIDEBAR_CLOSABLE_COMPONENT_CLOSED, false)) {
			sidebarClosableComponent.closeComponent();
		}
	}

	private void initializeSplitPaneLocations() {
		int pri = config.getInt(ConfigKey.PRIMARY_SPLITPANE_DIVIDER_LOCATION, DEFAULT_PRIMARY_DIVIDER);
		int sec = config.getInt(ConfigKey.SECONDARY_SPLITPANE_DIVIDER_LOCATION, DEFAULT_SECONDARY_DIVIDER);
		primarySplitPane.setDividerLocation(pri);
		secondarySplitPane.setDividerLocation(sec);
	}

	private void initializeLastModifications() {
		textAreaTabbedPane.redirectUserToTab(config.getInt(ConfigKey.LAST_SELECTED_TEXT_AREA_TAB_INDEX, 0), true);
	}

	private void initializeCentralArea() {
		secondarySplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textAreaTabbedPane, lowerSidebarClosableComponent);
		secondarySplitPane.setResizeWeight(SECONDARY_SPLIT_PANE_RESIZE_WEIGHT);

		primarySplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarClosableComponent, secondarySplitPane);
		primarySplitPane.setResizeWeight(PRIMARY_SPLIT_PANE_RESIZE_WEIGHT);

		add(primarySplitPane);

		textAreaTabbedPane.openHomepageIfAllClosed();
		terminalTabbedPane.openNewTerminalIfAllTabsAreClosed();
	}

	private void setupSplitPanes() {
		secondarySplitPane.setResizeWeight(SECONDARY_SPLIT_PANE_RESIZE_WEIGHT);
		primarySplitPane.setResizeWeight(PRIMARY_SPLIT_PANE_RESIZE_WEIGHT);
	}

	private void loadDefaultResources() {
		ToolsManager.loadTools();
		DatabaseManager.loadDatabases();
		TasksManager.loadTasks();

		if (config.getBoolean(ConfigKey.OPEN_RECENT_FILES, true)) {
			FileManager.openRecentFiles();
		}

		bookmarks.updateTreeData(TextAreaManager.getBookmarksOfAllFiles());

		int tasks = TasksManager.getAllTasks().size();

		if (tasks > 0 && getConfig().getBoolean(ConfigKey.TASKS_NOTIFICATION_ENABLED, true)) {
			Windows.sendNotification(languageInterface.get(221), languageInterface.get(222, tasks), TrayIcon.MessageType.INFO);
		}
	}

	private void setupStatusBar() {
		searchAndReplaceAndStatusBarPanel = new JPanel(new BorderLayout());
		searchAndReplace = new SearchAndReplace();

		/* ----  bar container  ---- */
		statusBarPanel = new JPanel();
		statusBarPanel.setLayout(new BoxLayout(statusBarPanel, BoxLayout.X_AXIS));
		statusBarPanel.setOpaque(false);                       // <- important
		statusBarPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")),
				BorderFactory.createEmptyBorder(2, 0, 2, 0)));

		/* ----  helper  ---- */
		java.util.function.BiFunction<String, String, JLabel> factory = (text, id) -> {
			JLabel l = new JLabel(text);
			l.setFont(l.getFont().deriveFont(Font.PLAIN, 11));
			l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
			l.setOpaque(false);                                // <- important
			statusWidgets.put(id, l);
			return l;
		};

		/* ----  left  ---- */
		JPanel left = createSectionPanel();
		left.add(factory.apply("UTF-8", "ENC"));
		left.add(createSeparator());
		left.add(factory.apply("Windows (CR LF)", "EOL"));
		left.add(createSeparator());
		left.add(factory.apply("UTF-8", "ENC"));

		/* ----  middle  ---- */
		JPanel mid = createSectionPanel();
		mid.add(factory.apply(languageInterface.get(3, 0, 0), "POS"));
		mid.add(createSeparator());
		mid.add(factory.apply(languageInterface.get(4, 0, 0), "SEL"));
		mid.add(createSeparator());
		mid.add(factory.apply(languageInterface.get(1, 0), "LEN"));

		/* ----  right  ---- */
		JPanel right = createSectionPanel();

		heapProgressBar = new JProgressBar(0, 100);
		heapProgressBar.setStringPainted(true);
		heapProgressBar.setPreferredSize(new Dimension(80, 16));
		heapProgressBar.setMaximumSize(new Dimension(80, 16));
		heapProgressBar.setFont(heapProgressBar.getFont().deriveFont(Font.PLAIN, 11));

		right.add(heapProgressBar);
		right.add(createSeparator());
		right.add(factory.apply("100%", "ZOOM"));

		statusBarPanel.add(left);
		statusBarPanel.add(Box.createHorizontalGlue());
		statusBarPanel.add(mid);
		statusBarPanel.add(Box.createHorizontalGlue());
		statusBarPanel.add(right);

		/* ----  assembly  ---- */
		searchAndReplaceAndStatusBarPanel.add(searchAndReplace, BorderLayout.NORTH);
		searchAndReplaceAndStatusBarPanel.add(statusBarPanel, BorderLayout.CENTER);

		add(searchAndReplaceAndStatusBarPanel, BorderLayout.SOUTH);
	}

	private JPanel createSectionPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.setOpaque(false);
		return p;
	}

	private JLabel createSeparator() {
		JLabel s = new JLabel(" | ");
		s.setForeground(UIManager.getColor("Separator.foreground"));
		s.setFont(s.getFont().deriveFont(Font.PLAIN, 11));
		return s;
	}

	private void updateHeapProgressBar() {
		Runtime runtime = Runtime.getRuntime();
		long total = runtime.totalMemory();
		long free = runtime.freeMemory();
		long used = total - free;
		int percent = (int) ((used * 100) / total);

		SwingUtilities.invokeLater(() -> {
			heapProgressBar.setValue(percent);
			heapProgressBar.setString(used / 1024 / 1024 + "M / " + total / 1024 / 1024 + "M");
		});
	}

	// Updated updateStatusBar method
	public void updateStatusBar() {
		if (statusWidgets.isEmpty()) return;

		PrismFile file = textAreaTabbedPane.getCurrentFile();
		if (file == null) {
			statusWidgets.get("ENC").setText("UTF-8");
			statusWidgets.get("EOL").setText("Windows (CR LF)");
			statusWidgets.get("POS").setText(languageInterface.get(3, 0, 0));
			statusWidgets.get("SEL").setText(languageInterface.get(4, 0, 0));
			statusWidgets.get("LEN").setText(languageInterface.get(1, 0));
			statusWidgets.get("ZOOM").setText("100%");
			return;
		}

		if (file.isImage()) {                         // image file
			statusWidgets.get("ENC").setText("Image");
			statusWidgets.get("EOL").setText("Unknown");
			statusWidgets.get("POS").setText(languageInterface.get(3, "?", "?"));
			statusWidgets.get("SEL").setText(languageInterface.get(4, "?", "?"));
			statusWidgets.get("LEN").setText(languageInterface.get(1, "?"));
			statusWidgets.get("ZOOM").setText("?");
			return;
		}

		TextArea ta = file.getTextArea();
		if (ta == null) return;

		/* ----  collect data  ---- */
		int caret = ta.getCaretPosition();
		int line = 1, col = 1;
		try {
			line = ta.getLineOfOffset(caret) + 1;
			col = caret - ta.getLineStartOffset(line - 1) + 1;
		} catch (BadLocationException ignore) {
		}

		String text = ta.getText();
		int len = text.length();
		int lineCount = text.isEmpty() ? 1 : text.split("\n", -1).length;

		int selLen = ta.getSelectionEnd() - ta.getSelectionStart();
		int selLines = 0;

		try {
			if (selLen > 0) {
				String selText = ta.getSelectedText();
				selLines = selText == null ? 0 : selText.split("\n", -1).length;
			}
		} catch (Exception _) {

		}

		String lang = Languages.getFullName(file.getFile());
		if (lang == null || lang.isEmpty()) lang = "Plain Text";

		int zoom = getConfig().getInt(ConfigKey.TEXTAREA_ZOOM, DEFAULT_TEXTAREA_ZOOM);

		String eol = detectEOLFormat(text);

		/* ----  push to widgets  ---- */
		statusWidgets.get("ENC").setText("UTF-8");
		statusWidgets.get("EOL").setText(eol);
		statusWidgets.get("POS").setText(languageInterface.get(3, line, col));
		statusWidgets.get("SEL").setText(languageInterface.get(4, selLen, selLines));
		statusWidgets.get("LEN").setText(languageInterface.get(1, String.format("%,d", len)));
		statusWidgets.get("ZOOM").setText(zoom + "%");

		/* ----  language name in the bar  ---- */
		statusWidgets.get("ENC").setText(lang);   // we reuse the first label for language
	}

	private String detectEOLFormat(String text) {
		if (text.contains("\r\n")) return "Windows (CR LF)";
		if (text.contains("\r")) return "Mac (CR)";
		if (text.contains("\n")) return "Unix (LF)";
		return "Windows (CR LF)";
	}

	private void startHeapTimer() {
		heapTimer = new Timer(3_000, e -> {
			if (!isHeapUpdateInProgress) {
				isHeapUpdateInProgress = true;
				updateHeapProgressBar();
				isHeapUpdateInProgress = false;
			}
		});

		heapTimer.start();
	}

	public void addBackComponent(ComponentType type) {
		JClosableComponent component = findAndRemoveComponent(type);

		if (component != null) {
			restoreComponent(component, type);
		}
	}

	public boolean isComponentRemoved(ComponentType type) {
		for (JClosableComponent component : REMOVED_COMPONENTS) {
			if (component != null && component.getType() == type) {
				return true;
			}
		}

		return false;
	}

	private JClosableComponent findAndRemoveComponent(ComponentType type) {
		for (int i = 0; i < REMOVED_COMPONENTS.size(); i++) {
			JClosableComponent component = REMOVED_COMPONENTS.get(i);

			if (component.getType() == type) {
				REMOVED_COMPONENTS.remove(i);

				return component;
			}
		}

		return null;
	}

	private void restoreComponent(JClosableComponent component, ComponentType type) {
		switch (type) {
			case LOWER_SIDEBAR -> restoreLowerSidebar(component);
			case SIDEBAR -> restoreSidebar(component);
		}
	}

	private void restoreLowerSidebar(JClosableComponent component) {
		secondarySplitPane.add(component, JSplitPane.RIGHT);
		secondarySplitPane.revalidate();
		secondarySplitPane.repaint();
		secondarySplitPane.setDividerSize(DIVIDER_SIZE);
		secondarySplitPane.setDividerLocation(
				config.getInt(ConfigKey.SECONDARY_SPLITPANE_DIVIDER_LOCATION, DEFAULT_SECONDARY_DIVIDER));
		secondarySplitPane.setResizeWeight(SECONDARY_SPLIT_PANE_RESIZE_WEIGHT);
	}

	private void restoreSidebar(JClosableComponent component) {
		primarySplitPane.add(component, JSplitPane.LEFT);
		primarySplitPane.revalidate();
		primarySplitPane.repaint();
		primarySplitPane.setDividerSize(DIVIDER_SIZE);
		primarySplitPane.setDividerLocation(
				config.getInt(ConfigKey.PRIMARY_SPLITPANE_DIVIDER_LOCATION, DEFAULT_PRIMARY_DIVIDER));
		primarySplitPane.setResizeWeight(PRIMARY_SPLIT_PANE_RESIZE_WEIGHT);
	}

	public void updateWindowTitle(PrismFile pf) {
		setTitle("Prism (" + Prism.getVersion() + ") - " + pf.getName() + ((!pf.isSaved() && !config.getBoolean(ConfigKey.AUTO_SAVE, true)) ? "*" : ""));
		textAreaTabbedPane.updateTabTitle(pf);
	}

	public void updateComponents(PrismFile pf) {
		primaryToolbar.updateComponent();
		menuBar.updateComponent();
		problems.updateTreeData();

		if (pf != null && pf.isText()) {
			if (codeFoldingPanel.getTextArea() != null && codeFoldingPanel.getTextArea().equals(pf.getTextArea())) {
				codeFoldingPanel.updateTree();
			} else {
				codeFoldingPanel.setFile(pf);
			}

			if (pf.getAbsolutePath() != null) {
				Service service = Languages.getService(pf.getFile());

				if (service != null) {
					service.updateSymbolsTree(pf, pf.getTextArea());
				}
			}
		}
	}

	public Config getConfig() {
		return config;
	}

	public LanguageIntf getLanguage() {
		return languageInterface;
	}

	public Sidebar getSidebar() {
		return sidebar;
	}

	public LowerSidebar getLowerSidebar() {
		return lowerSidebar;
	}

	public FileExplorer getFileExplorer() {
		return fileExplorer;
	}

	public TextAreaTabbedPane getTextAreaTabbedPane() {
		return textAreaTabbedPane;
	}

	public Bookmarks getBookmarks() {
		return bookmarks;
	}

	public Problems getProblems() {
		return problems;
	}

	public CodeFoldingPanel getCodeFoldingPanel() {
		return codeFoldingPanel;
	}

	public SymbolsPanel getSymbolsPanel() {
		return symbolsPanel;
	}

	public TasksList getTasksList() {
		return tasksList;
	}

	public PrimaryToolbar getPrimaryToolbar() {
		return primaryToolbar;
	}

	public PrismMenuBar getPrismMenuBar() {
		return menuBar;
	}

	public JSplitPane getPrimarySplitPane() {
		return primarySplitPane;
	}

	public JSplitPane getSecondarySplitPane() {
		return secondarySplitPane;
	}

	public PluginLoader getPluginLoader() {
		return pluginLoader;
	}

	public TerminalTabbedPane getTerminalTabbedPane() {
		return terminalTabbedPane;
	}

	public TerminalToolbar getTerminalToolbar() {
		return terminalToolbar;
	}

	public Database getDatabase() {
		return database;
	}

	public DatabaseToolbar getDatabaseToolbar() {
		return databaseToolbar;
	}

	public final class EarlyThemeSniffer {
		public static String getLafClassName() {
			File cfg = new File("config.properties");
			if (!cfg.exists()) {
				return "com.formdev.flatlaf.FlatLightLaf";
			}

			try (BufferedReader br = new BufferedReader(new FileReader(cfg))) {
				String line;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.startsWith("39=")) {
						String val = line.substring(3).trim();
						if ("2".equals(val)) {
							return "com.formdev.flatlaf.FlatDarculaLaf";
						}
						break;
					}
				}
			} catch (Exception _) {
			}

			return "com.formdev.flatlaf.FlatLightLaf";
		}
	}
}
