package uk.ac.cam.cl.dtg.android.time.data.handler;

public class BucksHandler extends ACISHandler {
	
	private static String URL = "http://bucks.acislive.com/LiveMaps/MapDataService.asmx/";
	private static String TAG = "bucks";
	
	// Constructor - pass into ACIS handler
	public BucksHandler() {
		
		super(URL, TAG);
		
	}
	
	public static void main(String[] args) {
		
		new BucksHandler().refreshDefinitions();
	}
}
