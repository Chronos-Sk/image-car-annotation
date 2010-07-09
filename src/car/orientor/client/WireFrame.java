package car.orientor.client;

import car.orientor.math.Point3D;

@Deprecated
public class WireFrame {
	
	public Point3D[] points;
	public int[] lines;
	
	/**
	 * @param points The list of points in the frame.
	 * @param lines An array of lines from points[i] -> points[i+1].
	 */
	public WireFrame(Point3D[] points, int[] lines) {
		this.points = points;
		this.lines = lines;
		
		assert isValid() : "WireFrame is invalid.";
	}
	
	public WireFrame() {
		this(new Point3D[0], new int[0]);
	}
	
	public boolean isValid() {
		if ( points == null || lines == null ) {
			return false;
		}
		
		if ( lines.length % 2 != 0 ) {
			return false;
		}
		
		for ( int p : lines ) {
			if ( p < 0 || p >= points.length ) {
				return false;
			}
		}
		
		return true;
	}
}
