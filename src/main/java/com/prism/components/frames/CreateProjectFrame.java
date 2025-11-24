package com.prism.components.frames;

import com.prism.Prism;
import com.prism.managers.FileManager;
import com.prism.services.Service;
import com.prism.services.ServiceForC;
import com.prism.services.ServiceForCPlusPlus;
import com.prism.services.ServiceForJava;
import com.prism.utils.ProjectNameGenerator;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class CreateProjectFrame extends JFrame {
	private static final Prism prism = Prism.getInstance();

	private static final String[] languages = {"Empty (no pre-set language)", "C", "C++", "Java"};
	private final Service[] services = {null, new ServiceForC(), new ServiceForCPlusPlus(), new ServiceForJava()};

	private long clickCounter = 0;
	private JTextField projectNameField;
	private JComboBox<String> languageCombo;
	private JTextField parentDirField;

    public CreateProjectFrame() {
		super();

		setTitle(prism.getLanguage().get(8));
		setSize(600, 220);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);

		setResizable(false);

		setIconImages(ResourceUtil.getAppIcon());

		init();

		setVisible(true);
	}

	private void init() {
		JPanel main = new JPanel(new BorderLayout(8, 8));
		main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel form = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 6, 6, 6);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Project Name
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		form.add(new JLabel(prism.getLanguage().get(9)), gbc);

		JPanel projectNamePanel = new JPanel(new BorderLayout(6, 6));
		projectNameField = new JTextField();
		projectNameField.setText(ProjectNameGenerator.randomProjectName());
		projectNamePanel.add(projectNameField, BorderLayout.CENTER);
		JButton randomNameButton = new JButton(prism.getLanguage().get(235));
		projectNamePanel.add(randomNameButton, BorderLayout.EAST);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		form.add(projectNamePanel, gbc);

		// Language
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		form.add(new JLabel(prism.getLanguage().get(10)), gbc);

		languageCombo = new JComboBox<>(languages);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		form.add(languageCombo, gbc);

		// Parent Directory
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		form.add(new JLabel(prism.getLanguage().get(11)), gbc);

		JPanel dirPanel = new JPanel(new BorderLayout(6, 6));
		parentDirField = new JTextField();
		parentDirField.setText(FileManager.getRootDirectory().getAbsolutePath());
		parentDirField.setEnabled(false);
		dirPanel.add(parentDirField, BorderLayout.CENTER);
        JButton chooseDirButton = new JButton(prism.getLanguage().get(144));
		dirPanel.add(chooseDirButton, BorderLayout.EAST);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		form.add(dirPanel, gbc);

		main.add(form, BorderLayout.CENTER);

		// Buttons
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton createButton = new JButton(prism.getLanguage().get(12));
        JButton cancelButton = new JButton(prism.getLanguage().get(13));
		buttons.add(createButton);
		buttons.add(cancelButton);
		main.add(buttons, BorderLayout.SOUTH);

		add(main);

		// Listeners
		randomNameButton.addActionListener(e -> {
			projectNameField.setText(ProjectNameGenerator.randomProjectName());

			clickCounter++;

			if (clickCounter >= 100) {
				JOptionPane.showMessageDialog(
						prism,
						prism.getLanguage().get(236),
						"",
						JOptionPane.PLAIN_MESSAGE
				);
			}
		});
		chooseDirButton.addActionListener(e -> onChooseDirectory());
		cancelButton.addActionListener(e -> onCancel());
		createButton.addActionListener(e -> onCreateProject());
	}

	private void onChooseDirectory() {
		JFileChooser chooser = new JFileChooser(parentDirField.getText());
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(FileManager.getRootDirectory());
		chooser.setDialogTitle(prism.getLanguage().get(14));

		int res = chooser.showOpenDialog(this);

		if (res == JFileChooser.APPROVE_OPTION) {
			File dir = chooser.getSelectedFile();
			parentDirField.setText(dir.getAbsolutePath());
		}
	}

	private void onCancel() {
		dispose();
	}

	private void onCreateProject() {
		String projectName = projectNameField.getText().trim();
		int langIndex = languageCombo.getSelectedIndex();

		if (projectName.isEmpty()) {
			JOptionPane.showMessageDialog(this, prism.getLanguage().get(72), prism.getLanguage().get(10000), JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (parentDirField.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, prism.getLanguage().get(73), prism.getLanguage().get(10000), JOptionPane.ERROR_MESSAGE);
			return;
		}

		File parentDir = new File(parentDirField.getText().trim());

		if (!parentDir.exists() || !parentDir.isDirectory()) {
			JOptionPane.showMessageDialog(this, prism.getLanguage().get(74), prism.getLanguage().get(10001), JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (projectName.contains("\\") || projectName.contains("/") || projectName.contains(":")) {
			JOptionPane.showMessageDialog(this, prism.getLanguage().get(75), prism.getLanguage().get(10001), JOptionPane.ERROR_MESSAGE);
			return;
		}

		File projectDir = new File(parentDir, projectName);
		if (projectDir.exists()) {
			JOptionPane.showMessageDialog(this, prism.getLanguage().get(76), prism.getLanguage().get(10001), JOptionPane.ERROR_MESSAGE);
			return;
		}

		boolean created = projectDir.mkdirs();
		if (!created) {
			JOptionPane.showMessageDialog(this, prism.getLanguage().get(77), prism.getLanguage().get(10002), JOptionPane.ERROR_MESSAGE);
			return;
		}

		Service chosenService = services[langIndex];

		if (chosenService != null) {
			boolean createdProjectFiles = chosenService.createNewProject(projectDir);

			if (!createdProjectFiles) {
				JOptionPane.showMessageDialog(this, prism.getLanguage().get(78), prism.getLanguage().get(10003), JOptionPane.WARNING_MESSAGE);
				return;
			}
		}

		FileManager.setRootDirectory(projectDir);
		prism.getTextAreaTabbedPane().closeAllTabs();

		dispose();
	}
}

