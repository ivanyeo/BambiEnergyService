package com.ndnxr.bambi;

import info.androidhive.tabsswipe.adapter.TabsPagerAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
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

public class Bambi extends FragmentActivity implements ActionBar.TabListener {

	// Declare UI Member
	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	private android.app.ActionBar actionBar;
	// Tab titles
	private String[] tabs = { "Energy Meter", "Task List", "App List" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initialization
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Adding Tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}

		// on swiping the view pager make respective tab selected
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.pager, new PlaceholderFragment()).commit();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Bind to BambiEnergyService
		bindBambiService();

	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// Wait for Service to start
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (mBambiServiceMessenger == null) {
						Thread.sleep(G.BAMBI_SERVICE_CONNECTION_WAIT_TIME);
					}
				} catch (InterruptedException e) {
					G.Log("onStart(): " + e.getMessage());
				}

				// Send message to get total number of bytes form
				// BambiEnergyService
				sendMessageToBambiService(BambiMessages.MESSAGE_GET_TOTAL_BYTES);

				// Send message to get Task list form BambiEnergyService
				sendMessageToBambiService(BambiMessages.MESSAGE_GET_TASK_LIST);

			}
		}).start();
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Unbind from BambiEnergyService
		unbindBambiService();
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
		intent.putExtra(BambiMessages.MESSAGE_ALARM,
				BambiMessages.MESSAGE_ALARM_ARRIVED);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

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
		Email email = new Email("cs246rocks@gmail.com", "cs202rocks",
				"smtp.gmail.com", "465", "woot", "subject here", "message",
				new String[] { "cs246rocks@gmail.com" }, null);

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
		WifiManager wifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);

		// Usually between 0 to -100 dBm; values closer to 0 means signal
		// strength is stronger.
		int signalStrength = wifiManager.getConnectionInfo().getRssi();

		G.Log("Wifi RSSI: " + signalStrength);
	}

	public void wifi_test(View v) {
		// Normal Task
		// Create email
		Email email = new Email("cs246rocks@gmail.com", "cs202rocks",
				"smtp.gmail.com", "465", "woot", "wifi test: NORMAL Task",
				"message", new String[] { "cs246rocks@gmail.com" }, null);

		// Create a task
		Task task = new Task(TASK_TYPE.EMAIL, URGENCY.NORMAL, null, email);

		// Create instance of BambiLib
		BambiLib bambiLib = new BambiLib(this);

		// Send email
		boolean output = bambiLib.sendEmail(task);
		G.Log("wifi_test(): Normal Task Scheduled!");

		// Schedule Task
		// Create Deadline
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, 8);

		Date deadline = calendar.getTime();

		// New email
		email = new Email("cs246rocks@gmail.com", "cs202rocks",
				"smtp.gmail.com", "465", "woot", "wifi test: SCHEDULE Task",
				"message", new String[] { "cs246rocks@gmail.com" }, null);

		// Create a task
		task = new Task(TASK_TYPE.EMAIL, URGENCY.SCHEDULE, deadline, email);

		// Send email
		output = bambiLib.sendEmail(task);

		// Shutdown Lib
		bambiLib.shutdown();

		G.Log("Here we go: " + output);
		G.Log("wifi_test() DONE!");
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
		if (currentapiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
			// API Level >= 17 to use getAllCellInfo()
			// Get Signal Strength based on connection type
			for (CellInfo cellInfo : telephonyManager.getAllCellInfo()) {
				int tempSignalStrength = 0;

				if (cellInfo instanceof CellInfoGsm) {
					// GSM
					CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
					CellSignalStrengthGsm cellSignalStrengthGsm = cellInfoGsm
							.getCellSignalStrength();
					tempSignalStrength = cellSignalStrengthGsm.getDbm();
				} else if (cellInfo instanceof CellInfoCdma) {
					// CDMA
					CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;

					CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma
							.getCellSignalStrength();
					tempSignalStrength = cellSignalStrengthCdma.getDbm();
				} else {
					// Shouldn't get here
					tempSignalStrength = -1;
				}

				// Store Signal Strength
				switch (tempSignalStrength) {
				case 0: // Error Checks
				case -1: // Fall through error condition
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
						// TODO Check TS 27.007 8.5 for verification of
						// conversion
						if (ss.getGsmSignalStrength() != 99) {
							signalStrength.signalStrength = ss
									.getGsmSignalStrength() * 2 - 113;
						} else {
							signalStrength.signalStrength = ss
									.getGsmSignalStrength();
						}
					} else {
						// CDMA Signal
						signalStrength.signalStrength = ss.getCdmaDbm();
					}

					// Unregister current listener
					telephonyManager.listen(this,
							PhoneStateListener.LISTEN_NONE);

					G.Log("Cell Signal Strength: "
							+ signalStrength.signalStrength);
				}
			};

			telephonyManager.listen(phoneStateListener,
					PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		}

		G.Log("Cell Signal Strength: " + signalStrength.signalStrength);
	}

	public void schedule_task(View v) {
		// Create email with attachment
		String attachment = null;
		String filename = "smurf2.png";

		if (this.getFileStreamPath(filename).exists()) {
			attachment = this.getFileStreamPath(filename).getAbsolutePath();
		}

		Email email = new Email("cs246rocks@gmail.com", "cs202rocks",
				"smtp.gmail.com", "465", "woot", "subject here", "message",
				new String[] { "cs246rocks@gmail.com" },
				new String[] { attachment });

		// Create Deadline
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		// calendar.add(Calendar.SECOND, 8);
		calendar.add(Calendar.SECOND, 10);

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

	public void schedule_normal_task(View v) {
		// Create email
		Email email = new Email("cs246rocks@gmail.com", "cs202rocks",
				"smtp.gmail.com", "465", "woot", "subject here: NORMAL TASK",
				"message", new String[] { "cs246rocks@gmail.com" }, null);

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

	public void schedule_1min_mail(View v) {
		// Create email with attachment
		String attachment = null;
		String filename = "smurf2.png";

		if (this.getFileStreamPath(filename).exists()) {
			attachment = this.getFileStreamPath(filename).getAbsolutePath();
		}

		Email email = new Email("cs246rocks@gmail.com", "cs202rocks",
				"smtp.gmail.com", "465", "woot", "subject here", "message",
				new String[] { "cs246rocks@gmail.com" },
				new String[] { attachment });

		// Create Deadline
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, 1);

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
		G.Log("schedule_1min_mail() DONE!");
	}

	public void delete_all_files(View v) {
		// G.Log("Not implemented.");
		G.Log("Deleting files ...");
		String[] files = { "FILE_NORMAL_TASKS", "FILE_SCHEDULE_TASKS",
				"FILE_CALLBACK_TASKS" };

		for (String file : files) {
			this.deleteFile(file);
		}

		G.Log("Done deleting all files.");
	}

	public void email_test(View v) {
		BambiMail m = new BambiMail("cs246rocks@gmail.com", "cs202rocks");

		String[] toArr = { "cs246rocks@gmail.com" };
		m.toArray = toArr;
		m.from = "wooo@wooo.com";
		m.subject = "This is an email sent using my Mail JavaMail wrapper from an Android device.";
		m.messageBody = "Email body.";

		try {
			// m.addAttachment("/sdcard/filelocation");

			if (m.sendEmail()) {
				Toast.makeText(this, "Email was sent successfully.",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "Email was not sent.", Toast.LENGTH_LONG)
						.show();
			}
		} catch (Exception e) {
			// Toast.makeText(MailApp.this,
			// "There was a problem sending the email.",
			// Toast.LENGTH_LONG).show();
			G.Log("Could not send email");
		}
	}

	public void get_service_message(View v) {
		sendMessageToBambiService(BambiMessages.MESSAGE_GET_TOTAL_BYTES);

		G.Log("get_service_message() DONE!");
	}

	public void save_service_message(View v) {
		// Make request to BambiEnergyService using System message
		try {
			// Get system message
			Message msg = Message.obtain(null,
					BambiMessages.MESSAGE_SAVE_TOTAL_BYTES);

			// Create bundle
			Bundle bundle = new Bundle();
			bundle.putLong(G.ENERGY_SERVICE_TOTAL_BYTES_PASSED_THROUGH, 300L*1024L);

			msg.setData(bundle);

			// Set replyTo handler
			msg.replyTo = mClientMessenger;

			// Send message to BambiEnergyService using the messenger
			mBambiServiceMessenger.send(msg);
		} catch (RemoteException e) {
			// If Service crashes, nothing to do here
		}

		G.Log("Message saved.");
	}

	private boolean sendMessageToBambiService(int message) {
		// Make request to BambiEnergyService using System message
		try {
			// Get system message
			Message msg = Message.obtain(null, message);

			// Set replyTo handler
			msg.replyTo = mClientMessenger;

			// Send message to BambiEnergyService using the messenger
			mBambiServiceMessenger.send(msg);
		} catch (RemoteException e) {
			// If Service crashes, nothing to do here
			return false;
		}

		return true;
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
	
	
	/**
	 * increase total bytes for proof of concept
	 */
	public void increaseTotalBytes(View v){
		// Increase baseline total bytes to 30GB
		save_service_message(v);
		
		// Send message to get total number of bytes form
		// BambiEnergyService
		sendMessageToBambiService(BambiMessages.MESSAGE_GET_TOTAL_BYTES);

	}

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

			// Messages that came from BambiEnergyService as replies
			case BambiMessages.MESSAGE_REPLY_TOTAL_BYTES: {
				Bundle bundle = msg.getData();
				long totalBytes = bundle
						.getLong(G.ENERGY_SERVICE_TOTAL_BYTES_PASSED_THROUGH);

				// Update the view's total number of bytes
				updateEnergySave(totalBytes);
				updateTotalBytes(totalBytes);

				G.Log("MESSAGE_REPLY_TOTAL_BYTES: " + totalBytes);
				break;
			}

			// Message that BambiEnergyService has completed processing a Task
			case BambiLib.MESSAGE_PROCESS_TASK_COMPLETE:

				// TODO: Get from Bambi a new set of data
				G.Log("MESSAGE_PROCESS_TASK_COMPLETE");

				// Send message to get total number of bytes form
				// BambiEnergyService
				sendMessageToBambiService(BambiMessages.MESSAGE_GET_TOTAL_BYTES);

				// Send message to get Task list form BambiEnergyService
				sendMessageToBambiService(BambiMessages.MESSAGE_GET_TASK_LIST);

				break;

			// Message that BambiEnergyService replies with a TaskList
			case BambiMessages.MESSAGE_REPLY_TASK_LIST: {
				// TODO: Update UI with the new Task List
				G.Log("MESSAGE_REPLY_TASK_LIST");

				// Get bundle data
				Bundle bundle = msg.getData();

				// Extract ArrayList<Task>
				bundle.setClassLoader(Task.class.getClassLoader());
				ArrayList<Task> replyList = (ArrayList<Task>) bundle
						.getSerializable(BambiMessages.MESSAGE_REPLY_TASK_LIST_KEY);
				updateTaskList(replyList);
				break;
			}

			default:
				super.handleMessage(msg);
				break;
			}
		}
	}

	/**
	 * Update UI to display task list
	 */
	private void updateTaskList(ArrayList<Task> replyList) {

		mAdapter.taskListFragment.drawTable(this, replyList);
	}

	/**
	 * Update UI to display total bytes of data transmission
	 * 
	 * @param totalBytes
	 */
	private void updateTotalBytes(long totalBytes) {
		totalDataBytes = totalBytes;
		mAdapter.energyMeterFragment.updateTotalBytes(totalBytes);
	}
	
	/**
	 * total data transmitted by Bambi
	 */
	public long totalDataBytes = 0;
	
	/**
	 * Update UI to display energy save ratio for pie chart
	 * 
	 * @param totalByte
	 */
	private void updateEnergySave(long totalByte) {
		
		// Convert to GB
		double totalGB = ((double) totalByte / 1024) / 1024;

		// Calculate energy saved percentage
		double EnergyWifi = 17.31 * (totalGB) - 2.28;
		double Energy3G = 71.27 * (totalGB) - 0.31;
		double EnergySave = Energy3G - EnergyWifi;
		double EnergySavePercent=0d;

		if (totalByte == 0) {
			energySavePercent = 0;
		} else if (EnergyWifi <= 0 || Energy3G <= 0) {
			energySavePercent = 1;
		} else {
			EnergySavePercent = EnergySave / Energy3G;
			energySavePercent = (int) (EnergySavePercent*100.0);
		}
		
		mAdapter.energyMeterFragment.updateEnergySaveChart(energySavePercent);
		// this.findViewById(R.id.pager).requestLayout();
	}
	
	/** Public variable of the total amount of energy saved by BambiEnergyService thus far. */
	public int energySavePercent = 0;

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

	/**
	 * Implement ActionBar.TabListener Integration
	 */
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	public void onTabSelected(Tab tab, FragmentTransaction arg1) {

	}

	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	public void onTabReselected(android.support.v7.app.ActionBar.Tab arg0,
			FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	public void onTabUnselected(android.support.v7.app.ActionBar.Tab arg0,
			FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabReselected(Tab arg0, android.app.FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction arg1) {
		// on tab selected
		// show respected fragment view
		viewPager.setCurrentItem(tab.getPosition());

	}

	@Override
	public void onTabUnselected(Tab arg0, android.app.FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

}
