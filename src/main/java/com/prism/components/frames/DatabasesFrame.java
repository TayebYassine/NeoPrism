package com.prism.components.frames;

import com.prism.Prism;
import com.prism.components.definition.Connection;
import com.prism.components.definition.DatabaseProvider;
import com.prism.components.extended.JDefaultKineticScrollPane;
import com.prism.managers.DatabaseManager;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DatabasesFrame extends JFrame {
	private static Prism prism = Prism.getInstance();

	private final DatabaseProvider provider;
	private final List<Connection> connections = new ArrayList<>();

	private final JPanel listPanel = new JPanel();
	private final JButton closeBtn = new JButton(prism.getLanguage().get(15));
	private final JButton addBtn = new JButton(prism.getLanguage().get(16));

	public DatabasesFrame(DatabaseProvider provider) {
		super(prism.getLanguage().get(17));
		this.provider = provider;

		setSize(600, 400);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);

		setResizable(true);

		setIconImages(ResourceUtil.getAppIcon());

		init();
		reload();
		setVisible(true);
	}

	private void init() {
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		JDefaultKineticScrollPane scroll = new JDefaultKineticScrollPane(listPanel);
		scroll.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		btnBar.add(addBtn);
		btnBar.add(closeBtn);

		addBtn.addActionListener(e -> onAdd());
		closeBtn.addActionListener(e -> dispose());

		add(scroll, BorderLayout.CENTER);
		add(btnBar, BorderLayout.SOUTH);
	}

	private void reload() {
		connections.clear();
		connections.addAll(
				DatabaseManager.getAllDatabases()
						.stream()
						.filter(c -> c.getProvider() == provider.getId())
						.toList());

		rebuildRows();
	}

	private void rebuildRows() {
		listPanel.removeAll();

		for (int i = 0; i < connections.size(); i++) {
			listPanel.add(createRow(connections.get(i)));

			if (i < connections.size() - 1) {
				JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
				sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
				listPanel.add(sep);
			}
		}

		listPanel.revalidate();
		listPanel.repaint();
	}

	private JPanel createRow(Connection conn) {
		JPanel row = new JPanel(new BorderLayout());
		row.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

		JPanel left = new JPanel(new GridLayout(0, 1, 0, 2));
		left.setOpaque(false);
		left.add(new JLabel(htmlBold(prism.getLanguage().get(79, conn.getName()))));
		left.add(new JLabel(prism.getLanguage().get(80, conn.getHost() + ":" + conn.getPort())));
		left.add(new JLabel(prism.getLanguage().get(81, conn.getUser())));

		JButton connectBtn = new JButton(prism.getLanguage().get(82));
		JButton forgetBtn  = new JButton(prism.getLanguage().get(83));

		connectBtn.addActionListener(e -> onConnect(conn));
		forgetBtn.addActionListener(e -> onForget(conn));

		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		right.setOpaque(false);
		right.add(connectBtn);
		right.add(forgetBtn);

		row.add(left,  BorderLayout.CENTER);
		row.add(right, BorderLayout.EAST);

		return row;
	}

	private void onAdd(String... defaults) {
		String[] data = showLoginDialog(prism.getLanguage().get(84, provider), defaults);
		if (data == null) return;

		boolean ok = Prism.getInstance()
				.getDatabase()
				.verifyConnectionWithParameters(data[0], data[1], data[2], data[3]);
		if (!ok) {          // retry on failure
			onAdd(data[0], data[1], data[2]);
			return;
		}

		DatabaseManager.addDatabase(
				new Connection(data[0] + "@" + data[1], data[0], data[1], data[2], provider.getId()));
		reload();
	}

	private void onConnect(Connection c) {
		String[] data = showLoginDialog(prism.getLanguage().get(85, c.getName()),
				c.getHost(), c.getPort(), c.getUser(), null);   // password field empty
		if (data == null) return;

		boolean ok = Prism.getInstance()
				.getDatabase()
				.connectWithParameters(data[0], data[1], data[2], data[3]);
		if (ok) dispose();
	}

	private void onForget(Connection c) {
		int ans = JOptionPane.showConfirmDialog(
				this,
				prism.getLanguage().get(16, c.getName()),
				prism.getLanguage().get(19),
				JOptionPane.YES_NO_OPTION);
		if (ans == JOptionPane.YES_OPTION) {
			DatabaseManager.removeDatabaseById(c.getId());
			reload();
		}
	}

	private String[] showLoginDialog(String title, String... defaultData) {
		JTextField host = new JTextField(20);
		host.setText(defaultData.length > 0 && defaultData[0] != null ? defaultData[0] : "localhost");

		JTextField port = new JTextField(20);
		port.setText(defaultData.length > 1 && defaultData[1] != null ? defaultData[1] : "3306");

		JTextField user = new JTextField(20);
		user.setText(defaultData.length > 2 && defaultData[2] != null ? defaultData[2] : "");

		JPasswordField pass = new JPasswordField(20);

		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0; gbc.gridy = 0; p.add(new JLabel(prism.getLanguage().get(86)), gbc);
		gbc.gridx = 1; p.add(host, gbc);

		gbc.gridx = 0; gbc.gridy++; p.add(new JLabel(prism.getLanguage().get(87)), gbc);
		gbc.gridx = 1; p.add(port, gbc);

		gbc.gridx = 0; gbc.gridy++; p.add(new JLabel(prism.getLanguage().get(88)), gbc);
		gbc.gridx = 1; p.add(user, gbc);

		gbc.gridx = 0; gbc.gridy++; p.add(new JLabel(prism.getLanguage().get(89)), gbc);
		gbc.gridx = 1; p.add(pass, gbc);

		pass.requestFocus();

		if (JOptionPane.showConfirmDialog(this, p, title,
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)
				!= JOptionPane.OK_OPTION) return null;

		return new String[]{
				host.getText().trim(),
				port.getText().trim(),
				user.getText().trim(),
				new String(pass.getPassword())
		};
	}

	private static String htmlBold(String s) {
		return "<html><b>" + s.replace("&", "&amp;").replace("<", "&lt;") + "</b></html>";
	}
}