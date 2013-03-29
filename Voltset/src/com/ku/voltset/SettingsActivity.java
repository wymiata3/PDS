package com.ku.voltset;

import com.ku.voltset.R;
import com.ku.voltset.R.layout;
import com.ku.voltset.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class SettingsActivity extends Activity implements OnSeekBarChangeListener {
	private int duration;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		int duration= this.getIntent().getIntExtra("duration",0);
		this.duration=duration;
		SeekBar durationLength=(SeekBar)findViewById(R.id.seekBar);
		durationLength.setProgress(duration);
		durationLength.setOnSeekBarChangeListener(this);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	        Intent mainActivity = new Intent(SettingsActivity.this,MainActivity.class);    
	        mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        mainActivity.putExtra("duration",duration);
	        startActivity(mainActivity);
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		duration=progress;
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

}
