package car.picker.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.Surface;

/**
 * A point representing a car in an image. Contains location and size
 * information, and can store a <code>Color</code> attribute.
 * 
 * Supplies the {@link #draw(Surface)} method for drawing itself onto a <code>
 * Surface</code>.
 * 
 * @author Joshua Little
 */
public class CarPoint extends Point2D {
	
	/**
	 * The default color for drawing <code>CarPoint</code>s.
	 */
	public final static Color DEFAULT_COLOR = new Color(0, 0, 0);

	private Size carSize;
	private double clickableSize = 12;
	private double pointSize;
	private double rectSize;
	
	private boolean movable;
	
	private Color color = DEFAULT_COLOR;
	
	/**
	 * Creates an instance of <code>CarPoint</code> located at the origin. Has
	 * an initial size of {@link Size#Medium} and is movable.
	 * 
	 * @see #CarPoint(double, double, boolean)
	 */
	public CarPoint() {
		super();
		setSize(Size.Medium);
		this.movable = true;
	}

	/**
	 * Creates an instance of <code>CarPoint</code> located at the specified
	 * coordinates. Has an initial size of {@link Size#Medium} and is movable if
	 * <code>movable</code> is set to <code>true</code>.
	 * 
	 * @param x the x-coordinate of the new <code>CarPoint</code>.
	 * @param y the y-coordinate of the new <code>CarPoint</code>.
	 * @param movable whether the new <code>CarPoint</code> should be movable.
	 * @see #CarPoint()
	 */
	public CarPoint(double x, double y, boolean movable) {
		super(x, y);
		setSize(Size.Medium);
		this.movable = movable;
	}
	
	/**
	 * Draws this <code>CarPoint</code> onto the supplied <code>Surface</code>.
	 * Will fill a square of size {@link Size#pointSize} and stroke a square
	 * of size {@link Size#rectSize}, according to the <code>CarPoint</code>s
	 * size. Both rectangles are drawn in the <code>CarPoint</code> color.
	 * 
	 * @param ctx the <code>Surface</code> to draw on.
	 * @see #getSize()
	 * @see #getColor()
	 */
	public void draw(Surface ctx) {
		ctx.setFillStyle(getColor());
		ctx.setStrokeStyle(getColor());
		
        ctx.fillRectangle(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize);
        ctx.strokeRectangle(x - rectSize / 2, y - rectSize / 2, rectSize, rectSize);
	}

	/**
	 * Sets the drawing size of this <code>CarPoint</code>.
	 * 
	 * @param size the new drawing size.
	 * @see Size
	 */
	public void setSize(Size size) {
		carSize = size;
		pointSize = size.pointSize;
		rectSize = size.rectSize;
	}
	
	/**
	 * Returns the drawing size of this <code>CarPoint</code>.
	 * 
	 * @return the drawing size.
	 */
	public Size getSize() {
		return carSize;
	}
	
	/**
	 * Sets the drawing color of this <code>CarPoint</code>.
	 * 
	 * @param color the new drawing color.
	 * @see #getColor()
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Returns the drawing color of this <code>CarPoint</code>.
	 * 
	 * @return the drawing color.
	 * @see #setColor(Color)
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets whether this <code>CarPoint</code> should be movable by
	 * user-interaction.
	 * 
	 * @param movable whether this <code>CarPoint</code> should be movable.
	 * @see #isMovable()
	 */
	public void setMovable(boolean movable) {
		this.movable = movable;
	}

	/**
	 * Returns whether this <code>CarPoint</code> should be movable by
	 * user-interaction.
	 * 
	 * @return <code>true</code> if this <code>CarPoint</code> should be movable, <code>false</code> otherwise.
	 * @see #setMovable(boolean)
	 */
	public boolean isMovable() {
		return movable;
	}

	/**
	 * Returns true if the supplied <code>Point2D</code> is within the clickable
	 * area of this <code>CarPoint</code>. The clickable area is defined by a
	 * rectangle centered at this point with a size defined by
	 * {@link #getClickableSize()}.
	 * 
	 * @param point the point to test.
	 * @return <code>true</code> if the point is within the clickable area, <code>false</code> otherwise.
	 * @see #onPoint(double, double)
	 */
	public boolean onPoint(Point2D point){
	        return onPoint(point.x, point.y);
	}

	/**
	 * Returns true if the supplied coordinates are within the clickable
	 * area of this <code>CarPoint</code>. The clickable area is defined by a
	 * square centered at this point with a size defined by
	 * {@link #getClickableSize()}.
	 * 
	 * @param cx x-coordinate to test.
	 * @param cy y-coordinate to test.
	 * @return <code>true</code> if the point is within the clickable area, <code>false</code> otherwise.
	 * @see #onPoint(Point2D)
	 */
	public boolean onPoint(double cx, double cy){
        return (x - clickableSize / 2 < cx && cx < x + clickableSize / 2 ) &&
                   (y - clickableSize / 2 < cy && cy < y + clickableSize / 2 );
	}
	
	/**
	 * Returns the dimension of the square defining the clickable area of this
	 * <code>CarPoint</code>.
	 * 
	 * @return the clickable dimension.
	 * @see #setClickableSize(double)
	 */
	public double getClickableSize() {
		return clickableSize;
	}

	/**
	 * Sets the dimension of the square defining the clickable area of this
	 * <code>CarPoint</code>.
	 * 
	 * @param clickableSize the new clickable dimension.
	 * @see #setClickableSize(double)
	 */
	public void setClickableSize(double clickableSize) {
		this.clickableSize = clickableSize;
	}

	/**
	 * The valid sizes for a <code>CarPoint</code>. Contains size information
	 * for drawing.
	 * 
	 * @author Joshua Little
	 */
	public enum Size {
		Small {{rectSize = 50;}},
		Medium {{rectSize = 100;}},
		Large {{rectSize = 150;}};
		
		/**
		 * The size of the square to fill.
		 */
		public double pointSize = 6;
		/**
		 * The size of the square to stroke.
		 */
		public double rectSize;
	}
}
