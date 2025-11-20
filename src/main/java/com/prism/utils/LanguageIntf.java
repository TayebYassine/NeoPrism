package com.prism.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageIntf extends YamlResourceLoader {
	private static final Pattern PLACEHOLDER = Pattern.compile("%(\\d+)");

	public LanguageIntf(String path) {
		super(path);
	}

	public String get(int key, Object... args) {
		String raw = get(key);

		if (!(raw instanceof String template)) {
			return raw == null ? null : raw.toString();
		}

		if (args == null || args.length == 0) {
			return template;
		}

		Matcher m = PLACEHOLDER.matcher(template);
		StringBuffer sb = new StringBuffer();

		while (m.find()) {
			int idx = Integer.parseInt(m.group(1));
			String replacement = idx < args.length ? String.valueOf(args[idx]) : m.group(0);
			m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}

		m.appendTail(sb);

		return sb.toString();
	}
}
