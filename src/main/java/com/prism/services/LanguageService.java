package com.prism.services;

import com.prism.Prism;

import javax.swing.*;

public class LanguageService {
	private static final Prism prism = Prism.getInstance();

	public LanguageService(Service service, JButton button) {
		service.showMenu(button);
	}
}
