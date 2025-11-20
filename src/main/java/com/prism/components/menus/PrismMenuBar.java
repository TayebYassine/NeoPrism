package com.prism.components.menus;

import com.prism.Prism;
import com.prism.components.definition.ComponentType;
import com.prism.components.definition.PrismFile;
import com.prism.components.definition.Tool;
import com.prism.components.frames.*;
import com.prism.components.terminal.Terminal;
import com.prism.components.textarea.TextArea;
import com.prism.managers.FileManager;
import com.prism.managers.ToolsManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class PrismMenuBar extends JMenuBar {

	private static final Prism prism = Prism.getInstance();
	// Custom tools list components
	private final JMenu toolsMenu;
	private final JMenu windowMenu;
	private final JSeparator toolMenuSeparator;
	private final JSeparator windowsMenuSeparator;
	// Existing Menu Items
	JMenuItem menuItemNewFile;
	JMenuItem menuItemOpenFile;
	JMenuItem menuItemOpenFolder;
	JMenuItem menuItemSave;
	JMenuItem menuItemSaveAs;
	JMenuItem menuItemSaveAll;
	JMenuItem menuItemCloseApp;
	JMenuItem menuItemUndo;
	JMenuItem menuItemRedo;
	JMenuItem menuItemCut;
	JMenuItem menuItemCopy;
	JMenuItem menuItemPaste;
	JMenuItem menuItemDelete;
	JMenuItem menuItemSelectAll;
	JMenuItem menuItemOptions;
	JMenuItem menuItemSidebar;
	JMenuItem menuItemLowerSidebar;
	JMenuItem menuItemNewTool;
	JMenuItem menuItemHelp;
	JMenuItem menuItemAbout;
	// --- New Menu Items ---
	// File Menu
	JMenuItem menuItemCloseFile;
	JMenuItem menuItemCloseAll;
	// Edit Menu
	// View Menu
	// Tools Menu
	JMenuItem menuItemEditTools;
	JMenuItem menuItemDeleteTools;
	// Go Menu
	JMenuItem menuItemGoToLine;
	JMenuItem menuItemNextTab;
	JMenuItem menuItemPreviousTab;
	// Window Menu
	JMenuItem menuItemWindows;
	// Help Menu
	JMenuItem menuItemCheckForUpdates;
	// ----------------------

	public PrismMenuBar() {
		/*
		 * File menu
		 */
		JMenu fileMenu = new JMenu(prism.getLanguage().get(122));

		JMenuItem menuItemNewFile = createMenuItem(prism.getLanguage().get(152), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
		menuItemNewFile.addActionListener((_) -> {
			FileManager.openNewFile();
		});

		JMenuItem menuItemNewProject = createMenuItem(prism.getLanguage().get(153), null, null, null);
		menuItemNewProject.addActionListener((_) -> {
			new CreateProjectFrame();
		});

		JMenu recentFiles = new JMenu(prism.getLanguage().get(154));

		if (FileManager.getRecentFiles().isEmpty()) {
			JMenuItem __ = createMenuItem(prism.getLanguage().get(155), null, null, null);
			__.setEnabled(false);
			recentFiles.add(__);
		} else {
			for (File file : FileManager.getRecentFiles()) {
				JMenuItem fileItem = createMenuItem(file.getName(), null, null, null);
				fileItem.addActionListener((_) -> {
					FileManager.openFile(file);
				});

				recentFiles.add(fileItem);
			}
		}

		menuItemOpenFile = createMenuItem(prism.getLanguage().get(156), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
		menuItemOpenFile.addActionListener((_) -> {
			FileManager.openFile();
		});

		menuItemOpenFolder = createMenuItem(prism.getLanguage().get(157), null,
				null, null);
		menuItemOpenFolder.addActionListener((_) -> {
			FileManager.openDirectory();
		});

		menuItemSave = createMenuItem(prism.getLanguage().get(158), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		menuItemSave.addActionListener((_) -> {
			FileManager.saveFile();
		});

		menuItemSaveAs = createMenuItem(prism.getLanguage().get(159), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
		menuItemSaveAs.addActionListener((_) -> {
			FileManager.saveAsFile();
		});

		menuItemSaveAll = createMenuItem(prism.getLanguage().get(160), null, null,
				null);
		menuItemSaveAll.addActionListener((_) -> {
			FileManager.saveAllFiles();
		});

		menuItemCloseFile = createMenuItem(prism.getLanguage().get(161), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
		menuItemCloseFile.addActionListener((_) -> {
			prism.getTextAreaTabbedPane().closeTabByIndex(prism.getTextAreaTabbedPane().getSelectedIndex(), true);
		});

		menuItemCloseAll = createMenuItem(prism.getLanguage().get(46), null, null, null);
		menuItemCloseAll.addActionListener((_) -> {
			prism.getTextAreaTabbedPane().closeAllTabs();
		});

		menuItemCloseApp = createMenuItem(prism.getLanguage().get(163), null, null,
				null);
		menuItemCloseApp.addActionListener((_) -> {
			if (prism.isDisplayable()) {
				prism.prepareClosing();

				System.exit(0);
			}
		});

		fileMenu.add(menuItemNewFile);
		fileMenu.add(menuItemNewProject);
		fileMenu.add(recentFiles);
		fileMenu.addSeparator();
		fileMenu.add(menuItemOpenFile);
		fileMenu.add(menuItemOpenFolder);
		fileMenu.addSeparator();
		fileMenu.add(menuItemSave);
		fileMenu.add(menuItemSaveAs);
		fileMenu.add(menuItemSaveAll);
		fileMenu.addSeparator();
		fileMenu.add(menuItemCloseFile); // New
		fileMenu.add(menuItemCloseAll); // New
		fileMenu.addSeparator();
		fileMenu.add(menuItemCloseApp);

		/*
		 * Edit menu
		 */
		JMenu editMenu = new JMenu(prism.getLanguage().get(56));

		menuItemUndo = createMenuItem(prism.getLanguage().get(164), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
		menuItemUndo.addActionListener((_) -> {
			TextArea textArea = prism.getTextAreaTabbedPane().getCurrentFile().getTextArea();

			if (textArea.canUndo()) {
				textArea.undoLastAction();
			}
		});

		menuItemRedo = createMenuItem(prism.getLanguage().get(165), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
		menuItemRedo.addActionListener((_) -> {
			TextArea textArea = prism.getTextAreaTabbedPane().getCurrentFile().getTextArea();

			if (textArea.canRedo()) {
				textArea.redoLastAction();
			}
		});

		menuItemCut = createMenuItem(prism.getLanguage().get(166), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
		menuItemCut.addActionListener((_) -> {
			TextArea textArea = prism.getTextAreaTabbedPane().getCurrentFile().getTextArea();

			textArea.cut();
		});

		menuItemCopy = createMenuItem(prism.getLanguage().get(40), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
		menuItemCopy.addActionListener((_) -> {
			TextArea textArea = prism.getTextAreaTabbedPane().getCurrentFile().getTextArea();

			textArea.copy();
		});

		menuItemPaste = createMenuItem(prism.getLanguage().get(41), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
		menuItemPaste.addActionListener((_) -> {
			TextArea textArea = prism.getTextAreaTabbedPane().getCurrentFile().getTextArea();

			textArea.paste();
		});

		menuItemDelete = createMenuItem(prism.getLanguage().get(57), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		menuItemDelete.addActionListener((_) -> {
			TextArea textArea = prism.getTextAreaTabbedPane().getCurrentFile().getTextArea();

			textArea.replaceSelection("");
		});

		menuItemSelectAll = createMenuItem(prism.getLanguage().get(170), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
		menuItemSelectAll.addActionListener((_) -> {
			TextArea textArea = prism.getTextAreaTabbedPane().getCurrentFile().getTextArea();

			textArea.selectAll();
		});

		menuItemOptions = createMenuItem(prism.getLanguage().get(171), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK + KeyEvent.ALT_DOWN_MASK));
		menuItemOptions.addActionListener((_) -> {
			new ConfigurationDialog();
		});

		editMenu.add(menuItemUndo);
		editMenu.add(menuItemRedo);
		editMenu.addSeparator();
		editMenu.add(menuItemCut);
		editMenu.add(menuItemCopy);
		editMenu.add(menuItemPaste);
		editMenu.add(menuItemDelete);
		editMenu.add(menuItemSelectAll);
		editMenu.addSeparator();
		editMenu.add(menuItemOptions);

		/*
		 * View menu
		 */
		JMenu viewMenu = new JMenu(prism.getLanguage().get(147));

		menuItemSidebar = createMenuItem(prism.getLanguage().get(172), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
		menuItemSidebar.addActionListener((_) -> {
			prism.addBackComponent(ComponentType.SIDEBAR);

			updateComponent();
		});

		menuItemLowerSidebar = createMenuItem(prism.getLanguage().get(173), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
		menuItemLowerSidebar.addActionListener((_) -> {
			prism.addBackComponent(ComponentType.LOWER_SIDEBAR);

			updateComponent();
		});

		viewMenu.add(menuItemSidebar);
		viewMenu.add(menuItemLowerSidebar);

		/*
		 * Tools menu
		 */
		toolsMenu = new JMenu(prism.getLanguage().get(148));

		menuItemNewTool = createMenuItem(prism.getLanguage().get(174), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_F9, KeyEvent.CTRL_DOWN_MASK));
		menuItemNewTool.addActionListener((_) -> {
			new NewToolFrame();
		});

		menuItemEditTools = createMenuItem(prism.getLanguage().get(175), null, null, null);
		menuItemEditTools.addActionListener((_) -> {
			List<Tool> allTools = ToolsManager.getAllTools();

			if (allTools.isEmpty()) {
				JOptionPane.showMessageDialog(
						prism,
						prism.getLanguage().get(63),
						prism.getLanguage().get(64),
						JOptionPane.INFORMATION_MESSAGE
				);
				return;
			}

			String[] toolNames = allTools.stream()
					.map(Tool::getName)
					.toArray(String[]::new);

			String selectedToolName = (String) JOptionPane.showInputDialog(
					prism,
					prism.getLanguage().get(184),
					prism.getLanguage().get(64),
					JOptionPane.QUESTION_MESSAGE,
					null,
					toolNames,
					toolNames[0]
			);

			if (selectedToolName != null) {
				allTools.stream()
						.filter(tool -> tool.getName().equals(selectedToolName))
						.findFirst().ifPresent(EditToolFrame::new);

			}
		});

		menuItemDeleteTools = createMenuItem(prism.getLanguage().get(176), null, null, null);
		menuItemDeleteTools.addActionListener((_) -> {
			List<Tool> allTools = ToolsManager.getAllTools();

			if (allTools.isEmpty()) {
				JOptionPane.showMessageDialog(
						prism,
						prism.getLanguage().get(64),
						prism.getLanguage().get(64),
						JOptionPane.INFORMATION_MESSAGE
				);
				return;
			}

			String[] toolNames = allTools.stream()
					.map(Tool::getName)
					.toArray(String[]::new);

			String selectedToolName = (String) JOptionPane.showInputDialog(
					prism,
					prism.getLanguage().get(185),
					prism.getLanguage().get(64),
					JOptionPane.QUESTION_MESSAGE,
					null,
					toolNames,
					toolNames[0]
			);

			if (selectedToolName != null) {
				allTools.stream()
						.filter(tool -> tool.getName().equals(selectedToolName))
						.findFirst().ifPresent(selectedTool -> ToolsManager.removeToolById(selectedTool.getId()));

			}
		});

		toolsMenu.add(menuItemNewTool);
		toolsMenu.add(menuItemEditTools);
		toolsMenu.add(menuItemDeleteTools);

		toolMenuSeparator = new JSeparator();
		toolsMenu.add(toolMenuSeparator);

		/*
		 * Go menu
		 */
		JMenu goMenu = new JMenu(prism.getLanguage().get(149));

		// --- New Go Menu Items ---
		menuItemGoToLine = createMenuItem(prism.getLanguage().get(177), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));
		menuItemGoToLine.addActionListener((_) -> {
			PrismFile pf = prism.getTextAreaTabbedPane().getCurrentFile();
			File file = pf.getFile();

			if (!pf.isText()) {
				return;
			}

			JSpinner lineSpin = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));

			int lines = countLines(file);
			SpinnerModel sm = lines > 0
					? new SpinnerNumberModel(1, 1, lines, 1)
					: new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
			lineSpin.setModel(sm);

			int res = JOptionPane.showConfirmDialog(prism, lineSpin, prism.getLanguage().get(225), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (res == JOptionPane.OK_OPTION) {
				pf.getTextArea().setCursorOnLine((int) lineSpin.getValue());
			}
		});

		menuItemNextTab = createMenuItem(prism.getLanguage().get(178), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_DOWN_MASK));
		menuItemNextTab.addActionListener((_) -> {
			prism.getTextAreaTabbedPane().nextTab();
		});

		menuItemPreviousTab = createMenuItem(prism.getLanguage().get(179), null, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_DOWN_MASK));
		menuItemPreviousTab.addActionListener((_) -> {
			prism.getTextAreaTabbedPane().previousTab();
		});
		// -------------------------

		goMenu.add(menuItemGoToLine); // New
		goMenu.addSeparator();
		goMenu.add(menuItemNextTab); // New
		goMenu.add(menuItemPreviousTab); // New

		/*
		 * Help menu
		 */
		windowMenu = new JMenu(prism.getLanguage().get(150));

		menuItemWindows = createMenuItem(prism.getLanguage().get(180), null,
				null,
				KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK + KeyEvent.ALT_DOWN_MASK));
		menuItemWindows.addActionListener((_) -> {
			new WindowsFrame();
		});

		windowMenu.add(menuItemWindows);

		windowsMenuSeparator = new JSeparator();
		windowMenu.add(windowsMenuSeparator);

		/*
		 * Help menu
		 */
		JMenu helpMenu = new JMenu(prism.getLanguage().get(151));

		menuItemHelp = createMenuItem(prism.getLanguage().get(181), null,
				null,
				null);
		menuItemHelp.addActionListener((_) -> {
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				try {
					Desktop.getDesktop().browse(new URI("https://tayebyassine.github.io/NeoPrism/"));
				} catch (IOException | URISyntaxException err) {
					WarningDialog.showWarningDialog(prism, err);
				}
			} else {
				JOptionPane.showMessageDialog(prism,
						"Faild to open Help URL.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		// --- New Help Menu Item ---
		menuItemCheckForUpdates = createMenuItem(prism.getLanguage().get(182), null, null, null);
		menuItemCheckForUpdates.addActionListener((_) -> {

		});

		menuItemAbout = createMenuItem(prism.getLanguage().get(183), null,
				null,
				null);
		menuItemAbout.addActionListener((_) -> {
			new AboutPrism();
		});

		helpMenu.add(menuItemHelp);
		helpMenu.add(menuItemCheckForUpdates);
		helpMenu.addSeparator();
		helpMenu.add(menuItemAbout);

		/*
		 * End
		 */
		add(fileMenu);
		add(editMenu);
		add(viewMenu);
		add(toolsMenu);
		add(goMenu);
		add(windowMenu);
		add(helpMenu);
	}

	private static JRadioButtonMenuItem getRadioButtonMenuItem(PrismFile pf) {
		JRadioButtonMenuItem windowItem = new JRadioButtonMenuItem(pf.getName(), pf.getIcon());

		PrismFile current = prism.getTextAreaTabbedPane().getCurrentFile();

		if (current != null && current.equals(pf)) {
			windowItem.setSelected(true);
		}

		windowItem.addActionListener((_) -> {
			prism.getTextAreaTabbedPane().redirectUserToTab(pf);
		});

		return windowItem;
	}

	public void refreshToolsMenu() {
		int initialItemCount = toolsMenu.getMenuComponentCount();
		int separatorIndex = -1;

		for (int i = 0; i < initialItemCount; i++) {
			if (toolsMenu.getMenuComponent(i) == toolMenuSeparator) {
				separatorIndex = i;
				break;
			}
		}

		if (separatorIndex != -1) {
			for (int i = initialItemCount - 1; i > separatorIndex; i--) {
				toolsMenu.remove(i);
			}
		}

		for (Tool tool : ToolsManager.getAllTools()) {
			JMenuItem toolItem = createMenuItem(
					tool.getName(),
					null,
					tool.getDescription().isEmpty() ? null : tool.getDescription(),
					null
			);

			toolItem.addActionListener((_) -> {
				Terminal terminal = prism.getTerminalTabbedPane().getCurrentTerminal();

				new Thread(() -> terminal.executeTool(tool)).start();
			});

			toolsMenu.add(toolItem);
		}

		toolsMenu.revalidate();
		toolsMenu.repaint();
	}

	public void refreshWindowsMenu() {
		int initialItemCount = windowMenu.getMenuComponentCount();
		int separatorIndex = -1;

		for (int i = 0; i < initialItemCount; i++) {
			if (windowMenu.getMenuComponent(i) == windowsMenuSeparator) {
				separatorIndex = i;
				break;
			}
		}

		if (separatorIndex != -1) {
			for (int i = initialItemCount - 1; i > separatorIndex; i--) {
				windowMenu.remove(i);
			}
		}

		ButtonGroup group = new ButtonGroup();

		for (PrismFile pf : FileManager.getFiles()) {
			JRadioButtonMenuItem windowItem = getRadioButtonMenuItem(pf);

			group.add(windowItem);

			windowMenu.add(windowItem);
		}

		windowMenu.revalidate();
		windowMenu.repaint();
	}

	public void updateComponent() {
		PrismFile prismFile = prism.getTextAreaTabbedPane().getCurrentFile();

		if (prismFile == null) {
			return;
		}

		if (prismFile.isText()) {
			TextArea textArea = prismFile.getTextArea();

			menuItemRedo.setEnabled(textArea.canRedo());
			menuItemUndo.setEnabled(textArea.canUndo());

			menuItemSave.setEnabled(!prismFile.isSaved());

			menuItemSaveAs.setEnabled(true);
			menuItemCloseFile.setEnabled(true);
			menuItemCut.setEnabled(true);
			menuItemCopy.setEnabled(true);
			menuItemPaste.setEnabled(true);
			menuItemDelete.setEnabled(true);
			menuItemSelectAll.setEnabled(true);
			menuItemGoToLine.setEnabled(true);
		}

		menuItemSidebar.setEnabled(prism.isComponentRemoved(ComponentType.SIDEBAR));
		menuItemLowerSidebar.setEnabled(prism.isComponentRemoved(ComponentType.LOWER_SIDEBAR));

		menuItemSaveAs.setEnabled(prismFile.isText());
		menuItemCloseFile.setEnabled(prismFile.isText());
		menuItemCut.setEnabled(prismFile.isText());
		menuItemCopy.setEnabled(prismFile.isText());
		menuItemPaste.setEnabled(prismFile.isText());
		menuItemDelete.setEnabled(prismFile.isText());
		menuItemSelectAll.setEnabled(prismFile.isText());
		menuItemGoToLine.setEnabled(prismFile.isText());

		menuItemDeleteTools.setEnabled(!ToolsManager.getAllTools().isEmpty());
		menuItemEditTools.setEnabled(!ToolsManager.getAllTools().isEmpty());

		refreshToolsMenu();
		refreshWindowsMenu();
	}

	private JMenuItem createMenuItem(String text, ImageIcon menuItemIcon, String tooltip, KeyStroke accelerator) {
		JMenuItem menuItem = new JMenuItem(text);

		if (tooltip != null) {
			menuItem.setToolTipText(tooltip);
		}

		if (accelerator != null) {
			menuItem.setAccelerator(accelerator);
		}

		if (menuItemIcon != null) {
			menuItem.setIcon(menuItemIcon);
		}

		return menuItem;
	}

	private int countLines(File f) {
		if (f == null || !f.isFile()) return -1;
		try (BufferedReader br = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
			int count = 0;
			while (br.readLine() != null) count++;
			return count;
		} catch (IOException ex) {
			return -1;
		}
	}
}
