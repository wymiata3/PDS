package com.ku.voltset.fragments;


import com.ku.voltset.R;
import com.ku.voltset.R.id;
import com.ku.voltset.R.layout;
import com.ku.voltset.R.string;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;
/**
 * creates a Dialog for selecting a time range.
 *
 */
public class TimeDialog extends DialogFragment {
	private View mRoot;
	private TimePicker tmf; //time picker from
	private TimePicker tmt; //time picker to
	//Android sdk: new instance not constructor
	//so as to get the bundled passed from startup
	static TimeDialog newInstance(/*Bundle data*/) {
		TimeDialog f = new TimeDialog();
		//f.setArguments(data);
		return f;
	}
	
	
	/**
	 * return the hours given in the time interval (from)
	 * 
	 * @return Integer
	 */
	public int gethourfrom(){
		tmf = (TimePicker) mRoot.findViewById(R.id.tmpickerFrom);
		return tmf.getCurrentHour();
	}
	
	/**
	 * return the minute given in the time interval (from)
	 * 
	 * @return Integer
	 */
	public int getminutefrom(){
		tmf = (TimePicker) mRoot.findViewById(R.id.tmpickerFrom);
		return tmf.getCurrentMinute();
	}
	
	/**
	 * return the hours given in the time interval (To)
	 * 
	 * @return Integer
	 */
	public int gethourto(){
		tmt = (TimePicker) mRoot.findViewById(R.id.tmpickerTo);
		return tmt.getCurrentHour();
	}
	
	/**
	 * return the minute given in the time interval (To)
	 * 
	 * @return Integer
	 */
	public int getminuteto(){
		tmt = (TimePicker) mRoot.findViewById(R.id.tmpickerTo);
		return tmt.getCurrentMinute();
	}
	/**
	 * not implemented
	 */
	public String getFirstLastTimeDate(){
		
		/*
		try {
			Logger log = new Logger(this.getActivity().getApplicationContext());
			log.setFile(file);
			FileInputStream fstream = new FileInputStream(log.getFile());
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				//logText.append(strLine + "\n");
			}

			// Close the input stream
			in.close();
		} catch (IOException io) {
			io.printStackTrace();
		}*/
		return "";
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//actually parse the bundle arguments
	}
	
	
//	/** 
//	 * not working....
//	 */
//	private void setCurrentTimeOnView() {
//		
//		LayoutInflater inflater = getActivity().getLayoutInflater();
//		mRoot = inflater.inflate(R.layout.time_layout, null);
//		TimePicker timePicker1 = (TimePicker) mRoot.findViewById(R.id.tmpickerFrom);
//		TimePicker timePicker2 = (TimePicker) mRoot.findViewById(R.id.tmpickerTo);
//		
//		final Calendar c = Calendar.getInstance();
////		int hour = c.get(Calendar.HOUR_OF_DAY);
////		int minute = c.get(Calendar.MINUTE);
// 
//		// set current time into timepicker
//		timePicker1.setCurrentHour(0);
//		timePicker1.setCurrentMinute(0);
//		timePicker2.setCurrentHour(0);
//		timePicker2.setCurrentMinute(0);
//		Toast.makeText(getActivity(), "smthing",).show();
//	}

	/**
	 * Sets up a dialog for time interval selection.
	 * 
	 * @param savedInstanceState the state of the instance.
	 * @return Dialog the created dialog 
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//display them in a dialogbox
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		mRoot = inflater.inflate(R.layout.time_layout, null);
		builder.setView(mRoot).setNegativeButton(R.string.dialog_close,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						TimeDialog.this.getDialog().cancel();
					}
				});

		
		return builder.create();
	}

}
