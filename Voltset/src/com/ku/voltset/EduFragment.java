package com.ku.voltset;

import java.util.Calendar;

import com.yoctopuce.YoctoAPI.*;

import com.ku.voltset.R;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author chmod
 * 
 */
public class EduFragment extends Fragment implements OnClickListener {
	float lastMeasurement = 0.0f;
	float currentMeasurement = 0.0f;
	ImageView image; // Arrow image
	Handler handler = null; // Handler for firing listening events on voltset
	int duration = 2500; // Animation duration not so fast
	String serial = null; // Serial number of device
	private final float Xaxis = 0.835366f; // Image Centers
	private final float Yaxis = 0.676692f; // Image Centers
	RotateAnimation rotate = null; // Rotate animation
	TextView mVoltsText; // Current measurement
	TextView prevText;// Previous measurement
	View mRoot;
	Logger log=null;
	static final int interval=500; //Every 500ms measure
	static final int threshold=(1000/interval)*3; //Seconds to reach before saying thats the correct value so as to save it in previous measurement
	int seconds=0;
	private static final String file="VoltSet.csv"; //Our log file
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//Show fragment_edu layout
		mRoot = inflater.inflate(R.layout.fragment_edu, container, false);
		//Parse image
		image = (ImageView) mRoot.findViewById(R.id.imageArrow);
		//Parse text text with current measurement
		mVoltsText = (TextView) mRoot.findViewById(R.id.mVoltsText);
		//Parse text with previous measurement
		prevText = (TextView) mRoot.findViewById(R.id.prevText);
		//Register the handler for every 500ms
		handler = new Handler();
		handler.postDelayed(r, interval); // Start measuring at start of activity
		log=new Logger(getActivity());
		log.setFile(file);
		return mRoot;
	}

	@Override
	public void onClick(View view) { // button clicks, on activity
	}

	/**
	 * Listener responsible for preventing arrow from resetting after animation
	 * end.
	 * 
	 */
	AnimationListener animationListener = new AnimationListener() {
		@Override
		public void onAnimationStart(Animation animation) {
			animation.setFillAfter(true);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub

		}
	};
	protected static String getTimeStamp(){
		return java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
	}
	/**
	 * Runnable binded to handler
	 * 
	 */
	final Runnable r = new Runnable() {
		public void run() {
			
			if (serial != null) {// So we finally have a serial, measure!
				YVoltage dc_sensor = YVoltage.FindVoltage(serial + ".voltage1"); // get
																					// the
																					// sensors,
																					// to
																					// be
																					// taken
																					// serialized/parced
																					// HardwareController
				try {
					currentMeasurement = Float.parseFloat((String.format(
							"%.1f", dc_sensor.getCurrentValue())));// We want
																	// decimal
																	// values
					if (currentMeasurement < 0) { // alert to reverse the
													// polarity
						Toast.makeText(mRoot.getContext(),
								"Reverse your cables!", Toast.LENGTH_SHORT)
								.show();
						handler.postDelayed(this, interval);
						seconds=0; //zero it we restart
						return;
					} else if (currentMeasurement == 0) {// No more measuring
						if (rotate != null) {
							rotate.cancel();
							lastMeasurement = 0.0f;
							mVoltsText.setText("0.0V");
						}
					} else if (currentMeasurement == lastMeasurement) {
						seconds+=1;
						if(seconds>=threshold){
							lastMeasurement = Float.parseFloat((String.format(
									"%.1f", dc_sensor.getCurrentValue())));
							prevText.setText(lastMeasurement+"V");
							seconds=0;
						}
					} else if (currentMeasurement > lastMeasurement) { // Probably
																		// new
																		// measurement,
																		// catch
																		// it
						rotate = new RotateAnimation(0,
								Float.valueOf(currentMeasurement * 36),
								Animation.RELATIVE_TO_SELF, Xaxis,
								Animation.RELATIVE_TO_SELF, Yaxis);
						rotate.setInterpolator(new LinearInterpolator());
						rotate.setDuration(duration);
						rotate.setRepeatCount(0);
						rotate.setAnimationListener(animationListener);
						image.startAnimation(rotate);
						mVoltsText.setText(String.format("%.1f %s",
								dc_sensor.getCurrentValue(),
								dc_sensor.getUnit())
								+ "V");
						log.write("Time:"+getTimeStamp()+",Measurement:" + currentMeasurement + "DC");

					} else {						
						
						rotate = new RotateAnimation(
								Float.valueOf(currentMeasurement * 36), 0,
								Animation.RELATIVE_TO_SELF, Xaxis,
								Animation.RELATIVE_TO_SELF, Yaxis);
						rotate.setInterpolator(new LinearInterpolator());
						rotate.setDuration(duration);
						rotate.setRepeatCount(0);
						rotate.setAnimationListener(animationListener);
						image.startAnimation(rotate);
						seconds=0;//zero it, cables unplugged
					}

				} catch (YAPI_Exception e) {
					e.printStackTrace();
				}
//				 YVoltage ac_sensor = YVoltage.FindVoltage(serial +
//				 ".voltage2");//AC sensor, to be taken by HardwareController
//				 try {
//					 acTxtView.setText(String.valueOf(ac_sensor.getCurrentValue()));
//				 } catch (YAPI_Exception e) {
//					 e.printStackTrace();
//				 }
			}
			handler.postDelayed(this, interval);// Run again in 2sec
		}
	};
	public String getSerial() {
		return serial;
	}
	public void setSerial(String serial) {
		this.serial = serial;
	}
}
