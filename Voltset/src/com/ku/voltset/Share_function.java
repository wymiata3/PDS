package com.ku.voltset;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class Share_function extends Activity implements OnSeekBarChangeListener{
	
	protected void onCreate (Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.share_activity);
	    
	     
	    
	    Button buttonSend = (Button) findViewById(R.id.SendButton);
	    final EditText ToText = (EditText) findViewById(R.id.ToText);
	    final EditText SubjectText = (EditText) findViewById(R.id.SubjectText);
	    final EditText MessageText = (EditText) findViewById(R.id.MessageText);

	    buttonSend.setOnClickListener(new OnClickListener(){
	    	
	    	

	        @Override
	        public void onClick(View v) {

	            String to = ToText.getText().toString();
	            String subject = SubjectText.getText().toString();
	            String message = MessageText.getText().toString();

	            Intent email = new Intent(Intent.ACTION_SEND);
	            email.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
	            // email.putExtra(Intent.EXTRA_CC, new String[]{ to});
	            // email.putExtra(Intent.EXTRA_BCC, new String[]{to});
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
