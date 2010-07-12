package car.orientor.client.wfio.obj;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;

import car.shared.math.Point3D;

/**
 * Class representing a 3D wire-frame, generally defined by an .obj file.
 * Consists of an array of vertices, and array of normals, and an array of
 * {@link Face}s. The indices in the <code>Face</code>s index into the
 * respective vertex and normal arrays in this class.
 * 
 * @author Joshua Little
 */
public class ObjWireFrame {
	
	public Point3D[] vertices; // Vertices that Faces index into.
	public Point3D[] normals; // Normals that Faces index into.
	public Face[] faces; // The faces comprising the wire-frame.
	
	/**
	 * Creates a default (empty) instance of <code>ObjWireFrame</code>.
	 */
	public ObjWireFrame() {
		this(new Point3D[0], new Point3D[0], new Face[0]);
	}

	/**
	 * Creates an instance of <code>ObjWireFrame</code>. Subsequent changes to
	 * the supplied arrays will reflect upon this <code>ObjWireFrame</code>
	 * 
	 * Will assert {@link #isValid()}, if assertions are on.
	 * 
	 * @param vertices the array of vertices comprising the wire-frame.
	 * @param normals the array of normals comprising the wire-frame.
	 * @param faces the array of {@link Face}s comprising the wire-frame.
	 */
	public ObjWireFrame(Point3D[] vertices, Point3D[] normals, Face[] faces) {
		this.vertices = vertices;
		this.normals = normals;
		this.faces = faces;
		
		assert isValid() : "ObjWireFrame is invalid.";
	}
	
	/**
	 * Returns a <code>String</code> representation of this <code>ObjWireFrame
	 * </code>. It is generally rather long and is split into multiple lines.
	 * 
	 * @return this objects <code>String</code> representation.
	 */
	@Override
	public String toString() {
		return "ObjWireFrame: [\n  "
			   + "Vertices = " + Arrays.deepToString(vertices) + ",\n  "
			   + "Normals = " + Arrays.deepToString(normals) + ",\n  "
			   + "Faces = " + Arrays.deepToString(faces) + "\n]";
	}
	
	/**
	 * Returns whether this <code>ObjWireFrame</code> is valid. An <code>
	 * ObjWireFrame</code> is invalid if any of <code>vertices</code>,
	 * <code>normals</code>, or <code>faces</code> are null; if any normal
	 * vectors (whether referenced or not) are non-normalized; or if any of the
	 * indices in any of the {@link Face}s refers to an invalid position in this
	 * {@link ObjWireFrame}'s respective array.
	 * 
	 * @return <code>true</code>, if the <code>ObjWireFrame</code> is valid, <code>false</code> otherwise.
	 */
	public boolean isValid() {
		// None of these should be null.
		if ( vertices == null ) {
			GWT.log("vertices is null.");
			return false;
		}
		if ( normals == null ) {
			GWT.log("normals is null.");
			return false;
		}
		if ( faces == null ) {
			GWT.log("faces is null.");
			return false;
		}
		
		for ( Point3D normal : normals ) {
			if ( Math.abs(normal.getMagnitude() - 1) > 0.0001 ) {
				// Normals must be normalized.
				GWT.log("Invalid normal: " + normal + ", magnitude: "
						+ normal.getMagnitude());
				return false;
			}
		}
		
		// Validate each face.
		for ( Face face : faces ) {
			int normal = face.getNormal();
			
			if ( normal != -1 ) { // If we have a normal.
				if ( normal < 0 || normal >= normals.length ) {
					// Not a valid index into the normals array.
					GWT.log("Invalid normal index: " + normal + " not in [0,"
							+ normals.length + ")");
					return false;
				}
			}
			
			for ( int vertex : face.getVertices() ) {
				// Not a valid index into the vertices array.
				if ( vertex < 0 || vertex >= vertices.length ) {
					GWT.log("Invalid vertex index: " + vertex + " not in [0,"
							+ face.getVertexCount() + ")");
					return false;
				}
			}
		}
		
		return true;
	}
	
}
