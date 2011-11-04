package car.picker.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.Surface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import car.shared.math.Point2D;
import car.shared.views.Drawable;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Image;

/**
 * Handles drawing and user-interaction for a set of {@link CarPoint}s.
 * 
 * Note: Car sizes are no longer used, as we have decided just to use points
 *       to denote cars, rather than rectangles.
 * 
 * @author Joshua Little
 */
public class CarPointHandler implements MouseDownHandler, MouseUpHandler,
										MouseMoveHandler, KeyUpHandler,
										HasSelectionHandlers<CarPoint>,
										Drawable {
	
	private List<CarPoint> carPoints = null; // List of current CarPoints.
	private Surface canvas = null; // The canvas to draw everything on.
	private Image background = null; // The background image.
	
	// Manages registration and event-firing for registered handles.
	private HandlerManager handlerManager;
	
	// List of registrations for the handlers that *this* object creates.
	private List<HandlerRegistration> handlerRegs = null;
	
	// Colors to draw CarPoints in:
	
	// Default state
	private Color regularFill   = CarPoint.DEFAULT_FILL;
	private Color regularStroke = CarPoint.DEFAULT_STROKE;
	
	//Selected, not dragging.
	private Color focusedFill   = new Color(255, 255, 0, 0.75);
	private Color focusedStroke = new Color(0, 0, 0);
	
	// Select and dragging.
	private Color draggingFill   = new Color(255, 255, 0, 1.0);
	private Color draggingStroke = new Color(0, 0, 0);
	
	// Mouse state
	private boolean dragging = false;
	private CarPoint focused = null; // Selected CarPoint.
	
	// Maximum number of CarPoints to allow. <0 = unbounded.
	private int maxCars = -1;
	
	/**
	 * Creates an instance of <code>CarPointHandler</code>.
	 * 
	 * @param canvas the <code>Canvas</code> to draw on.
	 * @param background the image to use as a background.
	 */
	public CarPointHandler(Surface canvas, Image background) {
		carPoints = new ArrayList<CarPoint>();
		this.canvas = canvas;
		this.background = background;
		
		handlerRegs = new ArrayList<HandlerRegistration>();
		handlerManager = new HandlerManager(this);
	}
	
	/**
	 * Registers mouse handlers onto the current drawing canvas.
	 * 
	 * @see #deregister()
	 */
	public void register() {
		handlerRegs.add(canvas.addMouseDownHandler(this));
		handlerRegs.add(canvas.addMouseUpHandler(this));
		handlerRegs.add(canvas.addMouseMoveHandler(this));
	}
	
	/**
	 * Deregisters all of this <code>CarPointHandler</code> handlers from its
	 * drawing canvas.
	 * 
	 * @see #register()
	 */
	public void deregister() {
		for ( HandlerRegistration handlerReg : handlerRegs ) {
			handlerReg.removeHandler();
		}
		
		handlerRegs.clear(); // These aren't registered no more.
	}
	
	/**
	 * Sets the maximum number of {@link CarPoint}s to allow. Supply a negative
	 * number to allow an unbounded number.
	 * 
	 * @param maxCars the maximum number of <code>CarPoint</code>s, or a negative number.
	 */
	public void setMaxCars(int maxCars) {
		this.maxCars = maxCars;
	}
	
	/**
	 * Returns the maximum number of {@link CarPoint}s to allow. A negative
	 * return value means that the number of <code>CarPoint</code>s is
	 * unbounded.
	 * 
	 * @return the maximum number of <code>CarPoint</code>s, or a negative number.
	 */
	public int getMaxCars() {
		return maxCars;
	}
	
	/**
	 * Returns whether a {@link CarPoint} may be added without throwing an
	 * exception.
	 * 
	 * @return <code>true</code> if a <code>CarPoint</code> may be added. <code>false</code>, otherwise.
	 */
	public boolean canAddCar() {
		maxCars = getMaxCars();
		
		return (maxCars < 0 || carPoints.size() < maxCars);
	}
	
	/**
	 * Returns an unmodifiable list of this <code>CarPointHandler</code>'s
	 * registered {@link CarPoint}s.
	 * 
	 * @return this <code>CarPointHandler</code>'s <code>CarPoint</code>s.
	 */
	public List<CarPoint> getCars() {
		return Collections.unmodifiableList(carPoints);
	}
	
	/**
	 * Returns the {@link CarPoint} at that location, or <code>null if there are
	 * none. If the return value is non-<code>null</code>, then it is the first
	 * <code>CarPoint</code> such that {@link CarPoint#onPoint(Point2D)} returns
	 * <code>true</code> for the supplied point.
	 * 
	 * @param point the point to test.
	 * @return the <code>CarPoint</code> at that point, or <code>null</code>.
	 * @see #getCars()
	 */
	public CarPoint findCarAt(Point2D point) {
		// Simpler linear search. There shouldn't be enough points to justify
		// some kind of geometric storage container.
		for ( CarPoint carPoint : carPoints ) {
			if ( carPoint.onPoint(point) ) {
				return carPoint;
			}
		}
		
		return null;
	}
	
	/**
	 * Removes every {@link CarPoint} from this <code>CarPointHandler</code>
	 */
	public void clearCars() {
		carPoints.clear();

		// setFocusedCar fires an event. Only fire that event if it's changed.
		if ( getFocusedCar() != null ) {
			setFocusedCar(null);
		} else {
			// setFocusedCar()'d redraw otherwise.
			draw();
		}
	}
	
	/**
	 * Returns the currently focused {@link CarPoint}, or <code>null</code> if
	 * there is none.
	 * 
	 * @return the currently focused <code>CarPoint</code>, or <code>null</code>.
	 * @see #setFocusedCar(CarPoint)
	 */
	public CarPoint getFocusedCar() {
		return focused;
	}
	
	/**
	 * Sets the currently focused {@link CarPoint}. Supply <code>null</code> to
	 * have no <code>CarPoint</code> focused.
	 * 
	 * @param carPoint the <code>CarPoint</code> to focus, or <code>null</code>.
	 * @see #getFocusedCar()
	 */
	public void setFocusedCar(CarPoint carPoint) {
		// Only do stuff (and fire off events) if the focused car is changing.
		if ( carPoint != focused ) {
			// Update drawing colors to look responsive.
			if ( focused != null ) {
				focused.setFillColor(regularFill);
				focused.setStrokeColor(regularStroke);
			}
			if ( carPoint != null ) {
				carPoint.setFillColor(focusedFill);
				carPoint.setStrokeColor(focusedStroke);
			}

			focused = carPoint;
			SelectionEvent.fire(this, focused); // Notify handlers.
			
			draw();
		}
	}
	
	/**
	 * Removes the currently focused {@link CarPoint} from this <code>
	 * CarPointHandler</code>.
	 * 
	 * @see #getFocusedCar()
	 */
	public void deleteFocusedCar() {
		if ( getFocusedCar() != null ) {
			removeCar(getFocusedCar());
			draw();
		}
	}
	
	/**
	 * Adds a movable {@link CarPoint} at the specified point.
	 * 
	 * @param point where to add the <code>CarPoint</code>
	 * @return the <code>CarPoint</code> added.
	 */
	public CarPoint addCar(Point2D point) {
		return addCar(point, true);
	}
	
	/**
	 * 
	 * Adds a {@link CarPoint} at the specified point. The
	 * <code>CodePoint</code> is movable if <code>movable</code> is set to
	 * <code>true</code>.
	 * 
	 * @param point where to add the <code>CarPoint</code>
	 * @param movable whether the <code>CarPoint</code> should be movable.
	 * @return the <code>CarPoint</code> added.
	 * @throws IllegalStateException if there's already the maximum number of <code>CarPoint</code>s.
	 * @see #getMaxCars()
	 */
	public CarPoint addCar(Point2D point, boolean movable) {
		if ( !canAddCar() ) {
			throw new IllegalStateException(
					"Maximum number of CarPoints reached.");
		}
		
		CarPoint newCarPoint = new CarPoint(point.x, point.y, movable);
		carPoints.add(newCarPoint);
		
		return newCarPoint;
	}

	/**
	 * Removes the specified {@link CarPoint} from this <code>CarPointHandler
	 * </code>.
	 * 
	 * @param carPoint the <code>CarPoint</code> to remove.
	 * @throws IllegalArgumentException if the supplied <code>CarPoint</code> not registered to this <code>CarPointHandler</code>.
	 */
	private void removeCar(CarPoint carPoint) {
		int oldIdx = carPoints.indexOf(carPoint); // Find the CarPoint.
		
		if ( oldIdx == -1 ) { // CarPoint not found.
			throw new IllegalArgumentException(
					"Supplied CarPoint not registered to this CarPointHandler");
		}
		
		carPoints.remove(oldIdx); // Remove car point.
		
		// If that was the focused car point...
		if ( carPoint == getFocusedCar() ) {
			if ( isDragging() ) { // End any dragging.
				endDragging();
			}
			
			// Update focus.
			
			// If there's nothing to focus.
			if ( carPoints.isEmpty() ) {
				setFocusedCar(null); // Focus nothing.
			} else {
				// Otherwise, focus the next CarPoint.
				int prevIdx = Math.max(0, oldIdx-1);
				setFocusedCar(carPoints.get(prevIdx));
			}
		}
	}
	
	/**
	 * Draws this widget onto its drawing canvas. Simply draws the background
	 * image in the center and tells each registered {@link CarPoint} to draw
	 * itself onto the canvas.
	 * 
	 * Always ends up redrawing this <code>CarPointHandler</code>.
	 * @return 
	 */
	public boolean draw() {
		canvas.drawImage(ImageElement.as(background.getElement()), 0, 0);
		
		for ( CarPoint carPoint : carPoints ) {
			carPoint.draw(canvas);
		}
		
		return true;
	}
	
	/**
	 * Simply calls {@link #draw()}, which always redraws the <code>
	 * CarPointHandler</code>.
	 */
	public void redraw() {
		draw();
	}
	
	/**
	 * Does nothing, as this <code>CarPointHandler</code> will always redraw
	 * itself when {@link #draw()} is called.
	 */
	public void invalidate() {
		// Do nothing. The draw method always draws.
	}
	
	/**
	 * Returns the position of the mouse specified by the event, relative to
	 * the drawing canvas' element.
	 * 
	 * @param event the event to grab the mouse position from.
	 * @return the relative position of the mouse.
	 */
	private Point2D getMousePoint(MouseEvent<?> event) {
		Element element = canvas.getElement();
		return new Point2D(event.getRelativeX(element), event.getRelativeY(element));
	}
	
	/**
	 * Causes the focused element to no longer follow the mouse cursor.
	 */
	private void endDragging() {
		getFocusedCar().setFillColor(focusedFill);
		getFocusedCar().setStrokeColor(focusedStroke);
		dragging = false;
	}

	/**
	 * Causes the focused element to follow the mouse cursor.
	 * @see #endDragging()
	 * @see #onMouseDown(MouseDownEvent)
	 */
	private void startDragging() {
		getFocusedCar().setFillColor(draggingFill);
		getFocusedCar().setStrokeColor(draggingStroke);
		dragging = true;
	}

	/**
	 * Returns whether the focused element is currently following the mouse
	 * cursor.
	 * 
	 * @return whether the focused element is following the mouse.
	 * @see #startDragging()
	 */
	private boolean isDragging() {
		return dragging;
	}
	
	/**
	 * Responds to mouse-down events. If the location of the event is on a
	 * currently existing {@link CarPoint}, the method will start dragging on
	 * that element. Otherwise, the method will create a new <code>CarPoint
	 * </code> at the location of the click, and this method will start dragging on
	 * the newly created <code>CarPoint</code>.
	 * 
	 * @see #startDragging()
	 * @see #addCar(Point2D)
	 */
	@Override
	public void onMouseDown(MouseDownEvent event) {
		Point2D mousePoint = getMousePoint(event); // Get relative location.
		CarPoint carAt = findCarAt(mousePoint); // Ask for car there.
		
		if ( carAt == null ) { // Is there no car there?
			if ( canAddCar() ) {
				carAt = addCar(mousePoint);
			} else {
				return; // We can't do anything.
			}
		}
		
		setFocusedCar(carAt);
		
		if ( getFocusedCar().isMovable() ) {
			startDragging();
		}
		
		draw();
	}

	/**
	 * Responds to mouse-up events. Ends dragging, if an element is currently
	 * being dragged.
	 */
	@Override
	public void onMouseUp(MouseUpEvent event) {
		if ( isDragging() ) {
			endDragging();
			draw();
		}
	}

	/**
	 * Responds to mouse-move events. If an element is currently being dragged,
	 * this method updates its position to the mouse location specified by the
	 * supplied event.
	 */
	@Override
	public void onMouseMove(MouseMoveEvent event) {
		if ( isDragging() ) {
			Point2D mousePoint = getMousePoint(event); // Get relative location.
			focused.setPoint(mousePoint); // Update the focused car's loc.
			draw();
		}
	}
	
	/**
	 * Handles key events. If the "delete" or "backspace" button was hit, this
	 * method will delete the currently focused {@link CarPoint}, if there is
	 * one. If the "escape" button was hit, this method will unfocus the
	 * currently focused <code>CarPoint</code>.
	 * 
	 * @see #deleteFocusedCar()
	 * @see #setFocusedCar(CarPoint)
	 */
	@Override
	public void onKeyUp(KeyUpEvent event) {
		int keyCode = event.getNativeKeyCode();
		
		switch ( keyCode ) {
			case KeyCodes.KEY_DELETE:
			case KeyCodes.KEY_BACKSPACE:
				event.preventDefault(); // Hasn't been working.
				deleteFocusedCar();
				break;
			case KeyCodes.KEY_ESCAPE:
				event.preventDefault();
				setFocusedCar(null);
				break;
		}
	}

	/**
	 * Registers the supplied <code>SelectionHandler</code> to this <code>
	 * CarPointHandler</code>. Registered <code>SelectionHandler</code>s will
	 * recieve events when the focused {@link CarPoint} is changed.
	 */
	@Override
	public HandlerRegistration addSelectionHandler(
										SelectionHandler<CarPoint> handler) {
		return handlerManager.addHandler(SelectionEvent.getType(), handler);
	}
	
	/**
	 * Sends the supplied event to this <code>CarPointHandler</code>'s
	 * appropriate registered handlers.
	 */
	@Override
	public void fireEvent(GwtEvent<?> event) {
		handlerManager.fireEvent(event);
	}

}
