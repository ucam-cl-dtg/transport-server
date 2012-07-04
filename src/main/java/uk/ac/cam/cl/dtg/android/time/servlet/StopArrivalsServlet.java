package uk.ac.cam.cl.dtg.android.time.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.servlet.http.HttpServletRequest;

import uk.ac.cam.cl.dtg.android.time.buses.BusArrival;
import uk.ac.cam.cl.dtg.time.data.handler.QueryHandler;

public class StopArrivalsServlet extends TransportServlet {

	private static class Arrivals {
	  public final long expiry;
	  public final List<BusArrival> arrivals;
	  public Arrivals(List<BusArrival> arrivals){
	    this.expiry = System.currentTimeMillis();
	    this.arrivals = arrivals;
	  }
	  public boolean hasExpired() {
	    return expiry < System.currentTimeMillis() - 1000L*30;//30 seconds
	  }
  }

  private static final long serialVersionUID = -474214128677551381L;

	// How many arrivals to fetch by default
	public static final int DEFAULT_ARRIVAL_COUNT = 5;
	private static Map<String,Arrivals> arrivalsCache = new WeakHashMap<String,Arrivals>();

	private QueryHandler lookupHandler(String atcoCode, Connection db) throws SQLException {
		String sql = "select handler from available_stops"
				+ " left outer join data_providers"
				+ " on data_providers.provider=available_stops.data_source"
				+ " where available_stops.atco_code=?";
		PreparedStatement lookupHandler = db.prepareStatement(sql);
		try {
			lookupHandler.setString(1, atcoCode);
			ResultSet rs = lookupHandler.executeQuery();
			try {
				if (rs.next()) {
					String handler = rs.getString("handler");
					Class<?> handlerClass = Class.forName(handler);
					return (QueryHandler) handlerClass.newInstance();
				}
				else {
					throw new SQLException("Handler not found for stop with ATCO code = "+atcoCode);
				}
			} catch (ClassNotFoundException e) {
				throw new SQLException(e);
			} catch (InstantiationException e) {
				throw new SQLException(e);
			} catch (IllegalAccessException e) {
				throw new SQLException(e);
			} finally {
				rs.close();
			}
		} finally {
			lookupHandler.close();
		}
	}

  @Override
  protected void xmlGet(HttpServletRequest req, Connection db, XMLWriter writer) throws Exception {
    ServletUtils.checkKeyForServices(req, db);

    String atcoCode = ServletUtils.getRequiredParameter(req, "atco");
    System.out.println("Recevied query for " + atcoCode);

    int numArrivals = ServletUtils.getIntParameter(req, "numarrivals", DEFAULT_ARRIVAL_COUNT);
    Arrivals cachedArrivals = arrivalsCache.get(atcoCode);
    if (cachedArrivals != null && !cachedArrivals.hasExpired()
        && cachedArrivals.arrivals.size() >= numArrivals) {
      writeResponse(cachedArrivals.arrivals, writer);
    } else {
      QueryHandler dataSourceHandler = lookupHandler(atcoCode, db);

      List<BusArrival> nextArrivals = dataSourceHandler.listArrivals(atcoCode, numArrivals);
      writeResponse(nextArrivals, writer);
      arrivalsCache.put(atcoCode, new Arrivals(nextArrivals));
    }
  }

	private void writeResponse(List<BusArrival> nextArrivals, XMLWriter writer) {
		writer.open("response");
		writer.open("arrivals", "count", String.valueOf(nextArrivals.size()));
		for (BusArrival arrival : nextArrivals) {
			writer.open("arrival");
			writer.textElement("service", arrival.getServiceID());
			writer.textElement("destination", arrival.getDestination());
			writer.open("time");
			writer.textElement("millis",
					String.valueOf(arrival.getDueTime().getTime()));
			writer.textElement("textual", arrival.getDueTime().toString());
			writer.textElement("isdue",
					String.valueOf(arrival.getDueTime().isDue));
			writer.textElement("islive",
					String.valueOf(arrival.getDueTime().isLiveData));
			writer.close("arrival");
		}
		writer.close("response");
	}

	@Override
  public String getServletInfo() {
		return "Retrieves live arrivals for a stop. By David Tattersall";
	}
}
