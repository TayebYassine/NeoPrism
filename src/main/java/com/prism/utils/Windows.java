package com.prism.utils;

import com.prism.Prism;
import com.prism.components.frames.AboutPrism;
import com.prism.managers.FileManager;

import javax.swing.*;
import java.awt.*;

public class Windows {
    public static SystemTray tray = SystemTray.getSystemTray();
    public static TrayIcon trayIcon = new TrayIcon(ResourceUtil.getAppIcon(), "Prism " + Prism.getVersion());

    static {
        trayIcon.setImageAutoSize(true);
    }

    public static void sendNotification(String caption, String text, TrayIcon.MessageType messageType) {
        if (SystemTray.isSupported()) {
            trayIcon.setPopupMenu(getMenu(tray, trayIcon));

            try {
                tray.add(trayIcon);

                trayIcon.displayMessage(caption, text, messageType);
            } catch (Exception e) {

            }
        }
    }

    private static PopupMenu getMenu(SystemTray tray, TrayIcon trayIcon) {
        PopupMenu popup = new PopupMenu();

        MenuItem aboutItem = new MenuItem("About");
        aboutItem.addActionListener(e -> SwingUtilities.invokeLater(AboutPrism::new));

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            tray.remove(trayIcon);

            FileManager.saveAllFiles();

            Prism.getInstance().prepareClosing();

            System.exit(0);
        });

        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(exitItem);

        return popup;
    }
}
