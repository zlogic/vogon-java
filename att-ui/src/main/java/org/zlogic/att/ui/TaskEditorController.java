package org.zlogic.att.ui;

import javafx.fxml.Initializable;
import org.zlogic.att.data.PersistenceHelper;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for task editor
 * <p/>
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 29.12.12
 * Time: 22:36
 */
public class TaskEditorController implements Initializable {
	private final static Logger log = Logger.getLogger(MainWindowController.class.getName());

	private PersistenceHelper storageManager;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
	}
}
