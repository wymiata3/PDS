package com.ku.voltset;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

public class DateDialog extends DialogFragment {

		private View mRoot;
		private static final String file = "VoltSet.csv"; // Our log file
		private DatePicker tmf; //time picker from
		private DatePicker tmt; //time picker to
		//Android sdk: new instance not constructor
		//so as to get the bundled passed from startup
		static DateDialog newInstance(/*Bundle data*/) {
			DateDialog f = new DateDialog();
			//f.setArguments(data);
			return f;
		}
		

		  
// we might need
		    public void onDateSet(DatePicker view, int year, int month, int day) {
		        // Do something with the date chosen by the user
		    }
		    
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			//actually parse the bundle arguments
		}
		
		/**
		 * Sets up a dialog for time interval selection.
		 * 
		 * @param savedInstanceState the state of the instance.
		 * @return Dialog the created dialog 
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
	        
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			LayoutInflater inflater = getActivity().getLayoutInflater();
			mRoot = inflater.inflate(R.layout.date_layout, null);
			builder.setView(mRoot).setNegativeButton(R.string.dialog_close,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							DateDialog.this.getDialog().cancel();
						}
					});

			
			return builder.create();
		}

	}
