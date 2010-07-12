package car.shared.math;

/**
 * A 3x3 matrix that allows direct access to its components.
 * 
 * @author Joshua Little
 */
public class Matrix3D implements Cloneable {
	public double m11, m12, m13,
		   		  m21, m22, m23,
		   		  m31, m32, m33;

	/**
	 * Creates a new instance of <code>Matrix3D</code> with the supplied
	 * components.
	 * 
	 * @param m11 Row 1, Column 1
	 * @param m12 Row 1, Column 2
	 * @param m13 Row 1, Column 3
	 * @param m21 Row 2, Column 1
	 * @param m22 Row 2, Column 2
	 * @param m23 Row 2, Column 3
	 * @param m31 Row 3, Column 1
	 * @param m32 Row 3, Column 2
	 * @param m33 Row 3, Column 3
	 */
	public Matrix3D(double m11, double m12, double m13,
					double m21, double m22, double m23,
					double m31, double m32, double m33) {
		
		this.m11 = m11; this.m12 = m12; this.m13 = m13;
		this.m21 = m21; this.m22 = m22; this.m23 = m23;
		this.m31 = m31; this.m32 = m32; this.m33 = m33;
	}
	
	/**
	 * Creates a new instance of <code>Matrix3D</code> equal to the identity
	 * matrix.
	 */
	public Matrix3D() {
		this(1, 0, 0, 0, 1, 0, 0, 0, 1); // Identity
	}
	
	/**
	 * Scales all components of this <code>Matrix3D</code> by the supplied
	 * value.
	 * 
	 * @param by the amount to scale by.
	 * @see #multiply(Matrix3D)
	 */
	public void scale(double by) {
		m11 *= by; m12 *= by; m13 *= by;
		m21 *= by; m22 *= by; m23 *= by;
		m31 *= by; m32 *= by; m33 *= by;
	}
	
	/**
	 * Multiply this <code>Matrix3D</code> on the left by the supplied <code>
	 * Matrix3D</code>.
	 * 
	 * @param by the matrix to multiply by.
	 * @see #scale(double)
	 */
	public void multiply(Matrix3D by) {
		Matrix3D what = clone();
		
		m11 = by.m11*what.m11 + by.m12*what.m21 + by.m13*what.m31;
		m12 = by.m11*what.m12 + by.m12*what.m22 + by.m13*what.m32;
		m13 = by.m11*what.m13 + by.m12*what.m23 + by.m13*what.m33;
		
		m21 = by.m21*what.m11 + by.m22*what.m21 + by.m23*what.m31;
		m22 = by.m21*what.m12 + by.m22*what.m22 + by.m23*what.m32;
		m23 = by.m21*what.m13 + by.m22*what.m23 + by.m23*what.m33;
		
		m31 = by.m31*what.m11 + by.m32*what.m21 + by.m33*what.m31;
		m32 = by.m31*what.m12 + by.m32*what.m22 + by.m33*what.m32;
		m33 = by.m31*what.m13 + by.m32*what.m23 + by.m33*what.m33;
	}
	
	/**
	 * Inverts this <code>Matrix3D</code> in place.
	 * 
	 * @throws IllegalStateException if this <code>Matrix3D</code> is not invertible.
	 */
	public void invert() {
		double det = determinant();
		
		if ( det == 0 ) {
			throw new IllegalStateException("This matrix is not invertible.");
		}
		
		Matrix3D what = clone();
		
		m11 = what.m33*what.m22 - what.m32*what.m23;
		m12 = what.m32*what.m13 - what.m33*what.m12;
		m13 = what.m23*what.m12 - what.m22*what.m13;
		
		m21 = what.m31*what.m23 - what.m33*what.m21;
		m22 = what.m33*what.m11 - what.m31*what.m13;
		m23 = what.m21*what.m13 - what.m23*what.m11;
		
		m31 = what.m32*what.m21 - what.m31*what.m22;
		m32 = what.m31*what.m12 - what.m32*what.m11;
		m33 = what.m22*what.m11 - what.m21*what.m12;
		
		scale(1 / determinant());
	}
	
	/**
	 * Computes the determinant of this <code>Matrix3D</code>.
	 * 
	 * @return the determinant.
	 */
	public double determinant() {
		return m11 * (m33*m22 - m32*m23)
			 - m21 * (m33*m12 - m32*m13)
			 + m31 * (m23*m12 - m22*m13);
	}
	
	/**
	 * Returns the <code>String</code> representation of this <code>Matrix3D
	 * </code>. The <code>String</code> is broken up into three lines.
	 * 
	 * @return the <code>String</code> representation.
	 */
	@Override
	public String toString() {
		return "[[" + m11 + ", " + m12 + ", " + m13 + "]\n"
			 + " [" + m21 + ", " + m22 + ", " + m23 + "]\n"
			 + " [" + m31 + ", " + m32 + ", " + m33 + "]]";
	}
	
	/**
	 * Returns a copy of this <code>Matrix3D</code>.
	 * 
	 * @return a copy of this <code>Matrix3D</code>.
	 */
	public Matrix3D clone() {
		return new Matrix3D(m11, m12, m13, m21, m22, m23, m31, m32, m33);
	}
	
	/**
	 * Multiplies the first <code>Matrix3D</code> on the right by the second
	 * </code>Matrix3D</code> and returns the results. Does not change either
	 * supplied <code>Matrix3D</code>
	 * 
	 * @param what the matrix on the left.
	 * @param by the matrix on the right.
	 * @return the product of the two matrices.
	 */
	public static Matrix3D multiply(Matrix3D what, Matrix3D by) {
		Matrix3D mat = new Matrix3D();
		
		mat.m11 = by.m11*what.m11 + by.m12*what.m21 + by.m13*what.m31;
		mat.m12 = by.m11*what.m12 + by.m12*what.m22 + by.m13*what.m32;
		mat.m13 = by.m11*what.m13 + by.m12*what.m23 + by.m13*what.m33;
		
		mat.m21 = by.m21*what.m11 + by.m22*what.m21 + by.m23*what.m31;
		mat.m22 = by.m21*what.m12 + by.m22*what.m22 + by.m23*what.m32;
		mat.m23 = by.m21*what.m13 + by.m22*what.m23 + by.m23*what.m33;
		
		mat.m31 = by.m31*what.m11 + by.m32*what.m21 + by.m33*what.m31;
		mat.m32 = by.m31*what.m12 + by.m32*what.m22 + by.m33*what.m32;
		mat.m33 = by.m31*what.m13 + by.m32*what.m23 + by.m33*what.m33;
		
		return mat;
	}
	
	/**
	 * Returns the inverse of the supplied <code>Matrix3D</code>. Does not
	 * change the supplied <code>Matrix3D</code>
	 * 
	 * @param what the <code>Matrix3D</code> to invert.
	 * @return the inverse of the <code>Matrix3D</code>.
	 */
	public static Matrix3D invert(Matrix3D what) {
		Matrix3D ret = what.clone();
		
		ret.invert();
		return ret;
	}
	
}
