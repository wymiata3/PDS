//package com.ku.voltset;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.app.DialogFragment;
//import android.content.DialogInterface;
//import android.os.Bundle;
//
//public class DialogRefresh extends DialogFragment{
//	@Override
//	public Dialog onCreateDialog(Bundle savedInstanceState){
//		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
//		builder.setMessage(R.string.dialog_rescan).setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
//			
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				HardwareController hc=new HardwareController();				
//			}
//		});
//		return builder.create();
//	}
//
//}
