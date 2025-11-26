package com.prism.components.sidebar.panels;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.definition.PrismFile;
import com.prism.components.textarea.TextArea;
import com.prism.services.Service;
import com.prism.utils.Languages;
import com.prism.utils.ResourceUtil;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import org.fife.ui.rsyntaxtextarea.folding.FoldManager;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CodeFoldingPanel extends JTree {
	private static final Prism prism = Prism.getInstance();

	private TextArea textArea;
	private File file;

	public CodeFoldingPanel() {
		super(new DefaultTreeModel(new DefaultMutableTreeNode("Folding")));
		tuneUi();
		addTreeNavigationListener();
	}

	public void setFile(PrismFile pf) {
		this.textArea = pf == null ? null : pf.getTextArea();
		this.file = pf == null ? null : pf.getFile();
		if (textArea != null && file != null) updateTree();
	}

	public void updateTree() {
		if (textArea == null) return;

		FoldManager fm = textArea.getFoldManager();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Folding");

		for (int i = 0, top = fm.getFoldCount(); i < top; i++) {
			DefaultMutableTreeNode node = createNode(fm.getFold(i));
			if (node != null) root.add(node);
		}

		setModel(new DefaultTreeModel(root));

		for (int i = 0; i < getRowCount(); i++) expandRow(i);
	}

	public void clear() {
		DefaultTreeModel model = (DefaultTreeModel) getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

		root.removeAllChildren();

		model.reload(root);
	}

	private void tuneUi() {
		setShowsRootHandles(true);
		setFocusable(true);
		setCellRenderer(new Renderer());
		setRootVisible(false);
	}

	private DefaultMutableTreeNode createNode(Fold fold) {
		if (shouldIgnore(fold)) return null;

		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new FoldWrapper(getFoldTitle(fold), fold));
		for (int i = 0, kids = fold.getChildCount(); i < kids; i++) {
			DefaultMutableTreeNode child = createNode(fold.getChild(i));
			if (child != null) node.add(child);
		}
		return node;
	}

	private boolean shouldIgnore(Fold fold) {
		if (!prism.getConfig().getBoolean(ConfigKey.CODE_FOLDING_IGNORE_COMMENTS, true))
			return false;
		String t = getFoldTitle(fold);
		return t.startsWith("//") || t.startsWith("/*") || t.startsWith("/**");
	}

	private String getFoldTitle(Fold fold) {
		try {
			int start = textArea.getLineStartOffset(fold.getStartLine());
			int end = textArea.getLineEndOffset(fold.getStartLine());
			String line = textArea.getText(start, end - start).trim();
			if (!line.isEmpty()) {
				return line.length() > 50 ? line.substring(0, 47) + "..." : line;
			}
		} catch (Exception ignored) {
		}
		return "<undefined at line " + (fold.getStartLine() + 1) + ">";
	}

	private void addTreeNavigationListener() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				TreePath path = getPathForLocation(e.getX(), e.getY());
				if (path == null) return;
				Object last = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
				if (!(last instanceof FoldWrapper)) return;

				Fold f = ((FoldWrapper) last).fold;
				try {
					if (f.isCollapsed()) f.setCollapsed(false);
					textArea.setCaretPosition(textArea.getLineStartOffset(f.getStartLine()));
					textArea.requestFocusInWindow();
				} catch (Exception ignore) {
				}
			}
		});
	}

	public TextArea getTextArea() {
		return textArea;
	}

	private static final class Renderer extends DefaultTreeCellRenderer {
		private final Icon mainParentIcon = ResourceUtil.getIcon("icons/ui/symbol-green-circle.gif");
		private final Icon nestedParentIcon = ResourceUtil.getIcon("icons/ui/symbol-yellow-square.gif");
		private final Icon leafIcon = ResourceUtil.getIcon("icons/ui/symbol-red-square.gif");

		private final Map<String, Icon> customIconCache = new ConcurrentHashMap<>();

		@Override
		public Component getTreeCellRendererComponent(
				JTree tree, Object value,
				boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			FoldWrapper fw = unwrap(value);
			if (fw == null) {
				setIcon(null);
				return this;
			}

			CodeFoldingPanel folding = prism.getCodeFoldingPanel();
			if (folding != null && folding.file != null && folding.file.exists()) {
				Service svc = Languages.getService(folding.file);
				if (svc != null) {
					Icon custom = customIconCache.computeIfAbsent(fw.title, t -> svc.getIconOfCodeFoldingLine(t));
					if (custom != null) {
						setIcon(custom);
						return this;
					}
				}
			}

			int depth = ((DefaultMutableTreeNode) value).getLevel();
			if (depth == 1) setIcon(mainParentIcon);
			else if (fw.fold.getChildCount() > 0) setIcon(nestedParentIcon);
			else setIcon(leafIcon);

			return this;
		}

		private static FoldWrapper unwrap(Object value) {
			if (!(value instanceof DefaultMutableTreeNode)) return null;
			Object u = ((DefaultMutableTreeNode) value).getUserObject();
			return (u instanceof FoldWrapper) ? (FoldWrapper) u : null;
		}
	}

	private static final class FoldWrapper {
		final String title;
		final Fold fold;

		FoldWrapper(String title, Fold fold) {
			this.title = title;
			this.fold = fold;
		}

		@Override
		public String toString() {
			return title;
		}
	}
}