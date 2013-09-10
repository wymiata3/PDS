package com.ku.voltset.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

import com.ku.voltset.Logger;
import com.ku.voltset.R;
import com.ku.voltset.services.HardwareController_service;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReportActivity extends Activity implements OnClickListener {
	private Messenger mService = null;
	private boolean mIsBound = false;
	private final Context context = this;
	private static final String TAG = "ReportActivity";
	private final Messenger mMessenger = new Messenger(new IncomingHandler());
	private Handler fiveSecHandler = new Handler();
	private Logger log;
	private int secs;
	private TextView currentTextView;
	private boolean locked = false;
	private TextView m1;
	private TextView m2;
	private TextView m3;
	private ArrayList<String> savedData = new ArrayList<String>();
	private int nextTextView = 0;
	private int reportNumber = 1;
	private String owner = "";
	private TextView voltsTextView;
	private Button viewReport;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report);
		Button start = (Button) findViewById(R.id.btnStart);
		start.setOnClickListener(this);
		Button generate = (Button) findViewById(R.id.btnGenerate);
		generate.setOnClickListener(this);
		m1 = (TextView) findViewById(R.id.txtMeasurement1);
		m2 = (TextView) findViewById(R.id.txtMeasurement2);
		m3 = (TextView) findViewById(R.id.txtMeasurement3);
		m2.setOnClickListener(this);
		m3.setOnClickListener(this);
		voltsTextView = (TextView) findViewById(R.id.txtVolts);
		viewReport=(Button)findViewById(R.id.btnView);
		viewReport.setOnClickListener(this);
		doBindService();
	}

	@Override
	protected void onDestroy() {
		try {
			// unbind the service, view is destroyed
			doUnbindService();
		} catch (Throwable t) {
			Log.e(TAG, "Failed to unbind from the service", t);
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.report, menu);
		return true;
	}

	ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mService = new Messenger(service);
			Log.d(TAG, "Attached.");

			// We want to monitor the service for as long as we are
			// connected to it.
			try {
				Message msg = Message.obtain(null,
						HardwareController_service.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even
				// do anything with it; we can count on soon being
				// disconnected (and then reconnected if it can be restarted)
				// so there is no need to do anything here.
			}

			// As part of the sample, tell the user what happened.
			Log.d(TAG, "Connected.");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.ServiceConnection#onServiceDisconnected(android.content
		 * .ComponentName)
		 */
		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;
			Log.d(TAG, "Disconnected.");

		}
	};

	/**
	 * Establish a connection with the service. We use an explicit class name
	 * because there is no reason to be able to let other applications replace
	 * our component. Run in another thread
	 */
	void doBindService() {
		Thread t = new Thread() {
			public void run() {
				getApplicationContext().bindService(
						new Intent(ReportActivity.this,
								HardwareController_service.class), mConnection,
						Context.BIND_AUTO_CREATE);
			}
		};
		mIsBound = true;
		t.start();
		Log.d(TAG, "Binding.");
	}

	/**
	 * Unbind service from activity Called at onDestoy()
	 */
	void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							HardwareController_service.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service
					// has crashed.
				}
			}
			// Detach our existing connection.
			getApplicationContext().unbindService(mConnection);
			mIsBound = false;
			Log.d(TAG, "MainActivity - unbinding.");
		}
	}

	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HardwareController_service.MSG_DISCONNECT:
				// PANIC
				// try to unbind
				doUnbindService();
				// Device is unplugged
				// inform user
				Toast.makeText(context, "Error! Device unplugged",
						Toast.LENGTH_LONG).show();
				// switch to startup activity
				Intent startupActivity = new Intent(context,
						StartupActivity.class);
				// reoder to front flag
				startupActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(startupActivity);
				break;
			case HardwareController_service.MSG_MEASUREMENT:
				try {
					String voltage = "";// immutable
					voltage = msg.getData().getString("dc");
					if(Float.parseFloat(voltage)<0){
						Toast.makeText(getApplicationContext(), "Reverse cables", Toast.LENGTH_SHORT).show();
						return;
					}
					voltsTextView.setText(voltage+"V");
				} catch (Exception e) {
					Log.d(TAG, "Exception", e);
				}

				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private void newReport() {
		m1.setText("");
		m2.setText("");
		m3.setText("");
		m1.setBackgroundColor(getResources().getColor(R.color.bckgrndColor));
		m2.setBackgroundColor(getResources().getColor(R.color.bckgrndColor));
		m3.setBackgroundColor(getResources().getColor(R.color.bckgrndColor));
		nextTextView = 0;
		locked = false;
	}

	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.btnView)
		{
			try {
		        Intent i = new Intent(Intent.ACTION_VIEW);
		        Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory()+ "/iamsofuckingawesome/report" + (reportNumber-1)+".xls")); 
		        String url = uri.toString();

		        String newMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
		                MimeTypeMap.getFileExtensionFromUrl(url));

		        i.setDataAndType(uri, newMimeType);
		        startActivity(i);
		    } catch (Exception e) {
		        Toast.makeText(getApplicationContext(), "No application was found able to open xls files", Toast.LENGTH_LONG).show();
		    }
		}
		if (v.getId() == R.id.btnStart) {
			TextView temp = (TextView) findViewById(R.id.txtMeasurement1);
			ColorDrawable drawable = (ColorDrawable) temp.getBackground();
			if (!(locked == true) && (drawable.getColor() != Color.GREEN)
					&& nextTextView == 0) {
				setMeasurementView(temp);
				getMeasurementView().setBackgroundColor(new Color().RED);
				fiveSecHandler.postDelayed(countdown, 0);
				secs = 5000;
				Toast.makeText(getApplicationContext(),
						String.valueOf(reportNumber), 1000).show();
			}
		}
		if (v.getId() == R.id.txtMeasurement2) {
			TextView temp = (TextView) findViewById(R.id.txtMeasurement2);
			ColorDrawable drawable = (ColorDrawable) temp.getBackground();
			if (!(locked == true) && (drawable.getColor() != Color.GREEN)
					&& nextTextView == 1) {
				setMeasurementView(temp);
				getMeasurementView().setBackgroundColor(new Color().RED);
				fiveSecHandler.postDelayed(countdown, 0);
				secs = 5000;
			}
		}
		if (v.getId() == R.id.txtMeasurement3) {
			TextView temp = (TextView) findViewById(R.id.txtMeasurement3);
			ColorDrawable drawable = (ColorDrawable) temp.getBackground();
			if (!(locked == true) && (drawable.getColor() != Color.GREEN)
					&& nextTextView == 2) {
				setMeasurementView(temp);
				getMeasurementView().setBackgroundColor(new Color().RED);
				fiveSecHandler.postDelayed(countdown, 0);
				secs = 5000;
			}
		}
		if (v.getId() == R.id.btnGenerate) {
			boolean nonEmptyFields = ValidateAllTextViews((RelativeLayout) findViewById(R.id.textsLayout));
			if (!nonEmptyFields) {
				Toast.makeText(
						context,
						"You must complete all steps before generating the report",
						1000).show();
			} else {

				AssetManager assetManager = getAssets();
				InputStream stream = null;
				try {
					stream = assetManager.open("template.xls");
					try {
						FileInputStream signatureStream = openFileInput("Signature.png");
						ExcelExporter xport = new ExcelExporter(
								reportNumber,
								getAllGreenTexts((RelativeLayout) findViewById(R.id.textsLayout)),
								owner, stream, signatureStream);
					} catch (FileNotFoundException fnfe) {
						ExcelExporter xport = new ExcelExporter(
								reportNumber,
								getAllGreenTexts((RelativeLayout) findViewById(R.id.textsLayout)),
								owner, stream);

					}
					reportNumber++;
					newReport();
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(),
							"Template was not found", Toast.LENGTH_SHORT)
							.show();
					return;
				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException e) {
						}
					}
				}
			}
		}
	}


	public boolean fileExists(String fname) {
		File file = getBaseContext().getFileStreamPath(fname);
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	private ArrayList<String> getAllGreenTexts(RelativeLayout parent) {
		ArrayList<String> temp = new ArrayList<String>();
		int childs = parent.getChildCount();
		for (int i = 0; i < childs; i++) {
			Object v = parent.getChildAt(i);
			if (v instanceof TextView) {

				TextView textObject = (TextView) v;
				ColorDrawable cd = (ColorDrawable) textObject.getBackground();
				if (cd.getColor() == Color.GREEN)
					temp.add(textObject
							.getText()
							.toString()
							.substring(
									0,
									textObject.getText().toString().length() - 1));
			}
		}
		return temp;
	}

	private boolean ValidateAllTextViews(RelativeLayout parent) {
		int childs = parent.getChildCount();
		for (int i = 0; i < childs; i++) {
			Object v = parent.getChildAt(i);
			if (v instanceof TextView) {

				TextView textObject = (TextView) v;
				ColorDrawable cd = (ColorDrawable) textObject.getBackground();
				if (cd.getColor() != Color.GREEN)
					return false;
			}
		}
		return true;
	}

	private void setMeasurementView(TextView txtView) {
		currentTextView = txtView;
	}

	private TextView getMeasurementView() {
		return currentTextView;
	}

	Runnable countdown = new Runnable() {
		@Override
		public void run() {
			TextView tappedView = getMeasurementView();
			if (secs <= 0) {
				tappedView.setText(String.valueOf(voltsTextView.getText()));
				tappedView.setBackgroundColor(new Color().GREEN);
				fiveSecHandler.removeCallbacks(countdown);
				Toast.makeText(context, "Tap on next measurement",
						Toast.LENGTH_SHORT);
				locked = false;
				nextTextView++;
				return;
				// break;
			}
			tappedView.setText("Keep it for " + secs / 1000 + " seconds");
			locked = true;
			secs -= 1000;
			fiveSecHandler.postDelayed(countdown, 1000);
		}
	};

	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = preferences.edit(); // Put the values
		editor.putString("m1", m1.getText().toString());
		editor.putString("m2", m2.getText().toString());
		editor.putString("m3", m3.getText().toString());
		editor.putInt("next", nextTextView);
		editor.putInt("reportNumber", reportNumber);
		editor.commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String a = prefs.getString("m1", "");
		String b = prefs.getString("m2", "");
		String c = prefs.getString("m3", "");
		owner = prefs.getString("employee", "");
		nextTextView = prefs.getInt("next", 0);
		reportNumber = prefs.getInt("reportNumber", 1);
		savedData.add(a);
		savedData.add(b);
		savedData.add(c);
		savedData = checkNoValue(savedData);
		loadValues(savedData);
	}

	private ArrayList<String> checkNoValue(ArrayList<String> data) {
		ArrayList<String> newList = new ArrayList<String>();
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).contains("Keep it for"))
				newList.add(data.get(i).toString().replaceAll(data.get(i), ""));
			else
				newList.add(data.get(i).toString());
		}
		return newList;
	}

	private void loadValues(ArrayList<String> data) {
		if (!data.get(0).toString().equalsIgnoreCase("")) {
			m1.setBackgroundColor(new Color().GREEN);
			m1.setText(data.get(0).toString());
		}
		if (!data.get(1).toString().equalsIgnoreCase("")) {
			m2.setBackgroundColor(new Color().GREEN);
			m2.setText(data.get(1).toString());
		}
		if (!data.get(2).toString().equalsIgnoreCase("")) {
			m3.setBackgroundColor(new Color().GREEN);
			m3.setText(data.get(2).toString());
		}
	}
}
