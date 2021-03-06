package com.ndnxr.bambi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BambiAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		G.Log("BambiAlarmReceiver::onReceive()");

		// Get Intent message
		int message = intent.getIntExtra(BambiMessages.MESSAGE_ALARM, 0);

		switch (message) {
		case BambiMessages.MESSAGE_ALARM_ARRIVED:
			// Create service intent
			Intent serviceIntent = new Intent(context, BambiEnergyService.class);
			
			// Append Message
			serviceIntent.putExtra(BambiMessages.MESSAGE_ALARM, BambiMessages.MESSAGE_ALARM_ARRIVED);
			
			// Start service
			context.startService(serviceIntent);
			break;
		case 0:
			// No such key in Intent: Incorrect message
			break;
		default:
			// No such message, do nothing
			break;
		}
	}

}
