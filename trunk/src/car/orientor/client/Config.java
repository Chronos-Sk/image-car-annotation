package car.orientor.client;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import car.orientor.client.wfio.obj.ObjIO;
import car.orientor.client.wfio.obj.ObjWireFrame;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

/**
 * This is the class that is responsible for loading configuration information
 * for the Car Orientor project. The configuration must be specified in an
 * external XML file. The <code>Config</code> will also automatically load and
 * parse each referenced wire-frame resource.
 * 
 * By default, the class will automatically load a configuration file
 * from a {@linkplain #DEFAULT_CONFIG default location}.
 * 
 * All loading is asynchronous, and one should call {@link #isLoaded()} before
 * using an instance for the first time to make sure everything is loaded. When
 * the <code>Config</code> is finished loading, it will fire a
 * <code>ValueChangeEvent<Boolean></code> with a value of <code>true</code>.
 * 
 * @author Joshua Little
 */
public class Config implements HasValueChangeHandlers<Boolean> {
	// Default configuration file location. Relative paths start from script
	// directory.
	public static final String DEFAULT_CONFIG = "../config.xml";
	
	private static Config DEFAULT = new Config(DEFAULT_CONFIG);
	
	/**
	 * Returns the singleton <code>Config</code> instance representing the
	 * default configuration.
	 * 
	 * @return the default <code>Config</code>.
	 */
	public static Config get() {
		return DEFAULT;
	}
	
	// Holds handlers waiting for the finished-loading event.
	HandlerManager handlers;
	
	private Document xmlDoc; // The underlying XML file for this Config.
	private Map<String, ObjWireFrame> wireFrames; // Names => wire-frames.
	
	private String defaultWireFrameName;
	private ObjWireFrame defaultWireFrame;

	int wireFramesToLoad; // How many wire-frames left need loading.
	boolean loaded = false; // Finished loading everything?
	
	/**
	 * Creates a new <code>Config</code> from the file at the specified URL.
	 * If the configuration file cannot be loaded (e.g., it does not exist),
	 * then the <code>Config</code> is set up by calling
	 * {@link #loadFallback()}.
	 * 
	 * @param url the location of the configuration file to load.
	 * @throws RuntimeException if there was an error requesting the file.
	 */
	public Config(String url) {
		wireFrames = new TreeMap<String, ObjWireFrame>();
		handlers = new HandlerManager(this);
		
		loadConfig(url);
	}
	
	/**
	 * Returns whether this <code>Config</code> is fully loaded or not.
	 * 
	 * @return <code>true</code> if it is fully loaded, <code>false</code> otherwise.
	 */
	public boolean isLoaded() {
		return loaded;
	}
	
	/**
	 * Returns an unmodifiable <code>Set</code> of the loaded wire-frame names.
	 * 
	 * @return an unmodifiable <code>Set</code> of wire-frame names.
	 * @see #getWireFrame(String)
	 */
	public Set<String> getWireFrameNames() {
		return Collections.unmodifiableSet(wireFrames.keySet());
	}
	
	/**
	 * Returns the wire-frame with the specified name.
	 * 
	 * @param name The name of the wire-frame to return.
	 * @return the wire-frame with the specified name.
	 * @see #getWireFrameNames()
	 * @throws IllegalArgumentException if no wire-frame with that name can be found.
	 */
	public ObjWireFrame getWireFrame(String name) {
		ObjWireFrame ret = wireFrames.get(name);
		
		if ( ret == null ) { // If the name is not in the map.
			throw new IllegalArgumentException(
					"No WireFrame with name \"" + name + "\" exists.");
		} else {
			return ret;
		}
	}
	
	/**
	 * Sets the default wire-frame to the wire-frame with the specified name.
	 * 
	 * @param name the name of the new default wire-frame.
	 * @throws IllegalArgumentException if no wire-frame with that name can be found.
	 * @see #getDefaultWireFrame()
	 * @see #getDefaultWireFrameName()
	 */
	public void setDefaultWireFrame(String name) {
		defaultWireFrame = getWireFrame(name); // Will throw if name is invalid.
		defaultWireFrameName = name;
	}

	/**
	 * Returns the default wire-frame.
	 * 
	 * @return the default wire-frame.
	 * @see #setDefaultWireFrame(String)
	 * @see #getDefaultWireFrameName()
	 */
	public ObjWireFrame getDefaultWireFrame() {
		return defaultWireFrame;
	}

	/**
	 * Returns the default wire-frame's name.
	 * 
	 * @return the default wire-frame's name.
	 * @see #setDefaultWireFrame(String)
	 * @see #getDefaultWireFrame()
	 */
	public String getDefaultWireFrameName() {
		return defaultWireFrameName;
	}
	
	/**
	 * Retrieves and parses the configuration file at the specified URL.
	 * Calls {@link #loadFallback()} if loading fails.
	 * 
	 * @param url the location of the file to load.
	 * @throws RuntimeException if there was an error requesting the file.
	 */
	private void loadConfig(String url) {
		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, url);
		
