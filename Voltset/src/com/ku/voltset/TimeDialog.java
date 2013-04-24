package com.ku.voltset;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class TimeDialog extends DialogFragment {
	private View mRoot;
	private static final String file = "VoltSet.csv"; // Our log file
	//Android sdk: new instance not constructor
	//so as to get the bundled passed from startup
	static TimeDialog newInstance(/*Bundle data*/) {
		TimeDialog f = new TimeDialog();
		//f.setArguments(data);
		return f;
	}
	//not implemented
	private String getFirstLastTimeDate(){
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
		}
		return "";
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//actually parse the bundle arguments
	}

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
