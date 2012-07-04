package uk.ac.cam.cl.dtg.android.time.data.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import uk.ac.cam.cl.dtg.android.time.buses.ArrivalTime;
import uk.ac.cam.cl.dtg.android.time.buses.BusArrival;
import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import uk.ac.cam.cl.dtg.android.time.servlet.ServletUtils;
import uk.ac.cam.cl.dtg.time.data.handler.AbstractHandler;

/**
 * Handler for the Traveline NextBusses API
 * 
 * @author drt24
 * 
 */
public class TravelineHandler extends AbstractHandler {

  private final String username;
  private final String authToken;

  public TravelineHandler() throws SQLException, NamingException, UnsupportedEncodingException {
    this.sourceID = "traveline";
    PreparedStatement ps =
        ServletUtils.getConnection().prepareStatement(
            "SELECT username, password FROM credentials WHERE provider = " + sourceID);
    try {
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        username = rs.getString("username");
        String password = rs.getString("password");
        authToken = Base64.encodeBase64String((username + ":" + password).getBytes("UTF-8"));
      } else {
        throw new SQLException("Credentials not found");
      }
    } finally {
      ps.close();
    }
  }

  public TravelineHandler(String username, String password) throws UnsupportedEncodingException {
    this.username = username;
    authToken = Base64.encodeBase64String((username + ":" + password).getBytes("UTF-8"));
  }

  private static final String requestBase = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
  		"<Siri version=\"1.0\" xmlns=\"http://www.siri.org.uk/\">" +
  		"<ServiceRequest>" +
  		  "<RequestTimestamp>%s</RequestTimestamp>" +
  		  "<RequestorRef>%s</RequestorRef>" +
  		  "<StopMonitoringRequest version=\"1.0\">" +
  		    "<RequestTimestamp>%s</RequestTimestamp>" +
  		    "<MessageIdentifier>12345</MessageIdentifier>" +
  		    "<MonitoringRef>%s</MonitoringRef>" +
  		  "</StopMonitoringRequest>" +
  		"</ServiceRequest></Siri>";
  private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

  @Override
  public List<BusArrival> listArrivals(String atcoCode, int numberOfArrivals) throws IOException {
    HttpClient httpClient = new DefaultHttpClient();
    HttpPost httpPost = new HttpPost("http://nextbus.mxdata.co.uk/nextbuses/1.0/1");
    String timestamp = dateFormat.format(new Date());
    String request = String.format(requestBase, timestamp, username, timestamp, atcoCode);
    httpPost.setEntity(new StringEntity(request));
    httpPost.setHeader("Authorization", "Basic " + authToken);
    HttpResponse response = httpClient.execute(httpPost);
    InputStream content = response.getEntity().getContent();
    return parseSiri(content);
  }

  protected List<BusArrival> parseSiri(InputStream content) throws IOException {
    try {
      XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
      SiriResponseHandler handler = new SiriResponseHandler();
      reader.setContentHandler(handler);

      reader.parse(new InputSource(content));

      return handler.getArrivals();
    } catch (SAXException e) {
      throw new IOException(e);
    } catch (ParserConfigurationException e) {
      throw new IOException(e);
    } finally {
      content.close();
    }
  }

  @Override
  protected List<BusStop> getAllStops() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  private static class SiriResponseHandler extends DefaultHandler {
    // we are interested in things inside Siri/ServiceDelivery/StopMonitoringDelivery
    // We want each
    // MonitoredStopVisit/{PublishedLineName,DirectionName,MonitoredCall/{ExpectedDepartureTime,AimedDepartureTime}}
    private static final String INTEREST = "StopMonitoringDelivery";
    private static final String ARRIVAL = "MonitoredStopVisit";
    private static final String LINE_NAME = "PublishedLineName";
    private static final String DIRECTION = "DirectionName";
    private static final String EXPECTED_TIME = "ExpectedDepartureTime";
    private static final String AIMED_TIME = "AimedDepartureTime";

    private enum STATE {
      INSTOPMONITORING, INMONITOREDSTOP, OUTSIDEINTEREST
    };

    private final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");

    private STATE state = STATE.OUTSIDEINTEREST;
    private String lineName = null;
    private String direction = null;
    private ArrivalTime time = null;
    private List<BusArrival> arrivals = new ArrayList<BusArrival>();
    private StringBuilder characters = null;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
      switch (state) {
        case OUTSIDEINTEREST: {
          if (INTEREST.equals(qName)) {
            state = STATE.INSTOPMONITORING;
          }
          break;
        }
        case INSTOPMONITORING: {
          if (ARRIVAL.equals(qName)) {
            state = STATE.INMONITOREDSTOP;
            characters = new StringBuilder();
          }
          break;
        }
        case INMONITOREDSTOP: {
          characters = new StringBuilder();
          break;
        }
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
      if (state == STATE.INMONITOREDSTOP) {
        characters.append(ch, start, length);
      }
    }

    private Date parseEvilTimestamp(String original) throws ParseException {
      final int origLength = original.length();
      String last = original.substring(origLength - 2, origLength);
      String first = original.substring(0, origLength - 3);
      return formatter.parse(first + last);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      try {
        switch (state) {
          case INMONITOREDSTOP: {
            if (LINE_NAME.equals(qName)) {
              lineName = characters.toString();
              characters = new StringBuilder();
            } else if (DIRECTION.equals(qName)) {
              direction = characters.toString();
              characters = new StringBuilder();
            } else if (EXPECTED_TIME.equals(qName)) {
              String original = characters.toString();
              String last = original.substring(original.length() - 2, original.length());
              String first = original.substring(0, original.length() - 3);
              time = new ArrivalTime(formatter.parse(first + last).getTime());
              time.isLiveData = true;
              characters = new StringBuilder();
            } else if (AIMED_TIME.equals(qName)) {
              if (time == null) {// Don't override expected times with aimed ones

                time = new ArrivalTime(parseEvilTimestamp(characters.toString()).getTime());
              }
              characters = new StringBuilder();
            } else if (ARRIVAL.equals(qName)) {
              arrivals.add(new BusArrival(lineName, direction, time));
              lineName = null;
              direction = null;
              time = null;
              state = STATE.INSTOPMONITORING;
            }
            break;
          }
          case INSTOPMONITORING: {
            if (INTEREST.equals(qName)) {
              state = STATE.OUTSIDEINTEREST;
            }
            break;
          }
          case OUTSIDEINTEREST:
            break;// DoKempston nothing as already outside

        }
      } catch (ParseException e) {
        throw new SAXException(e);
      }
    }

    public List<BusArrival> getArrivals() {
      return arrivals;
    }
  }
}
