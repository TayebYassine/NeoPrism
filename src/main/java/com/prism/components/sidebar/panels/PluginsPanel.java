package com.prism.components.sidebar.panels;

import com.prism.Prism;
import com.prism.components.extended.JDefaultKineticScrollPane;
import com.prism.managers.FileManager;
import com.prism.plugins.Plugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;

public class PluginsPanel extends JPanel {
    private static Prism prism = Prism.getInstance();

    public PluginsPanel(List<Plugin> plugins) {
        super(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        for (int i = 0; i < plugins.size(); i++) {
            content.add(createPluginRow(plugins.get(i)));

            if (i < plugins.size() - 1) {
                JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                content.add(sep);
            }
        }

        JDefaultKineticScrollPane sp = new JDefaultKineticScrollPane(content);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setBorder(null);
        add(sp, BorderLayout.CENTER);
    }

    private static JPanel createPluginRow(Plugin plugin) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); // uniform height

        JLabel nameLab = new JLabel(
                String.format("<html><b>%s</b> %s — </html>",
                        escape(plugin.getName()),
                        escape(plugin.getVersion() == null ? "" : plugin.getVersion())));
        nameLab.setFont(nameLab.getFont().deriveFont(Font.PLAIN, 12f));

        JCheckBox enabledCB = new JCheckBox(prism.getLanguage().get(30));
        enabledCB.setSelected(plugin.getEnabled());
        enabledCB.setFocusable(false);
        enabledCB.addItemListener(e ->
                plugin.setEnabled(e.getStateChange() == ItemEvent.SELECTED));

        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleBar.setOpaque(false);
        titleBar.add(nameLab);
        titleBar.add(enabledCB);

        row.add(titleBar, BorderLayout.NORTH);

        JLabel descLab = new JLabel(
                "<html><p style='width:300px;'>" +
                        escape(plugin.getDescription() == null ? "" : plugin.getDescription()) +
                        "</p></html>");
        row.add(descLab, BorderLayout.CENTER);

        JButton editBtn = new JButton(prism.getLanguage().get(31));
        editBtn.setFocusable(false);
        editBtn.addActionListener(a -> {
            FileManager.openFile(plugin.getFile());
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        south.setOpaque(false);
        south.add(editBtn);
        row.add(south, BorderLayout.SOUTH);

        return row;
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;");
    }
}