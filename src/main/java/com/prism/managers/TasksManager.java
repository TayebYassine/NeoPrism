package com.prism.managers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.prism.Prism;
import com.prism.components.definition.Task;
import com.prism.components.frames.ErrorDialog;
import com.prism.components.frames.WarningDialog;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TasksManager {
	private static final Prism prism = Prism.getInstance();

	private static final File TASKS_FILE = Paths.get("tasks.json").toFile();
	private static final ObjectMapper MAPPER = new ObjectMapper()
			.enable(SerializationFeature.INDENT_OUTPUT);

	public static List<Task> tasks = new ArrayList<>();

	public static void loadTasks() {
		if (!TASKS_FILE.exists()) {
			try {
				TASKS_FILE.createNewFile();
			} catch (Exception e) {
				new WarningDialog(prism, e);
			}

			tasks = new ArrayList<>();
			return;
		}

		try {
			tasks = MAPPER.readValue(TASKS_FILE, new TypeReference<List<Task>>() {
			});

			prism.getPrismMenuBar().updateComponent();

			for (Task task : tasks) {
				prism.getTasksList().addRow(task);
			}
		} catch (Exception e) {
			tasks = new ArrayList<>();
		}
	}

	public static void saveTasks() {
		try {
			MAPPER.writeValue(TASKS_FILE, tasks);

			prism.getPrismMenuBar().updateComponent();
		} catch (Exception e) {
			new ErrorDialog(prism, e);
		}
	}

	public static void addTask(Task task) {
		if (task != null) {
			tasks.add(task);
			saveTasks();
		}
	}

	public static boolean updateTask(Task updatedTask) {
		if (updatedTask == null) {
			return false;
		}

		UUID idToUpdate = updatedTask.getId();

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).getId().equals(idToUpdate)) {
				tasks.set(i, updatedTask);

				saveTasks();

				prism.getPrimaryToolbar().updateComponent();

				if (prism.getPrismMenuBar() != null) {
					prism.getPrismMenuBar().updateComponent();
				}

				return true;
			}
		}

		return false;
	}

	public static void removeTaskById(UUID taskId) {
		int indexToRemove = -1;

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).getId().equals(taskId)) {
				indexToRemove = i;
				break;
			}
		}

		if (indexToRemove != -1) {
			tasks.remove(indexToRemove);

			saveTasks();

			if (prism.getPrismMenuBar() != null) {
				prism.getPrismMenuBar().updateComponent();
			}
		}
	}

	public static List<Task> getAllTasks() {
		return tasks;
	}
}
