package com.ku.voltset;

import android.os.Bundle;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Startup extends Activity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);
		Button basicReading = (Button) findViewById(R.id.basicReading);
		Button conf = (Button) findViewById(R.id.conf);
		basicReading.setOnClickListener(this);
		conf.setOnClickListener(this);
		Button quit=(Button)findViewById(R.id.quit);
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
		if(v.getId()==R.id.basicReading)
		{
			Intent basicReadingActivity = new Intent(this, MainActivity.class);
			basicReadingActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			basicReadingActivity.putExtra("duration",2500);
			startActivity(basicReadingActivity);
			overridePendingTransition(R.anim.left_to_right,
					R.anim.right_to_left);
		}
		if(v.getId()==R.id.conf)
		{
			Intent settingsActivity = new Intent(this, SettingsActivity.class);
			settingsActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			settingsActivity.putExtra("duration",2500);
			startActivity(settingsActivity );
			overridePendingTransition(R.anim.left_to_right,
					R.anim.right_to_left);
		}
		if(v.getId()==R.id.quit)
		{
			finish();
		}
		
	}

}
