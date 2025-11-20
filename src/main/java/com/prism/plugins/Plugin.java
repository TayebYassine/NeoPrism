package com.prism.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.prism.Prism;
import com.prism.components.frames.WarningDialog;
import com.prism.plugins.data.PluginAutocomplete;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class Plugin {
	private static final Prism prism = Prism.getInstance();

	private final JSONObject json;
	private final File sourceFile;

	private final String name;
	private final String version;
	private final String description;
	private boolean enabled;

	private final PluginAutocomplete autocomplete;

	public Plugin(JSONObject json, File sourceFile) {
		this.json = json;
		this.sourceFile = sourceFile;

		this.name = json.optString("name", "Unnamed Plugin");
		this.version = json.optString("version", "0.0.0");
		this.description = json.optString("description", "No description provided");
		this.enabled = json.optBoolean("enabled", false);

		this.autocomplete = new PluginAutocomplete(json.optJSONObject("autocomplete"));
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getDescription() {
		return description;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;

		setEnabledToJson(this.enabled);
	}

	public PluginAutocomplete getAutocomplete() {
		return this.autocomplete;
	}

	public File getFile() {
		return sourceFile;
	}

	private void setEnabledToJson(boolean value) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

			json.put("enabled", value);

			writer.writeValue(sourceFile, json.toMap());
		} catch (Exception ex) {
			new WarningDialog(prism, ex);
		}
	}
}