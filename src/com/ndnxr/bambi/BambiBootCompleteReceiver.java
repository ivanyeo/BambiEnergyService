package com.ndnxr.bambi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BambiBootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		G.Log("BambiBootCompleteReceiver::onReceive()");
		
		// TODO startService() and do these in the Service
		/*		(1) Read in Tasks from Files (done in service)
		 *  	(2) Send anything that is past due
		 *  	(3) Set Alarm for SCHEDULE Tasks in the future as AlarmManager gets flushed on re-boot
		 */
	}

}
