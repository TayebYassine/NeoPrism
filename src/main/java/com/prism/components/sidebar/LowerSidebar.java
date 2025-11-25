package com.prism.components.sidebar;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import javax.swing.event.*;

public class LowerSidebar extends JTabbedPane {
	private static final Prism prism = Prism.getInstance();

	public LowerSidebar(JLabel header, JPanel terminalArea, JPanel bookmarksArea, JPanel tasksArea, JPanel databaseArea, JPanel problemsArea) {
		super(JTabbedPane.BOTTOM);

		setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		setFocusable(true);

		addTasks(tasksArea);
		addTerminalArea(terminalArea);
		addBookmarks(bookmarksArea);
		addDatabase(databaseArea);
		addProblems(problemsArea);

		addChangeListener(e -> {
            int index = getSelectedIndex();

            prism.getConfig().set(ConfigKey.LOWER_SIDEBAR_SELECTED_INDEX, index);

            switch (index) {
                case 0:
                    header.setText(prism.getLanguage().get(210));
                    break;
                case 1:
                    header.setText(prism.getLanguage().get(209));
                    break;
                case 2:
                    header.setText(prism.getLanguage().get(211));
                    break;
                case 3:
                    header.setText(prism.getLanguage().get(212));
                    break;
                case 4:
                    header.setText(prism.getLanguage().get(213));
                    break;
            }
        });

		setSelectedIndex(prism.getConfig().getInt(ConfigKey.LOWER_SIDEBAR_SELECTED_INDEX, 1));
	}

	private void addTasks(JPanel tasksArea) {
		addTab(prism.getLanguage().get(210), ResourceUtil.getIconFromSVG("icons/ui/checklist.svg", 16, 16), tasksArea);
	}

	private void addTerminalArea(JPanel terminalArea) {
		addTab(prism.getLanguage().get(209), ResourceUtil.getIconFromSVG("icons/ui/command-prompt.svg", 16, 16), terminalArea);
	}

	private void addBookmarks(JPanel bookmarksArea) {
		addTab(prism.getLanguage().get(211), ResourceUtil.getIconFromSVG("icons/ui/bookmark.svg", 16, 16), bookmarksArea);
	}

	private void addDatabase(JPanel databaseArea) {
		addTab(prism.getLanguage().get(212), ResourceUtil.getIconFromSVG("icons/ui/database.svg", 16, 16), databaseArea);
	}

	private void addProblems(JPanel problemsArea) {
		addTab(prism.getLanguage().get(213), ResourceUtil.getIconFromSVG("icons/ui/alert-circle.svg", 16, 16), problemsArea);
	}
}
