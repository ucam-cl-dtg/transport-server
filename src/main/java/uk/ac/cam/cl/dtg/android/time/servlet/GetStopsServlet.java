package uk.ac.cam.cl.dtg.android.time.servlet;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import uk.ac.cam.cl.dtg.android.time.data.CouncilDataSource;

/**
 * Provides XML/RPC interface to bus stop position web service Not exposed as
 * part of OmniBus - should instead use FindStopGroups / ListStopPoints. This is
 * here to support Cambridgeshire bus app
 * 
 * @author dt316
 * 
 */
public class GetStopsServlet extends OmniBusServlet {

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

		List<BusStop> stops = getCachedResult(level);

		// TODO: fix (this note left by David) I think the problem is that this
		// does a fetch for all the cambs stops but no others. Also why do we
		// need this at all? Don't we have the stops in the database already? acr31
		if (stops == null) {
			stops = CouncilDataSource.getBusStops(Integer.parseInt(level));
			cacheResult(level, stops);
		}

		writer.open("response");
		writer.open("stops", "count", String.valueOf(stops.size()));
		for (BusStop stop : stops) {
			writer.open("stop");
			writer.textElement("name", stop.getName());
			writer.textElement("ref", stop.getStopRef());
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

	@Override
  public String getServletInfo() {
		return "GetStopsServlet by David Tattersall";
	}
}
