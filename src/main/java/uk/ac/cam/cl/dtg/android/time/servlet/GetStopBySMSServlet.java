package uk.ac.cam.cl.dtg.android.time.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;

import uk.ac.cam.cl.dtg.android.time.buses.BusStop;

/**
 * Provides XML/RPC interface to bus stop position web service Not exposed as
 * part of OmniBus - should instead use LookupStop. This is here to support
 * Cambridgeshire bus app
 * 
 * @author dt316
 * 
 * smscodes are naptan_codes
 */
public class GetStopBySMSServlet extends OmniBusServlet {

	private static final long serialVersionUID = -4945691160872319999L;

	@Override
	protected void xmlGet(HttpServletRequest req, Connection db,
			XMLWriter writer) throws Exception {

		ServletUtils.checkKeyForServices(req, db);
		String smsCode = ServletUtils.getRequiredParameter(req, "smscode");

		BusStop stop = null;
		String sql = "SELECT available_stops.atco_code, available_stops.lat, available_stops.long, stop_name FROM available_stops, naptan_extended_stop_info WHERE available_stops.atco_code = naptan_extended_stop_info.atco_code AND naptan_extended_stop_info.naptan_code =?";
		PreparedStatement ps = db.prepareStatement(sql);
		try {
		  ps.setString(1, smsCode);
		  ResultSet rs = ps.executeQuery();
		  if (rs.next()) {
		    stop = new BusStop(rs.getString("stop_name"), Double.parseDouble(rs.getString("lat")), Double.parseDouble(rs.getString("long")), rs.getString("atco_code"));
		  }
		  rs.close();//on error closed by ps.close()
		} finally {
		  ps.close();
		}
		if (stop == null){
		  throw new NoSuchStopException(smsCode);
		}

		writer.open("response");
		writer.open("stop");
		writer.textElement("name", stop.getName());
		writer.textElement("ref", stop.getStopRef());
		writer.textElement("lat", stop.getLatitude() + "");
		writer.textElement("long", stop.getLongitude() + "");
		writer.close("response");
	}

	@Override
  public String getServletInfo() {
		return "GetStopByInfoServlet by David Tattersall";
	}

  private static class NoSuchStopException extends Exception {
    private static final long serialVersionUID = 1L;

    public NoSuchStopException(String smsCode) {
      super("No stop with ref: " + smsCode);
    }

  }
}
