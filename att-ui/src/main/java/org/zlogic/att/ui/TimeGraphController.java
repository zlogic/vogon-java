/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.att.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import org.zlogic.att.ui.adapters.DataManager;
import org.zlogic.att.ui.adapters.TimeSegmentAdapter;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Logger;

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

	private Point2D dragAnchor;

	private enum DragAction{
		MOVE,RESIZE
	};

	private DragAction dragAction;

	private DoubleProperty scale = new SimpleDoubleProperty(0.00005);

	private DoubleProperty layoutPos = new SimpleDoubleProperty(0);

	int resizeWidth = 10;

	private ObservableMap<TimeSegmentAdapter,TimeSegmentGraphics> timeSegmentGraphics = FXCollections.observableHashMap();

	private Date latestDate;

	private BooleanProperty visibleProperty = new SimpleBooleanProperty(false);

	private ListChangeListener<TimeSegmentAdapter> dataManagerListener = new ListChangeListener<TimeSegmentAdapter>() {
		@Override
		public void onChanged(Change<? extends TimeSegmentAdapter> change) {
			synchronized(timeSegmentGraphics){
				while(change.next()){
					if(change.wasRemoved())
						for(TimeSegmentAdapter timeSegment : change.getRemoved()){
							TimeSegmentGraphics removeGraphics  = timeSegmentGraphics.get(timeSegment);
							if(removeGraphics!=null){
								removeGraphics.dispose();
								timeSegmentGraphics.remove(timeSegment);
							}
						}
					if(change.wasAdded()){
						for(TimeSegmentAdapter timeSegment :change.getAddedSubList()){
							TimeSegmentGraphics existingGraphics  = timeSegmentGraphics.get(timeSegment);
							if(existingGraphics!=null){
								existingGraphics.dispose();
								timeSegmentGraphics.remove(timeSegment);
							}
							timeSegmentGraphics.put(timeSegment,new TimeSegmentGraphics(timeSegment));
						}
					}
				}
			}
		}
	};

	private class TimeSegmentGraphics{
		private Rectangle rect, rectLeft,rectRight;
		private Label label;
		private TimeSegmentAdapter timeSegment;
		private EventHandler<MouseEvent> mousePressHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				dragAction = DragAction.RESIZE;
			}
		};
		private BooleanProperty selectedProperty = new SimpleBooleanProperty(true);//TODO: bind to the currently selected item

		private ChangeListener<Date> updateListener = new ChangeListener<Date>(){
			@Override
			public void changed(ObservableValue<? extends Date> observableValue, Date oldValue, Date newValue) {
				updateGraphics();
			}
		};

		public TimeSegmentGraphics(TimeSegmentAdapter timeSegment){
			this.timeSegment = timeSegment;
			init();
		}

		private void setResizeHandleRectProperties(Rectangle rectHandle){
			rectHandle.setCursor(Cursor.W_RESIZE);
			rectHandle.setWidth(resizeWidth);
			rectHandle.heightProperty().bind(rect.heightProperty());
			rectHandle.layoutYProperty().bind(rect.layoutYProperty());
			rectHandle.getStyleClass().add("timegraph-handle"); //NOI18N
			rectHandle.disableProperty().bind(selectedProperty.not());
		}

		private void init(){
			//Bring selected item to front
			selectedProperty.addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
					if(newValue)
						toFront();
				}
			});

			//Init main rectangle
			rect = new Rectangle();
			rect.setHeight(100);
			rect.layoutYProperty().bind(timeGraphPane.heightProperty().subtract(rect.heightProperty()).divide(2));
			rect.getStyleClass().add("timegraph-segment"); //NOI18N
			rect.setCursor(Cursor.DEFAULT);
			timeSegment.startProperty().addListener(updateListener);
			timeSegment.endProperty().addListener(updateListener);
			updateGraphics();

			//Init resize rectangles
			rectLeft = new Rectangle();
			rectLeft.layoutXProperty().bind(rect.layoutXProperty().subtract(resizeWidth));
			setResizeHandleRectProperties(rectLeft);

			rectRight = new Rectangle();
			rectRight.layoutXProperty().bind(rect.layoutXProperty().add(rect.widthProperty()));
			setResizeHandleRectProperties(rectRight);

			//Init text label
			label = new Label();
			label.setAlignment(Pos.CENTER);
			label.layoutXProperty().bind(rect.layoutXProperty());
			label.layoutYProperty().bind(rect.layoutYProperty());
			label.maxHeightProperty().bind(rect.heightProperty());
			label.prefHeightProperty().bind(rect.heightProperty());
			label.maxWidthProperty().bind(rect.widthProperty());
			label.prefWidthProperty().bind(rect.widthProperty());
			label.setLabelFor(rect);
			label.getStyleClass().addAll("timegraph-segment", "text"); //NOI18N
			label.textProperty().bind(timeSegment.fullDescriptionProperty());

			//Add handlers for resize handles
			rectLeft.setOnMousePressed(mousePressHandler);
			rectRight.setOnMousePressed(mousePressHandler);

			rectLeft.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent) {
					double deltaX = mouseEvent.getSceneX()-dragAnchor.getX();
					double deltaY = mouseEvent.getSceneY()-dragAnchor.getY();
					mouseDown(mouseEvent);
					timeSegment.startProperty().setValue(new Date(timeSegment.startProperty().get().getTime()+(long)(deltaX/scale.get())));//TODO: use "add time" instead
					mouseEvent.consume();
				}
			});
			rectRight.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent) {
					double deltaX = mouseEvent.getSceneX()-dragAnchor.getX();
					double deltaY = mouseEvent.getSceneY()-dragAnchor.getY();
					mouseDown(mouseEvent);
					timeSegment.endProperty().setValue(new Date(timeSegment.endProperty().get().getTime()+(long)(deltaX/scale.get())));//TODO: use "add time" instead
					mouseEvent.consume();
				}
			});

			//Add everything to the graph
			timeGraphPane.getChildren().addAll(rect);
			timeGraphPane.getChildren().addAll(rectLeft);
			timeGraphPane.getChildren().addAll(rectRight);
			timeGraphPane.getChildren().addAll(label);
		}

		private void toFront(){
			label.toFront();
			rect.toFront();
			rectLeft.toFront();
			rectRight.toFront();
		}

		public void updateGraphics(){
			long start = timeSegment.startProperty().get().getTime();
			long end = timeSegment.endProperty().get().getTime();
			long duration = end - start;
			rect.layoutXProperty().bind(timeGraphPane.layoutXProperty().add(scale.multiply(start-latestDate.getTime())).add(layoutPos).add(resizeWidth));
			rect.widthProperty().bind(scale.multiply(duration).subtract(resizeWidth * 2));
		}

		public void dispose(){
			timeSegment.startProperty().removeListener(updateListener);
			timeSegment.endProperty().removeListener(updateListener);
			timeGraphPane.getChildren().removeAll(label,rectLeft,rectRight,rect);
		}
	}

	/**
	 * Initializes the controller
	 *
	 * @param url initialization URL
	 * @param resourceBundle supplied resources
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		timeGraphPane.setCursor(Cursor.MOVE);
		layoutPos.bind(timeGraphPane.widthProperty());

		visibleProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
				if (newValue == oldValue)
					return;
				if (newValue.equals(Boolean.TRUE)){
					updateTimescale();
					dataManager.getTimeSegments().addListener(dataManagerListener);
				}else{
					dataManager.getTimeSegments().removeListener(dataManagerListener);
					clearTimeScale();
				}
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
	 * @return the visibility property
	 */
	public BooleanProperty visibleProperty(){
		return visibleProperty;
	}

	/**
	 * Updates the latest date (which is displayed by default) by using the latest date from the time segment and the previously extracted date
	 * @param timeSegment the time segment from which the latest date should be extracted
	 */
	private void updateLatestDate(TimeSegmentAdapter timeSegment){
		if(latestDate == null || latestDate.before(timeSegment.endProperty().get()))
			latestDate = timeSegment.endProperty().get();
	}

	/**
	 * Peforms a full refresh of the time scale
	 */
	private void updateTimescale(){
		synchronized(timeSegmentGraphics){
			clearTimeScale();
			for(TimeSegmentAdapter timeSegment : dataManager.getTimeSegments())
				updateLatestDate(timeSegment);

			for(TimeSegmentAdapter timeSegment : dataManager.getTimeSegments()){
				TimeSegmentGraphics graphics = new TimeSegmentGraphics(timeSegment);
				timeSegmentGraphics.put(timeSegment,graphics);
			}
		}
	}

	/**
	 * Deletes all items from the time scale
	 */
	private void clearTimeScale(){
		synchronized (timeSegmentGraphics){
			latestDate = null;
			for(TimeSegmentGraphics graphics : timeSegmentGraphics.values())
				graphics.dispose();
			timeSegmentGraphics.clear();
		}
	}

	@FXML
	private void mouseDown(MouseEvent event){
		dragAnchor = new Point2D(event.getSceneX(),event.getSceneY());
		event.consume();
	}

	@FXML
	private void mouseUp(MouseEvent event){
		dragAction = DragAction.MOVE;
	}

	@FXML
	private void mouseDragged(MouseEvent event){
		if(dragAction == DragAction.MOVE){
			double deltaX = event.getX()-dragAnchor.getX();
			double deltaY = event.getY()-dragAnchor.getY();
			if(layoutPos.isBound())
				layoutPos.unbind();
			layoutPos.set(layoutPos.get()+deltaX);
			mouseDown(event);
		}
	}
}
