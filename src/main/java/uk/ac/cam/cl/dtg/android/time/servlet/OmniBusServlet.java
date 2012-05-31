package uk.ac.cam.cl.dtg.android.time.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

public abstract class OmniBusServlet extends HttpServlet {
	private static final long serialVersionUID = 7072675699040951250L;

	protected final Logger log = Logger.getLogger(this.getClass().getCanonicalName());
	private DataSource dataSource;

	/**
	 * Look up the DataSource for the database from the servlet context. You
	 * need to configure your servlet container (e.g. tomcat) to provide this.
	 * With something like:
	 * 
	 * <Resource auth="Container" driverClassName="org.postgresql.Driver"
	 * maxActive="8" maxIdle="4" name="jdbc/generaldb" password="transport"
	 * type="javax.sql.DataSource" url="jdbc:postgresql://localhost/transport"
	 * username="urop09"/>
	 * 
	 * You also need to make the driver classes available on the continer
	 * classpath rather than part of the webapp itself
	 * 
	 * @return the DataSource for the database
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			dataSource = ServletUtils.getDataSource();
		} catch (NamingException e) {
			throw new ServletException(e);
		}
	}

	private Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
				try {
					Connection db = getConnection();
					try {
            //log.log(Level.INFO, "Request to: " + req.getRequestURI() + " With query string: " + req.getQueryString());
						resp.setContentType(ServletUtils.MIME_XML);
						XMLWriter writer = new XMLWriter(resp.getWriter());
						xmlGet(req, db, writer);
						writer.flush();
            //log.log(Level.INFO, "Request completed.");
					} finally {
						db.close();
					}
				} catch (Exception e) {
          log.log(Level.WARNING, "Top level exception, telling the user", e);
					ServletUtils.printException(resp, e);
				}
			}

	protected abstract void xmlGet(HttpServletRequest req, Connection db, XMLWriter resp)
			throws Exception;

}