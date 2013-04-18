package uk.ac.cam.cl.dtg.android.time.servlet;

import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.exception.FlywayException;

/**
 * 
 * @author acr31
 *
 */
public class DatabaseInitialisationListener implements ServletContextListener {

	private static final Logger log = LoggerFactory.getLogger(DatabaseInitialisationListener.class);
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			DataSource datasource = ServletUtils.getDataSource();

			Flyway flyway = new Flyway();
			flyway.setDataSource(datasource);
			int numMigrations = flyway.migrate();
			log.info("Initialized database. {} migrations applied",numMigrations);
			
		} catch (FlywayException e) {
			log.error("Failed to initialize or update database",e);
		} catch (NamingException e) {
			log.error("Failed to lookup datasource in JNDI",e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub

	}

}
