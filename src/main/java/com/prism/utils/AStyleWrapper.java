package com.prism.utils;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AStyleWrapper {
	private static final Prism prism = Prism.getInstance();

	public static String[] STYLES = {
			"allman",
			"java",
			"kr",
			"stroustrup",
			"whitesmith",
			"banner",
			"gnu",
			"linux",
			"horstmann",
			"1tbs",
			"pico",
			"lisp",
	};
	public static String[] STYLE_NAMES = {
			"Allman",
			"Java",
			"Kernighan & Ritchie (K&R)",
			"Stroustrup",
			"Whitesmiths",
			"Ratliff (banner)",
			"GNU",
			"Linux",
			"Horstmann",
			"One True Brace (1tbs)",
			"Pico",
			"Lisp"
	};

	public static String[] STYLE_CODE_SAMPLES = {
		// Allman
		"""
		int main(void)
		{
			// Program
			return 0;
		}
		""",

				// Java
				"""
				int main(void) {
					// Program
					return 0;
				}
				""",

				// Kernighan & Ritchie (K&R)
				"""
				int main(void) {
					// Program
					return 0;
				}
				""",

				// Stroustrup
				"""
				int main(void)
				{
					// Program
					return 0;
				}
				""",

				// Whitesmiths
				"""
				int main(void)
					{
					// Program
					return 0;
					}
				""",

				// Ratliff (banner)
				"""
				int main(void) {
					// Program
					return 0;
					}
				""",

				// GNU
				"""
				int main(void)
				{
					// Program
					return 0;
				}
				""",

				// Linux
				"""
				int main(void)
				{
					// Program
					return 0;
				}
				""",

				// Horstmann
				"""
				int main(void)
				{   // Program
					return 0;
				}
				""",

				// One True Brace (1TBS)
				"""
				int main(void) {
					// Program
					return 0;
				}
				""",

				// Pico
				"""
				int main(void)
				{ // Program
				  return 0; }
				""",

				// Lisp
				"""
				int main (void)
				  {
				  // Program
				  return 0;
				  }
				"""
	};

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

	public static String getOptionsFromConfig(String defaultStyle) {
		String opts = String.format("--indent=spaces=%d ", prism.getConfig().getInt(ConfigKey.TAB_SIZE, 4));

		if (prism.getConfig().getBoolean(ConfigKey.ASTYLE_FORMAT_STYLE_AUTO, true)) {
			opts += ("--style=" + defaultStyle + " ");
		} else {
			opts += ("--style=" + STYLES[prism.getConfig().getInt(ConfigKey.ASTYLE_FORMAT_STYLE, 1)]  + " ");
		}

		if (prism.getConfig().getBoolean(ConfigKey.ASTYLE_DELETE_EMPTY_LINES, true)) {
			opts += "--delete-empty-lines ";
		}

		if (prism.getConfig().getBoolean(ConfigKey.ASTYLE_INDENT_NAMESPACES, true)) {
			opts += "--indent-namespaces ";
		}

		if (prism.getConfig().getBoolean(ConfigKey.ASTYLE_INDENT_LABELS, true)) {
			opts += "--indent-labels ";
		}

		if (prism.getConfig().getBoolean(ConfigKey.ASTYLE_INDENT_SWITCHES_AND_CASES, true)) {
			opts += "--indent-switches --indent-cases ";
		}

		return opts;
	}

	public interface Callback {
		void onSuccess(String formattedText);
		void onError(String message);
	}
}
