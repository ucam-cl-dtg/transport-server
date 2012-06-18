package uk.ac.cam.cl.dtg.android.time.cron;

import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;

/**
 * Start up cron jobs that need to run periodically
 * 
 * @author drt24
 * 
 */
public class CronStarterServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void init() throws ServletException {
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sche;
    try {
      sche = sf.getScheduler();

      sche.start();
      JobDetailImpl refreshDefinitions = new JobDetailImpl();
      refreshDefinitions.setName("Refresh Definitions");
      refreshDefinitions.setGroup("REFRESH");
      refreshDefinitions.setJobClass(RefreshDefinitions.class);

      CronTriggerImpl trigger = new CronTriggerImpl();
      trigger.setName("Weekly");
      trigger.setCronExpression("0 23 4 ? * TUE");// 04:23:00 on Tuesdays

      sche.scheduleJob(refreshDefinitions, trigger);
      // Run refresh definitions on startup
      new RefreshDefinitions().execute(null);

    } catch (SchedulerException e) {
      throw new ServletException(e);
    } catch (ParseException e) {
      throw new ServletException(e);
    }
  }
}
