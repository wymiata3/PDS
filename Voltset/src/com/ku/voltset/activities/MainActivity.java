package com.ku.voltset.activities;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.ku.voltset.Logger;
import com.ku.voltset.R;
import com.ku.voltset.fragments.DIYFragment;
import com.ku.voltset.fragments.EduFragment;
import com.ku.voltset.fragments.ProFragment;
import com.ku.voltset.services.HardwareController_service;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Main activity that holds the three fragments and communicates with service.
 * 
 * @author chmod
 * 
 */
public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener, TextToSpeech.OnInitListener,
		DIYFragment.onVoiceOnOff {
	Messenger mService = null;
	BluetoothClient bluetoothClient;
	private static final int REQUEST_ENABLE_BT = 1;
	List<String> mArrayAdapter = new ArrayList<String>();
	boolean mIsBound = false;
	// Log tag
	private static final String TAG = "MainActivity";
	// Message handler
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	final Context context = this;
	DIYFragment diyFragment = null;
	EduFragment eduFragment = null;
	ProFragment proFragment = null;
	private static final String file = "VoltSet.csv"; // Our log file
	Logger log;
	boolean isVoiceEnabled = false;
	boolean isBTEnabled = false;
	// init colors, max and avg
	Boolean alreadyColoredAC = false;
	Boolean alreadyColoredDC = false;
	static double max = 0.0;
	static double avg = 0.0;
	static int measurements = 0;
	static double maxEDU = 0.0;
	static double avgEDU = 0.0;
	static int measurementsEDU = 0;
	// TTS object
	private TextToSpeech mTts;
	// status check code
	private int MY_DATA_CHECK_CODE = 1234;
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	protected static String getTimeStamp() {
		return java.text.DateFormat.getDateTimeInstance().format(
				Calendar.getInstance().getTime());
	}

	public void onInit(int i) {
		// do we have tts engine?
		if (i == TextToSpeech.SUCCESS) {
			// is us locale avaible? Then set it
			if (mTts.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
				mTts.setLanguage(Locale.US);
			// inform about error
		} else if (i == TextToSpeech.ERROR) {
			Toast.makeText(this, "Sorry! Text To Speech failed...",
					Toast.LENGTH_LONG).show();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_settings:
			if (!item.isChecked()) {
				BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
						.getDefaultAdapter();
				if (mBluetoothAdapter == null) {
					// Device does not support Bluetooth
				}
				if (!mBluetoothAdapter.isEnabled()) {
					Intent enableBtIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				}
				Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
						.getBondedDevices();
				// If there are paired devices
				if (pairedDevices.size() > 0) {
					// Loop through paired devices
					for (BluetoothDevice device : pairedDevices) {
						// Add the name and address to an array adapter to show
						// in a
						// ListView
						mArrayAdapter.add(device.getAddress());

					}
				}
				item.setChecked(true);
				isBTEnabled = true;
			} else {
				isBTEnabled = false;
				item.setChecked(false);
				// TODO Stop bt
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// data check code is just a checksum, can be any value
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				mTts = new TextToSpeech(this, this);
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// start internal activity to check if there is TTS engine
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
		doBindService();
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// TODO implement it
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// TODO implement it
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		@SuppressWarnings("javadoc")
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			// Init the objects for each tab
			if (position == 0) {
				DIYFragment fragment = new DIYFragment();
				return fragment;
			} else if (position == 1) {
				ProFragment fragment = new ProFragment();
				return fragment;
			} else if (position == 2) {
				EduFragment fragment = new EduFragment();
				return fragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			// get the TAB caption in capitals
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * Inner Class responsible to process messages from service
	 * 
	 * @author chmod
	 * 
	 */
	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HardwareController_service.MSG_DISCONNECT:
				// PANIC
				// try to unbind
				doUnbindService();
				// Device is unplugged
				// inform user
				Toast.makeText(context, "Error! Device unplugged",
						Toast.LENGTH_LONG).show();
				// switch to startup activity
				Intent startupActivity = new Intent(context,
						StartupActivity.class);
				// reoder to front flag
				startupActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(startupActivity);
				break;
			case HardwareController_service.MSG_MEASUREMENT:
				try {
					String voltage = "";// immutable
					// logger is already initialized
					if (log == null) {
						log = new Logger(context);
						log.setFile(file);
					}
					// parse the value
					voltage = msg.getData().getString("dc");
					// parse the holded value, null if no "holded" in the bundle
					String holded = msg.getData().getString("holded");
					// avoid null
					if (diyFragment == null)
						diyFragment = (DIYFragment) getSupportFragmentManager()
								.findFragmentByTag(
										"android:switcher:" + R.id.pager + ":0");
					if (eduFragment == null)
						eduFragment = (EduFragment) getSupportFragmentManager()
								.findFragmentByTag(
										"android:switcher:" + R.id.pager + ":2");
					// we need to update the correct fragment
					if (diyFragment.isVisible()) {
						// update text
						diyFragment.updateMeasureText(voltage);
						// Log event to file, T:time, M:measurement
						log.write("T:" + getTimeStamp() + "|M:" + voltage
								+ "DC");

						// Colorize
						// AC
						if (alreadyColoredAC == false) {
							// Assume it's AC
							if (Double.valueOf(voltage) > 50) {
								// Color red
								diyFragment.setColorAC(Color.RED);
								// And remove color from DC
								diyFragment.setColorDC(getResources().getColor(
										R.color.generalColor));
							}
							// its colored, no need to recolor
							alreadyColoredAC = true;
						}
						// DC
						if (alreadyColoredDC == false) {
							// DC is 0< >50, !!Tommy!!
							if (Double.valueOf(voltage) > 0
									&& Double.valueOf(voltage) < 50) {
								// Color red the DC
								diyFragment.setColorDC(Color.RED);
								// And remove color from AC
								diyFragment.setColorAC(getResources().getColor(
										R.color.generalColor));
							}
							alreadyColoredDC = true;
						}
						// No color
						// Ensure we run only once!
						if (alreadyColoredAC == true
								|| alreadyColoredDC == true) {
							if (Double.valueOf(voltage) == 0) {
								diyFragment.setColorDC(getResources().getColor(
										R.color.generalColor));
								diyFragment.setColorAC(getResources().getColor(
										R.color.generalColor));
								alreadyColoredAC = false;
								alreadyColoredDC = false;
							}
						}
						// Colorize end
						if (holded != null) {
							// show the avg and max
							measurements += 1;
							if (max < Double.valueOf(holded))
								max = Double.valueOf(holded);
							avg += Double.valueOf(voltage) / measurements;
							showAVGandMAX(avg, max);
							if (isVoiceEnabled) {
								// Drop all pending entries in the
								// playback queue.
								mTts.speak(String.valueOf(holded) + " Volts.",
										TextToSpeech.QUEUE_FLUSH, null);
							}
							// update the value and play animation
							diyFragment.updateHolded(holded);
							if (isBTEnabled) {
								Bundle data = new Bundle();
								data.putString("holded", holded);
								data.putString("avg",
										String.format("%.2f", avg));
								data.putString("max", String.valueOf(max));
								bluetoothClient = new BluetoothClient(
										mArrayAdapter, data);
								bluetoothClient.start();
							}
						}
					} else if (eduFragment.isVisible()) {
						eduFragment.updateMeasureText(voltage);
						// Log event to file, T:time, M:measurement
						log.write("T:" + getTimeStamp() + "|M:" + voltage
								+ "DC");
						if (holded != null) {
							measurementsEDU += 1;
							if (maxEDU < Double.valueOf(holded))
								maxEDU = Double.valueOf(holded);
							avgEDU += Double.valueOf(voltage) / measurements;
							showAVGandMAX(avgEDU, maxEDU);
							eduFragment.updateHolded(holded);
							if (isVoiceEnabled) {
								// Drop all pending entries in the
								// playback queue.
								mTts.speak(String.valueOf(holded) + " Volts.",
										TextToSpeech.QUEUE_FLUSH, null);
							}
							// inform user to remove cables
							Toast.makeText(getApplicationContext(),
									"Remove cables now", Toast.LENGTH_SHORT)
									.show();
							// rotate the imageview if value was holded.
							eduFragment.rotateArrowUpwards(holded);
						}

					}
				} catch (Exception e) {
					Log.d(TAG, "Exception", e);
				}

				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Presentes the average and max in screen through TOAST
	 * 
	 * @param AVG
	 *            the average value of measurements
	 * @param MAX
	 *            the maximum value of measurements
	 */
	private void showAVGandMAX(double AVG, double MAX) {
		Toast.makeText(
				getApplicationContext(),
				"AVG:" + String.format("%.2f", AVG) + " MAX:"
						+ String.valueOf(MAX), Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onDestroy() {
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}
		try {
			// unbind the service, view is destroyed
			doUnbindService();
		} catch (Throwable t) {
			Log.e(TAG, "Failed to unbind from the service", t);
		}
		super.onDestroy();
	}

	/**
	 * Connection between service and activity
	 */
	ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mService = new Messenger(service);
			Log.d(TAG, "Attached.");

			// We want to monitor the service for as long as we are
			// connected to it.
			try {
				Message msg = Message.obtain(null,
						HardwareController_service.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even
				// do anything with it; we can count on soon being
				// disconnected (and then reconnected if it can be restarted)
				// so there is no need to do anything here.
			}

			// As part of the sample, tell the user what happened.
			Log.d(TAG, "Connected.");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.ServiceConnection#onServiceDisconnected(android.content
		 * .ComponentName)
		 */
		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;
			Log.d(TAG, "Disconnected.");

		}
	};

	/**
	 * Establish a connection with the service. We use an explicit class name
	 * because there is no reason to be able to let other applications replace
	 * our component. Run in another thread
	 */
	void doBindService() {
		Thread t = new Thread() {
			public void run() {
				getApplicationContext().bindService(
						new Intent(MainActivity.this,
								HardwareController_service.class), mConnection,
						Context.BIND_AUTO_CREATE);
			}
		};
		mIsBound = true;
		t.start();
		Log.d(TAG, "Binding.");
	}

	/**
	 * Unbind service from activity Called at onDestoy()
	 */
	void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							HardwareController_service.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service
					// has crashed.
				}
			}
			// Detach our existing connection.
			getApplicationContext().unbindService(mConnection);
			mIsBound = false;
			Log.d(TAG, "MainActivity - unbinding.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ku.voltset.DIYFragment.onVoiceOnOff#onVoiceOn()
	 */
	@Override
	public void onVoiceOn() {
		isVoiceEnabled = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ku.voltset.DIYFragment.onVoiceOnOff#onVoiceOff()
	 */
	@Override
	public void onVoiceOff() {
		isVoiceEnabled = false;
	}

	/**
	 * @author chmod Inner class to send messages to bluetooth
	 */
	class BluetoothClient extends Thread {

		BluetoothAdapter mBluetoothAdapter;
		private Bundle data = null;
		List<String> devices;
		String voltage = null;
		String avg = null;
		String max = null;

		public BluetoothClient(List<String> deviceList, Bundle data) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			this.devices = deviceList;
			this.data = data;
			voltage = data.getString("holded");
			avg = data.getString("avg");
			max = data.getString("max");
		}

		public void run() {
			BluetoothSocket clientSocket = null;
			// Client knows the server MAC address
			for (String device : devices) {
				BluetoothDevice mmDevice = mBluetoothAdapter
						.getRemoteDevice(device);
				Log.e("BT", device);
				try {
					// UUID string same used by server
					clientSocket = mmDevice
							.createRfcommSocketToServiceRecord(UUID
									.fromString("00001101-0000-1000-8000-00805F9B34FB"));
					clientSocket.connect();
					PrintWriter pWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
					pWriter.write("Holded: " + voltage + " Volts " + "MAX: "
							+ max + " AVG: " + avg);
//					DataOutputStream out = new DataOutputStream(
//							clientSocket.getOutputStream());
//					out.writeUTF("Holded: " + voltage + " Volts " + "MAX: "
//							+ max + " AVG: " + avg); // Send to server
				} catch (Exception e) {
					Log.e("BT", "ERROR", e);
				}
			}
		}
	}

}