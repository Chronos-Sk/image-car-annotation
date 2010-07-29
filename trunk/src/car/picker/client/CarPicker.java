package car.picker.client;

import gwt.g2d.client.graphics.Surface;
import car.shared.config.Config;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
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
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

/**
 * Entry point and enclosing <code>Widget</code> for the Car Picker project.
 * 
 * @author Joshua Little
 */
public class CarPicker extends FocusPanel implements EntryPoint {
	// Name of div tag to put main widget in.
	private static final String CONTAINER_NAME = "carpicker-container";
	// Parameter name for image URL.
	private static final String IMAGE_PARAM = "img";

	private Config config; // Stores form configuration.
	
	// Handles the drawing and manipulating of CarPoints.
	private CarPointHandler carPointHandler;
	
	private Panel container; // Main panel for this widget.
	
	private String imageURL; // URL to image to annotate.
	
	private Surface canvas = null; // Canvas to draw everything on.
	private Image image = null; // Image to annotate.
	
	private ComplexPanel controlPanel; // Panel to hold all the buttons.
	private Button removeButton; // Button to remove a CarPoint.
	private Button resetButton; // Button to reset the widget.
	
	// Submit form.
	private FormPanel form;
	private Panel formContainer;
	private Button submit;
	
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
	 * Exports the functions that should be available to hand-written
	 * JavaScript. Should be called before {@link #onModuleLoad()} returns.
	 */
	private native void exportFunctions() /*-{
		var _this = this;
		
		$wnd.CarPicker = new Object();
		$wnd.CarPicker.addFormEntry = $entry(function(name, value) {
			_this.@car.picker.client.CarPicker::addFormEntry(Ljava/lang/String;Ljava/lang/String;)(name,value);
		});
		$wnd.CarPicker.setFormEnabled = $entry(function(name, value) {
			_this.@car.picker.client.CarPicker::setFormEnabled(Z)(name,value);
		});
		$wnd.CarPicker.setMaxCars = $entry(function(max) {
			_this.@car.picker.client.CarPicker::carPointHandler.@car.picker.client.CarPointHandler::setMaxCars(I)(max);
		});
	}-*/;
	
	/**
	 * Calls the native JavaScript function <code>$wnd.afterCarPickerLoad()
	 * </code>, if it exists.
	 */
	private native void fireAfterModuleLoad() /*-{
		if ( $wnd.afterCarPickerLoad ) {
			$wnd.afterCarPickerLoad();
		}
	}-*/;

	/**
	 * Gets the configuration name defined in the global JavaScript variable
	 * "carpicker_config", or <code>null</code> if the variable evaluates to
	 * <code>false</code>.
	 * 
	 * @return the supplied configuration file name, or <code>null</code>.
	 */
	public native String readConfigName() /*-{
		return $wnd.carpicker_config;
	}-*/;
	
	/**
	 * Gets the image URL defined in the global JavaScript variable
	 * "carpicker_image", or <code>null</code> if the variable evaluates to
	 * <code>false</code>.
	 * 
	 * @return the supplied image URL, or <code>null</code>.
	 */
	public native String readImageURL() /*-{
		return $wnd.carpicker_image;
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
		buildControls();
		
		// Fill main panel.
		container = new FlowPanel();
		container.add(image);
		container.add(canvas);
		container.add(controlPanel);

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
		canvas.getElement().setId("carPickerCanvas");
		
		// Build handler responsible for dealing with user-interactions.
		carPointHandler = new CarPointHandler(canvas, image);
		carPointHandler.register();
		addKeyUpHandler(carPointHandler);

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

	/**
	 * Builds the all of the controls and sets the default parameters. Does not
	 * add them to the DOM.
	 */
	private void buildControls() {
		// Button removes focused car.
		removeButton = new Button("Remove", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				carPointHandler.deleteFocusedCar();
			}
		});
		
		// Button resets widget.
		resetButton = new Button("Reset", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				carPointHandler.clearCars();
			}
		});
		
		// Fill control panel.
		controlPanel = new FlowPanel();
		controlPanel.add(removeButton);
		controlPanel.add(resetButton);
		
		if ( config.hasForm() ) {
			buildForm();
			controlPanel.add(form);
		}
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
		formElement.setId("carpicker-form");

		// Make the submit button appear correctly.
		Style formStyle = formElement.getStyle();
		formStyle.setDisplay(Display.INLINE_BLOCK);
		formStyle.setFloat(Style.Float.RIGHT);

		submit = new Button("Submit");
		submit.getElement().setId("carpicker-submit");
		
		submit.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				form.submit();
			}
		});
		
		formContainer = new FlowPanel();
		formContainer.add(submit);
		
		form.add(formContainer);

		// Adds hidden tags with data before the form is actually submitted.
		form.addSubmitHandler(new SubmitHandler() {
			@Override
			public void onSubmit(SubmitEvent event) {
				generateFormData();
			}
		});
	}

	/**
	 * Converts the list of {@link CarPoint}s in the {@link CarPointHandler} and
	 * adds them to the attached form. Called when the attached form is being
	 * submitted. The attached form can be assumed to be non-<code>null</code>
	 * and initialized.
	 */
	private void generateFormData() {
		submit.setEnabled(false);
		
		for ( CarPoint carPoint : carPointHandler.getCars() ) {
			formContainer.add(new Hidden("car", "" + carPoint.toDataString()));
		}
	}
}
