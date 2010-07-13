package car.shared.math;

/**
 * A point with three coordinates, <code>x</code>, <code>y</code>, and <code>z
 * </code>, that allows direct access to its coordinates.
 * 
 * @author Joshua Little
 */
public class Point3D implements Cloneable {
	public double x, y, z; // The coordinates.

	/**
	 * Returns a new instance of <code>Point3D</code> positioned at the origin.
	 */
	public Point3D() {
		this(0, 0, 0);
	}

	/**
	 * Returns a new instance of <code>Point3D</code> positioned at the point
	 * (<code>x</code>, <code>y</code>, <code>z</code>).
	 * 
	 * @param x the x-coordinate of the new point.
	 * @param y the y-coordinate of the new point.
	 * @param z the z-coordinate of the new point.
	 */
	public Point3D(double x, double y, double z) {
		setPoint(x, y, z);
	}

	/**
	 * Changes the position of this <code>Point3D</code> to coincide with the
	 * supplied <code>Point3D</code>.
	 * 
	 * @param other the <code>Point3D</code> to set this point to.
	 */
	public void setPoint(Point3D other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}

	/**
	 * Changes the position of this <code>Point3D</code> to the supplied one.
	 * 
	 * @param x the new x-coordinate.
	 * @param y the new y-coordinate.
	 * @param z the new z-coordinate.
	 */
	public void setPoint(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Adds the supplied <code>Point3D</code> to this <code>Point3D</code>.
	 * 
	 * @param addend the <code>Point3D</code> to add.
	 */
	public void add(Point3D addend) {
		this.x += addend.x;
		this.y += addend.y;
		this.z += addend.z;
	}

	/**
	 * Subtracts the supplied <code>Point3D</code> from this
	 * <code>Point3D</code>.
	 * 
	 * @param what the <code>Point3D</code> to subtract.
	 */
	public void subtract(Point3D what) {
		this.x -= what.x;
		this.y -= what.y;
		this.z -= what.z;
	}

	/**
	 * Scales all three components of this <code>Point3D</code> by the supplied
	 * value.
	 * 
	 * @param by the amount to scale by.
	 * @see #dot(Point3D)
	 */
	public void scale(double by) {
		x *= by;
		y *= by;
		z *= by;
	}

	/**
	 * Computes the dot product of this <code>Point3D</code> with the supplied
	 * <code>Point3D</code>.
	 * 
	 * @param other the <code>Point3D</code> to dot with.
	 * @see #scale(double)
	 */
	public double dot(Point3D other) {
		return x*other.x + y*other.y + z*other.z;
	}

	/**
	 * Computes the magnitude of this <code>Point3D</code>.
	 * 
	 * @return the magnitude.
	 */
	public double getMagnitude() {
		return Math.hypot(Math.hypot(x, y), z);
	}

	/**
	 * Normalizes this <code>Point3D</code>. A normalized vector points in the
	 * same direction as the original vector, but has a magnitude of 1.
	 */
	public void normalize() {
		scale(1 / getMagnitude());
	}

	/**
	 * Multiplies this <code>Point3D</code> by the supplied {@link Matrix3D}.
	 * 
	 * @param mat the <code>Matrix3D</code> to multiply by.
	 */
	public void multiply(Matrix3D mat) {
		double tx = x, ty = y, tz = z;
		
		x = tx * mat.m11 + ty * mat.m12 + tz * mat.m13;
		y = tx * mat.m21 + ty * mat.m22 + tz * mat.m23;
		z = tx * mat.m31 + ty * mat.m32 + tz * mat.m33;
	}
	
	/**
	 * Multiplies the supplied <code>Point3D</code> by the supplied
	 * {@link Matrix3D} and returns the result. Does not change either argument.
	 * 
	 * @param point the <code>Point3D</code> to multiply.
	 * @param mat the <code>Matrix3D</code> to multiply by.
	 * @return the product.
	 */
	public static Point3D multiply(Point3D point, Matrix3D mat) {
		Point3D clone = point.clone();
		clone.multiply(mat);
		return clone;
	}

	/**
	 * Computes a hash-code of this <code>Point3D</code> which fufills the
	 * contract with this object's {@link #equals(Object)} method.
	 * 
	 * @return the hash-code.
	 */
	@Override
	public int hashCode() {
		return new Double(x).hashCode() ^ new Double(y).hashCode() ^ new Double(z).hashCode();
	}

	/**
	 * Returns <code>true</code> whether this <code>Point3D</code> and the
	 * supplied one have the same coordinates.
	 * 
	 * @return <code>true</code> if they have the same coordinates, <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		
		if ( obj == null ) {
			return false;
		}
		
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		
		Point2D other = (Point2D) obj;
		
		return ( x == other.x && y == other.y && y == other.y );
	}

	/**
	 * Returns the <code>String</code> representation of this <code>Point3D
	 * </code>.
	 * 
	 * @return the <code>String</code> representation.
	 */
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}

	/**
	 * Returns a copy of this <code>Point3D</code>.
	 * 
	 * @return a copy of this <code>Point3D</code>.
	 */
	public Point3D clone() {
		return new Point3D(x, y, z);
	}
}
