package car.orientor.views;

/**
 * Specifies a class that knows how to draw itself. Generally used with <code>
 * Widget</code>s that contain <code>canvas</code>s that need to be redrawn. In
 * response to certain events.
 * 
 * @author Joshua Little
 */
public interface Drawable {
	
	/**
	 * Tells this <code>Drawable</code> to draw itself if it needs to.
	 * @return whether the <code>Drawable</code> ended up redrawing itself.
	 */
	public abstract boolean draw();
	

	/**
	 * Forces this <code>Drawable</code> to redraw itself.
	 */
	public abstract void redraw();
	
	/**
	 * Sets an internal flag forcing this <code>Drawable</code> to redraw itself
	 * on the next call to <code>draw()</code>.
	 */
	public void invalidate();
}