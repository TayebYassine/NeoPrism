package com.prism.services;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.definition.PrismFile;
import com.prism.components.frames.WarningDialog;
import com.prism.components.terminal.Terminal;
import com.prism.components.textarea.TextArea;
import com.prism.managers.FileManager;
import com.prism.managers.ThreadsManager;
import com.prism.services.syntaxchecker.CPlusPlusSyntaxChecker;
import com.prism.utils.AStyleWrapper;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class ServiceForCPlusPlus extends Service {
	private static final Prism prism = Prism.getInstance();

	public ServiceForCPlusPlus() {
		PrismFile pf = prism.getTextAreaTabbedPane().getCurrentFile();
		File file = pf.getFile();

		JMenuItem buildAndRunExternalThreadItem = new JMenuItem("C++: Build & Run (External Thread)");
		buildAndRunExternalThreadItem.addActionListener(e -> {
			FileManager.saveFile(pf);

			buildAndRunExternalThreadFile(file);
		});

		JMenuItem buildAndRunItem = new JMenuItem("C++: Build & Run");
		buildAndRunItem.addActionListener(e -> {
			FileManager.saveFile(pf);

			buildAndRunInternalThreadFile(file);
		});

		JMenuItem formatSourceWithAStyleItem = new JMenuItem("Format Source Code (AStyle)");
		formatSourceWithAStyleItem.addActionListener(e -> {
			formatSourceCodeAStyle(pf);
		});

		add(buildAndRunExternalThreadItem);
		add(buildAndRunItem);
		addSeparator();
		add(formatSourceWithAStyleItem);
	}

	@Override
	public ImageIcon getIconOfCodeOutlineLine(String line) {
		line = line.trim();

		if (line.matches("^namespace\\s+\\w+.*"))
			return ResourceUtil.getIconFromSVG("icons/ui/symbol-namespace.svg", 16, 16);

		if (line.matches("^(template\\s*<[^>]+>\\s*)?(class|struct|union|enum)(\\s+\\w+)?.*"))
			return ResourceUtil.getIconFromSVG("icons/ui/symbol-type.svg", 16, 16);

		if (line.matches("^[\\w\\*\\s&:<>]+\\s+(\\w+::)?(~?\\w+|operator\\s*[^\\s]+)\\s*\\([^)]*\\)\\s*(const)?\\s*(\\{|=|;)"))
			return ResourceUtil.getIconFromSVG("icons/ui/symbol-function.svg", 16, 16);

		return ResourceUtil.getIconFromSVG("icons/ui/symbol-keyword.svg", 16, 16);
	}

	@Override
	public boolean createNewProject(File projectDir) {
		File mainFile = new File(projectDir, "main.cpp");

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
				#include <iostream>
				
				int main() {
				    std::cout << "Hello World!" << std::endl;
				    return 0;
				}
				""";
	}

	@Override
	public void installSyntaxChecker(PrismFile pf, TextArea textArea) {
		if (!(prism.getConfig().getBoolean(ConfigKey.ALLOW_SERVICES, true) && prism.getConfig().getBoolean(ConfigKey.ALLOW_SERVICE_SYNTAX_CHECKER, true))) {
			return;
		}

		if (prism.getConfig().getBoolean(ConfigKey.LANGUAGE_CPP_GNU_GPP_COMPILER_PROVIDED_IN_PATH_ENV, true)) {
			CPlusPlusSyntaxChecker.install(pf, textArea, null);
		} else {
			CPlusPlusSyntaxChecker.install(pf, textArea, Paths.get(prism.getConfig().getString(ConfigKey.LANGUAGE_CPP_GNU_GPP_COMPILER_PATH, "")));
		}
	}

	private void buildAndRunExternalThreadFile(File file) {
		File dir = file.getParentFile();
		String base = file.getName().replaceFirst("[.][^.]+$", "");

		String exePath;

		if (prism.getConfig().getBoolean(ConfigKey.LANGUAGE_CPP_GNU_GPP_COMPILER_PROVIDED_IN_PATH_ENV, true)) {
			exePath = "g++";
		} else {
			exePath = Paths.get(prism.getConfig().getString(ConfigKey.LANGUAGE_CPP_GNU_GPP_COMPILER_PATH, "")).toAbsolutePath().toString();
		}

		String cmdLine = String.format(
				"cmd /c start \"Running %s\" cmd /c \"(%s \"%s\" -o \"%s\" && \"%s\") & pause & exit\"",
				base, exePath, file.getName(), base, base);

		ThreadsManager.submitAndTrackThread("C++ Build " + file.getName() , () -> {
			try {
				Process p = new ProcessBuilder("cmd", "/c", cmdLine)
						.directory(dir)
						.inheritIO()
						.start();

				p.waitFor();
				p.destroyForcibly();
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

		String exePath;

		if (prism.getConfig().getBoolean(ConfigKey.LANGUAGE_CPP_GNU_GPP_COMPILER_PROVIDED_IN_PATH_ENV, true)) {
			exePath = "g++";
		} else {
			exePath = Paths.get(prism.getConfig().getString(ConfigKey.LANGUAGE_CPP_GNU_GPP_COMPILER_PATH, "")).toAbsolutePath().toString();
		}

		String cmdLine = String.format(
				"cmd /c \"%s \"%s\" -o \"%s\" & start \"\" \"%s\" & pause & exit\"",
				exePath, file.getName(), base, base
		);

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
}
