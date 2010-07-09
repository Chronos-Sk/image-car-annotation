package car.orientor.math;

/**
 * A point with two coordinates, <code>x</code> and <code>y</code>, that allows
 * direct access to its coordinates.
 * 
 * @author Joshua Little
 */
public class Point2D implements Cloneable {
	public double x, y; // The coordinates.
	
	/**
	 * Returns a new instance of <code>Point2D</code> positioned at the origin.
	 */
	public Point2D() {
		this(0, 0);
	}

	/**
	 * Returns a new instance of <code>Point2D</code> positioned at the point
	 * (<code>x</code>, <code>y</code>).
	 * 
	 * @param x the x-coordinate of the new point.
	 * @param y the y-coordinate of the new point.
	 */
	public Point2D(double x, double y) {
		setPoint(x, y);
	}
	
	/**
	 * Changes the position of this <code>Point2D</code> to coincide with the
	 * supplied <code>Point2D</code>.
	 * 
	 * @param other the <code>Point2D</code> to set this point to.
	 * @see #setPoint(double, double)
	 */
	public void setPoint(Point2D other) {
		this.x = other.x;
		this.y = other.y;
	}
	
	/**
	 * Changes the position of this <code>Point2D</code> to the supplied one.
	 * 
	 * @param x the new x-coordinate.
	 * @param y the new y-coordinate.
	 * @see #setPoint(Point3D)
	 */
	public void setPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Computes the magnitude of this <code>Point2D</code>.
	 * 
	 * @return the magnitude.
	 */
	public double getMagnitude() {
		return Math.hypot(x, y);
	}
	
	/**
	 * Normalizes this <code>Point2D</code>. A normalized vector points in the
	 * same direction as the original vector, but has a magnitude of 1.
	 */
	public void normalize() {
		double magnitude = getMagnitude();
		
		x /= magnitude;
		y /= magnitude;
	}
	
	/**
	 * Computes a hash-code of this <code>Point2D</code> which fufills the
	 * contract with this object's {@see #equals(Object)} method.
	 * 
	 * @return the hash-code.
	 */
	@Override
	public int hashCode() {
		return new Double(x).hashCode() ^ new Double(y).hashCode();
	}

	/**
	 * Returns <code>true</code> whether this <code>Point2D</code> and the
	 * supplied one have the same coordinates.
	 * 
	 * @return <code>true</code> if they have the same coordinates, <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		Point2D other = (Point2D) obj;
		
		return ( x == other.x && y == other.y );
	}

	/**
	 * Returns the <code>String</code> representation of this <code>Point2D
	 * </code>.
	 * 
	 * @return the <code>String</code> representation.
	 */
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	/**
	 * Returns a copy of this <code>Point2D</code>.
	 * 
	 * @return a copy of this <code>Point2D</code>.
	 */
	public Point2D clone() {
		return new Point2D(x, y);
	}
}
