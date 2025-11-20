package com.prism.components.definition;

public enum DatabaseProvider {
	SQLite(0),
	MySQL(1);

	public final int id;

	DatabaseProvider(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static DatabaseProvider fromValue(int id) {
		for (DatabaseProvider provider : DatabaseProvider.values()) {
			if (provider.id == id) {
				return provider;
			}
		}

		return null;
	}
}
