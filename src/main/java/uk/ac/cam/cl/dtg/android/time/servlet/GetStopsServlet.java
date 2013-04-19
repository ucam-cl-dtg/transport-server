package uk.ac.cam.cl.dtg.android.time.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import uk.ac.cam.cl.dtg.android.time.buses.BusStop;

/**
 * Provides XML/RPC interface to bus stop position web service Not exposed as
 * part of Transport Server - should instead use FindStopGroups / ListStopPoints. This is
 * here to support Cambridgeshire bus app
 * 
 * @author dt316
 * 
 */
public class GetStopsServlet extends TransportServlet {

	private static class Cached {
		List<BusStop> stops;
		long stored;

		public Cached(List<BusStop> stops, long stored) {
			this.stops = stops;
			this.stored = stored;
		}
	}

	private static final long serialVersionUID = -94888689529017047L;

	// Holds all cached results
	private Map<String, Cached> cachedResults;

	// How long before cached results expire and must be reloaded
	public static final int CACHE_TIMEOUT = 1000 * 60 * 60 * 48;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		cachedResults = Collections
				.synchronizedMap(new HashMap<String, Cached>());
	}

	@Override
	protected void xmlGet(HttpServletRequest req, Connection db,
			XMLWriter writer) throws Exception {

		ServletUtils.checkKeyForServices(req, db);
		String level = ServletUtils.getRequiredParameter(req, "level");

		//TODO(drt24) We don't have level information any more so we just return all the information for level 1
		if (!level.equals("1")){
		  writer.open("response");
		  writer.open("stops", "count", String.valueOf(0));
		  writer.close("stops");
		  writer.close("response");
		  log.fine(String.format("GetStops for level '%s' so returning empty", level));
		  return;
		}
		List<BusStop> stops = getCachedResult(level);

		if (stops == null || stops.size() == 0) {
			stops = getBusStopsForProvider("cambs", db);
			cacheResult(level, stops);
		}

		writer.open("response");
		writer.open("stops", "count", String.valueOf(stops.size()));
		for (BusStop stop : stops) {
			writer.open("stop");
			writer.textElement("name", stop.getName());
			writer.textElement("ref", stop.getAtcoCode());
			writer.textElement("lat", String.valueOf(stop.getLatitude()));
			writer.textElement("long", String.valueOf(stop.getLongitude()));
			writer.close("stop");
		}
		writer.close("response");
	}

	private List<BusStop> getCachedResult(String key) {
		Cached record = cachedResults.get(key);
		if (record == null)
			return null;

		if (System.currentTimeMillis() - record.stored > CACHE_TIMEOUT) {
			cachedResults.remove(key);
			return null;
		}
		return record.stops;
	}

	/**
	 * Stores a cached copy of a lookup result
	 * 
	 * @param key
	 *            Key uniquely identifying the lookup result
	 * @param data
	 *            Result to cache
	 */
	private void cacheResult(String key, List<BusStop> data) {
		cachedResults.put(key, new Cached(data, System.currentTimeMillis()));
  }

  protected List<BusStop> getBusStopsForProvider(String provider, Connection db)
      throws SQLException {
    PreparedStatement stmt =
        db.prepareStatement("SELECT atco_code, lat, long, stop_name FROM available_stops WHERE data_source = ?");
    try {
      stmt.setString(1, provider);
      ResultSet rs = stmt.executeQuery();
      List<BusStop> stops = new ArrayList<BusStop>();
      while (rs.next()) {
        String stopName = rs.getString("stop_name");
        double latitude = Double.parseDouble(rs.getString("lat"));
        double longitude = Double.parseDouble(rs.getString("long"));
        String atcoCode = rs.getString("atco_code");
        stops.add(new BusStop(stopName, latitude, longitude, atcoCode));
      }
      return stops;
    } finally {
      stmt.close();
    }
  }

	@Override
  public String getServletInfo() {
		return "GetStopsServlet by David Tattersall";
	}
}
