package com.prism.components.toolbar;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.definition.DatabaseProvider;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class DatabaseToolbar extends JToolBar {
	public static final Prism prism = Prism.getInstance();

	public static JButton buttonConnect;
	public static JButton buttonExecuteScript;
	public static JButton buttonStop;
	public static JComboBox comboboxProvider;
	public static JLabel status;

	public DatabaseToolbar(Prism prism) {
		setFloatable(false);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		setBorder(new EmptyBorder(5, 5, 5, 0));

		buttonConnect = createButton(ResourceUtil.getIconFromSVG("icons/ui/database-plus.svg"), prism.getLanguage().get(188));
		buttonConnect.addActionListener((_) -> {
			prism.getDatabase().onConnect();
		});

		buttonExecuteScript = createButton(ResourceUtil.getIconFromSVG("icons/ui/bolt.svg"), prism.getLanguage().get(189));
		buttonExecuteScript.setEnabled(false);
		buttonExecuteScript.addActionListener((_) -> {
			prism.getDatabase().onRun();
		});

		buttonStop = createButton(ResourceUtil.getIconFromSVG("icons/ui/stop.svg"), prism.getLanguage().get(190));
		buttonStop.setEnabled(false);
		buttonStop.addActionListener((_) -> {
			prism.getDatabase().closeConnection();
		});

		String[] terminalShells = {"SQLite", "MySQL"};
		comboboxProvider = new JComboBox<>(terminalShells);
		comboboxProvider.setFocusable(true);

		Dimension preferredSize = comboboxProvider.getPreferredSize();
		preferredSize.width += 20;
		comboboxProvider.setPreferredSize(preferredSize);
		comboboxProvider.setSelectedIndex(prism.getConfig().getInt(ConfigKey.DATABASE_SELECTED_PROVIDER, 0));
		comboboxProvider.addActionListener((_) -> {
			prism.getConfig().set(ConfigKey.DATABASE_SELECTED_PROVIDER, comboboxProvider.getSelectedIndex());
		});

		status = new JLabel(prism.getLanguage().get(53));
		status.setIcon(ResourceUtil.getIconFromSVG("icons/ui/database-x.svg", 16, 16));

		add(buttonConnect);
		add(Box.createRigidArea(new Dimension(4, 0)));
		add(buttonExecuteScript);
		add(Box.createRigidArea(new Dimension(4, 0)));
		add(buttonStop);

		add(Box.createRigidArea(new Dimension(4, 0)));
		addSeparator(new Dimension(4, 20));
		add(Box.createRigidArea(new Dimension(4, 0)));

		add(comboboxProvider);
		add(Box.createRigidArea(new Dimension(4, 0)));
		add(status);

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

	public DatabaseProvider getSelectedProvider() {
		return DatabaseProvider.fromValue(comboboxProvider.getSelectedIndex());
	}

	public void updateComponent() {
		buttonConnect.setEnabled(!prism.getDatabase().isConnected());
		buttonStop.setEnabled(prism.getDatabase().isConnected());
		buttonExecuteScript.setEnabled(prism.getDatabase().isConnected());

		if (prism.getDatabase().isConnected()) {
			status.setText(prism.getLanguage().get(52));
			status.setIcon(ResourceUtil.getIconFromSVG("icons/ui/database2.svg", 16, 16));
		} else {
			status.setText(prism.getLanguage().get(53));
			status.setIcon(ResourceUtil.getIconFromSVG("icons/ui/database-x.svg", 16, 16));
		}
	}
}
