package com.prism.components.definition;

public enum Language {
	ENGLISH_US(0, "languages/en-US.yml"),
	FRENCH(1, "languages/fr.yml");

	private final int id;
	private final String ymlPath;

	Language(int id, String ymlPath) {
		this.id = id;
		this.ymlPath = ymlPath;
	}

	public int getId() {
		return id;
	}

	public String getPath() {
		return ymlPath;
	}

	public static Language fromId(int id) {
		for (Language lang : Language.values()) {
			if (lang.getId() == id) {
				return lang;
			}
		}

		return null;
	}
}
