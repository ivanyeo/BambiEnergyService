package com.ndnxr.bambi;

import java.util.Calendar;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ndnxr.bambi.BambiLib.TASK_TYPE;
import com.ndnxr.bambi.BambiLib.URGENCY;

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
	
	public void set_alarm(View v) {
		// Get the AlarmManager
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		// Create Intent
		Intent intent = new Intent(getBaseContext(), BambiAlarmReceiver.class);
		intent.putExtra(BambiAlarm.MESSAGE_ALARM, BambiAlarm.MESSAGE_ALARM_ARRIVED);
		
		PendingIntent pendingIntent = PendingIntent
				.getBroadcast(this, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);

		G.Log("Setup the alarm");

		// Getting current time and add the seconds in it
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 3);

		alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				pendingIntent);

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
		
		// Create instance of BambiLib
		BambiLib bambiLib = new BambiLib(this);
		
		// Send email
		boolean output = bambiLib.sendEmail(task);
		
		// Shutdown Lib
		bambiLib.shutdown();
		
		G.Log("Here we go: " + output);
		G.Log("send_email() DONE!");
	}

	public void write_file(View v) {
		G.Log("Nothing done here. Function to be removed.");
	}

	public void read_file(View v) {
		G.Log("Nothing done here. Function to be removed.");
	}
	
	public void get_wifi_strength(View v) {
		// Get WifiManager from System Service
		WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		
		// Usually between 0 to -100 dBm; values closer to 0 means signal strength is stronger.
		int signalStrength = wifiManager.getConnectionInfo().getRssi();
		
		G.Log("Wifi RSSI: " + signalStrength);
	}
	
	// Capture of Signal Strength
	class SS {
		public int signalStrength = 0;
	}
	
	public final SS signalStrength = new SS();
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void get_mobile_strength(View v) {
		// Local Variable
		final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		
		// Get current API Level
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		
		// Execute necessary API Level
		if (currentapiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1){
		    // API Level >= 17 to use getAllCellInfo()
			// Get Signal Strength based on connection type
			for (CellInfo cellInfo : telephonyManager.getAllCellInfo()) {
				int tempSignalStrength = 0;
				
				if (cellInfo instanceof CellInfoGsm) {
					// GSM
					CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
					CellSignalStrengthGsm cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();
					tempSignalStrength = cellSignalStrengthGsm.getDbm();
				} else if (cellInfo instanceof CellInfoCdma) {
					// CDMA
					CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
					
					CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
					tempSignalStrength = cellSignalStrengthCdma.getDbm();
				} else {
					// Shouldn't get here
					tempSignalStrength = -1;
				}
				
				// Store Signal Strength
				switch (tempSignalStrength) {
				case 0:		// Error Checks
				case -1:	// Fall through error condition
					continue;
					
				default:
					// Store signal strength value
					signalStrength.signalStrength = tempSignalStrength;
				}
			}
			
			
			G.Log("Cell Signal Strength: " + signalStrength.signalStrength);
		} else {
		    // API Level < 17
			// Create PhoneStateListener to get Signal Strength
			PhoneStateListener phoneStateListener = new PhoneStateListener() {
		        @Override
		        public void onSignalStrengthsChanged(SignalStrength ss) {
		            super.onSignalStrengthsChanged(ss);
		            
		            // Get Signal Strength
		            if (ss.isGsm()) {
		            	// GSM Signal
		            	// TODO Check  TS 27.007 8.5 for verification of conversion
		                if (ss.getGsmSignalStrength() != 99) {
		                    signalStrength.signalStrength = ss.getGsmSignalStrength() * 2 - 113;
		                } else {
		                    signalStrength.signalStrength = ss.getGsmSignalStrength();
		                }
		            } else {
		            	// CDMA Signal
		                signalStrength.signalStrength = ss.getCdmaDbm();
		            }
		            
		            // Unregister current listener
		            telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
		            
		            G.Log("Cell Signal Strength: " + signalStrength.signalStrength);
		        }
			};
			
			telephonyManager.listen(phoneStateListener,      
					PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		}
		
		G.Log("Cell Signal Strength: " + signalStrength.signalStrength);
	}
	
	public void schedule_task (View v) {
		// Create email
		Email email = new Email("to@to.com", "from@from.com", "subject", "message here");
		
		// Create Deadline
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(new Date());
	    calendar.add(Calendar.SECOND, 8);
	    
		Date deadline = calendar.getTime();
		
		// Create a task
		Task task = new Task(TASK_TYPE.EMAIL, URGENCY.SCHEDULE, deadline, email);
		
		// Create instance of BambiLib
		BambiLib bambiLib = new BambiLib(this);
		
		// Send email
		boolean output = bambiLib.sendEmail(task);
		
		// Shutdown Lib
		bambiLib.shutdown();
		
		G.Log("Here we go: " + output);
		G.Log("schedule_task() DONE!");
	}
	
	public void schedule_normal_task (View v) {
		// Create email
		Email email = new Email("to@to.com", "from@from.com", "subject", "message here");
		
		// Create a task
		Task task = new Task(TASK_TYPE.EMAIL, URGENCY.NORMAL, null, email);
		
		// Create instance of BambiLib
		BambiLib bambiLib = new BambiLib(this);
		
		// Send email
		boolean output = bambiLib.sendEmail(task);
		
		// Shutdown Lib
		bambiLib.shutdown();
		
		G.Log("Here we go: " + output);
		G.Log("schedule_normal_task() DONE!");		
	}
	
	public void delete_all_files(View v) {
		G.Log("Not implemented.");
//		G.Log("Deleting files ...");
//		boolean out = this.deleteFile(BambiEnergyService.FILENAME_NORMAL_TASKS);
//		G.Log(out + "");
//		out = this.deleteFile(BambiEnergyService.FILENAME_CALLBACK_TASKS);
//		G.Log(out + "");
//		out = this.deleteFile(BambiEnergyService.FILENAME_SCHEDULE_TASKS);
//		G.Log(out + "");
//		this.deleteFile("BAMBI_STORAGE_FILE");
//		G.Log("Done deleting all files.");
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
