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

/**
 * Pro fragment, the second tab, showing graphs
 * @author chmod
 *
 */
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
		//get the layout
		mRoot= inflater.inflate(R.layout.fragment_pros, container,
				false);
		//init widgets
		btnShow=(Button)mRoot.findViewById(R.id.btnShow);
		btnShow.setOnClickListener(this);
		graphLayout= (RelativeLayout) mRoot.findViewById(R.id.graphLayout);
		//time/date/custom widget
		chooser=(Spinner)mRoot.findViewById(R.id.spinner1);
		//add listener
		chooser.setOnItemSelectedListener(this);
		return mRoot;
	}

	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.btnShow)
		{
			//be sure nothing is displayed
			graphLayout.removeAllViews();
			int line=0;
			try {
				//Generic ArrayList to Store only String objects
				ArrayList<GraphViewData> graphHolder= new ArrayList<GraphViewData>();
				//object of which data will be displayed
				GraphViewData graphData;
				//get the log file
				Logger log = new Logger(this.getActivity());
				log.setFile(file);
				FileInputStream fstream = new FileInputStream(log.getFile());
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				// Read File Line By Line
				while ((strLine = br.readLine()) != null) {
					//Split by | and keep only right part
					String measurement=strLine.split("\\|")[1];
					//we only need the values, discard rest
					measurement=measurement.substring(measurement.indexOf(":")+1, measurement.length()-2);
					//and save them in the graph data
					graphData=new GraphViewData(line++, Double.valueOf(measurement));
					graphHolder.add(graphData);
				}
				// Close the input stream
				in.close();
				//recreate array as required by api
				GraphViewData[] data=new GraphViewData[graphHolder.size()];
				for(int i=0;i<graphHolder.size();i++)
					data[i]=graphHolder.get(i);
				GraphViewSeries wholeLog=new GraphViewSeries(data);
				//end of recreate
				//build the graph
				GraphView graphView = new LineGraphView(this.getActivity(),"Whole Data Log");
				//no need to see all, just a portion
				graphView.setViewPort(20, 150);  
				//make it scrollable
				graphView.setScrollable(true);  
				// activate scaling / zooming  
				graphView.setScalable(true);  
				graphView.addSeries(wholeLog);
				//add it to layout
				graphLayout.addView(graphView);
			} catch (IOException io) {
				io.printStackTrace();
			}
			
		}
		
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
			long id) {
		//which item was selected from the spinner
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
		// do nothing, just close
		
	}

}
