package com.prism.components.frames;

import com.prism.Prism;
import com.prism.utils.ResourceUtil;
import com.prism.utils.Theme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class LoadingFrame extends JFrame {

	private static final int ARC = 28;
	private static final int SHADOW_SIZE = 16;

	private final float[] GRADIENT_FRACTIONS = {0f, 1f};
	private final Color[] GRADIENT_COLORS = {
			new Color(0xF6F8FA),
			new Color(0xECEFF4)
	};
	private Timer fadeTimer = null;
	private float opacity = 0f;

	public LoadingFrame() {
		setTitle("Prism");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setUndecorated(true);
		setIconImage(ResourceUtil.getAppIcon());

		if (Theme.isDarkTheme()) {
			setBackground(Theme.getPrimaryColor());
		} else {
			setBackground(new Color(0, 0, 0, 0));
		}

		setContentPane(createContentPanel());
		pack();

		applyRoundedShape();
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				applyRoundedShape();
			}
		});

		setLocationRelativeTo(null);

		fadeTimer = new Timer(16, e -> {
			opacity += 0.04f;
			if (opacity >= 1f) {
				opacity = 1f;
				fadeTimer.stop();
			}
			setOpacity(opacity);
		});
		fadeTimer.start();
	}

	private JPanel createContentPanel() {
		JPanel panel = new JPanel(new BorderLayout(15, 15)) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				int w = getWidth();
				int h = getHeight();

				if (Theme.isDarkTheme()) {
					g2.setPaint(new LinearGradientPaint(0, 0, 0, h,
							GRADIENT_FRACTIONS, new Color[]{ Theme.getPrimaryColor(), Theme.getSecondaryColor() }));
				} else {
					g2.setPaint(new LinearGradientPaint(0, 0, 0, h,
							GRADIENT_FRACTIONS, GRADIENT_COLORS));
				}

				g2.fill(new RoundRectangle2D.Float(0, 0, w, h, ARC, ARC));
				g2.dispose();
				super.paintComponent(g);
			}
		};
		panel.setOpaque(false);
		panel.setBorder(new EmptyBorder(20, 20, 20, 20));

		if (Theme.isDarkTheme()) {
			panel.setBackground(Theme.getPrimaryColor());
		}

		JLabel iconLabel = new JLabel(ResourceUtil.getIcon("icons/Prism.png", 64));
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

		JPanel logoPanel = new JPanel(new BorderLayout());
		logoPanel.setOpaque(false);
		logoPanel.add(iconLabel, BorderLayout.CENTER);
		logoPanel.setPreferredSize(new Dimension(80, 80));

		JLabel titleLabel = new JLabel(
				"<html><center><b>Prism</b><br><font size='-1'>" + Prism.getVersion() + "</font></center></html>");
		titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

		if (!Theme.isDarkTheme()) titleLabel.setForeground(new Color(0x2E3440));

		JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
		headerPanel.setOpaque(false);
		headerPanel.add(logoPanel, BorderLayout.NORTH);
		headerPanel.add(titleLabel, BorderLayout.CENTER);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setPreferredSize(new Dimension(250, 20));

		if (!Theme.isDarkTheme()) progressBar.setForeground(new Color(0x5E81AC));

		progressBar.setOpaque(false);

		JLabel copyrightLabel = new JLabel(
				"Copyright \u00a9 2025 Tayeb Yassine. All rights reserved.");
		copyrightLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
		copyrightLabel.setHorizontalAlignment(SwingConstants.CENTER);

		if (!Theme.isDarkTheme()) copyrightLabel.setForeground(new Color(0x4C566A));

		JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
		contentPanel.setOpaque(false);
		contentPanel.add(progressBar, BorderLayout.CENTER);
		contentPanel.add(copyrightLabel, BorderLayout.SOUTH);

		panel.add(headerPanel, BorderLayout.NORTH);
		panel.add(contentPanel, BorderLayout.CENTER);

		return panel;
	}

	private void applyRoundedShape() {
		int w = getWidth();
		int h = getHeight();
		setShape(new RoundRectangle2D.Float(0, 0, w, h, ARC, ARC));
	}
}