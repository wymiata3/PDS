package com.ku.voltset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.ku.voltset.services.HardwareController_service;
import com.yoctopuce.YoctoAPI.YAPI;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class VoltSet extends Activity implements OnClickListener {
	HardwareController hc;
	final Context context = this;
	AlphaAnimation aa;
	public ImageView infoIcon;
	Messenger mService = null;
	boolean mIsBound;
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	@Override
	protected void onStart() {
		super.onStart();
		// doBindService();
		Log.d("YOCTOPUS_SERVICE", "startt");
		if (hc == null) {
			hc = new HardwareController(context, this);
		}
	}

	static class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HardwareController_service.MSG_SET_VALUE:
				Log.d("YOCTOPUS_SERVICE",
						String.valueOf("Message arrived:" + msg.arg1));
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			hc.terminate();
			YAPI.FreeAPI();
		} catch (Exception e) {
			// Free no matter what
		}
	}

	// Unused animations, future maybe?
	// public static Animation runFadeOutAnimationOn(Activity ctx, View target)
	// {
	// Animation animation = AnimationUtils.loadAnimation(ctx,
	// R.anim.fadeout);
	// target.startAnimation(animation);
	// return animation;
	// }
	// public static Animation runFadeInAnimationOn(Activity ctx, View target) {
	// Animation animation = AnimationUtils.loadAnimation(ctx,
	// R.anim.fedein);
	// target.startAnimation(animation);
	// return animation;
	// }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);
		Button basicReading = (Button) findViewById(R.id.basicReading);
		basicReading.setOnClickListener(this);
		Button conf = (Button) findViewById(R.id.conf);
		conf.setOnClickListener(this);
		Button quit = (Button) findViewById(R.id.quit);
		quit.setOnClickListener(this);
		infoIcon = (ImageView) findViewById(R.id.infoIcon);
		infoIcon.setEnabled(false);
		//try to rotate log if too big or too old
		Logger log=new Logger(this);
		log.setFile("VoltSet.csv");
		log.logRotate(25L,100);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.startup, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.basicReading) {
			if (hc != null && !hc.isSerialNull()) {
				Intent basicReadingActivity = new Intent(this,
						BasicReadingActivity.class);
				basicReadingActivity
						.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				basicReadingActivity.putExtra("duration", 2500);
				basicReadingActivity.putExtra("serial_number", hc.getSerial());
				startActivity(basicReadingActivity);
				overridePendingTransition(R.anim.left_to_right,
						R.anim.right_to_left);
			} else {
				Toast.makeText(getApplicationContext(),
						"Device not found, can't proceed", Toast.LENGTH_SHORT)
						.show();
				return;
			}

		}
		if (v.getId() == R.id.conf) {
			Intent settingsActivity = new Intent(this, SettingsActivity.class);
			settingsActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			settingsActivity.putExtra("duration", 2500);
			startActivity(settingsActivity);
			overridePendingTransition(R.anim.left_to_right,
					R.anim.right_to_left);
		}
		if (v.getId() == R.id.quit) {
			hc.terminate();
			hc = null;
			finish();
		}

	}

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

				// Give it some value as an example.
				msg = Message.obtain(null,
						HardwareController_service.MSG_SET_VALUE,
						this.hashCode(), 0);
				mService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even
				// do anything with it; we can count on soon being
				// disconnected (and then reconnected if it can be restarted)
				// so there is no need to do anything here.
			}

			// As part of the sample, tell the user what happened.
			Log.d("YOCTOPUS_SERVICE", "Connected.");
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;
			Log.d("YOCTOPUS_SERVICE", "Disconnected.");

		}
	};

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		bindService(new Intent(VoltSet.this, HardwareController_service.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
		Log.d("YOCTOPUS_SERVICE", "Binding.");
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
			Log.d("YOCTOPUS_SERVICE", "unbinding.");
		}
	}
}
