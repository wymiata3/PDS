package com.ku.voltset;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Log viweing activity
 * 
 * @author chmod
 * 
 */
public class LogViewer extends Activity implements OnClickListener {
	private static final String file = "VoltSet.csv"; // Our log file
	TextView logText;
	ScrollView sv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_viewer);
		logText = (TextView) findViewById(R.id.txtLogs);// get textview widget
		sv = (ScrollView) findViewById(R.id.scrolllview);// get scrollview
															// widget
		Button btnToTop = (Button) findViewById(R.id.btnToTop);// get buttons
		Button btnToEnd = (Button) findViewById(R.id.btnToEnd);
		// add listeners
		btnToTop.setOnClickListener(this);
		btnToEnd.setOnClickListener(this);

		// Scrolling method in the larg text area
		logText.setMovementMethod(new ScrollingMovementMethod());
		// read file line by line
		// TODO might lock the UI, place in async task or thread.
		try {
			Logger log = new Logger(this.getApplicationContext());
			log.setFile(file);
			FileInputStream fstream = new FileInputStream(log.getFile());
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				logText.append(strLine + "\n");
			}
			// Close the input stream
			in.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
		// move to the end
		logText.post(new Runnable() {
			public void run() {
				sv.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log_viewer, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnToTop) {
			// move at top
			logText.post(new Runnable() {
				public void run() {
					sv.fullScroll(ScrollView.FOCUS_UP);
				}
			});
		}
		if (v.getId() == R.id.btnToEnd) {
			// move at end
			logText.post(new Runnable() {
				public void run() {
					sv.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
		}

	}

}
