package uk.ac.cam.cl.dtg.android.time.api;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.ac.cam.cl.dtg.android.time.buses.ArrivalTime;
import uk.ac.cam.cl.dtg.android.time.buses.BusArrival;
import uk.ac.cam.cl.dtg.android.time.buses.BusArrivalData;
import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import uk.ac.cam.cl.dtg.android.time.buses.StopGroup;
import uk.ac.cam.cl.dtg.android.time.data.TransportDataException;
import uk.ac.cam.cl.dtg.android.time.data.TransportDataProvider;
import uk.ac.cam.cl.dtg.compilesettingsloader.Loader;

public class APIIT {

  private static final String APIKEY = Loader.load("transport-server.release.apikey");
  private static final String FEEDURL = Loader.load("transport-server.development.feedurl");
  private TransportDataProvider tdp;

  @Before
  public void getProvider() {
    tdp = new TransportDataProvider(APIKEY,FEEDURL);
  }
  /**
   * We don't want to overload the upstream server when running tests so slow things down a bit
   * @throws InterruptedException
   */
  @Before
  public void slowDown() throws InterruptedException {
    Thread.sleep(500);
  }

  @Test
  public void getArrivalsNormal() throws TransportDataException {
    getArrivals("0500CCITY111");// This should work by the normal means
  }

  @Test
  public void getArrivalsFallback() throws TransportDataException {
    getArrivals("0500CCITY424");// This should fallback to the stop simulator
  }
  @Test
  public void getArrivalsTFL() throws TransportDataException {
    getArrivals("490007247S");
    getArrivals("150003475W");
  }

  public void getArrivals(String atcoCode) throws TransportDataException {
    BusArrivalData data = tdp.getBusArrivalData(atcoCode, 5);
    int size = data.getNextBuses().size();
    assertThat("Must be results", size, greaterThan(0));
    assertThat("Must not be too many results", size, lessThan(70));
    boolean allDue = true;
    boolean atLeastOneAfter = false;
    Date now = new Date();
    for (BusArrival arrival : data.getNextBuses()){
      ArrivalTime time = arrival.getDueTime();
      allDue &= time.isDue;
      atLeastOneAfter |= time.after(now);
    }
    assertFalse("At least one must not be due", allDue);
    assertTrue("At least one must be in the future", atLeastOneAfter);
    // Get the same thing again to trigger getting via the cache
    data = tdp.getBusArrivalData(atcoCode, 5);
    size = data.getNextBuses().size();
    assertThat("Must be results", size, greaterThan(0));
    assertThat("Must not be too many results", size, lessThan(70));
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
    assertThat("must be enough results", groups.size(), greaterThanOrEqualTo(10));// this is the old default value to return so we should get at least that
    for (StopGroup group : groups) {
      assertNotNull(group.getName());
      assertNotNull(group.getRef());
    }
  }

  @Test
  public void findStopGroupsWithin() throws TransportDataException {
    List<StopGroup> groups = tdp.getStopGroupsWithin(0.051241, 52.212537, 0.073557, 52.204910);
    assertThat("must be enough results", groups.size(), greaterThanOrEqualTo(2));// slightly less than we saw when writing this
    for (StopGroup group : groups) {
      assertNotNull(group.getName());
      assertNotNull(group.getRef());
    }
  }

  @Test
  public void listStopPoints() throws TransportDataException {
    List<BusStop> busStops = tdp.getBusStopsInGroup("050GCC000000");
    assertThat("Must be stops", busStops.size(), greaterThanOrEqualTo(10));// slightly less than we saw when writing this
    for (BusStop stop : busStops) {
      assertNotNull(stop.getName());
      assertNotNull(stop.getAtcoCode());
    }
  }
}
