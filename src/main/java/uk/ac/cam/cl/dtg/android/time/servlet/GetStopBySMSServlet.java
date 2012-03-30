package uk.ac.cam.cl.dtg.android.time.servlet;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import uk.ac.cam.cl.dtg.android.time.data.CouncilDataSource;

/**
 * Provides XML/RPC interface to bus stop position web service Not exposed as
 * part of OmniBus - should instead use LookupStop. This is here to support
 * Cambridgeshire bus app
 * 
 * @author dt316
 * 
 */
public class GetStopBySMSServlet extends OmniBusServlet {

	private static final long serialVersionUID = -4945691160872319999L;


	@Override
	protected void xmlGet(HttpServletRequest req, Connection db,
			XMLWriter writer) throws Exception {

		ServletUtils.checkKeyForServices(req, db);
		String smsCode = ServletUtils.getRequiredParameter(req, "smscode");

		BusStop stop = CouncilDataSource.lookupStopBySMS(smsCode);

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
}
