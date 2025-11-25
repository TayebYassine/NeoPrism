package com.prism.components.terminal;

import com.prism.Prism;
import com.prism.components.definition.PrismFile;
import com.prism.components.definition.Shell;
import com.prism.components.toolbar.TerminalToolbar;
import com.prism.managers.FileManager;
import com.prism.managers.TerminalManager;
import com.prism.utils.Keyboard;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class TerminalTabbedPane extends JTabbedPane {
	private static final Prism prism = Prism.getInstance();

	public TerminalTabbedPane() {
		setFocusable(true);
		setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
		//setTabPlacement(JTabbedPane.RIGHT);
	}

	public void addTerminalTab(Terminal terminal, Shell shell) {
		addTab(shell == Shell.POWERSHELL ? "Powershell" : "Command Prompt", terminal);

		addFeaturesToTab(terminal);
	}

	public void removeTerminalTab(Terminal terminal) {
		int index = findIndexByTerminal(terminal);

		if (index != -1) {
			removeTabAt(index);
		}
	}

	public void redirectUserToTab(Terminal terminal) {
		int index = findIndexByTerminal(terminal);

		if (index != -1) {
			setSelectedIndex(index);
		}
	}

	public int findIndexByTextArea(Terminal terminal) {
		int index = indexOfComponent(terminal);

		return index;
	}

	public int findIndexByTerminal(Terminal terminal) {
		for (int i = 0; i < getTabCount(); i++) {
			Terminal terminalIndexed = (Terminal) getComponentAt(i);

			if (terminalIndexed == terminal) {
				return i;
			}
		}

		return -1;
	}

	public void closeTabByIndex(int index, boolean... openNewTerminalIfAllTabsAreClosed) {
		if (index < 0 || index >= getTabCount()) {
			return;
		}

		removeTabAt(index);

		TerminalManager.terminals.remove(index);

		if (openNewTerminalIfAllTabsAreClosed.length == 1 || openNewTerminalIfAllTabsAreClosed[0]) {
			openNewTerminalIfAllTabsAreClosed();
		}
	}

	public Terminal getCurrentTerminal() {
		return getTerminalFromIndex(getSelectedIndex());
	}

	public Terminal getTerminalFromIndex(int index) {
		if (index < 0 || index >= getTabCount()) {
			return null;
		}

		return TerminalManager.terminals.get(index);
	}

	public void openNewTerminalIfAllTabsAreClosed() {
		if (getTabCount() == 0) {
			TerminalManager.newTerminal(Shell.fromValue(TerminalToolbar.comboboxTerminalShell.getSelectedIndex()));
		}
	}

	public void addFeaturesToTab(Terminal terminal) {
		int index = findIndexByTerminal(terminal);

		if (index != -1) {
			addFeaturesToTab(index, terminal.getShell() == Shell.COMMAND_PROMPT
					? ResourceUtil.getIconFromSVG("icons/ui/command-prompt.svg", 16, 16)
					: ResourceUtil.getIconFromSVG("icons/ui/powershell.svg", 16, 16)
			);
		}
	}

	public void addFeaturesToTab(int index, Icon icon) {
		JPanel tabPanel = new JPanel(new BorderLayout());
		tabPanel.setOpaque(false);

		JLabel tabTitle = new JLabel(getTitleAt(index), icon, JLabel.LEFT);
		tabTitle.setIconTextGap(5);
		tabTitle.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (SwingUtilities.isRightMouseButton(event)) {
					JPopupMenu contextMenu = new JPopupMenu();

					JMenuItem closeItem = new JMenuItem(prism.getLanguage().get(15));
					closeItem.addActionListener(e -> {
						int index1 = indexOfTabComponent(tabPanel);

						closeTabByIndex(index1, true);
					});

					JMenuItem closeAllItem = new JMenuItem(prism.getLanguage().get(46));
					closeAllItem.addActionListener(e -> TerminalManager.closeAllTabs());

					contextMenu.add(closeItem);
					contextMenu.add(closeAllItem);

					Point point = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), tabPanel);
					contextMenu.show(tabPanel, point.x, point.y);
				} else if (SwingUtilities.isLeftMouseButton(event)) {
					setSelectedIndex(indexOfTabComponent(tabPanel));
				}
			}
		});

		JButton closeButton = new JButton("  ✕");
		closeButton.setPreferredSize(new Dimension(17, 17));
		closeButton.setFocusable(true);
		closeButton.setBorder(BorderFactory.createEmptyBorder());
		closeButton.setContentAreaFilled(false);

		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = indexOfTabComponent(tabPanel);

				closeTabByIndex(index, true);
			}
		});

		tabPanel.add(tabTitle, BorderLayout.WEST);
		tabPanel.add(closeButton, BorderLayout.EAST);

		setTabComponentAt(index, tabPanel);
	}
}
