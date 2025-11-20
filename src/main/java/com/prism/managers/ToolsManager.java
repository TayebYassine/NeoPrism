package com.prism.managers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.prism.Prism;
import com.prism.components.definition.Tool;
import com.prism.components.frames.ErrorDialog;
import com.prism.components.frames.WarningDialog;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ToolsManager {
	private static final Prism prism = Prism.getInstance();

	private static final File TOOLS_FILE = Paths.get("tools.json").toFile();
	private static final ObjectMapper MAPPER = new ObjectMapper()
			.enable(SerializationFeature.INDENT_OUTPUT);

	public static List<Tool> tools = new ArrayList<>();

	public static void loadTools() {
		if (!TOOLS_FILE.exists()) {
			try {
				TOOLS_FILE.createNewFile();
			} catch (Exception e) {
				new WarningDialog(prism, e);
			}

			tools = new ArrayList<>();
			return;
		}

		try {
			tools = MAPPER.readValue(TOOLS_FILE, new TypeReference<List<Tool>>() {
			});

			prism.getPrismMenuBar().updateComponent();
		} catch (Exception e) {
			new ErrorDialog(prism, e);

			tools = new ArrayList<>();
		}
	}

	public static void saveTools() {
		try {
			MAPPER.writeValue(TOOLS_FILE, tools);

			prism.getPrismMenuBar().updateComponent();
		} catch (Exception e) {
			new ErrorDialog(prism, e);
		}
	}

	public static void addTool(Tool tool) {
		if (tool != null) {
			tools.add(tool);

			saveTools();
		}
	}

	public static void updateTool(Tool updatedTool) {
		if (updatedTool == null) {
			return;
		}

		UUID idToUpdate = updatedTool.getId();

		for (int i = 0; i < tools.size(); i++) {
			if (tools.get(i).getId().equals(idToUpdate)) {
				tools.set(i, updatedTool);

				saveTools();

				prism.getPrimaryToolbar().updateComponent();

				if (prism.getPrismMenuBar() != null) {
					prism.getPrismMenuBar().updateComponent();
				}

				return;
			}
		}

	}

	public static void removeToolById(UUID toolId) {
		int indexToRemove = -1;

		for (int i = 0; i < tools.size(); i++) {
			if (tools.get(i).getId().equals(toolId)) {
				indexToRemove = i;
				break;
			}
		}

		if (indexToRemove != -1) {
			tools.remove(indexToRemove);

			saveTools();

			if (prism.getPrismMenuBar() != null) {
				prism.getPrismMenuBar().updateComponent();
			}
		}
	}

	public static boolean isNameUnique(String name) {
        for (Tool tool : tools) {
            if (tool.getName().equalsIgnoreCase(name)) {
                return false;
            }
        }

		return true;
	}

	public static boolean isShortcutUnique(String shortcut) {
        for (Tool tool : tools) {
            if (tool.getShortcut().equalsIgnoreCase(shortcut)) {
                return true;
            }
        }

		return false;
	}

	public static List<Tool> getAllTools() {
		return tools;
	}
}
