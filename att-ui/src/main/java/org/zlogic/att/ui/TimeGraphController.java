/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.att.ui;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
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
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import org.zlogic.att.ui.adapters.DataManager;
import org.zlogic.att.ui.adapters.TimeSegmentAdapter;

import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
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

	private DragAction dragAction=DragAction.MOVE;

	private DoubleProperty scale = new SimpleDoubleProperty(0.00005);

	private LongProperty ticksStep = new SimpleLongProperty(0);

	private DoubleProperty layoutPos = new SimpleDoubleProperty(0);

	private int resizeWidth = 10;

	private double minTickSpacing = 100;

	private long timeSegmentGraphicsBinsize = 200000;//TODO: compute this as a product of scale and width, update timeSegmentGraphicsLocations on change

	private Map<TimeSegmentAdapter,TimeSegmentGraphics> timeSegmentGraphics = new HashMap<>();

	private NavigableMap<Long,Set<TimeSegmentGraphics>> timeSegmentGraphicsLocations = new TreeMap<>();

	private Date latestDate;//TODO: add centralized pixel<->time conversion functions

	private BooleanProperty visibleProperty = new SimpleBooleanProperty(false);

	private SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm:ss\ndd.MM.yyyy");//TODO: move to properties


	private ListChangeListener<TimeSegmentAdapter> dataManagerListener = new ListChangeListener<TimeSegmentAdapter>() {
		@Override
		public void onChanged(Change<? extends TimeSegmentAdapter> change) {
			while(change.next()){
				if(change.wasRemoved())
					for(TimeSegmentAdapter timeSegment : change.getRemoved())
						removeTimeSegmentGraphics(timeSegment);
				if(change.wasAdded()){
					for(TimeSegmentAdapter timeSegment : change.getAddedSubList()){
						removeTimeSegmentGraphics(timeSegment);
						addTimeSegmentGraphics(timeSegment);
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
		private boolean initialized = false;
		private BooleanProperty outOfRange = new SimpleBooleanProperty(true);

		private ChangeListener<Date> updateListener = new ChangeListener<Date>(){
			@Override
			public void changed(ObservableValue<? extends Date> observableValue, Date oldValue, Date newValue) {
				if(newValue.equals(oldValue))
					return;
				updateGraphics();
			}
		};

		private ChangeListener<Boolean> outOfRangeListener =  new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
				if(newValue)
					dispose();
			}
		};


		public TimeSegmentGraphics(TimeSegmentAdapter timeSegment){
			this.timeSegment = timeSegment;
			outOfRange.addListener(outOfRangeListener);
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
			if(initialized)
				return;
			initialized = true;
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
			rectLeft = new Rectangle();//TODO: draw vertical label with start/end time
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
					mouseEvent.consume();//TODO: editing for very short durations
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
			BooleanBinding outOfRangeExpression = rectLeft.layoutXProperty().greaterThan(timeGraphPane.layoutXProperty().add(timeGraphPane.widthProperty()))
					.or(rectRight.layoutXProperty().add(rectRight.widthProperty()).lessThan(timeGraphPane.layoutXProperty()));
			outOfRange.bind(outOfRangeExpression);
			outOfRangeListener.changed(outOfRangeExpression, true,outOfRangeExpression.get());
		}

		private void toFront(){
			if(!initialized)
				return;
			label.toFront();
			rect.toFront();
			rectLeft.toFront();
			rectRight.toFront();
		}

		private void updateGraphics(){
			if(!initialized)
				return;
			long start = timeSegment.startProperty().get().getTime();
			long end = timeSegment.endProperty().get().getTime();
			long duration = end - start;
			rect.layoutXProperty().bind(timeGraphPane.layoutXProperty().add(scale.multiply(start-latestDate.getTime())).add(layoutPos).add(resizeWidth));
			rect.widthProperty().bind(scale.multiply(duration).subtract(resizeWidth * 2));
			updateTimeSegmentGraphics(this);
			updateTimeSegmentGraphics();
		}

		public void dispose(){
			if(initialized){
				timeSegment.startProperty().removeListener(updateListener);
				timeSegment.endProperty().removeListener(updateListener);
				timeGraphPane.getChildren().removeAll(label,rectLeft,rectRight,rect);
				rect = null;
				rectLeft = null;
				rectRight = null;
				rect = null;
				initialized  = false;
				outOfRange.unbind();
				outOfRange.set(true);
			}
		}
	}

	private class Tick{
		private Line line;
		private Label label;
		public Tick(Line line,Label label){
			this.line = line;
			this.label = label;
			timeGraphPane.getChildren().addAll(line,label);
			label.toBack();
			line.toBack();
		}
		public void dispose(){
			timeGraphPane.getChildren().removeAll(line,label);
		}
	}

	private NavigableMap<Long,Tick> ticks = new TreeMap<>();

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

		updateTicksStep();

		scale.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				if(!newValue.equals(oldValue))
					return;
				updateTicksStep();
			}
		});

		timeGraphPane.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				if(!oldValue.equals(newValue)){
					updateTicks();
					updateTimeSegmentGraphics();
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
		clearTimeScale();
		for(TimeSegmentAdapter timeSegment : dataManager.getTimeSegments())
			updateLatestDate(timeSegment);

		for(TimeSegmentAdapter timeSegment : dataManager.getTimeSegments()){
			addTimeSegmentGraphics(timeSegment);
		}
		updateTicks();

		updateTimeSegmentGraphics();
	}

	/**
	 * Deletes all items from the time scale
	 */
	private void clearTimeScale(){
		synchronized (timeSegmentGraphicsLocations){
			latestDate = null;
			timeSegmentGraphicsLocations.clear();
			for(TimeSegmentGraphics graphics : timeSegmentGraphics.values())
				graphics.dispose();
			timeSegmentGraphics.clear();
		}
	}

	private void removeTimeSegmentGraphics(TimeSegmentAdapter timeSegment){
		TimeSegmentGraphics graphics = timeSegmentGraphics.remove(timeSegment);
		if(graphics!=null){
			boolean found = false;
			synchronized (timeSegmentGraphicsLocations){
				for(Map.Entry<Long,Set<TimeSegmentGraphics>> entry : timeSegmentGraphicsLocations.entrySet())
					found|=entry.getValue().remove(graphics);
			}
			graphics.dispose();
		}
	}

	private void addTimeSegmentGraphics(TimeSegmentAdapter timeSegment){
		long startBin = timeSegmentGraphicsBinsize * (timeSegment.startProperty().get().getTime()/timeSegmentGraphicsBinsize);
		long endBin = timeSegmentGraphicsBinsize * (timeSegment.endProperty().get().getTime()/timeSegmentGraphicsBinsize+1);
		TimeSegmentGraphics graphics = new TimeSegmentGraphics(timeSegment);
		timeSegmentGraphics.put(timeSegment,graphics);
		synchronized(timeSegmentGraphicsLocations){
			for(long bin = startBin;bin<=endBin;bin+=timeSegmentGraphicsBinsize){
				if(!timeSegmentGraphicsLocations.containsKey(bin))
					timeSegmentGraphicsLocations.put(bin,new HashSet<TimeSegmentGraphics>());
				timeSegmentGraphicsLocations.get(bin).add(graphics);
			}
		}
	}

	private void updateTimeSegmentGraphics(TimeSegmentGraphics graphics){
		long startBin = timeSegmentGraphicsBinsize * (graphics.timeSegment.startProperty().get().getTime()/timeSegmentGraphicsBinsize);
		long endBin = timeSegmentGraphicsBinsize * (graphics.timeSegment.endProperty().get().getTime()/timeSegmentGraphicsBinsize+1);
		synchronized(timeSegmentGraphicsLocations){
			//Remove from non-matching bins
			for(Map.Entry<Long,Set<TimeSegmentGraphics>> entry : timeSegmentGraphicsLocations.entrySet())
				if(entry.getKey()<startBin || entry.getKey()>endBin)
					entry.getValue().remove(graphics);
			//Add to any bins we aren't already in
			for(long bin = startBin;bin<=endBin;bin+=timeSegmentGraphicsBinsize){
				if(!timeSegmentGraphicsLocations.containsKey(bin))
					timeSegmentGraphicsLocations.put(bin,new HashSet<TimeSegmentGraphics>());
				timeSegmentGraphicsLocations.get(bin).add(graphics);
			}
		}
	}

	private void updateTicksStep(){
		long step = 1000;//1 second is the minimum
		long stepMultipliers[] = new long[]{1,2,5};
		int stepMultiplier = 0;
		while((step*stepMultipliers[stepMultiplier]*scale.get())<minTickSpacing && step<Long.MAX_VALUE && step>0){
			stepMultiplier++;
			if(stepMultiplier>=(stepMultipliers.length-1)){
				stepMultiplier=0;
				step*=10;
			}
		}
		if(step<Long.MAX_VALUE && step>0){
			ticksStep.set(step*stepMultipliers[stepMultiplier]);
		}else{
			ticksStep.set(0);
			log.severe(MessageFormat.format("Step {0} is out of range", new Object[]{step}));
		}
	}

	private void updateTimeSegmentGraphics(){
		long startTime = latestDate.getTime()-(long)((layoutPos.get()-timeGraphPane.getLayoutX())/scale.get());
		long endTime = latestDate.getTime()-(long)((layoutPos.get()-timeGraphPane.getLayoutX()-timeGraphPane.getWidth())/scale.get());
		//Initialize new objects
		for(NavigableMap.Entry<Long,Set<TimeSegmentGraphics>> entry : timeSegmentGraphicsLocations.subMap(startTime,true,endTime,true).entrySet())
			for(TimeSegmentGraphics graphics : entry.getValue())
				if(!graphics.initialized)
					graphics.init();
	}

	private void updateTicks(){
		if(ticksStep.get()<=0)
			return;
		long startTick = ticksStep.get()*((latestDate.getTime()-(long)((layoutPos.get()-timeGraphPane.getLayoutX())/scale.get()))/ticksStep.get());
		long endTick = ticksStep.get()*((latestDate.getTime()-(long)((layoutPos.get()-timeGraphPane.getLayoutX()-timeGraphPane.getWidth())/scale.get()))/ticksStep.get()+1);
		for(long currentTick=startTick;currentTick<=endTick;currentTick+=ticksStep.get()){
			if(ticks.containsKey(currentTick))
				continue;
			Line line = new Line();
			line.startYProperty().set(0);
			line.endYProperty().bind(timeGraphPane.heightProperty());
			line.setStroke(Color.ORANGE);//TODO: use styles
			line.layoutXProperty().bind(timeGraphPane.layoutXProperty().add(scale.multiply(currentTick-latestDate.getTime())).add(layoutPos));

			Label label = new Label(dateFormat.format(new Date(currentTick)));
			label.setLabelFor(line);
			label.setTextAlignment(TextAlignment.CENTER);//TODO: use styles
			label.setTextFill(Color.ORANGE);//TODO: use styles
			label.layoutXProperty().bind(line.layoutXProperty().subtract(label.widthProperty().divide(2)));
			label.layoutYProperty().bind(timeGraphPane.layoutYProperty());

			ticks.put(currentTick,new Tick(line, label));
		}
		while(ticks.firstKey()<startTick){
			ticks.firstEntry().getValue().dispose();
			ticks.remove(ticks.firstKey());
		}
		while(ticks.lastKey()>endTick){
			ticks.lastEntry().getValue().dispose();
			ticks.remove(ticks.lastKey());
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
			updateTicks();
			updateTimeSegmentGraphics();
		}
	}
}
