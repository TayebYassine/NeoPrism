package com.prism.config;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.frames.ErrorDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Config {
	private static final Prism prism = Prism.getInstance();

	private final File file;
	private final Map<Integer, String> CONFIG_MAP = new HashMap<>();
	private final Map<Integer, String> BACKUP_MAP = new HashMap<>();

	public Config(File file) {
		this.file = file;
	}

	public void set(ConfigKey key, String value) {
		CONFIG_MAP.put(key.getId(), value);
		save();
	}

	public void set(ConfigKey key, String value, boolean update) {
		if (!update) {
			BACKUP_MAP.put(key.getId(), value);
		} else {
			CONFIG_MAP.put(key.getId(), value);

			save();
		}
	}

	public void set(ConfigKey key, int value) {
		CONFIG_MAP.put(key.getId(), Integer.toString(value));
		save();
	}

	public void set(ConfigKey key, int value, boolean update) {
		if (!update) {
			BACKUP_MAP.put(key.getId(), Integer.toString(value));
		} else {
			CONFIG_MAP.put(key.getId(), Integer.toString(value));

			save();
		}
	}

	public void set(ConfigKey key, double value) {
		CONFIG_MAP.put(key.getId(), Double.toString(value));
		save();
	}

	public void set(ConfigKey key, double value, boolean update) {
		if (!update) {
			BACKUP_MAP.put(key.getId(), Double.toString(value));
		} else {
			CONFIG_MAP.put(key.getId(), Double.toString(value));

			save();
		}
	}

	public void set(ConfigKey key, boolean value) {
		CONFIG_MAP.put(key.getId(), Boolean.toString(value));
		save();
	}

	public void set(ConfigKey key, boolean value, boolean update) {
		if (!update) {
			BACKUP_MAP.put(key.getId(), Boolean.toString(value));
		} else {
			CONFIG_MAP.put(key.getId(), Boolean.toString(value));

			save();
		}
	}

	public void set(ConfigKey key, String[] values) {
		CONFIG_MAP.put(key.getId(), String.join("\\|", values));
		save();
	}

	public void set(ConfigKey key, String[] values, boolean update) {
		if (!update) {
			BACKUP_MAP.put(key.getId(), String.join("\\|", values));
		} else {
			CONFIG_MAP.put(key.getId(), String.join("\\|", values));

			save();
		}
	}

	public String getString(ConfigKey key) {
		return CONFIG_MAP.get(key.getId());
	}

	public String getString(ConfigKey key, String defaultValue) {
		return CONFIG_MAP.getOrDefault(key.getId(), defaultValue);
	}

	public int getInt(ConfigKey key, int defaultValue) {
		try {
			return Integer.parseInt(CONFIG_MAP.get(key.getId()));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public double getDouble(ConfigKey key, double defaultValue) {
		try {
			return Double.parseDouble(CONFIG_MAP.get(key.getId()));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public boolean getBoolean(ConfigKey key, boolean defaultValue) {
		String v = CONFIG_MAP.get(key.getId());
		return (v != null) ? Boolean.parseBoolean(v) : defaultValue;
	}

	public String[] getStringArray(ConfigKey key) {
		String v = CONFIG_MAP.get(key.getId());
		return (v != null && !v.isEmpty()) ? v.split("\\|") : new String[0];
	}

	public void save() {
		save(CONFIG_MAP);
	}

	public void save(Map<Integer, String> map) {
		try {
			Properties props = new Properties();

			for (Map.Entry<Integer, String> entry : map.entrySet()) {
				props.setProperty(String.valueOf(entry.getKey()), entry.getValue());
			}

			try (FileOutputStream fos = new FileOutputStream(file)) {
				props.store(fos, "Prism Configuration, DO NOT MODIFY IF YOU KNOW WHAT YOU ARE DOING");
			}
		} catch (Exception e) {
			new ErrorDialog(prism, e);
		}
	}

	public boolean load() {
		try {
			if (!file.exists()) {
				boolean res = file.createNewFile();

				if (!res) {
					return res;
				}
			}

			Properties props = new Properties();

			try (FileInputStream fis = new FileInputStream(file)) {
				props.load(fis);
			} catch (Exception e) {
				return false;
			}

			CONFIG_MAP.clear();

			for (String keyStr : props.stringPropertyNames()) {
				int key = Integer.parseInt(keyStr);

				CONFIG_MAP.put(key, props.getProperty(keyStr));
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Map<Integer, String> getMap() {
		return CONFIG_MAP;
	}

	public void prepareBackup() {
		BACKUP_MAP.clear();

		BACKUP_MAP.putAll(CONFIG_MAP);
	}

	public void mergeBackupToConfig() {
		CONFIG_MAP.putAll(BACKUP_MAP);
		save();
	}

	public void mergeConfigToBackup() {
		BACKUP_MAP.putAll(CONFIG_MAP);
		save();
	}
}