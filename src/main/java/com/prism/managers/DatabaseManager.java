package com.prism.managers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.prism.Prism;
import com.prism.components.definition.Connection;
import com.prism.components.frames.ErrorDialog;
import com.prism.components.frames.WarningDialog;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
	private static final Prism prism = Prism.getInstance();

	private static final File DATABASES_FILE = Paths.get("databases.json").toFile();
	private static final ObjectMapper MAPPER = new ObjectMapper()
			.enable(SerializationFeature.INDENT_OUTPUT);

	public static List<Connection> databases = new ArrayList<>();

	public static void loadDatabases() {
		if (!DATABASES_FILE.exists()) {
			try {
				DATABASES_FILE.createNewFile();
			} catch (Exception e) {
				new WarningDialog(prism, e);
			}

			databases = new ArrayList<>();
			return;
		}

		try {
			databases = MAPPER.readValue(DATABASES_FILE, new TypeReference<List<Connection>>() {
			});

			prism.getPrismMenuBar().updateComponent();
		} catch (Exception e) {
			new ErrorDialog(prism, e);

			databases = new ArrayList<>();
		}
	}

	public static void saveDatabases() {
		try {
			MAPPER.writeValue(DATABASES_FILE, databases);

			prism.getPrismMenuBar().updateComponent();
		} catch (Exception e) {
			new ErrorDialog(prism, e);
		}
	}

	public static void addDatabase(Connection database) {
		if (database != null) {
			databases.add(database);
			saveDatabases();
		}
	}

	public static void removeDatabaseById(UUID databaseId) {
		int indexToRemove = -1;

		for (int i = 0; i < databases.size(); i++) {
			if (databases.get(i).getId().equals(databaseId)) {
				indexToRemove = i;
				break;
			}
		}

		if (indexToRemove != -1) {
			databases.remove(indexToRemove);

			saveDatabases();

			if (prism.getPrismMenuBar() != null) {
				prism.getPrismMenuBar().updateComponent();
			}
		}
	}

	public static List<Connection> getAllDatabases() {
		return databases;
	}
}
