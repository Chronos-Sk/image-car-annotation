package car.picker.client;

import gwt.g2d.client.graphics.Surface;
import car.shared.config.Config;
import car.shared.math.Point2D;
import car.shared.views.Drawable;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point and enclosing <code>Widget</code> for the Car Picker project.
 * 
 * @author Joshua Little
 */
public class Viewer extends FocusPanel implements EntryPoint, Drawable {
	// Name of div tag to put main widget in.
	private static final String CONTAINER_NAME = "carpickerviewer-container";
	// Parameter name for image URL.
	private static final String IMAGE_PARAM = "img";

	private Config config; // Stores form configuration.
	
	// Handles the drawing and manipulating of CarPoints.
	private CarPointHandler carPointHandler;
	
	private Panel container; // Main panel for this widget.
	
	private String imageURL; // URL to image to annotate.
	
	private Surface canvas = null; // Canvas to draw everything on.
	private Image image = null; // Image to annotate.
	
	/**
	 * Exports the functions that should be available to hand-written
	 * JavaScript. Should be called before {@link #onModuleLoad()} returns.
	 */
	private native void exportFunctions() /*-{
		var _this = this;
		
		$wnd.CarPickerViewer = new Object();
		$wnd.CarPickerViewer.addCar = $entry(function(x,y) {
			_this.@car.picker.client.Viewer::addCar(DD)(x,y);
		});
	}-*/;
	
	/**
	 * Calls the native JavaScript function <code>
	 * $wnd.afterCarPickerViewerLoad()</code>, if it exists.
	 */
	private native void fireAfterModuleLoad() /*-{
		if ( $wnd.afterCarPickerViewerLoad ) {
			$wnd.afterCarPickerViewerLoad();
		}
	}-*/;

	/**
	 * Gets the configuration name defined in the global JavaScript variable
	 * "carpickerviewer_config", or <code>null</code> if the variable evaluates
	 * to <code>false</code>.
	 * 
	 * @return the supplied configuration file name, or <code>null</code>.
	 */
	public native String readConfigName() /*-{
		return $wnd.carpickerviewer_config;
	}-*/;
	
	/**
	 * Gets the image URL defined in the global JavaScript variable
	 * "carpickerviewer_image", or <code>null</code> if the variable evaluates
	 * to <code>false</code>.
	 * 
	 * @return the supplied image URL, or <code>null</code>.
	 */
	public native String readImageURL() /*-{
		return $wnd.carpickerviewer_image;
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
	 * Adds a new {@link CarPoint} at the specified location.
	 * 
	 * @param x the x-coordinate of the new <code>CarPoint</code>
	 * @param y the y-coordinate of the new <code>CarPoint</code>
	 */
	public void addCar(double x, double y) {
		carPointHandler.addCar(new Point2D(x, y));
		redraw();
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
			// Look for one in a GET parameter.
			url = Window.Location.getParameter(IMAGE_PARAM);
		}

		GWT.log("Image URL: " + url);
		setImageURL(url);
		
		String configName = readConfigName();
		
		if ( configName == null ) {
			config = Config.get(); // Load and grab default config.
		} else {
			config = new Config(configName);
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
		getElement().setClassName("carPicker");
		RootPanel.get(CONTAINER_NAME).add(this);
	}
	
	/**
	 * Called when the widget is fully integrated into the DOM tree. Responsible
	 * for building the internal HTML and setting up appropriate handlers. If
	 * this function returns normally, the widget should be fully initialized.
	 */
	public void onLoad() {
		buildView();
		
		// Fill main panel.
		container = new FlowPanel();
		container.add(image);
		container.add(canvas);

		setWidget(container); // Connects the main container to this widget.
	}

	/**
	 * Builds the view and image, and sets their default parameters. Does not
	 * add them to the DOM.
	 */
	private void buildView() {
		image = new Image(imageURL); // Prep the image for loading.
		
		// We're adding it to the DOM to make sure the the browser loads the
		// image when we want it to. Setting display to NONE makes sure that it
		// doesn't directly show up on the page.
		image.getElement().getStyle().setDisplay(Display.NONE);
		
		// Set up the canvas to draw everything on.
		canvas = new Surface();
		canvas.getElement().setId("carViewerCanvas");
		
		// Build handler responsible for dealing with user-interactions.
		carPointHandler = new CarPointHandler(canvas, image);
		
		// We don't register it because it's supposed to be read-only.
		// carPointHandler.register();

		// Load handler to set the widget size and draw everything once image
		// is done loading. We need the image information to do these things.
		image.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				canvas.setSize(image.getWidth(), image.getHeight());

				//Explicit width is needed for centering the widget on the page.
				Style style = getElement().getStyle();
				style.setWidth(image.getWidth(), Unit.PX);
				style.setHeight(image.getHeight(), Unit.PX);
				
				carPointHandler.draw();
				fireAfterModuleLoad();
			}
		});
	}

	@Override
	public boolean draw() {
		carPointHandler.draw();
		return true;
	}

	@Override
	public void invalidate() {
		return; // draw() always redraws.
	}

	@Override
	public void redraw() {
		draw();
	}
}
