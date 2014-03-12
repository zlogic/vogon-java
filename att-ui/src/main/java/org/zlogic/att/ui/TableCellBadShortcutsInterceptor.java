/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Listener to intercept "accidental" shortcuts like Shift+SPACE which cause a
 * currently running TableCell edit to be invalidated. This class intercepts
 * such keypresses and marks them as handled, preventing TableView from
 * receiving them
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class TableCellBadShortcutsInterceptor implements EventHandler<KeyEvent> {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(TableCellBadShortcutsInterceptor.class.getName());

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	/**
	 * Property indicating that events should be intercepted
	 */
	private ObservableValue<? extends Boolean> interceptEditProperty = new SimpleBooleanProperty();

	/**
	 * Construct the interceptor
	 *
	 * @param interceptEditProperty the ObservableValue that indicates that
	 * KeyEvents should be intercepted by this TableCellBadShortcutsInterceptor
	 */
	public TableCellBadShortcutsInterceptor(ObservableValue<? extends Boolean> interceptEditProperty) {
		this.interceptEditProperty = interceptEditProperty;
	}

	@Override
	public void handle(KeyEvent t) {
		if (interceptEditProperty != null && interceptEditProperty.getValue() != null && interceptEditProperty.getValue())
			if ((t.isShiftDown() || t.isAltDown() || t.isControlDown() || t.isMetaDown() || t.isShortcutDown())
					&& (t.getCode() == KeyCode.SPACE || t.getCode() == KeyCode.LEFT || t.getCode() == KeyCode.RIGHT)) {
				t.consume();
				log.log(Level.WARNING, messages.getString("CONSUMED_KEY_EVENT"), new String[]{
					t.isShiftDown() ? messages.getString("SHIFT+") : "", //NOI18N
					t.isAltDown() ? messages.getString("ALT+") : "", //NOI18N
					t.isControlDown() ? messages.getString("CONTROL+") : "", //NOI18N
					t.isMetaDown() ? messages.getString("META+") : "", //NOI18N
					t.isShortcutDown() ? messages.getString("SHORTCUT+") : "", //NOI18N
					t.getCode().toString()});
			}
	}
}
