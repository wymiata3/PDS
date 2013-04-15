package com.ku.voltset;

import com.ku.voltset.R;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class DIYFragment extends Fragment {
	private View mRoot;
	private TextView txtMeasurement;
	private TextView txtHolded;
	private TextView txtDC;
	private TextView txtAC;
	int num=1;
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
		txtAC=(TextView)mRoot.findViewById(R.id.txtAC);
		txtDC=(TextView)mRoot.findViewById(R.id.txtDC);
		return mRoot;
	}

	public void updateMeasureText(String measurement) {

		txtMeasurement.setText(measurement);
	}

	public void updateHolded(String holded) {
		txtHolded.setText("Holded:" + holded + "V");
	}

	public void setColorDC(int color) {
		txtDC.setTextColor(color);
	}
	public void setColorAC(int color) {
		txtAC.setTextColor(color);
	}
}
