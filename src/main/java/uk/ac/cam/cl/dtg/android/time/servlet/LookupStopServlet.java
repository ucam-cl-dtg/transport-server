package uk.ac.cam.cl.dtg.android.time.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides XML/RPC interface to bus stop position web service
 * 
 * @author dt316
 * 
 */
public class LookupStopServlet extends StopPointsServlet {
	private static final long serialVersionUID = 1L;

	private void queryNaptan(String naptanCode, Connection db, XMLWriter output)
			throws SQLException {
		String sqlNaPTAN = "select"
				+ " naptan_extended_stop_info.atco_code AS atco_code,"
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
				+ " naptan_extended_stop_info"
				+ " left outer join available_stops"
				+ " on (available_stops.atco_code = naptan_extended_stop_info.atco_code)"
				+ " where naptan_extended_stop_info.naptan_code=? limit 1";
		PreparedStatement ps = db.prepareStatement(sqlNaPTAN);
		try {
			ps.setString(1, naptanCode);
			writeResults(ps, output);
		} finally {
			ps.close();
		}
	}

	private void queryATCO(String atcoCode, Connection db, XMLWriter output)
			throws SQLException {
		String sqlATCO = "select"
				+ " naptan_extended_stop_info.atco_code AS atco_code,"
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
				+ " naptan_extended_stop_info"
				+ " left outer join available_stops"
				+ " on (available_stops.atco_code = naptan_extended_stop_info.atco_code)"
				+ " where naptan_extended_stop_info.atco_code=? limit 1";
		PreparedStatement ps = db.prepareStatement(sqlATCO);
		try {
			ps.setString(1, atcoCode);
			writeResults(ps, output);
		} finally {
			ps.close();
		}
	}

	@Override
	protected void xmlGet(HttpServletRequest req, Connection db,
			XMLWriter writer) throws Exception {
		ServletUtils.checkKeyForServices(req, db);
		String naptan = req.getParameter("naptan");
		if (naptan != null) {
			queryNaptan(naptan, db, writer);
			return;
		}

		String atco = req.getParameter("atco");
		if (atco != null) {
			queryATCO(atco, db, writer);
			return;
		}
		throw new Exception("Must supply either an ATCO or NaPTAN code.");
	}

	public String getServletInfo() {
		return "GetStopByInfoServlet by David Tattersall";
	}
}
