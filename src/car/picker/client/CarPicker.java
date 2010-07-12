package car.picker.client;

import gwt.g2d.client.graphics.Surface;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point and enclosing <code>Widget</code> for the Car Picker project.
 * 
 * @author Joshua Little
 */
public class CarPicker extends FocusPanel implements EntryPoint {
	// Name of div tag to put main widget in.
	private static final String CONTAINER_NAME = "canvasContainer";
	// Parameter name for image URL.
	private static final String IMAGE_PARAM = "img";

	// Labels for size radio buttons.
	private String SMALL_LABEL = "Small";
	private String MEDIUM_LABEL = "Medium";
	private String LARGE_LABEL = "Large";
	
	// Handles the drawing and manipulating of CarPoints.
	private CarPointHandler carPointHandler;
	
	private Panel container; // Main panel for this widget.
	
	private String imageURL; // URL to image to annotate.
	
	private Surface canvas = null; // Canvas to draw everything on.
	private Image image = null; // Image to annotate.
	
	private ComplexPanel controlPanel; // Panel to hold all the buttons.
	private Button removeButton; // Button to remove a CarPoint.
	private Button resetButton; // Button to reset the widget.
	
	// Changes the selected (and future) car points to their respective sizes.
	private RadioButton smallButton;
	private RadioButton mediumButton;
	private RadioButton largeButton;

	/**
	 * This is the entry point method. Called after JavaScript is loaded and
	 * environment is set up.
	 */
	public void onModuleLoad() {
		setImageURL(Window.Location.getParameter(IMAGE_PARAM));
		getElement().setClassName("carPicker");
		RootPanel.get(CONTAINER_NAME).add(this);
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
	 * Called when the widget is fully integrated into the DOM tree. Responsible
	 * for building the internal HTML and setting up appropriate handlers. If
	 * this function returns normally, the widget should be fully initialized.
	 */
	public void onLoad() {
		image = new Image(imageURL); // Prep the image for loading.
		
		// We're adding it to the DOM to make sure the the browser loads the
		// image when we want it to. Setting display to NONE makes sure that it
		// doesn't directly show up on the page.
		image.getElement().getStyle().setDisplay(Display.NONE);
		
		// Set up the canvas to draw everything on.
		canvas = new Surface();
		canvas.getElement().setId("carCanvas");
		
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
			}
		});
		
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
		
		// Radios set size of selected car and future cars.
		smallButton = new RadioButton("size", SMALL_LABEL);
		mediumButton = new RadioButton("size", MEDIUM_LABEL);
		largeButton = new RadioButton("size", LARGE_LABEL);
		
		mediumButton.setValue(true); // Default size is Medium.
		
		// Handler updates car size depending on source radio button.
		ClickHandler sizeHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String label = ((RadioButton) event.getSource()).getText();
				
				CarPoint.Size newSize = null;
				if ( label.equals(SMALL_LABEL) ) {
					newSize = CarPoint.Size.Small;
				} else if ( label.equals(MEDIUM_LABEL) ) {
					newSize = CarPoint.Size.Medium;
				} else if ( label.equals(LARGE_LABEL) ) {
					newSize = CarPoint.Size.Large;
				} else {
					throw new IllegalStateException(
						"Size handler received event from erroneous source.");
				}
				
				// Update new size.
				carPointHandler.setCarSize(newSize);
			}
		};
		
		smallButton.addClickHandler(sizeHandler);
		mediumButton.addClickHandler(sizeHandler);
		largeButton.addClickHandler(sizeHandler);
		
		// Updates the size radio buttons when the user selects a CarPoint.
		carPointHandler.addSelectionHandler(new SelectionHandler<CarPoint>() {
			@Override
			public void onSelection(SelectionEvent<CarPoint> event) {
				CarPoint carPoint = event.getSelectedItem();
				
				if ( carPoint != null ) {
					switch ( carPoint.getSize() ) {
						case Small:
							smallButton.setValue(true);
							break;
						case Medium:
							mediumButton.setValue(true);
							break;
						case Large:
							largeButton.setValue(true);
							break;
					}
				}
			}
		});
		
		// Fill control panel.
		controlPanel = new FlowPanel();
		controlPanel.add(removeButton);
		controlPanel.add(resetButton);
		controlPanel.add(smallButton);
		controlPanel.add(mediumButton);
		controlPanel.add(largeButton);

		// Fill main panel.
		container = new FlowPanel();
		container.add(image);
		container.add(canvas);
		container.add(controlPanel);
		
		setWidget(container); // Connects the main container to this widget.
	}
}
