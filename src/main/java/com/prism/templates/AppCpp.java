package com.prism.templates;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AppCpp extends AppBase {
	private String SAMPLE = """
			#include <iostream>
			
			int main() {
			    std::cout << "Hello World!" << std::endl;
			    
			    return 0;
			}
			""";

	public AppCpp() {
		super("C++: Console", "icons/languages/cpp.svg");
	}

	@Override
	public void create(File directory) {
		File mainFile = new File(directory, "main.cpp");

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
