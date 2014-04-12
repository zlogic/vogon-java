/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.util.converter.DateTimeStringConverter;
import org.zlogic.att.ui.adapters.DataManager;
import org.zlogic.att.ui.adapters.TimeSegmentAdapter;
import org.zlogic.att.ui.timegraph.MouseHandler;
import org.zlogic.att.ui.timegraph.TimeSegmentGraphicsManager;
import org.zlogic.att.ui.timegraph.TimeStepRangeCalculator;

/**
 * Controller for graphical time segment editor
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class TimeGraphController implements Initializable {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(TaskEditorController.class.getName());
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	/**
	 * DataManager reference
	 */
	private DataManager dataManager;
	/**
	 * Graphics objects
	 */
	private TimeSegmentGraphicsManager graphicsManager;
	/**
	 * Mouse events handler
	 */
	private MouseHandler mouseHandler = new MouseHandler();
	/**
	 * Pane used for rendering/output
	 */
	@FXML
	private Pane timeGraphPane;
	/**
	 * Jump to date field
	 */
	@FXML
	private TextField jumpDate;
	/*
	 * Begin: constants
	 */
	/**
	 * Scale change step
	 */
	private static final double scaleStep = 1.2;
	/**
	 * Default scale
	 */
	private static final double defaultScale = 0.00005;
	/**
	 * Minimum space between ticks in pixels
	 */
	private static final double minTickSpacing = 100;
	/*
	 * End: constants
	 */
	/**
	 * Current scale
	 */
	private DoubleProperty scale = new SimpleDoubleProperty(defaultScale);
	/**
	 * Current step between ticks in pixels
	 */
	private LongProperty ticksStep = new SimpleLongProperty(0);
	/**
	 * Current scroll position
	 */
	private DoubleProperty layoutPos = new SimpleDoubleProperty(0);
	/**
	 * Property specifying if the graphics are visible and should be updated on
	 * any changes
	 */
	private BooleanProperty visibleProperty = new SimpleBooleanProperty(false);
	/**
	 * Jump to date value
	 */
	private ObjectProperty<Date> jumpDateValue = new SimpleObjectProperty<>(new Date());
	/**
	 * Format for date/time in tick labels
	 */
	private SimpleDateFormat dateTimeFormat = new SimpleDateFormat(messages.getString("TIMEGRAPH_DATE_TIME_FORMAT"));
	/**
	 * Listener for added/deleted time segments which updates the time graph
	 * accordingly and creates/deletes necessary TimeSegmentGraphics objects
	 */
	private ListChangeListener<TimeSegmentAdapter> dataManagerListener = new ListChangeListener<TimeSegmentAdapter>() {
		@Override
		public void onChanged(Change<? extends TimeSegmentAdapter> change) {
			if (!visibleProperty.get())
				return;
			while (change.next()) {
				if (change.wasRemoved())
					for (TimeSegmentAdapter timeSegment : change.getRemoved())
						graphicsManager.removeTimeSegmentGraphics(timeSegment);
				if (change.wasAdded()) {
					for (TimeSegmentAdapter timeSegment : change.getAddedSubList()) {
						graphicsManager.removeTimeSegmentGraphics(timeSegment);
						graphicsManager.addTimeSegmentGraphics(timeSegment);
					}
				}
			}
		}
	};

	/**
	 * The time step calculator
	 */
	private TimeStepRangeCalculator stepCalculator = new TimeStepRangeCalculator();

	/**
	 * Graphics object containing a graph tick
	 */
	private class Tick {

		/**
		 * The tick line
		 */
		private Line line;
		/**
		 * The tick label
		 */
		private Label label;

		/**
		 * Creates the tick and adds it to the graph
		 *
		 * @param line the tick like object
		 * @param label the tick label object
		 */
		public Tick(Line line, Label label) {
			this.line = line;
			this.label = label;
			timeGraphPane.getChildren().addAll(line, label);
			label.toBack();
			line.toBack();
		}

		/**
		 * Deletes this tick
		 */
		public void dispose() {
			timeGraphPane.getChildren().removeAll(line, label);
		}
	}
	/**
	 * Ticks map, mapping time to a specific tick object
	 */
	private NavigableMap<Long, Tick> ticks = new TreeMap<>();

	/**
	 * Initializes the controller
	 *
	 * @param url initialization URL
	 * @param resourceBundle supplied resources
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		graphicsManager = new TimeSegmentGraphicsManager(mouseHandler, scale, timeGraphPane, layoutPos, visibleProperty);
		timeGraphPane.setCursor(Cursor.MOVE);

		//Enable/disable the component if it becomes visible/invisible
		visibleProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
				if (Objects.equals(newValue, oldValue))
					return;
				if (newValue.equals(Boolean.TRUE)) {
					if (timeGraphPane.widthProperty().greaterThan(0).get())
						updateTimescale();
					dataManager.getTimeSegments().addListener(dataManagerListener);
				} else {
					dataManager.getTimeSegments().removeListener(dataManagerListener);
					graphicsManager.clearTimeScale();
				}
			}
		});

		updateTicksStep();

		//Update graph on scale changes
		scale.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				if (newValue == null || newValue.equals(oldValue))
					return;
				if (!layoutPos.isBound())
					layoutPos.set((layoutPos.get() - timeGraphPane.getWidth() / 2) * (newValue.doubleValue() / oldValue.doubleValue()) + timeGraphPane.getWidth() / 2);
				graphicsManager.updateTimeSegmentGraphics();
				updateTicksStep();
				updateTicks();
			}
		});

		//Update graph on width changes
		timeGraphPane.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				if (!oldValue.equals(newValue)) {
					updateTicks();
					graphicsManager.updateTimeSegmentGraphics();
				}
			}
		});

		//Update "jump to date" value
		layoutPos.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (newValue != null && !newValue.equals(oldValue))
					jumpDateValue.set(graphicsManager.coordinatesToTime(timeGraphPane.widthProperty().divide(2).get()));
			}
		});
		jumpDate.textProperty().bindBidirectional(jumpDateValue, new DateTimeStringConverter());
	}

	/**
	 * Sets the DataManager reference
	 *
	 * @param dataManager the DataManager reference
	 */
	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	/**
	 * The visibility property (if anything is going to be rendered)
	 *
	 * @return the visibility property
	 */
	public BooleanProperty visibleProperty() {
		return visibleProperty;
	}

	/**
	 * Sets the selected time segments list. Doesn't listen to changes for
	 * selectedTimeSegments.
	 *
	 * @param selectedTimeSegments the selected time segments list
	 */
	public void setSelectedTimeSegments(List<TimeSegmentAdapter> selectedTimeSegments) {
		graphicsManager.setSelectedSegments(new ArrayList<>(selectedTimeSegments).toArray(new TimeSegmentAdapter[0]));
	}

	/**
	 * Performs a full refresh of the time scale
	 */
	private void updateTimescale() {
		graphicsManager.clearTimeScale();
		if (mouseHandler.getDragAnchor() == null) {
			//Graph was not moved - so we can jump to the latest time
			Date latestDate = null;
			for (TimeSegmentAdapter timeSegment : dataManager.getTimeSegments())
				if (latestDate == null || timeSegment.endProperty().get().after(latestDate))
					latestDate = timeSegment.endProperty().get();
			if (latestDate != null)
				layoutPos.bind(timeGraphPane.widthProperty()/*.negate().*/.subtract(scale.multiply(latestDate.getTime())));
		}

		//Add time segments to bins
		for (TimeSegmentAdapter timeSegment : dataManager.getTimeSegments())
			graphicsManager.addTimeSegmentGraphics(timeSegment);

		updateTicks();
		graphicsManager.updateTimeSegmentGraphics();
	}

	/**
	 * Updates the ticks step to match the current scale
	 */
	private void updateTicksStep() {
		ticksStep.set(stepCalculator.getTicksStep((long) (minTickSpacing / scale.get())));
	}

	/**
	 * Updates the currently visible time graph ticks
	 */
	private void updateTicks() {
		if (ticksStep.get() <= 0)
			return;
		long startTick = ticksStep.get() * (graphicsManager.coordinatesToTime(timeGraphPane.getLayoutX()).getTime() / ticksStep.get());
		long endTick = ticksStep.get() * (graphicsManager.coordinatesToTime(timeGraphPane.getLayoutX() + timeGraphPane.getWidth()).getTime() / ticksStep.get() + 1);
		Map<Long, Tick> newTicks = new HashMap<>();
		//Generate new ticks
		for (long currentTick = startTick; currentTick <= endTick; currentTick += ticksStep.get()) {
			//Keep old ticks which are still visible
			if (ticks.containsKey(currentTick)) {
				newTicks.put(currentTick, ticks.get(currentTick));
				ticks.remove(currentTick);
				continue;
			}
			//The tick line
			Line line = new Line();
			line.endYProperty().bind(timeGraphPane.heightProperty());
			line.layoutXProperty().bind(graphicsManager.timeToCoordinatesProperty(new Date(currentTick)));
			line.getStyleClass().add("timegraph-tick"); //NOI18N

			//The tick label
			Label label = new Label(dateTimeFormat.format(new Date(currentTick)));
			label.setLabelFor(line);
			label.layoutXProperty().bind(line.layoutXProperty().subtract(label.widthProperty().divide(2)));
			label.getStyleClass().add("timegraph-tick"); //NOI18N

			line.layoutYProperty().bind(label.layoutYProperty().add(label.heightProperty()));

			newTicks.put(currentTick, new Tick(line, label));
		}

		//Dispose unused ticks
		for (Tick oldTick : ticks.values())
			oldTick.dispose();
		ticks.clear();
		ticks.putAll(newTicks);
	}

	/**
	 * Mouse down handler
	 *
	 * @param event the mouse event
	 */
	@FXML
	private void mouseDown(MouseEvent event) {
		mouseHandler.mouseDown(event);
		event.consume();
	}

	/**
	 * Mouse up handler
	 *
	 * @param event the mouse event
	 */
	@FXML
	private void mouseUp(MouseEvent event) {
		mouseHandler.mouseUp(event);
	}

	/**
	 * Mouse dragged handler
	 *
	 * @param event the mouse event
	 */
	@FXML
	private void mouseDragged(MouseEvent event) {
		if (mouseHandler.getDragAction() == MouseHandler.DragAction.MOVE) {
			Point2D dragAnchor = mouseHandler.getDragAnchor();
			if (dragAnchor == null) {
				log.severe(messages.getString("DRAG_ANCHOR_IS_NULL_NOT_HANDLING_DRAG_EVENT"));
				return;
			}
			mouseHandler.mouseDragged(event);
			double deltaX = event.getX() - dragAnchor.getX();
			if (layoutPos.isBound())
				layoutPos.unbind();
			layoutPos.set(layoutPos.get() + deltaX);
			updateTicks();
			graphicsManager.updateTimeSegmentGraphics();
		}
	}

	/*
	 Zoom buttons
	 */
	/**
	 * ZoomPlus button
	 *
	 * @param event the event
	 */
	@FXML
	private void zoomPlus(ActionEvent event) {
		double currentScaleStep = Math.round(Math.log(scale.get() / defaultScale) / Math.log(scaleStep));
		currentScaleStep++;
		scale.set(defaultScale * Math.pow(scaleStep, currentScaleStep));
	}

	/**
	 * ZoomMinus button
	 *
	 * @param event the event
	 */
	@FXML
	private void zoomMinus(ActionEvent event) {
		double currentScaleStep = Math.round(Math.log(scale.get() / defaultScale) / Math.log(scaleStep));
		currentScaleStep--;
		scale.set(defaultScale * Math.pow(scaleStep, currentScaleStep));
	}

	/**
	 * Zoom Reset button
	 *
	 * @param event the event
	 */
	@FXML
	private void zoomReset(ActionEvent event) {
		scale.set(defaultScale);
	}

	/**
	 * Jump to date button
	 *
	 * @param event the event
	 */
	@FXML
	private void jumpToDate(ActionEvent event) {
		if (layoutPos.isBound())
			layoutPos.unbind();
		layoutPos.set(timeGraphPane.widthProperty().divide(2).subtract(scale.multiply(jumpDateValue.get().getTime())).get());

		updateTicks();
		graphicsManager.updateTimeSegmentGraphics();
	}

	/**
	 * Jump to current time button
	 *
	 * @param event the event
	 */
	@FXML
	private void jumpToCurrentTime(ActionEvent event) {
		jumpDateValue.set(new Date());
		jumpToDate(event);
	}
}
