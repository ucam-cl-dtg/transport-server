package uk.ac.cam.cl.dtg.android.time.cron;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Refresh the definitions provided by all the handlers.
 * 
 * @author drt24
 * 
 */
public class RefreshDefinitions implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    System.out.println("Cron job works.");
    // TODO Auto-generated method stub

  }

}
