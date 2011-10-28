package uk.ac.cam.cl.dtg.android.time.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import uk.ac.cam.cl.dtg.android.time.buses.*;
import uk.ac.cam.cl.dtg.android.time.data.handler.QueryHandler;


public class StopArrivalsServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	// How many arrivals to fetch by default
	public static final int DEFAULT_ARRIVAL_COUNT = 5;	
	
	// Our DB connection
	Connection conn;
	
	// The statement that will do the dirty work
	PreparedStatement lookupHandler;

	@Override
	public void init() throws ServletException {

		super.init();
		try {
		  // Get connection to DB
		  conn = DatabaseManager.getConnection();

		  // Define our prepared query
		  String sql = "select handler from available_stops"
		      + " left outer join data_providers"
		      + " on data_providers.provider=available_stops.data_source"
		      + " where available_stops.atco_code=?";

		  lookupHandler = conn.prepareStatement(sql);

		} catch (SQLException e) {

		  e.printStackTrace();

		}

	}

	/**
	 * Handle a GET request
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest req, HttpServletResponse res)	throws ServletException, IOException
	{
		// Set output type.
		res.setContentType( ServletUtils.MIME_XML );
		
		// Start try block. Any exception results in error for whole request.
		try {
			
			// Check if key is valid
		  ServletUtils.checkKeyForServices(req);
		
			// Check to see if we specified a stopref atco
			String ATCOCode = ServletUtils.getRequiredParameter(req,"atco");
			
			// Have we specified number of arrivals to fetch?
			int numarrivals = ServletUtils.getIntParameter(req, "numarrivals", DEFAULT_ARRIVAL_COUNT);

			// Get the relevant handler out of database
			lookupHandler.setString(1,ATCOCode);
			ResultSet rs = lookupHandler.executeQuery();
			rs.next();
			String handler = rs.getString("handler");
			
			// Create a handler
			Class handlerClass = Class.forName(handler);
			QueryHandler dataSourceHandler = (QueryHandler) handlerClass.newInstance();
			
			// Get the stop info
			//List<BusArrival> nextArrivals = CouncilDataSource.getBusArrivalData(new BusStop("",0,0,req.getParameter("atco")), numarrivals).NextBuses;

			// Get next arrivals
			List<BusArrival> nextArrivals = dataSourceHandler.listArrivals(ATCOCode, numarrivals); 
			
			// Get the output writer
			PrintWriter out = res.getWriter();
			
			// Create an XML output factory
			XMLOutputFactory factory = XMLOutputFactory.newInstance();			
			XMLStreamWriter writer = factory.createXMLStreamWriter(out);
			
			// Start the document
			writer.writeStartDocument();
			
			// Start <response> tag
			writer.writeStartElement("response");
			
			// Write <arrivals> tag
			writer.writeStartElement("arrivals");
			writer.writeAttribute("count", String.valueOf(nextArrivals.size()));
			
			// Write the <arrival> information
			for(BusArrival arrival : nextArrivals) {
				
				writer.writeStartElement("arrival");
				
				writer.writeStartElement("service");
				writer.writeCharacters(arrival.getServiceID());
				writer.writeEndElement();
				
				writer.writeStartElement("destination");
				writer.writeCharacters(arrival.getDestination());
				writer.writeEndElement();
				
				writer.writeStartElement("time");
				
				writer.writeStartElement("millis");
				writer.writeCharacters(String.valueOf(arrival.getDueTime().getTime()));
				writer.writeEndElement();
				
				writer.writeStartElement("textual");
				writer.writeCharacters(arrival.getDueTime().toString());
				writer.writeEndElement();
				
				writer.writeStartElement("isdue");
				writer.writeCharacters(String.valueOf(arrival.getDueTime().isDue));
				writer.writeEndElement();
				
				writer.writeStartElement("islive");
				writer.writeCharacters(String.valueOf(arrival.getDueTime().isLiveData));
				writer.writeEndElement();
				
				writer.writeEndElement();
				
				writer.writeEndElement();
				
			}
			
			// Close... </arrivals>
			writer.writeEndElement();
			
			// Close... </response>
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
		return "Retrieves live arrivals for a stop. By David Tattersall";
	}
}
