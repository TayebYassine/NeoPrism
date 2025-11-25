package com.prism.components.sidebar.panels;

import com.prism.Prism;
import com.prism.utils.StringUtil;
import com.prism.utils.Symbols;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolsPanel extends JPanel {
	private static final Prism prism = Prism.getInstance();

	private final JTree tree = new JTree();
	private final DefaultTreeModel model;

	public SymbolsPanel() {
		super(new java.awt.BorderLayout());

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(prism.getLanguage().get(237));
		model = new DefaultTreeModel(root);
		tree.setModel(model);
		tree.setRootVisible(false);

		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree,
														  Object value,
														  boolean sel,
														  boolean expanded,
														  boolean leaf,
														  int row,
														  boolean hasFocus) {

				super.getTreeCellRendererComponent(tree, value, sel,
						expanded, leaf, row, hasFocus);

				if (value instanceof KindNode kind) {
					setIcon(Symbols.getSymbolIcon(kind.getUserObject().toString().toLowerCase()));
				}

				return this;
			}
		});

		add(new JScrollPane(tree), java.awt.BorderLayout.CENTER);
	}

	public void updateTree(Map<String, List<String>> kindToSymbols) {
		SwingUtilities.invokeLater(() -> {
			DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode("Symbols");

			kindToSymbols.forEach((kind, names) -> {
				DefaultMutableTreeNode kindNode = new KindNode(StringUtil.capitalizeFirstLetter(kind));
				newRoot.add(kindNode);

				names.forEach(n -> kindNode.add(new SymbolNode(n)));
			});

			model.setRoot(newRoot);

			for (int i = 0; i < tree.getRowCount(); i++) {
				if (tree.getPathForRow(i).getPathCount() == 2) {
					tree.expandRow(i);
				}
			}
		});
	}

	class KindNode extends DefaultMutableTreeNode {
		KindNode(String kind){ super(kind); }
		@Override public boolean isLeaf(){ return false; }
	}

	class SymbolNode extends DefaultMutableTreeNode {
		SymbolNode(String name){ super(name); }
		@Override public boolean isLeaf(){ return true; }
	}
}