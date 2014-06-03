package com.ndnxr.bambi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BambiBootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		G.Log("BambiBootCompleteReceiver::onReceive()");
		
		// startService() and do these in the Service
		/*		(1) Read in Tasks from Files (done in service)
		 *  	(2) Send anything that is past due
		 *  	(3) Set Alarm for SCHEDULE Tasks in the future as AlarmManager gets flushed on re-boot
		 */

		// Create service intent
		Intent serviceIntent = new Intent(context, BambiEnergyService.class);

		// Append Message
		serviceIntent.putExtra(BambiMessages.MESSAGE_BOOT_COMPLETE, BambiMessages.MESSAGE_BOOT_COMPLETE_ARRIVED);

		// Start service
		context.startService(serviceIntent);
	}

}
