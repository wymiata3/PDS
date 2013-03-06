package com.ku.voltset;


import com.example.helloworld.R;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener,
		AnimationListener {
	private double lastMeasurement = 0.00;
	private ImageView image;
	float currentValue = 0.00f;
	final Handler mHandler = new Handler();
	private int duration=-1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button simButton = (Button) findViewById(R.id.simButton);
		Button settingsButton = (Button) findViewById(R.id.buttonSettings);
		image = (ImageView) findViewById(R.id.imageArrow);
		duration= this.getIntent().getIntExtra("duration",-1);
		simButton.setOnClickListener(this);
		settingsButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {

		if (view.getId() == R.id.buttonSettings) {
			Intent settingsActivity = new Intent(this, SettingsActivity.class);
			settingsActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if(duration==-1)
				duration=2500;
			settingsActivity.putExtra("duration",duration);
			startActivity(settingsActivity);
			overridePendingTransition(R.anim.left_to_right,
					R.anim.right_to_left);
		}
		if (view.getId() == R.id.simButton) {
			final EditText editText = (EditText) findViewById(R.id.simText);
			TextView prevText = (TextView) findViewById(R.id.prevText);
			final TextView mVoltsText = (TextView) findViewById(R.id.mVoltsText);
			
			if (editText.getText().length() == 0) {
				Toast.makeText(getApplicationContext(), "Input can't be empty",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if ((Float.valueOf(editText.getText().toString()) < 0)
					|| (Float.valueOf(editText.getText().toString()) > 180)) {
				Toast.makeText(getApplicationContext(),
						"Unaccepted value (0-180)", Toast.LENGTH_SHORT).show();
				return;
			}
			
			
			if (lastMeasurement == 0.00) {
				prevText.setText("-");
				
			} else {
				lastMeasurement=Float.valueOf(mVoltsText.getText()
						.toString().substring(0,mVoltsText.getText()
						.toString().indexOf("Volts")));
				String result = String.format("%.2f",lastMeasurement);
				prevText.setText(result);
			}
			
			if(duration==-1)
				duration=2500;
			// Animation start
			float Xaxis = 0.835366f;
			float Yaxis = 0.676692f;
			RotateAnimation rotateAnimation1 = new RotateAnimation(0,
					Float.valueOf(editText.getText().toString()),
					Animation.RELATIVE_TO_SELF, Xaxis,
					Animation.RELATIVE_TO_SELF, Yaxis);
			rotateAnimation1.setInterpolator(new LinearInterpolator());
			rotateAnimation1.setDuration(duration);
			rotateAnimation1.setRepeatCount(0);
			rotateAnimation1.setAnimationListener(this);
			image.startAnimation(rotateAnimation1);
			String result = String.format("%.2f",Float.parseFloat(editText.getText().toString()));
			mVoltsText.setText(result+"Volts");
			lastMeasurement=Float.valueOf(mVoltsText.getText()
					.toString().substring(0,mVoltsText.getText()
					.toString().indexOf("Volts")));

		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		animation.setFillAfter(true);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAnimationStart(Animation animation) {

	}
}
