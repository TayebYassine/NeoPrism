package com.prism.utils;

import javax.swing.*;
import java.util.List;

public class Symbols {
    public static Icon getSymbolIcon(String symbol) {
        switch (symbol) {
            case "enum":
                return ResourceUtil.getIconFromSVG("icons/ui/symbol-enum.svg", 16, 16);
            case "enumerator":
            case "enumconstant":
            case "enum2":
                return ResourceUtil.getIconFromSVG("icons/ui/symbol-enum2.svg", 16, 16);
            case "field":
                return ResourceUtil.getIconFromSVG("icons/ui/symbol-field.svg", 16, 16);
            case "global":
                return ResourceUtil.getIconFromSVG("icons/ui/symbol-global.svg", 16, 16);
            case "namespace":
                return ResourceUtil.getIconFromSVG("icons/ui/symbol-namespace.svg", 16, 16);
            case "member":
            case "local":
            case "qualified_name":
                return ResourceUtil.getIconFromSVG("icons/ui/symbol-qualified_name.svg", 16, 16);
            case "struct":
                return ResourceUtil.getIconFromSVG("icons/ui/symbol-struct.svg", 16, 16);
            case "class":
            case "typedef":
            case "type":
                return ResourceUtil.getIconFromSVG("icons/ui/symbol-type.svg", 16, 16);
            case "union":
                return ResourceUtil.getIconFromSVG("icons/ui/symbol-union.svg", 16, 16);
            case "method":
            case "function":
                return ResourceUtil.getIconFromSVG("icons/ui/symbol-function.svg", 16, 16);
            default:
                return ResourceUtil.getIconFromSVG("icons/ui/symbol-keyword.svg", 16, 16);
        }
    }

    public static boolean isFunction(String symbolName) {
        return List.of("method", "function").contains(symbolName);
    }
}
