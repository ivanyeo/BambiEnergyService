package com.ndnxr.bambi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

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
		// TODO Auto-generated method stub
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
		Intent i = new Intent(this, BambiEnergyService.class);
		i.putExtra("KEY1", "Value to be used by the service");
		G.Log("MainActivity::bind_service()");
		this.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
		
		final MainActivity outside = this;
		new Thread() {
			public void run() {
				G.Log("Thread - going to sleep");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				G.Log("Unbinding service");
				outside.unbindService(mConnection);
			}
		}.start();
	}
	
	// Service Items
//	BambiEnergyService 

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
//			LocalBinder binder = (LocalBinder) service;
//			mService = binder.getService();
//			mBound = true;
			G.Log("mConnection::onServiceConnected()");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
//			mBound = false;
			G.Log("mConnection::onServiceDisconnected()");
		}
	};

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
