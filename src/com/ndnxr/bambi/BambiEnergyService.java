package com.ndnxr.bambi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class BambiEnergyService extends Service {

	// Configurations
	private static final String FILENAME_NORMAL_TASKS = "FILE_NORMAL_TASKS";
	private static final String FILENAME_SCHEDULE_TASKS = "FILE_SCHEDULE_TASKS";
	private static final String FILENAME_CALLBACK_TASKS = "FILE_CALLBACK_TASKS";
	
	// Private Tasks to keep track of
	private ArrayList<Task> normalTaskList = null;
	private ArrayList<Task> scheduleTaskList = null;
	private ArrayList<Task> awaitingSignalStrengthCallbackList = null;

	// Clients registered to this Service
	private ArrayList<Messenger> mClients = new ArrayList<Messenger>();

	// Private startId so that Service is not accidentally stopped when
	// stopSelf(startId) is invoked
	private static int startId = 0;

	@Override
	public void onCreate() {
		super.onCreate();
		//android.os.Debug.waitForDebugger();
		
		// Load Bambi Tasks
		loadBambiTasks();

		G.Log("BambiEnergyService::onCreate()");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// throw new
		// RuntimeException("BambiEnergyService is designed to be bound to and not run by invoking startService().");
		G.Log("BambiEnergyService::onStartCommand()");

		// Update startId for use in stopSelf(startId)
		synchronized (BambiEnergyService.class) {
			BambiEnergyService.startId = startId;
		}

		// Get Intent message and check against BambiAlarm messages
		int message = intent.getIntExtra(BambiAlarm.MESSAGE_ALARM, 0);

		switch (message) {
		case BambiAlarm.MESSAGE_ALARM_ARRIVED:
			// TODO Process Tasks that have hit their deadlines in a different
			// thread and invoke bambiStopSelf()
			G.Log("BambiEnergyService::onStartCommand(): MESSAGE_ALARM_ARRIVED");

			new Thread(new Runnable() {
				@Override
				public void run() {
					// Process schduled Task list
					processScheduleTaskList();

					G.Log("DONE RUNNABLE THREAD!");

					bambiStopSelf();
				}
			}).start();

			break;
		}

		// Get Intent message and check against Bambi
		message = intent.getIntExtra(BambiLib.MESSAGE_STORE, 0);

		switch (message) {
		case BambiLib.MESSAGE_STORE_TASK:
			G.Log("MESSAGE_STORE_TASK: Processing Task Storage");

			// Get Task
			Task task = intent.getParcelableExtra(BambiLib.MESSAGE_TASK);

			// Store Task
			storeTask(task);

			break;
		case BambiLib.MESSAGE_STORE_OTHERS:
			G.Log("MESSAGE_STORE_OTHERS: Not implemented.");
			break;
		}

		// Tasks should invoke bambiStopSelf() upon completion
		// Request to Kernel to stop this Service
		// bambiStopSelf();

		// Setting:
		// If the process is killed with no remaining start commands to
		// deliver, then the service will be scheduled to start with the last
		// intent being re-delivered.
		return Service.START_REDELIVER_INTENT;
	}
	
	/**
	 * Method that processes tasks on the scheduleTaskList that have the deadline
	 * earlier or equal to the current time now.
	 */
	private void processScheduleTaskList() {
		// Get current time
		Date now = new Date();
		
		// Remaining Task list
		ArrayList<Task> remainingTasks = new ArrayList<Task>();
		
		// Process Tasks
		synchronized (scheduleTaskList) {
			// Iterate through Tasks
			for (Task t : scheduleTaskList) {
				if (t.getDeadline().before(now)) {
					// Process Task
					processTask(t);
				} else {
					// Add it to remaining Task that have to be scheduled to run
					remainingTasks.add(t);
				}
			}
			
			// Update scheduleTaskList
			scheduleTaskList = remainingTasks;
		}
	}
	
	/**
	 * Method that processes a given Task, based on the Task type.
	 * 
	 * @param task Task to be processed.
	 */
	private void processTask(Task task) {
		// Process Task according to Task type
		switch (task.getType()) {
		case EMAIL:
			// Extract data
			Email email = (Email) task.getPayload();
			
			// Create and Send Email
			BambiMail mail = new BambiMail(
					email.getUsername(),
					email.getPassword(),
					email.getServerAddress(),
					email.getServerPort(),
					email.getFrom(),
					email.getToArray(),
					email.getSubject(),
					email.getMessage()
					);
			
			// Add attachments if any
//			if (email.getFilePaths().length > 0) {
//				for (String filepath : email.getFilePaths()) {
//					if (!filepath.equals("")) {
//						mail.addFileAttachment(filepath);
//					}
//				}
//			}
			
			// Send email
			if (mail.sendEmail()) {
				G.Log("BambiEnergyService::processTask(): Email sent Successfully!");
			} else {
				G.Log("BambiEnergyService::processTask(): Error while sendnig email.");
			}
			
			break;
			
		default:
			throw new RuntimeException("Invalid TASK_TYPE.");
		}

	}

	/**
	 * Method attempts to store the given Task by first obtaining the cell's
	 * signal strength, depending on the supported API levels. For API 17 and
	 * above, the cell's signal strength can be obtained immediately. However,
	 * for API 17 and lower, a PhoneStateListener callback is required to obtain
	 * the signal strength.
	 * 
	 * Once the signal strength is obtained and updated in the given Task
	 * object, the scheduleTask(Task) method is invoked for the Task to be
	 * placed in the appropriate list and an Alarm set where applicable.
	 * 
	 * @param task
	 *            Task for which the cell signal should be obtained and stored
	 *            before being shipped off to be scheduled.
	 * 
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private void storeTask(Task task) {
		// Get current signal of Task base on API level
		/**
		 * Strategy: >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1: API 17
		 * and above, signal can be retrieved directly without the need for
		 * callback.
		 * 
		 * Othewise, callback of PhoneStateListener() is required.
		 */
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
					task.setCellSignalStrengthDbm(tempSignalStrength);
				}
			}

			// Finally, schedule the Task
			scheduleTask(task);

			G.Log("Cell Signal Strength: " + task.getCellSignalStrengthDbm());
		} else {
			// API Level < 17

			// Append to awaitingSignalStrengthCallback list
			awaitingSignalStrengthCallbackList.add(task);

			// Create PhoneStateListener to get Signal Strength
			PhoneStateListener phoneStateListener = new PhoneStateListener() {
				@Override
				public void onSignalStrengthsChanged(SignalStrength ss) {
					super.onSignalStrengthsChanged(ss);

					// Local Variable
					int signalStrength = 0;
					Task task = null;

					// Get Signal Strength
					if (ss.isGsm()) {
						// GSM Signal
						// TODO Check TS 27.007 8.5 for verification of
						// conversion
						if (ss.getGsmSignalStrength() != 99) {
							signalStrength = ss.getGsmSignalStrength() * 2 - 113;
						} else {
							signalStrength = ss.getGsmSignalStrength();
						}
					} else {
						// CDMA Signal
						signalStrength = ss.getCdmaDbm();
					}

					// Unregister current listener
					telephonyManager.listen(this,
							PhoneStateListener.LISTEN_NONE);

					// Update Task's signal and put it in the correct list
					synchronized (awaitingSignalStrengthCallbackList) {
						task = awaitingSignalStrengthCallbackList.remove(0);
						task.setCellSignalStrengthDbm(signalStrength);
					}

					// Finally, schedule the Task
					scheduleTask(task);

					// ... and Shutdown Service
					bambiStopSelf();

					G.Log("Cell Signal Strength: " + signalStrength);
				}
			};

			telephonyManager.listen(phoneStateListener,
					PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		}
	}

	/**
	 * Schedules a task by placing the task in the appropriate list and setting
	 * an alarm if any. This function does not stop the service; it merely
	 * schedules the Task.
	 * 
	 * @param task
	 *            Task to be scheduled
	 */
	private void scheduleTask(Task task) {
		switch (task.getUrgency()) {
		case SCHEDULE:
			// (1) Place in the schedule list
			scheduleTaskList.add(task);

			// (2) Set AlarmManager to wake at that time
			// Get the AlarmManager
			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

			// Create Intent
			Intent intent = new Intent(getBaseContext(),
					BambiAlarmReceiver.class);
			intent.putExtra(BambiAlarm.MESSAGE_ALARM,
					BambiAlarm.MESSAGE_ALARM_ARRIVED);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);

			// Getting Calendar instance and setting the deadline
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(task.getDeadline());

			alarmManager.set(AlarmManager.RTC_WAKEUP,
					calendar.getTimeInMillis(), pendingIntent);

			G.Log("BambiEnergyService::scheduleTask() with Deadline!");
			break;
		case NORMAL:
			// Add Task to the Normal Task list ... i.e waiting for Wifi
			normalTaskList.add(task);

			G.Log("BambiEnergyService::scheduleTask() NORMAL Task.");
			break;
		default:
			G.Log("BambiEnergyService::scheduleTask(): Invalid URGENCY Type.");
			break;
		}
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
		saveBambiTasks();
		G.Log("BambiEnergyService::onDestroy()");
	}

	/**
	 * Method that invokes Service.stopSelf(startId) for services that have been
	 * started with Context.startService(); startId is needed in event of
	 * concurrent invocations of startService() so that services aren't stop
	 * without the most recent startId being processed.
	 */
	private void bambiStopSelf() {
		// Local Variable
		int startId = 0;

		// Get current startId
		synchronized (BambiEnergyService.class) {
			startId = BambiEnergyService.startId;
		}

		// Invoke stop
		BambiEnergyService.this.stopSelf(startId);
	}

	/**
	 * Create Bambi Service messenger to be passed back as the IBinder when
	 * Clients are bound to this Service.
	 */
	final Messenger mBambiServiceMessenger = new Messenger(
			new BambiServiceHandler());

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
					msg.replyTo.send(Message.obtain(null,
							BambiLib.MESSAGE_REGISTER_SUCCESS));
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
					msg.replyTo.send(Message.obtain(null,
							BambiLib.MESSAGE_UNREGISTER_SUCCESS));
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
	 * Function that saves all Tasks to the persistent file storage. This
	 * function is invoked when the Service is destroyed or unloaded.
	 */
	private void saveBambiTasks() {
		// Save array lists
		saveArrayListToFile(FILENAME_NORMAL_TASKS, normalTaskList);
		saveArrayListToFile(FILENAME_SCHEDULE_TASKS, scheduleTaskList);
		saveArrayListToFile(FILENAME_CALLBACK_TASKS, awaitingSignalStrengthCallbackList);
	}

	private void saveArrayListToFile(String filename, ArrayList<Task> list) {
		try {
			FileOutputStream fos = this.openFileOutput(filename, Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);

			os.writeObject(list);
			os.close();

			G.Log(filename + ": Write success");
		} catch (FileNotFoundException e) {
			G.Log("Error: " + e.getMessage());
		} catch (IOException e) {
			G.Log("Error: " + e.getMessage());
		}
	}

	/**
	 * Function that loads all Tasks from the persistent storage when the
	 * service is loaded.
	 */
	private void loadBambiTasks() {
		// Load Files
		normalTaskList = loadArrayListFromFile(FILENAME_NORMAL_TASKS);
		scheduleTaskList = loadArrayListFromFile(FILENAME_SCHEDULE_TASKS);
		awaitingSignalStrengthCallbackList = loadArrayListFromFile(FILENAME_CALLBACK_TASKS);
	}

	private ArrayList<Task> loadArrayListFromFile(String filename) {
		// Local Variable
		File file = this.getFileStreamPath(filename);
		ArrayList<Task> list = null;

		// Check that file exists before loading
		if (file.exists()) {
			try {
				FileInputStream fis = this.openFileInput(filename);
				ObjectInputStream is = new ObjectInputStream(fis);
				Object o = is.readObject();

				if (o instanceof ArrayList) {
					list = (ArrayList<Task>) o;
					G.Log("ArrayList found in filename: " + filename);
					G.Log("ArrayList LENGTH: " + list.size());
				} else {
					G.Log(filename + " does not contain an ArrayList Object.");
				}

				is.close();
			} catch (FileNotFoundException e) {
				G.Log(e.getMessage());
			} catch (IOException e) {
				G.Log(e.getMessage());
			} catch (ClassNotFoundException e) {
				G.Log(e.getMessage());
			}
			
			G.Log("loadArrayListFromFile(): READ FILE: " + filename);
		} else {
			// Create an empty list
			list = new ArrayList<Task>();
			
			G.Log("loadArrayListFromFile(): EMPTY LIST");
		}

		G.Log("loadArrayListFromFile(): Done!");
		
		return list;
	}
}
