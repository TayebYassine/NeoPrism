package com.prism.templates;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AppC extends AppBase {
	private String SAMPLE = """
			#include <stdio.h>
			
			int main() {
			    printf("Hello World!\\n");
			    
			    return 0;
			}
			""";

	public AppC() {
		super("C: Console", "icons/languages/c.svg");
	}

	@Override
	public void create(File directory) {
		File mainFile = new File(directory, "main.c");

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
