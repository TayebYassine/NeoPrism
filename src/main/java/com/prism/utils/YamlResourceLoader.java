package com.prism.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class YamlResourceLoader {
	private final Map<Integer, Object> MAP = new LinkedHashMap<>();

	public YamlResourceLoader(String path) {
		String name = path.startsWith("/") ? path : ("/" + path);

		try (InputStream in = YamlResourceLoader.class.getResourceAsStream(name)) {
			if (in == null) {
				throw new IllegalArgumentException("YAML resource not found: " + name);
			}

			Map<Object, Object> raw = new Yaml().load(in);
			raw.forEach((k, v) -> MAP.put(Integer.valueOf(k.toString()), String.valueOf(v)));
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot load YAML resource: " + name, e);
		}
	}

	public String get(int key) {
		return MAP.get(key) != null ? ((String) MAP.get(key)) : ("STRING NOT FOUND, KEY=" + key);
	}
}