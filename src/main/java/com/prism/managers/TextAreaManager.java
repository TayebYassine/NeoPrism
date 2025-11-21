package com.prism.managers;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.definition.PrismFile;
import com.prism.components.extended.JDefaultKineticScrollPane;
import com.prism.components.extended.JKineticScrollPane;
import com.prism.components.frames.WarningDialog;
import com.prism.components.textarea.TextArea;
import com.prism.utils.ResourceUtil;
import com.prism.utils.Theme;
import org.fife.ui.rtextarea.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TextAreaManager {
	private static final Prism prism = Prism.getInstance();

	public static void setGutter(JKineticScrollPane scrollPane) {
		Gutter gutter = scrollPane.getGutter();

		Font lineNumberFont = gutter.getLineNumberFont();
		gutter.setLineNumberFont(lineNumberFont.deriveFont((float) prism.getConfig().getInt(ConfigKey.TEXTAREA_ZOOM, 12)));

		if (prism.getConfig().getBoolean(ConfigKey.BOOK_MARKS, true)) {
			gutter.setBookmarkingEnabled(true);
			gutter.setBookmarkIcon(ResourceUtil.getIconFromSVG("icons/ui/bookmark2.svg", 12, 12));

			gutter.addIconRowListener(new IconRowListener() {
				@Override
				public void bookmarkAdded(IconRowEvent e) {
					prism.getBookmarks().updateTreeData(TextAreaManager.getBookmarksOfAllFiles());
				}

				@Override
				public void bookmarkRemoved(IconRowEvent e) {
					prism.getBookmarks().updateTreeData(TextAreaManager.getBookmarksOfAllFiles());
				}
			});
		}

		gutter.setFoldIndicatorStyle(FoldIndicatorStyle.MODERN);

		if (Theme.isDarkTheme()) {
			gutter.setBorderColor(Theme.invertColor(Theme.getSecondaryColor()));
		}

		gutter.setLineNumbersEnabled(prism.getConfig().getBoolean(ConfigKey.SHOW_LINE_NUMBERS, true));
	}

	public static List<BookmarkInfo> getBookmarksOfFile(PrismFile pf) {
		JKineticScrollPane scrollPane = pf.getScrollPane();
		List<BookmarkInfo> bookmarks = new ArrayList<>();

		if (scrollPane == null) {
			return bookmarks;
		}

		Gutter gutter = scrollPane.getGutter();

		for (GutterIconInfo iconInfo : gutter.getBookmarks()) {
			try {
				int line;

				line = pf.getTextArea().getLineOfOffset(iconInfo.getMarkedOffset());

				bookmarks.add(new BookmarkInfo(line));
			} catch (Exception e) {
				new WarningDialog(prism, e);
			}
		}

		return bookmarks;
	}

	public static List<Bookmark> getBookmarksOfAllFiles() {
		List<Bookmark> bookmarks = new ArrayList<>();

		for (PrismFile file : FileManager.files) {
			List<BookmarkInfo> fileBookmarks = getBookmarksOfFile(file);

			bookmarks.add(new Bookmark(file, fileBookmarks));
		}

		return bookmarks;
	}

	public static void zoomIn() {
		if (!FileManager.files.isEmpty()) {
			for (PrismFile file : FileManager.files) {
				TextArea textArea = file.getTextArea();

				if (textArea == null) {
					continue;
				}

				Font font = textArea.getFont();
				float size = font.getSize() + 1.0f;

				if (size > 35.0f) {
					return;
				}

				textArea.setFont(font.deriveFont(size));

				JKineticScrollPane scrollPane = (JKineticScrollPane) SwingUtilities.getAncestorOfClass(JKineticScrollPane.class,
						textArea);
				if (scrollPane != null) {
					Gutter gutter = scrollPane.getGutter();

					Font lineNumberFont = gutter.getLineNumberFont();

					gutter.setLineNumberFont(lineNumberFont.deriveFont(size));
				}

				prism.getConfig().set(ConfigKey.TEXTAREA_ZOOM, Math.round(size));
				prism.updateStatusBar();
			}
		}
	}

	public static void zoomOut() {
		if (!FileManager.files.isEmpty()) {
			for (PrismFile file : FileManager.files) {
				TextArea textArea = file.getTextArea();

				if (textArea == null) {
					continue;
				}

				Font font = textArea.getFont();
				float size = font.getSize() - 1.0f;

				if (size < 5.0f) {
					return;
				}

				textArea.setFont(font.deriveFont(size));

				JKineticScrollPane scrollPane = (JKineticScrollPane) SwingUtilities.getAncestorOfClass(JKineticScrollPane.class,
						textArea);
				if (scrollPane != null) {
					Gutter gutter = scrollPane.getGutter();

					Font lineNumberFont = gutter.getLineNumberFont();

					gutter.setLineNumberFont(lineNumberFont.deriveFont(size));
				}

				prism.getConfig().set(ConfigKey.TEXTAREA_ZOOM, Math.round(size));
				prism.updateStatusBar();
			}
		}
	}

	public static class Bookmark {

		public PrismFile file;
		public List<BookmarkInfo> bookmarks = new ArrayList<>();

		public Bookmark(PrismFile file, List<BookmarkInfo> bookmarks) {
			this.file = file;
			this.bookmarks = bookmarks;
		}

		public PrismFile getFile() {
			return this.file;
		}

		public List<BookmarkInfo> getBookmarks() {
			return this.bookmarks;
		}
	}

	public static class BookmarkInfo {

		public int line;

		public BookmarkInfo(int line) {
			this.line = line;
		}

		public int getLine() {
			return this.line;
		}
	}

	public static class Problem {
		public String message;
		public int line;

		public Problem(String message, int line) {
			this.message = message;
			this.line = line;
		}

		public String getMessage() {
			return this.message;
		}

		public int getLine() {
			return this.line;
		}
	}
}
