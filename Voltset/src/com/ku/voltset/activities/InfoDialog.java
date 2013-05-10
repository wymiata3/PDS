package com.ku.voltset.activities;

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
import android.widget.TextView;

/**
 * Shows information dialog about yocto-device
 * @author chmod
 *
 */
public class InfoDialog extends DialogFragment {
	private View mRoot;
	String yocto_serial = null;
	String Luminosity = null;
	String UpTime = null;
	String UsbCurrent = null;
	String Beacon = null;

	//
	//so as to get the bundled passed from startup
	/**
	 * Android sdk: new instance not constructor
	 * so as to get the bundled passed from startup
	 * @param data Bundle contains keys: serial, Luminosity, UpTime, UsbCurrent, Beacon 
	 * @return An instance of InfoDialog
	 */
	static InfoDialog newInstance(Bundle data) {
		InfoDialog f = new InfoDialog();
		f.setArguments(data);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//actually parse the bundle arguments
		Bundle arguments = getArguments();
		this.yocto_serial = arguments.getString("serial");
		this.Luminosity = arguments.getString("Luminosity");
		this.UpTime = arguments.getString("UpTime");
		this.UsbCurrent = arguments.getString("UsbCurrent");
		this.Beacon = arguments.getString("Beacon");
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//display them in a dialogbox
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//parse the layout
		LayoutInflater inflater = getActivity().getLayoutInflater();
		mRoot = inflater.inflate(R.layout.infodialog, null);
		// update widget values
		TextView txtView = (TextView) mRoot.findViewById(R.id.txtSerialV);
		txtView.setText(yocto_serial);
		TextView txtView2 = (TextView) mRoot.findViewById(R.id.txtLumonosityV);
		txtView2.setText(Luminosity);
		TextView txtView3 = (TextView) mRoot.findViewById(R.id.txtUpTimeV);
		txtView3.setText(UpTime);
		TextView txtView4 = (TextView) mRoot.findViewById(R.id.txtUsbCurrentV);
		txtView4.setText(UsbCurrent);
		TextView beacon = (TextView) mRoot.findViewById(R.id.txtBeaconV);
		beacon.setText(Beacon);
		//end update
		
		//build the dialog butons - cancel
		builder.setView(mRoot).setNegativeButton(R.string.dialog_close,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						InfoDialog.this.getDialog().cancel();
					}
				});

		return builder.create();
	}

}
