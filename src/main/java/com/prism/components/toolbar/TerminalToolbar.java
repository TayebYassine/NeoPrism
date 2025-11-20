package com.prism.components.toolbar;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.definition.PrismFile;
import com.prism.components.definition.Shell;
import com.prism.components.definition.Tool;
import com.prism.components.terminal.Terminal;
import com.prism.managers.FileManager;
import com.prism.managers.TerminalManager;
import com.prism.managers.ToolsManager;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

import static javax.swing.JOptionPane.*;

public class TerminalToolbar extends JToolBar {

	public static JComboBox comboboxTerminalShell;

	public TerminalToolbar(Prism prism) {
		setFloatable(false);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		setBorder(new EmptyBorder(5, 5, 5, 0));

		JButton buttonNewTerminal = createButton(ResourceUtil.getIconFromSVG("icons/ui/plus.svg"), prism.getLanguage().get(202));
		buttonNewTerminal.addActionListener((_) -> {
			TerminalManager.newTerminal(Shell.fromValue(comboboxTerminalShell.getSelectedIndex()));
		});

		JButton buttonCopy = createButton(ResourceUtil.getIconFromSVG("icons/ui/copy.svg"), prism.getLanguage().get(203));
		buttonCopy.addActionListener((_) -> {
			Terminal terminal = prism.getTerminalTabbedPane().getCurrentTerminal();

			terminal.getTerminalArea().selectAll();
			terminal.getTerminalArea().copy();
			terminal.getTerminalArea().select(0, 0);
		});

		JButton buttonClearOutput = createButton(ResourceUtil.getIconFromSVG("icons/ui/eraser.svg"), prism.getLanguage().get(204));
		buttonClearOutput.addActionListener((_) -> {
			Terminal terminal = prism.getTerminalTabbedPane().getCurrentTerminal();

			terminal.clearTerminal();
		});

		JButton buttonSync = createButton(ResourceUtil.getIconFromSVG("icons/ui/sync.svg"), prism.getLanguage().get(205));
		buttonSync.addActionListener((_) -> {
			Terminal terminal = prism.getTerminalTabbedPane().getCurrentTerminal();

			PrismFile file = prism.getTextAreaTabbedPane().getCurrentFile();

			if (file != null && file.getFile() != null) {
				terminal.executeCommandSync("cd " + file.getFile().getParentFile().getAbsolutePath());
			}
		});

		JButton buttonProcessStart = createButton(ResourceUtil.getIconFromSVG("icons/ui/execute.svg"), prism.getLanguage().get(206));
		buttonProcessStart.addActionListener((_) -> {
			Terminal terminal = prism.getTerminalTabbedPane().getCurrentTerminal();

			List<Tool> allTools = ToolsManager.getAllTools();

			if (allTools.isEmpty()) {
				JOptionPane.showMessageDialog(prism, prism.getLanguage().get(63),
						prism.getLanguage().get(64),
						INFORMATION_MESSAGE);
				return;
			}

			String[] toolNames = allTools.stream()
					.map(Tool::getName)
					.toArray(String[]::new);

			String selectedToolName = (String) JOptionPane.showInputDialog(
					prism,
					prism.getLanguage().get(65),
					prism.getLanguage().get(64),
					JOptionPane.QUESTION_MESSAGE,
					null,
					toolNames,
					toolNames[0]
			);

			if (selectedToolName != null) {
                allTools.stream()
                        .filter(tool -> tool.getName().equals(selectedToolName))
                        .findFirst().ifPresent(terminal::executeTool);
            }
		});

		JButton buttonProcessRestart = createButton(ResourceUtil.getIconFromSVG("icons/ui/restart.svg"), prism.getLanguage().get(207));
		buttonProcessRestart.addActionListener((_) -> {
			Terminal terminal = prism.getTerminalTabbedPane().getCurrentTerminal();

			//terminal.restartProcess();
		});

		JButton buttonProcessStop = createButton(ResourceUtil.getIconFromSVG("icons/ui/stop.svg"), prism.getLanguage().get(208));
		buttonProcessStop.addActionListener((_) -> {
			Terminal terminal = prism.getTerminalTabbedPane().getCurrentTerminal();

			terminal.closeProcess();
		});

		String[] terminalShells = {"Command Prompt", "PowerShell"};
		comboboxTerminalShell = new JComboBox<>(terminalShells);
		comboboxTerminalShell.setSelectedIndex(prism.getConfig().getInt(ConfigKey.DEFAULT_TERMINAL_SHELL, 0));
		comboboxTerminalShell.setFocusable(true);
		comboboxTerminalShell.addActionListener((_) -> {
			prism.getConfig().set(ConfigKey.DEFAULT_TERMINAL_SHELL, comboboxTerminalShell.getSelectedIndex());
		});

		Dimension preferredSize = comboboxTerminalShell.getPreferredSize();
		preferredSize.width += 20;
		comboboxTerminalShell.setPreferredSize(preferredSize);

		add(buttonNewTerminal);
		add(Box.createRigidArea(new Dimension(4, 0)));
		add(buttonCopy);
		add(Box.createRigidArea(new Dimension(4, 0)));
		add(buttonClearOutput);
		add(Box.createRigidArea(new Dimension(4, 0)));
		add(buttonSync);

		add(Box.createRigidArea(new Dimension(4, 0)));
		addSeparator(new Dimension(4, 20));
		add(Box.createRigidArea(new Dimension(4, 0)));

		add(buttonProcessStart);
		add(Box.createRigidArea(new Dimension(4, 0)));
		add(buttonProcessRestart);
		add(Box.createRigidArea(new Dimension(4, 0)));
		add(buttonProcessStop);

		add(Box.createRigidArea(new Dimension(4, 0)));
		addSeparator(new Dimension(4, 20));
		add(Box.createRigidArea(new Dimension(4, 0)));

		add(comboboxTerminalShell);

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
