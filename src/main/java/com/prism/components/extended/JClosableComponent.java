package com.prism.components.extended;

import com.prism.Prism;
import com.prism.components.definition.ComponentType;
import com.prism.components.definition.ConfigKey;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class JClosableComponent extends JPanel {
    private static final Prism prism = Prism.getInstance();

    public final ComponentType type;

    public JClosableComponent(ComponentType type, JComponent header, JComponent component) {
        this.type = type;

        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());

        header.setOpaque(false);
        header.setBorder(new EmptyBorder(5, 5, 5, 0));

        headerPanel.add(header, BorderLayout.WEST);

        JButton closeButton = new JButton();
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(true);
        closeButton.setContentAreaFilled(false);
        closeButton.setPreferredSize(new Dimension(16, 16));

        ImageIcon closeIcon = ResourceUtil.getIconFromSVG("icons/ui/minus.svg");

        if (closeIcon != null) {
            Image scaledImage = closeIcon.getImage().getScaledInstance(12, 12,
                    Image.SCALE_FAST);
            closeButton.setIcon(new ImageIcon(scaledImage));
        }

        closeButton.addActionListener((_) -> {
            closeComponent();

            prism.getPrismMenuBar().updateComponent();
        });
        headerPanel.add(closeButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        add(component, BorderLayout.CENTER);
    }

    public ComponentType getType() {
        return this.type;
    }

    public boolean isClosed() {
        if (!prism.REMOVED_COMPONENTS.isEmpty()) {
            for (int i = 0; i < prism.REMOVED_COMPONENTS.size(); i++) {
                JClosableComponent component = prism.REMOVED_COMPONENTS.get(i);

                if (component.getType() == getType()) {
                    return true;
                }
            }
        }

        return false;
    }

    public void closeComponent() {
        Container parent = JClosableComponent.this.getParent();

        if (parent != null) {
            parent.remove(JClosableComponent.this);
            parent.revalidate();
            parent.repaint();

            prism.REMOVED_COMPONENTS.add(JClosableComponent.this);

            switch (JClosableComponent.this.getType()) {
                case LOWER_SIDEBAR:
                    prism.getConfig().set(ConfigKey.SECONDARY_SPLITPANE_DIVIDER_LOCATION,
                            prism.getSecondarySplitPane().getDividerLocation());
                    prism.getSecondarySplitPane().setDividerSize(0);
                    break;
                case SIDEBAR:
                    prism.getConfig().set(ConfigKey.PRIMARY_SPLITPANE_DIVIDER_LOCATION,
                            prism.getPrimarySplitPane().getDividerLocation());
                    prism.getPrimarySplitPane().setDividerSize(0);
                    break;
                default:
                    break;
            }
        }

        parent.revalidate();
        parent.repaint();

        prism.revalidate();
        prism.repaint();
    }
}
