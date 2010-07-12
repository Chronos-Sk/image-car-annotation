package car.orientor.client.wfio;

import car.orientor.client.WireFrame;
import car.shared.math.Point3D;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

@Deprecated
public abstract class XMLIO {
	
	private XMLIO() {}

	public static WireFrame createFromURL(String url, final Command onLoad) {
		WireFrame dest = new WireFrame();
		createFromURL(dest, url, onLoad);
		
		return dest;
	}
	
	public static WireFrame createFromURL(final WireFrame dest, String url, final Command onLoad) {
		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, url);
		
		rb.setCallback(new RequestCallback() {
			@Override
			public void onError(Request request, Throwable exception) {
				throw new RuntimeException(exception);
			}
			
			@Override
			public void onResponseReceived(Request request, Response response) {
				WireFrame temp = createFromXML(response.getText());
				
				dest.points = temp.points;
				dest.lines = temp.lines;
				
				if ( onLoad != null ) {
					onLoad.execute();
				}
			}
		});
		
		try {
			rb.send();
		} catch (RequestException ex) {
			throw new RuntimeException(ex);
		}
		
		return dest;
	}
	
	public static WireFrame createFromXML(String xmlContent) {
		Document xmlDoc = null;
		
		try {
			xmlDoc = XMLParser.parse(xmlContent);
			return createFromXML(xmlDoc);
		} catch ( Exception ex ) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static WireFrame createFromXML(Document xmlDoc) {
		Point3D[] points = readPoints(xmlDoc);
		int[] lines = readLines(xmlDoc);
		
		return new WireFrame(points, lines);
	}
	
	public static Point3D[] readPoints(Document xmlDoc) {
		Element pointsElement = 
			(Element) xmlDoc.getDocumentElement().getElementsByTagName("points").item(0);
		
		NodeList pointsList = pointsElement.getElementsByTagName("point");
		
		Point3D[] points = new Point3D[pointsList.getLength()];
		
		for ( int i = 0; i < pointsList.getLength(); i++ ) {
			points[i] = parsePoint(pointsList.item(i));
		}
		
		return points;
	}
	
	public static Point3D parsePoint(Node pointNode) {
		NodeList coords = pointNode.getChildNodes();
		
		double x = nodeToDouble(coords.item(0));
		double y = nodeToDouble(coords.item(1));
		double z = nodeToDouble(coords.item(2));
		
		return new Point3D(x, y, z);
	}
	
	public static int[] readLines(Document xmlDoc) {
		Element linesElement = 
			(Element) xmlDoc.getDocumentElement().getElementsByTagName("lines").item(0);
		
		NodeList linesList = linesElement.getElementsByTagName("line");
		
		int[] points = new int[2*linesList.getLength()];
		
		Node lineNode;
		for ( int i = 0; i < linesList.getLength(); i++ ) {
			lineNode = linesList.item(i);
			points[2*i]   = nodeToInt(lineNode.getFirstChild());
			points[2*i+1] = nodeToInt(lineNode.getLastChild());
		}
		
		return points;
	}

	public static double nodeToDouble(Node textNode) {
		return Double.parseDouble(((Text) textNode.getFirstChild()).getData().trim());
	}

	public static int nodeToInt(Node textNode) {
		return Integer.parseInt(((Text) textNode.getFirstChild()).getData().trim());
	}

}
