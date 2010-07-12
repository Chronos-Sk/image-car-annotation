package car.orientor.client;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;

/**
 * Class responsible for handling mouse events for a {@link MovableImageView}.
 * Updates the view's translation allowing the image to be dragged around.
 * 
 * The handler updates the translation of the <code>MovableImageView</code> when
 * the user drags the mouse around on it. After each translation update, the
 * handler also calls {@link Drawable#draw() draw()} on the {@link Drawable}.
 * 
 * @author Joshua Little
 */
public class MovableImageMouseHandler
			implements MouseDownHandler, MouseUpHandler, MouseMoveHandler {
	
	private MovableImageView view; // View this handler is responsible for.
	
	private Drawable drawer; // What to redraw when the view changes.
	
	private double oldX, oldY; // Old mouse position.
	private boolean dragging; // Is the user currently dragging?

	/**
	 * Creates a new instance of <code>MovableImageMouseHandler</code> with the
	 * specified {@link Drawable} and {@link MovableImageView}.
	 * 
	 * @param drawer the Drawable to call {@link Drawable#draw() draw()} on.
	 * @param view
	 */
	public MovableImageMouseHandler(Drawable drawer, MovableImageView view) {
		this.drawer = drawer;
		this.view = view;
		
		view.addMouseDownHandler(this);
		view.addMouseUpHandler(this);
		view.addMouseMoveHandler(this);
	}
	
	/**
	 * Starts dragging.
	 */
	@Override
	public void onMouseDown(MouseDownEvent event) {
		oldX = event.getRelativeX(view.getElement());
		oldY = event.getRelativeY(view.getElement());
		
		dragging = true;
	}

	/**
	 * Stops dragging.
	 */
	@Override
	public void onMouseUp(MouseUpEvent event) {
		dragging = false;
	}

	/**
	 * Updates the {@link MovableImageView}'s translation. Calls
	 * {@link MovableImageView#translate(double, double)} with the change in
	 * mouse position since the last mouse event. Calls {@link Drawable#draw()}
	 * afterwards.
	 */
	@Override
	public void onMouseMove(MouseMoveEvent event) {
		if ( dragging ) {
			// Grab the mouse's coordinates relative to the element.
			double newX = event.getRelativeX(view.getElement());
			double newY = event.getRelativeY(view.getElement());
			
			// Translate by difference.
			view.translate(oldX - newX, oldY - newY);
			drawer.draw();
			
			// Update old mouse coordinates for next time.
			oldX = newX;
			oldY = newY;
		}
	}

}
