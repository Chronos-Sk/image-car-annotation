package car.shared.views3d;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import car.shared.config.Config;
import car.shared.views3d.obj.ObjIO;
import car.shared.views3d.obj.ObjWireFrame;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;

/**
 * An extension of {@link car.shared.config.Config} that allows the loading of
 * .obj-based wire-frames specified in the Config file.
 * 
 * If the configuration file fails to load, the <code>WireFrameConfig</code>
 * will load the default fallback model defined in {@link FallbackModel}.
 * 
 * @author Joshua Little
 */
public class WireFrameConfig extends Config {
	// Fallback model lazily loaded from the FallbackModel bundled resource.
	private static ObjWireFrame FALLBACK_MODEL = null;
	
	// Stores wire-frames by name and id.
	private Map<Integer, ObjWireFrame> wireFrames;
	private Map<String, ObjWireFrame> wireFramesByName;
	
	// Identifies the default wire-frame.
	private String defaultWireFrameName;
	private ObjWireFrame defaultWireFrame;
	
	// How many wire-frames left before we're done loading.
	int wireFramesToLoad;
	
	// Default WireFrameConfig.
	private static WireFrameConfig DEFAULT = null;

	/**
	 * Returns the singleton <code>WireFrameConfig</code> instance representing
	 * the default configuration.
	 * 
	 * @return the default <code>WireFrameConfig</code>.
	 */
	public static WireFrameConfig get() {
		// JavaScript's (event-based) single-threaded, so this is fine.
		if ( DEFAULT == null ) {
			DEFAULT = new WireFrameConfig(DEFAULT_CONFIG);
		}
		
		return DEFAULT;
	}

	/**
	 * Creates a new <code>WireFrameConfig</code> from the file at the specified
	 * URL. If the configuration file cannot be loaded (e.g., it does not
	 * exist), then the <code>WireFrameConfig</code> is set up with a single
	 * dummy model named "Default", whose definition is obtained from the
	 * {@link FallbackModel} resource bundle.
	 * 
	 * @param url the location of the configuration file to load.
	 * @throws RuntimeException if there was an error requesting the file.
	 */
	public WireFrameConfig(String url) {
		super(url);
		
		wireFramesByName = new TreeMap<String, ObjWireFrame>();
		wireFrames = new HashMap<Integer, ObjWireFrame>();
	}

	/**
	 * Returns an unmodifiable <code>Set</code> of the loaded wire-frame names.
	 * 
	 * @return an unmodifiable <code>Set</code> of wire-frame names.
	 * @see #getWireFrame(String)
	 * @see #getWireFrameIds()
	 */
	public Set<String> getWireFrameNames() {
		return Collections.unmodifiableSet(wireFramesByName.keySet());
	}
	
	/**
	 * Returns an unmodifiable <code>Set</code> of the loaded wire-frame IDs.
	 * 
	 * @return an unmodifiable <code>Set</code> of wire-frame IDs.
	 */
	public Set<Integer> getWireFrameIds() {
		return Collections.unmodifiableSet(wireFrames.keySet());
	}

	/**
	 *Returns an unmodifiable <code>Collection</code> of the loaded wire-frames.
	 * 
	 * @return an unmodifiable <code>Set</code> of wire-frames.
	 */
	public Collection<ObjWireFrame> getWireFrames() {
		return Collections.unmodifiableCollection(wireFrames.values());
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
		ObjWireFrame ret = wireFramesByName.get(name);
		
		if ( ret == null ) { // If the name is not in the map.
			throw new IllegalArgumentException(
					"No WireFrame with name \"" + name + "\" exists.");
		} else {
			return ret;
		}
	}

