package com.prism.components.toolbar;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class CodeOutlineToolbar extends JPanel {
	public static final Prism prism = Prism.getInstance();

	public CodeOutlineToolbar(Prism prism) {
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		setBorder(new EmptyBorder(5, 5, 5, 0));

		JCheckBox checkboxIgnoreComments = new JCheckBox(prism.getLanguage().get(51));
		checkboxIgnoreComments.setFocusable(true);
		checkboxIgnoreComments.setSelected(prism.getConfig().getBoolean(ConfigKey.CODE_OUTLINE_IGNORE_COMMENTS, true));
		checkboxIgnoreComments.addActionListener(e -> prism.getConfig().set(ConfigKey.CODE_OUTLINE_IGNORE_COMMENTS, checkboxIgnoreComments.isSelected()));

		add(checkboxIgnoreComments);

		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
	}
}
