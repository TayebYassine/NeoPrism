package com.prism.components.textarea;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.definition.PrismFile;
import com.prism.components.extended.JKineticScrollPane;
import com.prism.managers.FileManager;
import com.prism.managers.TextAreaManager;
import com.prism.managers.ThreadsManager;
import com.prism.utils.Keyboard;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class TextAreaTabbedPane extends JTabbedPane {

	private static final Prism prism = Prism.getInstance();

	public TextAreaTabbedPane() {
		super();

		setFocusable(true);
		setTabLayoutPolicy(SCROLL_TAB_LAYOUT);

		addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				PrismFile file = getCurrentFile();

				prism.updateStatusBar();

				if (file != null) {
					prism.updateWindowTitle(file);
					prism.updateComponents(file);
				}
			}
		});
	}

	public void addTextAreaTab(PrismFile pf) {
		JKineticScrollPane scrollPane = new JKineticScrollPane(pf.getTextArea());

		TextAreaManager.setGutter(scrollPane);

		pf.setScrollPane(scrollPane);

		addTab(pf.getName(), scrollPane);

		addFeaturesToTab(pf);
	}

	public void addImageViewerTab(PrismFile pf) {
		addTab(pf.getName(), pf.getImageViewer());

		addFeaturesToTab(pf);
	}

	public void addHomepageTab(PrismFile pf) {
		addTab(pf.getName(), pf.getCustomJPanel());

		addFeaturesToTab(pf);
	}

	public void removeTextAreaTab(TextArea textArea) {
		int index = findIndexByTextArea(textArea);

		if (index != -1) {
			removeTabAt(index);
		}
	}

	public void removeTextAreaTab(PrismFile file) {
		int index = findIndexByPrismFile(file);

		if (index != -1) {
			removeTabAt(index);
		}
	}

	public void redirectUserToTab(PrismFile file) {
		int index = findIndexByPrismFile(file);

		if (index != -1) {
			setSelectedIndex(index);
		}
	}

	public int findIndexByTextArea(TextArea textArea) {
        return indexOfComponent(textArea);
	}

	public int findIndexByPrismFile(PrismFile file) {
		for (int i = 0; i < getTabCount(); i++) {
			Component componentIndex = getComponentAt(i);

			if (componentIndex instanceof JKineticScrollPane scrollPane) {
				TextArea textArea = (TextArea) scrollPane.getViewport().getView();

				if (textArea == file.getTextArea()) {
					return i;
				}
			} else if (componentIndex instanceof ImageViewer container) {
				if (container == file.getImageViewer()) {
					return i;
				}
			} else if (componentIndex instanceof Homepage container) {
				if (container == file.getCustomJPanel()) {
					return i;
				}
			}
		}

		return -1;
	}

	public void redirectUserToTab(int index, boolean... openHomepageIfAllClosed) {
		if (index < 0 || index >= getTabCount()) {
			if (getTabCount() > 0) {
				setSelectedIndex(0);
			} else {
				if (openHomepageIfAllClosed.length == 1 && openHomepageIfAllClosed[0]) {
					openHomepageIfAllClosed();
				}
			}

			return;
		}

		setSelectedIndex(index);
	}

	public void closeAllTabs() {
		int size = FileManager.files.size();

		for (int index = size - 1; index >= 0; index--) {
			closeTabByIndex(index, true);
		}
	}

	public void closeTabByIndex(int index, boolean... openHomepageIfAllClosed) {
		if (index < 0 || index >= getTabCount()) {
			return;
		}

		PrismFile fileIndex = getFileFromIndex(index);

		if (!fileIndex.isSaved()) {
			int response = JOptionPane.showConfirmDialog(Prism.getInstance(),
					"This file is marked with new changes, do you want to save it?", "File Changes",
					JOptionPane.YES_NO_CANCEL_OPTION);

			if (response == JOptionPane.YES_OPTION) {
				FileManager.saveFile();
			} else if (response == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}

		removeTabAt(index);

		FileManager.files.remove(index);

		if (openHomepageIfAllClosed.length == 1 && openHomepageIfAllClosed[0]) {
			openHomepageIfAllClosed();
		}

		if (getTabCount() > 0) {
			PrismFile file = getCurrentFile();

			prism.updateWindowTitle(file);
			prism.updateComponents(file);
		}

		prism.getBookmarks().updateTreeData(TextAreaManager.getBookmarksOfAllFiles());
	}

	public void updateTabTitle(PrismFile file) {
		int index = findIndexByPrismFile(file);

		if (index != -1) {
			JPanel tabPanel = (JPanel) getTabComponentAt(index);

			if (tabPanel != null && tabPanel.getComponent(0) instanceof JLabel tabTitle) {
				tabTitle.setText(file.getName() + ((!file.isSaved() && !prism.getConfig().getBoolean(ConfigKey.AUTO_SAVE, true)) ? "*" : ""));
			}
		}
	}

	public PrismFile getCurrentFile() {
		return getFileFromIndex(getSelectedIndex());
	}

	public PrismFile getFileFromIndex(int index) {
		if (index < 0 || index >= getTabCount()) {
			return null;
		}

		return FileManager.files.get(index);
	}

	public void nextTab() {
		if (getSelectedIndex() < getTabCount() - 1) {
			setSelectedIndex(getSelectedIndex() + 1);
		}
	}

	public void previousTab() {
		if (getSelectedIndex() > 0) {
			setSelectedIndex(getSelectedIndex() - 1);
		}
	}

	public void refresh() {
		ThreadsManager.submitAndTrackThread("Refresh Text Area", () -> {
			JOptionPane pane = new JOptionPane(prism.getLanguage().get(229), JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
			JDialog dialog = pane.createDialog(prism, prism.getLanguage().get(10006));
			dialog.setModal(true);
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.setResizable(false);

			SwingUtilities.invokeLater(() -> {
				dialog.setVisible(true);
			});

			final List<File> files = FileManager.getFiles().stream().map(PrismFile::getFile).toList();

			closeAllTabs();

			for (File file : files) {
				FileManager.openFile(file);
			}

			SwingUtilities.invokeLater(() -> {
				dialog.setVisible(false);
				dialog.dispose();
			});
		});
	}

	public void openHomepageIfAllClosed() {
		if (getTabCount() == 0) {
			FileManager.openHomepage();
		}
	}

	public void addFeaturesToTab(PrismFile file) {
		int index = findIndexByPrismFile(file);

		if (index != -1) {
			addFeaturesToTab(index, file.getIcon());
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
					closeAllItem.addActionListener(e -> closeAllTabs());

					JMenuItem closeAllFromRightItem = new JMenuItem(prism.getLanguage().get(239));
					closeAllFromRightItem.addActionListener(e -> {
						for (int i = indexOfTabComponent(tabPanel); i < getTabCount(); i++) {
							closeTabByIndex(i, true);
						}
					});

					JMenuItem closeOthersItem = new JMenuItem(prism.getLanguage().get(240));
					closeOthersItem.addActionListener(e -> {
						for (int i = 0; i < getTabCount(); i++) {
							if (i == indexOfTabComponent(tabPanel)) {
								continue;
							}

							closeTabByIndex(i, true);
						}
					});

					JMenuItem copyPathItem = new JMenuItem(prism.getLanguage().get(48));
					copyPathItem.addActionListener(e -> {
						int tabIndex = indexOfTabComponent(tabPanel);
						PrismFile prismFile = FileManager.files.get(tabIndex);

						if (prismFile == null) {
							return;
						}

						File file = prismFile.getFile();

						if (file == null) {
							return;
						}

						Keyboard.copyToClipboard(file.getAbsolutePath());
					});

					contextMenu.add(closeItem);
					contextMenu.add(closeAllItem);
					contextMenu.add(closeOthersItem);
					contextMenu.add(closeAllFromRightItem);
					contextMenu.addSeparator();
					contextMenu.add(copyPathItem);

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
