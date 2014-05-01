package com.ndnxr.bambi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BambiEnergyService extends Service {

	final String TAG = "-=]MyService[=-";
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO do something useful
//		Log.d(TAG, "MyService::onStartCommand()");
		G.Log("BambiEnergyService::onStartCommand()");
		// if the process is killed with no remaining start commands to 
		// deliver, then the service will be stopped instead of restarted
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO for communication return IBinder implementation
		G.Log("BambiEnergyService::onBind()");
		return null;
	}
	
	@Override
	public void onCreate() {
		G.Log("BambiEnergyService::onCreate()");
	}
	
	@Override
	public void onDestroy() {
		G.Log("BambiEnergyService::onDestroy()");
	}
}