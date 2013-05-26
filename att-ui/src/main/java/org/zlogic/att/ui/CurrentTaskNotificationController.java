/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.att.ui;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.zlogic.att.ui.adapters.DataManager;
import org.zlogic.att.ui.adapters.TaskAdapter;
import org.zlogic.att.ui.adapters.TimeSegmentAdapter;

/**
 * Controller for the current task notification
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public class CurrentTaskNotificationController implements Initializable {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	/**
	 * DataManager reference
	 */
	private DataManager dataManager;
	/**
	 * The current task label
	 */
	@FXML
	private Label currentTaskLabel;
	/**
	 * The current task time label
	 */
	@FXML
	private Label timeLabel;
	/**
	 * The root node
	 */
	@FXML
	private Node rootNode;
	/**
	 * The popup window
	 */
	private Popup popup = new Popup();
	/**
	 * The popup parent window
	 */
	private Node parentWindow;
	/**
	 * Fade in/out animation
	 */
	private Animation activeAnimation;
	/**
	 * The timer used to display notifications
	 */
	private Timer notificationTimer;

	/**
	 * Initializes the controller
	 *
	 * @param url initialization URL
	 * @param resourceBundle supplied resources
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		//Initialize the popup properties
		popup.getContent().add(rootNode);
		popup.setAutoFix(true);
	}

	/**
	 * Sets the dataManager reference
	 *
	 * @param dataManager the dataManager reference
	 */
	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;
		//Listen for active task changes
		ChangeListener<TimeSegmentAdapter> timeSegmentChangeListener = new ChangeListener<TimeSegmentAdapter>() {
			private ChangeListener<TaskAdapter> taskChangedListener = new ChangeListener<TaskAdapter>() {
				@Override
				public void changed(ObservableValue<? extends TaskAdapter> ov, TaskAdapter oldValue, TaskAdapter newValue) {
					currentTaskLabel.textProperty().unbind();
					if (newValue != null) {
						currentTaskLabel.textProperty().bind(newValue.nameProperty());
					} else {
						currentTaskLabel.setText(messages.getString("NO_TASK_CURRENTLY_ACTIVE"));
					}
				}
			};

			@Override
			public void changed(ObservableValue<? extends TimeSegmentAdapter> ov, TimeSegmentAdapter oldValue, TimeSegmentAdapter newValue) {
				if (newValue != null && newValue.equals(oldValue))
					return;
				if (oldValue != null)
					oldValue.ownerTaskProperty().removeListener(taskChangedListener);
				currentTaskLabel.textProperty().unbind();
				timeLabel.textProperty().unbind();
				if (newValue != null) {
					currentTaskLabel.textProperty().bind(newValue.ownerTaskProperty().get().nameProperty());
					timeLabel.textProperty().bind(newValue.durationProperty());
					newValue.ownerTaskProperty().addListener(taskChangedListener);
					//showWindowAnimation();
				} else {
					currentTaskLabel.setText(messages.getString("NO_TASK_CURRENTLY_ACTIVE"));
					timeLabel.setText(""); //NOI18N
				}
			}
		};
		//Set default text
		timeSegmentChangeListener.changed(null, null, null);
		this.dataManager.timingSegmentProperty().addListener(timeSegmentChangeListener);

		//Start the timer
		if (notificationTimer != null)
			notificationTimer.cancel();
		notificationTimer = new Timer(true);
		TimerTask showNotificationTask = new TimerTask() {
			@Override
			public void run() {
				showWindowAnimation();
			}
		};
		notificationTimer.scheduleAtFixedRate(
				showNotificationTask,
				15 * 60 * 1000,
				15 * 60 * 1000);//TODO: make this configurable
	}

	/**
	 * Set the parent window for the popup
	 *
	 * @param parentWindow the parent window for the popup
	 */
	public void setParentWindow(Node parentWindow) {
		this.parentWindow = parentWindow;
	}

	/**
	 * Shows/hides the notification window animation
	 */
	private void showWindowAnimation() {
		//Required for JavaFX, otherwise dialog won't be displayed
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (activeAnimation != null)//TODO: synchronize this
					activeAnimation.stop();
				if (parentWindow == null || parentWindow.getScene() == null || parentWindow.getScene().getWindow() == null)
					return;
				//Show the window
				Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
				popup.sizeToScene();
				popup.show(parentWindow,
						primaryScreenBounds.getMaxX() - popup.getWidth() - 100,
						primaryScreenBounds.getMaxY() - popup.getHeight() - 100);//FIXME: this is ugly and requires the main window to be constantly opened. But other options are annoying and can even steal focus.
				//Play the animation
				FadeTransition fadeInTransition = new FadeTransition(Duration.millis(500), rootNode);
				fadeInTransition.setFromValue(0.0);
				fadeInTransition.setToValue(1.0);

				FadeTransition fadeOutTransition = new FadeTransition(Duration.millis(500), rootNode);
				fadeOutTransition.setFromValue(1.0);
				fadeOutTransition.setToValue(0.0);

				SequentialTransition animation = new SequentialTransition(
						fadeInTransition,
						new PauseTransition(Duration.millis(10000)),
						fadeOutTransition);

				animation.setOnFinished(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent t) {
						popup.hide();
						activeAnimation = null;
					}
				});
				activeAnimation = animation;
				animation.play();
			}
		});
	}

	/*
	 * Callbacks
	 */
	/**
	 * Hides the window
	 */
	@FXML
	private void hideWindow() {
		rootNode.getScene().getWindow().hide();
	}
}
