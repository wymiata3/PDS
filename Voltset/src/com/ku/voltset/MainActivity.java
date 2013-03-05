package com.ku.voltset;

import com.example.helloworld.R;

import android.os.Bundle;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener,AnimationListener {
	private double lastMeasurement = 0.000;
	private ImageView image;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		image = (ImageView) findViewById(R.id.imageArrow);
		Button simButton = (Button) findViewById(R.id.simButton);
		simButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {

		if (view.getId() == R.id.simButton) {
			
			EditText editText = (EditText) findViewById(R.id.simText);
			TextView prevText = (TextView) findViewById(R.id.prevText);
			TextView mVoltsText = (TextView) findViewById(R.id.mVoltsText);
			if(editText.getText().length()==0){
				Toast.makeText(getApplicationContext(), "Input can't be empty", Toast.LENGTH_SHORT).show();
				return;
			}
			if((Float.valueOf(editText.getText().toString())<0) || (Float.valueOf(editText.getText().toString())>180))
			{
				Toast.makeText(getApplicationContext(), "Unaccepted value (0-180)", Toast.LENGTH_SHORT).show();
				return;
			}
			if (lastMeasurement == 0.000) {
				lastMeasurement = Double.valueOf(editText.getText().toString());
			}
			else
				lastMeasurement = Double.valueOf(mVoltsText.getText().toString());
			prevText.setText(String.valueOf(lastMeasurement));
			mVoltsText.setText(editText.getText().toString());
			//Animation start
			float Xaxis=0.835366f;
			float Yaxis=0.676692f;
			RotateAnimation rotateAnimation1 = new RotateAnimation(0, Float.valueOf(editText.getText().toString()),
					Animation.RELATIVE_TO_SELF, Xaxis,
					Animation.RELATIVE_TO_SELF, Yaxis);
			rotateAnimation1.setInterpolator(new LinearInterpolator());
			rotateAnimation1.setDuration(2500);
			rotateAnimation1.setRepeatCount(0);
			
			rotateAnimation1.setAnimationListener(this);
			image.clearAnimation();
			image.invalidate();
			rotateAnimation1.cancel();
			rotateAnimation1.reset();
			image.startAnimation(rotateAnimation1);
			    
			
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
