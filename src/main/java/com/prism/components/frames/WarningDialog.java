package com.prism.components.frames;

import com.prism.Prism;
import com.prism.components.extended.JDefaultKineticScrollPane;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class WarningDialog extends JDialog {
	private static final Prism prism = Prism.getInstance();

	public WarningDialog(Frame parent, Throwable throwable) {
		this(parent, "Something went terribly wrong. To avoid further damage, please restart the application.<br>The issue was stopped from continuing its execution, some features may not work.", throwable);
		setVisible(true);
	}

	public WarningDialog(Frame parent, String message, Throwable throwable) {
		super(parent, "Resolved Error", true);

		setSize(200, 200);
		setLayout(new BorderLayout(10, 10));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// Main error message
		JLabel label = new JLabel("<html><b>An unexpected error occurred:</b><br>" + message + "</html>");
		label.setBorder(new EmptyBorder(5, 5, 5, 5));
		label.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
		add(label, BorderLayout.NORTH);

		// Stack trace area
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		JTextArea textArea = new JTextArea(sw.toString());
		textArea.setEditable(false);
		JDefaultKineticScrollPane scrollPane = new JDefaultKineticScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(500, 200));
		scrollPane.setVisible(false);

		// Buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton detailsButton = new JButton("Show Details >>");
		detailsButton.setFocusable(true);
		JButton copyButton = new JButton("Copy to Clipboard");
		copyButton.setFocusable(true);
		JButton closeButton = new JButton("Close");
		closeButton.setFocusable(true);

		buttonPanel.add(copyButton);
		buttonPanel.add(detailsButton);
		buttonPanel.add(closeButton);
		add(buttonPanel, BorderLayout.SOUTH);

		add(scrollPane, BorderLayout.CENTER);

		// Button actions
		detailsButton.addActionListener((ActionEvent e) -> {
			boolean visible = scrollPane.isVisible();
			scrollPane.setVisible(!visible);
			detailsButton.setText(visible ? "Hide Details >>" : "Show Details <<");
			pack();
		});

		copyButton.addActionListener((ActionEvent e) -> {
			Toolkit.getDefaultToolkit()
					.getSystemClipboard()
					.setContents(new StringSelection(sw.toString()), null);

			copyButton.setText("Copied!");

			Prism.setTimeout(() -> {
				if (this.isDisplayable()) {
					copyButton.setText("Copy to Clipboard");
				}
			}, 3 * 1000);
		});

		closeButton.addActionListener((ActionEvent e) -> dispose());

		pack();
		setLocationRelativeTo(parent);

		setVisible(true);
	}

	public static void showWarningDialog(Frame parent, String message, Throwable throwable) {
		WarningDialog dialog = new WarningDialog(parent, message, throwable);
		dialog.setVisible(true);
	}

	public static void showWarningDialog(Frame parent, Throwable throwable) {
		WarningDialog dialog = new WarningDialog(parent, throwable);
		dialog.setVisible(true);
	}
}
