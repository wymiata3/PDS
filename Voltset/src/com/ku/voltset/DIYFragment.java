package com.ku.voltset;

import com.ku.voltset.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

public class DIYFragment extends Fragment {
	private View mRoot;

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
        TextView tv1=(TextView)mRoot.findViewById(R.id.txtHold);
        tv1.setText("Holded:1.534V");
		return mRoot;
	}
}
