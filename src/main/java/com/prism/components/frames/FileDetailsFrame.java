package com.prism.components.frames;

import com.prism.Prism;
import com.prism.components.extended.JDefaultKineticScrollPane;
import com.prism.utils.FileUtil;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class FileDetailsFrame extends JFrame {
	private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Prism prism = Prism.getInstance();

	public FileDetailsFrame(File file) {
		super(prism.getLanguage().get(214, file.getName()));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(700, 600);
		setResizable(false);
		setLocationRelativeTo(null);
		setIconImages(ResourceUtil.getAppIcon());

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;

		addFileProperties(panel, gbc, file);

		JDefaultKineticScrollPane scrollPane = new JDefaultKineticScrollPane(panel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, BorderLayout.CENTER);

		panel.setPreferredSize(null);

		setVisible(true);
	}

	private void addFileProperties(JPanel panel, GridBagConstraints gbc, File f) {
		addRow(panel, gbc, "", "");
		addSeparator(panel, gbc, "--- General ---");
		addRow(panel, gbc, "Name", f.getName());
		addRow(panel, gbc, "Absolute path", f.getAbsolutePath());
		addRow(panel, gbc, "Canonical path", getCanonicalPath(f));
		addRow(panel, gbc, "Parent", f.getParent());
		addRow(panel, gbc, "Exists", f.exists());
		addRow(panel, gbc, "Directory", f.isDirectory());
		addRow(panel, gbc, "Regular file", f.isFile());
		addRow(panel, gbc, "Hidden", f.isHidden());
		addRow(panel, gbc, "Absolute", f.isAbsolute());
		addRow(panel, gbc, "Last modified", DATE_FMT.format(new Date(f.lastModified())));
		addRow(panel, gbc, "Length", String.valueOf(f.length()) + "bytes");
		addRow(panel, gbc, "Free space", FileUtil.formatFileSize(f.getFreeSpace()));
		addRow(panel, gbc, "Total space", FileUtil.formatFileSize(f.getTotalSpace()));
		addRow(panel, gbc, "Usable space", FileUtil.formatFileSize(f.getUsableSpace()));

		try {
			Path p = f.toPath();
			addSeparator(panel, gbc, "--- NIO.2 Attributes ---");
			addRow(panel, gbc, "Readable", Files.isReadable(p));
			addRow(panel, gbc, "Writable", Files.isWritable(p));
			addRow(panel, gbc, "Executable", Files.isExecutable(p));
			addRow(panel, gbc, "Symbolic link", Files.isSymbolicLink(p));

			BasicFileAttributes bfa = Files.readAttributes(p, BasicFileAttributes.class);
			addRow(panel, gbc, "Creation time", bfa.creationTime().toString());
			addRow(panel, gbc, "Last access time", bfa.lastAccessTime().toString());
			addRow(panel, gbc, "Last modified time", bfa.lastModifiedTime().toString());
			addRow(panel, gbc, "File key", String.valueOf(bfa.fileKey()));

			try {
				PosixFileAttributes pfa = Files.readAttributes(p, PosixFileAttributes.class);
				addSeparator(panel, gbc, "--- POSIX ---");
				addRow(panel, gbc, "Owner", pfa.owner().getName());
				addRow(panel, gbc, "Group", pfa.group().getName());
				addRow(panel, gbc, "Permissions", perms(pfa.permissions()));
			} catch (UnsupportedOperationException ignored) {
				addSeparator(panel, gbc, "--- POSIX not supported ---");
			}

			try {
				DosFileAttributes dfa = Files.readAttributes(p, DosFileAttributes.class);
				addSeparator(panel, gbc, "--- DOS ---");
				addRow(panel, gbc, "Read-only", dfa.isReadOnly());
				addRow(panel, gbc, "Archive", dfa.isArchive());
				addRow(panel, gbc, "System", dfa.isSystem());
				addRow(panel, gbc, "Hidden", dfa.isHidden());
			} catch (UnsupportedOperationException ignored) {
				addSeparator(panel, gbc, "--- DOS not supported ---");
			}

		} catch (IOException e) {
			addRow(panel, gbc, "I/O Error", e.getMessage());
		}
	}

	private void addRow(JPanel panel, GridBagConstraints gbc, String label, Object value) {
		String val = value instanceof Boolean ? (((Boolean) value) ? "Yes" : "No") : value.toString();
		addRow(panel, gbc, label, val);
	}

	private void addRow(JPanel panel, GridBagConstraints gbc,
						String label, String value) {
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		JLabel key = new JLabel(label.endsWith(":") ? label : (label + ":"));
		key.setFont(key.getFont().deriveFont(Font.BOLD));
		panel.add(key, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JLabel val = new JLabel(value);
		val.setFont(val.getFont().deriveFont(Font.PLAIN));

		val.putClientProperty("html.disable", false);
		panel.add(val, gbc);

		gbc.gridy++;
	}

	private void addSeparator(JPanel panel, GridBagConstraints gbc, String title) {
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(15, 5, 5, 5);
		JLabel sep = new JLabel(title);
		sep.setFont(sep.getFont().deriveFont(Font.BOLD | Font.ITALIC));
		panel.add(sep, gbc);
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 5, 5, 5);
	}

	private String getCanonicalPath(File f) {
		try {
			return f.getCanonicalPath();
		} catch (IOException e) {
			return "<unable to resolve>";
		}
	}

	private String perms(Set<PosixFilePermission> set) {
		StringBuilder sb = new StringBuilder(9);
		sb.append(set.contains(PosixFilePermission.OWNER_READ) ? 'r' : '-');
		sb.append(set.contains(PosixFilePermission.OWNER_WRITE) ? 'w' : '-');
		sb.append(set.contains(PosixFilePermission.OWNER_EXECUTE) ? 'x' : '-');
		sb.append(set.contains(PosixFilePermission.GROUP_READ) ? 'r' : '-');
		sb.append(set.contains(PosixFilePermission.GROUP_WRITE) ? 'w' : '-');
		sb.append(set.contains(PosixFilePermission.GROUP_EXECUTE) ? 'x' : '-');
		sb.append(set.contains(PosixFilePermission.OTHERS_READ) ? 'r' : '-');
		sb.append(set.contains(PosixFilePermission.OTHERS_WRITE) ? 'w' : '-');
		sb.append(set.contains(PosixFilePermission.OTHERS_EXECUTE) ? 'x' : '-');
		return sb.toString();
	}
}