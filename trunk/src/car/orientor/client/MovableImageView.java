package car.orientor.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.math.Rectangle;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * This class allows the viewing of an image and a rectangle drawn upon the
 * image. This class has functions allowing the view to be scaled and translated
 * arbitrarily.
 * 
 * @author Joshua Little
 */
public class MovableImageView extends FocusPanel implements Drawable {
	private final double ZOOM_BASE = 2; // Base number for exponential zoom.
	private final double LOG_BASE = Math.log(ZOOM_BASE);
	
	private Image image; // Image to draw.
	private ImageElement imageElement; // ImageElement of image.
	
	private Rectangle rect; // Rectangle to draw. (Around car generally.)
	private double offX, offY; // Translation due to dragging.
	
	private Surface canvas; // Canvas to draw everything on.
	
	boolean dirty; // Needs to be redrawn?
	
	// Color to draw rectangle in.
	private Color rectColor = new Color(0, 0, 255);
	
	// Minimum and maximum zooms (mapped to zoom-factors 0.0 and 1.0, resp.)
	private double minZoom = 0.5;
	private double maxZoom = 4.0;
	
	// For convenience, log of minZoom and maxZoom in our base.
	private double minZoomLog = Math.log(minZoom) / LOG_BASE;
	private double maxZoomLog = Math.log(maxZoom) / LOG_BASE;
	
	private double defaultZoom = 1.0; // Starting zoom (not zoom-factor).
	
	private double zoom; // Current zoom.
	
	// Dimensions of canvas.
	private double width;
	private double height;
	
	/**
	 * Creates an instance of <code>MovableImageView</code>
	 * 
	 * @param image image to be drawn (can be <code>null</code>).
	 * @param rect rectangle to draw ontop of image.
	 * @param width width of canvas.
	 * @param height height of canvas.
	 */
	public MovableImageView(Image image, Rectangle rect, int width, int height){
		this.image = image;
		this.rect = rect;
		this.width = width;
		this.height = height;
		
		if ( image != null ) {
			imageElement = ImageElement.as(image.getElement());
		}
		
		canvas = new Surface(width, height);
		canvas.setStrokeStyle(rectColor); // Sets the rectangle color.

		reset(); // Set default offset and zoom.
		
		setWidget(canvas); // Sets the canvas to be the drawn widget.
		
		invalidate();
	}
	
	/**
	 * Sets the image to draw in the background.
	 * 
	 * @param image the image to draw.
	 * @see #getImage()
	 */
	public void setImage(Image image) {
		this.image = image;
		imageElement = ImageElement.as(image.getElement());

		invalidate();
	}
	
	/**
	 * Returns the image being drawn in the background.
	 * 
	 * @return the image being drawn.
	 * @see #setImage(Image)
	 */
	public Image getImage() {
		return image;
	}
	
	/**
	 * Sets the new x- and y-offsets for the view. Positive values for <code>x
	 * </code> and <code>y</code> move the image and rectangle down and right,
	 * respectively.
	 * 
	 * @param x the new x-offset.
	 * @param y the new y-offset.
	 * @see #getXOffset()
	 * @see #getYOffset()
	 */
	public void setOffset(double x, double y) {
		offX = -x; offY = -y;
		zoom = 1.0;
		
		// Recreate transformation matrix.
		setAbsoluteZoom(getZoom());
	}
	
	/**
	 * Returns the current x-offset. Positive values correspond to the image
	 * moving to the right.
	 * 
	 * @return the current x-offset.
	 */
	public double getXOffset() {
		return -offX;
	}

	/**
	 * Returns the current y-offset. Positive values correspond to the image
	 * moving down.
	 * 
	 * @return the current y-offset.
	 */
	public double getYOffset() {
		return -offY;
	}
	
	/**
	 * Sets the rectangle to draw ontop of the image.
	 * 
	 * @param rect the rectangle to draw.
	 * @see #getRectangle()
	 */
	public void setRectangle(Rectangle rect) {
		this.rect = rect;
	}

