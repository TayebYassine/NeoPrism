package com.prism.managers;

import com.prism.Prism;
import com.prism.components.definition.Shell;
import com.prism.components.terminal.Terminal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TerminalManager {
	private static final Prism prism = Prism.getInstance();

	public static List<Terminal> terminals = new ArrayList<>();

	public static void newTerminal(Shell shell) {
		newTerminal(FileManager.getRootDirectory(), shell);
	}

	public static void newTerminal(File directory, Shell shell) {
		Terminal terminal = new Terminal(directory, shell);

		terminals.add(terminal);

		prism.getTerminalTabbedPane().addTerminalTab(terminal, shell);

	}

	public static void closeAllTabs() {
		int size = terminals.size();

		for (int index = size - 1; index >= 0; index--) {
			Terminal terminal = terminals.get(index);

			terminal.closeProcess();

			prism.getTerminalTabbedPane().closeTabByIndex(index, true);
		}
	}

	public static void stopAllProcesses() {
		for (Terminal terminal : terminals) {
			terminal.closeProcess();
		}
	}
}
