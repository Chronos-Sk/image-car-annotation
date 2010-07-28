package car.viewer.client;

import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.graphics.canvas.CanvasElement;
import gwt.g2d.client.math.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import car.shared.input.Slider;
import car.shared.views.Drawable;
import car.shared.views.MovableImageMouseHandler;
import car.shared.views.MovableImageView;
import car.shared.views3d.WireFrameConfig;
import car.shared.views3d.WireFrameView;
import car.shared.views3d.obj.ObjWireFrame;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point and enclosing <code>Widget</code> for the Car Orientor project.
 * 
 * @author Joshua Little
 */
public class CarViewer extends FocusPanel implements EntryPoint, Drawable {
	static {
		Car.export(); // Export the native Car interface.
	}
	
	// Containing div id.
	private static final String CONTAINER_NAME = "carviewer-container";
	// Name of parameter used for image URL.
	private static final String IMAGE_PARAM = "img";
	
	private static final int DEFAULT_MINIVIEW_SIZE = 150;
	
	private WireFrameConfig config; // Holds wire-frame information.
	
	private String imageURL; // URL to image to annotate.
	private Image image; // Image to annotate.
	private Rectangle carRect; // Rectangle around car to annotate.
	
	private int viewWidth = 330; // Width for each view.
	private int viewHeight = 330; // Height for each view.
	
	private int miniviewSize = DEFAULT_MINIVIEW_SIZE;

	private Panel container; // Main container for this frame.

	// Input controls.
	private Slider zoomSlider;
	private Button resetButton;
	
	// Panels showing wire-frame and image.
	private WireFrameView wireFrame = null;
	private MovableImageView view = null;
	
	// Maps cars to miniviews.
	private Map<Car, MovableImageView> miniviews;
	
	// List of cars to draw on the view.
	private ArrayList<Car> cars;
	
	/**
	 * Exports the functions that should be available to hand-written
	 * JavaScript. Should be called before {@link #onModuleLoad()} returns.
	 */
	private native void exportFunctions() /*-{
		var _this = this;
		
		$wnd.CarViewer = new Object();
		$wnd.CarViewer.addCar = $entry(function(car, miniview) {
			_this.@car.viewer.client.CarViewer::addCar(Lcar/viewer/client/Car;Ljava/lang/String;)(car.carInst, miniview);
		});
		$wnd.CarViewer.redraw = $entry(function() {
			_this.@car.viewer.client.CarViewer::redraw()();
		});
		$wnd.CarViewer.setMiniview = $entry(function(car,id) {
			_this.@car.viewer.client.CarViewer::setMiniview(Lcar/viewer/client/Car;Ljava/lang/String;)(car,id);
		});
		$wnd.CarViewer.drawMiniview = $entry(function(car) {
			_this.@car.viewer.client.CarViewer::drawMiniview(Lcar/viewer/client/Car;)(car);
		});
	}-*/;

	/**
	 * Calls the native JavaScript function <code>$wnd.afterCarViewerLoad()
	 * </code>, if it exists.
	 */
	private native void fireAfterModuleLoad() /*-{
		if ( $wnd.afterCarViewerLoad ) {
			$wnd.afterCarViewerLoad();
		}
	}-*/;

	/**
	 * Gets the configuration name defined in the global JavaScript variable
	 * "carviewer_config", or <code>null</code> if the variable
	 * is <code>null</code> or <code>undefined</code>.
	 * 
	 * @return the supplied configuration file name, or <code>null</code>.
	 */
	public native String readConfigName() /*-{
		return $wnd.carviewer_config;
	}-*/;

	/**
	 * Gets the image URL defined in the global JavaScript variable
	 * "carviewer_image", or <code>null</code> if the variable
	 * is <code>null</code> or <code>undefined</code>.
	 * 
	 * @return the supplied image URL, or <code>null</code>.
	 */
	public native String readImageURL() /*-{
		return $wnd.carviewer_image;
	}-*/;

