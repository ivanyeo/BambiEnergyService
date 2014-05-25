package com.ndnxr.bambi;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class BambiEnergyService extends Service {

	// Clients registered to this Service
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();

	// Messages to be passed to the Service by Clients for processing
	static final int MESSAGE_REGISTER_CLIENT 	= 1;
	static final int MESSAGE_UNREGISTER_CLIENT 	= 2;
	static final int MESSAGE_REGISTER_SUCCESS	= 3;
	static final int MESSAGE_UNREGISTER_SUCCESS	= 4;
	
	// Supported Service Functions
	static final int MESSAGE_SEND_EMAIL			= 11;
//	static final int MESSAGE_REGISTER_SUCCESS	= 3;

	@Override
	public void onCreate() {
		G.Log("BambiEnergyService::onCreate()");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		G.Log("BambiEnergyService::onStartCommand()");

		// Setting:
		// If the process is killed with no remaining start commands to
		// deliver, then the service will be stopped instead of restarted
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		G.Log("BambiEnergyService::onBind()");
		
		// return an IBinder interface for the Client to use
		return mBambiServiceMessenger.getBinder();
	}

	@Override
	public void onDestroy() {
		G.Log("BambiEnergyService::onDestroy()");
	}

	/**
	 * Create Bambi Service messenger to be passed back as the IBinder when Clients 
	 * are bound to this Service.
	 */
	final Messenger mBambiServiceMessenger = new Messenger(new BambiServiceHandler());
	
	/**
	 * Handle messages from Clients that are connected to this Service.
	 */
	class BambiServiceHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_REGISTER_CLIENT:
					// Add Client to list
					mClients.add(msg.replyTo);
					
					// Respond registration successful to client
					try {
						msg.replyTo.send(Message.obtain(null, MESSAGE_REGISTER_SUCCESS));
					} catch (RemoteException e) {
						// Client dead
						mClients.remove(msg.replyTo);
					}
				break;
			case MESSAGE_UNREGISTER_CLIENT:
					// Remove Client from list
					mClients.remove(msg.replyTo);
					
					// Respond unregistration successful to client
					try {
						msg.replyTo.send(Message.obtain(null, MESSAGE_UNREGISTER_SUCCESS));
					} catch (RemoteException e) {
						// Client dead
						mClients.remove(msg.replyTo);
					}
				break;

			default:
					// Pass on to the super class
					super.handleMessage(msg);
				break;
			}
		}
	}
}