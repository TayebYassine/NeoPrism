package com.prism.components.sidebar.panels;

import com.prism.managers.ThreadsManager;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.List;
import java.util.UUID;

public class ThreadsTree extends JTree {

	private static final Icon ROOT_ICON = UIManager.getIcon("Tree.leafIcon");
	private static final Icon RUN_ICON = ResourceUtil.getIconFromSVG("icons/ui/settings.svg", 16, 16);
	private static final Icon STOP_ICON = ResourceUtil.getIconFromSVG("icons/ui/settings.svg", 16, 16);

	private final DefaultTreeModel model;
	private final DefaultMutableTreeNode rootNode;

	private final Timer refreshTimer;

	public ThreadsTree() {
		rootNode = new DefaultMutableTreeNode("Active Threads");
		model = new DefaultTreeModel(rootNode);
		setModel(model);

		setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree,
														  Object value,
														  boolean sel,
														  boolean expanded,
														  boolean leaf,
														  int row,
														  boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				if (value == rootNode) {
					setIcon(ROOT_ICON);
				} else if (value instanceof ThreadNode tn) {
					setIcon(tn.alive ? RUN_ICON : STOP_ICON);
				}
				return this;
			}
		});

		refreshTimer = new Timer(500, e -> rebuildTree());
		refreshTimer.setRepeats(true);
		refreshTimer.start();

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent e) {
			}

			public void ancestorMoved(AncestorEvent e) {
			}

			public void ancestorRemoved(AncestorEvent e) {
				refreshTimer.stop();
			}
		});
	}

	private void rebuildTree() {
		rootNode.removeAllChildren();

		List<UUID> ids = ThreadsManager.getRunningIds();

		for (UUID id : ids) {
			boolean alive = ThreadsManager.isAlive(id);
			rootNode.add(new ThreadNode(id, alive));
		}

		if (SwingUtilities.isEventDispatchThread()) {
			model.reload();
		} else {
			SwingUtilities.invokeLater(model::reload);
		}
	}

	private static class ThreadNode extends DefaultMutableTreeNode {
		final UUID id;
		final boolean alive;

		ThreadNode(UUID id, boolean alive) {
			this.id = id;
			this.alive = alive;

			setUserObject((alive ? "● " : "■ ") + id.toString().substring(0, 8));
		}
	}
}