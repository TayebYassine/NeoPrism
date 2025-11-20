package com.prism.plugins.data;

import org.json.JSONObject;

public class PluginAutocomplete {
	private final JSONObject json;

	public PluginAutocomplete(JSONObject json) {
		this.json = json;
	}

	public Object get(String key) {
		return this.json.opt(key);
	}

	public JSONObject getJson() {
		return this.json;
	}
}
