package car.picker.client;

import car.picker.client.Point2D;

public class Point2D {
	public double x, y;
	
	public Point2D() {
		this(0, 0);
	}
	
	public Point2D(double x, double y) {
		this.x = x; this.y = y;
	}
	
	public void setPosition(double x, double y) {
		this.x = x; this.y = y;
	}
	
	public void setPosition(Point2D point) {
		this.x = point.x; this.y = point.y;
	}
}
