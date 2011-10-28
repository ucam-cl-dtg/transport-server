package uk.ac.cam.cl.dtg.android.time.servlet;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.stream.*;


/**
 * Queries the database and returns all stop groups matching certain criteria
 * 
 * @author dt316
 *
 */
public class FindStopGroupsServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static int DEFAULT_RESULTS_RETURNED = 10;
	
	 // looks for groups only within 10km
	private static int DEFAULT_SEARCH_RADIUS = 10000;

	@Override
	public void init() throws ServletException {

		super.init();

	}

	/**
	 * Handle a GET request
	 */
	protected void doGet(HttpServletRequest req,
			HttpServletResponse res)
	throws ServletException, IOException
	{
		// Set output type.
		res.setContentType("application/xml");
		
		// Start try block. Any exception results in error for whole request.
		try {
			
			// Check if key is valid
			if(!KeyManager.isValidKey(req.getParameter("key"),"services")) throw new Exception("Invalid API key.");
			
			// Check to see if a method was supplied
			String method = req.getParameter("method");
			if(method==null) throw new Exception("Must supply method parameter.");
			if(!(method.equals("near") || method.equals("within"))) throw new Exception("Invalid method: either 'near' or 'within'");
			
			// Have we specified number of arrival to fetch?
			int numresults = DEFAULT_RESULTS_RETURNED;			
			if(req.getParameter("numresults") != null) numresults = Integer.parseInt(req.getParameter("numresults"));

			// Have we specified a search radius?
			int radius = DEFAULT_SEARCH_RADIUS;			
			if(req.getParameter("radius") != null) radius = Integer.parseInt(req.getParameter("radius"));
			double radiusInDegrees = ((double)radius) * 0.0000111111111;
			
			// Get connection to DB
			Connection conn = DatabaseManager.getConnection();
			
			// New statement object to do query
			Statement st = conn.createStatement();
			
			String sql = "";
			
			// Deal with 'near' query			
			if(method.equals("near")) {
				
				// Require lat/long
				if(req.getParameter("lat") == null || req.getParameter("long") == null) throw new Exception("Lat / Long required.");
				
				double lat = Double.parseDouble(req.getParameter("lat"));
				double lon = Double.parseDouble(req.getParameter("long"));
								
				sql = "select name,group_ref, lat, long, st_distance_sphere(geopoint, ST_GeomFromText('POINT(" + lon +" "+lat+")', -1)) as dist";
				sql = sql + " from naptan_groups";
				sql = sql + " where geopoint && Expand(ST_GeomFromText('POINT(" + lon + " "+ lat + ")', -1)," + radiusInDegrees+")";
				sql = sql + " order by dist asc";
				sql = sql + " limit " + numresults;	
								
			}
			
			// Deal with 'near' query			
			if(method.equals("within")) {
				
				// Require left/right/top/bottom
				if(req.getParameter("left") == null
						|| req.getParameter("right") == null
						|| req.getParameter("top") == null
						|| req.getParameter("bottom") == null)
					throw new Exception("Bounding box co-ordinates required.");
				
				double left = Double.parseDouble(req.getParameter("left"));
				double right = Double.parseDouble(req.getParameter("right"));
				double top = Double.parseDouble(req.getParameter("top"));
				double bottom = Double.parseDouble(req.getParameter("bottom"));
	
				sql = "select name,group_ref, lat, long";
				sql = sql + " from naptan_groups";
				sql = sql + " where geopoint && ST_GeomFromText('POLYGON((" + left + " "+ top + ", "
				+ right + " " + top+", "
				+ right + " " + bottom+", "
				+ left + " " + bottom + ", "
				+ left + " " + top + "))', -1)";
				sql = sql + " limit 10000";			
			}
			
			ResultSet rs = st.executeQuery(sql);


			// Get the output writer
			PrintWriter out = res.getWriter();
			
			// Create an XML output factory
			XMLOutputFactory factory = XMLOutputFactory.newInstance();			
			XMLStreamWriter writer = factory.createXMLStreamWriter(out);
			
			// Start the document
			writer.writeStartDocument();
			
			// Write <response> tag
			writer.writeStartElement("response");
			//writer.writeAttribute("sql", sql);
			
			// Write <groups> tag
			writer.writeStartElement("groups");
			
			while (rs.next()) {
				
				// Write <group> tag
				writer.writeStartElement("group");
				
				// Write ref
				writer.writeStartElement("ref");
				writer.writeCharacters(rs.getString(2));
				writer.writeEndElement();
				
				// Write name
				writer.writeStartElement("name");
				writer.writeCharacters(rs.getString(1));
				writer.writeEndElement();
				
				// Write lat
				writer.writeStartElement("lat");
				writer.writeCharacters(rs.getString(3));
				writer.writeEndElement();
				
				// Write long
				writer.writeStartElement("long");
				writer.writeCharacters(rs.getString(4));
				writer.writeEndElement();
				
				// Write distance?
				if(method.equals("near")) {
					// Write dist
					writer.writeStartElement("dist");
					writer.writeCharacters(rs.getString(5));
					writer.writeEndElement();					
				}
				
				// End group tag
				writer.writeEndElement();
			
			}
			
			// close </groups> and </response>
			writer.writeEndElement();
			writer.writeEndElement();
			
			// Close document
			writer.writeEndDocument();
			
			// Flush + send output
			writer.flush();			
			writer.close();

		} catch(Exception e) {
			
			// Any errors that occur, we need to know about them
			ServletUtils.PrintException(res, e);
			
		}


	}
	
	public String getServletInfo()
	{
		return "BusStopServlet by David Tattersall";
	}
}
