package uk.ac.cam.cl.dtg.android.time.servlet;


import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DumpRequest extends HttpServlet {

		private static final long serialVersionUID = 1L;

	
		@Override
		public void init() throws ServletException {

			super.init();

		}

		/**
		 * Handle a GET request
		 */
		protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			
			InputStream r = req.getInputStream();
			int b;
			
			while ( ( b = r.read() ) != -1 )
			 {

			     char c = (char)b;         

			     System.out.print(c);
			 }

			System.out.println("End.");
			
		}
}
