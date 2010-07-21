package car.orientor.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import car.shared.math.Point3D;
import car.shared.views3d.obj.Face;
import car.shared.views3d.obj.ObjWireFrame;

/**
 * This class supplies methods that can write
 * {@link car.shared.views3d.obj.ObjWireFrame}s out to files. This package
 * is not compiled into JavaScript, so this class can use the entire <code>
 * java.io</code> package.
 * 
 * @author Joshua Little
 */
public abstract class ObjOut {
	
	// Abstract + private constructor = non-instantiable.
	private ObjOut() {}
	
	/**
	 * Writes the supplied {@link car.shared.views3d.obj.ObjWireFrame} out
	 * to the output stream as a valid .obj file.
	 * 
	 * @param obj the <code>ObjWireFrame</code> to write out.
	 * @param out the output stream to write the <code>ObjWireFrame</code> to.
	 * @throws IOException if the <code>OutputStream</code> throws an <code>IOException</code>.
	 */
	public static void printObjFile(ObjWireFrame obj, OutputStream out)
															throws IOException {
		PrintStream sout = new PrintStream(out);
		
		// Print all of the vertices.
		for ( Point3D vertex : obj.vertices ) {
			sout.println(formatVertex(vertex));
		}
		
		sout.println();
		
		// Print all of the normals.
		for ( Point3D normal : obj.normals ) {
			sout.println(formatNormal(normal));
		}
		
		sout.println();
		
		// Print all of the faces.
		for ( Face face : obj.faces ) {
			sout.println(formatFace(face));
		}
		
		sout.close();
	}
	
	/**
	 * Formats the vertex into a line that can be written to an .obj file.
	 * 
	 * @param vertex the vertex to format.
	 * @return the formatted line.
	 */
	public static String formatVertex(Point3D vertex) {
		return "v " + vertex.x + " " + vertex.y + " " + vertex.z;
	}

	/**
	 * Formats the normal into a line that can be written to an .obj file.
	 * 
	 * @param normal the normal to format.
	 * @return the formatted line.
	 */
	public static String formatNormal(Point3D normal) {
		return "vn " + normal.x + " " + normal.y + " " + normal.z;
	}
	
	/**
	 * Formats the {@link car.shared.views3d.obj.Face} into a line that
	 * can be written to an .obj file.
	 * 
	 * @param face the <code>Face</code> to format.
	 * @return the formatted line.
	 */
	public static String formatFace(Face face) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("f");
		
		int normal = face.getNormal();
		
		if ( normal == -1 ) {
			// If there's no normal, we just write out the indices.
			
			for ( int index : face.getVertices() ) {
				sb.append(" ").append(index+1); // Obj files index from 1.
			}
		} else {
			// If there's a normal, we need to write that out in each token,
			// as well.
			
			String normalStr = "//" + (normal+1); // Obj files index from 1.
			
			for ( int index : face.getVertices() ) {
				sb.append(" ").append(index+1).append(normalStr); // Obj files index from 1.
			}
		}
		
		return sb.toString();
	}
	
}
