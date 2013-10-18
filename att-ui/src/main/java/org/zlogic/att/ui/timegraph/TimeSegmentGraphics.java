/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.att.ui.timegraph;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import org.zlogic.att.ui.TaskEditorController;
import org.zlogic.att.ui.adapters.TimeSegmentAdapter;

/**
 * Graphics object containing a time segment
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public class TimeSegmentGraphics {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(TaskEditorController.class.getName());
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	/**
	 * Time format for resize box labels
	 */
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat(messages.getString("TIMEGRAPH_TIME_FORMAT"));
	/**
	 * Mouse events handler
	 */
	private MouseHandler mouseHandler;
	private TimeSegmentGraphicsManager graphicsManager;
	/**
	 * Width of resize box in pixels
	 */
	private static final int resizeWidth = 10;
	/**
	 * The central rectangle
	 */
	private Rectangle rect;
	/**
	 * The left (start time) resize handle
	 */
	private Rectangle rectLeft;
	/**
	 * The right (start time) resize handle
	 */
	private Rectangle rectRight;
	/**
	 * The central rectangle label
	 */
	private Label rectLabel;
	/**
	 * The left (start time) resize handle label
	 */
	private Label rectLeftLabel;
	/**
	 * The right (start time) resize handle label
	 */
	private Label rectRightLabel;
	/**
	 * The associated time segment
	 */
	private TimeSegmentAdapter timeSegment;
	/**
	 * Local click location
	 */
	private Point2D localClick;
	/**
	 * Mouse press listener which sets the value of isResizing and prepares
	 * other resize-related variables
	 */
	private EventHandler<MouseEvent> mousePressHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent mouseEvent) {
			localClick = new Point2D(mouseEvent.getX(), mouseEvent.getY());
			mouseHandler.setDragAction(MouseHandler.DragAction.RESIZE);
			isResizing.set(true);
		}
	};
	/**
	 * Mouse press listener which sets the value of isResizing
	 */
	private EventHandler<MouseEvent> mouseReleaseHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent mouseEvent) {
			isResizing.set(false);
		}
	};
	/**
	 * Property indicating if this graphics object is selected
	 */
	BooleanProperty selectedProperty = new SimpleBooleanProperty(false);
	/**
	 * True if all graphics components are initialized
	 */
	boolean initialized = false;
	/**
	 * Property indicating if the graphics are out of range
	 */
	private BooleanProperty outOfRange = new SimpleBooleanProperty(true);
	/**
	 * Property indicating if one of the resize handles is dragged
	 */
	private BooleanProperty isResizing = new SimpleBooleanProperty(false);
	/**
	 * Listener which listens to updates from out-of-screen graphics and shows
	 * the graphics object if necessary
	 */
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
				graphicsManager.updateTimeSegmentGraphics(graphics);
				graphicsManager.updateTimeSegmentGraphics();
			}
			if (initialized)
				updateGraphics(true);
		}
	}.setGraphics(this);
	/**
	 * Listener which disposes graphics in case the graphics get out of visible
	 * range
	 */
	private ChangeListener<Boolean> outOfRangeListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
			if (newValue)
				disposeGraphics();
		}
	};
	/**
	 * Listens to the width property and shows/hides resize handles if width is
	 * larger/not larger than zero.
	 */
	private ChangeListener<Number> widthLargerThanZeroListener = new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
			if (newValue == oldValue || newValue == null)
				return;
			if ((newValue instanceof Double && (Double) newValue > 0) || newValue.floatValue() > 0) {
				graphicsManager.addGraphicsChildren(rectLeft);
				graphicsManager.addGraphicsChildren(rectLeftLabel);
				graphicsManager.addGraphicsChildren(rectRight);
				graphicsManager.addGraphicsChildren(rectRightLabel);
			} else {
				graphicsManager.removeGraphicsChildren(rectLeft, rectRight, rectLeftLabel, rectRightLabel);
			}
		}
	};
	/**
	 * Listener which sets the selected/deselected style
	 */
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
	/**
	 * Handler of mouse click selection
	 */
	private EventHandler<MouseEvent> selectHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent t) {
			graphicsManager.setSelectedSegments(timeSegment);
		}
	};

	/**
	 * Creates the graphics for a TimeSegmentAdapter
	 *
	 * @param timeSegment the TimeSegmentAdapter
	 */
	public TimeSegmentGraphics(TimeSegmentAdapter timeSegment, TimeSegmentGraphicsManager graphicsManager, MouseHandler mouseHandler) {
		this.timeSegment = timeSegment;
		this.graphicsManager = graphicsManager;
		this.mouseHandler = mouseHandler;
		outOfRange.addListener(outOfRangeListener);
		timeSegment.startProperty().addListener(updateListener);
		timeSegment.endProperty().addListener(updateListener);
	}

	/**
	 * Returns the current start date of the time segment
	 *
	 * @return the current start date of the time segment
	 */
	public Date getStartDate() {
		return timeSegment.startProperty().get();
	}

	/**
	 * Returns the current end date of the time segment
	 *
	 * @return the current end date of the time segment
	 */
	public Date getEndDate() {
		return timeSegment.endProperty().get();
	}

	/**
	 * Sets the properties for a resize handle rectangle
	 *
	 * @param rectHandle the resize handle rectangle
	 */
	private void setResizeHandleRectProperties(Rectangle rectHandle) {
		rectHandle.setCursor(Cursor.W_RESIZE);
		rectHandle.setWidth(resizeWidth);
		rectHandle.heightProperty().bind(rect.heightProperty());
		rectHandle.layoutYProperty().bind(rect.layoutYProperty());
		rectHandle.getStyleClass().add("timegraph-handle"); //NOI18N
		rectHandle.disableProperty().bind(selectedProperty.not());
	}

	/**
	 * Sets the properties (such as width, height) of a label inside a rectangle
	 *
	 * @param rectangle the rectangle
	 * @param label the label for a rectangle
	 */
	private void setLabelForRect(Rectangle rectangle, Label label) {
		label.layoutXProperty().bind(rectangle.layoutXProperty());
		label.layoutYProperty().bind(rectangle.layoutYProperty());
		label.maxHeightProperty().bind(rectangle.heightProperty());
		label.prefHeightProperty().bind(rectangle.heightProperty());
		label.maxWidthProperty().bind(rectangle.widthProperty());
		label.prefWidthProperty().bind(rectangle.widthProperty());
		label.setLabelFor(rectangle);
	}

	/**
	 * Sets the properties (such as width, height) of a vertical label inside a
	 * rectangle
	 *
	 * @param rectangle the rectangle
	 * @param label the label for a rectangle
	 */
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

	/**
	 * Initializes graphics objects to prepare the time segment for rendering
	 */
	public void init() {
		if (initialized)
			return;
		initialized = true;
		selectedProperty.set(false);
		selectedProperty.addListener(selectedListener);
		//Init main rectangle
		rect = new Rectangle();
		rect.setHeight(100);
		rect.layoutYProperty().bind(graphicsManager.graphicsHeightProperty().subtract(rect.heightProperty()).divide(2));
		rect.getStyleClass().add("timegraph-segment"); //NOI18N
		rect.setCursor(Cursor.DEFAULT);
		//Init left resize rectangle
		rectLeft = new Rectangle();
		rectLeft.layoutXProperty().bind(rect.layoutXProperty().subtract(resizeWidth));
		setResizeHandleRectProperties(rectLeft);
		rectLeftLabel = new Label();
		rectLeftLabel.setRotate(270);
		setLabelForRectVertical(rectLeft, rectLeftLabel);
		rectLeftLabel.setAlignment(Pos.CENTER);
		rectLeftLabel.setDisable(true);
		rectLeftLabel.getStyleClass().addAll("timegraph-handle", "text"); //NOI18N
		//Init right resize rectangle
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
		//Add handler for dragging of left rectangle
		rectLeft.setOnMouseDragged(new EventHandler<MouseEvent>() {
			private TimeSegmentGraphics owner;

			public EventHandler<MouseEvent> setOwner(TimeSegmentGraphics owner) {
				this.owner = owner;
				return this;
			}

			@Override
			public void handle(MouseEvent mouseEvent) {
				if (!graphicsManager.containsGraphicsChild(owner.rectLeft))
					return; //Skip drag if handle was hidden
				double clickLocation = localClick != null ? localClick.getX() : 0;
				Date newStart = graphicsManager.coordinatesToTime(mouseEvent.getSceneX() - clickLocation);
				if (newStart.after(timeSegment.endProperty().get())) {
					log.finer(messages.getString("START_CANNOT_BE_BEFORE_END_SKIPPING_EDIT"));
				} else if (graphicsManager.getIntersectionCount(owner, newStart, timeSegment.endProperty().get()) <= graphicsManager.getIntersectionCount(owner, timeSegment.startProperty().get(), timeSegment.endProperty().get())) {
					timeSegment.startProperty().setValue(newStart);
				} else {
					Date clippedStart = graphicsManager.clipStart(owner, newStart);
					if (!clippedStart.equals(newStart))
						timeSegment.startProperty().setValue(clippedStart);
				}
				//Handle update of mouse anchor
				mouseHandler.mouseDown(mouseEvent);
			}
		}.setOwner(this));
		//Add handler for dragging of right rectangle
		rectRight.setOnMouseDragged(new EventHandler<MouseEvent>() {
			private TimeSegmentGraphics owner;

			public EventHandler<MouseEvent> setOwner(TimeSegmentGraphics owner) {
				this.owner = owner;
				return this;
			}

			@Override
			public void handle(MouseEvent mouseEvent) {
				if (!graphicsManager.containsGraphicsChild(owner.rectRight))
					return; //Skip drag if handle was hidden
				double clickLocation = localClick != null ? (resizeWidth - localClick.getX()) : 0;
				Date newEnd = graphicsManager.coordinatesToTime(mouseEvent.getSceneX() + clickLocation);
				if (newEnd.before(timeSegment.startProperty().get())) {
					log.finer(messages.getString("START_CANNOT_BE_BEFORE_END_SKIPPING_EDIT"));
				} else if (graphicsManager.getIntersectionCount(owner, timeSegment.startProperty().get(), newEnd) <= graphicsManager.getIntersectionCount(owner, timeSegment.startProperty().get(), timeSegment.endProperty().get())) {
					timeSegment.endProperty().setValue(newEnd);
				} else {
					Date clippedEnd = graphicsManager.clipEnd(owner, newEnd);
					if (!clippedEnd.equals(newEnd))
						timeSegment.endProperty().setValue(clippedEnd);
				}
				//Handle update of mouse anchor
				mouseHandler.mouseDown(mouseEvent);
			}
		}.setOwner(this));
		//Update rectangle width
		rect.widthProperty().addListener(widthLargerThanZeroListener);
		updateGraphics(false);
		//Add handler for main rectangle
		rect.setOnMouseClicked(selectHandler);
		rectLabel.setOnMouseClicked(selectHandler);
		//Add everything to the graph
		graphicsManager.addGraphicsChildren(rect);
		graphicsManager.addGraphicsChildren(rectLabel);
		BooleanBinding outOfRangeExpression = rectLeft.layoutXProperty().greaterThan(graphicsManager.graphicsLayoutXProperty().add(graphicsManager.graphicsWidthProperty())).or(rectRight.layoutXProperty().add(rectRight.widthProperty()).lessThan(graphicsManager.graphicsLayoutXProperty()));
		outOfRange.bind(outOfRangeExpression);
		outOfRangeListener.changed(outOfRangeExpression, true, outOfRangeExpression.get());
		//Add to visible graphics list
		graphicsManager.addVisibleSegment(this);
		//Set selected style
		selectedProperty.set(graphicsManager.isSegmentSelected(timeSegment));
	}

	/**
	 * Brings graphics objects to front in correct order
	 */
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

	/**
	 * Updates the start/end properties of graphics
	 *
	 * @param initIfNecessary true if TimeSegmentGraphics should be initialized
	 */
	private void updateGraphics(boolean initIfNecessary) {
		if (!initialized)
			return;
		long start = timeSegment.startProperty().get().getTime();
		long end = timeSegment.endProperty().get().getTime();
		long duration = end - start;
		rect.layoutXProperty().bind(graphicsManager.timeToCoordinatesProperty(timeSegment.startProperty().get()).add(resizeWidth));
		rect.widthProperty().bind(graphicsManager.scaleProperty().multiply(duration).subtract(resizeWidth * 2));
		rectLeftLabel.setText(timeFormat.format(timeSegment.startProperty().get()));
		rectRightLabel.setText(timeFormat.format(timeSegment.endProperty().get()));
		graphicsManager.updateTimeSegmentGraphics(this);
		if (initIfNecessary)
			graphicsManager.updateTimeSegmentGraphics();
	}

	/**
	 * Disposes all graphics objects, e.g. when this segment moves offscreen
	 */
	public void disposeGraphics() {
		if (initialized && !isResizing.get()) {
			selectedProperty.removeListener(selectedListener);
			graphicsManager.removeGraphicsChildren(rectLabel, rectLeftLabel, rectRightLabel, rectLeft, rectRight, rect);
			rect = null;
			rectLeft = null;
			rectRight = null;
			rectLabel = null;
			rectLeftLabel = null;
			rectRightLabel = null;
			initialized = false;
			outOfRange.unbind();
			outOfRange.set(true);
			graphicsManager.removeVisibleSegment(this);
		}
	}

	/**
	 * Destroys all graphics, stops listening for changes. Should be called
	 * before removal.
	 */
	public void dispose() {
		disposeGraphics();
		timeSegment.startProperty().removeListener(updateListener);
		timeSegment.endProperty().removeListener(updateListener);
	}
}
