package com.ku.voltset;

import com.yoctopuce.YoctoAPI.*;


import com.ku.voltset.R;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
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

public class MainActivity extends Activity implements OnClickListener {
	private float lastMeasurement = 0.0f;
	private float currentMeasurement = 0.0f;
	private ImageView image;

	final Handler mHandler = new Handler();
	private int duration = -1;
	private String serial = null;
	private Handler handler = null;
	private final float Xaxis = 0.835366f;
	private final float Yaxis = 0.676692f;
	private RotateAnimation rotateAnimation1 = null;
	private TextView mVoltsText;
	private TextView prevText;// = 
	AnimationListener al = new AnimationListener() {

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
	final Runnable r = new Runnable() {
		public void run() {
			if (serial != null) {
				YVoltage dc_sensor = YVoltage.FindVoltage(serial + ".voltage1");
				try {
					currentMeasurement = Float.parseFloat((String.format(
							"%.1f", dc_sensor.getCurrentValue())));
					if (currentMeasurement == 0) {
						if (rotateAnimation1 != null) {
							rotateAnimation1.cancel();
							lastMeasurement = 0.0f;
							mVoltsText.setText("0.0V");
						}
					} else if (currentMeasurement == lastMeasurement) {
						//Same measurement, do nothing
					} else {
						rotateAnimation1 = new RotateAnimation(0,
								Float.valueOf(currentMeasurement * 36),
								Animation.RELATIVE_TO_SELF, Xaxis,
								Animation.RELATIVE_TO_SELF, Yaxis);
						rotateAnimation1
								.setInterpolator(new LinearInterpolator());
						rotateAnimation1.setDuration(duration);
						rotateAnimation1.setRepeatCount(0);
						rotateAnimation1.setAnimationListener(al);
						image.startAnimation(rotateAnimation1);
						lastMeasurement = Float.parseFloat((String.format(
								"%.1f", dc_sensor.getCurrentValue())));
						prevText.setText(mVoltsText.getText().toString()+"V");
						mVoltsText.setText(String.format("%.1f %s",
								dc_sensor.getCurrentValue(), dc_sensor.getUnit())+"V");

					}

				} catch (YAPI_Exception e) {
					e.printStackTrace();
				}
				YVoltage ac_sensor = YVoltage.FindVoltage(serial + ".voltage2");
				try {
					TextView view = (TextView) findViewById(R.id.acVolt);
					view.setText(String.format("%.1f %s",
							ac_sensor.getCurrentValue(), ac_sensor.getUnit()));
				} catch (YAPI_Exception e) {
					e.printStackTrace();
				}
			}
			handler.postDelayed(this, 1000);
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		try {

			YAPI.EnableUSBHost(this);
			YAPI.RegisterHub("usb");
			YModule module = YModule.FirstModule();
			while (module != null) {
				Toast.makeText(getApplicationContext(),
						module.getProductName(), Toast.LENGTH_LONG).show();
				if (module.get_productName().equalsIgnoreCase("Yocto-Volt")) {
					serial = module.get_serialNumber();
				}
				module = module.nextModule();
			}
		} catch (YAPI_Exception e) {
			e.printStackTrace();
		}
		handler.postDelayed(r, 500);
	}

	@Override
	protected void onStop() {
		super.onStop();
		handler.removeCallbacks(r);
		YAPI.FreeAPI();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		Button simButton = (Button) findViewById(R.id.simButton);
		Button settingsButton = (Button) findViewById(R.id.buttonSettings);
		image = (ImageView) findViewById(R.id.imageArrow);

		duration = this.getIntent().getIntExtra("duration", -1);
		simButton.setOnClickListener(this);
		settingsButton.setOnClickListener(this);
		mVoltsText= (TextView) findViewById(R.id.mVoltsText);
		prevText=(TextView) findViewById(R.id.prevText);
		handler = new Handler();

	}

	@Override
	public void onClick(View view) {

		if (view.getId() == R.id.buttonSettings) {
			Intent settingsActivity = new Intent(this, SettingsActivity.class);
			settingsActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (duration == -1)
				duration = 2500;
			settingsActivity.putExtra("duration", duration);
			startActivity(settingsActivity);
			overridePendingTransition(R.anim.left_to_right,
					R.anim.right_to_left);
		}
//		if (view.getId() == R.id.simButton) {
//
//			
//			
//
//			if (editText.getText().length() == 0) {
//				Toast.makeText(getApplicationContext(), "Input can't be empty",
//						Toast.LENGTH_SHORT).show();
//				return;
//			}
//			if ((Float.valueOf(editText.getText().toString()) < 0)
//					|| (Float.valueOf(editText.getText().toString()) > 180)) {
//				Toast.makeText(getApplicationContext(),
//						"Unaccepted value (0-180)", Toast.LENGTH_SHORT).show();
//				return;
//			}
//
//			if (lastMeasurement == 0.00) {
//				prevText.setText("-");
//
//			} else {
//				lastMeasurement = Float.valueOf(mVoltsText
//						.getText()
//						.toString()
//						.substring(
//								0,
//								mVoltsText.getText().toString()
//										.indexOf("Volts")));
//				String result = String.format(Locale.getDefault(), "%.2f",
//						lastMeasurement);
//				prevText.setText(result);
//			}
//
//			if (duration == -1)
//				duration = 2500;
//			// Animation start
//
//			String result = String.format(Locale.getDefault(), "%.2f",
//					Float.parseFloat(editText.getText().toString()));
//			mVoltsText.setText(result + "Volts");
//			try {
//				lastMeasurement = Float.valueOf(mVoltsText
//						.getText()
//						.toString()
//						.substring(
//								0,
//								mVoltsText.getText().toString()
//										.indexOf("Volts")));
//			} catch (Exception e) {
//				Log.e("voltset", e.getLocalizedMessage());
//			}
//
//		}
	}
}
