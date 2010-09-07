package car.orientor.client;

import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.graphics.canvas.CanvasElement;
import gwt.g2d.client.math.Rectangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import car.shared.input.Slider;
import car.shared.views.Drawable;
import car.shared.views.MovableImageMouseHandler;
import car.shared.views.MovableImageView;
import car.shared.views3d.WireFrameConfig;
import car.shared.views3d.WireFrameMouseHandler;
import car.shared.views3d.WireFrameView;
import car.shared.views3d.obj.ObjWireFrame;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
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
	private static final String CONTAINER_NAME = "carorientor-container";
	// Name of parameter used for image URL.
	private static final String IMAGE_PARAM = "img";
	// Assumed total horizontal border width of views.
	private static final int BORDER_WIDTH = 4;
	
	private WireFrameConfig config; // Holds wire-frame information.
	
	private String imageURL; // URL to image to annotate.
	private Image image; // Image to annotate.
	private Rectangle carRect; // Rectangle around car to annotate.
	
	private int viewWidth = 330; // Width for each view.
	private int viewHeight = 330; // Height for each view.
	private int sepWidth = 10; // Width for separator inbetween views.

	private Panel container; // Main container for this frame.
	private Panel sliderPanel; // Panel containing sliders and their labels.
	
	// Panels showing wire-frame and image.
	private WireFrameView wireFrameView = null;
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
	
	// No car here?
	private CheckBox noCarBox;
	private FlowPanel noCar;
	
	// Submit form.
	private FormPanel form;
	private Panel formContainer;
	private Button submit;
	private Set<Hidden> genHiddens; // Handles to added data fields.

	/**
	 * Adds a hidden input to the submit form with the specified name and value.
	 * Does nothing if this <code>CarPicker</code> has no form.
	 * 
	 * @param name the name of the new hidden input.
	 * @param value the value of the new hidden input.
	 */
	public void addFormEntry(String name, String value) {
		if ( form != null ) {
			formContainer.add(new Hidden(name, value));
		}
	}

	/**
	 * Sets whether the submit button in the form should be enabled.
	 * 
	 * @param enabled whether the submit button should be enabled.
	 */
	public void setFormEnabled(boolean enabled) {
		submit.setEnabled(enabled);
	}
	
	/**
	 * Changes the car rectangle being drawn.
	 * 
	 * @param x x-coordinate of upper-left corner.
	 * @param y y-coordinate of upper-left corner.
	 * @param width width of new rectangle.
	 * @param height height of new rectangle.
	 */
	public void setRectangle(double x, double y, double width, double height) {
		carRect = new Rectangle(x, y, width, height);
		movableImageView.setRectangle(carRect);
		movableImageView.resetOffset();
		redraw();
	}
	
	/**
	 * Sets the minimum zoom of this <code>CarOrientor</code>'s
	 * {@link car.shared.views.MovableImageView}.
	 * 
	 * @param minZoom the new minimum zoom.
	 */
	public void setMinZoom(double minZoom) {
		movableImageView.setMinimumZoom(minZoom);
		redraw();
	}
	
	/**
	 * Sets the minimum zoom of this <code>CarOrientor</code>'s
	 * {@link car.shared.views.MovableImageView}.
	 * 
	 * @param minZoom the new minimum zoom.
	 */
	public void setMaxZoom(double minZoom) {
		movableImageView.setMaximumZoom(minZoom);
		redraw();
	}
	
	/**
	 * Sets the current zoom of this <code>CarOrientor</code>'s
	 * {@link car.shared.views.MovableImageView}.
	 * 
	 * @param zoom the new zoom.
	 */
	public void setZoom(double zoom) {
		movableImageView.setZoom(zoom);
		redraw();
	}
	
	/**
	 * Returns the current zoom of this <code>CarOrientor</code>'s
	 * {@link car.shared.views.MovableImageView}.
	 * 
	 * @return the current zoom.
	 */
	public double getZoom() {
		return movableImageView.getZoom();
	}
	
	/**
	 * Exports the functions that should be available to hand-written
	 * JavaScript. Should be called before {@link #onModuleLoad()} returns.
	 */
	private native void exportFunctions() /*-{
		var _this = this;
		
		$wnd.CarOrientor = new Object();
		$wnd.CarOrientor.addFormEntry = $entry(function(name, value) {
			_this.@car.orientor.client.CarOrientor::addFormEntry(Ljava/lang/String;Ljava/lang/String;)(name,value);
		});
		$wnd.CarOrientor.setFormEnabled = $entry(function(enabled) {
			_this.@car.orientor.client.CarOrientor::setFormEnabled(Z)(enabled);
		});
		$wnd.CarOrientor.resetForm = $entry(function(enabled) {
			_this.@car.orientor.client.CarOrientor::resetForm()();
		});
		$wnd.CarOrientor.setCarRectangle = $entry(function(x,y,w,h) {
			_this.@car.orientor.client.CarOrientor::setRectangle(DDDD)(x,y,w,h);
		});
		
		$wnd.CarOrientor.setMaxZoom = $entry(function(minz) {
			_this.@car.orientor.client.CarOrientor::setMaxZoom(D)(maxz);
		});
		$wnd.CarOrientor.setMinZoom = $entry(function(maxz) {
			_this.@car.orientor.client.CarOrientor::setMinZoom(D)(minz);
		});
		$wnd.CarOrientor.setZoom = $entry(function(z) {
			_this.@car.orientor.client.CarOrientor::setZoom(D)(z);
		});
		$wnd.CarOrientor.getZoom = $entry(function() {
			return _this.@car.orientor.client.CarOrientor::getZoom()();
		});
	}-*/;

	/**
	 * Calls the native JavaScript function <code>$wnd.afterCarOrientorLoad()
	 * </code>, if it exists.
	 */
	private native void fireAfterModuleLoad() /*-{
		if ( $wnd.afterCarOrientorLoad ) {
			$wnd.afterCarOrientorLoad();
		}
	}-*/;

	/**
	 * Calls the native JavaScript function <code>$wnd.onCarOrientorSubmit()
	 * </code>, if it exists.
	 */
	private native void fireOnFormSubmit() /*-{
		if ( $wnd.onCarOrientorSubmit ) {
			$wnd.onCarOrientorSubmit();
		}
	}-*/;

	/**
	 * Gets the configuration name defined in the global JavaScript variable
	 * "carorientor_config", or <code>null</code> if the variable evaluates to
	 * <code>false</code>.
	 * 
	 * @return the supplied configuration file name, or <code>null</code>.
	 */
	public native String readConfigName() /*-{
		return $wnd.carorientor_config;
	}-*/;

	/**
	 * Gets the image URL defined in the global JavaScript variable
	 * "carorientor_image", or <code>null</code> if the variable evaluates to
	 * <code>false</code>.
	 * 
	 * @return the supplied image URL, or <code>null</code>.
	 */
	public native String readImageURL() /*-{
		return $wnd.carorientor_image;
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
	 * Sets the rectangle drawn on the view for this <code>CarOrientor</code>.
	 * 
	 * @param carRect the new rectangle to draw.
	 */
	public void setCarRectangle(Rectangle carRect) {
		this.carRect = carRect;
		draw();
	}

	/**
	 * Converts the scale specific to this <code>Widget</code>'s
	 * {@link car.shared.views3d.WireFrameView} and image into a generic
	 * generic car-scale that maps <code>y = [-1, 1]</code> in image-space to
	 * <code>y = [-1,1]</code> in car-space.
	 * 
	 * @param viewScale the car scale to convert.
	 * @return the scale in car-space.
	 */
	private double computeCarScale(double viewScale) {
		return viewScale * viewHeight / movableImageView.getImage().getHeight();
	}
	
	/**
	 * This is the entry point method. Called after JavaScript is loaded and
	 * environment is set up.
	 */
	public void onModuleLoad() {
		exportFunctions();
		
		Map<String, List<String>> paramMap = Window.Location.getParameterMap();
		
		String url = readImageURL();
		
		// If a url wasn't specified in the host HTML...
		if ( url == null ) {
			// Look for one in a GET parameter.
			url = paramMap.get(IMAGE_PARAM).get(0);
		}

		GWT.log("Image URL: " + url);
		setImageURL(url);
		
		// If it looks like we're GETting a rectangle...
		if ( paramMap.containsKey("x") ) {
			// Set it.
			setCarRectangle(new Rectangle(
					Integer.valueOf(paramMap.get("x").get(0)),
					Integer.valueOf(paramMap.get("y").get(0)),
					Integer.valueOf(paramMap.get("w").get(0)),
					Integer.valueOf(paramMap.get("h").get(0))
					));
		}
		
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
		
		buildWireFrameView();
		buildImageView();
		buildControls();
		
		// Fill up the main container.
		container = new FlowPanel();
		container.add(image);
		container.add(wireFrameView);
		container.add(movableImageView);
		container.add(sliderPanel);
		container.add(carSelectBox);
		container.add(noCar);
		container.add(resetButton);
		
		if ( config.hasForm() ) {
			buildForm();
			container.add(form);
		}
		
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
		wireFrameView = new WireFrameView(carFrame, viewWidth, viewHeight);
		
		// Set up view CSS style.
		Style wfvStyle = wireFrameView.getElement().getStyle();
		// Inline-block allows them to appear side-by-side.
		wfvStyle.setDisplay(Display.INLINE_BLOCK);
		wfvStyle.setPaddingRight(sepWidth, Unit.PX); // Add the separator.
		
		// Handles rotating the wire-frame..
		wfmh = new WireFrameMouseHandler(this, wireFrameView);
	}

	/**
	 * Builds the {@link MovableImageView} and sets the default parameters. Does
	 * not add it to the DOM.
	 */
	private void buildImageView() {
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
				movableImageView.setZoomFactor(frac);
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
		
		noCarBox = new CheckBox();
		noCarBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				redraw(); // Redraw so that the wire-frame dis/reappears.
			}
		});
		
		noCar = new FlowPanel();
		//noCar.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		
		Element noCarElement = noCar.getElement();
		noCarElement.setId("carorientor-nocar-panel");
		noCarElement.getStyle().setDisplay(Display.INLINE);

		noCar.add(new Label("No car:"));
		noCar.add(noCarBox);
	}
	
	/**
	 * Builds the form and form container. Does not add it to the DOM.
	 */
	private void buildForm() {
		form = new FormPanel(config.getFormTarget());
		form.setAction(config.getFormAction());
		form.setMethod(config.getFormMethod());
		
		// Set id.
		Element formElement = form.getElement();
		formElement.setId("carorientor-form");
		
		// Make the submit button appear correctly.
		Style formStyle = formElement.getStyle();
		formStyle.setDisplay(Display.INLINE_BLOCK);
		formStyle.setPaddingLeft(0.5, Unit.EM);
		
		submit = new Button("Submit");
		submit.getElement().setId("carorientor-submit");
		
		formContainer = new FlowPanel();
		formContainer.add(submit);
		
		form.add(formContainer);
		
		// Adds hidden tags with data before the form is actually submitted.
		submit.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if ( shouldSubmitForm() ) {
					// Turn internal variables into hidden inputs.
					generateFormData();
					fireOnFormSubmit();
					form.submit();
				}
			}
		});
		
		genHiddens = new HashSet<Hidden>();
	}
	
	/**
	 * Resets the form by removing the generated fields and enabling the submit
	 * button. Removes all of the fields in {@link #genHiddens} from the
	 * {@linkplain #formContainer form}, clears the set, and calls
	 * <code>setEnabled(true)</code> on {@link #submit}.
	 * 
	 * If this <code>CarOrientor</code> has no form, this method does nothing.
	 */
	public void resetForm() {
		if ( form != null ) { // If we have a form.
			// Remove all our generated data fields.
			for ( Hidden hidden : genHiddens ) {
				formContainer.remove(hidden);
			}
			
			genHiddens.clear();
			submit.setEnabled(true);
		}
	}
	
	/**
	 * Checks to make sure that the form should be submitted. Ensures that the
	 * car has been translated, rotated, and scaled by the user. Returns
	 * <code>true</code> if it has. Returns <code>false</code>, otherwise.
	 * 
	 * @return whether the form should be submitted.
	 */
	private boolean shouldSubmitForm() {
		ArrayList<String> notDone = new ArrayList<String>();
		
		if ( !movableImageView.hasBeenMoved() ) {
			notDone.add("moved");
		}
		if ( !movableImageView.hasBeenScaled() ) {
			notDone.add("scaled");
		}
		if ( !wireFrameView.hasBeenRotated() ) {
			notDone.add("rotated");
		}

		if ( notDone.size() == 0 ) {
			return true; // Everything's been changed.
		}
		
		// Construct confirmation.
		StringBuilder confirmMsg = new StringBuilder("The car hasn't been ");
		
		if ( notDone.size() == 1 ) {
			confirmMsg.append(notDone.get(0)).append('.');
		} else if ( notDone.size() == 2 ) {
			confirmMsg.append(notDone.get(0)).append(" or ");
			confirmMsg.append(notDone.get(1)).append('.');
		} else {
			for ( int i = 0; i < notDone.size() - 1; i++ ) {
				confirmMsg.append(notDone.get(i)).append(", ");
			}
			
			confirmMsg.append("or ").append(notDone.get(notDone.size()-1));
			confirmMsg.append(".");
		}
		
		confirmMsg.append("\nCannot submit until the left and right views and" +
				"the zoom-slider have been changed.");
		
		Window.alert(confirmMsg.toString());
		
		return false;
	}
	
	/**
	 * Converts all of the internal orientation information and adds it to the
	 * attached form. Called when the attached form is being submitted. The
	 * attached form can be assumed to be non-<code>null</code> and initialized.
	 */
	private void generateFormData() {
		submit.setEnabled(false);
		
		// Generate hidden data fields.
		
		// Is there a car?
		if ( noCarBox.getValue() ) {
			genHiddens.add(new Hidden("car", "false"));
		} else {
			genHiddens.add(new Hidden("car", "true"));
			genHiddens.add(new Hidden(
					"carType", "" + wireFrameView.getWireFrame().id));
			
			genHiddens.add(new Hidden("rotX", "" + wireFrameView.getRotateX()));
			genHiddens.add(new Hidden("rotY", "" + wireFrameView.getRotateY()));
			genHiddens.add(new Hidden("rotZ", "" + wireFrameView.getRotateZ()));
			
			double zoom = movableImageView.getZoom();
			
			// offX and offY go to the corner of the image view. The model is in
			// the center of the wire-frame view.
			int posX = (int) movableImageView.getXOffset();
			posX += wireFrameView.getSurface().getWidth() /zoom / 2;
			
			int posY = (int) movableImageView.getYOffset();
			posY += wireFrameView.getSurface().getHeight()/zoom / 2;
			
			genHiddens.add(new Hidden("posX", "" + posX));
			genHiddens.add(new Hidden("posY", "" + posY));
			
			genHiddens.add(new Hidden("scale", "" + computeCarScale(zoom)));
		}
		
		// Add hidden data fields to form.
		for ( Hidden hidden : genHiddens ) {
			formContainer.add(hidden);
		}
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
			
			// If it was redrawn and we're displaying the wire-frame...
			if ( movableImageRedrawn && !noCarBox.getValue() ) {
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
