package car.orientor.client;

import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.graphics.canvas.CanvasElement;
import gwt.g2d.client.math.Rectangle;
import car.orientor.client.wfio.obj.ObjWireFrame;
import car.orientor.input.Slider;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point and enclosing <code>Widget</code> for the Car Orientor project.
 * 
 * @author Joshua Little
 */
public class CarOrientor extends FocusPanel implements EntryPoint, Drawable {
	// Containing div id.
	private static final String CONTAINER_NAME = "canvasContainer";
	// Name of parameter used for image URL.
	private static final String IMAGE_PARAM = "img";
	// Assumed total horizontal border width of views.
	private static final int BORDER_WIDTH = 4;
	
	private Config config; // Holds wire-frame information.
	
	private String imageURL; // URL to image to annotate.
	private Image image; // Image to annotate.
	private Rectangle carRect; // Rectangle around car to annotate.
	
	private int viewWidth = 300; // Width for each view.
	private int viewHeight = 300; // Height for each view.
	private int sepWidth = 10; // Width for separator inbetween views.

	private Panel container; // Main container for this frame.
	private Panel sliderPanel; // Panel containing sliders and their labels.
	
	// Panels showing wire-frame and image.
	private ObjWireFrameView wireFrameView = null;
	private MovableImageView movableImageView = null;
	
	// Classes responsible for handling mouse events on the views.
	@SuppressWarnings("unused")
	private WireFrameMouseHandler wfmh;
	@SuppressWarnings("unused")
	private MovableImageMouseHandler mimh;
	
	// Input controls.
	private Slider zoomSlider;
	private Slider rollSlider;
	private Button resetButton;
	private ListBox carSelectBox;
	
	/**
	 * Sets the URL used to load the image to annotate. Must be set before
	 * onLoad() is called.
	 * @param imageURL
	 */
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	/**
	 * Sets the URL used to load the rectangle around the car to annotate. Must
	 * be set before onLoad() is called.
	 * @param imageURL
	 */
	public void setCarRectangle(Rectangle carRect) {
		this.carRect = carRect;
		draw();
	}
	
