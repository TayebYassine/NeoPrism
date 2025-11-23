package com.prism.utils;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AStyleWrapper {
	public static void formatFileAsync(File source,
									   String aStyleExePath,
									   String aStyleOptions,
									   Callback callback) {
		new SwingWorker<String, Object>() {
			@Override
			protected String doInBackground() throws Exception {
				return formatFile(source, aStyleExePath, aStyleOptions);
			}

			@Override
			protected void done() {
				try {
					callback.onSuccess(get());
				} catch (Exception ex) {
					callback.onError(ex.getMessage());
				}
			}
		}.execute();
	}

	private static String formatFile(File src,
									 String aStyleExePath,
									 String aStyleOptions) throws IOException, InterruptedException {

		Path in  = Files.createTempFile("astyin",  ".tmp");
		Path out = Files.createTempFile("astyout", ".tmp");

		/* copy source -> dummy input file */
		Files.write(in, Files.readAllBytes(src.toPath()));

		/* build command */
		List<String> cmd = new ArrayList<>();
		cmd.add(aStyleExePath);
		if (aStyleOptions != null && !aStyleOptions.isBlank()) {
			for (String s : aStyleOptions.split("\\s+")) cmd.add(s);
		}
		cmd.add("--stdin="  + in.toAbsolutePath());
		cmd.add("--stdout=" + out.toAbsolutePath());
		cmd.add("--suffix=none");          // no .orig

		ProcessBuilder pb = new ProcessBuilder(cmd)
				.redirectErrorStream(true);

		Process p = pb.start();
		String log;
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) sb.append(line).append('\n');
			log = sb.toString();
		}

		int exit = p.waitFor();
		if (exit != 0) {
			throw new IOException("AStyle exit " + exit + "\n" + log);
		}

		String result = Files.readString(out, StandardCharsets.UTF_8);

		/* clean up */
		Files.deleteIfExists(in);
		Files.deleteIfExists(out);

		return result;
	}

	public interface Callback {
		void onSuccess(String formattedText);
		void onError(String message);
	}
}