	/**
	 * Gets the miniview size defined in the global JavaScript variable
	 * "carviewer_miniview_size" as an integer, or <code>-1</code> if the
	 * variable is <code>null</code>, <code>undefined</code>, or not a number.
	 * 
	 * @return the supplied miniview size, or <code>-1</code>.
	 */
	public native int readMiniviewSize() /*-{
		if ( typeof($wnd.carviewer_miniview_size) == "number" ) {
			return Math.floor($wnd.carviewer_miniview_size);
		} else {
			return -1;
		}
	}-*/;
	
	/**
	 * Creates a new instance of <code>CarViewer</code>.
	 */
	public CarViewer() {
		miniviews = new HashMap<Car, MovableImageView>();
		cars = new ArrayList<Car>();
		
		int miniviewSize = readMiniviewSize();
		if ( miniviewSize != -1 ) {
			// We were supplied one via JavaScript.
			this.miniviewSize = miniviewSize;
		}
	}
	
	/**
	 * Sets the URL used to load the image to annotate. Must be set before
	 * onLoad() is called.
	 * 
	 * @param imageURL the new image URL.
	 */
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	/**
	 * Sets the URL used to load the rectangle around the car to annotate. Must
	 * be set before onLoad() is called.
	 * 
	 * @param carRect the new rectangle.
	 */
	public void setCarRectangle(Rectangle carRect) {
		this.carRect = carRect;
		draw();
	}
	
	/**
	 * Adds the specified car to this <code>CarViewer</code>. If
	 * <code>miniviewId</code> is supplied, this function will attempt to build
	 * a miniview of the car and add it to the 
	 * 
	 * @param car
	 * @param miniviewId
	 */
	public void addCar(Car car, String miniviewId) {
		if ( car == null ) {
			throw new NullPointerException("Cannot add null car to CarViewer.");
		}
		
		GWT.log("Adding car: " + car);
		
		cars.add(car);
		
		if ( miniviewId != null ) {
			setMiniview(car, miniviewId);
		}
		
		redraw();
	}
	
	/**
	 * Builds a miniview for the car and places it in the element with the
	 * specified ID.
	 * 
	 * @param car the car to build a miniview for.
	 * @param miniviewId the ID of the element to add the miniview to.
	 * @return the miniview
	 */
	private MovableImageView buildMiniview(Car car, String miniviewId) {
		MovableImageView miniview = new MovableImageView(
				view.getImage(), null, miniviewSize, miniviewSize); 
		RootPanel.get(miniviewId).add(miniview);
		
		drawMiniview(car, miniview);
		
		return miniview;
	}
	
	/**
	 * Draws the miniview for the specified car.
	 * 
	 * @param car the car to draw the miniview for.
	 */
	public void drawMiniview(Car car) {
		drawMiniview(car, miniviews.get(car));
	}
	
	/**
	 * Draws the miniview for the specified car onto the specified
	 * {@link car.shared.views.MovableImageView}.
	 * 
	 * @param car the car to draw the miniview for.
	 * @param miniview the view to draw the miniview on.
	 */
	private void drawMiniview(Car car, MovableImageView miniview) {
		double zoom = 0.5 / computeViewScale(car.getScale());
		double offX = car.getPositionX()-view.getXOffset()-miniviewSize/zoom/2;
		double offY = car.getPositionY()-view.getYOffset()-miniviewSize/zoom/2;

		miniview.setZoom(zoom);
		miniview.setOffset(offX, offY);
		miniview.draw();
		drawCarOn(car, miniview);
	}
	
	/**
	 * Sets the miniview for the specified car, removing the old one if it
	 * exists.
	 * 
	 * @param car the car to set the miniview for.
	 * @param miniviewId the new containing element's ID.
	 */
	public void setMiniview(Car car, String miniviewId) {
		MovableImageView oldView = miniviews.get(car);
		
		// If there's an old one, remove it.
		if ( oldView != null ) {
			oldView.removeFromParent();
		}
		
		// Build the new miniview.
		MovableImageView miniview = buildMiniview(car, miniviewId);
		miniviews.put(car, miniview);
	}
	
