package com.prism.components.toolbar;

import com.prism.Prism;
import com.prism.components.frames.CreateProjectFrame;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class FileExplorerToolbar extends JToolBar {

	public FileExplorerToolbar(Prism prism) {
		setFloatable(false);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		setBorder(new EmptyBorder(5, 5, 5, 0));

		JButton buttonNewProject = createButton(ResourceUtil.getIconFromSVG("icons/ui/project-plus.svg"), prism.getLanguage().get(191));
		buttonNewProject.addActionListener(e -> new CreateProjectFrame());

		JButton buttonNewFile = createButton(ResourceUtil.getIconFromSVG("icons/ui/file-plus.svg"), prism.getLanguage().get(152));
		buttonNewFile.addActionListener(e -> {
            File targetDir = prism.getFileExplorer().getSelectedFile();

            if (targetDir == null) {
                return;
            }

            if (!targetDir.isDirectory()) {
                targetDir = targetDir.getParentFile();
            }

            prism.getFileExplorer().newFile(false);
        });

		JButton buttonNewFolder = createButton(ResourceUtil.getIconFromSVG("icons/ui/folder-plus.svg"), prism.getLanguage().get(192));
		buttonNewFolder.addActionListener(e -> {
            File targetDir = prism.getFileExplorer().getSelectedFile();

            if (targetDir == null) {
                return;
            }

            if (!targetDir.isDirectory()) {
                targetDir = targetDir.getParentFile();
            }

            prism.getFileExplorer().newFile(true);
        });

		JButton buttonRefresh = createButton(ResourceUtil.getIconFromSVG("icons/ui/refresh.svg"), prism.getLanguage().get(49));
		buttonRefresh.addActionListener(e -> prism.getFileExplorer().refresh());

		add(buttonNewProject);
		add(Box.createRigidArea(new Dimension(4, 0)));
		add(buttonNewFile);
		add(Box.createRigidArea(new Dimension(4, 0)));
		add(buttonNewFolder);
		add(Box.createRigidArea(new Dimension(4, 0)));
		add(buttonRefresh);

		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
	}

	private JButton createButton(ImageIcon buttonIcon, String tooltip) {
		JButton button = new JButton();

		if (tooltip != null) {
			button.setToolTipText(tooltip);
		}

		button.setPreferredSize(new Dimension(24, 24));

		if (buttonIcon != null) {
			Image scaledImage = buttonIcon.getImage().getScaledInstance(16, 16, Image.SCALE_FAST);
			button.setIcon(new ImageIcon(scaledImage));
		}

		button.setFocusPainted(true);

		return button;
	}
}
