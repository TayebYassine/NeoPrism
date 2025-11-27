package com.prism.projects;

import com.prism.Prism;
import com.prism.projects.apps.*;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class ProjectsGridFrame extends JFrame {
	private static final Prism prism = Prism.getInstance();

	private static final int TILE_WIDTH = 128;
	private static final int TILE_HEIGHT = 128;
	public File directory = null;
	private static AppBase[] APPS = {
			null,
			new AppC(),
			new AppCpp(),
			new AppCppGUI(),
			new AppJava(),
			new AppJavaSwingGUI(),
	};
	private static String[] APP_NAMES = {
			"Empty",
			"C: Console",
			"C++: Console",
			"C++: wxWidgets GUI",
			"Java: Console",
			"Java: Swing GUI"
	};
	private static ImageIcon[] APP_ICONS = {
			ResourceUtil.getIconFromSVG("icons/ui/folder.svg", 32, 32),
			ResourceUtil.getIconFromSVG("icons/languages/c.svg", 32, 32),
			ResourceUtil.getIconFromSVG("icons/languages/cpp.svg", 32, 32),
			ResourceUtil.getIconFromSVG("icons/languages/cpp.svg", 32, 32),
			ResourceUtil.getIconFromSVG("icons/languages/java.svg", 32, 32),
			ResourceUtil.getIconFromSVG("icons/languages/java.svg", 32, 32)
	};

	public ProjectsGridFrame(File directory) {
		super(prism.getLanguage().get(242));

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(700, 500);
		setResizable(false);
		setLocationRelativeTo(null);

		setIconImages(ResourceUtil.getAppIcon());

		this.directory = directory;

		init();

		setVisible(true);
	}

	public void init() {
		JPanel main = new JPanel();
		main.setLayout(new BorderLayout());
		main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		TilePanel tilePanel = new TilePanel(TILE_WIDTH, TILE_HEIGHT, 10, 10);
		ButtonGroup group = new ButtonGroup();

		for (int i = 0; i < APPS.length; i++) {
			AppBase app = APPS[i];
			String name = APP_NAMES[i];
			ImageIcon imgIcon = APP_ICONS[i];

			ProjectTile tile = new ProjectTile(name, imgIcon);
			tile.putClientProperty("app", app);

			group.add(tile);
			tilePanel.addTile(tile);
		}

		JScrollPane scroll = new JScrollPane(tilePanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getViewport().setBackground(Color.WHITE);
		main.add(scroll, BorderLayout.CENTER);

		// Bottom panel
		JPanel bottomPanel = new JPanel(new BorderLayout());

		JLabel status = new JLabel(prism.getLanguage().get(243));
		bottomPanel.add(status, BorderLayout.WEST);

		// Buttons panel
		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.setEnabled(false);
		JButton noThanksButton = new JButton(prism.getLanguage().get(244));

		buttonPanel.add(okButton);
		buttonPanel.add(noThanksButton);

		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		main.add(bottomPanel, BorderLayout.SOUTH);

		// Final
		add(main, BorderLayout.CENTER);

		// Listeners
		tilePanel.addSelectionListener(e -> {
			AbstractButton b = (AbstractButton) e.getItemSelectable();
			if (b.isSelected()) {
				status.setText(prism.getLanguage().get(245, b.getText()));
				okButton.setEnabled(true);
			} else {
				status.setText(prism.getLanguage().get(243));
				okButton.setEnabled(false);
			}
		});

		okButton.addActionListener(e -> {
			for (Component comp : tilePanel.getComponents()) {
				if (comp instanceof ProjectTile && ((ProjectTile) comp).isSelected()) {
					AppBase selectedApp = (AppBase) ((ProjectTile) comp).getClientProperty("app");

					boolean created = directory.mkdirs();
					if (!created) {
						JOptionPane.showMessageDialog(this, prism.getLanguage().get(77), prism.getLanguage().get(10002), JOptionPane.ERROR_MESSAGE);
						return;
					}

					if (selectedApp != null) {
						selectedApp.create(directory);
					}

					dispose();

					break;
				}
			}
		});

		noThanksButton.addActionListener(e -> {
			try {
				directory.delete();
			} catch (Exception _) {

			}

			dispose();
		});
	}

	public static class ProjectTile extends JToggleButton {
		public ProjectTile(String text, ImageIcon icon) {
			super(text, icon);
			setVerticalTextPosition(SwingConstants.BOTTOM);
			setHorizontalTextPosition(SwingConstants.CENTER);
			setIconTextGap(6);
			setPreferredSize(new Dimension(TILE_WIDTH - 10, TILE_HEIGHT - 10));
			setFocusPainted(false);
			setContentAreaFilled(false);
			setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY), new EmptyBorder(6, 6, 6, 6)));

			addItemListener(e -> {
				if (isSelected()) {
					setBorder(new CompoundBorder(new LineBorder(new Color(60, 120, 200), 2), new EmptyBorder(5, 5, 5, 5)));
					setOpaque(true);
					setBackground(new Color(220, 235, 255));
				} else {
					setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY), new EmptyBorder(6, 6, 6, 6)));
					setOpaque(false);
				}
				repaint();
			});
		}
	}

	public static class TilePanel extends JPanel implements ComponentListener {
		private final int tileWidth;
		private final int tileHeight;
		private final int hgap;
		private final int vgap;

		public TilePanel(int tileWidth, int tileHeight, int hgap, int vgap) {
			this.tileWidth = tileWidth;
			this.tileHeight = tileHeight;
			this.hgap = hgap;
			this.vgap = vgap;
			setBackground(Color.WHITE);
			setLayout(new GridLayout(0, 1, hgap, vgap));
			addComponentListener(this);
		}

		public void addTile(Component c) {
			add(c);
		}

		public void addSelectionListener(ItemListener l) {
			for (Component comp : getComponents()) {
				if (comp instanceof AbstractButton) ((AbstractButton) comp).addItemListener(l);
			}

			addContainerListener(new ContainerAdapter() {
				@Override
				public void componentAdded(ContainerEvent e) {
					if (e.getChild() instanceof AbstractButton) ((AbstractButton) e.getChild()).addItemListener(l);
				}
			});
		}

		private void recomputeGrid() {
			int width = getWidth();
			if (width <= 0) return;
			int cols = Math.max(1, width / tileWidth);
			((GridLayout) getLayout()).setColumns(cols);
			revalidate();
		}

		@Override
		public void componentResized(ComponentEvent e) {
			recomputeGrid();
		}

		@Override
		public void componentMoved(ComponentEvent e) {
		}

		@Override
		public void componentShown(ComponentEvent e) {
			recomputeGrid();
		}

		@Override
		public void componentHidden(ComponentEvent e) {
		}

	}
}
