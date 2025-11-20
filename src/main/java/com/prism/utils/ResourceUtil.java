package com.prism.utils;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Objects;

public class ResourceUtil {
	public static Image getAppIcon() {
		return Objects.requireNonNull(ResourceUtil.getIcon("icons/Prism.png")).getImage();
	}

	public static ImageIcon getIcon(String resourcePath) {
		if (resourcePath.startsWith("icons/ui/")) {
			if (Theme.isDarkTheme()) {
				resourcePath = "icons/ui/light/" + (resourcePath.split("icons/ui/")[1]);
			} else {
				resourcePath = "icons/ui/dark/" + (resourcePath.split("icons/ui/")[1]);
			}
		}

		URL url = ResourceUtil.class.getClassLoader().getResource(resourcePath);

		if (url != null) {
			return new ImageIcon(url);
		}

		return null;
	}

	public static ImageIcon getIcon(String resourcePath, int size) {
        return getIcon(resourcePath, size, size);
	}

	public static ImageIcon getIcon(String resourcePath, int width, int height) {
		if (resourcePath.startsWith("icons/ui/")) {
			if (Theme.isDarkTheme()) {
				resourcePath = "icons/ui/light/" + (resourcePath.split("icons/ui/")[1]);
			} else {
				resourcePath = "icons/ui/dark/" + (resourcePath.split("icons/ui/")[1]);
			}
		}

		URL url = ResourceUtil.class.getClassLoader().getResource(resourcePath);

		if (url != null) {
			ImageIcon imageIcon = new ImageIcon(url);

			Image image = imageIcon.getImage();
			Image newImg = image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
			imageIcon = new ImageIcon(newImg);

			return imageIcon;
		}

		return null;
	}

	public static ImageIcon getIconFromSVG(String resourcePath) {
		return getIconFromSVG(resourcePath, 64, 64);
	}

	public static ImageIcon getIconFromSVG(String resourcePath, int width, int height) {
		if (resourcePath.startsWith("icons/ui/")) {
			if (Theme.isDarkTheme()) {
				resourcePath = "icons/ui/light/" + (resourcePath.split("icons/ui/")[1]);
			} else {
				resourcePath = "icons/ui/dark/" + (resourcePath.split("icons/ui/")[1]);
			}
		}

		try {
			PNGTranscoder t = new PNGTranscoder();
			t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
			t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);

			URL url = ResourceUtil.class.getClassLoader().getResource(resourcePath);
			if (url == null) {
				return null;
			}

			TranscoderInput input = new TranscoderInput(url.toString());
			ByteArrayOutputStream os = new ByteArrayOutputStream(16_000);
			TranscoderOutput output = new TranscoderOutput(os);

			t.transcode(input, output);

			byte[] png = os.toByteArray();
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(png));
			return img == null ? null : new ImageIcon(img);

		} catch (Exception _) {
			return null;
		}
	}
}
