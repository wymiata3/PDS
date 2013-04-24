package com.ku.voltset;

import com.ku.voltset.R;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author chmod
 *
 * Fragment responsible for showing text measurements
 * 
 */
/**
 * @author chmod
 *
 */
public class DIYFragment extends Fragment {
	private View mRoot;
	private TextView txtMeasurement;
	private TextView txtHolded;
	private TextView txtDC;
	private TextView txtAC;
	int num = 1;

	public DIYFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//get the layout
		mRoot = inflater.inflate(R.layout.fragment_diy, container, false);
		//initialize data
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
		//end of initializ data
		return mRoot;
	}

	//Fade out animation
	public static Animation runFadeOutAnimationOn(Context ctx, View target) {
		Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.fadeout);
		target.startAnimation(animation);
		return animation;
	}

	//Fade in animation
	public static Animation runFadeInAnimationOn(Context ctx, View target) {
		Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.fedein);
		target.startAnimation(animation);
		return animation;
	}

	/**
	 * Sets the measurement in the text view
	 * @param measurement value to be displayed
	 */
	public void updateMeasureText(String measurement) {
		txtMeasurement.setText(measurement);
	}

	/**
	 * Sets the holded in the text view
	 * along with gradient animation
	 * @param holded value to be displayed
	 */
	public void updateHolded(String holded) {
		txtHolded.setText("Holded:" + holded + "V");
		//do a animation from red to blue gradiently
		Integer colorFrom = Color.RED;
		Integer colorTo = Color.BLUE;
		//register and init listener
		ValueAnimator colorAnimation = ValueAnimator.ofObject(
				new ArgbEvaluator(), colorFrom, colorTo);
		colorAnimation.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				txtHolded.setTextColor((Integer) animation.getAnimatedValue());

			}
		});
		//animation duration
		colorAnimation.setDuration(5000);
		//play the animation
		colorAnimation.start();
	}

	/**
	 * Changes color at DC textview
	 * @param color color to be for DC
	 */
	public void setColorDC(int color) {
		txtDC.setTextColor(color);
	}

	/**
	 * Changes color at AC textview
	 * @param color color to be for AC
	 */
	public void setColorAC(int color) {
		txtAC.setTextColor(color);
	}
}
