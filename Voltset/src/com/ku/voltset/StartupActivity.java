package com.ku.voltset;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YModule;

import android.os.Bundle;
import android.os.Handler;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class StartupActivity extends FragmentActivity implements
		OnClickListener {
	final Context context = this;
	ImageView infoIcon;
	Bundle yocto_values = null;// holds device characteristics
	private static final String file = "VoltSet.csv"; // Our log file
	Handler serialscanner = new Handler();
	YModule module = null;
	String serial = "null";
	Runnable scan = new Runnable() {
		@Override
		public void run() {
			try {
				// Scan for device changes
				YAPI.UpdateDeviceList();
				// be sure to have initialized value
				serial = "null";
				module = null;
				// scan for module
				module = YModule.FirstModule();
				while (module != null) {
					if (module.get_productName().equals("Yocto-Volt")) {
						serial = module.get_serialNumber();
						infoIcon.setEnabled(true);
						// remove transparency
						infoIcon.setAlpha(1f);
						break;
					}
					module = module.nextModule();
				}
				// end of scan
				// re run at interval of 500ms
				serialscanner.postDelayed(scan, 500);
			} catch (YAPI_Exception yapi) {
				yapi.printStackTrace();
			}

		}

	};

	@Override
	protected void onStart() {
		super.onStart();
		try {
			// enable usb for all application
			YAPI.EnableUSBHost(getApplicationContext());
			// register events
			YAPI.RegisterHub("usb");
			// aaaand again be sure all is null
			serial = "null";
			module = null;
			// start the scanner
			serialscanner.postDelayed(scan, 500);
		} catch (YAPI_Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @author chmod Handles messages from service
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);
		// Parse elements
		Button basicReading = (Button) findViewById(R.id.btnMeasure);
		basicReading.setOnClickListener(this);
		Button conf = (Button) findViewById(R.id.btnConf);
		conf.setOnClickListener(this);
		Button share = (Button) findViewById(R.id.btnShare);
		share.setOnClickListener(this);
		Button quit = (Button) findViewById(R.id.btnQuit);
		quit.setOnClickListener(this);
		infoIcon = (ImageView) findViewById(R.id.infoIcon);
		// play animation for icon if module not found
		runFadeOutAnimationOn(context, infoIcon).setFillAfter(true);
		// we cant take values if module not plugged
		infoIcon.setEnabled(false);
		infoIcon.setOnClickListener(this);
		// try to rotate log if too big or too old
		Logger log = new Logger(this.getApplicationContext());
		log.setFile(file);
		log.logRotate(25L, 100);
		log = null; // Not used anymore
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Plays fade out animation see res/anim/ for more info
	 * 
	 * @param ctx
	 *            Context the widget belongs
	 * @param target
	 *            the widget to be animated
	 * @return
	 */
	public static Animation runFadeOutAnimationOn(Context ctx, View target) {
		Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.fadeout);
		target.startAnimation(animation);
		return animation;
	}

	/**
	 * Plays fade in animation see res/anim/ for more info
	 * 
	 * @param ctx
	 *            Context the widget belongs
	 * @param target
	 *            the widget to be animated
	 * @return
	 */
	public static Animation runFadeInAnimationOn(Context ctx, View target) {
		Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.fedein);
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
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.startup, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// User clicked Measurement
		if (v.getId() == R.id.btnMeasure) {
			// we have connected device?
			if (!serial.equalsIgnoreCase("null")) {
				// remove the callbacks, we are in another activity
				serialscanner.removeCallbacks(scan);
				// init the new activity
				Intent mainActivity = new Intent(this, MainActivity.class);
				// start activity
				startActivity(mainActivity);
				// with animation
				overridePendingTransition(R.anim.left_to_right,
						R.anim.right_to_left);
				// dont progress to next activity is serial is "null"
				// and inform user
			} else {
				Toast.makeText(getApplicationContext(),
						"Device not found, can't proceed", Toast.LENGTH_SHORT)
						.show();
				return;
			}

		}
		// clicked on share
		if (v.getId() == R.id.btnShare) {

			Intent share = new Intent(this, Share_function.class);
			// Bring activity to front
			share.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			// start activity
			startActivity(share);
			// with animation
			overridePendingTransition(R.anim.left_to_right,
					R.anim.right_to_left);

		}
		// User clicked conf
		if (v.getId() == R.id.btnConf) {
			// init activity
			Intent settingsActivity = new Intent(this, SettingsActivity.class);
			// set the flags
			settingsActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			// duration of arrow animation
			settingsActivity.putExtra("duration", 2500);
			// start activity
			startActivity(settingsActivity);
			// with animation
			overridePendingTransition(R.anim.left_to_right,
					R.anim.right_to_left);
		}
		if (v.getId() == R.id.btnQuit) {// user clicked quit
			finish();
		}
		if (v.getId() == R.id.infoIcon) {// user clicked info
			if (!serial.equalsIgnoreCase("null")) {
				try {
					// initialize the bundle
					yocto_values = new Bundle();
					// parse module values
					yocto_values.putString("Luminosity",
							String.format("%d%%", module.get_luminosity()));
					yocto_values.putString("UpTime", module.get_upTime() / 1000
							+ " sec");
					yocto_values.putString("UsbCurrent",
							module.get_usbCurrent() + " mA");
					yocto_values.putString("Beacon",
							(module.get_beacon() == YModule.BEACON_ON) ? "on"
									: "off");
					yocto_values.putString("serial", module.getSerialNumber());
					// parse module values end
				} catch (YAPI_Exception e) {
					e.printStackTrace();
				}
				// Instantiate new dialog
				InfoDialog info = InfoDialog.newInstance(yocto_values);
				// And show it
				info.show(getSupportFragmentManager(), "info");
			}
		}

	}
}
