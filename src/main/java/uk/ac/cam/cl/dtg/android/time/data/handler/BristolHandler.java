package uk.ac.cam.cl.dtg.android.time.data.handler;

public class BristolHandler extends ACISHandler {
	
	private static String BRISTOL_URL = "http://bristol.acislive.com/LiveMaps/MapDataService.asmx/";
	private static String BRISTOL_TAG = "bristol";
	
	public BristolHandler() {
		super(BRISTOL_URL, BRISTOL_TAG);
		
	}
}
