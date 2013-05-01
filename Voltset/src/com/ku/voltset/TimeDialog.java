package com.ku.voltset;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
/**
 * creates a Dialog for selecting a time range.
 *
 */
public class TimeDialog extends DialogFragment {
	private View mRoot;
	private static final String file = "VoltSet.csv"; // Our log file
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
	 * return the hours given in the time interval
	 * 
	 * @return String a string in the format of hourfrom--minutefrom--hourto--minuteto
	 */
	public String gethours(){
		
		String hours;
		int hourfrom;
		int minutefrom;
		int hourto;
		int minuteto;
		
		tmf = (TimePicker) mRoot.findViewById(R.id.tmpickerFrom);
		tmt = (TimePicker) mRoot.findViewById(R.id.tmpickerTo);
		hourfrom = tmf.getCurrentHour();
		minutefrom = tmf.getCurrentMinute();
		hourto = tmt.getCurrentHour();
		minuteto = tmt.getCurrentMinute();
		hours =hourfrom + "--" + minutefrom + "--" + hourto +"--" + minuteto;
		return hours;
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
	
	
	/** 
	 * not working....
	 */
	private void setCurrentTimeOnView() {
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		mRoot = inflater.inflate(R.layout.time_layout, null);
		TimePicker timePicker1 = (TimePicker) mRoot.findViewById(R.id.tmpickerFrom);
		TimePicker timePicker2 = (TimePicker) mRoot.findViewById(R.id.tmpickerTo);
		int hour;
		int minute;
		
		final Calendar c = Calendar.getInstance();
		hour = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);
 
		// set current time into timepicker
		timePicker1.setCurrentHour(0);
		timePicker1.setCurrentMinute(0);
		timePicker2.setCurrentHour(0);
		timePicker2.setCurrentMinute(0);
		Toast.makeText(getActivity(), "smthing", 4000).show();
	}

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
