package uk.ac.cam.cl.dtg.android.time.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.ac.cam.cl.dtg.android.time.buses.BusArrivalData;
import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import uk.ac.cam.cl.dtg.android.time.buses.StopGroup;
import uk.ac.cam.cl.dtg.android.time.data.TransportDataException;
import uk.ac.cam.cl.dtg.android.time.data.TransportDataProvider;
import uk.ac.cam.cl.dtg.compilesettingsloader.Loader;

public class APIIT {

  private TransportDataProvider tdp;

  @Before
  public void getProvider() {
    tdp =
        new TransportDataProvider(Loader.load("omnibus.release.apikey"),
            Loader.load("omnibus.development.feedurl"));
  }

  @Test
  public void getArrivals() throws TransportDataException {
    BusArrivalData data = tdp.getBusArrivalData("0500CCITY111", 5);
    int size = data.getNextBuses().size();
    assertTrue("Must be results", size > 0);
    assertTrue("Must not be too many results", size <= 5);
  }

  @Test
  public void lookupStopNaptan() throws TransportDataException {
    String code = "cmbdajdm";
    BusStop stop = tdp.getStopByNaptan(code);
    assertEquals(code, stop.getNaptanCode());
  }

  @Test
  public void lookupStopAtco() throws TransportDataException {
    String code = "0500CCITY111";
    BusStop stop = tdp.getStopByAtco(code);
    assertEquals(code, stop.getAtcoCode());
  }

  @Test
  public void findStopGroupsNear() throws TransportDataException {
    List<StopGroup> groups = tdp.getStopGroupsNear(52.2, 0.09);
    assertTrue("must be enough results", groups.size() >= 10);// this is the old default value to return so we should get at least that
    for (StopGroup group : groups) {
      assertNotNull(group.getName());
      assertNotNull(group.getRef());
    }
  }

  @Test
  public void findStopGroupsWithin() throws TransportDataException {
    List<StopGroup> groups = tdp.getStopGroupsWithin(0.051241, 52.212537, 0.073557, 52.204910);
    assertTrue("must be enough results", groups.size() >= 2);// slightly less than we saw when writing this
    for (StopGroup group : groups) {
      assertNotNull(group.getName());
      assertNotNull(group.getRef());
    }
  }

  @Test
  public void listStopPoints() throws TransportDataException {
    List<BusStop> busStops = tdp.getBusStopsInGroup("050GCC000000");
    assertTrue("Must be stops", busStops.size() >= 10);// slightly less than we saw when writing this
    for (BusStop stop : busStops) {
      assertNotNull(stop.getName());
      assertNotNull(stop.getAtcoCode());
    }
  }
}
