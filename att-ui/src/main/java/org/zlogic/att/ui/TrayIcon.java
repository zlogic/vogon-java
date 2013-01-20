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
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Class for creating and showing the tray icon.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
//FIXME: Replace with native Java FX version from Java FX 3.0
public class TrayIcon {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(TrayIcon.class.getName());
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	/**
	 * The Tray Icon
	 */
	private java.awt.TrayIcon trayIcon;

	/**
	 * Constructs the Tray Icon
	 *
	 * @param primaryStage the stage to be shown/hidden by double-clicking the
	 * icon
	 */
	public TrayIcon(Stage primaryStage) {
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon/att-tilt-16.png")); //NOI18N

			PopupMenu popup = new PopupMenu();
			MenuItem item = new MenuItem(messages.getString("EXIT"));

			popup.add(item);

			trayIcon = new java.awt.TrayIcon(image, messages.getString("AWESOME_TIME_TRACKER"), popup);
			trayIcon.setImageAutoSize(true);

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
				log.log(Level.SEVERE, messages.getString("CANNOT_ADD_ICON_TO_TRAY"), e);
			}
		} else {
			log.severe(messages.getString("SYSTEM_TRAY_IS_UNAVAILABLE"));
		}
	}

	/**
	 * Removes icon from the tray, then proceeds with stopping Java FX
	 */
	public void exitApplication() {
		if (trayIcon != null)
			SystemTray.getSystemTray().remove(trayIcon);
		trayIcon = null;
		Platform.exit();
	}
}
