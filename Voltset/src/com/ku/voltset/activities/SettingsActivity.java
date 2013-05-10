package com.ku.voltset.activities;

import com.ku.voltset.LogViewer;
import com.ku.voltset.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * Activity responsible for showing settings
 * 
 * @author chmod
 * 
 */
public class SettingsActivity extends Activity implements
		OnSeekBarChangeListener, OnClickListener {
	private int duration;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		// default value =0
		int duration = this.getIntent().getIntExtra("duration", 0);
		this.setDuration(duration);
		// TODO pass these parameters to other activities
		SeekBar durationLength = (SeekBar) findViewById(R.id.seekBar);
		durationLength.setProgress(duration);
		durationLength.setOnSeekBarChangeListener(this);
		Button btnLogView = (Button) findViewById(R.id.btnViewLogs);
		btnLogView.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// user is scrolling the bar, update the value
		this.setDuration(progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * Default getter
	 * 
	 * @return duration
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * Default setter
	 * 
	 * @param duration
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnViewLogs) {
			// clicked on view logs, create the activity
			Intent logViewer = new Intent(this, LogViewer.class);
			startActivity(logViewer);// And now start
			// with animation
			overridePendingTransition(R.anim.left_to_right,
					R.anim.right_to_left);
		}
	}
}