	/**
	 * Returns the rectangle to draw ontop of the image.
	 * 
	 * @return the rectangle being drawn.
	 * @see #setRectangle(Rectangle)
	 */
	public Rectangle getRectangle() {
		return rect;
	}

	/**
	 * Sets the zoom which maps to a zoom-factor of 0.0.
	 * 
	 * @param z the new minimum zoom.
	 * @see #getMinimumZoom()
	 */
	public void setMinimumZoom(double z) {
		minZoom = z;
		minZoomLog = Math.log(minZoom) / LOG_BASE;
	}

	/**
	 * Returns the zoom which maps to a zoom-factor of 0.0.
	 * 
	 * @return the minimum zoom.
	 * @see #setMinimumZoom(double)
	 */
	public double getMinimumZoom() {
		return minZoom;
	}

	/**
	 * Sets the zoom which maps to a zoom-factor of 1.0.
	 * 
	 * @param z the new maximum zoom.
	 * @see #getMaximumZoom()
	 */
	public void setMaximumZoom(double z) {
		maxZoom = z;
		maxZoomLog = Math.log(maxZoom) / LOG_BASE;
	}

	/**
	 * Returns the zoom which maps to a zoom-factor of 1.0.
	 * 
	 * @return the maximum zoom.
	 * @see #setMaximumZoom(double)
	 */
	public double getMaximumZoom() {
		return maxZoom;
	}
	
	/**
	 * Changes the default zoom to the specified value. This value is used when
	 * {@link #resetZoom()} or {@link #reset()} is called.
	 * 
	 * @param z the new default zoom.
	 * @see #getDefaultZoom()
	 */
	public void setDefaultZoom(double z) {
		defaultZoom = z;
	}

	/**
	 * Returns the current default zoom. This value is used when
	 * {@link #resetZoom()} or {@link #reset()} is called.
	 * 
	 * @see #setDefaultZoom(double)
	 */
	public double getDefaultZoom() {
		return defaultZoom;
	}
	
	/**
	 * Sets the current zoom applied to the image and rectangle. The supplied
	 * value should be a value between 0.0 and 1.0, inclusive. This factor is
	 * then ran through an exponential scale and transformed into a value
	 * between the minimum zoom and maximum zoom. This scale is monotonically
	 * increasing.
	 * 
	 * @param t the new zoom factor.
	 * @see #getZoom()
	 * @see #setAbsoluteZoom(double)
	 * @see #getZoomFactor()
	 */
	public void setZoom(double t) {
		setAbsoluteZoom(calcZoom(t));
	}
	
	/**
	 * Sets the absolute zoom applied to the image and rectangle. The supplied
	 * value is not run through the exponential scale. As a result, calling
	 * {@link #getZoom()} immediately after is guaranteed to return the value
	 * supplied to this function.
	 * 
	 * @param newZoom the new absolute zoom to apply.
	 * @see #setZoom(double)
	 * @see #getZoom()
	 * @see #getZoomFactor()
	 */
	public void setAbsoluteZoom(double newZoom) {
		// Reset transformation to identity.
		canvas.setTransform(1, 0, 0, 1, 0, 0);
		canvas.scale(newZoom); // Zoom on origin.
		canvas.translate(offX/newZoom, offY/newZoom); // Translate back.
		
		// Try (and fail) to keep center of view the same.
		translate(width/2*(newZoom - zoom), height/2*(newZoom - zoom));
		
		zoom = newZoom;
		invalidate();
	}
	
	/**
	 * Returns the effective zoom applied to the image and rectangle.
	 * 
	 * @return the effective zoom.
	 * @see #setZoom(double)
	 * @see #setAbsoluteZoom(double)
	 * @see #getZoomFactor()
	 */
	public double getZoom() {
		return zoom;
	}
	
	/**
	 * Returns a zoom-factor which will map to the supplied zoom when ran
	 * through the exponential scale.
	 * 
	 * @param z the zoom to transform.
	 * @return the zoom-factor representing the supplied zoom.
	 */
	private double calcZoomFactor(double z) {
		// Simply the inverse of calcZoom(double).
		return (Math.log(z) / LOG_BASE - minZoomLog) / (maxZoomLog-minZoomLog);
	}
	
