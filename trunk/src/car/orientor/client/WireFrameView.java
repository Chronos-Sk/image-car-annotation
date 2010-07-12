package car.orientor.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Gradient;
import gwt.g2d.client.graphics.LinearGradient;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.math.MathHelper;
import gwt.g2d.client.math.Matrix;
import gwt.g2d.client.math.Vector2;

import car.shared.math.Matrix3D;
import car.shared.math.Point3D;

import com.google.gwt.user.client.ui.FocusPanel;

@Deprecated
public class WireFrameView extends FocusPanel implements Drawable {
	
	private WireFrame wireFrame = null;

	private Matrix baseTransform; // Transformation so that y = [-1.0, 1.0].
	private Matrix3D rotMatrix;
	
	private double rotX = Math.PI/8;
	private double rotY = MathHelper.PI_OVER_4;
	private double rotZ = 0; //MathHelper.PI_OVER_4;
	
	private Point3D[] tPoints; // Transformed (rotated) points.
	private Gradient[] grads;
	private boolean dirty = true; // Need to retransform points?
	
	private Surface canvas;
	private DirectShapeRenderer builder; // Allows custom path drawing.
	
	private Color bgColor = new Color(220, 220, 220);
	
	private Color backLineColor = new Color(0, 0, 0, 0.0);
	private Color frontLineColor = new Color(0, 0, 0, 1.0);
	
	private double backLineZ = 1.0;
	private double frontLineZ = -0.75;
	
	private Color lineColor = new Color(0, 0, 0);
	
	public WireFrameView(WireFrame wireFrame, int width, int height) {
		this.wireFrame = wireFrame;
		reconstructBuffer();
		
		canvas = new Surface(width, height);
		builder = new DirectShapeRenderer(canvas);
		
		baseTransform = new Matrix();
		// Scale uniformly so that y = [0.0, 2.0].
		baseTransform.setScale(height/2.0, height/2.0);
		// Translate uniformly so that y = [-1.0, 1.0]
		baseTransform = baseTransform.translate(((double) width)/height, 1);
		
		// If width == height, x = [-1.0, 1.0]. Regardless, x = [-width/height, width/height].
		
		// Line sizes get transformed as well, so we'll have to reset them to 1 px.
		canvas.setLineWidth(2.0/height);
		
		rotMatrix = new Matrix3D();
		
		canvas.setTransform(baseTransform);
		
		setWidget(canvas);
	}
	
	public WireFrame getWireFrame() {
		return wireFrame;
	}
	
	public void setWireFrame(WireFrame wireFrame) {
		this.wireFrame = wireFrame;
		
		reconstructBuffer();
		dirty = true;
	}
	
	public void reset() {
		rotX = Math.PI/8;
		rotY = MathHelper.PI_OVER_4;
		rotZ = 0;
		dirty = true;
	}
	
	public Surface getSurface() {
		return canvas;
	}
	
	public void setSize(int width, int height) {
		canvas.setSize(width, height);
	}
	
	public int getWidth() {
		return canvas.getWidth();
	}

	public int getHeight() {
		return canvas.getHeight();
	}

