package car.orientor.client.wfio.obj;

/**
 * Represents a face of a {@link ObjWireFrame}. The vertex and normal values
 * index into the vertex and normal arrays of the enclosing <code>ObjWireFrame
 * </code>.
 * 
 * @author Joshua Little
 */
public class Face {
	private int[] vertices; // Vertex indices.
	private int normal; // Normal index.
	
	/**
	 * Creates and new instance of <code>Face</code>.
	 * 
	 * @param vertices the vertex indices to use.
	 * @param normal the normal index to use.
	 */
	public Face(int[] vertices, int normal) {
		setVertices(vertices);
		setNormal(normal);
	}
	
	/**
	 * Returns the vertex indices array. Changes to this array will reflect on
	 * the owning <code>Face</code>
	 * 
	 * @return the vertex indices array.
	 * @see #setVertices(int[])
	 */
	public int[] getVertices() {
		return vertices;
	}

	/**
	 * Changes this <code>Face</code>'s vertex indices array to the supplied
	 * value. Future changes to the supplied array will reflect upon this <code>
	 * Face</code>.
	 * 
	 * @param points the new vertex indices array.
	 * @see #getVertices()
	 */
	public void setVertices(int[] points) {
		this.vertices = points;
	}
	
	/**
	 * Returns the face at the specified index.
	 * 
	 * @param index the index of the vertex to be returned.
	 * @return The vertex at the specified index.
	 * @throws ArrayIndexOutOfBoundsException if the index is outside of the array bounds.
	 * @see #setVertex(int, int)
	 */
	public int getVertex(int index) {
		return vertices[index];
	}

	/**
	 * Changes the face at the specified index to the supplied value.
	 * 
	 * @param index the index of the face to change.
	 * @param vertex the new vertex for the specified index..
	 * @throws ArrayIndexOutOfBoundsException if the index is outside of the array bounds.
	 * @see #getVertex(int)
	 */
	public void setVertex(int index, int vertex) {
		vertices[index] = vertex;
	}
	
	/**
	 * Returns the number of this <code>Face</code>'s vertex indices.
	 * 
	 * @return the number of vertex indices.
	 */
	public int getVertexCount() {
		return vertices.length;
	}
	
	/**
	 * Returns this <code>Face</code>'s normal index.
	 * 
	 * @return the normal index.
	 * @see #setNormal(int)
	 */
	public int getNormal() {
		return normal;
	}

	/**
	 * Returns this <code>Face</code>'s normal index.
	 * 
	 * @param normal the new normal index.
	 * @see #getNormal()
	 */
	public void setNormal(int normal) {
		this.normal = normal;
	}
	
	/**
	 * Returns the <code>String</code> representation of this face.<br /><br />
	 * 
	 * Ex.</br>
	 * <code>Face: [Normal = (0.3, 0.4, 0.5), Vertices = [</code>some points
	 * <code>]]</code>
	 * 
	 * @return the <code>String</code> representation of this face.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Face: [Normal = ").append(normal).append(", Vertices = [");

		if ( vertices.length > 0 ) {
			sb.append(vertices[0]);
			
			for ( int i = 0; i < vertices.length; i++ ) {
				sb.append(", ").append(vertices[i]);
			}
		}
		
		sb.append("]]");
		
		return sb.toString();
	}
	
}
