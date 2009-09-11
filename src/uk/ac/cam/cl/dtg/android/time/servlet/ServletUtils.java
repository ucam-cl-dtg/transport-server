package uk.ac.cam.cl.dtg.android.time.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

public class ServletUtils {

	public static void PrintException(HttpServletResponse res, Exception e) {
		
		try {
			PrintWriter out = res.getWriter();
			
			// Create an XML output factory
			XMLOutputFactory factory = XMLOutputFactory.newInstance();			
			XMLStreamWriter writer = factory.createXMLStreamWriter(out);			
			
			StringWriter stringWriter = new StringWriter();
			String stackTrace = null;
			e.printStackTrace(new PrintWriter(stringWriter));

			// get the stackTrace as String...
			stackTrace = stringWriter.toString();
			
			// Write to the document
			writer.writeStartDocument();
			writer.writeStartElement("response");
			writer.writeStartElement("error");
			writer.writeCharacters(stackTrace);
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndDocument();
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}
}
