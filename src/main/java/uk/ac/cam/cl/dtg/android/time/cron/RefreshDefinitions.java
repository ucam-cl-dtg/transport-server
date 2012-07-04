package uk.ac.cam.cl.dtg.android.time.cron;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import uk.ac.cam.cl.dtg.android.time.servlet.ServletUtils;
import uk.ac.cam.cl.dtg.time.data.handler.QueryHandler;

/**
 * Refresh the definitions provided by all the handlers.
 * 
 * @author drt24
 * 
 */
public class RefreshDefinitions implements Job {

  private static Logger log = Logger.getLogger(RefreshDefinitions.class.getCanonicalName());
  
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      Connection conn = ServletUtils.getConnection();
      PreparedStatement stmt = conn.prepareStatement("SELECT * FROM data_providers");
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        String provider = rs.getString("provider");
        String handler = rs.getString("handler");
        log.log(Level.INFO, "Starting refreshing definitions for: " + provider);
        try {
          Class<?> handlerClass = Class.forName(handler);
          QueryHandler handlerInstance = (QueryHandler) handlerClass.newInstance();
          handlerInstance.refreshDefinitions(conn);
        } catch (InstantiationException e) {
          log.log(Level.SEVERE,"Error while refreshing provider " + provider, e);
        } catch (IllegalAccessException e) {
          log.log(Level.SEVERE,"Error while refreshing provider " + provider, e);
        } catch (ClassNotFoundException e) {
          log.log(Level.SEVERE,"Error while refreshing provider " + provider, e);
        } catch (IOException e) {
          log.log(Level.SEVERE,"Error while refreshing provider " + provider, e);
        }
        log.log(Level.INFO, "Finished refreshing definitions for: " + provider);
      }
    } catch (SQLException e) {
      throw new JobExecutionException(e);
    } catch (NamingException e) {
      throw new JobExecutionException(e);
    }
    //TODO(drt24) better error handling
  }

}
