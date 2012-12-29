package org.zlogic.att.ui;

import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

/**
 * Class for creating and showing the tray icon.
 * TODO: Replace with native Java FX version from Java FX 3.0
 * <p/>
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 30.12.12
 * Time: 1:34
 */
public class TrayIcon {
	private final static Logger log = Logger.getLogger(TrayIcon.class.getName());

	public TrayIcon(Stage primaryStage) {
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon/att-16.png"));

			PopupMenu popup = new PopupMenu();
			MenuItem item = new MenuItem("Exit");

			popup.add(item);

			java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image, "Awesome Time Tracker", popup);

			ActionListener listener = new ActionListener() {
				private java.awt.TrayIcon trayIcon;

				public ActionListener setStage(java.awt.TrayIcon trayIcon) {
					this.trayIcon = trayIcon;
					return this;
				}

				@Override
				public void actionPerformed(java.awt.event.ActionEvent arg0) {
					SystemTray.getSystemTray().remove(trayIcon);
					Platform.exit();
				}
			}.setStage(trayIcon);

			ActionListener listenerTray = new ActionListener() {
				private Stage stage;

				public ActionListener setStage(Stage stage) {
					this.stage = stage;
					return this;
				}

				@Override
				public void actionPerformed(java.awt.event.ActionEvent arg0) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							if (stage.isShowing())
								stage.hide();
							else
								stage.show();
						}
					});
				}
			}.setStage(primaryStage);

			trayIcon.addActionListener(listenerTray);
			item.addActionListener(listener);

			try {
				tray.add(trayIcon);
			} catch (Exception e) {
				log.severe("Cannot add icon to tray");
			}
		} else {
			log.severe("System tray is unavailable");
		}
	}
}