	/**
	 * Returns the wire-frame with the specified ID.
	 * 
	 * @param id The ID of the wire-frame to return.
	 * @return the wire-frame with the specified ID.
	 * @see #getWireFrameIds()
	 * @throws IllegalArgumentException if no wire-frame with that ID can be found.
	 */
	public ObjWireFrame getWireFrame(int id) {
		ObjWireFrame ret = wireFrames.get(id);
		
		if ( ret == null ) { // If the name is not in the map.
			throw new IllegalArgumentException(
					"No WireFrame with ID \"" + id + "\" exists.");
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
	 * Starts loading and parsing wire-frames.
	 */
	@Override
	protected void load() {
		assert wireFrames != null && wireFramesByName != null;
		
		loadWireFrames(getXMLDoc().getDocumentElement());
	}
	
	/**
	 * Loads the bundled default wire-frame.
	 */
	protected void fallback() {
		if ( FALLBACK_MODEL == null ) {
			// If the fallback model ain't built yet, build it.
			FallbackModel fm = GWT.create(FallbackModel.class);
			FALLBACK_MODEL = ObjIO.parseObjFile(fm.getFallbackObj().getText());
		}
		
		assert wireFrames != null && wireFramesByName != null;
		
		wireFrames.clear(); wireFramesByName.clear(); // Clear any partial data.
		
		wireFramesByName.put("Default", FALLBACK_MODEL);
		wireFrames.put(0, FALLBACK_MODEL);
		
		setDefaultWireFrame("Default");
		
		doneLoading();
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
			
			int id = Integer.valueOf(wireFrameElement.getAttribute("id"));
			
			// getAttribute() will return null if the "default" attribute has
			// not been explicitly set. Thus use "true"'s equals function.
			boolean isDefault =
				"true".equals(wireFrameElement.getAttribute("default"));
			
			// Location of the wire-frame's definition.
			String url = ((Text) wireFrameElement.getFirstChild()).getData();
			
			// Create default shell.
			ObjWireFrame wireFrame = new ObjWireFrame();
			wireFrame.id = id;
			
			GWT.log("Loading wire-frame: " + name);
			
			// Start loading process.
			ObjIO.createFromURL(wireFrame, url,
					new AddCommand(id, name, wireFrame, isDefault));
		}
	}
	
	/**
	 * Called when a wire-frame is loaded. Used to update
	 * {@link #wireFramesToLoad} variable. Currently calls
	 * {@link #doneLoading()} when the countdown reaches zero and ensures
	 * there's a default wire-frame.
	 */
	private void wireFrameLoaded() {
		if ( --wireFramesToLoad == 0 ) {
			// We've loaded all of the wire-frames. Clean-up time.
			if ( defaultWireFrame == null ) {
				// Make sure there's a default wire-frame.
				setDefaultWireFrame(getWireFrameNames().iterator().next());
			}
			
			doneLoading();
		}
	}

	/**
	 * Class wrapping a function that adds the supplied wire-frame to this
	 * <code> Config</code> when called. Supplied as the callback to
	 * {@link car.shared.views3d.obj.ObjIO#createFromURL(ObjWireFrame, String, Command)}
	 * 
	 * @author Joshua
	 */
	private class AddCommand implements Command {
		private String name;
		private int id;
		private ObjWireFrame wireFrame;
		private boolean isDefault;
		
		public AddCommand(
				int id, String name, ObjWireFrame wireFrame, boolean isDefault){
			this.name = name;
			this.id = id;
			this.wireFrame = wireFrame;
			this.isDefault = isDefault;
		}
		
		@Override
		public void execute() {
			// Add the wire-frame to the maps.
			wireFramesByName.put(name, wireFrame);
			wireFrames.put(id, wireFrame);
			
			if ( isDefault ) {
				// Will override previously specified default wire-frame.
				defaultWireFrame = wireFrame;
				defaultWireFrameName = name;
			}

			GWT.log("Wire-frame loaded: " + name);
			wireFrameLoaded(); // Decrements countdown.
		}
	}
	
	/**
	 * Resource bundle containing text to the default car object.
	 * @author Joshua Little
	 */
	protected interface FallbackModel extends ClientBundle {
		// Protected, not private, so that deferred binding works properly.
		
		@Source("fallback.objm")
		TextResource getFallbackObj();
	}
}
