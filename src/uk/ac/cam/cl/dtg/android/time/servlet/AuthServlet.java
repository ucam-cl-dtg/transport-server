package uk.ac.cam.cl.dtg.android.time.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.*;

public class AuthServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
		
	Connection conn;
	
	public AuthServlet() {
		
	
	}

	public static boolean isValidKey(String key) {
		
			
		return key.equals("david") ? true : false;
	}
	
	protected void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException, IOException
	{

	}
}
