package com.ndnxr.bambi;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class BambiEnergyService extends Service {

	// Configuration
	private static final String SERVICE_STORAGE_FILENAME = "BAMBI_SERVICE_STORAGE_FILE";
	
	// Clients registered to this Service
	private ArrayList<Messenger> mClients = new ArrayList<Messenger>();

	// Private startId so that Service is not accidentally stopped when stopSelf(startId) is invoked
	private static int startId = 0;
	
	@Override
	public void onCreate() {
		// TODO Load Service Tasks here
		
		G.Log("BambiEnergyService::onCreate()");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//throw new RuntimeException("BambiEnergyService is designed to be bound to and not run by invoking startService().");
		G.Log("BambiEnergyService::onStartCommand()");
		
		// Update startId for use in stopSelf(startId)
		synchronized (BambiEnergyService.class) {
			BambiEnergyService.startId = startId;
		}
		
		// Get Intent message and check against BambiAlarm messages
		int message = intent.getIntExtra(BambiAlarm.MESSAGE_ALARM, 0);

		switch (message) {
		case BambiAlarm.MESSAGE_ALARM_ARRIVED:
			// TODO Process Tasks that have hit their deadlines
			break;
		case 0:
			// No such key in Intent: Incorrect message
			break;
		default:
			// No such message, do nothing
			break;
		}	
		// Update startId
		synchronized (BambiEnergyService.class) {
			BambiEnergyService.startId = startId;
		}

		// Setting:
		// If the process is killed with no remaining start commands to
		// deliver, then the service will be scheduled to start with the last 
		// intent being re-delivered.
		return Service.START_REDELIVER_INTENT;
		
		// Setting:
		// If the process is killed with no remaining start commands to
		// deliver, then the service will be stopped instead of restarted
		//return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		G.Log("BambiEnergyService::onBind()");
		
		// return an IBinder interface for the Client to use
		return mBambiServiceMessenger.getBinder();
	}

	@Override
	public void onDestroy() {
		// TODO Save Service Tasks here
		
		G.Log("BambiEnergyService::onDestroy()");
	}
	
	/**
	 * Method that invokes Service.stopSelf(startId) for services that have been started
	 * with Context.startService(); startId is needed in event of concurrent invocations
	 * of startService() so that services aren't stop without the most recent startId 
	 * being processed.
	 */
	private void bambiStopSelf() {
		int startId = 0;
		
		// Get current startId
		synchronized (BambiEnergyService.class) {
			startId = BambiEnergyService.startId;
		}
		
		// Invoke stop
		BambiEnergyService.this.stopSelf(startId);
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
			case BambiLib.MESSAGE_REGISTER_CLIENT:
					// Add Client to list
					mClients.add(msg.replyTo);
					
					// Respond registration successful to client
					try {
						msg.replyTo.send(Message.obtain(null, BambiLib.MESSAGE_REGISTER_SUCCESS));
					} catch (RemoteException e) {
						// Client dead
						mClients.remove(msg.replyTo);
					}
				break;
				
			case BambiLib.MESSAGE_UNREGISTER_CLIENT:
					// Remove Client from list
					mClients.remove(msg.replyTo);
					
					// Respond unregistration successful to client
					try {
						msg.replyTo.send(Message.obtain(null, BambiLib.MESSAGE_UNREGISTER_SUCCESS));
					} catch (RemoteException e) {
						// Client dead
						mClients.remove(msg.replyTo);
					}
				break;

			case BambiLib.MESSAGE_SEND_EMAIL:
				G.Log("WE GET TO SEND THE EMAIL!!");
				// Extract Email object through IPC 
				// Set class loader to be used
				msg.getData().setClassLoader(Email.class.getClassLoader());
				
				// Get actual Email object
				Email email = (Email) msg.getData().getParcelable("email");
				
				G.Log("BAMBISERVICE, Email: " + email.toString());
				break;
				
			default:
					// Pass on to the super class
					super.handleMessage(msg);
				break;
			}
		}
	}
	
	/**
	 * Function that saves all Tasks to the persistent file storage. This function
	 * is invoked when the Service is destroyed or unloaded.
	 */
	private void saveBambiTasks() {
		// TODO Write actual code for this function
		try {
			FileOutputStream fos = this.openFileOutput(SERVICE_STORAGE_FILENAME,
					Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);

			Email email = new Email("to", "from", "subject", "message");

			os.writeObject(email);
			os.close();
			
			G.Log("Write success");
		} catch (FileNotFoundException e) {
			G.Log("Error: " + e.getMessage());
		} catch (IOException e) {
			G.Log("Error: " + e.getMessage());
		}		
	}
	
	/**
	 * Function that loads all Tasks from the persistent storage when the service is
	 * loaded.
	 */
	private void loadBambiTasks() {
		// TODO Clean up this function for actual use
		try {
			FileInputStream fis = this.openFileInput(SERVICE_STORAGE_FILENAME);
			ObjectInputStream is = new ObjectInputStream(fis);
			Object o =  is.readObject();
			
			if (o instanceof Email) {
				G.Log("Woohoo! Instance of email!");
			} else {
				G.Log("Nope, couldn't make out what file object was read in.");
			}
			
			is.close();
		} catch (FileNotFoundException e) {
			G.Log(e.getMessage());
		} catch (IOException e) {
			G.Log(e.getMessage());
		} catch (ClassNotFoundException e) {
			G.Log(e.getMessage());
		}
	}
}
