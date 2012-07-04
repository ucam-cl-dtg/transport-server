package uk.ac.cam.cl.dtg.android.time.servlet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public abstract class StopPointsServlet extends TransportServlet {

	private static final long serialVersionUID = -7628758598052964002L;

	protected void writeResults(PreparedStatement ps, XMLWriter writer)
			throws SQLException {

		writer.open("response").open("stops");

		ResultSet rs = ps.executeQuery();
		try {
			while (rs.next()) {
				String atcoCode = rs.getString("atco_code");
				String name = rs.getString("ds_name");
				String lat = rs.getString("lat");
				String lon = rs.getString("long");
				String naptanCode = rs.getString("naptan_code");
				String commonName = rs.getString("common_name");
				String shortName = rs.getString("short_name");
				String landmark = rs.getString("landmark");
				String street = rs.getString("street");
				String locInd = rs.getString("location_indicator");
				String bearing = rs.getString("bearing");

				writer.open("stop", "haslivedata", name != null ? "true"
						: "false");
				writer.textElement("atco", atcoCode);
				writer.textElement("naptan", naptanCode);
				writer.open("location");
				writer.textElement("lat", lat);
				writer.textElement("long", lon);
				writer.textElement("street", street);
				writer.textElement("landmark", landmark);
				writer.textElement("indicator", locInd);
				writer.close("location");
				writer.open("naming");
				writer.textElement("common", commonName);
				writer.textElement("short", shortName);
				writer.close("naming");
				writer.textElement("direction", bearing);
				writer.close("stop");
			}
		} finally {
			rs.close();
		}
		writer.close("response");
	}

}