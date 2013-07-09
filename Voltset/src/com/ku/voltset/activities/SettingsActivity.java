package com.ku.voltset.activities;

import com.ku.voltset.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
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
	public static final int SIGNATURE_ACTIVITY = 1;
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
		Button form=(Button)findViewById(R.id.btnForm);
		form.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, FormActivity.class); 
                startActivityForResult(intent,SIGNATURE_ACTIVITY);
            }
        });
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode) {
        case SIGNATURE_ACTIVITY: 
            if (resultCode == RESULT_OK) {
 
                Bundle bundle = data.getExtras();
                String status  = bundle.getString("status");
                if(status.equalsIgnoreCase("done")){
                    Toast toast = Toast.makeText(this, "Signature capture successful!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 105, 50);
                    toast.show();
                }
            }
            break;
        }
 
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
