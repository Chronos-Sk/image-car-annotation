/**
 * 
 */
package car.shared.views3d;

import car.shared.views.Drawable;
import car.shared.views.MovableImageView;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;

/**
 * Class responsible for handling mouse events for a {@link WireFrameView}.
 * Updates the view's rotation allowing the image to be spun around.
 * 
 * The handler updates the x- and y-rotations of the <code>WireFrameView
 * </code> when the user drags the mouse around on it. After each rotation
 * update, the handler also calls {@link Drawable#draw() draw()} on the
 * {@link Drawable}.
 * 
 * @author Joshua Little
 */
public class WireFrameMouseHandler implements MouseDownHandler, MouseUpHandler,
											  MouseMoveHandler {
	
	/**
	 * Default pixel-to-radian factor when dragging.
	 */
	public static final double DEFAULT_DRAGGING_SPEED = 0.01;
	
	private WireFrameView view; // View this handler is responsible for.
	
	// Speed factor for translating mouse-movement to rotation.
	private double speed;

	private Drawable drawer; // What to redraw when the view changes.
	
	private double oldX, oldY; // Old mouse position.
	private boolean dragging; // Is the user currently dragging?

	/**
	 * Creates a new instance of <code>WireFrameMouseHandler</code> with the
	 * specified {@link Drawable}, {@link WireFrameView}, and the default
	 * rotation speed.
	 * 
	 * @param drawer 
	 * @param view
	 */
	public WireFrameMouseHandler(Drawable drawer, WireFrameView view) {
		this(drawer, view, 0.01);
	}

	/**
	 * Creates a new instance of <code>WireFrameMouseHandler</code> with the
	 * specified {@link Drawable}, {@link WireFrameView}, and rotation speed.
	 * 
	 * @param drawer
	 * @param view
	 */
	public WireFrameMouseHandler(Drawable drawer, WireFrameView view, double speed) {
		this.drawer = drawer;
		this.view = view;
		this.speed = speed;
		
		// Add appropriate event-handlers.
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
	 * Updates the {@link WireFrameView}'s translation. Calls 
	 * {@link MovableImageView#translate(double, double) translate} with
	 * the change in mouse position since the last mouse event. Calls
     * {@link Drawable#draw()} afterwards.
	 */
	@Override
	public void onMouseMove(MouseMoveEvent event) {
		if ( dragging ) {
			// Grab the mouse's coordinates relative to the element.
			double newX = event.getRelativeX(view.getElement());
			double newY = event.getRelativeY(view.getElement());
			
			// We're rotating *around* the x- and y-axes. So mouse-x and mouse-y
			// don't map directly to rotate-x and rotate-y respectively.
			view.rotate((newY - oldY) * speed, (newX - oldX) * speed);
			drawer.draw();

			// Update old mouse coordinates for next time.
			oldX = newX;
			oldY = newY;
		}
	}
	
}