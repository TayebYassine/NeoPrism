package com.prism.managers;

import com.prism.Prism;
import com.prism.utils.ResourceUtil;
import com.prism.utils.Symbols;
import org.fife.ui.autocomplete.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

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
                String desc = o.optString("description");
                /*boolean isFunction = sym != null && sym.equalsIgnoreCase("function");

                if (isFunction) {
                    String returnType = o.optString("returnType") == null ? "void" : o.optString("returnType");

                    FunctionCompletion fc = new FunctionCompletion(provider, kw + "(", returnType);
                    fc.setIcon(Symbols.getSymbolIcon("function"));
                    fc.setShortDescription(desc);



                    fc.setParams(List.of(new ParameterizedCompletion.Parameter("type", "name", true)));

                    out.add(fc);
                } else {*/
                    BasicCompletion bc = new BasicCompletion(provider, kw, null, desc);

					assert sym != null;
					bc.setIcon(Symbols.getSymbolIcon(sym.toLowerCase()));

                    out.add(bc);
                //}
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
            String desc = o.optString("description");
            BasicCompletion bc = new BasicCompletion(provider, kw, null, desc);
            bc.setIcon(Symbols.getSymbolIcon(sym.toLowerCase()));
            provider.addCompletion(bc);
        }
    }
}