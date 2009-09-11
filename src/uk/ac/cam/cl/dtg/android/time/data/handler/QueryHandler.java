package uk.ac.cam.cl.dtg.android.time.data.handler;

import java.io.IOException;
import java.util.List;

import uk.ac.cam.cl.dtg.android.time.buses.*;

public interface QueryHandler {
	
	// Provide live arrival information at a Bus Stop
	public List<BusArrival> listArrivals(String ATCOCode, int numberOfArrivals) throws IOException;

	
}
