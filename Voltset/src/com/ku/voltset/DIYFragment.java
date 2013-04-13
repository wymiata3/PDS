package com.ku.voltset;

import com.ku.voltset.R;

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
	private TextView txtView;
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
        mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator("TAB1").setContent(R.id.info));
        mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator("TAB2").setContent(R.id.data));
        mTabHost.addTab(mTabHost.newTabSpec("tab_test3").setIndicator("TAB3").setContent(R.id.guide));

        mTabHost.setCurrentTab(0);
		return mRoot;
	}
	public void updateMeasureText(String measurement)
	{
		txtView=(TextView)mRoot.findViewById(R.id.txtMeasurement);
		txtView.setText(measurement);
	}
	public void updateHolded(String holded){
		txtView=(TextView)mRoot.findViewById(R.id.txtHold);
		txtView.setText("Holded:"+holded+"V");
	}
	
}
