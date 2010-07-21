package car.shared.views3d.obj;

import java.util.ArrayList;

import car.shared.math.Point3D;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;

/**
 * Handles the parsing of .obj files, and converts them into
 * {@link ObjWireFrame}s.
 * 
 * Doesn't support writing them, since that's not quite possible in JavaScript
 * (as far as I care, anyways).
 * 
 * @author Joshua
 */
public abstract class ObjIO {
	
	// Abstract + private constructor = non-instantiable.
	private ObjIO() {}

	/**
	 * Equivalent to calling
	 * <code>createFromURL(new ObjWireFrame(), url, onLoad)</code>
	 * 
	 * @param url
	 * @param onLoad
	 * @return the wire-frame that the parsed results will be stored in.
	 * @see #createFromURL(ObjWireFrame, String, Command)
	 */
	public static ObjWireFrame createFromURL(String url, final Command onLoad) {
		ObjWireFrame dest = new ObjWireFrame();
		createFromURL(dest, url, onLoad);
		
		return dest;
	}

	/**
	 * Starts the asynchronous request for the .obj file and parses the results.
	 * The results are eventually stored in the supplied {@link ObjWireFrame}.
	 * Once the .obj file is finished loading and parsing.
	 * 
	 * @param url the URL of the .obj file to load.
	 * @param onLoad the <code>Command</code> to execute when it's finished loading.
	 * @see #createFromURL(String, Command)
	 */
	public static void createFromURL(
				final ObjWireFrame dest, String url, final Command onLoad) {
		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, url);
		
		rb.setCallback(new RequestCallback() {
			@Override
			public void onError(Request request, Throwable exception) {
				// Unexpected error, chain and throw upwards.
				throw new RuntimeException(exception);
			}
			
			@Override
			public void onResponseReceived(Request request, Response response) {
				// Parse results into ObjWireFrame.
				ObjWireFrame temp = parseObjFile(response.getText());
				
				// Copy into destination ObjWireFrame.
				dest.vertices = temp.vertices;
				dest.normals = temp.normals;
				dest.faces = temp.faces;
				
				// We're done parsing, execute onLoad.
				if ( onLoad != null ) {
					onLoad.execute();
				}
			}
		});
		
		try {
			rb.send();
		} catch (RequestException ex) {
			// Unexpected local error, chain and throw upwards.
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Creates an {@link ObjWireFrame} from the specified .obj file contents.
	 * 
	 * @param contents the contents of the .obj file.
	 * @return the <code>ObjWireFrame</code> representing the <code>contents.</code>
	 */
	public static ObjWireFrame parseObjFile(String contents) {
		ArrayList<Point3D> vertices = new ArrayList<Point3D>();
		ArrayList<Point3D> normals = new ArrayList<Point3D>();
		ArrayList<Face> faces = new ArrayList<Face>();
		
		String[] strLines = contents.split("\n"); // Split file into lines.
		for ( String strLine : strLines ) {
			// Split line into tokens.
			String[] tokens = strLine.trim().split(" ");
			
			if ( tokens[0].equals("v") ) {
				// It's a vertex definition.
				vertices.add(parseVertex(tokens));
			} else if ( tokens[0].equals("vn") ) {
				// It's a normal definition.
				normals.add(parseNormal(tokens));
			} else if ( tokens[0].equals("f") ) {
				// It's a face definition.
				faces.add(parseFace(tokens));
			} else {
				// It's unimportant and/or unneeded.
			}
		}
		
		// Return our results.
		return new ObjWireFrame(vertices.toArray(new Point3D[0]),
								normals.toArray(new Point3D[0]),
								faces.toArray(new Face[0]));
	}
	
	/**
	 * Parse a vertex from the supplied line tokens.
	 * 
	 * @param tokens the tokens of the line making up the vertex definition.
	 * @return the vertex defined by the line.
	 */
	private static Point3D parseVertex(String[] tokens) {
		double x = Double.parseDouble(tokens[1]);
		double y = Double.parseDouble(tokens[2]);
		double z = Double.parseDouble(tokens[3]);
		
		return new Point3D(x, y, z);
	}

	/**
	 * Parse a normal from the supplied line tokens.
	 * 
	 * @param tokens the tokens of the line making up the normal definition.
	 * @return the normal defined by the line.
	 */
	private static Point3D parseNormal(String[] tokens) {
		double x = Double.parseDouble(tokens[1]);
		double y = Double.parseDouble(tokens[2]);
		double z = Double.parseDouble(tokens[3]);
		
		return new Point3D(x, y, z);
	}

	/**
	 * Parse a {@link Face} from the supplied line tokens.
	 * 
	 * @param tokens the tokens of the line making up the <code>Face</code> definition.
	 * @return the <code>Face</code> defined by the line.
	 */
	private static Face parseFace(String[] tokens) {
		// Token: vertex, vertex/texture, vertex/texture/normal,
		// 		  or vertex//normal.
		
		// One vertex per token past the 'f'.
		int[] vertices = new int[tokens.length - 1];
		int normal = -1; // Default value, representing no normal.
		
		// If we have at least one point in the fdace.
		if ( tokens.length > 1 ) {
			// Try and grab the normal for that point, which we'll use as the
			// normal for the entire face.
			
			String[] indices = tokens[1].split("/");
			
			if ( indices.length > 2 ) {
				// We've got a normal.
				
				// Obj files index from 1.
				normal = Integer.parseInt(indices[2]) - 1;
			}
		}
		
		// Parse each vertex.
		for ( int i = 1; i < tokens.length; i++ ) {
			// Obj files index from 1.
			vertices[i-1] = parseFacePoint(tokens[i]) - 1;
		}
		
		return new Face(vertices, normal);
	}

	/**
	 * Parse a point in a {@link Face} definition
	 * 
	 * @param token the token defining the point in the <code>Face</code>
	 * @return the index of the vertex for that point.
	 */
	private static int parseFacePoint(String token) {
		// We only care about the first value, the vertex index.
		
		int cutoff = token.indexOf('/');
		
		if ( cutoff == -1 ) {
			// The index is the entire thing.
			return Integer.parseInt(token);
		} else {
			// The index is up to that first '/'.
			return Integer.parseInt(token.substring(0, cutoff));
		}
	}
	
}
