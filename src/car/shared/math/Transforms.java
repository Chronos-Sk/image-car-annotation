package car.shared.math;

import gwt.g2d.client.math.Matrix;

public abstract class Transforms {
	
	// Abstract + private constructor = non-instantiable.
	private Transforms() {}
	
	static public Matrix centerAndScale(Point2D pos, double scale) {
		return centerAndScale(pos.x, pos.y, scale);
	}
	
	static public void centerAndScale(Matrix mat, Point2D pos, double scale) {
		centerAndScale(mat, pos.x, pos.y, scale);
	}
	
	static public Matrix centerAndScale(double x, double y, double scale) {
		Matrix mat = new Matrix();
		centerAndScale(mat, x, y, scale);
		return mat;
	}

	static public void centerAndScale(
								Matrix mat, double x, double y, double scale) {
		mat.set(scale, 0, 0, scale, -scale*x, -scale*y);
	}
}
