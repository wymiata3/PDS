package com.ku.voltset.fragments;

import com.ku.voltset.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Fragment responsible for showing Edu tab tab is for educational purposes with
 * an animated image arrow
 * 
 * @author chmod
 * 
 */
public class EduFragment extends Fragment {
	ImageView image; // Arrow image
	int duration = 2500; // Animation duration not so fast
	private final float Xaxis = 0.835366f; // Image Centers
	private final float Yaxis = 0.676692f; // Image Centers
	RotateAnimation rotate = null; // Rotate animation
	TextView mVoltsText; // Current measurement
	TextView prevText;// Previous measurement
	View mRoot;
	String prevDC = "0";

	/**
	 * Default empty constructor
	 */
	public EduFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Show fragment_edu layout
		mRoot = inflater.inflate(R.layout.fragment_edu, container, false);
		// Parse image
		image = (ImageView) mRoot.findViewById(R.id.imageArrow);
		// Parse text text with current measurement
		mVoltsText = (TextView) mRoot.findViewById(R.id.mVoltsText);
		// Parse text with previous measurement
		prevText = (TextView) mRoot.findViewById(R.id.prevText);
		return mRoot;
	}

	/**
	 * Updates UI. Sets current value of dc sensor.
	 * 
	 * @param measurement
	 *            The value of measurement to be shown.
	 */
	public void updateMeasureText(String measurement) {
		mVoltsText.setText(measurement);
	}

	/**
	 * Updates UI. Sets the value of holded.
	 * 
	 * @param holded
	 *            The value to be shown.
	 */
	public void updateHolded(String holded) {
		prevText.setText("Holded:" + holded + "V");
	}

	/**
	 * Listener responsible for preventing arrow from resetting after animation
	 * end.
	 * 
	 */
	static AnimationListener animationListener = new AnimationListener() {
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

	/**
	 * Method to make arrow animate up to holded value
	 * 
	 * @param holded
	 *            Holds the value in a climate of max 5*36
	 */
	public void rotateArrowUpwards(String holded) {
		// Dont rotate if same value
		if (prevDC.equalsIgnoreCase(holded))
			rotate = null;
		if (rotate == null) {
			// Climax is 5(max) * 36
			rotate = new RotateAnimation(0, Float.valueOf(holded) * 36,
					Animation.RELATIVE_TO_SELF, Xaxis,
					Animation.RELATIVE_TO_SELF, Yaxis);
			rotate.setInterpolator(new LinearInterpolator());
			rotate.setDuration(duration);
			rotate.setRepeatCount(0);
			rotate.setAnimationListener(animationListener);
			image.startAnimation(rotate);
		}
		prevDC = holded;

	}

	// Not implemented yet
	public void rotateArrowBackwards(String currentMeasurement) {
		// rotate = new RotateAnimation(
		// Float.valueOf(currentMeasurement) * 36, 0,
		// Animation.RELATIVE_TO_SELF, Xaxis,
		// Animation.RELATIVE_TO_SELF, Yaxis);
		// rotate.setInterpolator(new LinearInterpolator());
		// rotate.setDuration(duration);
		// rotate.setRepeatCount(0);
		// rotate.setAnimationListener(animationListener);
		// image.startAnimation(rotate);
	}
}