	/**
	 * This is the entry point method. Called after JavaScript is loaded and
	 * environment is set up.
	 */
	public void onModuleLoad() {
		setImageURL(Window.Location.getParameter(IMAGE_PARAM));
		
		setCarRectangle(new Rectangle(
				Integer.valueOf(Window.Location.getParameter("x")),
				Integer.valueOf(Window.Location.getParameter("y")),
				Integer.valueOf(Window.Location.getParameter("w")),
				Integer.valueOf(Window.Location.getParameter("h"))
				));
		
		config = Config.get(); // Load and grab configuration file.
		
		// Wait for Config to finish loading everything (i.e. wire-frames).
		if ( !config.isLoaded() ) {
			// Whill fire a ValueChangeEvent with value <code>true</code> when
			// finished. JavaScript is single-threaded (and event driven), so
			// this code doesn't produce a race condition.
			config.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if ( event.getValue() ) {
						configLoaded();
					}
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
		getElement().setClassName("carOrientor");
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
		style.setWidth(2 * viewWidth + sepWidth + BORDER_WIDTH, Unit.PX);
		
		// Load default car wire-frame and set up wire-frame view.
		ObjWireFrame carFrame = config.getDefaultWireFrame(); 
		wireFrameView = new ObjWireFrameView(carFrame, viewWidth, viewHeight);
		
		// Set up view CSS style.
		Style wfvStyle = wireFrameView.getElement().getStyle();
		// Inline-block allows them to appear side-by-side.
		wfvStyle.setDisplay(Display.INLINE_BLOCK);
		wfvStyle.setPaddingRight(sepWidth, Unit.PX); // Add the separator.
		
		// Handles rotating the wire-frame..
		wfmh = new WireFrameMouseHandler(this, wireFrameView);

		// Initialize the image view with a <code>null</code> image.
		movableImageView = new MovableImageView(image, carRect, viewWidth, viewHeight);
		movableImageView.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);

		// Load the image and give it to the image view when it's done loading.
		image = new Image(imageURL);
		image.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				movableImageView.setImage(image);
				draw();
			}
		});
		
		// We're adding it to the DOM to make sure the the browser loads the
		// image when we want it to. Setting display to NONE makes sure that it
		// doesn't directly show up on the page.
		image.getElement().getStyle().setDisplay(Display.NONE);
		
		// Handles translating the image.
		mimh = new MovableImageMouseHandler(CarOrientor.this, movableImageView);
		
		// Resets rotation, translation, zoom, and selected car.
		resetButton = new Button("Reset", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				reset();
			}
		});
		resetButton.setStylePrimaryName("resetButton");
		
		// Controls the wire-frame's roll.
		rollSlider = new Slider();
		rollSlider.addValueChangeHandler(new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> event) {
				// Sets the roll value depending on values value relative to its
				// maximum value.
				
				double frac = event.getValue() / rollSlider.getMaximum() - 0.5;
				wireFrameView.setRotateZ(-frac * Math.PI);
				draw();
			}
		});

		// Controls the image-frame's zoom.
		zoomSlider = new Slider();
		zoomSlider.addValueChangeHandler(new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> event) {
				// Sets the zoom. The view converts this linear scale into an
				// exponential one.
				
				double frac = event.getValue() / zoomSlider.getMaximum();
				movableImageView.setZoom(frac);
				draw();
			}
		});

		// Set the initial slider value depending on the initial zoom.
		double initialZoom =
				movableImageView.getZoomFactor() * zoomSlider.getMaximum();
		zoomSlider.setValue(initialZoom);
		
		// Put the slider stuff in the slider panel.
		sliderPanel = new FlowPanel();
		sliderPanel.setStylePrimaryName("sliderPanel");
		sliderPanel.add(new InlineLabel("Roll: "));
		sliderPanel.add(rollSlider);
		sliderPanel.add(new InlineLabel("Zoom: "));
		sliderPanel.add(zoomSlider);
		
		// Controls which car wire-frame is shown.
		carSelectBox = new ListBox();
		
		// Fill up the list box.
		for ( String name : config.getWireFrameNames() ) {
			carSelectBox.addItem(name);
		}
		
		carSelectBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				// Determine which name was selected and set the appropriate
				// wire-frame.
				int selectedIndex = carSelectBox.getSelectedIndex();
				String selectedName = carSelectBox.getItemText(selectedIndex);
				
				wireFrameView.setWireFrame(config.getWireFrame(selectedName));
				draw();
			}
		});
		
		// Set which name was already picked.
		setSelectedCarName(config.getDefaultWireFrameName());
		
		// Fill up the main container.
		container = new FlowPanel();
		container.add(image);
		container.add(wireFrameView);
		container.add(movableImageView);
		container.add(sliderPanel);
		container.add(carSelectBox);
		container.add(resetButton);
		
		setWidget(container);
		draw();
	}
	
	/**
	 * Finds the index of the supplied name in the <code>carSelectBox</code> and
	 * selects it.
	 * 
	 * @param name Name of wire-frame to select.
	 * @throws IllegalArgumentException if name is not in the <code>carSelectBox</code>
	 */
	private void setSelectedCarName(String name) {
		// If name is null, unselect current car name.
		if ( name == null ) {
			carSelectBox.setItemSelected(carSelectBox.getSelectedIndex(),false);
			return;
		}
		
		// Find name and select it. We're assuming there aren't that many names
		// in the select box, otherwise we'd build some kind of table.
		for ( int i = 0; i < carSelectBox.getItemCount(); i++ ) {
			if ( name.equals(carSelectBox.getItemText(i)) ) {
				carSelectBox.setSelectedIndex(i);
				return;
			}
		}
		
		// Name is not in there, throw an exception.
		throw new IllegalArgumentException(
				"\"" + name + "\" is not in the carSelectBox.");
	}
	
	/**
	 * Resets state of this widget. Resets rotation, translation, zoom, and the
	 * currently selected car values. Does not clear the image or car rectangle,
	 * nor does it rebuild any actual HTML.
	 */
	public void reset() {
		// Tell the two views to reset.
		wireFrameView.reset();
		movableImageView.reset();
		
		// Sets the default slider values.
		zoomSlider.setValue(
				movableImageView.getZoomFactor() * zoomSlider.getMaximum());
		rollSlider.setValue(50);
		
		// If the wire-frame has been changed from the default, change it back.
		ObjWireFrame def = config.getDefaultWireFrame();
		if ( wireFrameView.getWireFrame() != def ) {
			wireFrameView.setWireFrame(def);
			setSelectedCarName(config.getDefaultWireFrameName());
		}
		
		draw();
	}
	
	/**
	 * Draws the two views and draws the wire-frame ontop of the image-view.
	 * Only redraws the frames that need to be redrawn.
	 */
	@Override
	public boolean draw() {
		// Draw only redraws if necessary. Returns whether it redrew or not.
		
		boolean wireFrameRedrawn = false;
		if ( wireFrameView != null ) {
			wireFrameRedrawn = wireFrameView.draw();
		}
		
		if ( movableImageView != null ) {
			boolean movableImageRedrawn;
			
			// If the wire-frame was redrawn, we need to redraw this.
			if ( wireFrameRedrawn ) { 
				movableImageView.redraw();
				movableImageRedrawn = true;
			} else { // Otherwise, only redraw it if it needs redrawing.
				movableImageRedrawn = movableImageView.draw();
			}
			
			if ( movableImageRedrawn ) { // If it was redrawn...
				// Redraw wire-frame ontop of it.
				drawWireFrameOn(movableImageView.getSurface());
			}
		}
		
		return true;
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
		wireFrameView.invalidate();
		movableImageView.invalidate();
	}
	
	/**
	 * Draws the wire-frame onto the supplied surface. It does this by directly
	 * rendering the wire-frame view's canvas onto the supplied canvas.
	 * 
	 * @param canvas Canvas to draw the wire-frame on.
	 */
	protected void drawWireFrameOn(Surface canvas) {
		CanvasElement fromCanvas = wireFrameView.getSurface().getCanvas();
		
		// The canvas may (will) have a strange transform.
		canvas.save(); // Save the weird transform.
		// Set the transformation to identity.
		canvas.setTransform(1, 0, 0, 1, 0, 0);
		// Draw.
		canvas.drawImage(fromCanvas, 0, 0, canvas.getWidth(), canvas.getHeight());
		canvas.restore(); // Restore weird transform.
	}
}
