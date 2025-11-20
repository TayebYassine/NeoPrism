package com.prism.utils;

import com.prism.managers.FileManager;

import javax.swing.*;
import java.awt.event.*;

public class Keybinds {
	public Keybinds(JTabbedPane tabbedPane) {
		tabbedPane.registerKeyboardAction(e -> FileManager.saveFile(), KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

		tabbedPane.registerKeyboardAction(e -> FileManager.saveAsFile(), KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		tabbedPane.registerKeyboardAction(e -> FileManager.openNewFile(), KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

		tabbedPane.registerKeyboardAction(e -> FileManager.openFile(), KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
}