package uk.ac.cam.cl.dtg.android.time.data.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;

import uk.ac.cam.cl.dtg.android.time.buses.BusArrival;

public class ACISTest {

  @Test
  public void processStopSim() throws IOException {
    BufferedReader input = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("acis/stopSimulator.html")));
    StringBuilder responseBuilder = new StringBuilder();
    String line = input.readLine();
    while (line != null){
      responseBuilder.append(line);
      line = input.readLine();
    }
    ACISHandler handler = new ACISHandler("", "");
    List<BusArrival> arrivals = handler.processStopSim(responseBuilder.toString());
    assertEquals(3,arrivals.size());
    String[] times = {"09:55","10:15","10:35"};
    for (int i = 0; i < arrivals.size(); ++i){
      BusArrival arrival = arrivals.get(i);
      assertEquals("UNI4",arrival.getServiceID());
      assertEquals("Mad-ley Rd",arrival.getDestination());
      assertFalse(arrival.getDueTime().isLiveData);
      assertEquals(times[i],arrival.getDueTime().getArrivalTime());
    }
  }
}
