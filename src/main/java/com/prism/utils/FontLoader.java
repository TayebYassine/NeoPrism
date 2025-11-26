package com.prism.utils;

import com.prism.Prism;
import com.prism.components.frames.ErrorDialog;

import java.awt.*;
import java.io.InputStream;

public class FontLoader {
	private static Font FONT;

	public static Font getIntelliJFont(float size) {
		if (FONT == null) {
			try (InputStream is = FontLoader.class.getResourceAsStream("/fonts/JetBrainsMono.ttf")) {
				assert is != null;
				FONT = Font.createFont(Font.TRUETYPE_FONT, is);
			} catch (Exception ex) {
				new ErrorDialog(Prism.getInstance(), ex);
			}
		}

		return FONT.deriveFont(size);
	}
}