	public void setBGColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	public Color getBGColor() {
		return bgColor;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public Color getLineColor() {
		return lineColor;
	}
	
	public void rotateX(double dx) {
		rotX += dx; // Rotate
		
		// Clamp
		while ( rotX < 0 ) {
			rotX += MathHelper.TWO_PI;
		}
		
		while ( rotX > MathHelper.TWO_PI ) {
			rotX -= MathHelper.TWO_PI;
		}
		
		dirty = true;
	}
	
	public void rotateY(double dy) {
		rotY += dy; // Rotate
		
		// Clamp
		while ( rotY < 0 ) {
			rotY += MathHelper.TWO_PI;
		}
		
		while ( rotY > MathHelper.TWO_PI ) {
			rotY -= MathHelper.TWO_PI;
		}
		
		dirty = true;
	}
	
	public void rotateZ(double dz) {
		rotZ += dz;
		
		// Clamp
		while ( rotZ < 0 ) {
			rotZ += MathHelper.TWO_PI;
		}
		
		while ( rotZ > MathHelper.TWO_PI ) {
			rotZ -= MathHelper.TWO_PI;
		}
		
		dirty = true;
	}
	
	public void rotate(double dx, double dy) {
		rotateX(dx);
		rotateY(dy);
	}

	public void rotate(double dx, double dy, double dz) {
		rotate(dx, dy);
		rotateZ(dz);
	}
	
	public void setRotateX(double rotX) {
		this.rotX = 0;
		rotateX(rotX);
	}
	
	public void setRotateY(double rotY) {
		this.rotY = 0;
		rotateY(rotY);
	}
	
	public void setRotateZ(double rotZ) {
		this.rotZ = 0;
		rotateZ(rotZ);
	}
	
	public void setRotate(double rotX, double rotY) {
		setRotateX(rotX);
		setRotateY(rotY);
	}
	
	public void setRotate(double rotX, double rotY, double rotZ) {
		setRotate(rotX, rotY);
		setRotateZ(rotZ);
	}
	
	
	public boolean draw() {
		if ( dirty ) {
			canvas.clear();
			recomputeBuffer();
			drawBuffer();
			
			dirty = false;
			
			return true;
		} else {
			return false;
		}
	}
	
	public void redraw() {
		dirty = true;
		draw();
	}
	
	@Override
	public void invalidate() {
		dirty = true;
	}

	private void drawBuffer() {
//		builder.beginPath();
		
		int[] lines = wireFrame.lines;
		Vector2 from = new Vector2();
		Vector2 to = new Vector2();
		
		for ( int i = 0; i < lines.length; i += 2 ) {
			builder.beginPath();
			
			// Project Point3D's onto two dimensions.
			project(tPoints[lines[i]], from);
			project(tPoints[lines[i+1]], to);

			canvas.setStrokeStyle(grads[i/2]);
			
			// Draw line.
			builder.moveTo(from);
			builder.drawLineTo(to);
			
			builder.closePath();
			builder.stroke();
		}
		
//		Builder.closePath();
//		Builder.stroke();
	}
	
	private void recomputeBuffer() {
		transformPoints();
		buildGradients();
	}
	
	public void reconstructBuffer() {
		tPoints = new Point3D[wireFrame.points.length];
		
		for ( int i = 0; i < tPoints.length; i++ ) {
			tPoints[i] = new Point3D();
		}
		
		grads = new Gradient[wireFrame.lines.length / 2];
		
		dirty = true;
	}
	
	private void transformPoints() {
		generateRotMatrix();
		
		// Multiply each point by the rotation matrix to transform.
		Point3D[] points = wireFrame.points;
		for ( int i = 0; i < points.length; i++ ) {
			tPoints[i].setPoint(points[i]);
			tPoints[i].multiply(rotMatrix);
		}
	}
	
	private void buildGradients() {
		int[] lines = wireFrame.lines;
		
		Point3D from;
		Point3D to;
		
		Vector2 tFrom = new Vector2();
		Vector2 tTo = new Vector2();

		double zDist = frontLineZ-backLineZ;
		
		for ( int i = 0; i < lines.length; i += 2 ) {
			from = tPoints[lines[i]];
			to = tPoints[lines[i+1]];
			
			project(from, tFrom);
			project(to, tTo);
			
			Color backColor;
			Color frontColor;
			
			if ( i == 22 ) {
				backColor = new Color(0, 255, 0);
				frontColor = new Color(0, 255, 0);
			} else {
				backColor = Color.lerp(frontLineColor, backLineColor, (from.z - backLineZ)/zDist);
				frontColor = Color.lerp(frontLineColor, backLineColor, (to.z - backLineZ)/zDist);
			}
			
			Gradient grad = new LinearGradient(tFrom, tTo);
			grad.addColorStop(0, backColor);
			grad.addColorStop(1, frontColor);
			grads[i/2] = grad;
		}
	}
	
	// Original
//	private void generateRotMatrix() {
//		// M = Ry * Rx.
//		
//		rotMatrix.m11 = Math.cos(rotY);
//		rotMatrix.m12 = Math.sin(rotY) * Math.sin(rotX);
//		rotMatrix.m13 = Math.sin(rotY) * Math.cos(rotX);
//
//		rotMatrix.m21 = 0;
//		rotMatrix.m22 = Math.cos(rotX);
//		rotMatrix.m23 = -Math.sin(rotX);
//
//		rotMatrix.m31 = -Math.sin(rotY);
//		rotMatrix.m32 = Math.cos(rotY) * Math.sin(rotX);
//		rotMatrix.m33 = Math.cos(rotY) * Math.cos(rotX);
//	}
	
	// Roll bar
//	private void generateRotMatrix() {
//		// M = Ry * Rx * Rz.
//
//		double sinX = Math.sin(rotX);
//		double cosX = Math.cos(rotX);
//		
//		double sinY = Math.sin(rotY);
//		double cosY = Math.cos(rotY);
//		
//		double sinZ = Math.sin(rotZ);
//		double cosZ = Math.cos(rotZ);
//		
//		rotMatrix.m11 =  cosY*cosZ + sinX*sinY*sinZ;
//		rotMatrix.m12 = -cosY*sinZ + sinX*sinY*cosZ;
//		rotMatrix.m13 =  sinY*cosX;
//
//		rotMatrix.m21 = cosX*sinZ;
//		rotMatrix.m22 = cosX*cosZ;
//		rotMatrix.m23 = -sinX;
//
//		rotMatrix.m31 = -sinY*cosZ + sinX*cosY*sinZ;
//		rotMatrix.m32 =  sinY*sinZ + sinX*cosY*cosZ;
//		rotMatrix.m33 =  cosX*cosY;
//	}

	// Vertical lines
//	private void generateRotMatrix() {
//		// M = Ry * Rx * Rz.
//
//		double sinX = Math.sin(rotX);
//		double cosX = Math.cos(rotX);
//		
//		double sinY = Math.sin(rotY);
//		double cosY = Math.cos(rotY);
//		
//		rotMatrix.m11 =  cosY;
//		rotMatrix.m12 =  0;
//		rotMatrix.m13 =  sinY;
//
//		rotMatrix.m21 =  sinX*sinY;
//		rotMatrix.m22 =  cosX;
//		rotMatrix.m23 = -sinX*cosY;
//
//		rotMatrix.m31 = -cosX*sinY;
//		rotMatrix.m32 =  sinX;
//		rotMatrix.m33 =  cosX*cosY;
//	}
	
	// Both
	private void generateRotMatrix() {
		// M = Ry * Rx * Rz.

		double sinX = Math.sin(rotX);
		double cosX = Math.cos(rotX);
		
		double sinY = Math.sin(rotY);
		double cosY = Math.cos(rotY);

		double sinZ = Math.sin(rotZ);
		double cosZ = Math.cos(rotZ);
		
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
	
	private void project(Point3D p3, Vector2 p2) {
		// Standard orthogonal projection. (y => -y so that +y is up.)
		
		p2.setX(p3.x);
		p2.setY(-p3.y);
	}

}
