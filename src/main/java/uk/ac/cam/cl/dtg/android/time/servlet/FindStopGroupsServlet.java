package uk.ac.cam.cl.dtg.android.time.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

/**
 * Queries the database and returns all stop groups matching certain criteria
 * 
 * @author dt316
 * 
 */
public class FindStopGroupsServlet extends OmniBusServlet {

	private static final long serialVersionUID = -19443654039914185L;

	private static int DEFAULT_RESULTS_RETURNED = 20;

	// looks for groups only within 10km
	private static int DEFAULT_SEARCH_RADIUS = 10000;

	@Override
	protected void xmlGet(HttpServletRequest req, Connection db,
			XMLWriter writer) throws Exception {
		ServletUtils.checkKeyForServices(req, db);

		String method = ServletUtils.getRequiredParameter(req, "method",
				"near", "within");

		if (method.equals("near")) {
		  int numresults = ServletUtils.getIntParameter(req, "numresults",
	        DEFAULT_RESULTS_RETURNED);
		  int radius = ServletUtils.getIntParameter(req, "radius",
	        DEFAULT_SEARCH_RADIUS);
	    double radiusInDegrees = ((double) radius) * 0.0000111111111;
			double lat = Double.parseDouble(ServletUtils.getRequiredParameter(
					req, "lat"));
			double lon = Double.parseDouble(ServletUtils.getRequiredParameter(
					req, "long"));
			queryNear(radiusInDegrees, lat, lon, numresults, db, writer);
		} else if (method.equals("within")) {
			double left = Double.parseDouble(ServletUtils.getRequiredParameter(
					req, "left"));
			double right = Double.parseDouble(ServletUtils
					.getRequiredParameter(req, "right"));
			double top = Double.parseDouble(ServletUtils.getRequiredParameter(
					req, "top"));
			double bottom = Double.parseDouble(ServletUtils
					.getRequiredParameter(req, "bottom"));
			queryWithin(left, top, right, bottom, db, writer);
		}
	}

	private void queryNear(double radiusInDegrees, double lat, double lon,
			int numResults, Connection db, XMLWriter res) throws SQLException,
			IOException {
		String sql = "select "
				+ "name,"
				+ "group_ref,"
				+ "lat,"
				+ "long,"
				+ "st_distance_sphere(geopoint, ST_GeomFromText(?,-1)) as dist "
				+ "from naptan_groups "
				+ "where geopoint && Expand(ST_GeomFromText(?,-1),?) "
				+ "order by dist asc limit ?";
		PreparedStatement ps = db.prepareStatement(sql);
		try {
		  String point = "POINT( "+ lon + " " + lat + ")";
		  ps.setString(1, point);
			ps.setString(2, point);
			ps.setDouble(3, radiusInDegrees);
			ps.setInt(4, numResults);
			writeResults(ps, res);
		} finally {
			ps.close();
		}
	}

	private void queryWithin(double left, double top, double right,
			double bottom, Connection db, XMLWriter res) throws SQLException,
			IOException {
		String sql = "select "
				+ "name,"
				+ "group_ref,"
				+ "lat,"
				+ "long,"
				+ "-1 as dist "
				+ "from naptan_groups "
				+ "where geopoint && ST_GeomFromText(?, -1) "
				+ "limit 10000";
		PreparedStatement ps = db.prepareStatement(sql);
		try {
		  String polygon = String.format("POLYGON((%f %f, %f %f, %f %f, %f %f, %f %f))",left, top, right, top, right, bottom, left, bottom, left, top);
		  ps.setString(1, polygon);
			writeResults(ps, res);
		} finally {
			ps.close();
		}
	}

	private void writeResults(PreparedStatement st, XMLWriter writer)
			throws IOException, SQLException {
		ResultSet rs = st.executeQuery();
		try {
			writer.open("response").open("groups");
			while (rs.next()) {
				String name = rs.getString(1);
				String ref = rs.getString(2);
				String rlat = rs.getString(3);
				String rlon = rs.getString(4);
				String dist = rs.getString(5);

				writer.open("group");
				writer.textElement("ref", ref);
				writer.textElement("name", name);
				writer.textElement("lat", rlat);
				writer.textElement("long", rlon);
				if (!dist.equals("-1"))
					writer.textElement("dist", dist);
				writer.close("group");
			}
			writer.close("response");
		} finally {
			rs.close();
		}
	}

	@Override
  public String getServletInfo() {
		return "FindStopGroupsServlet by David Tattersall";
	}
}
