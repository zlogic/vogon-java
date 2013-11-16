/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.timegraph;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;

/**
 * Mouse state and action handler
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class MouseHandler {

	/**
	 * Drag action types (chosen depending on the clicked object)
	 */
	public enum DragAction {

		/**
		 * The graph is being scrolled by moving
		 */
		MOVE,
		/**
		 * A component is being resized
		 */
		RESIZE
	};
	/**
	 * Drag start location
	 */
	private Point2D dragAnchor;
	/**
	 * Current drag action
	 */
	private DragAction dragAction = DragAction.MOVE;

	/**
	 * Returns the drag start anchor, or null if drag is not initiated
	 *
	 * @return the drag start anchor, or null if drag is not initiated
	 */
	public Point2D getDragAnchor() {
		return dragAnchor;
	}

	/**
	 * Returns the current drag action
	 *
	 * @return the current drag action
	 */
	public DragAction getDragAction() {
		return dragAction;
	}

	/**
	 * Sets a new drag action (e.g. when another action was detected by another
	 * controller)
	 *
	 * @param dragAction the DragAction to set
	 */
	public void setDragAction(DragAction dragAction) {
		this.dragAction = dragAction;
	}

	/**
	 * Mouse down handler
	 *
	 * @param event the mouse event
	 */
	public void mouseDown(MouseEvent event) {
		dragAnchor = new Point2D(event.getSceneX(), event.getSceneY());
	}

	/**
	 * Mouse up handler
	 *
	 * @param event the mouse event
	 */
	public void mouseUp(MouseEvent event) {
		dragAction = DragAction.MOVE;
	}

	/**
	 * Mouse dragged handler
	 *
	 * @param event the mouse event
	 */
	public void mouseDragged(MouseEvent event) {
		if (dragAction == DragAction.MOVE)
			mouseDown(event);
	}
}