	/**
	 * Converts the generic car-scale into one specific to this
	 * <code>Widget</code>'s {@link car.shared.views3d.WireFrameView} and image.
	 * 
	 * @param carScale the car scale to convert.
	 * @return the scale in view-space.
	 */
	private double computeViewScale(double carScale) {
		// The scale gets inverted between CarOrientor and here (converting
		// image-scale to car-scale) so it's still the same multiplicand.
		return carScale * viewHeight / view.getImage().getHeight();
	}
	
	/**
	 * This is the entry point method. Called after JavaScript is loaded and
	 * environment is set up.
	 */
	public void onModuleLoad() {
		exportFunctions();
		
		String url = readImageURL();

		// If a url wasn't specified in the host HTML...
		if ( url == null ) {
			Map<String,List<String>> paramMap=Window.Location.getParameterMap();
			// Look for one in a GET parameter.
			List<String> urls = paramMap.get(IMAGE_PARAM);
			
			if ( urls != null ) {
				url = urls.get(0);
			}
		}

		GWT.log("Image URL: " + url);
		setImageURL(url);
		
		String configName = readConfigName();
		
		if ( configName == null ) {
			config = WireFrameConfig.get(); // Load and grab default config.
		} else {
			config = new WireFrameConfig(configName);
		}
		
		// Wait for Config to finish loading everything (i.e. wire-frames).
		if ( !config.isLoaded() ) {
			// Will fire a ValueChangeEvent with value <code>true</code> when
			// finished. JavaScript is single-threaded (and event driven), so
			// this code doesn't produce a race condition.
			config.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					configLoaded();
				}
			});
		} else {
			configLoaded();
		}
	}

	/**
	 * Adds this widget to the HTML document. The widget goes inside of the div
	 * with id equal to <code>CONTAINER_NAME</code>. If this class was the
	 * page's entry point, this function will be called automatically once the
	 * configuration file is finished loading.
	 */
	public void configLoaded() {
		getElement().setClassName("carViewer");
		RootPanel.get(CONTAINER_NAME).add(this);
	}
	
	/**
	 * Called when the widget is fully integrated into the DOM tree. Responsible
	 * for building the internal HTML and setting up appropriate handlers. If
	 * this function returns normally, the widget should be fully initialized.
	 */
	public void onLoad() {
		// Explicit width is needed for centering the widget on the page.
		Style style = getElement().getStyle();
		style.setWidth(viewWidth, Unit.PX);
		
		buildWireFrameView();
		buildImageView();
		buildControls();
		
		// Fill up the main container.
		container = new FlowPanel();
		container.add(image);
		container.add(wireFrame);
		// container.add(view);
		
		setWidget(container);
		draw();
		
	}

	/**
	 * Builds the {@link car.shared.views3d.WireFrameView} and sets the
	 * default parameters. Does not add it to the DOM.
	 */
	private void buildWireFrameView() {
		// Load default car wire-frame and set up wire-frame view.
		ObjWireFrame carFrame = config.getDefaultWireFrame(); 
		wireFrame = new WireFrameView(carFrame, viewWidth, viewHeight);
		
		// Set up view CSS style.
		Style wfvStyle = wireFrame.getElement().getStyle();
		// Inline-block allows them to appear side-by-side.
		wfvStyle.setDisplay(Display.NONE);
	}
	
	/**
	 * Builds the {@link MovableImageView} and sets the default parameters. Does
	 * not add it to the DOM.
	 */
	private void buildImageView() {
		// Initialize the image view with a <code>null</code> image.
		//view = new MovableImageView(image, carRect, viewWidth, viewHeight);

		// Load the image and give it to the image view when it's done loading.
		image = new Image(imageURL);
		image.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				view = new MovableImageView(image, carRect, image.getWidth(), image.getHeight());
				view.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
				new MovableImageMouseHandler(CarViewer.this, view);

				// Set the initial slider value depending on the initial zoom.
				double initialZoom = view.getZoomFactor() * zoomSlider.getMaximum();
				zoomSlider.setValue(initialZoom);
				
				container.add(view);
				
				// Add the things that go after the view:
				container.add(new InlineLabel("Zoom: "));
				container.add(zoomSlider);
				container.add(resetButton);
				
				// Set the size so that everything is nice and centered.
				CarViewer.this.setSize(
						"" + image.getWidth() , "" + image.getHeight());
				draw();
				
				fireAfterModuleLoad(); // Call the native after-load hook.
			}
		});
		
		// We're adding it to the DOM to make sure the the browser loads the
		// image when we want it to. Setting display to NONE makes sure that it
		// doesn't directly show up on the page.
		image.getElement().getStyle().setDisplay(Display.NONE);
	}

	/**
	 * Builds the all of the controls and sets the default parameters. Does not
	 * add them to the DOM.
	 */
	private void buildControls() {
		// Resets rotation, translation, zoom, and selected car.
		resetButton = new Button("Reset", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				reset();
			}
		});
		resetButton.setStylePrimaryName("resetButton");
		
		// Controls the image-frame's zoom.
		zoomSlider = new Slider();
		zoomSlider.addValueChangeHandler(new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> event) {
				// Sets the zoom. The view converts this linear scale into an
				// exponential one.
				
				double frac = event.getValue() / zoomSlider.getMaximum();
				view.setZoomFactor(frac);
				draw();
			}
		});

	}
		
	/**
	 * Draws the view and the wire-frames ontop of it. Does not redraw
	 * miniviews.
	 */
	@Override
	public boolean draw() {
		boolean viewRedrawn = false;
		
		if ( view != null ) {
			viewRedrawn = view.draw();
			
			if ( viewRedrawn ) {
				// Redraw wire-frames ontop of it.
				
				for ( Car car : cars ) {
					drawCarOn(car, view);
				}
			}
		}
		
		return viewRedrawn;
	}
	
	/**
	 * Redraws all internal views.
	 */
	@Override
	public void redraw() {
		invalidate();
		draw();
	}

	@Override
	public void invalidate() {
		if ( view != null ) {
			view.invalidate();
		}
	}
	
	public void reset() {
		view.reset();
		zoomSlider.setValue(view.getZoomFactor());
		redraw();
	}
	
	/**
	 * Draws the car onto the supplied surface.
	 * 
	 * @param car the car to draw.
	 * @param view view to draw the wire-frame on.
	 */
	protected void drawCarOn(Car car, MovableImageView view) {
		CanvasElement fromCanvas = wireFrame.getSurface().getCanvas();
		Surface surface = view.getSurface();
		
		// wireFrame.reset();
		wireFrame.setWireFrame(config.getWireFrame(car.getType()));
		wireFrame.setRotate(car.getRotateX(),car.getRotateY(),car.getRotateZ());
		wireFrame.getSurface().setStrokeStyle(car.getColor());
		wireFrame.draw();
		
		surface.save(); // Save the weird transform.
		
		double viewZoom = view.getZoom();
		double carViewScale = computeViewScale(car.getScale());
		
		// Umm, magic?
		double zoom = viewZoom * carViewScale;
		double offX = viewWidth  * carViewScale/2- car.getPositionX() +view.getXOffset();
		double offY = viewHeight * carViewScale/2- car.getPositionY() +view.getYOffset();
		
		surface.setTransform(zoom, 0, 0, zoom, -offX*viewZoom, -offY*viewZoom);
		
		surface.drawImage(fromCanvas, 0, 0, fromCanvas.getWidth(), fromCanvas.getHeight());
		
		surface.restore(); // Restore weird transform.
	}
}
