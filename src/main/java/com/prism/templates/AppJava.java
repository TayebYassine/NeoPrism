package com.prism.templates;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AppJava extends AppBase {
	private String SAMPLE = """
			public class Main {
			    public static void main(String[] args) {
			        System.out.println("Hello, World!");
			    }
			}
			""";

	public AppJava() {
		super("Java: Console", "icons/languages/java.svg");
	}

	@Override
	public void create(File directory) {
		File mainFile = new File(directory, "Main.java");

		if (!mainFile.exists()) {
			try {
				mainFile.createNewFile();
			} catch (IOException e) {
				return;
			}
		}

		try (FileWriter fw = new FileWriter(mainFile)) {
			fw.write(SAMPLE);
		} catch (IOException _) {

		}
	}
}
