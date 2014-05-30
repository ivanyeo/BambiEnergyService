package com.ndnxr.bambi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BambiBootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		G.Log("BambiBootCompleteReceiver::onReceive()");
		
		// TODO startService() to: (1) Read in Tasks from Files and (2) Send anything that is past due
	}

}
