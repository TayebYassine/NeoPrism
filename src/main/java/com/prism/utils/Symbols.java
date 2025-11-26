package com.prism.utils;

import javax.swing.*;
import java.util.List;

public class Symbols {
    public static Icon getSymbolIcon(String symbol, boolean allowUnknownIcon) {
        String fileName = switch (symbol) {
			case "attribute" -> "icons/ui/attribute";
			case "class" -> "icons/ui/class";
			case "constant" -> "icons/ui/constant";
			case "enum" -> "icons/ui/enum";
			case "enum2", "enumconstant", "enumerator" -> "icons/ui/enumconstant";
			case "exception" -> "icons/ui/exception";
			case "field" -> "icons/ui/field";
			case "function" -> "icons/ui/function";
			case "gvariable" -> "icons/ui/gvariable";
			case "interface" -> "icons/ui/interface";
			case "lambda" -> "icons/ui/lambda";
			case "method" -> "icons/ui/method";
			case "namespace" -> "icons/ui/namespace";
			case "parameter" -> "icons/ui/parameter";
			case "property" -> "icons/ui/property";
			case "record" -> "icons/ui/record";
			case "struct" -> "icons/ui/struct";
			case "static" -> "icons/ui/static";
			case "test" -> "icons/ui/test";
			case "type" -> "icons/ui/type";
			case "typedef" -> "icons/ui/typedef";
			case "union" -> "icons/ui/union";
			case "variable" -> "icons/ui/variable";
			default -> allowUnknownIcon ? "icons/ui/unknown" : "icons/ui/dot";
		};

		return ResourceUtil.getIconFromSVG(fileName + (Theme.isDarkTheme() ? "_dark.svg" : ".svg"), 16, 16);
    }

    public static boolean isFunction(String symbolName) {
        return List.of("method", "function").contains(symbolName);
    }
}
