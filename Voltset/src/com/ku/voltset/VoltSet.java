package com.ku.voltset;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class VoltSet extends Activity implements OnClickListener {
	HardwareController hc;
	final Context context = this;
	@Override
	protected void onStart() {
		super.onStart();
		hc = new HardwareController(context);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();

//		hc.terminate();
		try{YAPI.FreeAPI();}catch(Exception e){}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);
		Button basicReading = (Button) findViewById(R.id.basicReading);
		basicReading.setOnClickListener(this);
		Button conf = (Button) findViewById(R.id.conf);
		conf.setOnClickListener(this);
		Button quit = (Button) findViewById(R.id.quit);
		quit.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.startup, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.basicReading) {
			if (hc != null && !hc.isSerialNull()) {
				Intent basicReadingActivity = new Intent(this,
						BasicReadingActivity.class);
				basicReadingActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				basicReadingActivity.putExtra("duration", 2500);
				basicReadingActivity.putExtra("serial_number", hc.getSerial());
				startActivity(basicReadingActivity);
				overridePendingTransition(R.anim.left_to_right,
						R.anim.right_to_left);
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Device not found, can't proceed",Toast.LENGTH_SHORT).show();
				return;
			}
			
		}
		if (v.getId() == R.id.conf) {
			Intent settingsActivity = new Intent(this, SettingsActivity.class);
			settingsActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			settingsActivity.putExtra("duration", 2500);
			startActivity(settingsActivity);
			overridePendingTransition(R.anim.left_to_right,
					R.anim.right_to_left);
		}
		if (v.getId() == R.id.quit) {
			hc.terminate();
			hc=null;
			finish();
		}

	}

}
