package com.prism.utils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class Keyboard {
    public static void copyToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        clipboard.setContents(stringSelection, null);
    }
}
