package com.prism.components.toolbar;

import com.prism.Prism;
import com.prism.managers.TextAreaManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class BookmarksToolbar extends JPanel {

	public BookmarksToolbar(Prism prism) {
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		setBorder(new EmptyBorder(5, 5, 5, 0));

		JButton buttonRefreshAll = createButton(prism.getLanguage().get(49), null, prism.getLanguage().get(186));
		buttonRefreshAll.addActionListener(e -> prism.getBookmarks().updateTreeData(TextAreaManager.getBookmarksOfAllFiles()));

		JButton buttonExpandAll = createButton(prism.getLanguage().get(50), null, prism.getLanguage().get(187));
		buttonExpandAll.addActionListener(e -> prism.getBookmarks().expandAllNodes());

		add(buttonRefreshAll);
		add(Box.createRigidArea(new Dimension(4, 0)));
		add(buttonExpandAll);

		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
	}

	private JButton createButton(String label, ImageIcon buttonIcon, String tooltip) {
		JButton button = new JButton(label);
		button.setFocusable(true);

		if (tooltip != null) {
			button.setToolTipText(tooltip);
		}

		if (buttonIcon != null) {
			Image scaledImage = buttonIcon.getImage().getScaledInstance(16, 16, Image.SCALE_FAST);
			button.setIcon(new ImageIcon(scaledImage));
		}

		return button;
	}
}
