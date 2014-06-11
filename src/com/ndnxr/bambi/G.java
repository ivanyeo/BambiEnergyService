package com.ndnxr.bambi;

public class G {
	private static final String TAG = "Bambi";
	
	public static void Log(String msg) {
		android.util.Log.d(TAG, msg);
	}
	
	/**
	 *  Timeout for checking Wifi connection in BambiEnergyService::processTask()
	 *  in milliseconds. 
	 */
	public static final int WIFI_RETRY_TIMEOUT = 200;
	
	/**
	 * Number of times that the Wifi connection should be probed before being
	 * considered non-active.
	 */
	public static final int WIFI_RETRY_COUNT = 8;
}
