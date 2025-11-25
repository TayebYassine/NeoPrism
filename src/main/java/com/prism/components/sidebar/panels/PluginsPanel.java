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
    private static final Prism prism = Prism.getInstance();

    private final List<Plugin> plugins;
    private final JPanel listPanel = new JPanel();

    public PluginsPanel(List<Plugin> plugins) {
        super(new BorderLayout());
        this.plugins = plugins;

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        JDefaultKineticScrollPane scroll = new JDefaultKineticScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
        rebuildRows();
    }

    private void rebuildRows() {
        listPanel.removeAll();

        for (int i = 0; i < plugins.size(); i++) {
            listPanel.add(createRow(plugins.get(i)));

            if (i < plugins.size() - 1) {
                JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                listPanel.add(sep);
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createRow(Plugin plugin) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JPanel left = new JPanel(new GridLayout(0, 1, 0, 2));
        left.setOpaque(false);
        left.add(new JLabel(htmlBold(escape(plugin.getName()) + " " + escape(plugin.getVersion()))));
        left.add(new JLabel("<html><p style='width:175px;'>" +
                escape(plugin.getDescription() == null ? "" : plugin.getDescription()) +
                "</p></html>"));

        JCheckBox enabledCB = new JCheckBox(prism.getLanguage().get(30));
        enabledCB.setSelected(plugin.getEnabled());
        enabledCB.setFocusable(false);
        enabledCB.addItemListener(e ->
                plugin.setEnabled(e.getStateChange() == ItemEvent.SELECTED));

        JButton editJsonBtn = new JButton("Edit JSON");
        editJsonBtn.setFocusable(false);
        editJsonBtn.addActionListener(a -> FileManager.openFile(plugin.getFile()));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        right.setOpaque(false);
        right.add(enabledCB);
        right.add(editJsonBtn);

        row.add(left, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;");
    }

    private static String htmlBold(String s) {
        return "<html><b>" + escape(s) + "</b></html>";
    }
}