	/**
	 * Returns the zoom represented by the zoom-factor.
	 * 
	 * @param t the zoom-factor.
	 * @return the absolute zoom the zoom-factor represents.
	 */
	private double calcZoom(double t) {
		// Linearly scales t into the range defined by the logs of the minimum
		// and maximum values, and then raises the base to the resultant number.
		return Math.pow(ZOOM_BASE, t * (maxZoomLog - minZoomLog) + minZoomLog);
	}
	
	/**
	 * Returns the zoom-factor that will map to the current zoom. If one calls
	 * <code>setZoom(getZoomFactor())</code>, the only change that might result
	 * would be due to rounding error.
	 * 
	 * @return the zoom-factor mapping to the current zoom.
	 * @see #setZoom(double)
	 * @see #setAbsoluteZoom(double)
	 * @see #getZoom()
	 */
	public double getZoomFactor() {
		return calcZoomFactor(zoom); // We don't store the zoom-factor.
	}
	
	/**
	 * Returns the underlying canvas surface that this <code>MovableImageView
	 * </code> draws on. Drawing on this will result in an immediate change to
	 * the resultant view.
	 * 
	 * @return the underlying canvas surface.
	 */
	public Surface getSurface() {
		return canvas;
	}
	
	/**
	 * Resets this <code>MovableImageView</code> to its default state. Calls
	 * both {@link #resetOffset()} and {@link #resetZoom()}.
	 */
	public void reset() {
		resetOffset();
		resetZoom();

		invalidate();
	}
	
	/**
	 * Recenters the view on this <code>MovableImageView</code>'s rectangle if
	 * it has one. If the rectangle is null, resets offset to zero.
	 * 
	 * @see #reset()
	 * @see #resetZoom()
	 */
	public void resetOffset() {
		offX = -rect.getX() + -rect.getWidth()/2  + width/2;
		offY = -rect.getY() + -rect.getHeight()/2 + height/2;
		zoom = 1.0;
		
		// Recreate transformation matrix.
		setAbsoluteZoom(getZoom());
	}
	
	/**
	 * Resets the zoom to the {@linkplain #getDefaultZoom() default zoom}.
	 * 
	 * @see #reset()
	 * @see #resetOffset()
	 */
	public void resetZoom() {
		setAbsoluteZoom(defaultZoom);
		invalidate();
	}
	
	/**
	 * Draws this <code>MovableImageView</code>'s image and rectangle onto the
	 * view. Will only draw if it has been invalidated, whether be changing its
	 * zoom or offset, or by calling {@link #invalidate()}.
	 * 
	 * @see #redraw()
	 */
	@Override
	public boolean draw() {
		if ( dirty ) {
			canvas.clear(); // Clear current view for drawing.
			
			// Draw stuff.
			if ( image != null ) {
				canvas.drawImage(imageElement, 0, 0);
			}
			
			if ( rect != null ) {
				// The rectangle color is set in the constructor.
				canvas.strokeRectangle(rect);
			}
			
			dirty = false;
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Forces this <code>MovableImageView</code> to redraw itself.
	 * 
	 * @see #draw()
	 */
	@Override
	public void redraw() {
		invalidate();
		draw();
	}
	
	/**
	 * Invalidates this <code>MovableImageView</code> so that it will be forced
	 * to redraw itself on the next call to {@link #draw()}.
	 * 
	 * @see #redraw()
	 */
	public void invalidate() {
		dirty = true;
	}
	
	/**
	 * Changes this <code>MovableImageView</code>'s offset by the supplied
	 * amounts. Positive values for <code>dx</code> and <code>dy</code> move the
	 * image and rectangle right and down, respectively.
	 * 
	 * @param dx the amount to move right.
	 * @param dy the ammount to move down.
	 */
	public void translate(double dx, double dy) {
		double oldX = offX, oldY = offY;
		
		offX -= dx;
		offY -= dy;
		
		canvas.translate((offX - oldX)/zoom, (offY - oldY)/zoom); // Translate back.

		invalidate();
	}
	
}
