/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.timegraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.zlogic.att.ui.adapters.TimeSegmentAdapter;

/**
 * Time segment graphics owner class
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class TimeSegmentGraphicsManager {

	/**
	 * Mouse events handler
	 */
	private MouseHandler mouseHandler;
	/**
	 * Current scale
	 */
	private DoubleProperty scale = new SimpleDoubleProperty();
	/**
	 * Map for mapping TimeSegmentAdapter to its graphical representation
	 */
	private Map<TimeSegmentAdapter, TimeSegmentGraphics> timeSegmentGraphics = new HashMap<>();
	/**
	 * Size of bins which are used for quick access to time segments
	 */
	private ReadOnlyLongWrapper timeSegmentGraphicsBinsize = new ReadOnlyLongWrapper();
	/**
	 * TimeSegmentGraphics grouped into bins for quick access
	 */
	private final NavigableMap<Long, Set<TimeSegmentGraphics>> timeSegmentGraphicsLocations = new TreeMap<>();
	/**
	 * Currently visible time segments
	 */
	private Set<TimeSegmentGraphics> visibleTimeSegments = new HashSet<>();
	/**
	 * Selected time segments list
	 */
	private ObservableList<TimeSegmentAdapter> selectedTimeSegments = FXCollections.observableList(new LinkedList<TimeSegmentAdapter>());
	/**
	 * The target graphics rendering node
	 */
	private Pane graphicsNode;
	/**
	 * Current scroll position
	 */
	private DoubleProperty layoutPos = new SimpleDoubleProperty(0);
	/**
	 * Property specifying if the graphics are visible
	 */
	private BooleanProperty visibleProperty = new SimpleBooleanProperty(false);
	/**
	 * Listener for selected time segments which enables editing of time
	 * segments
	 */
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

	/**
	 * Creates the TimeSegmentGraphicsManager
	 *
	 * @param mouseHandler the mouse handler
	 * @param scale the scale property
	 * @param graphicsNode the target graphics rendering node
	 * @param layoutPos the current scroll position property
	 * @param visibleProperty the property specifying if the graphics are
	 * visible
	 */
	public TimeSegmentGraphicsManager(MouseHandler mouseHandler, DoubleProperty scale, Pane graphicsNode, ObservableValue<? extends Number> layoutPos, ObservableValue<Boolean> visibleProperty) {
		this.mouseHandler = mouseHandler;
		this.scale.bind(scale);
		this.graphicsNode = graphicsNode;
		this.layoutPos.bind(layoutPos);
		this.visibleProperty.bind(visibleProperty);

		timeSegmentGraphicsBinsize.bind(new SimpleDoubleProperty(1).divide(scale).multiply(graphicsNode.widthProperty()));
		selectedTimeSegments.addListener(selectedTimeSegmentsListener);
	}

	/**
	 * Deletes all items from the time scale
	 */
	public void clearTimeScale() {
		synchronized (this) {
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
	public void removeTimeSegmentGraphics(TimeSegmentAdapter timeSegment) {
		TimeSegmentGraphics graphics = timeSegmentGraphics.remove(timeSegment);
		if (graphics != null) {
			synchronized (this) {
				for (Map.Entry<Long, Set<TimeSegmentGraphics>> entry : timeSegmentGraphicsLocations.entrySet())
					entry.getValue().remove(graphics);
			}
			graphics.dispose();
		}
	}

	/**
	 * Adds a TimeSegmentAdapter for display and into location bins
	 *
	 * @param timeSegment the TimeSegment to add
	 */
	public void addTimeSegmentGraphics(TimeSegmentAdapter timeSegment) {
		long binSize = timeSegmentGraphicsBinsize.get();
		long startBin = binSize * (timeSegment.startProperty().get().getTime() / binSize);
		long endBin = binSize * (timeSegment.endProperty().get().getTime() / binSize + 1);
		TimeSegmentGraphics graphics = new TimeSegmentGraphics(timeSegment, this, mouseHandler);
		timeSegmentGraphics.put(timeSegment, graphics);
		synchronized (this) {
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
	public void updateTimeSegmentGraphics(TimeSegmentGraphics graphics) {
		long binSize = timeSegmentGraphicsBinsize.get();
		long startBin = binSize * (graphics.getStartDate().getTime() / binSize);
		long endBin = binSize * (graphics.getEndDate().getTime() / binSize + 1);
		synchronized (this) {
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

	/**
	 * Initializes visible TimeSegmentGraphics objects
	 */
	public void updateTimeSegmentGraphics() {
		long startTime = coordinatesToTime(graphicsNode.getLayoutX()).getTime();
		long endTime = coordinatesToTime(graphicsNode.getLayoutX() + graphicsNode.getWidth()).getTime();
		//Initialize new objects
		for (NavigableMap.Entry<Long, Set<TimeSegmentGraphics>> entry : timeSegmentGraphicsLocations.subMap(startTime, true, endTime, true).entrySet())
			for (TimeSegmentGraphics graphics : entry.getValue())
				if (!graphics.initialized)
					graphics.init();
	}

	/**
	 * Returns the number of times a segment intersects other visible segments
	 *
	 * @param segmentGraphics the TimeSegmentGraphics (used to exclude itself
	 * from comparison)
	 * @param segmentStartTime the start time of a segment
	 * @param segmentEndTime the end time of a segment
	 * @return the number of intersections
	 */
	public int getIntersectionCount(TimeSegmentGraphics segmentGraphics, Date segmentStartTime, Date segmentEndTime) {
		//Check the current intersections count
		int currentIntersectionsCount = 0;
		synchronized (this) {
			//TODO: use bins instead of visible segments
			for (TimeSegmentGraphics graphics : visibleTimeSegments)
				if (graphics != segmentGraphics) {
					Date start = graphics.getStartDate();
					Date end = graphics.getEndDate();
					if (segmentStartTime.before(start) && segmentEndTime.after(start))
						currentIntersectionsCount++;
					else if (segmentStartTime.before(end) && segmentEndTime.after(end))
						currentIntersectionsCount++;
				}
		}
		return currentIntersectionsCount;
	}

	/**
	 * Returns the time mapped to local coordinates as a property (automatically
	 * updated on scrolling)
	 *
	 * @param time the time to map
	 * @return the local coordinates property
	 */
	public DoubleBinding timeToCoordinatesProperty(Date time) {
		return graphicsNode.layoutXProperty().add(layoutPos).add(scale.multiply(time.getTime()));
	}

	/**
	 * Returns the time mapped to local coordinates
	 *
	 * @param time the time to map
	 * @return the local coordinates
	 */
	public double timeToCoordinates(Date time) {
		return graphicsNode.layoutXProperty().get() + layoutPos.get() + scale.get() * (time.getTime());
	}

	/**
	 * Returns the local coordinates mapped to time
	 *
	 * @param coordinates the coordinates to map
	 * @return the time
	 */
	public Date coordinatesToTime(Double coordinates) {
		return new Date((long) ((coordinates - layoutPos.get()) / scale.get()));
	}

	/**
	 * Clips the date in case the new start causes the time segment to intersect
	 * with a new time segment
	 *
	 * @param owner the TimeSegmentGraphics which is being clipped
	 * @param newStart the start time to be clipped
	 * @return the clipped start time
	 */
	protected Date clipStart(TimeSegmentGraphics owner, Date newStart) {
		Date oldStart = owner.getStartDate();
		Date clippedStart = newStart;
		synchronized (this) {
			for (TimeSegmentGraphics graphics : visibleTimeSegments) {
				if (graphics == owner)
					continue;
				Date end = graphics.getEndDate();
				if (!end.after(clippedStart))
					continue;
				if ((end.before(oldStart) || end.equals(oldStart)) && (end.after(newStart) || end.equals(newStart)))
					clippedStart = end;
			}
		}
		return clippedStart;
	}

	/**
	 * Clips the date in case the new end causes the time segment to intersect
	 * with a new time segment
	 *
	 * @param owner the TimeSegmentGraphics which is being clipped
	 * @param newEnd the end time to be clipped
	 * @return the clipped end time
	 */
	protected Date clipEnd(TimeSegmentGraphics owner, Date newEnd) {
		Date oldEnd = owner.getEndDate();
		Date clippedEnd = newEnd;
		synchronized (this) {
			for (TimeSegmentGraphics graphics : visibleTimeSegments) {
				if (graphics == owner)
					continue;
				Date start = graphics.getStartDate();
				if (!start.before(clippedEnd))
					continue;
				if ((start.after(oldEnd) || start.equals(oldEnd)) && (start.before(newEnd) || start.equals(newEnd)))
					clippedEnd = start;
			}
		}
		return clippedEnd;
	}

	/**
	 * Adds a TimeSegmentGraphics to the list of visible segments
	 *
	 * @param graphics the TimeSegmentGraphics to add
	 */
	protected void addVisibleSegment(TimeSegmentGraphics graphics) {
		synchronized (this) {
			visibleTimeSegments.add(graphics);
		}
	}

	/**
	 * Removes a TimeSegmentGraphics to the list of visible segments
	 *
	 * @param graphics the TimeSegmentGraphics to add
	 */
	protected void removeVisibleSegment(TimeSegmentGraphics graphics) {
		synchronized (this) {
			visibleTimeSegments.remove(graphics);
		}
	}

	/**
	 * Sets the currently selected TimeSegmentAdapters
	 *
	 * @param timeSegments the selected TimeSegmentAdapters
	 */
	public void setSelectedSegments(TimeSegmentAdapter... timeSegments) {
		synchronized (this) {
			selectedTimeSegments.setAll(timeSegments);
		}
	}

	/**
	 * Returns true if the time segment is selected
	 *
	 * @param timeSegment the TimeSegmentAdapter to check
	 * @return true if the time segment is selected
	 */
	protected boolean isSegmentSelected(TimeSegmentAdapter timeSegment) {
		synchronized (this) {
			return selectedTimeSegments.contains(timeSegment);
		}
	}

	/**
	 * Removes nodes from the target graphics rendering node's children. Doesn't
	 * remove already removed nodes.
	 *
	 * @param nodes the nodes to remove
	 */
	protected void removeGraphicsChildren(Node... nodes) {
		synchronized (this) {
			graphicsNode.getChildren().removeAll(nodes);
		}
	}

	/**
	 * Adds nodes to the target graphics rendering node's children. Doesn't add
	 * already added nodes.
	 *
	 * @param nodes the nodes to add
	 */
	protected void addGraphicsChildren(Node... nodes) {
		synchronized (this) {
			List<Node> filteredList = new ArrayList(Arrays.asList(nodes));
			filteredList.removeAll(graphicsNode.getChildren());
			graphicsNode.getChildren().addAll(filteredList);
		}
	}

	/**
	 * Returns true if the node exists in the target graphics rendering node's
	 * children list.
	 *
	 * @param node the node to check
	 * @return true if the node exists in the target graphics rendering node's
	 * children list
	 */
	protected boolean containsGraphicsChild(Node node) {
		return graphicsNode.getChildren().contains(node);
	}

	/**
	 * Returns the width property of the target graphics rendering node
	 *
	 * @return the width property of the target graphics rendering node
	 */
	protected ReadOnlyDoubleProperty graphicsWidthProperty() {
		return graphicsNode.widthProperty();
	}

	/**
	 * Returns the height property of the target graphics rendering node
	 *
	 * @return the height property of the target graphics rendering node
	 */
	protected ReadOnlyDoubleProperty graphicsHeightProperty() {
		return graphicsNode.heightProperty();
	}

	/**
	 * Returns the Layout X property of the target graphics rendering node
	 *
	 * @return the Layout X property of the target graphics rendering node
	 */
	protected ReadOnlyDoubleProperty graphicsLayoutXProperty() {
		return graphicsNode.layoutXProperty();
	}

	/**
	 * Returns the scale property
	 *
	 * @return the scale property
	 */
	protected DoubleProperty scaleProperty() {
		return scale;
	}

	/**
	 * Returns the size of bins which are used for quick access to time segments
	 *
	 * @return the size of bins which are used for quick access to time segments
	 */
	public ReadOnlyLongProperty timeSegmentGraphicsBinsizeProperty() {
		return timeSegmentGraphicsBinsize.getReadOnlyProperty();
	}
}
