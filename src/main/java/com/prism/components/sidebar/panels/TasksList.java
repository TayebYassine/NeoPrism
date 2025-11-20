package com.prism.components.sidebar.panels;

import com.prism.components.definition.Task;
import com.prism.components.extended.JDefaultKineticScrollPane;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class TasksList extends JPanel {
	public JTable table;
	public DefaultTableModel model;

	public String[] columns = {"File", "Line", "Description"};

	public TasksList() {
		setLayout(new BorderLayout());

		Object[][] data = {};

		model = new DefaultTableModel(data, columns) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		table = new JTable(model);
		table.setFocusable(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		table.getColumnModel().getColumn(0).setPreferredWidth(150);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		table.getColumnModel().getColumn(2).setPreferredWidth(300);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		add(new JDefaultKineticScrollPane(table), BorderLayout.CENTER);
	}

	public void addRow(Task task) {
		String fileName = task.getFile() != null ? task.getFile().getName() : "?";
		String line = String.valueOf(task.getLine());
		model.addRow(new Object[]{fileName, line, task.getDescription()});
	}

	public void removeRow(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < model.getRowCount()) {
			model.removeRow(rowIndex);
		}
	}

	public void clearRows() {
		model.setRowCount(0);
	}

	public void setTasks(List<Task> tasks) {
		clearRows();
		for (Task task : tasks) {
			addRow(task);
		}
	}
}