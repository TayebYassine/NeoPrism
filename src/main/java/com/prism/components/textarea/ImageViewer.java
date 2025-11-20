package com.prism.components.textarea;

import com.prism.Prism;
import com.prism.utils.ResourceUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

public class ImageViewer extends JPanel {
	private static Prism prism = Prism.getInstance();

	private final ImagePanel imagePanel;
	private final JLabel zoomLabel;

	private final double[] ZOOM_STEPS = {.25, .33, .5, .67, .75, .9, 1, 1.1, 1.25, 1.5, 2, 2.5, 3, 4, 5};
	private int zoomIndex = 6;                       // 1.0 lives here
	private boolean zoomToFit = false;               // new feature

	public ImageViewer(String imagePath) {
		super(new BorderLayout());

		this.imagePanel = new ImagePanel(imagePath);
		this.zoomLabel = new JLabel(prism.getLanguage().get(4, "100%"), SwingConstants.CENTER);

		JScrollPane scroll = new JScrollPane(imagePanel);
		scroll.setBorder(BorderFactory.createEmptyBorder());

		installKeyboardActions(scroll);
		addInteractiveListeners(scroll);

		add(createZoomToolBar(), BorderLayout.NORTH);
		add(scroll, BorderLayout.CENTER);
	}

	private JToolBar createZoomToolBar() {
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		/* buttons */
		JButton zoomIn = btn("icons/ui/zoom-in.svg", () -> zoom(1));
		JButton zoomOut = btn("icons/ui/zoom-out.svg", () -> zoom(-1));
		JButton reset = btn("1:1", () -> setScale(1.0));
		JButton fitBtn = btn(prism.getLanguage().get(47), () -> setZoomToFit(!zoomToFit));

		/* slider for smooth zoom */
		JSlider slider = new JSlider(25, 500, 100);
		slider.setPreferredSize(new Dimension(120, slider.getPreferredSize().height));
		slider.addChangeListener(e -> {
			if (!slider.getValueIsAdjusting()) setScale(slider.getValue() / 100.0);
		});

		JComboBox<Double> presets = new JComboBox<>(new Double[]{.25, .5, .75, 1., 1.25, 1.5, 2., 3., 4.});
		presets.setEditable(true);
		presets.setSelectedItem(1.0);
		presets.addActionListener(e -> setScale((Double) presets.getSelectedItem()));

		bar.add(zoomIn);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(zoomOut);
		bar.add(Box.createHorizontalStrut(10));
		bar.add(reset);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(fitBtn);
		bar.addSeparator();
		bar.add(slider);
		bar.addSeparator();
		bar.add(presets);
		bar.add(Box.createHorizontalGlue());
		bar.add(zoomLabel);

		return bar;
	}

	private void zoom(int dir) {
		zoomIndex = Math.max(0, Math.min(ZOOM_STEPS.length - 1, zoomIndex + dir));
		setScale(ZOOM_STEPS[zoomIndex]);
	}

	private void setScale(double s) {
		if (zoomToFit) setZoomToFit(false);               // user grabbed manual control
		imagePanel.setScale(s);
		zoomIndex = findClosestIndex(s);
		updateZoomLabel();
	}

	private void setZoomToFit(boolean on) {
		zoomToFit = on;
		if (on) {
			Dimension vp = ((JViewport) imagePanel.getParent()).getExtentSize();
			double sx = vp.width / (double) imagePanel.getWidth();
			double sy = vp.height / (double) imagePanel.getHeight();
			imagePanel.setScale(Math.min(sx, sy));
		} else {
			imagePanel.setScale(1.0);
		}
		updateZoomLabel();
	}

	private int findClosestIndex(double s) {
		int idx = 0;
		double best = Double.MAX_VALUE;
		for (int i = 0; i < ZOOM_STEPS.length; i++) {
			double d = Math.abs(ZOOM_STEPS[i] - s);
			if (d < best) {
				best = d;
				idx = i;
			}
		}
		return idx;
	}

