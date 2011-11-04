package car.shared.views3d;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.Composition;
import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.math.MathHelper;
import gwt.g2d.client.math.Matrix;
import gwt.g2d.client.math.Vector2;
import car.shared.math.Matrix3D;
import car.shared.math.Point3D;
import car.shared.views.Drawable;
import car.shared.views3d.obj.Face;
import car.shared.views3d.obj.ObjWireFrame;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FocusPanel;

/**
 * This class allows the viewing of a 3D
 * {@link car.shared.views3d.obj.ObjWireFrame}. It uses a simple
 * orthogonal projection. It has functions that allow the rotation of the
 * <code>ObjWireFrame</code> around all three axes.
 * 
 * The z-rotation is applied first, then the y-rotation, then finally the
 * x-rotation.
 * 
 * Positive-y is upwards.
 * 
 * @author Joshua Little
 */
public class WireFrameView extends FocusPanel implements Drawable {
	/**
	 * The default x-axis rotation for newly created and reset
	 * <code>WireFrameView</code>s.
	 */
	public static double DEFAULT_X_ROTATION = Math.PI/8;
	
	/**
	 * The default y-axis rotation for newly created and reset
	 * <code>WireFrameView</code>s.
	 */
	public static double DEFAULT_Y_ROTATION = MathHelper.PI_OVER_4;
	
	private ObjWireFrame wireFrame = null; // Wire-frame to draw.

	private Matrix baseTransform; // Transformation so that y = [-1.0, 1.0].
	
	// Matrix to multiply vertices by to rotate them.
	private Matrix3D rotMatrix;
	
	// Rotations around the x-, y-, and z-axes respectively.
	private double rotX = DEFAULT_X_ROTATION;
	private double rotY = DEFAULT_Y_ROTATION;
	private double rotZ = 0;
	
	private Point3D[] tVertices; // Transformed (rotated) points.
	private Point3D[] tNormals; // Transformed (rotated) normals.
	private Vector2[] vPoints; // Points projected into view-coordinates.
	private boolean dirty; // Need redraw?
	
	private Surface canvas; // Main canvas to draw on.
	private DirectShapeRenderer builder; // Allows custom path drawing.

	// Check if user has modified state.
	private boolean rotated = false;
	
	// Color to draw the wire-frame in.
	private Color lineColor = new Color(0, 0, 0);
	
	/**
	 * Creates an instance of <code>WireFrameView</code>. The x- and
	 * y-rotation will be set to their respective default values.
	 * 
	 * @param wireFrame the wire-frame to draw.
	 * @param width the width of the canvas.
	 * @param height the height of the canvas.
	 * @see #DEFAULT_X_ROTATION
	 * @see #DEFAULT_Y_ROTATION
	 */
	public WireFrameView(ObjWireFrame wireFrame, int width, int height) {
		this.wireFrame = wireFrame;
		reconstructBuffer(); // Build all of the buffer variables.
		
		canvas = new Surface(width, height);
		builder = new DirectShapeRenderer(canvas);// Allows custom path drawing.
		
		// Change our view-coordinates to be all nice.
		baseTransform = new Matrix();
		// Scale uniformly so that y = [0.0, 2.0].
		baseTransform.setScale(height/2.0, height/2.0);
		// Translate uniformly so that y = [-1.0, 1.0]
		baseTransform = baseTransform.translate(((double) width)/height, 1);
		canvas.setTransform(baseTransform);
		
		// If width == height, x = [-1.0, 1.0].
		// Regardless, x = [-width/height, width/height].
		
		// Line sizes get transformed as well, so we'll have to reset them to 1.
		canvas.setLineWidth(2.0/height);
		canvas.setStrokeStyle(lineColor);
		
		// Fill in with transparent black.
		canvas.setFillStyle(new Color(0, 0, 0, 0));
		// Set alpha composite so that source replaces destination.
		canvas.setGlobalCompositeOperation(Composition.COPY);
		// This means that back lines are properly occluded, but that the fill
		// will be completely transparent when drawn onto other canvases.
		
		rotMatrix = new Matrix3D();
		
		setWidget(canvas); // Sets the canvas to be the drawn widget.
		
		invalidate(); // So it'll redraw next call to draw().
	}
	
