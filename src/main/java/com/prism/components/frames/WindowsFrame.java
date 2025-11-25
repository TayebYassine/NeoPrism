package com.prism.components.frames;

import com.prism.Prism;
import com.prism.components.definition.PrismFile;
import com.prism.components.extended.JDefaultKineticScrollPane;
import com.prism.managers.FileManager;
import com.prism.utils.FileUtil;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WindowsFrame extends JFrame {
	private static Prism prism = Prism.getInstance();

	private JTable fileTable;
	private JButton redirectButton;
	private JButton closeButton;
	private JButton closeWindowsButton;
	private DefaultTableModel tableModel;

	public WindowsFrame() {
		initializeUI();
		loadFiles();

		setVisible(true);
	}

	private void initializeUI() {
		int docs = FileManager.getFiles().size();

		setTitle(prism.getLanguage().get(111, docs));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(800, 600);
		setResizable(false);
		setLocationRelativeTo(null);
		setIconImages(ResourceUtil.getAppIcon());

		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		String[] columnNames = {prism.getLanguage().get(140), prism.getLanguage().get(141), prism.getLanguage().get(142), prism.getLanguage().get(143)};
		tableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		fileTable = new JTable(tableModel);
		fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fileTable.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

		fileTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		fileTable.getColumnModel().getColumn(1).setPreferredWidth(300);
		fileTable.getColumnModel().getColumn(2).setPreferredWidth(80);
		fileTable.getColumnModel().getColumn(3).setPreferredWidth(100);

		JDefaultKineticScrollPane scrollPane = new JDefaultKineticScrollPane(fileTable);
		scrollPane.setPreferredSize(new Dimension(600, 400));

		JPanel buttonManagePanel = new JPanel();
		buttonManagePanel.setLayout(new BoxLayout(buttonManagePanel, BoxLayout.Y_AXIS));
		buttonManagePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		redirectButton = new JButton(prism.getLanguage().get(112));
		closeButton = new JButton(prism.getLanguage().get(15));
		closeWindowsButton = new JButton(prism.getLanguage().get(46));

		redirectButton.setEnabled(false);
		closeButton.setEnabled(false);

		Dimension buttonSize = new Dimension(120, 30);
		redirectButton.setPreferredSize(buttonSize);
		redirectButton.setMaximumSize(buttonSize);
		closeButton.setPreferredSize(buttonSize);
		closeButton.setMaximumSize(buttonSize);
		closeWindowsButton.setPreferredSize(buttonSize);
		closeWindowsButton.setMaximumSize(buttonSize);

		redirectButton.addActionListener(e -> redirectToFile());
		closeButton.addActionListener(e -> closeSelectedFile());
		closeWindowsButton.addActionListener(e -> {
			prism.getTextAreaTabbedPane().closeAllTabs();

			loadFiles();
		});

		buttonManagePanel.add(Box.createVerticalGlue());
		buttonManagePanel.add(redirectButton);
		buttonManagePanel.add(Box.createRigidArea(new Dimension(0, 10)));
		buttonManagePanel.add(closeButton);
		buttonManagePanel.add(Box.createRigidArea(new Dimension(0, 10)));
		buttonManagePanel.add(closeWindowsButton);
		buttonManagePanel.add(Box.createVerticalGlue());

		mainPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BorderLayout());

		JButton okButton = new JButton("OK");
		okButton.addActionListener((e) -> dispose());
		okButton.setPreferredSize(buttonSize);
		okButton.setMaximumSize(buttonSize);

		buttonsPanel.add(buttonManagePanel, BorderLayout.NORTH);
		buttonsPanel.add(okButton, BorderLayout.SOUTH);

		mainPanel.add(buttonsPanel, BorderLayout.EAST);

		add(mainPanel);
	}

	private void loadFiles() {
		tableModel.setRowCount(0);

		CopyOnWriteArrayList<PrismFile> files = FileManager.getFiles();

		for (PrismFile prismFile : files) {
			Object[] row = new Object[4];
			row[0] = prismFile.getName();
			row[1] = prismFile.getAbsolutePath();
			row[2] = prismFile.isHomepage() ? "?" : (prismFile.isImage() ? prism.getLanguage().get(139) : prism.getLanguage().get(138));

			File file = prismFile.getFile();
			if (file != null && file.exists()) {
				long sizeInBytes = file.length();
				row[3] = FileUtil.formatFileSize(sizeInBytes);
			} else {
				row[3] = "?";
			}

			tableModel.addRow(row);
		}

		int docs = FileManager.getFiles().size();

		setTitle(prism.getLanguage().get(111, docs));
	}

	private void updateButtonStates() {
		boolean hasSelection = fileTable.getSelectedRow() != -1;
		redirectButton.setEnabled(hasSelection);
		closeButton.setEnabled(hasSelection);
	}

	private void redirectToFile() {
		int selectedRow = fileTable.getSelectedRow();
		if (selectedRow != -1) {
			CopyOnWriteArrayList<PrismFile> files = FileManager.getFiles();
			if (selectedRow < files.size()) {
				PrismFile selectedFile = files.get(selectedRow);

				FileManager.openFile(selectedFile.getFile());
			}
		}
	}

	private void closeSelectedFile() {
		int selectedRow = fileTable.getSelectedRow();

		if (selectedRow != -1) {
			CopyOnWriteArrayList<PrismFile> files = FileManager.getFiles();
			if (selectedRow < files.size()) {
				PrismFile selectedFile = files.get(selectedRow);

				int index = prism.getTextAreaTabbedPane().findIndexByPrismFile(selectedFile);

				prism.getTextAreaTabbedPane().closeTabByIndex(index, true);

				loadFiles();
			}
		}
	}
}
