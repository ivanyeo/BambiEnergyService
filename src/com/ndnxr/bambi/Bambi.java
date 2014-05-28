package com.ndnxr.bambi;

import com.ndnxr.bambi.BambiLib.TASK_TYPE;
import com.ndnxr.bambi.BambiLib.URGENCY;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class Bambi extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void toast_message(View v) {
		// TODO Remove this block of code
		Toast.makeText(this, "Hello!", Toast.LENGTH_LONG).show();
	}

	public void start_service(View v) {
		// use this to start and trigger a service
		Intent i = new Intent(this, BambiEnergyService.class);

		// potentially add data to the intent
		i.putExtra("KEY1", "Value to be used by the service");
		this.startService(i);
	}

	public void bind_service(View v) {
		// TODO: Remove this block of code
		Intent i = new Intent(this, BambiEnergyService.class);
		i.putExtra("KEY1", "Value to be used by the service");
		G.Log("MainActivity::bind_service()");
		this.bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);

		final Bambi outside = this;
		new Thread() {
			public void run() {
				G.Log("Thread - going to sleep");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				G.Log("Unbinding service");
				outside.unbindService(mServiceConnection);
			}
		}.start();
	}

	public void bind_service2(View v) {
		bindBambiService();
	}

	public void unbind_service(View v) {
		unbindBambiService();
	}

	public void send_email(View v) {
		// Create email
		Email email = new Email("to@to.com", "from@from.com", "subject", "message here");
		
		// Create a task
		Task task = new Task(TASK_TYPE.EMAIL, URGENCY.URGENT, null, email);
		
		// Create instance od BambiLib
		BambiLib bambiLib = new BambiLib(this);
		
		// Send email
		boolean output = bambiLib.sendEmail(task);
		
		G.Log("Here we go: " + output);
		G.Log("send_email() DONE!");
	}

	public void write_file(View v) {
		G.Log("Nothing done here. Function to be removed.");
	}

	public void read_file(View v) {
		G.Log("Nothing done here. Function to be removed.");
	}

	/**
	 * Method that binds to the BambiEnergyService.
	 */
	private void bindBambiService() {
		// Bind to Service
		this.bindService(new Intent(this, BambiEnergyService.class),
				mServiceConnection, Context.BIND_AUTO_CREATE);

		// Set bound flag
		mIsBambiServiceBound = true;

		// Output Log
		G.Log("MainActivity::bindBambiService()");
	}

	/**
	 * Method that unbinds from the BambiEnergyService.
	 */
	private void unbindBambiService() {
		// Unregister Service if it has been bound
		if (mIsBambiServiceBound) {
			if (mBambiServiceMessenger != null) {
				try {
					// Send Message to BambiEnergy Service to UNREGISTER_CLIENT
					Message msg = Message.obtain(null,
							BambiLib.MESSAGE_UNREGISTER_CLIENT);

					msg.replyTo = mClientMessenger;

					mBambiServiceMessenger.send(msg);
				} catch (RemoteException e) {
					// Service has crashed, nothing to do here
					G.Log("MainActivity::unbindBambiService() Error: " + e);
				}
			}

			// Release connection
			this.unbindService(mServiceConnection);
			mIsBambiServiceBound = false;

			G.Log("MainActivity::unbindBambiService(): Success");
		}
	}

	/** Messenger connection to BambiEnergyService */
	private Messenger mBambiServiceMessenger = null;

	/** Flag if client is connected to BambiEnergyService */
	private boolean mIsBambiServiceBound = false;

	/** Client ServiceConnection to BambiEnergyService */
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// Bound to BambiEnergyService, establish Messenger to the Service
			mBambiServiceMessenger = new Messenger(service);

			// Make request to register client
			try {
				Message msg = Message.obtain(null,
						BambiLib.MESSAGE_REGISTER_CLIENT);
				msg.replyTo = mClientMessenger;
				mBambiServiceMessenger.send(msg);
			} catch (RemoteException e) {
				// If Service crashes, nothing to do here
			}

			G.Log("mServiceConnection::onServiceConnected()");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// In event of unexpected disconnection with the Service.
			// Not expecting to get here.
			mServiceConnection = null;
			// mIsBambiServiceBound = false;

			G.Log("mConnection::onServiceDisconnected()");
		}
	};

	/** Client Message Handler */
	final Messenger mClientMessenger = new Messenger(new ClientHandler());

	/**
	 * Client Message Handler Class
	 */
	class ClientHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BambiLib.MESSAGE_REGISTER_SUCCESS:
				G.Log("ClientHandler::handleMessage(): MESSAGE_REGISTER_SUCCESS");
				break;
			case BambiLib.MESSAGE_UNREGISTER_SUCCESS:
				G.Log("ClientHandler::handleMessage(): MESSAGE_UNREGISTER_SUCCESS");
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
