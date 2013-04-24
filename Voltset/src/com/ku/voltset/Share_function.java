package com.ku.voltset;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/*This class implements the share function in which users are able to send a message through the mail service
 * or Bluetooth to another mobile.
 */
public class Share_function extends Activity implements OnSeekBarChangeListener{
	protected void onCreate (Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.share_activity); //The layout for this functionality
	    final String file = "VoltSet.csv"; // Our log file
	    
	    Button GetLogsButton = (Button) findViewById(R.id.GetLogsButton);
	    Button buttonSend = (Button) findViewById(R.id.SendButton);
	    final EditText ToText = (EditText) findViewById(R.id.ToText);
	    final EditText SubjectText = (EditText) findViewById(R.id.SubjectText);
	    final EditText MessageText = (EditText) findViewById(R.id.MessageText);
	    
	    
	    /*This is a way to get the logs from the measurements and paste them into message so as 
	    a user can send the whole log file through e-mail
	    */
	    String logs = "";
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
				logs = logs + strLine + "\n";
			}
			// Close the input stream
			in.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	    
	    
	    final String flogs = logs; 
	    GetLogsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				MessageText.append(flogs);
				
			}
		});
	    
	    
	    buttonSend.setOnClickListener(new OnClickListener(){
	    
	        @Override
	        public void onClick(View v) {
	        	
	        	//keep the receiver, the subject and the message as a parameters and remove the first part of the
	        	//string since each EditText includes a word to clarify each field to the user
	            String to = ToText.getText().toString();
	            to = to.substring(3, to.length());
	            String subject = SubjectText.getText().toString();
	            subject = subject.substring(8, subject.length());
	            String message = MessageText.getText().toString();
	            message = message.substring(8, message.length());

	            Intent email = new Intent(Intent.ACTION_SEND);
	            email.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
	            email.putExtra(Intent.EXTRA_SUBJECT, subject);
	            email.putExtra(Intent.EXTRA_TEXT, message);

	            // need this to prompts email client only
	            email.setType("message/rfc822");

	            startActivity(Intent.createChooser(email, "Choose an Email client :"));

	        }
	    });
	    
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

}