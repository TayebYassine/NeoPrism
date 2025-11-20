package com.prism.components.sidebar.panels;

import com.prism.Prism;
import com.prism.components.frames.FileDetailsFrame;
import com.prism.components.frames.WarningDialog;
import com.prism.managers.FileManager;
import com.prism.utils.Keyboard;
import com.prism.utils.Languages;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static javax.swing.JOptionPane.*;

public class FileExplorer extends JTree {

	private static final int LOADING_TIMEOUT_SECONDS = 30;
	private static final int MAX_CONCURRENT_LOADERS = 3;

	private final Prism prism = Prism.getInstance();
	private final FileSystemView fsv = FileSystemView.getFileSystemView();
	private final ExecutorService loaderExecutor = Executors.newFixedThreadPool(MAX_CONCURRENT_LOADERS);
	private final Map<File, CompletableFuture<List<File>>> loadingCache = new ConcurrentHashMap<>();
	private final Set<File> currentlyLoading = ConcurrentHashMap.newKeySet();

	private final Map<String, Icon> fileIconCache = new ConcurrentHashMap<>();
	private final Icon[] folderIcons = new Icon[2];
	private Icon rootIcon;

	private File rootDirectory;
	private volatile boolean isRefreshing = false;

	public FileExplorer(File rootDirectory) {
		super();
		this.rootDirectory = rootDirectory;
		initializeTree();
	}

	private void initializeTree() {
		setModel(createTreeModel(rootDirectory));
		setRootVisible(true);
		setShowsRootHandles(true);
		setFocusable(true);
		setCellRenderer(new FileTreeCellRenderer());

		// Add listeners
		addTreeExpansionListener(new FileTreeExpansionListener());
		addTreeSelectionListener(new FileTreeSelectionDirectoryListener());
		addMouseListener(new RightClickMouseListener());
		addMouseListener(new LeftClickMouseListener());

		// Enable drag and drop
		setDragEnabled(true);
		setDropMode(DropMode.ON);
		setTransferHandler(new FileTransferHandler());
	}

	private TreeModel createTreeModel(File rootFile) {
		DefaultMutableTreeNode root = createNode(rootFile);
		return new DefaultTreeModel(root);
	}

	private DefaultMutableTreeNode createNode(File file) {
		return new DefaultMutableTreeNode(file);
	}

	private CompletableFuture<List<File>> loadChildrenAsync(File directory) {
		if (!directory.isDirectory()) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}

