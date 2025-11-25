package com.prism.components.sidebar;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.extended.JDefaultKineticScrollPane;
import com.prism.components.sidebar.panels.CodeFoldingPanel;
import com.prism.components.sidebar.panels.FileExplorer;
import com.prism.components.sidebar.panels.PluginsPanel;
import com.prism.components.sidebar.panels.SymbolsPanel;
import com.prism.components.toolbar.CodeFoldingToolbar;
import com.prism.components.toolbar.FileExplorerToolbar;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

public class Sidebar extends JTabbedPane {
	private final static Prism prism = Prism.getInstance();

	public Sidebar(JLabel header, FileExplorer fileExplorer, CodeFoldingPanel codeFoldingPanel, SymbolsPanel symbolsPanel, PluginsPanel pluginsPanel) {
		super(JTabbedPane.LEFT);

		setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		setFocusable(true);

		addFileExplorer(fileExplorer);
		addFolding(codeFoldingPanel);
		addSymbols(symbolsPanel);
		addPlugins(pluginsPanel);

		setSelectedIndex(prism.getConfig().getInt(ConfigKey.SIDEBAR_SELECTED_INDEX, 0));

		addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int index = getSelectedIndex();

				prism.getConfig().set(ConfigKey.SIDEBAR_SELECTED_INDEX, index);

				switch (index) {
					case 0:
						header.setText(prism.getLanguage().get(33));
						break;
					case 1:
						header.setText(prism.getLanguage().get(34));
						break;
					case 2:
						header.setText(prism.getLanguage().get(237));
						break;
					case 3:
						header.setText(prism.getLanguage().get(35));
						break;
				}
			}
		});
	}

	private void addFileExplorer(FileExplorer fileExplorer) {
		JPanel panel = new JPanel(new BorderLayout());

		JPanel headerPanel = new JPanel(new BorderLayout());

		headerPanel.add(new FileExplorerToolbar(prism), BorderLayout.NORTH);
		headerPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);

		JDefaultKineticScrollPane scrollPane = new JDefaultKineticScrollPane(fileExplorer);

		panel.add(headerPanel, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);

		addTab(null, ResourceUtil.getIconFromSVG("icons/ui/folders.svg", 16, 16), panel, prism.getLanguage().get(33));
	}

	private void addFolding(CodeFoldingPanel codeFoldingPanel) {
		JPanel panel = new JPanel(new BorderLayout());

		JPanel headerPanel = new JPanel(new BorderLayout());

		headerPanel.add(new CodeFoldingToolbar(prism), BorderLayout.NORTH);
		headerPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);

		JDefaultKineticScrollPane scrollPane = new JDefaultKineticScrollPane(codeFoldingPanel);

		panel.add(headerPanel, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);

		addTab(null, ResourceUtil.getIconFromSVG("icons/ui/outline.svg", 16, 16), panel, prism.getLanguage().get(34));
	}

	private void addSymbols(SymbolsPanel symbolsPanel) {
		addTab(null, ResourceUtil.getIconFromSVG("icons/ui/symbols.svg", 16, 16), symbolsPanel, prism.getLanguage().get(237));
	}

	private void addPlugins(PluginsPanel pluginsPanel) {
		addTab(null, ResourceUtil.getIconFromSVG("icons/ui/plug.svg", 16, 16), pluginsPanel, prism.getLanguage().get(35));
	}
}
