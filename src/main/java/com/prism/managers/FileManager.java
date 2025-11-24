package com.prism.managers;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.definition.PrismFile;
import com.prism.components.extended.JKineticScrollPane;
import com.prism.components.frames.ErrorDialog;
import com.prism.components.frames.WarningDialog;
import com.prism.components.textarea.Homepage;
import com.prism.components.textarea.ImageViewer;
import com.prism.components.textarea.TextArea;
import com.prism.services.Service;
import com.prism.utils.FileUtil;
import com.prism.utils.Languages;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static javax.swing.JOptionPane.*;

public class FileManager {
	private static final Prism prism = Prism.getInstance();

	public static HashMap<String, String> DIFF_TOOL_CACHE = new HashMap<>();
	public static int DEBOUNCE_MS = 300;
	public static List<PrismFile> files = new ArrayList<>();
	public static JFileChooser fileChooser = new JFileChooser();
	public static JFileChooser directoryChooser = new JFileChooser();
	public static javax.swing.Timer debounce;
	private static File rootDirectory = null;

	static {
		fileChooser.setCurrentDirectory(rootDirectory);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setMultiSelectionEnabled(false);

		directoryChooser.setCurrentDirectory(rootDirectory);
		directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		directoryChooser.setAcceptAllFileFilterUsed(false);
	}

	public static File getRootDirectory() {
		return rootDirectory;
	}

	public static void setRootDirectory(File directory) {
		if (directory == null || !directory.exists() || !directory.isDirectory()) {
			showMessageDialog(prism,
					"Unable to set the directory; Received a null object or the directory is a file.", "Error",
					ERROR_MESSAGE);

			return;
		}

		rootDirectory = directory;

		prism.getConfig().set(ConfigKey.ROOT_DIRECTORY_PATH, directory.getAbsolutePath());

		if (prism.getFileExplorer() != null) {
			prism.getFileExplorer().setModel(null);
		}

		fileChooser.setCurrentDirectory(directory);
		directoryChooser.setCurrentDirectory(directory);

		if (prism.getFileExplorer() != null) {
			prism.getFileExplorer().setRootDirectory(directory);
		}
	}

	public static void openHomepage() {
		Homepage panel = new Homepage();

		PrismFile pf = new PrismFile(null, panel);
		pf.setHomepage(true);

		files.add(pf);
		prism.getTextAreaTabbedPane().addHomepageTab(pf);

		prism.getTextAreaTabbedPane().redirectUserToTab(pf);
	}

	public static void openNewFile() {
		TextArea textArea = new TextArea();

		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
		textArea.addSyntaxHighlighting();

		PrismFile pf = new PrismFile(null, textArea);

		files.add(pf);
		prism.getTextAreaTabbedPane().addTextAreaTab(pf);

		addListenersToTextArea(textArea, pf);

		prism.getTextAreaTabbedPane().redirectUserToTab(pf);
	}

	public static void openFile() {
		fileChooser.setDialogTitle("Open File");

		int response = fileChooser.showOpenDialog(prism);

		if (response == JFileChooser.APPROVE_OPTION) {
			File selected = fileChooser.getSelectedFile();

			openFile(selected);
		}
	}

	public static void openDirectory() {
		directoryChooser.setDialogTitle("Open Directory");

		int response = directoryChooser.showOpenDialog(prism);

		if (response == JFileChooser.APPROVE_OPTION) {
			File selected = directoryChooser.getSelectedFile();

			openDirectory(selected);
		}
	}

