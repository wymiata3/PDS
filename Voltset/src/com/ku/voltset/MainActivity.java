package com.ku.voltset;

import com.example.helloworld.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	Button lastPressedButton = null;
	int buttons[] = { R.id.aButton, R.id.cButton, R.id.pButton, R.id.vButton }; //Buttons IDs

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//Get buttons
		Button buttonVoltages = (Button) findViewById(R.id.vButton);
		Button buttonCurrent = (Button) findViewById(R.id.cButton);
		Button buttonAmpere = (Button) findViewById(R.id.aButton);
		Button buttonPower = (Button) findViewById(R.id.pButton);

		buttonVoltages.setEnabled(false);

		//Add Listeners
		buttonVoltages.setOnClickListener(voltageListener);
		buttonCurrent.setOnClickListener(currentListener);
		buttonAmpere.setOnClickListener(ampereListener);
		buttonPower.setOnClickListener(powerListener);
	}

	private void enableButClicked(Button pressed) {
		for (int j = 0; j < 4; j++) {
			if (pressed.getId() == buttons[j]) {//If clicked, disable
				pressed.setEnabled(false);
				continue;
			}
			if(!findViewById(buttons[j]).isEnabled()) //Not enabled? Enable
				findViewById(buttons[j]).setEnabled(true);
		}
	}

	private OnClickListener voltageListener = new OnClickListener() { // Change text to Volts
		public void onClick(View v) {
			TextView tv = (TextView) findViewById(R.id.lblCurrentMeasurement);
			tv.setText("Volts");
			lastPressedButton = (Button) findViewById(R.id.vButton);
			enableButClicked(lastPressedButton);
		}
	};

	private OnClickListener powerListener = new OnClickListener() {
		public void onClick(View v) {
			TextView tv = (TextView) findViewById(R.id.lblCurrentMeasurement); //Change text to Power
			tv.setText("Power");
			lastPressedButton = (Button) findViewById(R.id.pButton);
			enableButClicked(lastPressedButton);
		}
	};

	private OnClickListener ampereListener = new OnClickListener() {// Change text to Ampere
		public void onClick(View v) {
			TextView tv = (TextView) findViewById(R.id.lblCurrentMeasurement);
			tv.setText("Ampere");
			lastPressedButton = (Button) findViewById(R.id.aButton);
			enableButClicked(lastPressedButton);
		}
	};

	private OnClickListener currentListener = new OnClickListener() { // Change text to Current

		@Override
		public void onClick(View v) {
			TextView tv = (TextView) findViewById(R.id.lblCurrentMeasurement);
			tv.setText("Current");
			lastPressedButton = (Button) findViewById(R.id.cButton);
			enableButClicked(lastPressedButton);
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
