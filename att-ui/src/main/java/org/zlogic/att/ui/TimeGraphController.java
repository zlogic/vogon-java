/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.att.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.zlogic.att.ui.adapters.DataManager;
import org.zlogic.att.ui.adapters.TaskAdapter;
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

		private ChangeListener<Date> updateListener = new ChangeListener<Date>(){
			@Override
			public void changed(ObservableValue<? extends Date> observableValue, Date oldValue, Date newValue) {
				updateGraphics(timeSegment);
			}
		};

		public TimeSegmentGraphics(TimeSegmentAdapter timeSegment){
			this.timeSegment = timeSegment;
			init();
		}

		private void init(){
			//TODO: use Java FX Styles
			rect = new Rectangle();
			timeSegment.startProperty().addListener(updateListener);
			timeSegment.endProperty().addListener(updateListener);
			updateGraphics(timeSegment);

			rect.setHeight(100);
			rect.layoutYProperty().bind(timeGraphPane.heightProperty().subtract(rect.heightProperty()).divide(2));
			rect.setCursor(Cursor.DEFAULT);
			rect.setStrokeWidth(2);
			rect.setStroke(Color.GAINSBORO);

			rectLeft = new Rectangle();
			rectLeft.setCursor(Cursor.W_RESIZE);
			rectLeft.layoutXProperty().bind(rect.layoutXProperty().subtract(resizeWidth));
			rectLeft.setWidth(resizeWidth);
			rectLeft.heightProperty().bind(rect.heightProperty());
			rectLeft.layoutYProperty().bind(rect.layoutYProperty());

			rectRight = new Rectangle();
			rectRight.setCursor(Cursor.W_RESIZE);
			rectRight.layoutXProperty().bind(rect.layoutXProperty().add(rect.widthProperty()));
			rectRight.setWidth(resizeWidth);
			rectRight.heightProperty().bind(rect.heightProperty());
			rectRight.layoutYProperty().bind(rect.layoutYProperty());

			label = new Label();
			label.setAlignment(Pos.CENTER);
			label.layoutXProperty().bind(rect.layoutXProperty());
			label.layoutYProperty().bind(rect.layoutYProperty());
			label.maxHeightProperty().bind(rect.heightProperty());
			label.prefHeightProperty().bind(rect.heightProperty());
			label.maxWidthProperty().bind(rect.widthProperty());
			label.prefWidthProperty().bind(rect.widthProperty());
			label.setLabelFor(rect);
			label.textProperty().bind(timeSegment.ownerTaskProperty().get().nameProperty().concat("\n").concat(timeSegment.descriptionProperty()).concat("\n").concat(timeSegment.durationProperty()));//TODO: monitor changes, check for NULL

			rectLeft.setOnMousePressed(mousePressHandler);
			rectRight.setOnMousePressed(mousePressHandler);

			rectLeft.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent) {
					double deltaX = mouseEvent.getSceneX()-dragAnchor.getX();
					double deltaY = mouseEvent.getSceneY()-dragAnchor.getY();
					mouseDown(mouseEvent);
					timeSegment.startProperty().setValue(new Date(timeSegment.startProperty().get().getTime()+(long)(deltaX/scale.get())));//TODO: use "add time" instead
				}
			});
			rectRight.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent) {
					double deltaX = mouseEvent.getSceneX()-dragAnchor.getX();
					double deltaY = mouseEvent.getSceneY()-dragAnchor.getY();
					mouseDown(mouseEvent);
					timeSegment.endProperty().setValue(new Date(timeSegment.endProperty().get().getTime()+(long)(deltaX/scale.get())));//TODO: use "add time" instead
				}
			});

			timeGraphPane.getChildren().addAll(rect);
			timeGraphPane.getChildren().addAll(rectLeft);
			timeGraphPane.getChildren().addAll(rectRight);
			timeGraphPane.getChildren().addAll(label);
		}

		public void updateGraphics(TimeSegmentAdapter timeSegment){
			//TODO: check not null
			//TODO: use the timeSegment we currently have?
			this.timeSegment = timeSegment;
			long start = timeSegment.startProperty().get().getTime();
			long end = timeSegment.endProperty().get().getTime();
			long duration = end - start;
			rect.layoutXProperty().bind(timeGraphPane.layoutXProperty().add(scale.multiply(start-latestDate.getTime())).add(layoutPos).add(resizeWidth));
			rect.widthProperty().bind(scale.multiply(duration).subtract(resizeWidth*2));
		}

		public void setFill(Paint paint){
			rect.setFill(paint);
		}

		public void dispose(){
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
				if (newValue.equals(Boolean.TRUE))
					updateTimescale();
				else
					clearTimeScale();
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

	public BooleanProperty visibleProperty(){
		return visibleProperty;
	}

	private void updateLatestDate(TimeSegmentAdapter timeSegment){
		if(latestDate == null || latestDate.before(timeSegment.endProperty().get()))
			latestDate = timeSegment.endProperty().get();
	}

	private void updateTimescale(){
		synchronized(timeSegmentGraphics){
			int colorIndex = 0;
			latestDate = null;
			for(TaskAdapter task : dataManager.getTasks())
				for(TimeSegmentAdapter timeSegment : task.timeSegmentsProperty())
					updateLatestDate(timeSegment);
			for(TaskAdapter task : dataManager.getTasks()){
				for(TimeSegmentAdapter timeSegment : task.timeSegmentsProperty()){
					TimeSegmentGraphics graphics = new TimeSegmentGraphics(timeSegment);
					switch(colorIndex){
						case 0:
							graphics.setFill(Color.ORANGE);
							break;
						case 1:
							graphics.setFill(Color.BLUEVIOLET);
							break;
						case 2:
							graphics.setFill(Color.CORNFLOWERBLUE);
							break;
					}
					colorIndex = colorIndex>=2 ? (0):(colorIndex+1);
					timeSegmentGraphics.put(timeSegment,graphics);
				}
			}
		}
		//TODO: monitor deletions
	}

	private void clearTimeScale(){
		synchronized (timeSegmentGraphics){
			for(TimeSegmentGraphics graphics : timeSegmentGraphics.values())
				graphics.dispose();
			timeSegmentGraphics.clear();
		}
	}

	@FXML
	private void mouseDown(MouseEvent event){
		dragAnchor = new Point2D(event.getSceneX(),event.getSceneY());
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
