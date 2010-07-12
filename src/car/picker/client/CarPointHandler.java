package car.picker.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.Surface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 * @author Joshua
 */
public class CarPointHandler implements MouseDownHandler, MouseUpHandler,
										MouseMoveHandler, KeyUpHandler,
										HasSelectionHandlers<CarPoint> {
	
	private List<CarPoint> carPoints = null; // List of current CarPoints.
	private Surface canvas = null; // The canvas to draw everything on.
	private Image background = null; // The background image.
	
	// Manages registration and event-firing for registered handles.
	private HandlerManager handlerManager;
	
	// List of registrations for the handlers that *this* object creates.
	private List<HandlerRegistration> handlerRegs = null;
	
	// Current selected car size.
	private CarPoint.Size carSize = CarPoint.Size.Medium;
	
	// Colors to draw CarPoints in.
	private Color regularColor = CarPoint.DEFAULT_COLOR; // Default state
	private Color focusedColor = new Color(0, 0, 126); //Selected, not dragging.
	private Color draggingColor = new Color(0, 0, 255); // Select and dragging.
	
	// Mouse state
	private boolean dragging = false;
	private CarPoint focused = null; // Selected CarPoint.
	
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
	 * Sets the car {@linkplain CarPoint.Size size} to use when creating
	 * {@link CarPoint}s. Also updates the currently focused
	 * <code>CarPoint</code>'s size.
	 * 
	 * @param carSize the new size.
	 * @see #getCarSize()
	 */
	public void setCarSize(CarPoint.Size carSize) {
		this.carSize = carSize;
		
		CarPoint focusedCar = getFocusedCar();
		if ( focusedCar != null ) { // If there's a focused car...
			focusedCar.setSize(carSize); // Update its size.
			draw();
		}
	}
	
	/**
	 * Returns the current car {@linkplain CarPoint.Size size} used for creating
	 * cars.
	 * 
	 * @return the current car size.
	 * @see #setCarSize(CarPoint.Size)
	 */
	public CarPoint.Size getCarSize() {
		return carSize;
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
				focused.setColor(regularColor);
			}
			if ( carPoint != null ) {
				carPoint.setColor(focusedColor);
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
		return addCar(point, carSize, true);
	}
	
	/**
	 * 
	 * Adds a {@link CarPoint} of the specified {@linkplain CarPoint.Size size}
	 * at the specified point. The <code>CodePoint</code> is movable if <code>
	 * movable</code> is set to <code>true</code>.
	 * 
	 * @param point where to add the <code>CarPoint</code>
	 * @param size the size of the new <code>CarPoint</code>
	 * @param movable whether the <code>CarPoint</code> should be movable.
	 * @return the <code>CarPoint</code> added.
	 */
	public CarPoint addCar(Point2D point, CarPoint.Size size, boolean movable) {
		CarPoint newCarPoint = new CarPoint(point.x, point.y, movable);
		newCarPoint.setSize(size);
		
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
	 */
	public void draw() {
		canvas.drawImage(ImageElement.as(background.getElement()), 0, 0);
		
		for ( CarPoint carPoint : carPoints ) {
			carPoint.draw(canvas);
		}
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
		// draw() always draws.
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
		getFocusedCar().setColor(focusedColor);
		dragging = false;
	}

	/**
	 * Causes the focused element to follow the mouse cursor.
	 * @see #endDragging()
	 * @see #onMouseDown(MouseDownEvent)
	 */
	private void startDragging() {
		getFocusedCar().setColor(draggingColor);
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
	 * </code> at the location of the click, with the current car
	 * {@linkplain CarPoint.Size size}, and this method will start dragging on
	 * the newly created <code>CarPoint</code>.
	 * 
	 * @see #startDragging()
	 * @see #addCar(Point2D)
	 * @see #getCarSize()
	 */
	@Override
	public void onMouseDown(MouseDownEvent event) {
		Point2D mousePoint = getMousePoint(event); // Get relative location.
		CarPoint carAt = findCarAt(mousePoint); // Ask for car there.
		
		if ( carAt == null ) { // Is there no car there?
			carAt = addCar(mousePoint);
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
			focused.setPosition(mousePoint); // Update the focused car's loc.
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
				event.preventDefault();
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
