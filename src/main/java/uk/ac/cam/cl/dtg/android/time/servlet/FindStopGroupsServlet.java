package uk.ac.cam.cl.dtg.android.time.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Queries the database and returns all stop groups matching certain criteria
 * 
 * @author dt316
 * 
 */
public class FindStopGroupsServlet extends OmniBusServlet {

	private static final long serialVersionUID = -19443654039914185L;

	private static int DEFAULT_RESULTS_RETURNED = 10;

	// looks for groups only within 10km
	private static int DEFAULT_SEARCH_RADIUS = 10000;

	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
	}

	@Override
	protected void xmlGet(HttpServletRequest req, Connection db,
			XMLWriter writer) throws Exception {
		ServletUtils.checkKeyForServices(req, db);

		String method = ServletUtils.getRequiredParameter(req, "method",
				"near", "within");
		int numresults = ServletUtils.getIntParameter(req, "numresults",
				DEFAULT_RESULTS_RETURNED);
		int radius = ServletUtils.getIntParameter(req, "radius",
				DEFAULT_SEARCH_RADIUS);
		double radiusInDegrees = ((double) radius) * 0.0000111111111;

		if (method.equals("near")) {
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
				+ "st_distance_sphere(geopoint, ST_GeomFromText('POINT(? ?)',-1)) as dist "
				+ "from naptan_groups "
				+ "where geopoint && Expand(ST_GeomFromText('POINT(? ?)',-1),?) "
				+ "order by dist asc limit ?";
		PreparedStatement ps = db.prepareStatement(sql);
		try {
			ps.setDouble(1, lon);
			ps.setDouble(2, lat);
			ps.setDouble(3, lon);
			ps.setDouble(4, lat);
			ps.setInt(5, numResults);
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
				+ "where geopoint && ST_GeomFromText('POLYGON((? ?, ? ?, ? ?, ? ?, ? ?))', -1) "
				+ "limit 10000";
		PreparedStatement ps = db.prepareStatement(sql);
		try {
			ps.setDouble(1, left);
			ps.setDouble(2, top);
			ps.setDouble(3, right);
			ps.setDouble(4, top);
			ps.setDouble(5, right);
			ps.setDouble(6, bottom);
			ps.setDouble(7, left);
			ps.setDouble(8, bottom);
			ps.setDouble(9, left);
			ps.setDouble(10, top);
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
