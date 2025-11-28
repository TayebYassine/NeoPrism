package com.prism.services;

import com.prism.Prism;
import com.prism.components.definition.PrismFile;
import com.prism.components.textarea.TextArea;
import org.fife.rsta.ac.c.CLanguageSupport;

import javax.swing.*;
import java.io.File;

public class Service extends JPopupMenu {
	private static final Prism prism = Prism.getInstance();

	public void showMenu(JButton button) {
		this.show(button, 0, button.getHeight());
	}

	public ImageIcon getIconOfCodeFoldingLine(String line) {
		return null;
	}

	public void installSyntaxChecker(PrismFile pf, TextArea textArea) {
		return;
	}

	public void updateSymbolsTree(PrismFile pf, TextArea textArea) {
		return;
	}
}