	/**
	 * Returns <code>true</code> if the wire-frame has been rotated from its
	 * default state, <code>false</code> otherwise.
	 * 
	 * @return whether the wire-frame has been rotated.
	 */
	public boolean hasBeenRotated() {
		return rotated;
	}
	
	/**
	 * Returns the currently drawn wire-frame. This wire-frame will not reflect
	 * any transformations applied to the view.
	 * 
	 * @return the wire-frame currently being drawn.
	 * @see #setWireFrame(ObjWireFrame)
	 */
	public ObjWireFrame getWireFrame() {
		return wireFrame;
	}
	
	/**
	 * Sets the wire-frame to draw. Does not reset any of the current
	 * transformations.
	 * 
	 * @param wireFrame the wire-frame to draw.
	 */
	public void setWireFrame(ObjWireFrame wireFrame) {
		this.wireFrame = wireFrame;
		
		reconstructBuffer();
		invalidate();
	}
	
	/**
	 * Resets this <code>WireFrameView</code>. Changes the rotation to their
	 * default rotations.
	 * 
	 * @see #DEFAULT_X_ROTATION
	 * @see #DEFAULT_Y_ROTATION
	 */
	public void reset() {
		rotX = DEFAULT_X_ROTATION;
		rotY = DEFAULT_Y_ROTATION;
		rotZ = 0;
		invalidate();
		
		rotated = false;
	}

	/**
	 * Returns the underlying canvas surface that this <code>WireFrameView
	 * </code> draws on. Drawing on this will result in an immediate change to
	 * the resultant view.
	 * 
	 * @return the underlying canvas surface.
	 */
	public Surface getSurface() {
		return canvas;
	}
	
