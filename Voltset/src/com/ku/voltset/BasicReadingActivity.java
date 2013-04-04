package com.ku.voltset;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.yoctopuce.YoctoAPI.*;

import com.ku.voltset.R;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author chmod
 * 
 */
public class BasicReadingActivity extends Activity implements OnClickListener {
	float lastMeasurement = 0.0f;
	float currentMeasurement = 0.0f;
	ImageView image; // Arrow image
	Handler handler = null; // Handler for firing listening events on voltset
	int duration = 2500; // Animation duration not so fast
	String serial = null; // Serial number of device
	private final float Xaxis = 0.835366f; // Image Centers
	private final float Yaxis = 0.676692f; // Image Centers
	RotateAnimation rotate = null; // Rotate animation
	TextView mVoltsText; // Current measurement
	TextView prevText;// Previous measurement
	Logger log;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Fancy init stuff
		setContentView(R.layout.activity_main);
		Button settingsButton = (Button) findViewById(R.id.buttonSettings);
		image = (ImageView) findViewById(R.id.imageArrow);
		duration = this.getIntent().getIntExtra("duration", -1);
		serial = this.getIntent().getStringExtra("serial_number");
		settingsButton.setOnClickListener(this);
		mVoltsText = (TextView) findViewById(R.id.mVoltsText);
		prevText = (TextView) findViewById(R.id.prevText);
		log = new Logger(this);
		log.setFile("VoltSet.csv");
		handler = new Handler();
		handler.postDelayed(r, 500); // Start measuring at start of activity
		
		//Testing purposes button only.
		Button testButton = (Button) findViewById(R.id.button1);
		testButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) { // button clicks, on activity
		if (view.getId() == R.id.buttonSettings) {// touch on settings
			Intent settingsActivity = new Intent(this, SettingsActivity.class);
			settingsActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			if (duration == -1)
				duration = 2500;
			settingsActivity.putExtra("duration", duration);
			startActivity(settingsActivity);
			overridePendingTransition(R.anim.left_to_right,
					R.anim.right_to_left);
		}
		if (view.getId() == R.id.button1) {
			try {
				FileInputStream fstream = new FileInputStream(log.getFile());
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
				String strLine;
				// Read File Line By Line
				while ((strLine = br.readLine()) != null) {
					// Print the content on the console
					Toast.makeText(getApplicationContext(), strLine, 1000).show();
				}
				
				// Close the input stream
				in.close();
			} catch (IOException io) {
				io.printStackTrace();
			}
		}
	}

	/**
	 * Listener responsible for preventing arrow from resetting after animation
	 * end.
	 * 
	 */
	AnimationListener animationListener = new AnimationListener() {
		@Override
		public void onAnimationStart(Animation animation) {
			animation.setFillAfter(true);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub

		}
	};
	protected static String getTimeStamp(){
		return java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
	}
	/**
	 * Runnable binded to handler
	 * 
	 */
	final Runnable r = new Runnable() {
		public void run() {
			
			if (serial != null) {// So we finally have a serial, measure!
				YVoltage dc_sensor = YVoltage.FindVoltage(serial + ".voltage1"); // get
																					// the
																					// sensors,
																					// to
																					// be
																					// taken
																					// serialized/parced
																					// HardwareController

				try {
					currentMeasurement = Float.parseFloat((String.format(
							"%.1f", dc_sensor.getCurrentValue())));// We want
																	// decimal
																	// values
					if (currentMeasurement < 0) { // alert to reverse the
													// polarity
						Toast.makeText(getApplicationContext(),
								"Reverse your cables!", Toast.LENGTH_SHORT)
								.show();
						handler.postDelayed(this, 2000);
						return;
					} else if (currentMeasurement == 0) {// No more measuring
						if (rotate != null) {
							rotate.cancel();
							lastMeasurement = 0.0f;
							mVoltsText.setText("0.0V");
						}
					} else if (currentMeasurement == lastMeasurement) {
						log.write("EVENT:"+getTimeStamp()+" Measurement:" + currentMeasurement + "DC");
					} else if (currentMeasurement > lastMeasurement) { // Probably
																		// new
																		// measurement,
																		// catch
																		// it
						rotate = new RotateAnimation(0,
								Float.valueOf(currentMeasurement * 36),
								Animation.RELATIVE_TO_SELF, Xaxis,
								Animation.RELATIVE_TO_SELF, Yaxis);
						rotate.setInterpolator(new LinearInterpolator());
						rotate.setDuration(duration);
						rotate.setRepeatCount(0);
						rotate.setAnimationListener(animationListener);
						image.startAnimation(rotate);
						lastMeasurement = Float.parseFloat((String.format(
								"%.1f", dc_sensor.getCurrentValue())));
						prevText.setText(mVoltsText.getText().toString() + "V");
						mVoltsText.setText(String.format("%.1f %s",
								dc_sensor.getCurrentValue(),
								dc_sensor.getUnit())
								+ "V");

					} else {						
						
						rotate = new RotateAnimation(
								Float.valueOf(currentMeasurement * 36), 0,
								Animation.RELATIVE_TO_SELF, Xaxis,
								Animation.RELATIVE_TO_SELF, Yaxis);
						rotate.setInterpolator(new LinearInterpolator());
						rotate.setDuration(duration);
						rotate.setRepeatCount(0);
						rotate.setAnimationListener(animationListener);
						image.startAnimation(rotate);
					}

				} catch (YAPI_Exception e) {
					e.printStackTrace();
				}
				// YVoltage ac_sensor = YVoltage.FindVoltage(serial +
				// ".voltage2");//AC sensor, to be taken by HardwareController
				// try {
				// TextView view = (TextView) findViewById(R.id.acVolt);
				// view.setText(String.format("%.1f %s",
				// ac_sensor.getCurrentValue(), ac_sensor.getUnit()));
				// } catch (YAPI_Exception e) {
				// e.printStackTrace();
				// }
			}
			handler.postDelayed(this, 2000);// Run again in 2sec
		}
	};
}
