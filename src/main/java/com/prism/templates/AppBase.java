package com.prism.templates;

import com.prism.utils.ResourceUtil;

import javax.swing.*;
import java.io.File;

public class AppBase {
	private String name;
	private String iconPath;

	public AppBase(String name, String iconPath) {
		this.name = name;
		this.iconPath = iconPath;
	}

	public String getName() {
		return name;
	}

	public ImageIcon getIcon() {
		return ResourceUtil.getIconFromSVG(iconPath, 32, 32);
	}

	public void create(File directory) {
		return;
	}
}
