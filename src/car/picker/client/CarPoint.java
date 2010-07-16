package car.picker.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.graphics.shapes.CircleShape;
import car.shared.math.Point2D;

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
	public final static Color DEFAULT_FILL   = new Color(0, 0, 255, 0.75);
	public final static Color DEFAULT_STROKE = new Color(0, 0, 0);

	private double selectRadius = 6;
	private double drawRadius = 4;
	
	private boolean movable;
	
	private Color fillColor = DEFAULT_FILL;
	private Color strokeColor = DEFAULT_STROKE;
	
	/**
	 * Creates an instance of <code>CarPoint</code> located at the origin. Has
	 * an initial size of {@link Size#Medium} and is movable.
	 * 
	 * @see #CarPoint(double, double, boolean)
	 */
	public CarPoint() {
		super();
		this.movable = true;
	}

	/**
	 * Creates an instance of <code>CarPoint</code> located at the specified
	 * coordinates. Is initially movable if <code>movable</code> is set to
	 * <code>true</code>.
	 * 
	 * @param x the x-coordinate of the new <code>CarPoint</code>.
	 * @param y the y-coordinate of the new <code>CarPoint</code>.
	 * @param movable whether the new <code>CarPoint</code> should be movable.
	 * @see #CarPoint()
	 */
	public CarPoint(double x, double y, boolean movable) {
		super(x, y);
		this.movable = movable;
	}
	
	/**
	 * Draws this <code>CarPoint</code> onto the supplied <code>Surface</code>.
	 * Will fill and stroke a circle of size {@link drawSize} in the <code>
	 * CarPoint</code>'s {@linkplain #getFillColor() fill color} and
	 * {@linkplain #getFillColor() stroke}
	 * 
	 * @param ctx the <code>Surface</code> to draw on.
	 * @see #getFillColor()
	 * @see #getStrokeColor()
	 */
	public void draw(Surface ctx) {
		ctx.setFillStyle(getFillColor());
		ctx.setStrokeStyle(getStrokeColor());

		CircleShape circle = new CircleShape(x, y, drawRadius);
		
		ctx.fillShape(circle);
		ctx.strokeShape(circle);
	}

	/**
	 * Sets the fill color of this <code>CarPoint</code>.
	 * 
	 * @param color the new fill color.
	 * @see #getColor()
	 */
	public void setFillColor(Color color) {
		this.fillColor = color;
	}

	/**
	 * Returns the fill color of this <code>CarPoint</code>.
	 * 
	 * @return the fill color.
	 * @see #setColor(Color)
	 */
	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * Sets the stroke color of this <code>CarPoint</code>.
	 * 
	 * @param color the new stroke color.
	 * @see #getColor()
	 */
	public void setStrokeColor(Color color) {
		this.strokeColor = color;
	}

	/**
	 * Returns the stroke color of this <code>CarPoint</code>.
	 * 
	 * @return the stroke color.
	 * @see #setColor(Color)
	 */
	public Color getStrokeColor() {
		return strokeColor;
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
	 * Returns true if the supplied <code>Point2D</code> are within the
	 * clickable area of this <code>CarPoint</code>. The clickable area is
	 * defined by a circle centered at this point with a radius of
	 * {@link #getSelectRadius()}.
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
	 * circle centered at this point with a radius of
	 * {@link #getSelectRadius()}.
	 * 
	 * @param cx x-coordinate to test.
	 * @param cy y-coordinate to test.
	 * @return <code>true</code> if the point is within the clickable area, <code>false</code> otherwise.
	 * @see #onPoint(Point2D)
	 */
	public boolean onPoint(double cx, double cy){
        return Math.hypot(x-cx, y-cy) < selectRadius;
	}
	
	/**
	 * Returns the radius of the circle defining the clickable area of this
	 * <code>CarPoint</code>.
	 * 
	 * @return the clickable dimension.
	 * @see #setClickableSize(double)
	 */
	public double getSelectRadius() {
		return selectRadius;
	}

	/**
	 * Sets the radius of the circle defining the clickable area of this
	 * <code>CarPoint</code>.
	 * 
	 * @param selectRadius the new clickable dimension.
	 * @see #getSelectRadius()
	 */
	public void setSelectRadius(double selectRadius) {
		this.selectRadius = selectRadius;
	}
}
