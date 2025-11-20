package com.prism.components.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.UUID;

public class Task {
	private UUID id;
	private String filePath;
	private int line;
	private String description;

	public Task() { }

	public Task(UUID id, String filePath, int line, String description) {
		this.id = id;
		this.line = line;
		this.filePath = filePath;
		this.description = description;
	}

	public Task(File file, int line, String description) {
		this.id = UUID.randomUUID();
		this.line = line;
		this.filePath = file.getAbsolutePath();
		this.description = description;
	}

	public UUID getId() {
		return id;
	}

	@JsonIgnore
	public File getFile() {
		File file = new File(filePath);

		return file.exists() ? file : null;
	}

	public String getFilePath() {
		return filePath;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
