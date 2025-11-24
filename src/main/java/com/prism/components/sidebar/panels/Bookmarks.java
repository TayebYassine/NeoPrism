package com.prism.components.sidebar.panels;

import com.prism.Prism;
import com.prism.components.definition.PrismFile;
import com.prism.components.extended.JDefaultKineticScrollPane;
import com.prism.managers.TextAreaManager.Bookmark;
import com.prism.managers.TextAreaManager.BookmarkInfo;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bookmarks extends JPanel {
	private static Prism prism = Prism.getInstance();

	private Map<PrismFile, List<BookmarkInfo>> fileLineData;
	private final JTree fileTree;

	private DefaultTreeModel buildTreeModel(Map<PrismFile, List<BookmarkInfo>> data) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(prism.getLanguage().get(26));

		for (Map.Entry<PrismFile, List<BookmarkInfo>> entry : data.entrySet()) {
			PrismFile prismFile = entry.getKey();
			DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(prismFile);

			List<BookmarkInfo> infos = entry.getValue();
			if (infos != null) {
				for (BookmarkInfo info : infos) {
					// Store the BookmarkInfo object as the user object of the leaf node
					DefaultMutableTreeNode bookmarkNode = new DefaultMutableTreeNode(info);
					fileNode.add(bookmarkNode);
				}
			}

			rootNode.add(fileNode);
		}

		return new DefaultTreeModel(rootNode);
	}

	public Bookmarks() {
		super(new BorderLayout());

		this.fileLineData = new HashMap<>();

		DefaultTreeModel treeModel = buildTreeModel(this.fileLineData);

		fileTree = new JTree(treeModel);
		fileTree.setFocusable(true);
		fileTree.setBorder(new EmptyBorder(5, 5, 5, 0));
		fileTree.setRootVisible(true);

		fileTree.setCellRenderer(new FileTreeCellRenderer());

		fileTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();

				if (node == null || node.isRoot()) {
					return;
				}

				if (node.isLeaf()) {
					Object userObj = node.getUserObject();
					int lineNumber = -1;

					if (userObj instanceof BookmarkInfo info) {
						lineNumber = info.getLine();
					} else if (userObj instanceof Integer) {
						lineNumber = (int) userObj;
					} else {
						return;
					}

					DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
					PrismFile fileObject = null;

					Object parentObj = parentNode.getUserObject();
					if (parentObj instanceof PrismFile) {
						fileObject = (PrismFile) parentObj;
					}

					if (fileObject != null) {
						fileObject.getTextArea().setCursorOnLine(lineNumber);

						Prism.getInstance().getTextAreaTabbedPane().redirectUserToTab(fileObject);
					}
				}
			}
		});

		expandAllNodes(); // expand initial tree (empty)

		JDefaultKineticScrollPane scrollPane = new JDefaultKineticScrollPane(fileTree);
		this.add(scrollPane, BorderLayout.CENTER);

		this.setPreferredSize(new Dimension(450, 300));
	}

	public void updateTreeData(List<Bookmark> bookmarks) {
		this.fileLineData = new HashMap<>();

		for (Bookmark bookmark : bookmarks) {
			PrismFile file = bookmark.getFile();

			List<BookmarkInfo> infos = bookmark.getBookmarks();

			if (infos == null) {
				continue;
			}

            List<BookmarkInfo> existing = this.fileLineData.computeIfAbsent(file, k -> new ArrayList<>());

            existing.addAll(infos);
		}

		DefaultTreeModel newModel = buildTreeModel(this.fileLineData);
		fileTree.setModel(newModel);
	}

	public void expandAllNodes() {
		for (int i = 0; i < fileTree.getRowCount(); i++) {
			fileTree.expandRow(i);
		}
	}

	private static class FileTreeCellRenderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
													  boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object userObj = node.getUserObject();

			if (userObj instanceof PrismFile pf) {
				setText(pf.getName());

                setIcon(pf.getIcon());
			} else if (userObj instanceof BookmarkInfo info) {
				String label = null;
				try {
					try {
						label = (String) info.getClass().getMethod("getLabel").invoke(info);
					} catch (Exception ex1) {
						try {
							label = (String) info.getClass().getMethod("getName").invoke(info);
						} catch (Exception ex2) {
							try {
								label = (String) info.getClass().getMethod("getText").invoke(info);
							} catch (Exception ex3) {
								/* ignore */
							}
						}
					}
				} catch (Exception ignored) {
				}

				if (label == null || label.trim().isEmpty()) {
					setText("Line " + (info.getLine() + 1));
				} else {
					setText(String.format("%s (Line %d)", label, (info.getLine() + 1)));
				}

				Icon icon = ResourceUtil.getIconFromSVG("icons/ui/bookmark2.svg", 18, 18);
				setIcon(icon);
			} else if (node.isRoot()) {
				setText(String.valueOf(userObj));

				Icon icon = ResourceUtil.getIconFromSVG("icons/ui/book.svg", 18, 18);
				setIcon(icon);
			} else {
				setText(String.valueOf(userObj));
				setIcon(ResourceUtil.getIcon("icons/file.png"));
			}

			setFont(getFont().deriveFont(Font.PLAIN));
			return this;
		}
	}
}
