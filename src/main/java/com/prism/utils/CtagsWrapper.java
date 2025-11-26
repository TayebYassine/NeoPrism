package com.prism.utils;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CtagsWrapper {

	public static void extractSymbolsAsync(File source,
										   String ctagsExePath,
										   String[] options,
										   Callback callback) {
		new SwingWorker<Map<String, List<String>>, Void>() {
			@Override
			protected Map<String, List<String>> doInBackground() throws Exception {
				return extractSymbols(source, ctagsExePath, options);
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

	private static Map<String, List<String>> extractSymbols(File src,
															String ctagsExePath,
															String[] options) throws IOException, InterruptedException {

		Path temp = Files.createTempFile(src.getName(), ".tmp");
		Files.write(temp, Files.readAllBytes(src.toPath()));

		List<String> cmd = new ArrayList<>();
		cmd.add(ctagsExePath);
		cmd.add("-x");
		if (options != null) Collections.addAll(cmd, options);
		cmd.add("-o");
		cmd.add("-");
		//cmd.add("-L");
		cmd.add(temp.toAbsolutePath().toString());

		ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(true);
		Process p = pb.start();

		Map<String, List<String>> bucket = new LinkedHashMap<>();

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {

			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.trim().split("\\s+", 4);

				if (parts.length < 3) continue;

				String name = parts[0];
				String kind = parts[1];

				if (!kind.startsWith("__")) {
					bucket.computeIfAbsent(kind, k -> new ArrayList<>()).add(name);
				}
			}
		}

		boolean finished = p.waitFor(5, TimeUnit.SECONDS);
		if (!finished) {
			p.destroyForcibly();
			throw new IOException("ctags timed out");
		}
		if (p.exitValue() != 0) {
			throw new IOException("ctags exit " + p.exitValue());
		}

		Files.deleteIfExists(temp);
		return bucket;
	}


	public interface Callback {
		void onSuccess(Map<String, List<String>> kindToSymbols);
		void onError(String message);
	}

	private CtagsWrapper() {}
}