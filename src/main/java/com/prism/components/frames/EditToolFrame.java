package com.prism.components.frames;

import com.prism.Prism;
import com.prism.components.definition.Tool;
import com.prism.components.extended.JDefaultKineticScrollPane;
import com.prism.components.extended.JExtendedTextField;
import com.prism.managers.ToolsManager;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class EditToolFrame extends JFrame {
	private static final Prism prism = Prism.getInstance();
	private final DefaultListModel<String> argumentsModel = new DefaultListModel<>();
	public Tool tool;
	private JExtendedTextField nameField, descriptionField, shortcutField;
	private JList<String> argumentsList;

	public EditToolFrame(Tool tool) {
		this.tool = tool;

		for (int i = 0; i < tool.getArguments().size(); i++) {
			String arg = tool.getArguments().get(i);

			if (arg != null && !arg.trim().isEmpty()) {
				argumentsModel.addElement(arg.trim());
			}
		}

		setTitle(prism.getLanguage().get(91, tool.getName()));
		setSize(600, 500);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);

		setResizable(false);

		setIconImages(ResourceUtil.getAppIcon());

		init();

		this.nameField.setText(tool.getName());
		this.nameField.setEnabled(false);
		this.descriptionField.setText(tool.getDescription());
		this.shortcutField.setText(tool.getShortcut());

		setVisible(true);
	}

	public void init() {
		JPanel mainPanel = new JPanel(new BorderLayout());

		JPanel fieldsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		nameField = new JExtendedTextField(60);
		nameField.setPlaceholder(prism.getLanguage().get(92));

		descriptionField = new JExtendedTextField(60);
		descriptionField.setPlaceholder(prism.getLanguage().get(93));

		shortcutField = new JExtendedTextField(60);
		shortcutField.setPlaceholder(prism.getLanguage().get(94));

		gbc.weightx = 0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		fieldsPanel.add(new JLabel(prism.getLanguage().get(95)), gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		fieldsPanel.add(nameField, gbc);

		gbc.gridwidth = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 0;
		fieldsPanel.add(new JLabel(prism.getLanguage().get(62)), gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		fieldsPanel.add(descriptionField, gbc);

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		fieldsPanel.add(new JLabel(prism.getLanguage().get(97)), gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		fieldsPanel.add(shortcutField, gbc);

		// Variables

		String variables = """
				Variables:
				- $(FILE_NAME): The current and selected file name (with extension)
				- $(FILE_NAME_NO_EXS): The current and selected file name (without extension)
				- $(FILE_PATH): The current and selected file path
				- $(DIR_PATH): The directory path from File Explorer
				""";

		JTextPane variablesPane = new JTextPane();
		variablesPane.setCaretColor(variablesPane.getBackground());
		variablesPane.setEditable(false);
		variablesPane.setBackground(getBackground());
		variablesPane.setText(variables);

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		fieldsPanel.add(variablesPane, gbc);

		// Command Line Arguments
		JPanel argumentsPanel = new JPanel(new BorderLayout());
		argumentsPanel.setBorder(BorderFactory.createTitledBorder(prism.getLanguage().get(98)));

		//argumentsModel = new DefaultListModel<>();
		argumentsList = new JList<>(argumentsModel);
		argumentsList.setFocusable(true);
		argumentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JDefaultKineticScrollPane scrollPane = new JDefaultKineticScrollPane(argumentsList);

		argumentsPanel.add(scrollPane, BorderLayout.CENTER);

		// Buttons for arguments
		JPanel argButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton addBtn = new JButton(prism.getLanguage().get(16));
		addBtn.setFocusable(true);
		JButton removeBtn = new JButton(prism.getLanguage().get(99));
		removeBtn.setFocusable(true);
		JButton modifyBtn = new JButton(prism.getLanguage().get(100));
		modifyBtn.setFocusable(true);
		modifyBtn.setEnabled(false);
		JButton upBtn = new JButton(prism.getLanguage().get(101));
		upBtn.setFocusable(true);
		upBtn.setEnabled(false);
		JButton downBtn = new JButton(prism.getLanguage().get(102));
		downBtn.setFocusable(true);
		downBtn.setEnabled(false);

		argButtonsPanel.add(addBtn);
		argButtonsPanel.add(removeBtn);
		argButtonsPanel.add(modifyBtn);
		argButtonsPanel.add(upBtn);
		argButtonsPanel.add(downBtn);

		argumentsPanel.add(argButtonsPanel, BorderLayout.SOUTH);

		JPanel mainContent = new JPanel(new BorderLayout());
		mainContent.add(fieldsPanel, BorderLayout.NORTH);
		mainContent.add(argumentsPanel, BorderLayout.CENTER);

		mainPanel.add(mainContent, BorderLayout.CENTER);

		// Bottom OK/Cancel
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton updateBtn = new JButton(prism.getLanguage().get(100));
		updateBtn.setFocusable(true);

		JButton cancelBtn = new JButton(prism.getLanguage().get(13));
		cancelBtn.setFocusable(true);
		bottomPanel.add(updateBtn);
		bottomPanel.add(cancelBtn);

		add(mainPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);

		// ---- Event Handlers ----
		argumentsList.addListSelectionListener(e -> {
			modifyBtn.setEnabled(argumentsList.getSelectedIndex() != -1);
			upBtn.setEnabled(argumentsList.getSelectedIndex() != -1);
			downBtn.setEnabled(argumentsList.getSelectedIndex() != -1);
		});

		addBtn.addActionListener(e -> {
			JPanel panel = new JPanel();
			panel.setLayout(new FlowLayout(FlowLayout.LEFT));

			JExtendedTextField commandArgument = new JExtendedTextField(35);
			commandArgument.setPlaceholder(prism.getLanguage().get(90));

			JComboBox variablesComboBox = new JComboBox(new String[]{
					"$(FILE_NAME)",
					"$(FILE_NAME_NO_EXS)",
					"$(FILE_PATH)",
					"$(DIR_PATH)"
			});
			variablesComboBox.setSelectedIndex(0);
			variablesComboBox.addActionListener((_) -> {
				commandArgument.setText(commandArgument.getText() + variablesComboBox.getSelectedItem().toString());
			});

			panel.add(commandArgument);
			panel.add(variablesComboBox);

			int res = JOptionPane.showConfirmDialog(this, panel, prism.getLanguage().get(103),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (res == JOptionPane.OK_OPTION && !commandArgument.getText().trim().isEmpty()) {
				argumentsModel.addElement(commandArgument.getText().trim());
			}
		});

		removeBtn.addActionListener(e -> {
			int idx = argumentsList.getSelectedIndex();
			if (idx != -1) {
				argumentsModel.remove(idx);
			}
		});

		modifyBtn.addActionListener(e -> {
			int idx = argumentsList.getSelectedIndex();
			if (idx != -1) {
				String current = argumentsModel.get(idx);

				JPanel panel = new JPanel();
				panel.setLayout(new FlowLayout(FlowLayout.LEFT));

				JExtendedTextField commandArgument = new JExtendedTextField(35);
				commandArgument.setText(current);
				commandArgument.setPlaceholder(prism.getLanguage().get(90));

				JComboBox variablesComboBox = new JComboBox(new String[]{
						"$(FILE_NAME)",
						"$(FILE_NAME_NO_EXS)",
						"$(FILE_PATH)",
						"$(DIR_PATH)"
				});
				variablesComboBox.setSelectedIndex(0);
				variablesComboBox.addActionListener((_) -> {
					commandArgument.setText(commandArgument.getText() + variablesComboBox.getSelectedItem().toString());
				});

				panel.add(commandArgument);
				panel.add(variablesComboBox);

				int res = JOptionPane.showConfirmDialog(this, panel, prism.getLanguage().get(96),
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

				if (res == JOptionPane.OK_OPTION && !commandArgument.getText().trim().isEmpty()) {
					argumentsModel.set(idx, commandArgument.getText().trim());
				}
			}
		});

		upBtn.addActionListener(e -> {
			int idx = argumentsList.getSelectedIndex();
			if (idx > 0) {
				String element = argumentsModel.remove(idx);
				argumentsModel.add(idx - 1, element);
				argumentsList.setSelectedIndex(idx - 1);
			}
		});

		downBtn.addActionListener(e -> {
			int idx = argumentsList.getSelectedIndex();
			if (idx != -1 && idx < argumentsModel.size() - 1) {
				String element = argumentsModel.remove(idx);
				argumentsModel.add(idx + 1, element);
				argumentsList.setSelectedIndex(idx + 1);
			}
		});

		updateBtn.addActionListener(e -> {
			if (nameField.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this, prism.getLanguage().get(104), prism.getLanguage().get(10000),
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (!shortcutField.getText().isEmpty() && shortcutField.getText().contains(" ")) {
				JOptionPane.showMessageDialog(this, prism.getLanguage().get(105), prism.getLanguage().get(10000),
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (argumentsModel.isEmpty()) {
				JOptionPane.showMessageDialog(this, prism.getLanguage().get(106), prism.getLanguage().get(10000),
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (!shortcutField.getText().isEmpty() && !tool.getShortcut().equals(shortcutField.getText()) && ToolsManager.isShortcutUnique(shortcutField.getText())) {
				JOptionPane.showMessageDialog(this, prism.getLanguage().get(107), prism.getLanguage().get(10001),
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			tool.setName(nameField.getText());
			tool.setDescription(descriptionField.getText());
			tool.setShortcut(shortcutField.getText());
			tool.setArguments(new ArrayList<>());

			for (int i = 0; i < argumentsModel.size(); i++) {
				tool.addArgument(argumentsModel.get(i));
			}

			ToolsManager.updateTool(tool);

			if (isDisplayable()) {
				dispose();
			}
		});

		cancelBtn.addActionListener(e -> dispose());
	}
}