	public static void openFile(File file) {
		if (file == null || !file.exists() || !file.isFile()) {
			return;
		}

		if (!Languages.isSupported(file)) {
			int response = showConfirmDialog(prism,
					"This file is not supported by Prism.\nWould you like to open it by its default application?",
					"Unsupported File", YES_NO_CANCEL_OPTION);

			if (response == YES_OPTION) {
				try {
					Desktop.getDesktop().open(file);
				} catch (Exception e) {
					new WarningDialog(prism, e);
				}

				return;
			} else if (response == CANCEL_OPTION) {
				return;
			}
		}

		if (prism.getConfig().getBoolean(ConfigKey.WARN_BEFORE_OPENING_LARGE_FILES, true)) {
			int size = getFileSizeInMBExact(file);
			int maxSize = prism.getConfig().getInt(ConfigKey.MAX_FILE_SIZE_FOR_WARNING, 10);

			if (size >= maxSize) {
				int confirm = showConfirmDialog(
						prism,
						"Are you sure you want to open \"" + file.getName() + "\"?\nThe file is too large to open.",
						"Large File",
						YES_NO_OPTION,
						WARNING_MESSAGE);

				if (confirm != YES_OPTION) {
					return;
				}
			}
		}

		for (PrismFile pf : files) {
			if (pf.getAbsolutePath() != null && pf.getAbsolutePath().equals(file.getAbsolutePath())) {
				prism.getTextAreaTabbedPane().redirectUserToTab(pf);
				return;
			}
		}

		boolean isImage = FileUtil.isViewableImage(file);

		if (!isImage) {
			TextArea textArea = new TextArea();

			textArea.setSyntaxEditingStyle(Languages.getHighlighter(file));
			textArea.addSyntaxHighlighting();

			if (prism.getConfig().getBoolean(ConfigKey.AUTOCOMPLETE_ENABLED, true)) {
				textArea.addAutocomplete(file);
			}

			PrismFile pf = new PrismFile(file, textArea);

			files.add(pf);
			prism.getTextAreaTabbedPane().addTextAreaTab(pf);

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));

				textArea.read(reader, null);

				reader.close();

				JKineticScrollPane scrollPane = (JKineticScrollPane) SwingUtilities.getAncestorOfClass(JKineticScrollPane.class,
						textArea);

