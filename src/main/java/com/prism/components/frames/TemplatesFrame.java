package com.prism.components.frames;

import com.prism.Prism;
import com.prism.components.extended.JDefaultKineticScrollPane;
import com.prism.managers.FileManager;
import com.prism.templates.*;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class TemplatesFrame extends JFrame {
	private static final Prism prism = Prism.getInstance();

	private final File directory;
	private final DefaultListModel<AppBase> model = new DefaultListModel<>();
	private final JList<AppBase> list = new JList<>(model);
	private final JLabel statusLabel = new JLabel(prism.getLanguage().get(243));

	public TemplatesFrame(File directory) {
		super(Prism.getInstance().getLanguage().get(242));
		this.directory = directory;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(500, 400);
		setLocationRelativeTo(null);
		setResizable(false);
		setIconImages(ResourceUtil.getAppIcon());
		setLayout(new BorderLayout());

		initComponents();
		loadTemplates();
		setVisible(true);
	}

	private void initComponents() {
		list.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		list.setCellRenderer(new TemplateListRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(e -> {
			var selected = list.getSelectedValue();
			statusLabel.setText(selected != null ? prism.getLanguage().get(245, selected.getName()) : prism.getLanguage().get(243));
		});

		JDefaultKineticScrollPane scroll = new JDefaultKineticScrollPane(list);
		scroll.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 10, 10, 10), // outer padding
				scroll.getBorder() // keep original border
		));
		add(scroll, BorderLayout.CENTER);

		JPanel south = new JPanel(new BorderLayout());
		south.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		south.add(statusLabel, BorderLayout.WEST);

		JButton okButton = new JButton(prism.getLanguage().get(12));
		okButton.addActionListener(e -> createSelectedTemplate());
		south.add(okButton, BorderLayout.EAST);

		add(south, BorderLayout.SOUTH);
	}

	private void loadTemplates() {
		TemplateRegistry.getAll().forEach(model::addElement);
	}

	private void createSelectedTemplate() {
		var selected = list.getSelectedValue();
		if (selected == null) return;

		if (!directory.mkdirs() && !directory.exists()) {
			JOptionPane.showMessageDialog(this, prism.getLanguage().get(77), prism.getLanguage().get(10002), JOptionPane.ERROR_MESSAGE);
			return;
		}

		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() {
				selected.create(directory);
				return null;
			}

			@Override
			protected void done() {
				FileManager.setRootDirectory(directory);
				Prism.getInstance().getTextAreaTabbedPane().closeAllTabs();
				dispose();
			}
		};
		worker.execute();
	}

	private static class TemplateListRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,
													  boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof AppBase t) {
				setText(t.getName());
				setIcon(t.getIcon());
			}
			return this;
		}
	}

	public static class TemplateRegistry {
		public static List<AppBase> getAll() {
			return List.of(
					new AppBase("Empty", "icons/ui/folder.svg"),
					new AppC(),
					new AppCpp(),
					new AppCppGUI(),
					new AppJava(),
					new AppJavaSwingGUI()
			);
		}
	}
}