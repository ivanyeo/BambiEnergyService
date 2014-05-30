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
	        	// Wifi Connected
	        	// TODO startService() and send all normal and scheduled Tasks
	        } else {
	            // Wifi Disconnected
	        }
	    }
	}

}