		return loadingCache.computeIfAbsent(directory, dir -> CompletableFuture.supplyAsync(() -> {
			try {
				currentlyLoading.add(dir);
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				File[] files = dir.listFiles();
				if (files == null) {
					return Collections.<File>emptyList();
				}

				return Arrays.stream(files).sorted((f1, f2) -> {
					if (f1.isDirectory() && !f2.isDirectory()) return -1;
					if (!f1.isDirectory() && f2.isDirectory()) return 1;
					return f1.getName().compareToIgnoreCase(f2.getName());
				}).collect(Collectors.toList());
			} finally {
				currentlyLoading.remove(dir);
				if (currentlyLoading.isEmpty()) {
					SwingUtilities.invokeLater(() -> setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)));
				}
			}
		}, loaderExecutor).orTimeout(LOADING_TIMEOUT_SECONDS, TimeUnit.SECONDS).exceptionally(ex -> {
			System.err.println("Error loading directory " + dir + ": " + ex.getMessage());
			return Collections.emptyList();
		}));
	}

	private void updateNodeChildren(DefaultMutableTreeNode parentNode, List<File> newFiles) {
		if (SwingUtilities.isEventDispatchThread()) {
			performUpdate(parentNode, newFiles);
		} else {
			SwingUtilities.invokeLater(() -> performUpdate(parentNode, newFiles));
		}
	}

	private void performUpdate(DefaultMutableTreeNode parentNode, List<File> newFiles) {
		DefaultTreeModel model = (DefaultTreeModel) getModel();

		// Create maps for efficient lookup
		Map<File, DefaultMutableTreeNode> existingNodes = new HashMap<>();
		for (int i = 0; i < parentNode.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) parentNode.getChildAt(i);
			existingNodes.put((File) child.getUserObject(), child);
		}

		Set<File> seenFiles = new HashSet<>();
		int insertIndex = 0;

		// Update or add nodes
		for (File file : newFiles) {
			DefaultMutableTreeNode node = existingNodes.get(file);
			if (node == null) {
				node = createNode(file);
				model.insertNodeInto(node, parentNode, insertIndex);
			} else {
				int currentIndex = parentNode.getIndex(node);
				if (currentIndex != insertIndex) {
					model.removeNodeFromParent(node);
					model.insertNodeInto(node, parentNode, insertIndex);
				}
			}
			seenFiles.add(file);
			insertIndex++;
		}

		// Remove nodes that no longer exist
		List<DefaultMutableTreeNode> toRemove = new ArrayList<>();
		for (int i = 0; i < parentNode.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) parentNode.getChildAt(i);
			if (!seenFiles.contains(child.getUserObject())) {
				toRemove.add(child);
			}
		}

		for (DefaultMutableTreeNode node : toRemove) {
			model.removeNodeFromParent(node);
		}
	}

	public void refresh() {
		if (isRefreshing) return;
		isRefreshing = true;

		SwingUtilities.invokeLater(() -> {
			try {
				loadingCache.clear();
				TreePath[] expandedPaths = getExpandedPaths();
				TreePath selectedPath = getSelectionPath();

				setModel(createTreeModel(rootDirectory));

				for (TreePath path : expandedPaths) {
					expandPathIfExists(path);
				}

				if (selectedPath != null) {
					expandPathIfExists(selectedPath);
					setSelectionPath(selectedPath);
				}
			} finally {
				isRefreshing = false;
			}
		});
	}

	private TreePath[] getExpandedPaths() {
		List<TreePath> expanded = new ArrayList<>();
		for (int i = 0; i < getRowCount(); i++) {
			TreePath path = getPathForRow(i);
			if (isExpanded(path)) {
				expanded.add(path);
			}
		}
		return expanded.toArray(new TreePath[0]);
	}

	private void expandPathIfExists(TreePath targetPath) {
		if (targetPath == null || isRefreshing) return;

		SwingUtilities.invokeLater(() -> {
			List<Object> newPathComponents = new ArrayList<>();
			newPathComponents.add(getModel().getRoot());

			TreePath currentPath = new TreePath(newPathComponents.toArray());
			expandPath(currentPath);

			for (int i = 1; i < targetPath.getPathCount(); i++) {
				DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) targetPath.getPathComponent(i);
				File targetFile = (File) targetNode.getUserObject();

				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) currentPath.getLastPathComponent();

				// Ensure children are loaded
				if (currentNode.getChildCount() == 0) {

					File currentFile = (File) currentNode.getUserObject();
					if (currentFile.isDirectory()) {
						try {
							List<File> children = loadChildrenAsync(currentFile).get(5, TimeUnit.SECONDS);
							updateNodeChildren(currentNode, children);
						} catch (Exception ex) {
							System.err.println("Error loading children for " + currentFile + ": " + ex.getMessage());
							return;
						}
					}
				}

				// Find matching child
				boolean found = false;
				for (int j = 0; j < currentNode.getChildCount(); j++) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) currentNode.getChildAt(j);
					File childFile = (File) child.getUserObject();

					if (childFile.equals(targetFile)) {
						newPathComponents.add(child);
						currentPath = new TreePath(newPathComponents.toArray());
						expandPath(currentPath);
						found = true;
						break;
					}
				}

				if (!found) return;
			}
		});
	}

	public void setRootDirectory(File newRootDirectory) {
		this.rootDirectory = newRootDirectory;
		loadingCache.clear();
		refresh();
	}

	public void shutdown() {
		loaderExecutor.shutdown();
		try {
			if (!loaderExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
				loaderExecutor.shutdownNow();
			}
		} catch (InterruptedException e) {
			loaderExecutor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	private JPopupMenu createFolderContextMenu(File file) {
		JPopupMenu folderContextMenu = new JPopupMenu();

		JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener((_) -> {
			FileManager.openFile(file);
		});

		JMenuItem moveItem = new JMenuItem("Move");
		moveItem.addActionListener((_) -> {
			moveFile(file);
		});

		folderContextMenu.add(moveItem);
		folderContextMenu.addSeparator();

		JMenuItem copyPathItem = new JMenuItem("Copy Path");
		copyPathItem.addActionListener((_) -> {
			Keyboard.copyToClipboard(file.getAbsolutePath());
		});

		folderContextMenu.add(copyPathItem);
		folderContextMenu.addSeparator();

		JMenuItem createFileItem = new JMenuItem("New File");
		createFileItem.addActionListener((_) -> {
			newFile(false);
		});

		JMenuItem createFolderItem = new JMenuItem("New Folder");
		createFolderItem.addActionListener((_) -> {
			newFile(true);
		});

		JMenuItem renameItem = new JMenuItem("Rename...");
		renameItem.addActionListener((_) -> {
			renameFolder(file);
		});

		JMenuItem deleteItem = new JMenuItem("Delete");
		deleteItem.addActionListener((_) -> {
			deleteFile(file);
		});

		if (FileManager.getRootDirectory() != null && file.getAbsolutePath().equalsIgnoreCase(FileManager.getRootDirectory().getAbsolutePath())) {
			moveItem.setEnabled(false);
			renameItem.setEnabled(false);
			deleteItem.setEnabled(false);
		}

		folderContextMenu.add(createFileItem);
		folderContextMenu.add(createFolderItem);
		folderContextMenu.addSeparator();
		folderContextMenu.add(renameItem);
		folderContextMenu.add(deleteItem);

		return folderContextMenu;
	}

	private JPopupMenu createFileContextMenu(File file) {
		JPopupMenu fileContextMenu = new JPopupMenu();

		JMenuItem openItem = new JMenuItem(prism.getLanguage().get(116));
		openItem.addActionListener((_) -> {
			FileManager.openFile(file);
		});

		JMenuItem moveItem = new JMenuItem(prism.getLanguage().get(117));
		moveItem.addActionListener((_) -> {
			moveFile(file);
		});

		fileContextMenu.add(openItem);
		fileContextMenu.add(moveItem);

		fileContextMenu.addSeparator();

		JMenuItem copyPathItem = new JMenuItem(prism.getLanguage().get(48));
		copyPathItem.addActionListener((_) -> {
			Keyboard.copyToClipboard(file.getAbsolutePath());
		});

		fileContextMenu.add(copyPathItem);

		fileContextMenu.addSeparator();

		JMenuItem renameItem = new JMenuItem(prism.getLanguage().get(118));
		renameItem.addActionListener((_) -> {
			renameFile(file);
		});

		JMenuItem deleteItem = new JMenuItem(prism.getLanguage().get(57));
		deleteItem.addActionListener((_) -> {
			deleteFile(file);
		});

		fileContextMenu.add(renameItem);
		fileContextMenu.add(deleteItem);

		fileContextMenu.addSeparator();

		JMenuItem propertiesItem = new JMenuItem(prism.getLanguage().get(120));
		propertiesItem.addActionListener((_) -> {
			new FileDetailsFrame(file);
		});

		fileContextMenu.add(propertiesItem);

		return fileContextMenu;
	}

	public void newFile(boolean isFolder) {
		File selected = getSelectedFile();
		File parentDir = (selected != null && selected.isDirectory()) ? selected : rootDirectory;

		String type = isFolder ? prism.getLanguage().get(121) : prism.getLanguage().get(122);
		String name = JOptionPane.showInputDialog(prism, prism.getLanguage().get(123, type.toLowerCase()), prism.getLanguage().get(124, type), JOptionPane.QUESTION_MESSAGE);

		if (name == null || name.trim().isEmpty()) {
			return;
		}

		File newFile = new File(parentDir, name.trim());

		if (newFile.exists()) {
			showMessageDialog(prism, prism.getLanguage().get(125), prism.getLanguage().get(10002), ERROR_MESSAGE);
			return;
		}

		try {
			boolean created = isFolder ? newFile.mkdir() : newFile.createNewFile();
			if (created) {
				refreshNode(parentDir);
			} else {
				showMessageDialog(prism, prism.getLanguage().get(126, type.toLowerCase()), prism.getLanguage().get(10002), ERROR_MESSAGE);
			}
		} catch (Exception ex) {
			new WarningDialog(prism, ex);
		}
	}

	public void renameFile(File target) {
		if (target == null || !target.exists() || target.isDirectory()) {
			return;
		}
		showRenameDialog(target);
	}

	public void renameFolder(File target) {
		if (target == null || !target.exists() || !target.isDirectory()) {
			return;
		}
		showRenameDialog(target);
	}

	public void deleteFile(File target) {
		int confirm = JOptionPane.showConfirmDialog(prism, prism.getLanguage().get(127, target.getName()), prism.getLanguage().get(128), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		boolean success = deleteRecursively(target);
		if (!success) {
			showMessageDialog(prism, prism.getLanguage().get(29), prism.getLanguage().get(10002), ERROR_MESSAGE);
		} else {
			File parentDir = target.getParentFile();
			refreshNode(parentDir);
		}
	}

	public void moveFile(File target) {
		if (target == null || !target.exists()) {
			return;
		}

		java.util.List<File> directories = new java.util.ArrayList<>();
		collectDirectories(rootDirectory, directories);

		if (directories.isEmpty()) {
			return;
		}

		JComboBox<String> comboBox = new JComboBox<>();
		comboBox.setFocusable(true);
		for (File dir : directories) {
			comboBox.addItem(dir.getAbsolutePath());
		}

		File parentDir = target.getParentFile();
		comboBox.setSelectedItem(parentDir.getAbsolutePath());

		int result = showConfirmDialog(prism, comboBox, prism.getLanguage().get(130, target.getName()), OK_CANCEL_OPTION, QUESTION_MESSAGE);

		if (result != OK_OPTION) {
			return;
		}

		File selectedDir = new File((String) Objects.requireNonNull(comboBox.getSelectedItem()));
		if (!selectedDir.exists() || !selectedDir.isDirectory()) {
			showMessageDialog(prism, prism.getLanguage().get(131), prism.getLanguage().get(10001), ERROR_MESSAGE);
			return;
		}

		if (selectedDir.equals(parentDir)) {
			return;
		}

		File newFile = new File(selectedDir, target.getName());
		if (newFile.exists()) {
			showMessageDialog(prism, prism.getLanguage().get(125), prism.getLanguage().get(10002), ERROR_MESSAGE);
			return;
		}

		boolean success = target.renameTo(newFile);
		if (!success) {
			showMessageDialog(prism, prism.getLanguage().get(132), prism.getLanguage().get(10002), ERROR_MESSAGE);
		} else {
			refreshNode(parentDir);
			refreshNode(selectedDir);
		}
	}

	private void collectDirectories(File parent, java.util.List<File> directories) {
		if (parent == null || !parent.isDirectory()) {
			return;
		}

		directories.add(parent);

		File[] children = parent.listFiles(File::isDirectory);
		if (children != null) {
			for (File child : children) {
				collectDirectories(child, directories);
			}
		}
	}

	private boolean deleteRecursively(File file) {
		if (file.isDirectory()) {
			File[] contents = file.listFiles();
			if (contents != null) {
				for (File child : contents) {
					if (!deleteRecursively(child)) {
						return false;
					}
				}
			}
		}

		return file.delete();
	}

	private void showRenameDialog(File target) {
		String newName = JOptionPane.showInputDialog(prism, prism.getLanguage().get(133), target.getName());

		if (newName == null || newName.trim().isEmpty()) {
			return;
		}

		File parent = target.getParentFile();
		File newFile = new File(parent, newName.trim());

		if (newFile.exists()) {
			showMessageDialog(prism, prism.getLanguage().get(125), prism.getLanguage().get(10002), ERROR_MESSAGE);
			return;
		}

		boolean success = target.renameTo(newFile);
		if (success) {
			refreshNode(parent);
		} else {
			showMessageDialog(prism, prism.getLanguage().get(134), prism.getLanguage().get(10002), ERROR_MESSAGE);
		}
	}

	private void refreshNode(File directory) {
		DefaultTreeModel model = (DefaultTreeModel) getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

		DefaultMutableTreeNode node = findNode(root, directory);
		if (node != null) {
			loadingCache.remove(directory);

			loadChildrenAsync(directory).thenAccept(files -> updateNodeChildren(node, files));
		} else {
			refresh();
		}
	}

	private DefaultMutableTreeNode findNode(DefaultMutableTreeNode parent, File target) {
		File nodeFile = (File) parent.getUserObject();
		if (nodeFile.equals(target)) {
			return parent;
		}

		for (int i = 0; i < parent.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
			DefaultMutableTreeNode result = findNode(child, target);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	// Get the currently selected file
	public File getSelectedFile() {
		TreePath selectionPath = getSelectionPath();
		if (selectionPath != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
			if (node.getUserObject() instanceof File) {
				return (File) node.getUserObject();
			}
		}
		return null;
	}

	public Icon getSystemFileIcon(File file) {
		return fsv.getSystemIcon(file);
	}

	public Icon getFileIcon(File file) {
		return fileIconCache.computeIfAbsent(file.getName().toLowerCase(Locale.ROOT), n -> Languages.getIcon(file));
	}

	public Icon getFolderIcon(boolean expanded) {
		int idx = expanded ? 1 : 0;
		if (folderIcons[idx] == null) {
			folderIcons[idx] = ResourceUtil.getIconFromSVG(expanded ? "icons/ui/folder-open.svg" : "icons/ui/folder.svg", 16, 16);
		}
		return folderIcons[idx];
	}

	public Icon getRootFolderIcon(boolean expanded) {
		if (rootIcon == null) {
			rootIcon = ResourceUtil.getIconFromSVG("icons/ui/folder-root.svg", 16, 16);
		}

		return rootIcon;
	}

	private class FileTreeCellRenderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

			if (value instanceof DefaultMutableTreeNode node) {
				Object userObject = node.getUserObject();

				if (userObject instanceof File file) {

					setText(file.getName());

					if (file.isDirectory() && file.getAbsolutePath().equals(rootDirectory.getAbsolutePath())) {
						setIcon(getRootFolderIcon(expanded));
					} else if (file.isDirectory()) {
						setIcon(getFolderIcon(expanded));
					} else {
						setIcon(getFileIcon(file));
					}
				}
			}

			return this;
		}
	}

	private class FileTreeExpansionListener implements TreeExpansionListener {
		@Override
		public void treeExpanded(TreeExpansionEvent event) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
			File dir = (File) node.getUserObject();
			if (dir.isDirectory() && !currentlyLoading.contains(dir)) {
				loadChildrenAsync(dir).thenAccept(files -> updateNodeChildren(node, files));
			}
		}

		@Override
		public void treeCollapsed(TreeExpansionEvent event) {

		}
	}

	private class FileTreeSelectionDirectoryListener implements TreeSelectionListener {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath path = e.getPath();
			if (path == null) return;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			File dir = (File) node.getUserObject();
			if (dir.isDirectory() && !currentlyLoading.contains(dir)) {
				loadChildrenAsync(dir).thenAccept(files -> updateNodeChildren(node, files));
			}
		}
	}

	private class RightClickMouseListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent event) {
			if (SwingUtilities.isRightMouseButton(event)) {
				TreePath path = getPathForLocation(event.getX(), event.getY());

				if (path != null) {
					setSelectionPath(path);

					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
					File file = (File) selectedNode.getUserObject();

					if (file.isDirectory()) {
						createFolderContextMenu(file).show(prism.getFileExplorer(), event.getX(), event.getY());
					} else if (file.isFile()) {
						createFileContextMenu(file).show(prism.getFileExplorer(), event.getX(), event.getY());
					}
				}
			}
		}
	}

	private class LeftClickMouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent event) {
			if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2) {
				TreePath path = getPathForLocation(event.getX(), event.getY());

				if (path != null) {
					setSelectionPath(path);

					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
					File file = (File) selectedNode.getUserObject();

					if (file.isFile()) {
						FileManager.openFile(file);
					}
				}
			}
		}
	}

	private class FileTransferHandler extends TransferHandler {

		// Define the actions supported (COPY and MOVE)
		@Override
		public int getSourceActions(JComponent c) {
			return MOVE;
		}

		// Prepare the data to be dragged (export)
		@Override
		protected Transferable createTransferable(JComponent c) {
			JTree tree = (JTree) c;
			// Get all selected file nodes
			TreePath[] paths = tree.getSelectionPaths();

			if (paths == null || paths.length == 0) {
				return null;
			}

			List<File> filesToTransfer = new ArrayList<>();
			for (TreePath path : paths) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				Object userObject = node.getUserObject();
				if (userObject instanceof File) {
					filesToTransfer.add((File) userObject);
				}
			}

			if (filesToTransfer.isEmpty()) {
				return null;
			}

			// Use the standard DataFlavor for a list of files
			return new Transferable() {
				@Override
				public DataFlavor[] getTransferDataFlavors() {
					return new DataFlavor[]{DataFlavor.javaFileListFlavor};
				}

				@Override
				public boolean isDataFlavorSupported(DataFlavor flavor) {
					return flavor.equals(DataFlavor.javaFileListFlavor);
				}

				@Override
				public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
					if (!isDataFlavorSupported(flavor)) {
						throw new UnsupportedFlavorException(flavor);
					}
					return filesToTransfer;
				}
			};
		}

		// Check if data can be dropped (import)
		@Override
		public boolean canImport(TransferSupport support) {
			if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				return false;
			}

			JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
			TreePath path = dropLocation.getPath();

			if (path == null) {
				// Cannot drop outside the tree structure
				return false;
			}

			DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path.getLastPathComponent();
			Object userObject = targetNode.getUserObject();

			if (userObject instanceof File targetFile) {

				// Allow dropping on a directory, or on a file (to get its parent directory)
				if (targetFile.isDirectory() || targetFile.isFile()) {
					// Prevent dropping a folder into itself or its descendant
					try {
						@SuppressWarnings("unchecked") List<File> draggedFiles = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

						for (File draggedFile : draggedFiles) {
							File dropDir = getDropDirectory(targetFile);

							// Check if the dragged file is an ancestor of the target directory
							if (draggedFile.isDirectory() && dropDir.toPath().startsWith(draggedFile.toPath())) {
								return false; // Cannot drop an ancestor into a descendant
							}

							// Prevent dropping a file to its own current directory
							if (dropDir.equals(draggedFile.getParentFile())) {
								return false;
							}
						}
					} catch (Exception e) {
						return true;
					}

					return true;
				}
			}

			return false;
		}

		// Perform the drop action (import)
		@Override
		public boolean importData(TransferSupport support) {
			if (!canImport(support)) {
				return false;
			}

			JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
			TreePath path = dropLocation.getPath();
			DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path.getLastPathComponent();
			File targetFile = (File) targetNode.getUserObject();

			// Determine the actual destination directory based on the user's request
			File destinationDir = getDropDirectory(targetFile);

			try {
				@SuppressWarnings("unchecked") List<File> filesToMove = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

				boolean success = true;
				for (File sourceFile : filesToMove) {
					File newFile = new File(destinationDir, sourceFile.getName());

					// Move the file (rename is a move on the same file system)
					if (!sourceFile.renameTo(newFile)) {
						// Fallback to copy/delete if rename fails (e.g., cross-filesystem move)
						try {
							Files.move(sourceFile.toPath(), newFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							success = false;
							break;
						}
					}
				}

				if (success) {
					prism.getFileExplorer().refresh();
					return true;
				} else {
					return false;
				}

			} catch (Exception e) {
				return false;
			}
		}

		private File getDropDirectory(File targetFile) {
			if (targetFile.isDirectory()) {
				return targetFile;
			} else {
				return targetFile.getParentFile();
			}
		}
	}
}