	/**
	 * Changes the line color used to draw the wire-frame. {@link #draw()} will
	 * need to be called manually for the changes to take effet.
	 * 
	 * @param lineColor the new line color to draw the wire-frame in.
	 * @see #getLineColor()
	 */
	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
		canvas.setStrokeStyle(lineColor);
		invalidate();
	}

	/**
	 * Returns the line color used to draw the wire-frame.
	 * 
	 * @return the line color used to draw the wire-frame.
	 */
	public Color getLineColor() {
		return lineColor;
	}

	/**
	 * Returns the current x-rotation of this <code>WireFrameView</code>.
	 * The returned number will be between 0 and two pi, inclusive.
	 * 
	 * @return the current x-rotation.
	 */
	public double getRotateX() {
		return rotX;
	}

	/**
	 * Returns the current y-rotation of this <code>WireFrameView</code>.
	 * The returned number will be between 0 and two pi, inclusive.
	 * 
	 * @return the current y-rotation.
	 */
	public double getRotateY() {
		return rotY;
	}

	/**
	 * Returns the current z-rotation of this <code>WireFrameView</code>.
	 * The returned number will be between 0 and two pi, inclusive.
	 * 
	 * @return the current z-rotation.
	 */
	public double getRotateZ() {
		return rotZ;
	}

	/**
	 * Changes the x-rotation by the supplied amount. Positive values rotate
	 * the y-axis towards the z-axis.
	 * 
	 * @param dx the amount to increase the x-rotation by.
	 * @see #setRotateX(double)
	 */
	public void rotateX(double dx) {
		rotX += dx; // Rotate
		
		// Clamp
		while ( rotX < 0 ) {
			rotX += MathHelper.TWO_PI;
		}
		
		while ( rotX > MathHelper.TWO_PI ) {
			rotX -= MathHelper.TWO_PI;
		}

		invalidate();
		rotated = true;
	}

	/**
	 * Changes the y-rotation by the supplied amount. Positive values rotate
	 * the z-axis towards the x-axis.
	 * 
	 * @param dy the amount to increase the y-rotation by.
	 * @see #setRotateY(double)
	 */
	public void rotateY(double dy) {
		rotY += dy; // Rotate
		
		// Clamp
		while ( rotY < 0 ) {
			rotY += MathHelper.TWO_PI;
		}
		
		while ( rotY > MathHelper.TWO_PI ) {
			rotY -= MathHelper.TWO_PI;
		}

		invalidate();
		rotated = true;
	}

	/**
	 * Changes the z-rotation by the supplied amount. Positive values rotate
	 * the x-axis towards the y-axis.
	 * 
	 * @param dz the amount to increase the z-rotation by.
	 * @see #setRotateZ(double)
	 */
	public void rotateZ(double dz) {
		rotZ += dz;
		
		// Clamp
		while ( rotZ < 0 ) {
			rotZ += MathHelper.TWO_PI;
		}
		
		while ( rotZ > MathHelper.TWO_PI ) {
			rotZ -= MathHelper.TWO_PI;
		}

		invalidate();
		rotated = true;
	}
	
	/**
	 * Changes the x- and y-rotations by the supplied amounts. Positive <code>dx
	 * </code> values rotate the y-axis towards the z-axis, and positive
     * <code>dy</code> values rotate the z-axis towards the x-axis.
	 * 
	 * @param dx the amount to increase the x-rotation by.
	 * @param dy the amount to increase the y-rotation by.
	 * @see #setRotate(double, double)
	 * @see #rotateX(double)
	 * @see #rotateY(double)
	 */
	public void rotate(double dx, double dy) {
		rotateX(dx);
		rotateY(dy);
	}

	/**
	 * Changes the x-, y-, and z-rotations by the supplied amounts. Positive
	 * <code>dx</code> values rotate the y-axis towards the z-axis, positive
     * <code>dy</code> values rotate the z-axis towards the x-axis, and positive
     * <code>dz</code> values rotate the x-axis towards the y-axis.
	 * 
	 * @param dx the amount to increase the x-rotation by.
	 * @param dy the amount to increase the y-rotation by.
	 * @param dz the amount to increase the z-rotation by.
	 * @see #setRotate(double, double, double)
	 * @see #rotateX(double)
	 * @see #rotateY(double)
	 * @see #rotateZ(double)
	 */
	public void rotate(double dx, double dy, double dz) {
		rotate(dx, dy);
		rotateZ(dz);
	}

	/**
	 * Set the x-rotation to the supplied values. Positive values rotate
	 * the y-axis towards the z-axis.
	 * 
	 * @param rotX the new x-rotation value.
	 * @see #rotateX(double)
	 */
	public void setRotateX(double rotX) {
		this.rotX = 0;
		rotateX(rotX);
	}

	/**
	 * Set the y-rotation to the supplied values. Positive values rotate
	 * the z-axis towards the x-axis.
	 * 
	 * @param rotY the new y-rotation value.
	 * @see #rotateY(double)
	 */
	public void setRotateY(double rotY) {
		this.rotY = 0;
		rotateY(rotY);
	}

	/**
	 * Set the z-rotation to the supplied values. Positive values rotate
	 * the x-axis towards the y-axis.
	 * 
	 * @param rotZ the new z-rotation value.
	 * @see #rotateZ(double)
	 */
	public void setRotateZ(double rotZ) {
		this.rotZ = 0;
		rotateZ(rotZ);
	}

	/**
	 * Sets the x- and y-rotations to the supplied values. Positive <code>dx
	 * </code> values rotate the y-axis towards the z-axis, and positive
     * <code>dy</code> values rotate the z-axis towards the x-axis.
	 * 
	 * @param rotX the new x-rotation value.
	 * @param rotY the new y-rotation value.
	 * @see #rotate(double, double)
	 * @see #setRotateX(double)
	 * @see #setRotateY(double)
	 */
	public void setRotate(double rotX, double rotY) {
		setRotateX(rotX);
		setRotateY(rotY);
	}

	/**
	 * Sets the x-, y-, and z-rotations to the supplied values. Positive
	 * <code>dx</code> values rotate the y-axis towards the z-axis, positive
     * <code>dy</code> values rotate the z-axis towards the x-axis, and positive
     * <code>dz</code> values rotate the x-axis towards the y-axis.
	 * 
	 * @param rotX the new x-rotation value.
	 * @param rotY the new y-rotation value.
	 * @param rotZ the new z-rotation value.
	 * @see #rotate(double, double, double)
	 * @see #setRotateX(double)
	 * @see #setRotateY(double)
	 * @see #setRotateZ(double)
	 */
	public void setRotate(double rotX, double rotY, double rotZ) {
		setRotate(rotX, rotY);
		setRotateZ(rotZ);
	}

	/**
	 * Draws this <code>WireFrameView</code>'s wire-frame onto the view. Will
	 * only draw if it has been invalidated, whether be changing its
	 * wire-frame or rotation, or by calling {@link #invalidate()}.
	 * 
	 * @see #redraw()
	 */
	@Override
	public boolean draw() {
		if ( dirty ) {
			canvas.clear(); // Clear canvas for drawing.
			
			recomputeBuffer(); // Retransform the points.
			drawBuffer(); // Draw the transformed wire-frame onto the canvas.
			
			dirty = false;
			
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Forces this <code>WireFrameView</code> to redraw itself.
	 * 
	 * @see #draw()
	 */
	@Override
	public void redraw() {
		invalidate();
		draw();
	}

	/**
	 * Invalidates this <code>WireFrameView</code> so that it will be forced
	 * to redraw itself on the next call to {@link #draw()}.
	 * 
	 * @see #redraw()
	 */
	@Override
	public void invalidate() {
		dirty = true;
	}
	
	/**
	 * Draws the transformed wire-frame onto the canvas. Should be preceded by a
	 * call to {@link #recomputeBuffer()}.
	 */
	private void drawBuffer() {
		Point3D view = new Point3D(0, 0, -1.0); // We're looking towards -z.
		
		Face[] faces = wireFrame.faces;
		
		// Arrays.sort() uses MergeSort, which goes too deep for Safari's
		// tastes. Furthermore, the faces should be mostly-sorted each iteration
		// after the first run. Insertion sort, FTW.
		insertionZSort(faces); 
		
		// Draw each face.
		for ( Face face : faces ) {
			int[] vertices = face.getVertices();
			int normal = face.getNormal();
			
			// If face has no normal, or normal is in the direction of view.
			if ( normal == -1 || tNormals[normal].dot(view) >= 0 ) {
				
				//Draw it.
				builder.beginPath();
				
				builder.moveTo(vPoints[vertices[0]]); // Move to first point.
				
				// Draw each line.
				for ( int i = 1; i < vertices.length; i++ ) {
					builder.drawLineTo(vPoints[vertices[i]]);
				}
				
				builder.drawLineTo(vPoints[vertices[0]]); // Close path.
				
				builder.closePath();
				// Fill with transparent black.
				// Occludes lines in the back due to z-sorting.
				// We need to clip since Firefox and Chrome decided that fill
				// should fill the entire canvas.
				canvas.save();
				builder.clip();
				builder.fill();
				builder.stroke(); // Stroke lines.
				canvas.restore();
			}
		}
	}
	
	/**
	 * Z-sorts the faces in place according to their maximum transformed
	 * z-coordinate in ascending order. Uses insertion sort.
	 * 
	 * @param faces the array of faces to sort.
	 */
	private void insertionZSort(Face[] faces) {
		double[] faceMaxZs = new double[faces.length];
		for ( int i = 0; i < faces.length; i++ ) {
			faceMaxZs[i] = findMaxZ(faces[i]);
		}
		
		// Run-of-the-mill insertion sort.
		for ( int i = 0; i < faces.length; i++ ) {
			// Move around both values to keep track of keys.
			Face face = faces[i];
			double maxZ = faceMaxZs[i];
	        
	        int j;
	        for ( j = i; j > 0 && faceMaxZs[j-1] > maxZ; j-- ) {
            	faces[j] = faces[j-1];
            	faceMaxZs[j] = faceMaxZs[j-1];
	        }
	        
	        faces[j] = face;
	        faceMaxZs[j] = maxZ;
		}
	}
	
	/**
	 * Finds the maximum z-coordinate of the supplied face's transformed
	 * vertices. Run-of-the-mill find-maximum algorithm.
	 * 
	 * @param face the face to transform over.
	 * @return the maximum z-coordinate.
	 */
	public double findMaxZ(Face face) {
		double maxZ = Double.NEGATIVE_INFINITY; // Sentinel value.
		
		for ( int vertex : face.getVertices() ) {
			maxZ = Math.max(maxZ, tVertices[vertex].z);
		}
		
		return maxZ;
	}
	
	/**
	 * Rebuilds all buffered values.
	 */
	private void recomputeBuffer() {
		transformPoints();
		//buildGradients();
	}
	
	/**
	 * Rebuilds all of the buffer arrays. Used when changing the wire-frame.
	 * 
	 * @see #setWireFrame(ObjWireFrame)
	 */
	private void reconstructBuffer() {
		tVertices = new Point3D[wireFrame.vertices.length];
		vPoints = new Vector2[tVertices.length];
		tNormals = new Point3D[wireFrame.normals.length];
		
		for ( int i = 0; i < tVertices.length; i++ ) {
			tVertices[i] = new Point3D();
			vPoints[i] = new Vector2();
		}
		
		for ( int i = 0; i < tNormals.length; i++ ) {
			tNormals[i] = new Point3D();
		}
	}
	
	/**
	 * Transforms all of the vertices and normals in the wire-frame. Simply
	 * rotates them and projects them. The resultant transformed vertices,
	 * normals, and projected vertices are stored in <code>tVertices</code>,
	 * <code>tNormals</code> and <code>vPoints</code>, respectively.
	 * 
	 * @see #generateRotMatrix()
	 * @see #project(Point3D, Vector2)
	 */
	private void transformPoints() {
		// Sets up rotMatrix according to the current rotations.
		generateRotMatrix();
		
		// Multiply each point by the rotation matrix to transform.
		Point3D[] vertices = wireFrame.vertices;
		for ( int i = 0; i < vertices.length; i++ ) {
			tVertices[i].setPoint(vertices[i]);
			tVertices[i].multiply(rotMatrix); // Rotate
			
			project(tVertices[i], vPoints[i]);
		}
		
		Point3D[] normals = wireFrame.normals;
		for ( int i = 0; i < normals.length; i++ ) {
			tNormals[i].setPoint(normals[i]);
			tNormals[i].multiply(rotMatrix); // Rotate
		}
	}
	
	/**
	 * Computes <code>rotMatrix</code> so that multiplying points by it rotates
	 * them according to this </code>WireFrameView</code>'s current
	 * rotations.
	 * 
	 * Applies the z-rotation, then the y-rotation, and then the x-rotation.
	 */
	private void generateRotMatrix() {
		// M = Ry * Rx * Rz.

		double sinX = Math.sin(rotX);
		double cosX = Math.cos(rotX);
		
		double sinY = Math.sin(rotY);
		double cosY = Math.cos(rotY);

		double sinZ = Math.sin(rotZ);
		double cosZ = Math.cos(rotZ);
		
		// Warning: Math
		rotMatrix.m11 =  cosY*cosZ;
		rotMatrix.m12 = -cosY*sinZ;
		rotMatrix.m13 =  sinY;

		rotMatrix.m21 =  sinX*sinY*cosZ + cosX*sinZ;
		rotMatrix.m22 = -sinX*sinY*sinZ + cosX*cosZ;
		rotMatrix.m23 = -sinX*cosY;

		rotMatrix.m31 = -cosX*sinY*cosZ + sinX*sinZ;
		rotMatrix.m32 =  cosX*sinY*sinZ + sinX*cosZ;
		rotMatrix.m33 =  cosX*cosY;
	}
	
	/**
	 * Projects the point in 3D wire-frame space into 2D view space. Standard
	 * orthogonal projection: just drops the z-coordinate. y is negated so that
	 * because +y is up in wire-frame space, but -y is up in view space.
	 * 
	 * @param p3 the 3D point to transform.
	 * @param p2 the 2D point to store the result in.
	 */
	private void project(Point3D p3, Vector2 p2) {
		// Standard orthogonal projection. (y => -y so that +y is up.)
		
		p2.setX(p3.x);
		p2.setY(-p3.y);
	}

}
