/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.awt.MouseInfo;
import java.awt.Point;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatterBuilder;
import org.zlogic.att.ui.adapters.DataManager;
import org.zlogic.att.ui.adapters.TaskAdapter;
import org.zlogic.att.ui.adapters.TimeSegmentAdapter;

/**
 * Controller for the inactivity prompt dialog
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class InactivityDialogController implements Initializable {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(InactivityDialogController.class.getName());
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	/**
	 * DataManager reference
	 */
	private DataManager dataManager;
	/**
	 * Exception handler
	 */
	private ExceptionHandler exceptionHandler;
	/**
	 * The root node
	 */
	@FXML
	private Node rootNode;
	/**
	 * Accept action button
	 */
	@FXML
	private Button acceptButton;
	/**
	 * The current task label
	 */
	@FXML
	private Label currentTaskLabel;
	/**
	 * Inactivity time label
	 */
	@FXML
	private Label inactivityTimeLabel;
	/**
	 * Selected action radio group
	 */
	@FXML
	private ToggleGroup selectedAction;
	/**
	 * Keep tracking the current task radio button
	 */
	@FXML
	private RadioButton keepTrackingToggle;
	/**
	 * Split time radio button
	 */
	@FXML
	private RadioButton splitTimeToggle;
	/**
	 * Discard time ratio button
	 */
	@FXML
	private RadioButton discardTimeToggle;
	/**
	 * The stage for this window
	 */
	@FXML
	private Stage stage;
	/**
	 * Timer for checking mouse events
	 */
	private Timer mouseCheckTimer;
	/**
	 * The time inactivity was first detected
	 */
	private Date inactivityStarted;

	/**
	 * Initializes the controller
	 *
	 * @param url initialization URL
	 * @param resourceBundle supplied resources
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		//Prepare the stage
		stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		//Initialize the scene properties
		if (rootNode != null) {
			Scene scene = new Scene((Parent) rootNode);
			stage.setTitle(messages.getString("TIMEOUT_DETECTED"));
			stage.setScene(scene);
		}

		acceptButton.disableProperty().bind(selectedAction.selectedToggleProperty().isNull());

		//Detect mouse movement
		mouseCheckTimer = new Timer(true);
		TimerTask checkMouseMovement = new TimerTask() {
			private int prevX = 0, prevY = 0;
			private Date previousMoveEvent = new Date();
			private long inactivityTimeout = 10 * 60 * 1000;//TODO: make the timeout configurable

			@Override
			public void run() {
				Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
				if ((mouseLocation.x != prevX || mouseLocation.y != prevY) && !stage.isShowing()) {
					inactivityStarted = null;
					previousMoveEvent = new Date();
					prevX = mouseLocation.x;
					prevY = mouseLocation.y;
				} else if ((new Date().getTime() - previousMoveEvent.getTime()) > inactivityTimeout) {
					inactivityStarted = previousMoveEvent;
					//Inactivity detected, show the dialog
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							if (!stage.isShowing() && dataManager.timingSegmentProperty().get() != null)
								stage.show();
							//Update the inactivity time property
							String inactivityTimeString = new Interval(new DateTime(previousMoveEvent), new DateTime()).toPeriod().normalizedStandard(PeriodType.time()).toString(new PeriodFormatterBuilder().printZeroIfSupported().appendHours().appendSeparator(":").minimumPrintedDigits(2).appendMinutes().appendSeparator(":").appendSeconds().toFormatter());
							inactivityTimeLabel.setText(inactivityTimeString);
						}
					});
				}
			}
		};
		mouseCheckTimer.scheduleAtFixedRate(checkMouseMovement, 0, 500);//TODO: make the time configurable
	}

	/**
	 * Sets the dataManager reference
	 *
	 * @param dataManager the dataManager reference
	 */
	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;
		//Listen for active task changes
		this.dataManager.timingSegmentProperty().addListener(new ChangeListener<TimeSegmentAdapter>() {
			private ChangeListener<TaskAdapter> taskChangedListener = new ChangeListener<TaskAdapter>() {
				@Override
				public void changed(ObservableValue<? extends TaskAdapter> ov, TaskAdapter oldValue, TaskAdapter newValue) {
					currentTaskLabel.textProperty().unbind();
					if (newValue != null)
						currentTaskLabel.textProperty().bind(newValue.nameProperty());
					else
						currentTaskLabel.setText(messages.getString("NO_TASK_CURRENTLY_ACTIVE"));
				}
			};

			@Override
			public void changed(ObservableValue<? extends TimeSegmentAdapter> ov, TimeSegmentAdapter oldValue, TimeSegmentAdapter newValue) {
				if (newValue != null && newValue.equals(oldValue))
					return;
				if (oldValue != null)
					oldValue.ownerTaskProperty().removeListener(taskChangedListener);
				currentTaskLabel.textProperty().unbind();
				if (newValue != null) {
					currentTaskLabel.textProperty().bind(newValue.ownerTaskProperty().get().nameProperty());
					newValue.ownerTaskProperty().addListener(taskChangedListener);
				} else {
					currentTaskLabel.setText(messages.getString("NO_TASK_CURRENTLY_ACTIVE"));
				}
			}
		});

	}

	/**
	 * Returns the exception handler
	 *
	 * @return the exception handler
	 */
	public ExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	/**
	 * Sets the exception handler
	 *
	 * @param exceptionHandler the exception handler to set
	 */
	public void setExceptionHandler(ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * Sets the window icons
	 *
	 * @param icons the icons to be set
	 */
	public void setWindowIcons(ObservableList<Image> icons) {
		stage.getIcons().setAll(icons);
	}

	/*
	 * Callbacks
	 */
	/**
	 * Accepts the changes and hides the window
	 */
	@FXML
	private void acceptChanges() {
		//Choose an action based on the selected radio button
		if (selectedAction.getSelectedToggle().equals(keepTrackingToggle)) {
			//Do nothing
		} else if (selectedAction.getSelectedToggle().equals(splitTimeToggle)) {
			//Split time
			TimeSegmentAdapter initialSegment = dataManager.timingSegmentProperty().get();
			if (initialSegment != null) {
				TaskAdapter taskAdapter = initialSegment.ownerTaskProperty().get();
				Date inactivityStopped = new Date();
				//Stop timing
				dataManager.stopTiming();
				initialSegment.endProperty().set(inactivityStarted);
				//Create inactivity segment
				TimeSegmentAdapter inactivitySegmentAdapter = taskAdapter.createTimeSegment();
				inactivitySegmentAdapter.setStartEndTime(inactivityStarted, inactivityStopped);
				//Create & start timing the new segment
				TimeSegmentAdapter newSegmentAdapter = taskAdapter.createTimeSegment();
				newSegmentAdapter.setStartEndTime(inactivityStopped, inactivityStopped);
				dataManager.startTiming(newSegmentAdapter);
			}
		} else if (selectedAction.getSelectedToggle().equals(discardTimeToggle)) {
			//Discard time after inactivity was detected
			TimeSegmentAdapter segment = dataManager.timingSegmentProperty().get();
			if (segment != null) {
				segment.stopTiming();
				segment.endProperty().set(inactivityStarted);
			}
		} else {
			log.log(Level.SEVERE, messages.getString("INVALID_ACTION_SELECTED_FORMAT"), selectedAction.getSelectedToggle().toString());
			exceptionHandler.showException(messages.getString("INVALID_ACTION_SELECTED") + selectedAction.getSelectedToggle().toString(), null, false);
		}
		selectedAction.selectToggle(null);
		rootNode.getScene().getWindow().hide();
	}
}
