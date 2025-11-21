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
import com.prism.services.syntaxchecker.JavaSyntaxChecker;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static javax.swing.JOptionPane.*;

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

		JMenuItem formatSourceItem = new JMenuItem("Format Source Code");
		formatSourceItem.addActionListener(e -> {
            formatSourceCode(pf);
        });

		add(buildAndRunExternalThreadItem);
		add(buildAndRunItem);
		addSeparator();
		add(formatSourceItem);
	}

	@Override
	public ImageIcon getIconOfCodeOutlineLine(String line) {
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

	private void buildAndRunExternalThreadFile(File file) {
		File dir = file.getParentFile();
		String base = file.getName().replaceFirst("[.][^.]+$", "");

		String cmdLine = String.format(
				"cmd /c start \"Running %s\" cmd /c \"(javac \"%s.java\" && java \"%s\") & pause & exit\"",
				base, base, base);

		try {
			new ProcessBuilder("cmd", "/c", cmdLine)
					.directory(dir)
					.inheritIO()
					.start();
		} catch (Exception ex) {
			new WarningDialog(prism, ex);
		}
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

	private void formatSourceCode(PrismFile pf) {
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
			pf.getTextArea().replace(pretty);
		}
	}
}
