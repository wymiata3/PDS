package com.ku.voltset;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.ku.voltset.R;
import com.jjoe64.graphview.GraphView.GraphViewData;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;

public class ProFragment extends Fragment implements OnClickListener,OnItemSelectedListener {
	Button btnShow;
	View mRoot;
	RelativeLayout graphLayout;
	Spinner chooser;
	String userChoice="all";
	private static final String file = "VoltSet.csv"; // Our log file

	public ProFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRoot= inflater.inflate(R.layout.fragment_pros, container,
				false);
		btnShow=(Button)mRoot.findViewById(R.id.btnShow);
		btnShow.setOnClickListener(this);
		graphLayout= (RelativeLayout) mRoot.findViewById(R.id.graphLayout);
		chooser=(Spinner)mRoot.findViewById(R.id.spinner1);
		chooser.setOnItemSelectedListener(this);
		return mRoot;
	}

	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.btnShow)
		{
			graphLayout.removeAllViews();
			int line=0;
			try {
				//Generic ArrayList to Store only String objects
				ArrayList<GraphViewData> graphHolder= new ArrayList<GraphViewData>(); 
				GraphViewData graphData;
				Logger log = new Logger(this.getActivity());
				log.setFile(file);
				FileInputStream fstream = new FileInputStream(log.getFile());
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				// Read File Line By Line
				while ((strLine = br.readLine()) != null) {
					String measurement=strLine.split("\\|")[1];
					measurement=measurement.substring(measurement.indexOf(":")+1, measurement.length()-2);
					graphData=new GraphViewData(line++, Double.valueOf(measurement));
					graphHolder.add(graphData);
				}
				// Close the input stream
				in.close();
				GraphViewData[] data=new GraphViewData[graphHolder.size()];
				for(int i=0;i<graphHolder.size();i++)
					data[i]=graphHolder.get(i);
				GraphViewSeries wholeLog=new GraphViewSeries(data);
				GraphView graphView = new LineGraphView(this.getActivity(),"Whole Data Log");
				graphView.setViewPort(20, 150);  
				graphView.setScrollable(true);  
				// activate scaling / zooming  
				graphView.setScalable(true);  
				graphView.addSeries(wholeLog);
				graphLayout.addView(graphView);
			} catch (IOException io) {
				io.printStackTrace();
			}
			
		}
		
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
			long id) {
		switch (pos){
		case 0:
			userChoice="all";
			break;
		case 2:
			TimeDialog td=TimeDialog.newInstance();
			td.show(getFragmentManager(), "time");
			userChoice="time";
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}
