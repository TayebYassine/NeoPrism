package com.prism.managers;

import com.prism.Prism;
import com.prism.utils.ResourceUtil;
import com.prism.utils.Symbols;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutocompleteManager {

	private static final Prism prism = Prism.getInstance();

	public static void installCompletions(DefaultCompletionProvider provider, String lang) {
		JSONObject root = prism.getPluginLoader()
				.getMergedAutocomplete()
				.getJson();
		if (root == null) return;

		JSONObject langObj = root.optJSONObject(lang);
		if (langObj == null) return;

		JSONArray shorthands = langObj.optJSONArray("shorthand");
		if (shorthands != null) {
			for (int i = 0; i < shorthands.length(); i++) {
				JSONObject o = shorthands.optJSONObject(i);
				if (o == null) continue;
				String input = o.optString("input");
				String replacement = o.optString("replacement");
				String desc = o.optString("description");
				ShorthandCompletion sc = new ShorthandCompletion(provider, input, replacement, desc);
				sc.setIcon(ResourceUtil.getIconFromSVG("icons/ui/lightbulb.svg", 16, 16));
				provider.addCompletion(sc);
			}
		}

		JSONArray tops = langObj.optJSONArray("default");
		if (tops != null) {
			addLevel(provider, tops);
		}
	}

	public static List<Completion> getChildren(DefaultCompletionProvider provider, String language,
											   String prefix) {
		JSONObject root = prism.getPluginLoader()
				.getMergedAutocomplete()
				.getJson();

		if (root == null) return Collections.emptyList();

		JSONObject langObj = root.optJSONObject(language);
		if (langObj == null) return Collections.emptyList();

		JSONArray arr = langObj.optJSONArray("default");
		if (arr == null) return Collections.emptyList();

		prefix = prefix.trim();

		String[] parts = prefix.isEmpty() ? new String[0] : prefix.split("\\.");

		for (String p : parts) {
			boolean found = false;

			for (int i = 0; i < arr.length(); i++) {
				JSONObject o = arr.optJSONObject(i);

				if (o != null && p.equalsIgnoreCase(o.optString("keyword"))) {
					arr = o.optJSONArray("default");
					found = true;
					break;
				}
			}

			if (!found) return Collections.emptyList();
		}

		List<Completion> out = new ArrayList<>();
		if (arr != null) {
			for (int i = 0; i < arr.length(); i++) {
				JSONObject o = arr.optJSONObject(i);
				if (o == null) continue;

				String kw = o.optString("keyword");
				String sym = o.optString("symbol");
				String shortDesc = o.optString("shortDescription");
				String desc = o.optString("description");

				String returns = o.optString("returns");
				String definedIn = o.optString("definedIn");

				BasicCompletion bc = new BasicCompletion(provider, kw, shortDesc, getHTMLDescription(kw, desc, returns, definedIn, Symbols.isFunction(sym)));

				assert sym != null;
				bc.setIcon(Symbols.getSymbolIcon(sym.toLowerCase()));

				out.add(bc);
			}
		}
		return out;
	}

	private static void addLevel(DefaultCompletionProvider provider,
								 JSONArray arr) {
		for (int i = 0; i < arr.length(); i++) {
			JSONObject o = arr.optJSONObject(i);
			if (o == null) continue;

			String kw = o.optString("keyword");
			String sym = o.optString("symbol");
			String shortDesc = o.optString("shortDescription");
			String desc = o.optString("description");

			String returns = o.optString("returns");
			String definedIn = o.optString("definedIn");

			BasicCompletion bc = new BasicCompletion(provider, kw, shortDesc, getHTMLDescription(kw, desc, returns, definedIn, Symbols.isFunction(sym)));

			bc.setIcon(Symbols.getSymbolIcon(sym.toLowerCase()));

			provider.addCompletion(bc);
		}
	}

	private static String getHTMLDescription(String kw, String desc, String returns, String definedIn, boolean isFunction) {
		if (desc == null || desc.isEmpty()) {
			desc = "<em>No description was provided</em>";
		} else {
			desc = desc.replace("\n","<br/>");
		}

		if (returns == null || returns.isEmpty()) returns = "<em>Unknown</em>";
		if (definedIn == null || definedIn.isEmpty()) definedIn = "<em>Pre-defined</em>";

		return "<html><body style='padding:6px;font-family:Sans-Serif;'>"
				+ "<b>" + kw + "</b><br/>"
				+ "<br>"
				+ (desc)
				+ (isFunction ? (
				    "<br><br>"
						+ "<b>Returns: </b>"
						+ (returns)
				) : "")
				+ "<hr style='border:none;border-top:1px solid #ccc;margin:10px 0 5px 0;'>"
				+ "<div style='text-align:center;font-size:0.9em'>"
				+ "<b>Defined in: </b>"
				+ (definedIn)
				+ "</div>"
				+ "</body></html>";
	}
}