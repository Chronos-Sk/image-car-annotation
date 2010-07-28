package car.viewer.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.KnownColor;
import car.shared.math.Point2D;

/**
 * Represents a car--with location, rotation, and scale--to be drawn onto a
 * {@link CarViewer}.
 * 
 * Exports a native JavaScript interface class <code>Car</code> when statically
 * initialized, or when {@link #export()} is called.
 * 
 * @author Joshua Little
 */
public class Car {
	/**
	 * Exports this objects native JavaScript interface.
	 */
	public static native void export() /*-{
		if ( !$wnd.Car ) { // If it hasn't been exported yet.
			@car.viewer.client.Car::createCarClass()();
		}
	}-*/;
	
	/**
	 * Creates the native JavaScript <code>Car</code> wrapper class that allows
	 * access from stand-alone JavaScript.
	 */
	static native void createCarClass() /*-{
		Car = function() {
			this.carInst = @car.viewer.client.Car::new()();
		};
		
		Car.prototype.setPosition = $entry(function(x,y) {
			this.carInst.@car.viewer.client.Car::setPosition(DD)(x,y);
		});
		
		Car.prototype.setRotation = $entry(function(x,y,z) {
	    	this.carInst.@car.viewer.client.Car::setRotation(DDD)(x,y,z);
		});
		
		Car.prototype.setScale = $entry(function(scale) {
	    	this.carInst.@car.viewer.client.Car::setScale(D)(scale);
		});
		
		Car.prototype.setType = $entry(function(type) {
	    	this.carInst.@car.viewer.client.Car::setType(I)(type);
		});
		
		Car.prototype.setColor = $entry(function(r,g,b) {
	    	this.carInst.@car.viewer.client.Car::setColor(III)(r,g,b);
		});
	    
	    
		Car.prototype.getPositionX = $entry(function() {
	    	return this.carInst.@car.viewer.client.Car::getPositionX()();
		});
		
		Car.prototype.getPositionY = $entry(function() {
	    	return this.carInst.@car.viewer.client.Car::getPositionY()();
		});
		
		Car.prototype.getRotateX = $entry(function() {
	    	return this.carInst.@car.viewer.client.Car::getRotateX()();
		});
		
		Car.prototype.getRotateY = $entry(function() {
	    	return this.carInst.@car.viewer.client.Car::getRotateY()();
		});
		
		Car.prototype.getRotateZ = $entry(function() {
	    	return this.carInst.@car.viewer.client.Car::getRotateZ()();
		});
		
		Car.prototype.getScale = $entry(function() {
	    	return this.carInst.@car.viewer.client.Car::getScale()();
		});
		
		Car.prototype.getType = $entry(function() {
	    	return this.carInst.@car.viewer.client.Car::getType()();
		});
		
		$wnd.Car = Car
	}-*/;
	
	static {
		export();
	}
	
	int carType;
	
	Point2D pos = new Point2D();
	double rotX, rotY, rotZ;
	double scale;
	Color color = KnownColor.BLACK;

	/**
	 * Sets the car type that this car uses.
	 * 
	 * Mainly, this just determines the car model used.
	 * 
	 * @param carType the new car type
	 * @see #setType(int)
	 */
	public void setType(int carType) {
		this.carType = carType;
	}
	
	/**
	 * Sets the position in image pixels for the center of the car model.
	 *  
	 * @param posX the new x-coordinate.
	 * @param posY the new y-coordinate.
	 * @see #getPositionX()
	 * @see #getPositionY()
	 * @see #getPosition()
	 */
	public void setPosition(double posX, double posY) {
		pos.setPoint(posX, posY);
	}

	/**
	 * Sets the position in image pixels for the center of the car model.
	 *  
	 * @param pos the new position.
	 * @see #getPositionX()
	 * @see #getPositionY()
	 * @see #getPosition()
	 */
	public void setPosition(Point2D pos) {
		pos.setPoint(pos);
	}
	
	/**
	 * Sets the rotation to be applied to the car model.
	 * 
	 * The model is rotated around the z-axis, y-axis, and x-axis in that order.
	 * 
	 * @param rotX the new x-rotation.
	 * @param rotY the new y-rotation.
	 * @param rotZ the new z-rotation.
	 * @see #getRotateX()
	 * @see #getRotateY()
	 * @see #getRotateZ()
	 */
	public void setRotation(double rotX, double rotY, double rotZ) {
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
	}
	
	/**
	 * Sets the scale to be applied to the car model.
	 * 
	 * @param scale the new scale for the car model.
	 * @see #getScale()
	 */
	public void setScale(double scale) {
		this.scale = scale;
	}
	
	/**
	 * Sets the <code>Color</code> to draw the car model in.
	 * 
	 * @param color the new color for the car model.
	 * @see #setColor(int, int, int)
	 * @see #getColor()
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Sets the <code>Color</code> to draw the car model.
	 * 
	 * @param r the new red-value for this car's color.
	 * @param g the new green-value for this car's color.
	 * @param b the new blue-value for this car's color.
	 * @see #setColor(Color)
	 * @see #getColor()
	 */
	public void setColor(int r, int g, int b) {
		this.color = new Color(r, g, b);
	}

	/**
	 * Returns the car type that this car uses.
	 * 
	 * Mainly, this just determines the car model used.
	 * 
	 * @return the car type
	 * @see #setType(int)
	 */
	public int getType() {
		return carType;
	}

	/**
	 * Returns the x-coordinate for the center of the car model.
	 * 
	 * @return the center x-coordinate
	 */
	public double getPositionX() {
		return pos.x;
	}

	/**
	 * Returns the y-coordinate for the center of the car model.
	 * 
	 * @return the center y-coordinate
	 */
	public double getPositionY() {
		return pos.y;
	}
	
	/**
	 * Returns the position of the center of the car model.
	 * 
	 * @return the position of the center of the car model.
	 */
	public Point2D getPosition() {
		return pos;
	}

	/**
	 * Returns the x-rotation of the car model, in radians.
	 * 
	 * @return the x-rotation in radians
	 */
	public double getRotateX() {
		return rotX;
	}

	/**
	 * Returns the y-rotation of the car model, in radians.
	 * 
	 * @return the y-rotation in radians
	 */
	public double getRotateY() {
		return rotY;
	}

	/**
	 * Returns the z-rotation of the car model, in radians.
	 * 
	 * @return the z-rotation in radians
	 */
	public double getRotateZ() {
		return rotZ;
	}

	/**
	 * Returns the scale applied to the car model.
	 * 
	 * @return the scale of the car
	 */
	public double getScale() {
		return scale;
	}
	
	/**
	 * Returns the color this car should be drawn in.
	 * 
	 * @return the color this car should be drawn in.
	 */
	public Color getColor() {
		return color;
	}
}
