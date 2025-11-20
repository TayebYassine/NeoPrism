package com.prism.components.toolbar;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.definition.PrismFile;
import com.prism.components.definition.Task;
import com.prism.components.extended.JExtendedTextField;
import com.prism.managers.FileManager;
import com.prism.managers.TasksManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class TasksToolbar extends JPanel {

    private static final Prism prism = Prism.getInstance();

    public TasksToolbar(Prism prism) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBorder(new EmptyBorder(5, 5, 5, 0));

        JButton btnNew = createButton(prism.getLanguage().get(55), null, prism.getLanguage().get(199));
        btnNew.addActionListener(e -> onNewTask());

        JButton btnEdit = createButton(prism.getLanguage().get(56), null, prism.getLanguage().get(200));
        btnEdit.addActionListener(e -> onEditTask());

        JButton btnDel = createButton(prism.getLanguage().get(57), null, prism.getLanguage().get(201));
        btnDel.addActionListener(e -> onDeleteTask());

        JCheckBox chkNotify = new JCheckBox(prism.getLanguage().get(54));
        chkNotify.setSelected(prism.getConfig().getBoolean(ConfigKey.TASKS_NOTIFICATION_ENABLED, true));
        chkNotify.setFocusable(true);
        chkNotify.addActionListener(e ->
                prism.getConfig().set(ConfigKey.TASKS_NOTIFICATION_ENABLED, chkNotify.isSelected()));

        add(btnNew);
        add(Box.createRigidArea(new Dimension(4, 0)));
        add(btnEdit);
        add(Box.createRigidArea(new Dimension(4, 0)));
        add(btnDel);
        add(Box.createRigidArea(new Dimension(4, 0)));
        add(chkNotify);

        add(new JPanel(), BorderLayout.CENTER);
    }

    private void onNewTask() {
        Object[] ans = showNewTaskDialog(prism.getLanguage().get(58));
        if (ans == null) return;
        File file = (File) ans[0];
        int line = (int) ans[1];
        String description = (String) ans[2];

        Task t = new Task(file, line, description);

        prism.getTasksList().addRow(t);
        TasksManager.addTask(t);
    }

    private void onEditTask() {
        int idx = prism.getTasksList().table.getSelectedRow();
        if (idx == -1) {
            return;
        }

        Object[] ans = showNewTaskDialog(prism.getLanguage().get(59));
        if (ans == null) return;

        File newFile = (File) ans[0];
        int newLine = (int) ans[1];
        String newDesc = (String) ans[2];

        DefaultTableModel m = prism.getTasksList().model;
        m.setValueAt(newFile.getName(), idx, 0);
        m.setValueAt(newDesc, idx, 2);

        int i = 0;
        for (Task task : TasksManager.getAllTasks()) {
            if (i == idx) {
                Task updatedTask = new Task(task.getId(), newFile.getAbsolutePath(), newLine, newDesc);

                TasksManager.updateTask(updatedTask);

                break;
            }

            i++;
        }
    }

    private void onDeleteTask() {
        int idx = prism.getTasksList().table.getSelectedRow();

        if (idx == -1) {
            return;
        }

        prism.getTasksList().removeRow(idx);

        int i = 0;
        for (Task task : TasksManager.getAllTasks()) {
            if (i == idx) {
                TasksManager.removeTaskById(task.getId());
                break;
            }

            i++;
        }
    }

    private JButton createButton(String label, ImageIcon icon, String tooltip) {
        JButton b = new JButton(label);

        b.setFocusable(true);

        if (tooltip != null) b.setToolTipText(tooltip);

        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            b.setIcon(new ImageIcon(img));
        }

        return b;
    }

    private Object[] showNewTaskDialog(String title) {
        JComboBox<String> fileCbo = new JComboBox<>();
        Map<String, File> pathToFile = new LinkedHashMap<>();

        for (PrismFile pf : FileManager.getFiles()) {
            String abs = pf.getFile().getAbsolutePath();

            pathToFile.put(abs, pf.getFile());
            fileCbo.addItem(abs);
        }

        JSpinner lineSpin = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        fileCbo.addActionListener(e -> {
            File selected = pathToFile.get(fileCbo.getSelectedItem());
            int lines = countLines(selected);
            SpinnerModel sm = lines > 0
                    ? new SpinnerNumberModel(1, 1, lines, 1)
                    : new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
            lineSpin.setModel(sm);
        });

        if (!pathToFile.isEmpty()) {
            fileCbo.setSelectedIndex(0);
        }

        JTextField descFld = new JExtendedTextField(20);

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; p.add(new JLabel(prism.getLanguage().get(60)), gbc);
        gbc.gridx = 1; p.add(fileCbo, gbc);

        gbc.gridx = 0; gbc.gridy++; p.add(new JLabel(prism.getLanguage().get(61)), gbc);
        gbc.gridx = 1; p.add(lineSpin, gbc);

        gbc.gridx = 0; gbc.gridy++; p.add(new JLabel(prism.getLanguage().get(62)), gbc);
        gbc.gridx = 1; p.add(descFld, gbc);

        if (JOptionPane.showConfirmDialog(prism, p, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)
                != JOptionPane.OK_OPTION) return null;

        return new Object[]{
                pathToFile.get(fileCbo.getSelectedItem()),
                lineSpin.getValue(),
                descFld.getText().trim()
        };
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