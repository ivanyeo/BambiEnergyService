package com.ndnxr.bambi;

public class BambiMessages {
	// Alarm Message: Key values used for Intent Messages
	public static final String MESSAGE_ALARM = "ALARM_MESSAGE";

	// Alarm Messages
	public static final int MESSAGE_ALARM_ARRIVED = 1;

	
	// Boot Complete Message: Key values used for Intent Messages
	public static final String MESSAGE_BOOT_COMPLETE = "BOOT_COMPLETE_MESSAGE";

	// Boot Complete Messages
	public static final int MESSAGE_BOOT_COMPLETE_ARRIVED = 2;

	
	// Wifi Connected Message: Key values used for Intent Messages
	public static final String MESSAGE_WIFI_CONNECTED = "WIFI_CONNECTED_MESSAGE";

	// Wifi Connected Messages
	public static final int MESSAGE_WIFI_CONNECTED_ARRIVED = 3;

	
	// Wifi Disconnected Message: Key values used for Intent Messages
	public static final String MESSAGE_WIFI_DISCONNECTED = "WIFI_DISCONNECTED_MESSAGE";

	// Wifi Disonnected Messages
	public static final int MESSAGE_WIFI_DISCONNECTED_ARRIVED = 3;
	

	// Getting of the total number of bytes that pass through BambiEnergyService
	public static final int MESSAGE_GET_TOTAL_BYTES = 4;
	public static final int MESSAGE_REPLY_TOTAL_BYTES = 5;
	
	// Message to be sent to BambiEnergyService to save the new total number of bytes passed in a Bundle
	public static final int MESSAGE_SAVE_TOTAL_BYTES = 6;
	
	
	// Getting of ArrayList<Task> from BambiEnergyService
	public static final int MESSAGE_GET_TASK_LIST = 7;
	public static final int MESSAGE_REPLY_TASK_LIST = 8;
	public static final String MESSAGE_REPLY_TASK_LIST_KEY = "TASK_ARRAY_LIST";
	
}
