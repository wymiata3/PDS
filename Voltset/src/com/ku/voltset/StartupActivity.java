package com.ku.voltset;

import com.ku.voltset.services.HardwareController_service;
import com.yoctopuce.YoctoAPI.YAPI;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class StartupActivity extends FragmentActivity implements
		OnClickListener {
	private static final String TAG = "StartupActivity";
	final Context context = this;
	AlphaAnimation aa;
	public ImageView infoIcon;
	Messenger mService = null;
	boolean mIsBound = false;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	String yocto_serial = null;
	Bundle yocto_values=null;//holds device characteristics
	/**
	 * @author chmod Handles messages from service
	 */
	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HardwareController_service.MSG_SET_STRING_VALUE:
				String message = msg.getData().getString("serial");// Get the
																	// serial
				if (message.equalsIgnoreCase("null"))// device not found
				{
					yocto_serial = null; // Ensure it is null
					infoIcon.setEnabled(false); // No info
				} else // device found
				{
					yocto_serial = message; // get the serial
					runFadeInAnimationOn(context,infoIcon);
					infoIcon.setEnabled(true); // enable the info
				}
				break;
				//we asked for values
			case HardwareController_service.MSG_GET_YOCTO_VALUES:
				//parse them and place in bundle
				yocto_values= msg.getData();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			doUnbindService(); // clear all
			YAPI.FreeAPI(); // Important! Free the api!
		} catch (Throwable t) {
			Log.e("StartupActivity", "Failed to unbind from the service", t); // Log
																				// errors
		}
	}

	// Unused animations, future maybe?
	 public static Animation runFadeOutAnimationOn(Context ctx, View target)
	 {
	 Animation animation = AnimationUtils.loadAnimation(ctx,
	 R.anim.fadeout);
	 target.startAnimation(animation);
	 return animation;
	 }
	 public static Animation runFadeInAnimationOn(Context ctx, View target) {
	 Animation animation = AnimationUtils.loadAnimation(ctx,
	 R.anim.fedein);
	 target.startAnimation(animation);
	 return animation;
	 }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os
	 * .Bundle)
	 * 
	 * Save state so as not to register multiple services.
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean("service_started", mIsBound);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mIsBound = savedInstanceState.getBoolean("service_started");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);
		if (!mIsBound)
			doBindService();
		// Parse elements
		Button basicReading = (Button) findViewById(R.id.btnMeasure);
		basicReading.setOnClickListener(this);
		Button conf = (Button) findViewById(R.id.btnConf);
		conf.setOnClickListener(this);
		Button quit = (Button) findViewById(R.id.btnQuit);
		quit.setOnClickListener(this);
		infoIcon = (ImageView) findViewById(R.id.infoIcon);
		runFadeOutAnimationOn(context,infoIcon);	
		infoIcon.setEnabled(false);
		infoIcon.setOnClickListener(this);
		// try to rotate log if too big or too old
		Logger log = new Logger(this);
		log.setFile("VoltSet.csv");
		log.logRotate(25L, 100);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.startup, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnMeasure) {// User clicked Measurement
			if (yocto_serial != null) {
				Intent mainActivity = new Intent(this, MainActivity.class);
				mainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);// Bring
																				// activity
																				// to
																				// front
				startActivity(mainActivity);// And now start
				overridePendingTransition(R.anim.left_to_right, // with
																// animation
						R.anim.right_to_left);
			} else { // dont progress to next activity is serial is null
				Toast.makeText(getApplicationContext(),
						"Device not found, can't proceed", Toast.LENGTH_SHORT)
						.show();
				return;
			}

		}
		if (v.getId() == R.id.btnConf) {// User clicked conf
			Intent settingsActivity = new Intent(this, SettingsActivity.class);
			settingsActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			settingsActivity.putExtra("duration", 2500);
			startActivity(settingsActivity);
			overridePendingTransition(R.anim.left_to_right,
					R.anim.right_to_left);
		}
		if (v.getId() == R.id.btnQuit) {// user clicked quit
			finish();
		}
		if (v.getId() == R.id.infoIcon) {// user clicked info
			try {
				//Ask service for device characteristics
				Message msg = Message.obtain(null,
						HardwareController_service.MSG_GET_YOCTO_VALUES);
				msg.replyTo = mMessenger;
				mService.send(msg);
				//Instantiate new dialog
				InfoDialog info = new InfoDialog();
				//To bundle insert also our serial
				yocto_values.putString("serial", yocto_serial);
				//Set the bundle as arguments 
				info.setArguments(yocto_values);
				//And show it
				info.show(getSupportFragmentManager(), "info");
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	// Connection between service and activity
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mService = new Messenger(service);
			Log.d("YOCTOPUS_SERVICE", "Attached.");

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

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;
			Log.d(TAG, "Disconnected.");

		}
	};

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		bindService(new Intent(StartupActivity.this,
				HardwareController_service.class), mConnection,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
		Log.d(TAG, "Binding.");
	}

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
			unbindService(mConnection);
			mIsBound = false;
			Log.d(TAG, "unbinding.");
		}
	}
}
