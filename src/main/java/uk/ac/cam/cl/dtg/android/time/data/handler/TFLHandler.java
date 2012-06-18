package uk.ac.cam.cl.dtg.android.time.data.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import uk.ac.cam.cl.dtg.android.time.buses.BusArrival;
import uk.ac.cam.cl.dtg.android.time.buses.BusStop;

/**
 * Handler for the Transport for London bus feeds
 *  http://www.tfl.gov.uk/developers
 * @author drt24
 *
 */
public class TFLHandler extends AbstractHandler {

  private static final String URL = "http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?";

  public TFLHandler() {
    sourceID = "tfl";
  }

  @Override
  public List<BusArrival> listArrivals(String ATCOCode, int numberOfArrivals) throws IOException {
    //TODO(drt24): We don't handle the various error conditions specified in the TFL API docs

    // Response will not be in the order specified in ReturnList but in the order specified by
    // the sequence number in the TFL API docs
    List<String> lines = getArrays("StopCode2=" + ATCOCode
        + "&ReturnList=LineName,DestinationName,EstimatedTime");
    try {
      List<BusArrival> nextBusses = new ArrayList<BusArrival>();
      for (String line : lines) {
        JSONArray array = new JSONArray(line);
        int type = array.getInt(0);
        if (1 == type) {// If a predicted arrival time
          String lineName = array.getString(1);
          String destination = array.getString(2);
          String estimatedTime = array.getString(3);
          nextBusses.add(new BusArrival(lineName, destination, Long.parseLong(estimatedTime)));
        }
      }
      return nextBusses;
    } catch (JSONException e) {
      throw new IOException("Error parsing response from server: " + e.getMessage());
    }
  }

  @Override
  protected List<BusStop> getAllStops() throws MalformedURLException, IOException {
    List<String> lines = getArrays("StopAlso=True&ReturnList=StopPointName,StopCode2,Latitude,Longitude");
    try {
      List<BusStop> stops = new ArrayList<BusStop>();
      for (String line : lines){
        JSONArray array = new JSONArray(line);
        int type = array.getInt(0);
        if (0 == type) {// If a stop
          String stopName = array.getString(1);
          String atcoCode = array.getString(2);
          if (null == atcoCode || "null".equals(atcoCode)){
            continue;//some codes are null
          }
          double latitude = array.getDouble(3);
          double longitude = array.getDouble(4);
          stops.add(new BusStop(stopName, latitude, longitude, atcoCode));
        }
      }
      return stops;
    } catch (JSONException e) {
      throw new IOException("Error parsing response from server: " + e.getMessage());
    }
  }

  private List<String> getArrays(String request) throws MalformedURLException, IOException{
    URLConnection urlConn =
        new URL(URL + request).openConnection();
    urlConn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
    BufferedReader input =
        new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));
    List<String> lines = new ArrayList<String>();
    {
      String line = input.readLine();
      while (line != null) {
        lines.add(line);
        line = input.readLine();
      }
    }
    return lines;
  }
}
