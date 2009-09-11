package uk.ac.cam.cl.dtg.android.time.servlet;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.stream.*;

public class ListStopPointsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	Connection conn;
	PreparedStatement searchStatement;

	@Override
	public void init() throws ServletException {

		super.init();
		
		// Get connection to DB
		conn = DatabaseManager.getConnection();
		
		String sql = "select"
			+ " naptan_group_memberships.atco_code AS atco_code,"
			+ " available_stops.stop_name AS ds_name,"
			+ " naptan_extended_stop_info.lat AS lat,"
			+ " naptan_extended_stop_info.long AS long,"
			+ " naptan_extended_stop_info.naptan_code AS naptan_code," 
			+ " naptan_extended_stop_info.common_name AS common_name,"
			+ " naptan_extended_stop_info.short_name AS short_name,"
			+ " naptan_extended_stop_info.landmark AS landmark,"
			+ " naptan_extended_stop_info.street AS street,"
			+ " naptan_extended_stop_info.location_indicator AS location_indicator,"
			+ " naptan_extended_stop_info.bearing AS bearing"
			+ " from"
			+ " naptan_group_memberships"
			+ " left outer join available_stops"
			+ " on (available_stops.atco_code = naptan_group_memberships.atco_code)"
			+ " left outer join naptan_extended_stop_info"
			+ " on (naptan_group_memberships.atco_code = naptan_extended_stop_info.atco_code)"
		    + " where group_ref=?";
		
		try {
			searchStatement = conn.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		

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
			
			// New statement object to do query
			Statement st = conn.createStatement();
			
			// Check to see if a group ref was supplied
			String groupref = req.getParameter("groupref");
			if(groupref==null) throw new Exception("Must supply a groupref.");
			
			// Set group ref parameter and get results
			searchStatement.setString(1, groupref);			
			ResultSet rs = searchStatement.executeQuery();

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
			writer.writeStartElement("stops");
			
			while (rs.next()) {
				
				// Write <stop> tag

				writer.writeStartElement("stop");
				writer.writeAttribute("haslivedata", (rs.getString(2)!=null) ? "true" : "false");
				
				// Write ref
				writer.writeStartElement("atco");
				writer.writeCharacters(rs.getString("atco_code"));
				writer.writeEndElement();
				
				// Write naptan code
				writer.writeStartElement("naptan");
				writer.writeCharacters(rs.getString("naptan_code"));
				writer.writeEndElement();

				// Write <location> info
				writer.writeStartElement("location");
				
				// Write lat
				writer.writeStartElement("lat");
				writer.writeCharacters(rs.getString("lat"));
				writer.writeEndElement();
				
				// Write long
				writer.writeStartElement("long");
				writer.writeCharacters(rs.getString("long"));
				writer.writeEndElement();
				
				// write street
				writer.writeStartElement("street");
				writer.writeCharacters(rs.getString("street"));
				writer.writeEndElement();
				
				// write street
				writer.writeStartElement("landmark");
				writer.writeCharacters(rs.getString("landmark"));
				writer.writeEndElement();
				
				// write street
				writer.writeStartElement("indicator");
				writer.writeCharacters(rs.getString("location_indicator"));
				writer.writeEndElement();
				
				// close </location>
				writer.writeEndElement();
				
				// write out naming info
				writer.writeStartElement("naming");				
				writer.writeStartElement("common");
				writer.writeCharacters(rs.getString("common_name"));
				writer.writeEndElement();
				writer.writeStartElement("short");
				writer.writeCharacters(rs.getString("short_name"));
				writer.writeEndElement();
				writer.writeEndElement();
				
				// write bearing
				writer.writeStartElement("direction");
				writer.writeCharacters(rs.getString("bearing"));
				writer.writeEndElement();
				
				// End stop tag
				writer.writeEndElement();
			
			}
			
			// close </stops> and </response>
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
