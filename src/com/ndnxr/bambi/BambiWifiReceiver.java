package com.ndnxr.bambi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class BambiWifiReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		G.Log("BambiWifiReceiver::onReceive()");
		
		// Extract intent action
		final String action = intent.getAction();
		
		// Check Connection
	    if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
	        if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)){
	        	G.Log("BambiWifiReceiver: Wifi Connected!");
	        	// Wifi Connected
	        	
	    		// Create service intent
	    		Intent serviceIntent = new Intent(context, BambiEnergyService.class);

	    		// Append Message
	    		serviceIntent.putExtra(BambiMessages.MESSAGE_WIFI_CONNECTED, BambiMessages.MESSAGE_WIFI_CONNECTED_ARRIVED);

	    		// Start service
	    		context.startService(serviceIntent);
	    		
	        } else {
	        	G.Log("BambiWifiReceiver: Wifi Disonnected!");
	            // Wifi Disconnected
	        	
	        	/*
	        	 * Because Android does not provide an API to check if a service is running,
	        	 * we have to send a message to the service in order to take care of the case
	        	 * when the Service is already running and the Wifi connection was lost.
	        	 * 
	        	 * This allows BambiEnergyService to update it's internal boolean variable wifiConnected
	        	 * if it is running so as to not allow Tasks to make use of the Wifi connection.
	        	 * 
	        	 * Reference:
	        	 * http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-in-android
	        	 * https://groups.google.com/forum/#!topic/android-developers/jEvXMWgbgzE
	        	 */
	        	
	    		// Create service intent
	    		Intent serviceIntent = new Intent(context, BambiEnergyService.class);

	    		// Append Message
	    		serviceIntent.putExtra(BambiMessages.MESSAGE_WIFI_DISCONNECTED, BambiMessages.MESSAGE_WIFI_DISCONNECTED_ARRIVED);

	    		// Start service
	    		context.startService(serviceIntent);	        	
	        }
	    }
	}

}
