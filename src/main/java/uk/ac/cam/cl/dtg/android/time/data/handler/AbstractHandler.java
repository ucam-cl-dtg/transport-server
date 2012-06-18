package uk.ac.cam.cl.dtg.android.time.data.handler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.cam.cl.dtg.android.time.buses.BusStop;

/**
 * Abstract functionality for {@link QueryHandler}, particularly
 * {@link #refreshDefinitions(Connection)}
 * 
 * @author drt24
 * 
 */
public abstract class AbstractHandler implements QueryHandler {
  protected String sourceID;
  protected final Logger log = Logger.getLogger(this.getClass().getCanonicalName());

  @Override
  public void refreshDefinitions(Connection conn) throws IOException, SQLException {
    boolean autoCommit = conn.getAutoCommit();
    conn.setAutoCommit(false);
    Savepoint savepoint = conn.setSavepoint();
    try {

      List<BusStop> stops = getAllStops();

      // Output about how many stops we've fetched
      log.log(Level.INFO, "[" + sourceID + "] Fetched " + stops.size() + " stops from server");

      PreparedStatement empty =
          conn.prepareStatement("DELETE FROM available_stops WHERE data_source=?");
      try {
        empty.setString(1, sourceID);
        empty.execute();
      } finally {
        empty.close();
      }

      log.log(Level.INFO, "[" + sourceID + "] Emptied stop table with stops from source '"
          + sourceID + "'");

      // Prepare a statement to check whether the stop already exists - this can happen if it is
      // provided by more than one provider
      PreparedStatement checkPreExisting =
          conn.prepareStatement("SELECT * FROM available_stops WHERE atco_code = ?");
      // Prepare an insertion statement
      PreparedStatement insert =
          conn.prepareStatement("INSERT INTO available_stops (atco_code,lat,long,data_source,stop_name) VALUES ( ?, ?, ?, ?, ?)");
      try {
        for (BusStop stop : stops) {
          try {
            checkPreExisting.setString(1, stop.getAtcoCode());
            ResultSet rs = checkPreExisting.executeQuery();
            boolean exists = rs.next();
            rs.close();// enclosing finally will ensure it is closed on error.
            if (!exists) {
              insert.setString(1, stop.getAtcoCode());
              insert.setDouble(2, stop.getLatitude());
              insert.setDouble(3, stop.getLongitude());
              insert.setString(4, sourceID);
              insert.setString(5, stop.getName());
              insert.execute();
            }
          } catch (SQLException e) {
            log.log(Level.WARNING, "Error inserting " + stop.toString() + " Message: "
                + e.getMessage());
            break;// TODO(drt24) do we want to ignore this and keep going? If so we need to use
                  // savepoints.
          }
        }
      } finally {
        insert.close();
        checkPreExisting.close();
      }

      log.log(Level.INFO, "[" + sourceID + "]Stops inserted into database.");

      // Now add spatial information
      log.log(Level.INFO, "[" + sourceID + "] Adding spatial information to stops...");

      PreparedStatement spatial =
          conn.prepareStatement("UPDATE available_stops SET geometry = PointFromText('POINT(' || long || ' ' || lat || ')',-1) WHERE data_source=?");
      try {
        spatial.setString(1, sourceID);
        spatial.execute();
      } finally {
        spatial.close();
      }
      log.log(Level.INFO, "...done.");
    } catch (Throwable e) {
      conn.rollback(savepoint);
      log.log(Level.SEVERE, "Exception while refreshingDefinitions, rolling back. Message: "
          + e.getMessage());
      throw new IOException(e);
    } finally {
      conn.setAutoCommit(autoCommit);
    }

  }

  /**
   * 
   * @return all the stops which are provided by this handler
   * @throws IOException
   */
  protected abstract List<BusStop> getAllStops() throws IOException;
}
