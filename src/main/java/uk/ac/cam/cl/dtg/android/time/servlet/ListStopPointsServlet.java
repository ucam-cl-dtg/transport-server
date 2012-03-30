package uk.ac.cam.cl.dtg.android.time.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

/**
 * Web service which lists all stop points within a stop group
 * 
 * @author dt316
 * 
 */
public class ListStopPointsServlet extends StopPointsServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void xmlGet(HttpServletRequest req, Connection db,
			XMLWriter writer) throws Exception {

		ServletUtils.checkKeyForServices(req, db);
		String groupref = ServletUtils.getRequiredParameter(req, "groupref");
		queryList(groupref, db, writer);
	}

	private void queryList(String groupref, Connection db, XMLWriter res)
			throws SQLException {
		String sql = "select"
				+ " naptan_group_memberships.atco_code AS atco_code,"
				+ " available_stops.stop_name AS ds_name,"
				+ " naptan_extended_stop_info.lat AS lat,"
				+ " naptan_extended_stop_info.long AS long,"
				+ " naptan_extended_stop_info.naptan_code AS naptan_code,"
				+ " naptan_extended_stop_info.common_name AS common_name,"
				+ " naptan_extended_stop_info.short_name AS short_name,"
				+ " naptan_extended_stop_info.landmark AS landmark,"
				+ " naptan_extended_stop_info.street AS street,"
				+ " naptan_extended_stop_info.location_indicator AS location_indicator,"
				+ " naptan_extended_stop_info.bearing AS bearing"
				+ " from"
				+ " naptan_group_memberships"
				+ " left outer join available_stops"
				+ " on (available_stops.atco_code = naptan_group_memberships.atco_code)"
				+ " left outer join naptan_extended_stop_info"
				+ " on (naptan_group_memberships.atco_code = naptan_extended_stop_info.atco_code)"
				+ " where group_ref=?";

		PreparedStatement ps = db.prepareStatement(sql);
		try {
			ps.setString(1, groupref);
			writeResults(ps, res);
		} finally {
			ps.close();
		}
	}

	@Override
  public String getServletInfo() {
		return "BusStopServlet by David Tattersall";
	}
}