		rb.setCallback(new RequestCallback() {
			@Override
			public void onResponseReceived(Request request, Response response) {
				// Try parsing everything.
				try {
					if ( !success(response) ) {
						// If we failed to load the file, abort.
						throw new RuntimeException("Could not retrieve config file.");
					}
					
					// Otherwise, start parsing
					parseConfig(response.getText());
				} catch ( RuntimeException ex ) {
					// There was an exception while parsing the file.
					// Ensure default is loaded and rethrow exception.
					try {
						loadFallback();
					} catch ( Exception ex2 ) {
						// Exception chains can't form trees. Print one, chain
						// the other, I guess.
						ex.printStackTrace();
						
						throw new RuntimeException(
								"loadFallback() failed.", ex2);
					}
					
					// Only reached if loadFallback() completes successfully.
					throw ex;
				}
			}
			@Override
			public void onError(Request request, Throwable exception) {
				// Something unexpected went wrong. load default and chain
				// exceptions.
				try {
					loadFallback();
				} catch ( Exception ex ) {
					// Exception chains can't form trees. Print one, chain
					// the other, I guess.
					exception.printStackTrace();
					
					throw new RuntimeException("loadFallback() failed.", ex);
				}
				
				// Only reached if loadFallback() completes successfully.
				throw new RuntimeException(exception);
			}
			
			/**
			 * Returns whether the the filed seems to have been loaded
			 * successfully. Currently assumes any status code starting with a
			 * two represents success.
			 * 
			 * @param response
			 * @return <code>true</code> is successful, <code>false</code> otherwise.
			 * @see <a href="http://en.wikipedia.org/wiki/List_of_HTTP_status_codes">HTTP Status Codes</a>
			 */
			private boolean success(Response response) {
				return (response.getStatusCode() / 100 == 2);
			}
		});
		
		try {
			rb.send(); // Send request.
		} catch (RequestException ex) {
			// Something unexpected went wrong locally.
			throw new RuntimeException(
					"Error requesting config file: \"" + url + "\"", ex);
		}
	}
	
	/**
	 * Parses the supplied content into a XML document and extracts the
	 * configuration info.
	 * 
	 * @param content the XML file's content to parse.
	 * @throws DOMParseException if the content does not represent a parsable XML file.
	 */
	private void parseConfig(String content) {
		xmlDoc = XMLParser.parse(content);
		
		Element root = xmlDoc.getDocumentElement();
		
		loadWireFrames(root);
		//load...(root);
	}
	
	/**
	 * Extracts the wire-frame names and URL's from the XML document and starts
	 * loading the wire-frames.
	 * 
	 * @param config the element to start searching from.
	 */
	private void loadWireFrames(Element config) {
		// Grab all <wire-frame> nodes in the <wire-frames> element.
		Element wfsElement =
			(Element) config.getElementsByTagName("wire-frames").item(0);
		NodeList wfsMappings = wfsElement.getElementsByTagName("wire-frame");
		
		wireFramesToLoad = wfsMappings.getLength();
		
		// Go through each <wire-frame> element.
		for ( int i = 0; i < wireFramesToLoad; i++ ) {
			//Grab the element.
			Element wireFrameElement = (Element) wfsMappings.item(i);
			
			// Wire-frame's name.
			String name = wireFrameElement.getAttribute("name");
			
			// getAttribute() will return null if the "default" attribute has
			// not been explicitly set. Thus use "true"'s equals function.
			boolean isDefault =
				"true".equals(wireFrameElement.getAttribute("default"));
			
			// Location of the wire-frame's definition.
			String url = ((Text) wireFrameElement.getFirstChild()).getData();
			
			// Create default shell.
			ObjWireFrame wireFrame = new ObjWireFrame();
			
			GWT.log("Loading wire-frame: " + name);
			
			// Start loading process.
			ObjIO.createFromURL(wireFrame, url,
					new AddCommand(name, wireFrame, isDefault));
		}
	}
	
	/**
	 * Called when a wire-frame is loaded. Used to update
	 * {@link #wireFramesToLoad} variable. Currently calls {@link #loaded()}
	 * when the countdown reaches zero.
	 */
	private void wireFrameLoaded() {
		if ( --wireFramesToLoad == 0 ) {
			loaded();
		}
	}
	
	/**
	 * Called when this <code>Config</code> is finished loading. Ensures there's
	 * a default wire-frame (choosing an arbitrary one, if need be), set's the
	 * loaded flag to true, and fires the loaded event.
	 */
	private void loaded() {
		if ( defaultWireFrame == null ) {
			setDefaultWireFrame(getWireFrameNames().iterator().next());
		}
		
		loaded = true;
		ValueChangeEvent.fire(this, loaded);
	}
	
	/**
	 * Puts this <code>Config</code> into it's fallback default state. Currently
	 * does nothing but call {@link #loaded()}.
	 */
	protected void loadFallback() {
//		WireFrame box = XMLIO.createFromXML(CarBundle.INSTANCE.carXML().getText());
//		wireFrames.put("Box", box);

//		setDefaultWireFrame("Box");
		
		loaded();
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		handlers.fireEvent(event);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<Boolean> handler) {
		
		return handlers.addHandler(ValueChangeEvent.getType(), handler);
	}
	
	/**
	 * Class wrapping a function that adds the supplied wire-frame to this
	 * <code> Config</code> when called. Supplied as the callback to
	 * {@link car.orientor.client.wfio.obj.ObjIO#createFromURL(ObjWireFrame, String, Command)}
	 * 
	 * @author Joshua
	 */
	private class AddCommand implements Command {
		private String name;
		private ObjWireFrame wireFrame;
		private boolean isDefault;
		
		public AddCommand(String name, ObjWireFrame wireFrame, boolean isDefault) {
			this.name = name;
			this.wireFrame = wireFrame;
			this.isDefault = isDefault;
		}
		
		@Override
		public void execute() {
			wireFrames.put(name, wireFrame); // Add the wire-frame to the map.
			
			if ( isDefault ) {
				// Will override previously specified default wire-frame.
				defaultWireFrame = wireFrame;
				defaultWireFrameName = name;
			}

			GWT.log("Wire-frame loaded: " + name);
			wireFrameLoaded(); // Decrements countdown.
		}
	}
	
}
