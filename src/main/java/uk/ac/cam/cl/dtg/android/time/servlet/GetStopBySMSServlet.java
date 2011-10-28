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
 * Not exposed as part of OmniBus - should instead use LookupStop.
 * This is here to support Cambridgeshire bus app
 * 
 * @author dt316
 *
 */
public class GetStopBySMSServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;


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

			// Check if key is valid
			if(req.getParameter("smscode")==null) throw new Exception("SMS code is required");
			
			// Get the data from server
			BusStop stop = CouncilDataSource.lookupStopBySMS(req.getParameter("smscode"));

			// Get the output writer
			PrintWriter out = res.getWriter();

			// Create an XML output factory
			XMLOutputFactory factory = XMLOutputFactory.newInstance();			
			XMLStreamWriter writer = factory.createXMLStreamWriter(out);

			// Start the document
			writer.writeStartDocument();

			// Start <response> tag
			writer.writeStartElement("response");


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

	public String getServletInfo()
	{
		return "GetStopByInfoServlet by David Tattersall";
	}
}
