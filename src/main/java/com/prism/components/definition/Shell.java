package com.prism.components.definition;

public enum Shell {
	COMMAND_PROMPT(0),
	POWERSHELL(1);

	public final int id;

	Shell(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static Shell fromValue(int id) {
		for (Shell shell : Shell.values()) {
			if (shell.id == id) {
				return shell;
			}
		}

		return null;
	}
}