	private void updateZoomLabel() {
		zoomLabel.setText(prism.getLanguage().get(4, String.format("%.0f%%", imagePanel.getScale() * 100)));
	}

	private void addInteractiveListeners(JScrollPane scroll) {
		imagePanel.addMouseWheelListener(e -> {
			if (e.isControlDown()) {
				setScale(imagePanel.getScale() * (e.getWheelRotation() < 0 ? 1.1 : 1 / 1.1));
				e.consume();
			}
		});

		MouseAdapter panner = new MouseAdapter() {
			private Point origin;

			@Override
			public void mousePressed(MouseEvent e) {
				origin = e.getPoint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (origin == null) return;
				JViewport vp = (JViewport) imagePanel.getParent();
				int dx = origin.x - e.getX();
				int dy = origin.y - e.getY();
				Point vpPos = vp.getViewPosition();
				vp.setViewPosition(new Point(vpPos.x + dx, vpPos.y + dy));
				origin = e.getPoint();
			}
		};
		imagePanel.addMouseListener(panner);
		imagePanel.addMouseMotionListener(panner);
	}

	private void installKeyboardActions(JComponent target) {
		InputMap im = target.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = target.getActionMap();

		im.put(KeyStroke.getKeyStroke("PLUS"), "zoomIn");
		im.put(KeyStroke.getKeyStroke("MINUS"), "zoomOut");
		im.put(KeyStroke.getKeyStroke("0"), "zoom100");
		im.put(KeyStroke.getKeyStroke("F"), "fit");

		am.put("zoomIn", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				zoom(+1);
			}
		});
		am.put("zoomOut", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				zoom(-1);
			}
		});
		am.put("zoom100", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				setScale(1.0);
			}
		});
		am.put("fit", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				setZoomToFit(!zoomToFit);
			}
		});
	}

	private JButton btn(String iconOrText, Runnable r) {
		JButton b = iconOrText.endsWith(".svg")
				? new JButton(ResourceUtil.getIconFromSVG(iconOrText, 16, 16))
				: new JButton(iconOrText);
		b.setFocusable(true);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		b.addActionListener(e -> r.run());
		return b;
	}

	public class ImagePanel extends JPanel {

		private final String imagePath;
		private BufferedImage image;
		private double scale = 1.0;

		public ImagePanel(String path) {
			this.imagePath = path;
			try {
				this.image = ImageIO.read(new File(path));

				if (this.image == null) {
					createPlaceholderImage("Error reading image format.");
				}
			} catch (IOException e) {
				e.printStackTrace();
				createPlaceholderImage("Failed to load image: " + new File(path).getName());
			}

			setLayout(new BorderLayout());
		}

		private void createPlaceholderImage(String message) {
			int width = 400;
			int height = 300;
			this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = this.image.createGraphics();
			g2d.fillRect(0, 0, width, height);

			g2d.setFont(new Font("Inter", Font.BOLD, 16));

			FontMetrics fm = g2d.getFontMetrics();
			int x = (width - fm.stringWidth(message)) / 2;
			int y = (fm.getAscent() + (height - fm.getHeight()) / 2);
			g2d.drawString(message, x, y);

			g2d.dispose();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (image != null) {
				Graphics2D g2d = (Graphics2D) g.create();

				int scaledWidth = (int) (image.getWidth() * scale);
				int scaledHeight = (int) (image.getHeight() * scale);

				int x = (getWidth() - scaledWidth) / 2;
				int y = (getHeight() - scaledHeight) / 2;

				g2d.drawImage(image, x, y, scaledWidth, scaledHeight, this);

				g2d.dispose();
			}
		}

		@Override
		public Dimension getPreferredSize() {
			if (image == null) {
				return new Dimension(500, 400);
			}

			return new Dimension(
					(int) (image.getWidth() * scale),
					(int) (image.getHeight() * scale));
		}

		public double getScale() {
			return scale;
		}

		public void setScale(double newScale) {
			if (newScale > 0.1 && newScale < 10.0) {
				this.scale = newScale;
				revalidate();
				repaint();
			}
		}
	}
}