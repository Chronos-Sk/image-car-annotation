package car.viewer.client;

import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.graphics.canvas.CanvasElement;
import gwt.g2d.client.math.Matrix;
import gwt.g2d.client.math.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import car.shared.input.Slider;
import car.shared.math.Transforms;
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
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point and enclosing <code>Widget</code> for the Car Orientor project.
 * 
 * @author Joshua Little
 */
public class CarViewer extends FocusPanel implements EntryPoint, Drawable {
	static {
		Car.export();
	}
	
	// Containing div id.
	private static final String CONTAINER_NAME = "carviewer-container";
	// Name of parameter used for image URL.
	private static final String IMAGE_PARAM = "img";
	
	private WireFrameConfig config; // Holds wire-frame information.
	
	private String imageURL; // URL to image to annotate.
	private Image image; // Image to annotate.
	private Rectangle carRect; // Rectangle around car to annotate.
	
	private int viewWidth = 330; // Width for each view.
	private int viewHeight = 330; // Height for each view.

	private Panel container; // Main container for this frame.

	// Input controls.
	private Slider zoomSlider;
	private Button resetButton;
	
	// Panels showing wire-frame and image.
	private WireFrameView wireFrame = null;
	private MovableImageView view = null;
	
	private ArrayList<Car> cars = new ArrayList<Car>();
	
	/**
	 * Exports the functions that should be available to hand-written
	 * JavaScript. Should be called before {@link #onModuleLoad()} returns.
	 */
	private native void exportFunctions() /*-{
		var _this = this;
		
		$wnd.CarViewer = new Object();
		$wnd.CarViewer.addCar = $entry(function(car) {
			_this.@car.viewer.client.CarViewer::addCar(Lcar/viewer/client/Car;)(car.carInst);
		});
		$wnd.CarViewer.redraw = $entry(function() {
			_this.@car.viewer.client.CarViewer::redraw()();
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
	 * "carviewer_config", or <code>null</code> if the variable evaluates to
	 * <code>false</code>.
	 * 
	 * @return the supplied configuration file name, or <code>null</code>.
	 */
	public native String getConfigName() /*-{
		return $wnd.carviewer_config;
	}-*/;

	/**
	 * Gets the image URL defined in the global JavaScript variable
	 * "carviewer_image", or <code>null</code> if the variable evaluates to
	 * <code>false</code>.
	 * 
	 * @return the supplied image URL, or <code>null</code>.
	 */
	public native String getImageURL() /*-{
		return $wnd.carviewer_image;
	}-*/;
	
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
	
	public void addCar(Car car) {
		if ( car == null ) {
			throw new NullPointerException("Cannot add null car to CarViewer.");
		}
		
		GWT.log("Adding car: " + car);
		
		cars.add(car);
		redraw();
	}
	
	/**
	 * This is the entry point method. Called after JavaScript is loaded and
	 * environment is set up.
	 */
	public void onModuleLoad() {
		exportFunctions();
		
		String url = getImageURL();

		
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
		
		String configName = getConfigName();
		
		if ( configName == null ) {
			config = WireFrameConfig.get(); // Load and grab default config.
		} else {
			config = new WireFrameConfig(configName);
		}
		
		// Wait for Config to finish loading everything (i.e. wire-frames).
		if ( !config.isLoaded() ) {
			// Whill fire a ValueChangeEvent with value <code>true</code> when
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
		container.add(view);
		
		setWidget(container);
		draw();
		
		fireAfterModuleLoad();
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
		view = new MovableImageView(image, carRect, viewWidth, viewHeight);
		view.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);

		// Load the image and give it to the image view when it's done loading.
		image = new Image(imageURL);
		image.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				view.setImage(image);
				draw();
			}
		});
		
		// We're adding it to the DOM to make sure the the browser loads the
		// image when we want it to. Setting display to NONE makes sure that it
		// doesn't directly show up on the page.
		image.getElement().getStyle().setDisplay(Display.NONE);
		
		// Handles translating the image.
		new MovableImageMouseHandler(CarViewer.this, view);
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
				view.setZoom(frac);
				draw();
			}
		});

		// Set the initial slider value depending on the initial zoom.
		double initialZoom = view.getZoomFactor() * zoomSlider.getMaximum();
		zoomSlider.setValue(initialZoom);
	}
		
	/**
	 * Draws the view and the wire-frames ontop of it.
	 */
	@Override
	public boolean draw() {
		boolean viewRedrawn = false;
		
		if ( view != null ) {
			viewRedrawn = view.draw();
			
			if ( viewRedrawn ) {
				// Redraw wire-frames ontop of it.
				
				Surface surface = view.getSurface();
				Matrix buff = new Matrix(); // Buffer matrix.
				for ( Car car : cars ) {
					drawCarOn(car, surface, buff);
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
		view.invalidate();
	}
	
	public void reset() {
		view.reset();
		redraw();
	}
	
	/**
	 * Draws the car onto the supplied surface.
	 * 
	 * @param car the car to draw.
	 * @param canvas Canvas to draw the wire-frame on.
	 * @param mat a buffer matrix to set transformations on, can be <code>null</code>.
	 */
	protected void drawCarOn(Car car, Surface canvas, Matrix mat) {
		if ( mat == null ) {
			mat = new Matrix();
		}
		
		CanvasElement fromCanvas = wireFrame.getSurface().getCanvas();
		
		wireFrame.reset();
		wireFrame.setWireFrame(config.getWireFrame(car.getType()));
		wireFrame.setRotate(car.getRotateX(),car.getRotateY(),car.getRotateZ());
		wireFrame.draw();
		
		// The canvas may (will) have a strange transform.
		canvas.save(); // Save the weird transform.
		// Set the transformation.
		Transforms.centerAndScale(mat, car.getPosition(), car.getScale());
		canvas.setTransform(mat);
		// Draw.
		canvas.drawImage(fromCanvas, 0, 0, canvas.getWidth(), canvas.getHeight());
		canvas.restore(); // Restore weird transform.
	}
}