				SwingUtilities.invokeLater(() -> {
					JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
					verticalScrollBar.setValue(verticalScrollBar.getMinimum());
				});
			} catch (Exception e) {
				ErrorDialog.showErrorDialog(prism, e);
			}

			addListenersToTextArea(textArea, pf);

			prism.getTextAreaTabbedPane().redirectUserToTab(pf);

			prism.getBookmarks().updateTreeData(TextAreaManager.getBookmarksOfAllFiles());

			Service service = Languages.getService(file);

			if (service != null) {
				service.installSyntaxChecker(pf, textArea);
			}

			if (files.size() == 2) {
				PrismFile firstPf = files.getFirst();

				if (firstPf.getAbsolutePath() == null && firstPf.isText() && firstPf.getTextArea().getText().trim().isEmpty()) {
					prism.getTextAreaTabbedPane().closeTabByIndex(0);
				}
			}

			DIFF_TOOL_CACHE.putIfAbsent(file.getAbsolutePath(), textArea.getText());
		} else {
			ImageViewer viewer = new ImageViewer(file.getPath());

			PrismFile pf = new PrismFile(file, viewer);

			files.add(pf);
			prism.getTextAreaTabbedPane().addImageViewerTab(pf);

			prism.getTextAreaTabbedPane().redirectUserToTab(pf);
		}
	}

	public static void openDirectory(File directory) {
		if (directory == null || !directory.exists() || directory.isFile()) {
			showMessageDialog(prism,
					"Unable to load the folder; Received a null object or the folder is a file.", "Error",
					ERROR_MESSAGE);

			return;
		}

		setRootDirectory(directory);
	}

	public static List<File> getRecentFiles() {
		String[] paths = prism.getConfig().getStringArray(ConfigKey.RECENT_OPENED_FILES);
		List<File> files = new ArrayList<>();

		for (String path : paths) {
			File file = new File(path);

			if (file.exists() && file.isFile()) {
				files.add(file);
			}
		}

		return files;
	}

	public static void openRecentFiles() {
		String[] paths = prism.getConfig().getStringArray(ConfigKey.RECENT_OPENED_FILES);

		for (String path : paths) {
			File file = new File(path);

			if (file.exists() && file.isFile()) {
				openFile(file);
			}
		}
	}

	public static void saveFile() {
		PrismFile pf = prism.getTextAreaTabbedPane().getCurrentFile();

		saveFile(pf);
	}

	public static void saveFile(PrismFile pf) {
		if (!pf.isText()) {
			return;
		}

		String string = pf.getTextArea().getText();
		File selectedFile = null;

		File file = pf.getFile();

		if (file == null) {
			fileChooser.setDialogTitle("Save File");
			int reponse = fileChooser.showSaveDialog(prism);

			if (reponse == JFileChooser.APPROVE_OPTION) {
				selectedFile = fileChooser.getSelectedFile();

				pf.setFile(selectedFile);
			} else {
				return;
			}
		} else {
			selectedFile = file;
		}

		if (selectedFile != null) {
			try {
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(selectedFile), StandardCharsets.UTF_8));
				writer.write(string);
				writer.close();

				pf.setSaved(true);

				prism.updateWindowTitle(pf);
				prism.updateComponents(pf);
			} catch (Exception e) {
				new WarningDialog(prism, e);
			}
		}
	}

	public static void saveAsFile() {
		PrismFile pf = prism.getTextAreaTabbedPane().getCurrentFile();

		saveAsFile(pf);
	}

	public static void saveAsFile(PrismFile pf) {
		String string = pf.getTextArea().getText();
		File selectedFile = null;

		fileChooser.setDialogTitle("Save As");
		int reponse = fileChooser.showSaveDialog(prism);

		if (reponse == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();

			pf.setFile(selectedFile);
		} else {
			return;
		}

		if (selectedFile != null) {
			try {
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(selectedFile), StandardCharsets.UTF_8));
				writer.write(string);
				writer.close();

				pf.setSaved(true);

				if (pf.getAbsolutePath() != null && !pf.getAbsolutePath().equals(selectedFile.getPath())) {
					pf.setFile(selectedFile);
				}

				prism.updateWindowTitle(pf);
				prism.updateComponents(pf);
			} catch (Exception e) {
				new WarningDialog(prism, e);
			}
		}
	}

	public static void saveAllFiles() {
		for (PrismFile pf : files) {
			saveFile(pf);
		}
	}

	public static List<PrismFile> getFiles() {
		return files;
	}

	public static int getFileSizeInMBExact(File file) {
		if (file == null || !file.exists() || file.isDirectory()) {
			return -1;
		}

		double fileSizeInBytes = file.length();
		double fileSizeInMB = fileSizeInBytes / (1024.0 * 1024.0);

		return (int) Math.round(fileSizeInMB);
	}

	public static String getOriginalText(File file) {
		return DIFF_TOOL_CACHE.getOrDefault(file.getAbsolutePath(), null);
	}

	private static void addListenersToTextArea(TextArea textArea, PrismFile prismFile) {
		textArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				prism.updateStatusBar();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				prism.updateStatusBar();
			}

			@Override
			public void keyTyped(KeyEvent e) {
				prism.updateStatusBar();
			}
		});

		textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				prism.updateStatusBar();
			}
		});

		textArea.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0) {
					if (e.getWheelRotation() < 0) {
						TextAreaManager.zoomIn();
					} else if (e.getWheelRotation() > 0) {
						TextAreaManager.zoomOut();
					}
				} else {
					e.getComponent().getParent().dispatchEvent(e);
				}
			}
		});

		debounce = new Timer(DEBOUNCE_MS, (e) -> {
			PrismFile pf = prism.getTextAreaTabbedPane().getCurrentFile();

			prism.updateComponents(pf);
		});

		debounce.setRepeats(false);

		textArea.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {

			}

			@Override
			public void removeUpdate(DocumentEvent e) {

			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				prismFile.setSaved(false);

				prism.updateWindowTitle(prismFile);
				prism.updateStatusBar();

				restart();
			}

			private void restart() {
				debounce.restart();
			}
		});
	}
}
