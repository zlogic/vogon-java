/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Class for creating and showing the tray icon.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
//TODO: Replace with native Java FX version from Java FX 3.0
public class TrayIcon {

	private final static Logger log = Logger.getLogger(TrayIcon.class.getName());
	private java.awt.TrayIcon trayIcon;

	public TrayIcon(Stage primaryStage) {
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon/att-16.png"));

			PopupMenu popup = new PopupMenu();
			MenuItem item = new MenuItem("Exit");

			popup.add(item);

			trayIcon = new java.awt.TrayIcon(image, "Awesome Time Tracker", popup);

			ActionListener listener = new ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent arg0) {
					exitApplication();
				}
			};

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

	public void exitApplication() {
		if (trayIcon != null)
			SystemTray.getSystemTray().remove(trayIcon);
		trayIcon = null;
		Platform.exit();
	}
}
