package uk.ac.cam.cl.dtg.android.time.servlet;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.stream.*;

import uk.ac.cam.cl.dtg.android.time.buses.*;
import uk.ac.cam.cl.dtg.android.time.data.CouncilDataSource;

/**
 * Provides XML/RPC interface to bus stop position web service
 * 
 * @author dt316
 *
 */
public class LookupStopServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	// Our DB connection
	Connection conn;
	
	// Ready-made statements for the two query types
	PreparedStatement searchByATCO;
	PreparedStatement searchByNaPTAN;

	@Override
	public void init() throws ServletException {

		super.init();

		// Get connection to DB
		try {
		  conn = DatabaseManager.getConnection();

		  // Query by ATCO
		  String sqlATCO = "select"
		      + " naptan_extended_stop_info.atco_code AS atco_code,"
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
		      + " naptan_extended_stop_info"
		      + " left outer join available_stops"
		      + " on (available_stops.atco_code = naptan_extended_stop_info.atco_code)"
		      + " where naptan_extended_stop_info.atco_code=? limit 1";

		  // Query by NaPTAN
		  String sqlNaPTAN = "select"
		      + " naptan_extended_stop_info.atco_code AS atco_code,"
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
		      + " naptan_extended_stop_info"
		      + " left outer join available_stops"
		      + " on (available_stops.atco_code = naptan_extended_stop_info.atco_code)"
		      + " where naptan_extended_stop_info.naptan_code=? limit 1";



		  searchByATCO = conn.prepareStatement(sqlATCO);
		  searchByNaPTAN = conn.prepareStatement(sqlNaPTAN);

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

			// Check to see if an atco or naptan code was supplied
			String naptan = req.getParameter("naptan");
			String atco = req.getParameter("atco");
			if(naptan==null && atco==null) throw new Exception("Must supply either an ATCO or NaPTAN code.");

			// Choose the correct query
			PreparedStatement s;
			if(naptan==null) {
				s = searchByATCO;
				s.setString(1, atco);
			} else {
				s = searchByNaPTAN;
				s.setString(1, naptan);
			}
			
			ResultSet rs = s.executeQuery();

			// Get the output writer
			PrintWriter out = res.getWriter();

			// Create an XML output factory
			XMLOutputFactory factory = XMLOutputFactory.newInstance();			
			XMLStreamWriter writer = factory.createXMLStreamWriter(out);

			// Start the document
			writer.writeStartDocument();

			// Write <response> tag
			writer.writeStartElement("response");

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
		return "GetStopByInfoServlet by David Tattersall";
	}
}

