package com.prism.utils;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.frames.WarningDialog;

import javax.swing.*;
import java.awt.*;

public class Theme {
	private final static Prism prism = Prism.getInstance();

	public static void setupFromConfig() {
		switch (prism.getConfig().getInt(ConfigKey.THEME, 1)) {
			case 0 -> setSystemLookAndFeel();
			case 1 -> setLightFlatlafLookAndFeel();
			case 2 -> setDarkFlatlafLookAndFeel();
		}
	}

	public static Color getPrimaryColor() {
		// Only one case, because Light theme and Windows theme have by default their light colors.
		switch (prism.getConfig().getInt(ConfigKey.THEME, 1)) {
			case 2 -> {
				return Color.decode("#3c3f41");
			}
			default -> {
				return null;
			}
		}
	}

	public static Color getSecondaryColor() {
		// Only one case, because Light theme and Windows theme have by default their light colors.
		switch (prism.getConfig().getInt(ConfigKey.THEME, 1)) {
			case 2 -> {
				return Color.decode("#46494b");
			}
			default -> {
				return null;
			}
		}
	}

	public static Color getTertiaryColor() {
		// Only one case, because Light theme and Windows theme have by default their light colors.
		switch (prism.getConfig().getInt(ConfigKey.THEME, 1)) {
			case 2 -> {
				return Color.decode("#dddddd");
			}
			default -> {
				return null;
			}
		}
	}

	public static boolean isDarkTheme() {
		switch (prism.getConfig().getInt(ConfigKey.THEME, 1)) {
			case 2 -> {
				return true;
			}
			default -> {
				return false;
			}
		}
	}

	public static Color invertColorIfDarkThemeSet(Color lightColor) {
		if (!isDarkTheme()) {
			return lightColor;
		}

		return invertColor(lightColor);
	}

	public static Color invertColor(Color lightColor) {
		if (!isDarkTheme()) {
			return lightColor;
		}

		int red = 255 - lightColor.getRed();
		int green = 255 - lightColor.getGreen();
		int blue = 255 - lightColor.getBlue();

		red = (int) (red * 0.9f);
		green = (int) (green * 0.9f);
		blue = (int) (blue * 0.9f);

		return new Color(
				Math.min(255, Math.max(0, red)),
				Math.min(255, Math.max(0, green)),
				Math.min(255, Math.max(0, blue))
		);
	}

	public static void setSystemLookAndFeel() {
		try {
			try {
				UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				WarningDialog.showWarningDialog(prism, e);
			}
			SwingUtilities.updateComponentTreeUI(prism);
		} catch (UnsupportedLookAndFeelException e) {
			WarningDialog.showWarningDialog(prism, e);
		}
	}

	public static void setLightFlatlafLookAndFeel() {
		try {
			com.formdev.flatlaf.FlatLightLaf.setup(new com.formdev.flatlaf.FlatLightLaf());
		} catch (Exception e) {
			setSystemLookAndFeel();
			WarningDialog.showWarningDialog(prism, e);
		}
	}

	public static void setDarkFlatlafLookAndFeel() {
		try {
			com.formdev.flatlaf.FlatLightLaf.setup(new com.formdev.flatlaf.FlatDarkLaf());
		} catch (Exception e) {
			setSystemLookAndFeel();
			WarningDialog.showWarningDialog(prism, e);
		}
	}
}
