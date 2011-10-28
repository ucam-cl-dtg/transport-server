package uk.ac.cam.cl.dtg.android.time.servlet;

import java.io.*;
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
 * Not exposed as part of OmniBus - should instead use FindStopGroups / ListStopPoints.
 * This is here to support Cambridgeshire bus app
 * 
 * @author dt316
 *
 */
public class GetStopsServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	// Holds all cached results
	Map<String, List<BusStop>> cachedResults;

	// Holds the times results were last fetched
	Map<String, Double> lastCache;

	// How long before cached results expire and must be reloaded
	public static final int CACHE_TIMEOUT = 43200000;

	@Override
	public void init() throws ServletException {

		super.init();

		// Initialise the storage for mem-caching results
		cachedResults = new HashMap<String, List<BusStop>>();
		lastCache = new HashMap<String, Double>();


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

			// Get system time - for timing how long download takes (debug)
			double t = System.currentTimeMillis();
			
			// Has the request already been cached?
			List<BusStop> stops = getCachedResult(req.getParameter("level"));	

			// If not grab from server
			//TODO: fix
			if(stops==null){
				stops = CouncilDataSource.getBusStops(Integer.parseInt(req.getParameter("level")));
				cacheResult(req.getParameter("level"), stops);
			}
			double dltime = System.currentTimeMillis() - t;
			
			// Get the output writer
			PrintWriter out = res.getWriter();
			
			// Create an XML output factory
			XMLOutputFactory factory = XMLOutputFactory.newInstance();			
			XMLStreamWriter writer = factory.createXMLStreamWriter(out);
			
			// Start the document
			writer.writeStartDocument();
			
			// Start <response> tag
			writer.writeStartElement("response");

			// Start <stops> tag + add count info
			writer.writeStartElement("stops");
			writer.writeAttribute("count", String.valueOf(stops.size()));

			// Internal timing
			t = System.currentTimeMillis();
			
			// Loop through each result...
			for(BusStop stop : stops) {

				// Start <stop> tag
				writer.writeStartElement("stop");

				// Write <name>
				writer.writeStartElement("name");
				writer.writeCharacters(stop.getName());
				writer.writeEndElement();
				
				// Write <ref>
				writer.writeStartElement("ref");
				writer.writeCharacters(stop.getStopRef());
				writer.writeEndElement();
				
				// Write <lat>
				writer.writeStartElement("lat");
				writer.writeCharacters(String.valueOf(stop.getLatitude()));
				writer.writeEndElement();
				
				// Write <long>
				writer.writeStartElement("long");
				writer.writeCharacters(String.valueOf(stop.getLongitude()));
				writer.writeEndElement();
				
				// Close... </stop>
				writer.writeEndElement();
			}
			double looptime = System.currentTimeMillis() - t;
			
			// Close... </stops>
			writer.writeEndElement();

			/*		writer.writeStartElement("downloadtime");
			writer.writeCharacters(String.valueOf(dltime));
			writer.writeEndElement();

			writer.writeStartElement("looptime");
			writer.writeCharacters(String.valueOf(looptime));
			writer.writeEndElement(); */
			
			// Close... </response>
			writer.writeEndElement();

			// Close document
			writer.writeEndDocument();
			
			// Flush + send output
			writer.flush();			
			writer.close();

		} catch(Exception e) {
			
			// Any errors that occur, we need to know about them
			writeException(res,e);
			
		}


	}

	/**
	 * Write out an exception to the response
	 * @param res Response object
	 * @param e Exception that occurred
	 */
	private void writeException(HttpServletResponse res, Exception e) {

		PrintWriter out;

		try {
			out = res.getWriter();
			out.write("<?xml version=\"1.0\" ?><response><error>" + e.getClass().getSimpleName() + " : " + e.getMessage() + "</error></response>");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		


	}

	/**
	 * Returns a lookup result that was previously cached. If the result has not been cached, returns null
	 * @param key Key uniquely identifying the lookup result
	 * @return
	 */
	private List<BusStop> getCachedResult(String key) {

		if(!lastCache.containsKey(key)) return null;

		double time = lastCache.get(key);

		if(System.currentTimeMillis()-time > CACHE_TIMEOUT) return null;

		return cachedResults.get(key);
	}
	
	/**
	 * Stores a cached copy of a lookup result
	 * 
	 * @param key Key uniquely identifying the lookup result
	 * @param data Result to cache
	 */
	private void cacheResult(String key, List<BusStop> data) {

		lastCache.put(key, (double)System.currentTimeMillis());
		cachedResults.put(key, data);
	}

	public String getServletInfo()
	{
		return "BusStopServelet by David Tattersall";
	}
}
