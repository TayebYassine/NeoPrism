package com.prism.services;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.definition.PrismFile;
import com.prism.components.frames.WarningDialog;
import com.prism.components.terminal.Terminal;
import com.prism.components.textarea.TextArea;
import com.prism.managers.FileManager;
import com.prism.managers.ThreadsManager;
import com.prism.services.syntaxchecker.JavaSyntaxChecker;
import com.prism.utils.AStyleWrapper;
import com.prism.utils.CtagsWrapper;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ServiceForJava extends Service {
	private static final Prism prism = Prism.getInstance();

	public ServiceForJava() {
		PrismFile pf = prism.getTextAreaTabbedPane().getCurrentFile();
		File file = pf.getFile();

		JMenuItem buildAndRunExternalThreadItem = new JMenuItem("Java: Build & Run (External Thread)");
		buildAndRunExternalThreadItem.addActionListener(e -> {
			FileManager.saveFile(pf);

			buildAndRunExternalThreadFile(file);
		});

		JMenuItem buildAndRunItem = new JMenuItem("Java: Build & Run");
		buildAndRunItem.addActionListener(e -> {
			FileManager.saveFile(pf);

			buildAndRunInternalThreadFile(file);
		});

		JMenuItem formatSourceWithGoogleItem = new JMenuItem("Format Source Code (Google)");
		formatSourceWithGoogleItem.addActionListener(e -> {
			formatSourceCodeGoogle(pf);
		});

		JMenuItem formatSourceWithAStyleItem = new JMenuItem("Format Source Code (AStyle)");
		formatSourceWithAStyleItem.addActionListener(e -> {
			formatSourceCodeAStyle(pf);
		});

		add(buildAndRunExternalThreadItem);
		add(buildAndRunItem);
		addSeparator();
		add(formatSourceWithGoogleItem);
		add(formatSourceWithAStyleItem);
	}

	@Override
	public ImageIcon getIconOfCodeFoldingLine(String line) {
		line = line.trim();

		if (line.matches("^(public\\s+|protected\\s+|private\\s+)?(static\\s+)?(final\\s+)?(class|interface|enum|record|@interface)\\s+\\w+.*"))
			return ResourceUtil.getIconFromSVG("icons/ui/symbol-type.svg", 16, 16);

		if (line.matches("^(public\\s+|protected\\s+|private\\s+)?(static\\s+)?(final\\s+|abstract\\s+)?\\w+[\\<\\>\\w\\s]*\\s+\\w+\\s*\\(.*"))
			return ResourceUtil.getIconFromSVG("icons/ui/symbol-function.svg", 16, 16);

		if (line.matches("^(public\\s+|protected\\s+|private\\s+)?(static\\s+)?(final\\s+)?\\w+[\\<\\>\\w\\s]*\\s+\\w+\\s*(=|;).*"))
			return ResourceUtil.getIconFromSVG("icons/ui/symbol-field.svg", 16, 16);

		return ResourceUtil.getIconFromSVG("icons/ui/symbol-keyword.svg", 16, 16);
	}

	@Override
	public boolean createNewProject(File projectDir) {
		File mainFile = new File(projectDir, "Main.java");

		if (!mainFile.exists()) {
			try {
				mainFile.createNewFile();
			} catch (IOException e) {
				return false;
			}
		}

		try (FileWriter fw = new FileWriter(mainFile)) {
			fw.write(getSample());

			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public String getSample() {
		return """
				public class Main {
				    public static void main(String[] args) {
				        System.out.println("Hello World!");
				    }
				}
				""";
	}

	@Override
	public void installSyntaxChecker(PrismFile pf, TextArea textArea) {
		if (!(prism.getConfig().getBoolean(ConfigKey.ALLOW_SERVICES, true) && prism.getConfig().getBoolean(ConfigKey.ALLOW_SERVICE_SYNTAX_CHECKER, true))) {
			return;
		}

		JavaSyntaxChecker.install(pf, textArea);
	}

	@Override
	public void updateSymbolsTree(PrismFile pf, TextArea textArea) {
		File ctagsFile = new File("Ctags/ctags.exe");

		if (ctagsFile == null || !ctagsFile.exists() || !ctagsFile.isFile()) {
			JOptionPane.showMessageDialog(prism, prism.getLanguage().get(238), prism.getLanguage().get(10002), JOptionPane.ERROR_MESSAGE);
			return;
		}

		String[] opts = {"--language-force=Java", "--kinds-Java=*"};

		CtagsWrapper.extractSymbolsAsync(
				pf.getFile(),
				ctagsFile.getAbsolutePath(),
				opts,
				new CtagsWrapper.Callback() {
					@Override
					public void onSuccess(Map<String, List<String>> kindToSymbols) {
						prism.getSymbolsPanel().updateTree(kindToSymbols);
					}

					@Override
					public void onError(String message) {
						new WarningDialog(prism, new Error(message));
					}
				});
	}

	private void buildAndRunExternalThreadFile(File file) {
		File dir = file.getParentFile();
		String base = file.getName().replaceFirst("[.][^.]+$", "");

		String cmdLine = String.format(
				"cmd /c start \"Running %s\" cmd /c \"(javac \"%s.java\" && java \"%s\") & pause & exit\"",
				base, base, base);

		ThreadsManager.submitAndTrackThread("Java Build " + file.getName(), () -> {
			try {
				new ProcessBuilder("cmd", "/c", cmdLine)
						.directory(dir)
						.inheritIO()
						.start();
			} catch (Exception ex) {
				new WarningDialog(prism, ex);
			}
		});
	}

	private void buildAndRunInternalThreadFile(File file) {
		Terminal terminal = prism.getTerminalTabbedPane().getCurrentTerminal();

		if (terminal == null) {
			return;
		}

		prism.getLowerSidebar().setSelectedIndex(1);

		terminal.closeProcess();

		String base = file.getName().replaceFirst("[.][^.]+$", "");

		String cmdLine = String.format(
				"javac \"%s.java\" && java \"%s\"",
				base, base);

		terminal.executeCommandSync(cmdLine);
	}

	private void formatSourceCodeAStyle(PrismFile pf) {
		File aStyleFile = new File("AStyle/astyle.exe");

		if (aStyleFile == null || !aStyleFile.exists() || !aStyleFile.isFile()) {
			JOptionPane.showMessageDialog(prism, prism.getLanguage().get(228), prism.getLanguage().get(10002), JOptionPane.ERROR_MESSAGE);
			return;
		}

		AStyleWrapper.formatFileAsync(pf.getFile(), aStyleFile.getAbsolutePath(),
				String.format("--style=java --indent=spaces=%d",
						prism.getConfig().getInt(ConfigKey.TAB_SIZE, 4)
				),
				new AStyleWrapper.Callback() {
					@Override
					public void onSuccess(String formattedText) {
						pf.getTextArea().replace(formattedText, true);
					}

					@Override
					public void onError(String message) {
						new WarningDialog(prism, new Error(message));
					}
				});
	}

	private void formatSourceCodeGoogle(PrismFile pf) {
		Formatter formatter = new Formatter(
				JavaFormatterOptions.builder()
						.style(JavaFormatterOptions.Style.AOSP)
						.formatJavadoc(true)
						.build());

		String pretty = null;

		try {
			pretty = formatter.formatSource(pf.getTextArea().getText());
		} catch (Exception e) {

		}

		if (pretty != null) {
			pf.getTextArea().replace(pretty, true);
		}
	}
}
