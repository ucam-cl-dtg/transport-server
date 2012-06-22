package uk.ac.cam.cl.dtg.android.time.data.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.cam.cl.dtg.android.time.buses.BusArrival;
import uk.ac.cam.cl.dtg.compilesettingsloader.Loader;

public class TravelineTest {

  private static TravelineHandler handler;

  @BeforeClass
  public static void setupHandler() throws UnsupportedEncodingException {
    handler = new TravelineHandler(Loader.load("omnibus.traveline.api.user"), Loader
            .load("omnibus.traveline.api.password"));
  }

  @Test
  public void parseSiri() throws IOException {
    List<BusArrival> arrivals =
        handler.parseSiri(this.getClass().getClassLoader().getResourceAsStream(
            "siri/exampleResponse.xml"));
    assertEquals(5, arrivals.size());

    BusArrival first = arrivals.get(0);
    assertEquals("42",first.getServiceID());
    assertEquals("Toddington, The Green",first.getDestination());
    assertFalse(first.getDueTime().isLiveData);

    BusArrival third = arrivals.get(2);
    assertEquals("1",third.getServiceID());
    assertEquals("Kempston",third.getDestination());
    assertTrue(third.getDueTime().isLiveData);
  }

  @Test
  public void getArrivals() throws IOException {
    List<BusArrival> arrivals = handler.listArrivals("627003020520", 5);
    assertTrue(arrivals.size() > 0);
  }
}
