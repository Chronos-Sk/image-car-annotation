package car.shared.config;

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
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;
import com.google.gwt.xml.client.impl.DOMParseException;

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
 * <code>ValueChangeEvent<Boolean></code> with a value of <code>true</code>,
 * if the requested configuration file could be loaded, or <code>false</code> if
 * it could not and a fallback state was loaded.
 * 
 * When extending this class, {@link #load()} should be overrided with the
 * custom parsing, and {@link #fallback()} should be overriden to provide a
 * fallback state in case parsing fails. Once one has finished loading or
 * setting up a fallback state, one should call {@link #doneLoading()} to fire
 * the loaded event and set the loaded flag to <code>true</code>.
 * 
 * @author Joshua Little
 */
public class Config implements HasValueChangeHandlers<Boolean> {
	/**
	 * Default configuration file location.
	 */
	public static final String DEFAULT_CONFIG
									= GWT.getHostPageBaseURL() + "config.xml";
	
	private static Config DEFAULT = null;
	
	/**
	 * Returns the singleton <code>Config</code> instance representing the
	 * default configuration.
	 * 
	 * @return the default <code>Config</code>.
	 */
	public static Config get() {
		// JavaScript is (event-based) single-threaded, so this works fine.
		if ( DEFAULT == null ) {
			DEFAULT = new Config(DEFAULT_CONFIG);
		}
		
		return DEFAULT;
	}
	
	// Holds handlers waiting for the finished-loading event.
	HandlerManager handlers;
	
	private Document xmlDoc; // The underlying XML file for this Config.
	private boolean loaded = false; // Finished loading everything?
	private boolean fallback = false;
	
	private String configURL;
	
	// Form information.
	private boolean hasForm;
	private String formAction;
	private String formMethod;
	private String formTarget;
	
	/**
	 * Creates a new <code>Config</code> from the file at the specified URL.
	 * If the configuration file cannot be loaded (e.g., it does not exist),
	 * then the <code>Config</code> is set up by calling
	 * {@link #fallback()}.
	 * 
	 * @param url the location of the configuration file to load.
	 * @throws RuntimeException if there was an error requesting the file.
	 */
	public Config(String url) {
		configURL = url;
		
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
	 * Sets the loaded attribute and fires the loaded event.
	 * 
	 * @param success whether the requested configuration file could be loaded.
	 */
	private void setLoaded(boolean success) {
		loaded = true;
		ValueChangeEvent.fire(this, success);
	}
	
	/**
	 * Returns whether the configuration file defined form information. If this
	 * method returns <code>false</code>. Then {@link #getFormMethod()} and
	 * {@link #getFormAction()} will both return <code>null</code> if false.
	 * Otherwise, they will both return non-<code>null</code> values.
	 * 
	 * @return <code>true</code>, if there's form information, <code>false</code> otherwise.
	 */
	public boolean hasForm() {
		return hasForm;
	}
	
	/**
	 * Returns the form method defined in the configuration file. If no form
	 * was defined, will return <code>null</code>. If a form was defined, but
	 * no method declared, will return "GET".
	 * 
	 * @return the form method, or <code>null</code>.
	 */
	public String getFormMethod() {
		if ( hasForm() ) {
			return formMethod;
		} else {
			return null;
		}
	}

	/**
	 * Returns the form action defined in the configuration file. If no form
	 * was defined, will return <code>null</code>. If a form was defined, but
	 * no action declared, will return the empty <code>String</code>.
	 * 
	 * @return the form action, or <code>null</code>.
	 */
	public String getFormAction() {
		if ( hasForm() ) {
			return formAction;
		} else {
			return null;
		}
	}

	/**
	 * Returns the form target defined in the configuration file. If no form
	 * was defined, will return <code>null</code>. If a form was defined, but
	 * no target declared, will return "_self".
	 * 
	 * @return the form action, or <code>null</code>.
	 */
	public String getFormTarget() {
		if ( hasForm() ) {
			return formTarget;
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the underlying XML <code>Document</code> for this <code>Config
	 * </code>. Can be used to load custom properties.
	 * 
	 * @return the underlying XML <code>Document</code>
	 */
	protected Document getXMLDoc() {
		return xmlDoc;
	}
	
	/**
	 * Retrieves and parses the configuration file at the specified URL.
	 * Calls {@link #finishFallback()} if loading fails.
	 * 
	 * @param url the location of the file to load.
	 * @throws RuntimeException if there was an error requesting the file.
	 */
	private void loadConfig(String url) {
		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, url);
		
		rb.setCallback(new RequestCallback() {
			
			/**
			 * Called when we receive the server's response.
			 */
			@Override
			public void onResponseReceived(Request request, Response response) {
				// Try parsing everything.
				try {
					if ( success(response) ) {
						// If we've loaded the file, start parsing.
						parseConfig(response.getText());
						finishLoading();
					} else {
						// abort.
						throw new RuntimeException(
								"Could not load config file \"" + configURL
								+ "\". (Status: " + response.getStatusCode()
								+ ")");
					}
				} catch ( RuntimeException ex ) {
					// There was an exception while parsing the file.
					// Ensure default is loaded and rethrow exception.
					try {
						finishFallback();
					} catch ( Exception ex2 ) {
						ex2.printStackTrace();
					}
					
					throw ex;
				}
			}
			
			/**
			 * Called if there's an error and we won't get a response from the
			 * server.
			 */
			@Override
			public void onError(Request request, Throwable exception) {
				// Something unexpected went wrong. load default and chain
				// exceptions.
				try {
					finishFallback();
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
	 * Parses the supplied content into a XML document.
	 * 
	 * @param content the XML file's content to parse.
	 * @throws DOMParseException if the content does not represent a parsable XML file.
	 */
	private void parseConfig(String content) {
		xmlDoc = XMLParser.parse(content);
	}

	/**
	 * Hook for subclasses to provide custom parsing. Called once the XML
	 * document has been created, but before the loaded event is fired.
	 * {@link #isLoaded()} should return <code>false</code> while still within
	 * this method. Currently just calls {@link #doneLoading}.
	 */
	protected void load() {
		doneLoading();
	}

	/**
	 * Hook for subclasses to provide a default fallback state if loading config
	 * loading fails. Note that if this function was called due to an exception
	 * propagating out of a subclass's {@link #load()} method, the subclass's
	 * data might be in an inconsistent state. The base class <code>Config
	 * </code> should be in a consistant state, however. Currently just calls
	 * {@link #doneLoading}.
	 */
	protected void fallback() {
		doneLoading();
	}
	
	/**
	 * Finishes loading this <code>Config</code> after the XML configuration
	 * file has been retrieved. Calls {@link #load()} so subclasses can
	 * extract the needed data.
	 */
	private void finishLoading() {
		loadForm();
		
		fallback = false;
		load();
	}

	/**
	 * Finishes loading this <code>Config</code> into its fallback state after
	 * loading fails. Calls {@link #fallback()} so subclasses can construct
	 * their own fallback state.
	 */
	private void finishFallback() {
		hasForm = false; // Default to no form.
		
		fallback = true;
		fallback();
	}
	
	/**
	 * Loads form information from the XML document. If no form tag is present,
	 * sets <code>hasForm</code> to <code>false</code> Otherwise, sets <code>
	 * hasForm</code> to <code>true</code> and sets each form parameter either
	 * to its specified value, or to its default value if no value is specified.
	 */
	private void loadForm() {
		Element root = getXMLDoc().getDocumentElement();
		NodeList formNodes = root.getElementsByTagName("form");
		
		if ( formNodes.getLength() == 0 ) {
			// There's no form defined.
			hasForm = false;
		} else {
			Element formElement = (Element) formNodes.item(0);
			
			// Load parameters.
			formMethod = getElementText(formElement, "method", "GET");
			formAction = getElementText(formElement, "action", "");
			formTarget = getElementText(formElement, "target", "_self");
			
			// Finally...
			hasForm = true;
		}
	}
	
	/**
	 * Returns the text content of the named subelement of the specified
	 * element. If no subelement with the supplied name exists, returns the
	 * supplied default value.
	 * 
	 * @param element the element to search in.
	 * @param name the name of the element to search for.
	 * @param def the default value if no specified value can be found.
	 * @return the specified value, or the default value if none exists.
	 */
	private String getElementText(Element element, String name, String def) {
		NodeList methodNodes = element.getElementsByTagName(name);
		if ( methodNodes.getLength() == 0 ) {
			// There's no element of this name defined, use default.
			return def;
		} else {
			// Use the defined value.
			Text data = (Text) methodNodes.item(0).getFirstChild();
			return data.getData();
		}
	}
	
	/**
	 * Fires loaded event. To be called by subclasses when they've finished
	 * setting up their state.
	 */
	protected void doneLoading() {
		if ( isLoaded() ) {
			// Only fire events once.
			throw new IllegalStateException("Already loaded.");
		}
		
		setLoaded(!fallback); // !fallback = success
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
	
}
