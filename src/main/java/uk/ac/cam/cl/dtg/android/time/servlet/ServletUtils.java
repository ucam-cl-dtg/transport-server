package uk.ac.cam.cl.dtg.android.time.servlet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import uk.ac.cam.cl.dtg.android.time.servlet.KeyManager.InvalidKeyException;

/**
 * Class containing some useful service-wide stuff.
 * 
 * @author dt316
 * 
 */
public class ServletUtils {

	public static final String MIME_XML = "application/xml";

	/**
	 * Outputs an exception in a nice format.
	 * 
	 * @param res
	 * @param e
	 */
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

	/**
	 * Get the parameter name from the request req and if it is null throw a
	 * RequiredParameterException
	 * 
	 * @param req
	 *            is the HttpServletRequest object for this request
	 * @param name
	 *            of the HTTP request value
	 * @param validOps
	 *            if provided will ensure that the returned String is one of
	 *            these values
	 * @return the parameter value, not null
	 * @throws RequiredParameterException
	 *             if the parameter is not specified
	 * @throws InvalidParameterException
	 */
	public static String getRequiredParameter(HttpServletRequest req,
			String name, String... validOpts) throws BadParameterException {
		String answer = req.getParameter(name);
		if (answer == null) {
			throw new RequiredParameterException(name);
		}
		if (validOpts != null && validOpts.length != 0) {
			for (String s : validOpts)
				if (answer.equals(s))
					return s;
			throw new InvalidParameterException(
					"Provided parameter not in expected list");
		} else {
			return answer;
		}
	}

	public static void checkKeyForServices(HttpServletRequest req, Connection db)
			throws InvalidKeyException, BadParameterException {
		KeyManager.isValidForServices(
				ServletUtils.getRequiredParameter(req, "key"), db);
	}

	public static int getIntParameter(HttpServletRequest req, String name,
			int defaultValue) throws InvalidParameterException {
		String value = req.getParameter(name);
		if (value == null) {
			return defaultValue;
		} else {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new InvalidParameterException("Expected parameter "
						+ name + " to be an integer but was: " + value);
			}
		}
	}

	public static class BadParameterException extends Exception {
		private static final long serialVersionUID = 1L;

		public BadParameterException(String message) {
			super(message);
		}
	}

	/**
	 * Thrown if a required parameter is not specified.
	 * 
	 * @author drt24
	 * 
	 */
	public static class RequiredParameterException extends
			BadParameterException {
		private static final long serialVersionUID = 1L;

		public RequiredParameterException(String name) {
			super("Must supply " + name + " parameter.");
		}
	}

	public static class InvalidParameterException extends BadParameterException {
		private static final long serialVersionUID = 1L;

		public InvalidParameterException(String message) {
			super(message);
		}
  }

  public static DataSource getDataSource() throws NamingException {
    Context ctx = new InitialContext();
    return (DataSource) ctx.lookup("java:comp/env/jdbc/generaldb");
  }

  public static Connection getConnection() throws SQLException, NamingException {
    return getDataSource().getConnection();
  }
}
