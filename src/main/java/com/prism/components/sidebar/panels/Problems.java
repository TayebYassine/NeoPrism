package com.prism.components.sidebar.panels;

import com.prism.Prism;
import com.prism.components.definition.PrismFile;
import com.prism.components.extended.JDefaultKineticScrollPane;
import com.prism.managers.FileManager;
import com.prism.managers.TextAreaManager;
import com.prism.utils.Languages;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Problems extends JPanel {
	private static Prism prism = Prism.getInstance();

	private Map<PrismFile, List<TextAreaManager.Problem>> fileLineData;
	private final JTree fileTree;

	private DefaultTreeModel buildTreeModel(Map<PrismFile, List<TextAreaManager.Problem>> data) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(prism.getLanguage().get(32));

		for (Map.Entry<PrismFile, List<TextAreaManager.Problem>> entry : data.entrySet()) {
			PrismFile prismFile = entry.getKey();
			DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(prismFile);

			List<TextAreaManager.Problem> infos = entry.getValue();
			if (infos != null) {
				for (TextAreaManager.Problem info : infos) {
					DefaultMutableTreeNode problemNode = new DefaultMutableTreeNode(info);
					fileNode.add(problemNode);
				}
			}

			rootNode.add(fileNode);
		}

		return new DefaultTreeModel(rootNode);
	}

	public Problems() {
		super(new BorderLayout());

		this.fileLineData = new HashMap<>();

		DefaultTreeModel treeModel = buildTreeModel(this.fileLineData);

		fileTree = new JTree(treeModel);
		fileTree.setFocusable(true);
		fileTree.setBorder(new EmptyBorder(5, 5, 5, 0));
		fileTree.setRootVisible(false);

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

					if (userObj instanceof TextAreaManager.Problem info) {
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

	public void updateTreeData() {
		this.fileLineData = new HashMap<>();

		for (PrismFile file : FileManager.getFiles()) {
			List<TextAreaManager.Problem> infos = file.getProblems();

			if (Languages.getService(file.getFile()) == null || infos == null) {
				continue;
			}

            List<TextAreaManager.Problem> existing = this.fileLineData.computeIfAbsent(file, k -> new ArrayList<>());

            existing.addAll(infos);
		}

		DefaultTreeModel newModel = buildTreeModel(this.fileLineData);
		fileTree.setModel(newModel);

		expandAllNodes();
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

				File actualFile = pf.getFile();

				if (actualFile != null) {
					setIcon(Languages.getIcon(actualFile));
				} else {
					setIcon(ResourceUtil.getIcon("icons/file.png"));
				}

			} else if (userObj instanceof TextAreaManager.Problem info) {
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
					setText(prism.getLanguage().get(135, (info.getLine() + 1), info.getMessage()));
				} else {
					setText(String.format("%s (@ %d)", label, (info.getLine() + 1)));
				}

				Icon icon = ResourceUtil.getIconFromSVG("icons/ui/alert-small.svg", 18, 18);
				setIcon(icon);
			} else if (node.isRoot()) {
				setText(String.valueOf(userObj));
			} else {
				setText(String.valueOf(userObj));
				setIcon(ResourceUtil.getIcon("icons/file.png"));
			}

			setFont(getFont().deriveFont(Font.PLAIN));
			return this;
		}
	}
}
