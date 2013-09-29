/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.att.ui;

import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import org.zlogic.att.ui.adapters.DataManager;
import org.zlogic.att.ui.adapters.TimeSegmentAdapter;

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
	@FXML
	private Pane timeGraphPane;
	private ObservableList<TimeSegmentAdapter> selectedTimeSegments = FXCollections.observableList(new LinkedList<TimeSegmentAdapter>());
	private Point2D dragAnchor;

	private enum DragAction {

		MOVE, RESIZE
	};
	private DragAction dragAction = DragAction.MOVE;
	/*
	 * Begin: constants
	 */
	private static final double defaultScale = 0.00005;
	private static final int resizeWidth = 10;
	private static final double minTickSpacing = 100;
	private static final long minimumStep = 1000;//1 second
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat(messages.getString("TIMEGRAPH_TIME_FORMAT"));
	/*
	 * End: constants
	 */
	private DoubleProperty scale = new SimpleDoubleProperty(defaultScale);
	private LongProperty ticksStep = new SimpleLongProperty(0);
	private DoubleProperty layoutPos = new SimpleDoubleProperty(0);
	private LongProperty timeSegmentGraphicsBinsize = new SimpleLongProperty();
	private Map<TimeSegmentAdapter, TimeSegmentGraphics> timeSegmentGraphics = new HashMap<>();
	private final NavigableMap<Long, Set<TimeSegmentGraphics>> timeSegmentGraphicsLocations = new TreeMap<>();
	private Set<TimeSegmentGraphics> visibleTimeSegments = new HashSet<>();
	private BooleanProperty visibleProperty = new SimpleBooleanProperty(false);
	private SimpleDateFormat dateFormat = new SimpleDateFormat(messages.getString("TIMEGRAPH_DATE_TIME_FORMAT"));
	private ListChangeListener<TimeSegmentAdapter> dataManagerListener = new ListChangeListener<TimeSegmentAdapter>() {
		@Override
		public void onChanged(Change<? extends TimeSegmentAdapter> change) {
			if (!visibleProperty.get())
				return;
			while (change.next()) {
				if (change.wasRemoved())
					for (TimeSegmentAdapter timeSegment : change.getRemoved())
						removeTimeSegmentGraphics(timeSegment);
				if (change.wasAdded()) {
					for (TimeSegmentAdapter timeSegment : change.getAddedSubList()) {
						removeTimeSegmentGraphics(timeSegment);
						addTimeSegmentGraphics(timeSegment);
					}
				}
			}
		}
	};
	private ListChangeListener<TimeSegmentAdapter> selectedTimeSegmentsListener = new ListChangeListener<TimeSegmentAdapter>() {
		@Override
		public void onChanged(ListChangeListener.Change<? extends TimeSegmentAdapter> change) {
			if (!visibleProperty.get())
				return;
			while (change.next()) {
				if (change.wasRemoved())
					for (TimeSegmentAdapter timeSegment : change.getRemoved()) {
						TimeSegmentGraphics graphics = timeSegmentGraphics.get(timeSegment);
						if (graphics != null)
							graphics.selectedProperty.set(false);
					}
				if (change.wasAdded())
					for (TimeSegmentAdapter timeSegment : change.getAddedSubList()) {
						TimeSegmentGraphics graphics = timeSegmentGraphics.get(timeSegment);
						if (graphics != null)
							graphics.selectedProperty.set(true);
					}
			}

		}
	};

	private class TimeSegmentGraphics {

		private Rectangle rect, rectLeft, rectRight;
		private Label rectLabel, rectLeftLabel, rectRightLabel;
		private TimeSegmentAdapter timeSegment;
		private Point2D localClick;
		private EventHandler<MouseEvent> mousePressHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				localClick = new Point2D(mouseEvent.getX(), mouseEvent.getY());
				dragAction = DragAction.RESIZE;
				isDragged.set(true);
			}
		};
		private EventHandler<MouseEvent> mouseReleaseHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				isDragged.set(false);
			}
		};
		private BooleanProperty selectedProperty = new SimpleBooleanProperty(false);
		private boolean initialized = false;
		private BooleanProperty outOfRange = new SimpleBooleanProperty(true);
		private BooleanProperty isDragged = new SimpleBooleanProperty(false);
		private ChangeListener<Date> updateListener = new ChangeListener<Date>() {
			private TimeSegmentGraphics graphics;

			public ChangeListener<Date> setGraphics(TimeSegmentGraphics graphics) {
				this.graphics = graphics;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends Date> observableValue, Date oldValue, Date newValue) {
				if (newValue.equals(oldValue))
					return;
				if (!initialized) {
					//Check if time segment has crawled into the visible screen
					updateTimeSegmentGraphics(graphics);
					updateTimeSegmentGraphics();
				}
				if (initialized)
					updateGraphics();
			}
		}.setGraphics(this);
		private ChangeListener<Boolean> outOfRangeListener = new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
				if (newValue)
					disposeGraphics();
			}
		};
		private ChangeListener<Number> widthLargerThanZeroListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
				if (newValue == oldValue || newValue == null)
					return;
				if ((newValue instanceof Double && (Double) newValue > 0) || newValue.floatValue() > 0) {
					if (!timeGraphPane.getChildren().contains(rectLeft)) {
						timeGraphPane.getChildren().add(rectLeft);
						timeGraphPane.getChildren().add(rectLeftLabel);
					}
					if (!timeGraphPane.getChildren().contains(rectRight)) {
						timeGraphPane.getChildren().add(rectRight);
						timeGraphPane.getChildren().add(rectRightLabel);
					}
				} else {
					timeGraphPane.getChildren().removeAll(rectLeft, rectRight, rectLeftLabel, rectRightLabel);
				}
			}
		};
		private ChangeListener<Boolean> selectedListener = new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
				if (newValue.equals(oldValue) || rect == null)
					return;
				if (newValue)
					rect.getStyleClass().add("selected-segment"); //NOI18N
				else
					rect.getStyleClass().remove("selected-segment"); //NOI18N
				//Bring selected item to front
				if (newValue)
					toFront();
			}
		};
		private EventHandler<MouseEvent> selectHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent t) {
				selectedTimeSegments.setAll(timeSegment);
			}
		};

		public TimeSegmentGraphics(TimeSegmentAdapter timeSegment) {
			this.timeSegment = timeSegment;
			outOfRange.addListener(outOfRangeListener);
			timeSegment.startProperty().addListener(updateListener);
			timeSegment.endProperty().addListener(updateListener);
		}

		private void setResizeHandleRectProperties(Rectangle rectHandle) {
			rectHandle.setCursor(Cursor.W_RESIZE);
			rectHandle.setWidth(resizeWidth);
			rectHandle.heightProperty().bind(rect.heightProperty());
			rectHandle.layoutYProperty().bind(rect.layoutYProperty());
			rectHandle.getStyleClass().add("timegraph-handle"); //NOI18N
			rectHandle.disableProperty().bind(selectedProperty.not());
		}

		private void setLabelForRect(Rectangle rectangle, Label label) {
			label.layoutXProperty().bind(rectangle.layoutXProperty());
			label.layoutYProperty().bind(rectangle.layoutYProperty());
			label.maxHeightProperty().bind(rectangle.heightProperty());
			label.prefHeightProperty().bind(rectangle.heightProperty());
			label.maxWidthProperty().bind(rectangle.widthProperty());
			label.prefWidthProperty().bind(rectangle.widthProperty());
			label.setLabelFor(rectangle);
		}

		private void setLabelForRectVertical(Rectangle rectangle, Label label) {
			label.layoutXProperty().bind(rectangle.layoutXProperty().add(label.heightProperty().subtract(label.widthProperty()).divide(2)));
			label.layoutYProperty().bind(rectangle.layoutYProperty().add(label.widthProperty().subtract(label.heightProperty()).divide(2)));
			label.maxHeightProperty().bind(rectangle.widthProperty());
			label.minHeightProperty().bind(rectangle.widthProperty());
			label.prefHeightProperty().bind(rectangle.widthProperty());
			label.maxWidthProperty().bind(rectangle.heightProperty());
			label.minWidthProperty().bind(rectangle.heightProperty());
			label.prefWidthProperty().bind(rectangle.heightProperty());
			label.setLabelFor(rectangle);
		}

		private void init() {
			if (initialized)
				return;
			initialized = true;

			selectedProperty.set(false);
			selectedProperty.addListener(selectedListener);

			//Init main rectangle
			rect = new Rectangle();
			rect.setHeight(100);
			rect.layoutYProperty().bind(timeGraphPane.heightProperty().subtract(rect.heightProperty()).divide(2));
			rect.getStyleClass().add("timegraph-segment"); //NOI18N
			rect.setCursor(Cursor.DEFAULT);

			//Init resize rectangles
			rectLeft = new Rectangle();
			rectLeft.layoutXProperty().bind(rect.layoutXProperty().subtract(resizeWidth));
			setResizeHandleRectProperties(rectLeft);

			rectLeftLabel = new Label();
			rectLeftLabel.setRotate(270);
			setLabelForRectVertical(rectLeft, rectLeftLabel);
			rectLeftLabel.setAlignment(Pos.CENTER);
			rectLeftLabel.setDisable(true);
			rectLeftLabel.getStyleClass().addAll("timegraph-handle", "text"); //NOI18N

			rectRight = new Rectangle();
			rectRight.layoutXProperty().bind(rect.layoutXProperty().add(rect.widthProperty()));
			setResizeHandleRectProperties(rectRight);

			rectRightLabel = new Label();
			rectRightLabel.setRotate(270);
			setLabelForRectVertical(rectRight, rectRightLabel);
			rectRightLabel.setAlignment(Pos.CENTER);
			rectRightLabel.setDisable(true);
			rectRightLabel.getStyleClass().addAll("timegraph-handle", "text"); //NOI18N

			//Init text label
			rectLabel = new Label();
			setLabelForRect(rect, rectLabel);
			rectLabel.setAlignment(Pos.CENTER);
			rectLabel.getStyleClass().addAll("timegraph-segment", "text"); //NOI18N
			rectLabel.textProperty().bind(timeSegment.fullDescriptionProperty());
			rectLabel.visibleProperty().bind(rect.widthProperty().greaterThan(0));

			//Add handlers for resize handles
			rectLeft.setOnMousePressed(mousePressHandler);
			rectLeft.setOnMouseReleased(mouseReleaseHandler);
			rectRight.setOnMousePressed(mousePressHandler);
			rectRight.setOnMouseReleased(mouseReleaseHandler);

			rectLeft.setOnMouseDragged(new EventHandler<MouseEvent>() {
				private TimeSegmentGraphics owner;

				public EventHandler<MouseEvent> setOwner(TimeSegmentGraphics owner) {
					this.owner = owner;
					return this;
				}

				private Date clipDate(Date newStart) {
					Date oldStart = timeSegment.startProperty().get();
					Date clippedStart = newStart;
					for (TimeSegmentGraphics graphics : visibleTimeSegments) {
						if (graphics == owner)
							continue;
						Date end = graphics.timeSegment.endProperty().get();
						if (!end.after(clippedStart))
							continue;
						if ((end.before(oldStart) || end.equals(oldStart)) && (end.after(newStart) || end.equals(newStart)))
							clippedStart = end;
					}
					return clippedStart;
				}

				@Override
				public void handle(MouseEvent mouseEvent) {
					if (!timeGraphPane.getChildren().contains(owner.rectLeft))
						return;//Skip drag if handle was hidden
					double clickLocation = localClick != null ? localClick.getX() : 0;
					Date newStart = coordinatesToTime(mouseEvent.getSceneX() - clickLocation);
					if (newStart.after(timeSegment.endProperty().get())) {
						log.finer(messages.getString("START_CANNOT_BE_BEFORE_END_SKIPPING_EDIT"));
					} else if (getIntersectionCount(owner, newStart, timeSegment.endProperty().get()) <= getIntersectionCount(owner, timeSegment.startProperty().get(), timeSegment.endProperty().get())) {
						timeSegment.startProperty().setValue(newStart);
					} else {
						Date clippedStart = clipDate(newStart);
						if (!clippedStart.equals(newStart))
							timeSegment.startProperty().setValue(clippedStart);
					}
					mouseDown(mouseEvent);
				}
			}.setOwner(this));
			rectRight.setOnMouseDragged(new EventHandler<MouseEvent>() {
				private TimeSegmentGraphics owner;

				public EventHandler<MouseEvent> setOwner(TimeSegmentGraphics owner) {
					this.owner = owner;
					return this;
				}

				private Date clipDate(Date newEnd) {
					Date oldEnd = timeSegment.endProperty().get();
					Date clippedEnd = newEnd;
					for (TimeSegmentGraphics graphics : visibleTimeSegments) {
						if (graphics == owner)
							continue;
						Date start = graphics.timeSegment.startProperty().get();
						if (!start.before(clippedEnd))
							continue;
						if ((start.after(oldEnd) || start.equals(oldEnd)) && (start.before(newEnd) || start.equals(newEnd)))
							clippedEnd = start;
					}
					return clippedEnd;
				}

				@Override
				public void handle(MouseEvent mouseEvent) {
					if (!timeGraphPane.getChildren().contains(owner.rectRight))
						return;//Skip drag if handle was hidden
					double clickLocation = localClick != null ? (resizeWidth - localClick.getX()) : 0;
					Date newEnd = coordinatesToTime(mouseEvent.getSceneX() + clickLocation);
					if (newEnd.before(timeSegment.startProperty().get())) {
						log.finer(messages.getString("START_CANNOT_BE_BEFORE_END_SKIPPING_EDIT"));
					} else if (getIntersectionCount(owner, timeSegment.startProperty().get(), newEnd) <= getIntersectionCount(owner, timeSegment.startProperty().get(), timeSegment.endProperty().get())) {
						timeSegment.endProperty().setValue(newEnd);
					} else {
						Date clippedEnd = clipDate(newEnd);
						if (!clippedEnd.equals(newEnd))
							timeSegment.endProperty().setValue(clippedEnd);
					}
					mouseDown(mouseEvent);
				}
			}.setOwner(this));

			//Update rectangle width
			rect.widthProperty().addListener(widthLargerThanZeroListener);
			updateGraphics();

			//Add handler for main rectangle
			rect.setOnMouseClicked(selectHandler);
			rectLabel.setOnMouseClicked(selectHandler);

			//Add everything to the graph
			timeGraphPane.getChildren().addAll(rect);
			timeGraphPane.getChildren().addAll(rectLabel);
			BooleanBinding outOfRangeExpression = rectLeft.layoutXProperty().greaterThan(timeGraphPane.layoutXProperty().add(timeGraphPane.widthProperty()))
					.or(rectRight.layoutXProperty().add(rectRight.widthProperty()).lessThan(timeGraphPane.layoutXProperty()));
			outOfRange.bind(outOfRangeExpression);
			outOfRangeListener.changed(outOfRangeExpression, true, outOfRangeExpression.get());

			//Add to visible graphics list
			visibleTimeSegments.add(this);

			//Set selected style
			if (selectedTimeSegments != null)
				selectedProperty.set(selectedTimeSegments.contains(timeSegment));
		}

		private void toFront() {
			if (!initialized)
				return;
			rect.toFront();
			rectLeft.toFront();
			rectRight.toFront();
			rectLabel.toFront();
			rectLeftLabel.toFront();
			rectRightLabel.toFront();
		}

		private void updateGraphics() {
			if (!initialized)
				return;
			long start = timeSegment.startProperty().get().getTime();
			long end = timeSegment.endProperty().get().getTime();
			long duration = end - start;
			rect.layoutXProperty().bind(timeToCoordinatesProperty(timeSegment.startProperty().get()).add(resizeWidth));
			rect.widthProperty().bind(scale.multiply(duration).subtract(resizeWidth * 2));
			rectLeftLabel.setText(timeFormat.format(timeSegment.startProperty().get()));
			rectRightLabel.setText(timeFormat.format(timeSegment.endProperty().get()));
			updateTimeSegmentGraphics(this);
			updateTimeSegmentGraphics();
		}

		public void disposeGraphics() {
			if (initialized && !isDragged.get()) {
				selectedProperty.removeListener(selectedListener);
				timeGraphPane.getChildren().removeAll(rectLabel, rectLeftLabel, rectRightLabel, rectLeft, rectRight, rect);
				rect = null;
				rectLeft = null;
				rectRight = null;
				rectLabel = null;
				rectLeftLabel = null;
				rectRightLabel = null;
				initialized = false;
				outOfRange.unbind();
				outOfRange.set(true);
				visibleTimeSegments.remove(this);
			}
		}

		public void dispose() {
			disposeGraphics();
			timeSegment.startProperty().removeListener(updateListener);
			timeSegment.endProperty().removeListener(updateListener);
		}
	}

	private class Tick {

		private Line line;
		private Label label;

		public Tick(Line line, Label label) {
			this.line = line;
			this.label = label;
			timeGraphPane.getChildren().addAll(line, label);
			label.toBack();
			line.toBack();
		}

		public void dispose() {
			timeGraphPane.getChildren().removeAll(line, label);
		}
	}
	private NavigableMap<Long, Tick> ticks = new TreeMap<>();

	/**
	 * Initializes the controller
	 *
	 * @param url initialization URL
	 * @param resourceBundle supplied resources
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		timeGraphPane.setCursor(Cursor.MOVE);
		selectedTimeSegments.addListener(selectedTimeSegmentsListener);

		visibleProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
				if (newValue == oldValue)
					return;
				if (newValue.equals(Boolean.TRUE)) {
					if (timeGraphPane.widthProperty().greaterThan(0).get())
						updateTimescale();
					dataManager.getTimeSegments().addListener(dataManagerListener);
				} else {
					dataManager.getTimeSegments().removeListener(dataManagerListener);
					clearTimeScale();
				}
			}
		});

		updateTicksStep();

		scale.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				if (!newValue.equals(oldValue))
					return;
				updateTicksStep();
			}
		});

		timeGraphPane.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				if (!oldValue.equals(newValue)) {
					updateTicks();
					updateTimeSegmentGraphics();
				}
			}
		});

		timeSegmentGraphicsBinsize.bind(new SimpleDoubleProperty(1).divide(scale).multiply(timeGraphPane.widthProperty()));
		timeSegmentGraphicsBinsize.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
				if (newValue == null || newValue.equals(oldValue))
					return;
				if (visibleProperty.get())
					updateTimescale();//TODO: replace this with a less compute-intensive operation?
			}
		});
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

	public void setSelectedTimeSegments(List<TimeSegmentAdapter> selectedTimeSegments) {
		this.selectedTimeSegments.setAll(selectedTimeSegments);
	}

	/**
	 * Performs a full refresh of the time scale
	 */
	private void updateTimescale() {
		clearTimeScale();
		if (dragAnchor == null) {
			//Graph was not moved - so we can jump to the latest time
			Date latestDate = null;
			for (TimeSegmentAdapter timeSegment : dataManager.getTimeSegments())
				if (latestDate == null || timeSegment.endProperty().get().after(latestDate))
					latestDate = timeSegment.endProperty().get();
			if (latestDate != null)
				layoutPos.bind(timeGraphPane.widthProperty()/*.negate().*/.subtract(scale.multiply(latestDate.getTime())));
		}

		for (TimeSegmentAdapter timeSegment : dataManager.getTimeSegments()) {
			addTimeSegmentGraphics(timeSegment);
		}
		updateTicks();

		updateTimeSegmentGraphics();
	}

	/**
	 * Deletes all items from the time scale
	 */
	private void clearTimeScale() {
		synchronized (timeSegmentGraphicsLocations) {
			timeSegmentGraphicsLocations.clear();
			for (TimeSegmentGraphics graphics : timeSegmentGraphics.values())
				graphics.dispose();
			timeSegmentGraphics.clear();
		}
	}

	/**
	 * Removes a specific TimeSegment from the location bins
	 *
	 * @param timeSegment the TimeSegment to remove
	 */
	private void removeTimeSegmentGraphics(TimeSegmentAdapter timeSegment) {
		TimeSegmentGraphics graphics = timeSegmentGraphics.remove(timeSegment);
		if (graphics != null) {
			synchronized (timeSegmentGraphicsLocations) {
				for (Map.Entry<Long, Set<TimeSegmentGraphics>> entry : timeSegmentGraphicsLocations.entrySet())
					entry.getValue().remove(graphics);
			}
			graphics.dispose();
		}
	}

	/**
	 * Adds a TimeSegmentAdapter for display
	 *
	 * @param timeSegment the TimeSegment to add
	 */
	private void addTimeSegmentGraphics(TimeSegmentAdapter timeSegment) {
		long binSize = timeSegmentGraphicsBinsize.get();
		long startBin = binSize * (timeSegment.startProperty().get().getTime() / binSize);
		long endBin = binSize * (timeSegment.endProperty().get().getTime() / binSize + 1);
		TimeSegmentGraphics graphics = new TimeSegmentGraphics(timeSegment);
		timeSegmentGraphics.put(timeSegment, graphics);
		synchronized (timeSegmentGraphicsLocations) {
			for (long bin = startBin; bin <= endBin; bin += binSize) {
				if (!timeSegmentGraphicsLocations.containsKey(bin))
					timeSegmentGraphicsLocations.put(bin, new HashSet<TimeSegmentGraphics>());
				timeSegmentGraphicsLocations.get(bin).add(graphics);
			}
		}
	}

	/**
	 * Updates the graphics for a TimeSegment in location bins
	 *
	 * @param graphics
	 */
	private void updateTimeSegmentGraphics(TimeSegmentGraphics graphics) {
		long binSize = timeSegmentGraphicsBinsize.get();
		long startBin = binSize * (graphics.timeSegment.startProperty().get().getTime() / binSize);
		long endBin = binSize * (graphics.timeSegment.endProperty().get().getTime() / binSize + 1);
		synchronized (timeSegmentGraphicsLocations) {
			//Remove from non-matching bins
			for (Map.Entry<Long, Set<TimeSegmentGraphics>> entry : timeSegmentGraphicsLocations.entrySet())
				if (entry.getKey() < startBin || entry.getKey() > endBin)
					entry.getValue().remove(graphics);
			//Add to any bins we aren't already in
			for (long bin = startBin; bin <= endBin; bin += binSize) {
				if (!timeSegmentGraphicsLocations.containsKey(bin))
					timeSegmentGraphicsLocations.put(bin, new HashSet<TimeSegmentGraphics>());
				timeSegmentGraphicsLocations.get(bin).add(graphics);
			}
		}
	}

	private void updateTicksStep() {
		long step = minimumStep;
		long stepMultipliers[] = new long[]{1, 2, 5};
		int stepMultiplier = 0;
		while ((step * stepMultipliers[stepMultiplier] * scale.get()) < minTickSpacing && step < Long.MAX_VALUE && step > 0) {
			stepMultiplier++;
			if (stepMultiplier >= (stepMultipliers.length - 1)) {
				stepMultiplier = 0;
				step *= 10;
			}
		}
		if (step < Long.MAX_VALUE && step > 0) {
			ticksStep.set(step * stepMultipliers[stepMultiplier]);
		} else {
			ticksStep.set(0);
			log.severe(MessageFormat.format(messages.getString("STEP_IS_OUT_OF_RANGE"), new Object[]{step}));
		}
	}

	private void updateTimeSegmentGraphics() {
		long startTime = coordinatesToTime(timeGraphPane.getLayoutX()).getTime();
		long endTime = coordinatesToTime(timeGraphPane.getLayoutX() + timeGraphPane.getWidth()).getTime();
		//Initialize new objects
		for (NavigableMap.Entry<Long, Set<TimeSegmentGraphics>> entry : timeSegmentGraphicsLocations.subMap(startTime, true, endTime, true).entrySet())
			for (TimeSegmentGraphics graphics : entry.getValue())
				if (!graphics.initialized)
					graphics.init();
	}

	private void updateTicks() {
		if (ticksStep.get() <= 0)
			return;
		long startTick = ticksStep.get() * (coordinatesToTime(timeGraphPane.getLayoutX()).getTime() / ticksStep.get());
		long endTick = ticksStep.get() * (coordinatesToTime(timeGraphPane.getLayoutX() + timeGraphPane.getWidth()).getTime() / ticksStep.get() + 1);
		Map<Long, Tick> newTicks = new HashMap<>();
		//Generate new ticks
		for (long currentTick = startTick; currentTick <= endTick; currentTick += ticksStep.get()) {
			if (ticks.containsKey(currentTick)) {
				newTicks.put(currentTick, ticks.get(currentTick));
				ticks.remove(currentTick);
				continue;
			}
			Line line = new Line();
			line.endYProperty().bind(timeGraphPane.heightProperty());
			line.layoutXProperty().bind(timeToCoordinatesProperty(new Date(currentTick)));
			line.getStyleClass().add("timegraph-tick"); //NOI18N

			Label label = new Label(dateFormat.format(new Date(currentTick)));
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

	private int getIntersectionCount(TimeSegmentGraphics segmentGraphics, Date segmentStartTime, Date segmentEndTime) {
		//Check the current intersections count
		int currentIntersectionsCount = 0;
		for (TimeSegmentGraphics graphics : visibleTimeSegments)
			if (graphics != segmentGraphics) {
				Date start = graphics.timeSegment.startProperty().get();
				Date end = graphics.timeSegment.endProperty().get();
				if (segmentStartTime.before(start) && segmentEndTime.after(start))
					currentIntersectionsCount++;
				else if (segmentStartTime.before(end) && segmentEndTime.after(end))
					currentIntersectionsCount++;
			}
		return currentIntersectionsCount;
	}

	private DoubleBinding timeToCoordinatesProperty(Date time) {
		return timeGraphPane.layoutXProperty().add(layoutPos).add(scale.multiply(time.getTime()));
	}

	private double timeToCoordinates(Date time) {
		return timeGraphPane.layoutXProperty().get() + layoutPos.get() + scale.get() * (time.getTime());
	}

	private Date coordinatesToTime(Double coordinates) {
		return new Date((long) ((coordinates - layoutPos.get()) / scale.get()));
	}

	@FXML
	private void mouseDown(MouseEvent event) {
		dragAnchor = new Point2D(event.getSceneX(), event.getSceneY());
		event.consume();
	}

	@FXML
	private void mouseUp(MouseEvent event) {
		dragAction = DragAction.MOVE;
	}

	@FXML
	private void mouseDragged(MouseEvent event) {
		if (dragAction == DragAction.MOVE) {
			double deltaX = event.getX() - dragAnchor.getX();
			if (layoutPos.isBound())
				layoutPos.unbind();
			layoutPos.set(layoutPos.get() + deltaX);
			mouseDown(event);
			updateTicks();
			updateTimeSegmentGraphics();
		}
	}
}
