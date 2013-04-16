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
		mRoot = inflater.inflate(R.layout.fragment_diy, container, false);
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
		return mRoot;
	}

	public static Animation runFadeOutAnimationOn(Context ctx, View target) {
		Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.fadeout);
		target.startAnimation(animation);
		return animation;
	}

	public static Animation runFadeInAnimationOn(Context ctx, View target) {
		Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.fedein);
		target.startAnimation(animation);
		return animation;
	}

	public void updateMeasureText(String measurement) {

		txtMeasurement.setText(measurement);
	}

	public void updateHolded(String holded) {
		txtHolded.setText("Holded:" + holded + "V");
		//do a animation from red to blue gradiently
		Integer colorFrom = Color.RED;
		Integer colorTo = Color.BLUE;
		ValueAnimator colorAnimation = ValueAnimator.ofObject(
				new ArgbEvaluator(), colorFrom, colorTo);
		colorAnimation.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				txtHolded.setTextColor((Integer) animation.getAnimatedValue());

			}
		});
		colorAnimation.setDuration(5000);
		colorAnimation.start();
	}

	public void setColorDC(int color) {
		txtDC.setTextColor(color);
	}

	public void setColorAC(int color) {
		txtAC.setTextColor(color);
	}
}
