package car.orientor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import car.shared.math.Point3D;
import car.shared.views3d.obj.ObjIO;
import car.shared.views3d.obj.ObjWireFrame;

/**
 * This class can be run to normal a .obj file into the coordinate system that
 * {@link car.shared.views3d.WireFrameView} uses.
 * 
 * Usage: <in_file> <out_file>
 * 
 * Currently just centers the object at zero, and scales it so that its
 * maximum dimension is 0.75.
 * 
 * @author Joshua Little
 */
public class ObjNormalizer {
	private static final double MAX_MEASURE = 0.75;
	
	
	public static void main(String[] args) throws IOException {
		if ( args.length == 1 ) {
			if ( !args[0].equals("batch_it") ) {
				System.out.println("Usage: <in_file> <out_file>");
				return;
			}
			
			// Convert these files, which is probably what I would want.
			
			convert("war/rawModels/carPoly.obj", "war/sedan.obj");
			convert("war/rawModels/hatchbackPoly.obj", "war/hatchback.obj");
			convert("war/rawModels/pickup.obj", "war/pickup.obj");
			convert("war/rawModels/suvPoly.obj", "war/suv.obj");
			convert("war/rawModels/vanPoly.obj", "war/van.obj");
		} else { 
			if ( args.length != 2 ) {
				System.out.println("Usage: <in_file> <out_file>");
				return;
			}
			
			convert(args[0], args[1]);
		}
	}
	
	/**
	 * Converts the specified .obj file. Transforms the file referenced by the
	 * first <code>String</code> into the coordinate system that
	 * {@link car.shared.views3d.WireFrameView} uses and stores it in the
	 * file referenced by the second <code>String</code>.
	 * 
	 * @param fromFile the in file.
	 * @param toFile the out file.
	 * @throws IOException if there is an error reading from or writing two one of the files.
	 */
	public static void convert(String fromFile, String toFile)
														throws IOException {
		File inFile = new File(fromFile);
		File outFile = new File(toFile);
		
		BufferedReader fin = new BufferedReader(new FileReader(inFile));
		
		StringBuilder contents = new StringBuilder();
		
		// Read in the entire file.
		String line = fin.readLine();
		while ( line != null ) {
			contents.append(line).append('\n');
			line = fin.readLine();
		}
		
		fin.close();
		
		// Parse the file into an ObjWireFrame.
		ObjWireFrame wireFrame = ObjIO.parseObjFile(contents.toString());

		fixWireFrame(wireFrame); // Fix it.
		
		// Write it out again.
		OutputStream fout = new FileOutputStream(outFile);
		try {
			ObjOut.printObjFile(wireFrame, fout);
		} finally {
			fout.close();
		}
	}
	
	/**
	 * Fixes the wire-frame so that it is in the coordinate system that
	 * {@link car.shared.views3d.WireFrameView} uses.
	 * 
	 * @param wireFrame the wire-frame to fix.
	 */
	public static void fixWireFrame(ObjWireFrame wireFrame) {
		Point3D mean = findMeanPoint(wireFrame); // Find the average point.
		
		for ( Point3D vertex : wireFrame.vertices ) {
			// Translate the wire-frame so that that point lies on the origin.
			vertex.subtract(mean);
		}
		
		// Scale the wire-frame so that it's maximum dimension is 0.75.
		double scale = MAX_MEASURE / findMaxMeasure(wireFrame);
		
		for ( Point3D vertex : wireFrame.vertices ) {
			vertex.scale(scale);
		}
	}
	
	/**
	 * Finds the average point in the supplied wire-frame.
	 * 
	 * @param wireFrame the wire-frame to find the average point of.
	 * @return the average point.
	 */
	public static Point3D findMeanPoint(ObjWireFrame wireFrame) {
		Point3D mean = new Point3D(0.0, 0.0, 0.0);
		
		for ( Point3D vertex : wireFrame.vertices ) {
			mean.add(vertex);
		}

		mean.scale(1.0 / wireFrame.vertices.length);
		return mean;
	}
	
	/**
	 * Finds the maximum dimension of the supplied wire-frame.
	 * 
	 * @param wireFrame the wire-frame to find the maximum dimension of.
	 * @return the maximum dimension's measure.
	 */
	public static double findMaxMeasure(ObjWireFrame wireFrame) {
		double maxMeasure = 1.0;
		
		for ( Point3D vertex : wireFrame.vertices ) {
			double absX = Math.abs(vertex.x);
			double absY = Math.abs(vertex.y);
			double absZ = Math.abs(vertex.z);
			
			maxMeasure = Math.max(maxMeasure, absX);
			maxMeasure = Math.max(maxMeasure, absY);
			maxMeasure = Math.max(maxMeasure, absZ);
		}
		
		return maxMeasure;
	}
	
}
