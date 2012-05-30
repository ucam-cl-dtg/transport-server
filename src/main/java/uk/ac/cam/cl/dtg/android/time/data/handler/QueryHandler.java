package uk.ac.cam.cl.dtg.android.time.data.handler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import uk.ac.cam.cl.dtg.android.time.buses.*;

public interface QueryHandler {

  /** Provide live arrival information at a Bus Stop */
  public List<BusArrival> listArrivals(String ATCOCode, int numberOfArrivals) throws IOException;

  /**
   * Refresh the list of stops available from a data source
   * @param conn
   * @throws IOException
   * @throws SQLException
   */
  public void refreshDefinitions(Connection conn) throws IOException, SQLException;
}
