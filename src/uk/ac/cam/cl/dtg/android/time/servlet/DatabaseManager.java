package uk.ac.cam.cl.dtg.android.time.servlet;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {

	static Connection conn;
	
	private static String DB_USER = "transport";
	private static String DB_PASS = "urop09";
	private static String DB_URL = "jdbc:postgresql://localhost/transport";

	public static Connection getConnection() {

		if(conn == null) {

			// Attempt to load database driver
			try
			{
				// Load Sun's jdbc-odbc driver
				Class.forName("org.postgresql.Driver").newInstance();		


				Properties props = new Properties();
				props.setProperty("user",DB_USER);
				props.setProperty("password",DB_PASS);
				conn = DriverManager.getConnection(DB_URL, props);

				return conn;
			}

			catch (ClassNotFoundException cnfe) // driver not found
			{
				System.err.println ("Unable to load database driver");
				System.err.println ("Details : " + cnfe);

				return null;

			} catch (Exception e) {

				System.err.println("Exception during EnsureConnection: "+e);

				return null;

			}
			
		} else {
			
			// Already connected - return handle
			return conn;
			
		}
	}

	
}
