package com.prism.components.frames;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.*;
import com.prism.Prism;
import com.prism.components.extended.JKineticScrollPane;
import com.prism.components.textarea.TextArea;
import com.prism.utils.Languages;
import com.prism.utils.ResourceUtil;
import com.prism.utils.Theme;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class TextDifferFrame extends JFrame {
	private static Prism prism = Prism.getInstance();

	private final Icon YELLOW_ICON = ResourceUtil.getIcon("icons/ui/symbol-yellow-square.gif");
	private final Icon GREEN_ICON = ResourceUtil.getIcon("icons/ui/symbol-green-circle.gif");
	private final Icon RED_ICON = ResourceUtil.getIcon("icons/ui/symbol-red-square.gif");
	private final Highlighter.HighlightPainter removedPainter = new DefaultHighlighter.DefaultHighlightPainter(Theme.invertColorIfDarkThemeSet(new Color(255, 190, 190))); // Light Red
	private final Highlighter.HighlightPainter addedPainter = new DefaultHighlighter.DefaultHighlightPainter(Theme.invertColorIfDarkThemeSet(new Color(190, 255, 190))); // Light Green
	private final Highlighter.HighlightPainter changedPainter = new DefaultHighlighter.DefaultHighlightPainter(Theme.invertColorIfDarkThemeSet(new Color(255, 255, 150))); // Light Yellow
	private final TextArea oldTextArea;
	private final TextArea newTextArea;
	private final JKineticScrollPane oldScrollPane;
	private final JKineticScrollPane newScrollPane;
	private final JLabel label;

	public TextDifferFrame(File oldFile, String oldText, File file, String text) {
		setTitle(prism.getLanguage().get(110, file.getAbsolutePath()));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(null);

		setExtendedState(MAXIMIZED_BOTH);

		setIconImages(ResourceUtil.getAppIcon());

		oldTextArea = createTextArea(oldFile, oldText);
		newTextArea = createTextArea(file, text);

		oldScrollPane = new JKineticScrollPane(oldTextArea);
		oldScrollPane.setBorder(new EmptyBorder(2, 2, 2, 2));

		newScrollPane = new JKineticScrollPane(newTextArea);
		newScrollPane.setBorder(new EmptyBorder(2, 2, 2, 2));

		oldScrollPane.getVerticalScrollBar().setModel(newScrollPane.getVerticalScrollBar().getModel());

		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(oldScrollPane, BorderLayout.CENTER);
		leftPanel.setBorder(BorderFactory.createTitledBorder(prism.getLanguage().get(109)));

		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(newScrollPane, BorderLayout.CENTER);
		rightPanel.setBorder(BorderFactory.createTitledBorder(prism.getLanguage().get(108)));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
		splitPane.setResizeWeight(0.5);
		splitPane.setBorder(new EmptyBorder(0, 5, 0, 5));

		label = new JLabel(prism.getLanguage().get(20));
		label.setBorder(new EmptyBorder(10, 10, 10, 0));

		SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.5));

		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
		add(label, BorderLayout.NORTH);

		compareAndHighlight(oldText, text);

		setVisible(true);
	}

	private TextArea createTextArea(File file, String text) {
		TextArea textArea = new TextArea(true);

		textArea.setText(text);

		textArea.setSyntaxEditingStyle(Languages.getHighlighter(file));
		textArea.addSyntaxHighlighting();
		textArea.setEditable(false);
		textArea.setHighlightCurrentLine(false);

		return textArea;
	}

	private void compareAndHighlight(String oldText, String newText) {
		oldTextArea.getHighlighter().removeAllHighlights();
		newTextArea.getHighlighter().removeAllHighlights();

		List<String> oldLines = Arrays.asList(oldText.split("\n"));
		List<String> newLines = Arrays.asList(newText.split("\n"));

		try {
			Patch<String> patch = DiffUtils.diff(oldLines, newLines);

			int deleted = 0, inserted = 0, changed = 0;

			for (AbstractDelta<String> delta : patch.getDeltas()) {

				int originalStart = delta.getSource().getPosition();
				int originalSize = delta.getSource().getLines().size();

				int revisedStart = delta.getTarget().getPosition();
				int revisedSize = delta.getTarget().getLines().size();

				if (delta instanceof DeleteDelta) {
					highlightLineRange(oldTextArea, originalStart, originalStart + originalSize - 1, removedPainter);

					addLineTrackingIcon(oldScrollPane, originalStart, originalStart + originalSize - 1, RED_ICON, "Delete");

					deleted++;
				} else if (delta instanceof InsertDelta) {
					highlightLineRange(newTextArea, revisedStart, revisedStart + revisedSize - 1, addedPainter);

					addLineTrackingIcon(newScrollPane, revisedStart, revisedStart + revisedSize - 1, GREEN_ICON, "Insert");

					inserted++;
				} else if (delta instanceof ChangeDelta) {
					highlightLineRange(oldTextArea, originalStart, originalStart + originalSize - 1, changedPainter);
					highlightLineRange(newTextArea, revisedStart, revisedStart + revisedSize - 1, changedPainter);

					addLineTrackingIcon(oldScrollPane, originalStart, originalStart + originalSize - 1, YELLOW_ICON, "Change");
					addLineTrackingIcon(newScrollPane, revisedStart, revisedStart + revisedSize - 1, YELLOW_ICON, "Change");

					changed++;
				}
			}

			if (deleted == 0 && inserted == 0 && changed == 0) {
				label.setText(prism.getLanguage().get(20));
			} else {
				label.setText(prism.getLanguage().get(21, changed, inserted, deleted));
			}
		} catch (Exception e) {

		}
	}

	private void addLineTrackingIcon(JKineticScrollPane scrollPane, int startLineIndex, int endLineIndex, Icon icon, String tip) {
		Gutter gutter = scrollPane.getGutter();

		if (!gutter.isBookmarkingEnabled()) {
			gutter.setBookmarkingEnabled(true);
		}

		for (int i = startLineIndex; i <= endLineIndex; i++) {
			try {
				gutter.addLineTrackingIcon(i, icon, tip);
			} catch (BadLocationException e) {

			}
		}
	}

	private void highlightLineRange(RSyntaxTextArea textArea, int startLineIndex, int endLineIndex, Highlighter.HighlightPainter painter) {
		try {
			int startOffset = textArea.getLineStartOffset(startLineIndex);
			int endOffset;

			if (endLineIndex + 1 < textArea.getLineCount()) {
				endOffset = textArea.getLineStartOffset(endLineIndex + 1);
			} else {
				endOffset = textArea.getDocument().getLength();
			}

			if (endOffset > startOffset) {
				textArea.getHighlighter().addHighlight(startOffset, endOffset, painter);
			}
		} catch (BadLocationException e) {

		}
	}
}
