package com.ku.voltset;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class InfoDialog extends DialogFragment {
	private String serial;
	private View mRoot;
	public InfoDialog(){
		
		Bundle arguments=getArguments();
		this.serial=arguments.getString("serial");
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		mRoot = inflater.inflate(R.layout.infodialog, null);
		TextView serialView=(TextView)mRoot.findViewById(R.id.textView2);
		serialView.setText(serial);
		builder.setView(mRoot)
				.setNegativeButton(R.string.dialog_close,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								InfoDialog.this.getDialog().cancel();
							}
						});
		
		return builder.create();
	}

}
