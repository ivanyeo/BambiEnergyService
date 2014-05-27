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
	private static final String APP_STORAGE_FILENAME = "BAMBI_STORAGE_FILE";
	
	// Clients registered to this Service
	private ArrayList<Messenger> mClients = new ArrayList<Messenger>();

	@Override
	public void onCreate() {
		// TODO Load Service Tasks here
		
		G.Log("BambiEnergyService::onCreate()");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		throw new RuntimeException("BambiEnergyService is designed to be bound to and not run by invoking startService().");
		//G.Log("BambiEnergyService::onStartCommand()");

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
		// Save Service Tasks here
		
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
			FileOutputStream fos = this.openFileOutput(APP_STORAGE_FILENAME,
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
			FileInputStream fis = this.openFileInput(APP_STORAGE_FILENAME);
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