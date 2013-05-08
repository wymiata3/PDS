package com.ku.voltset;

import com.ku.voltset.R;
import com.ku.voltset.interfaces.VoiceOnOff;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * @author chmod
 * 
 *         Fragment responsible for showing text measurements
 * 
 */
public class DIYFragment extends Fragment {
	private View mRoot;
	private TextView txtMeasurement;
	private TextView txtHolded;
	private TextView txtDC;
	private TextView txtAC;
	int num = 1;
	onVoiceOnOff mCallback;
	private ToggleButton tbtnVoice;
	public DIYFragment() {

	}
	public interface onVoiceOnOff {
        public void onVoiceOn();
        public void onVoiceOff();
    }
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (onVoiceOnOff) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// get the layout
		mRoot = inflater.inflate(R.layout.fragment_diy, container, false);
		// initialize data
		TabHost mTabHost = (TabHost) mRoot.findViewById(android.R.id.tabhost);
		mTabHost.setup();
		mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator("TAB1")
				.setContent(R.id.info));
		mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator("TAB2")
				.setContent(R.id.data));
		mTabHost.addTab(mTabHost.newTabSpec("tab_test3").setIndicator("TAB3")
				.setContent(R.id.guide));
		mTabHost.setCurrentTab(0);
		txtMeasurement = (TextView) mRoot.findViewById(R.id.txtMeasurement);
		txtHolded = (TextView) mRoot.findViewById(R.id.txtHold);
		txtAC = (TextView) mRoot.findViewById(R.id.txtAC);
		txtDC = (TextView) mRoot.findViewById(R.id.txtDC);
		tbtnVoice=(ToggleButton)mRoot.findViewById(R.id.tbtnVoice);
		tbtnVoice.setOnCheckedChangeListener( new OnCheckedChangeListener() {
	        @Override
	        public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
	           if(isChecked)
	        	   mCallback.onVoiceOn();
	           else
	        	   mCallback.onVoiceOff();
	        }
	    }) ;
		// end of initializ data
		return mRoot;
	}

	// Fade out animation
	public static Animation runFadeOutAnimationOn(Context ctx, View target) {
		Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.fadeout);
		target.startAnimation(animation);
		return animation;
	}

	// Fade in animation
	public static Animation runFadeInAnimationOn(Context ctx, View target) {
		Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.fedein);
		target.startAnimation(animation);
		return animation;
	}

	/**
	 * Sets the measurement in the text view
	 * 
	 * @param measurement
	 *            value to be displayed
	 */
	public void updateMeasureText(String measurement) {
		txtMeasurement.setText(measurement);
	}

	/**
	 * Sets the holded in the text view along with gradient animation
	 * 
	 * @param holded
	 *            value to be displayed
	 */
	public void updateHolded(String holded) {
		txtHolded.setText("Holded:" + holded + "V");
		// do a animation from red to blue gradiently
		Integer colorFrom = Color.RED;
		Integer colorTo = Color.BLUE;
		// register and init listener
		ValueAnimator colorAnimation = ValueAnimator.ofObject(
				new ArgbEvaluator(), colorFrom, colorTo);
		colorAnimation.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				txtHolded.setTextColor((Integer) animation.getAnimatedValue());

			}
		});
		// animation duration
		colorAnimation.setDuration(5000);
		// play the animation
		colorAnimation.start();
	}

	/**
	 * Changes color at DC textview
	 * 
	 * @param color
	 *            color to be for DC
	 */
	public void setColorDC(int color) {
		txtDC.setTextColor(color);
	}

	/**
	 * Changes color at AC textview
	 * 
	 * @param color
	 *            color to be for AC
	 */
	public void setColorAC(int color) {
		txtAC.setTextColor(color);
	}
}
