package com.prism.components.textarea;

import com.prism.Prism;
import com.prism.components.frames.CreateProjectFrame;
import com.prism.components.frames.WarningDialog;
import com.prism.managers.FileManager;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

public class Homepage extends JPanel {

	private static final Prism prism = Prism.getInstance();

	public Homepage() {
		super(new BorderLayout());
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		Box center = Box.createVerticalBox();

		JLabel logo = new JLabel(ResourceUtil.getIcon("icons/Prism.png", 64));
		logo.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel welcome = new JLabel(prism.getLanguage().get(233));
		welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 18f));
		welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
		welcome.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

		center.add(Box.createVerticalGlue());
		center.add(logo);
		center.add(welcome);
		center.add(Box.createVerticalGlue());

		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
		bottom.setOpaque(false);

		bottom.add(createLink(prism.getLanguage().get(230), CreateProjectFrame::new));
		bottom.add(createLink(prism.getLanguage().get(231), FileManager::openDirectory));
		bottom.add(createLink(prism.getLanguage().get(232), Homepage::browseHelp));

		add(center, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);
	}

	private static JButton createLink(String text, Runnable action) {
		JButton link = new JButton(text);
		link.setContentAreaFilled(false);
		link.setBorderPainted(false);
		link.setFocusPainted(false);
		link.setOpaque(false);
		link.setForeground(UIManager.getColor("Component.linkColor"));
		link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		link.setFont(link.getFont().deriveFont(Font.PLAIN));

		link.addMouseListener(new MouseAdapter() {
			@Override public void mouseEntered(MouseEvent e) {
                Map<TextAttribute, Object> attributes = (Map<TextAttribute, Object>)
                        Collections.synchronizedMap(link.getFont().getAttributes());

                attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);

                Font underlinedFont = new Font(attributes);

                link.setFont(underlinedFont);
			}
			@Override public void mouseExited(MouseEvent e) {
                Map<TextAttribute, Object> attributes = (Map<TextAttribute, Object>)
                        Collections.synchronizedMap(link.getFont().getAttributes());

                attributes.remove(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);

                Font underlinedFont = new Font(attributes);

                link.setFont(underlinedFont);
			}
		});

		link.addActionListener(e -> action.run());
		return link;
	}

	private static void browseHelp() {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI("https://tayebyassine.github.io/NeoPrism/"));
			} catch (IOException | URISyntaxException ex) {
				WarningDialog.showWarningDialog(prism, ex);
			}
		} else {
			JOptionPane.showMessageDialog(prism,
					"Failed to open Help URL.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}