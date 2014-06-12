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
	
	/**
	 * Amount of time in milliseconds to wait for BambiEnergyService to be started.
	 */
	public static final int BAMBI_SERVICE_CONNECTION_WAIT_TIME = 200;
	
	/**
	 * Preference file that is private to BambiEnergyService.
	 */
	public static final String ENERGY_SERVICE_PREFERENCE_FILE_KEY = "com.ndnxr.bambienergyservice.SERVICE_PREFERENCE_FILE";
	
	/**
	 * Key for the total number of bytes that passed through BambiEnergyService
	 */
	public static final String ENERGY_SERVICE_TOTAL_BYTES_PASSED_THROUGH = "com.ndnxr.bambienergyservice.TOTAL_BYTES_PASSED_THROUGH";
	
	
	
	
}
