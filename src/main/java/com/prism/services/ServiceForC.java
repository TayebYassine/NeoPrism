package com.prism.services;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.definition.PrismFile;
import com.prism.components.frames.WarningDialog;
import com.prism.components.terminal.Terminal;
import com.prism.components.textarea.TextArea;
import com.prism.managers.FileManager;
import com.prism.managers.ThreadsManager;
import com.prism.services.syntaxchecker.CSyntaxChecker;
import com.prism.utils.AStyleWrapper;
import com.prism.utils.CtagsWrapper;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class ServiceForC extends Service {
	private static final Prism prism = Prism.getInstance();

	private long lastUpdateTime = 0;
	private static final long DEBOUNCE_MS = 3000;

	public ServiceForC() {
		PrismFile pf = prism.getTextAreaTabbedPane().getCurrentFile();
		File file = pf.getFile();

		JMenuItem buildItem = new JMenuItem("C: Build");
		buildItem.addActionListener(e -> {
			FileManager.saveFile(pf);

			buildFile(file);
		});

		JMenuItem runItem = new JMenuItem("C: Run");
		runItem.addActionListener(e -> {
			runFile(file);
		});

		JMenuItem buildAndRunItem = new JMenuItem("C: Build & Run");
		buildAndRunItem.addActionListener(e -> {
			FileManager.saveFile(pf);

			buildAndRunFile(file);
		});

		JMenuItem formatSourceWithAStyleItem = new JMenuItem("Format Source Code (AStyle)");
		formatSourceWithAStyleItem.addActionListener(e -> {
			formatSourceCodeAStyle(pf);
		});

		add(buildItem);
		add(runItem);
		add(buildAndRunItem);
		addSeparator();
		add(formatSourceWithAStyleItem);
	}

	@Override
	public ImageIcon getIconOfCodeFoldingLine(String line) {
		line = line.trim();

		if (line.matches("^(typedef\\s+)?(struct|enum)(\\s+\\w+)?\\s*\\{?"))
			return ResourceUtil.getIconFromSVG("icons/ui/symbol-type.svg", 16, 16);

		if (line.matches("^(typedef\\s+)?(union)(\\s+\\w+)?\\s*\\{?"))
			return ResourceUtil.getIconFromSVG("icons/ui/symbol-union.svg", 16, 16);

		if (line.matches("^[\\w\\*\\s]+\\s+\\w+\\s*\\([^)]*\\)\\s*\\{?"))
			return ResourceUtil.getIconFromSVG("icons/ui/symbol-function.svg", 16, 16);

		if (line.matches("^[\\w\\*\\s]+\\s+\\w+(\\[\\d*\\])?\\s*(=|;).*"))
			return ResourceUtil.getIconFromSVG("icons/ui/symbol-field.svg", 16, 16);

		return ResourceUtil.getIconFromSVG("icons/ui/symbol-keyword.svg", 16, 16);
	}

	@Override
	public void installSyntaxChecker(PrismFile pf, TextArea textArea) {
		if (!(prism.getConfig().getBoolean(ConfigKey.ALLOW_SERVICES, true) && prism.getConfig().getBoolean(ConfigKey.ALLOW_SERVICE_SYNTAX_CHECKER, true))) {
			return;
		}

		if (prism.getConfig().getBoolean(ConfigKey.LANGUAGE_C_GNU_GCC_COMPILER_PROVIDED_IN_PATH_ENV, true)) {
			CSyntaxChecker.install(pf, textArea, null);
		} else {
			CSyntaxChecker.install(pf, textArea, Paths.get(prism.getConfig().getString(ConfigKey.LANGUAGE_C_GNU_GCC_COMPILER_PATH, "")));
		}
	}

	@Override
	public void updateSymbolsTree(PrismFile pf, TextArea textArea) {
		long now = System.currentTimeMillis();
		if (now - lastUpdateTime < DEBOUNCE_MS) {
			return;
		}
		lastUpdateTime = now;

		File ctagsFile = new File("Ctags/ctags.exe");

		if (ctagsFile == null || !ctagsFile.exists() || !ctagsFile.isFile()) {
			JOptionPane.showMessageDialog(prism, prism.getLanguage().get(238), prism.getLanguage().get(10002), JOptionPane.ERROR_MESSAGE);
			return;
		}

		String[] opts = prism.getConfig().getString(ConfigKey.LANGUAGE_C_CTAGS_OPTIONS, "--language-force=C --kinds-C=*").split(" ");

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

	private void buildFile(File file) {
		File dir = file.getParentFile();
		String base = file.getName().replaceFirst("[.][^.]+$", "");

		String exePath;

		if (prism.getConfig().getBoolean(ConfigKey.LANGUAGE_C_GNU_GCC_COMPILER_PROVIDED_IN_PATH_ENV, true)) {
			exePath = "gcc";
		} else {
			exePath = Paths.get(prism.getConfig().getString(ConfigKey.LANGUAGE_C_GNU_GCC_COMPILER_PATH, "")).toAbsolutePath().toString();
		}

		String cmdLine = String.format(
				"cmd /c start \"Running %s\" cmd /c \"(%s \"%s\" -o \"%s\") & exit\"",
				base, exePath, file.getName(), base);

		ThreadsManager.submitAndTrackThread("C Build " + file.getName() , () -> {
			try {
				Process p = new ProcessBuilder("cmd", "/c", cmdLine)
						.directory(dir)
						.redirectError(ProcessBuilder.Redirect.DISCARD)
						.redirectOutput(ProcessBuilder.Redirect.DISCARD)
						.start();
				p.waitFor();
				p.destroyForcibly();
			} catch (Exception ex) {
				new WarningDialog(prism, ex);
			}
		});
	}

	private void runFile(File file) {
		File dir = file.getParentFile();
		String base = file.getName().replaceFirst("[.][^.]+$", "");

		String cmdLine = String.format(
				"cmd /c start \"Running %s\" cmd /c \"(\"%s\") & pause & exit\"",
				base, base);

		ThreadsManager.submitAndTrackThread("C Run " + file.getName() , () -> {
			try {
				Process p = new ProcessBuilder("cmd", "/c", cmdLine)
						.directory(dir)
						.redirectError(ProcessBuilder.Redirect.DISCARD)
						.redirectOutput(ProcessBuilder.Redirect.DISCARD)
						.start();
				p.waitFor();
				p.destroyForcibly();
			} catch (Exception ex) {
				new WarningDialog(prism, ex);
			}
		});
	}

	private void buildAndRunFile(File file) {
		File dir = file.getParentFile();
		String base = file.getName().replaceFirst("[.][^.]+$", "");

		String exePath;

		if (prism.getConfig().getBoolean(ConfigKey.LANGUAGE_C_GNU_GCC_COMPILER_PROVIDED_IN_PATH_ENV, true)) {
			exePath = "gcc";
		} else {
			exePath = Paths.get(prism.getConfig().getString(ConfigKey.LANGUAGE_C_GNU_GCC_COMPILER_PATH, "")).toAbsolutePath().toString();
		}

		String cmdLine = String.format(
				"cmd /c start \"Running %s\" cmd /c \"(%s \"%s\" -o \"%s\" && \"%s\") & pause & exit\"",
				base, exePath, file.getName(), base, base);

		ThreadsManager.submitAndTrackThread("C Build and Run " + file.getName() , () -> {
			try {
				Process p = new ProcessBuilder("cmd", "/c", cmdLine)
						.directory(dir)
						.redirectError(ProcessBuilder.Redirect.DISCARD)
						.redirectOutput(ProcessBuilder.Redirect.DISCARD)
						.start();
				p.waitFor();
				p.destroyForcibly();
			} catch (Exception ex) {
				new WarningDialog(prism, ex);
			}
		});
	}

	private void formatSourceCodeAStyle(PrismFile pf) {
		File aStyleFile = new File("AStyle/astyle.exe");

		if (aStyleFile == null || !aStyleFile.exists() || !aStyleFile.isFile()) {
			JOptionPane.showMessageDialog(prism, prism.getLanguage().get(228), prism.getLanguage().get(10002), JOptionPane.ERROR_MESSAGE);
			return;
		}

		AStyleWrapper.formatFileAsync(pf.getFile(), aStyleFile.getAbsolutePath(),
				AStyleWrapper.getOptionsFromConfig("kr"),
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
}
