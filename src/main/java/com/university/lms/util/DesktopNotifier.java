package com.university.lms.util;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Best-effort OS desktop notification via the system tray. A silent no-op wherever the tray
 *  (or a desktop environment at all) is unavailable, e.g. a headless server or CI sandbox. */
public final class DesktopNotifier {

    private static final Logger log = LoggerFactory.getLogger(DesktopNotifier.class);

    private TrayIcon trayIcon;

    public void show(String title, String message) {
        if (!SystemTray.isSupported()) {
            return;
        }
        try {
            TrayIcon icon = trayIcon();
            icon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        } catch (AWTException e) {
            log.debug("Desktop notification tray unavailable: {}", e.getMessage());
        }
    }

    private synchronized TrayIcon trayIcon() throws AWTException {
        if (trayIcon == null) {
            BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            trayIcon = new TrayIcon(image, "Library Management System");
            trayIcon.setImageAutoSize(true);
            SystemTray.getSystemTray().add(trayIcon);
        }
        return trayIcon;
    }
}
