package com.ku.voltset.services;

import java.util.ArrayList;
import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI.DeviceRemovalCallback;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YModule;
import com.yoctopuce.YoctoAPI.YVoltage;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/**
 * Service activity to handle communication between Activities and H/W device.
 * @author chmod
 *
 */
public class HardwareController_service extends Service {
	String serial;
	Handler measuringController;
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_DISCONNECT = 3;
	public static final int MSG_MEASUREMENT = 5;
	// Interval to communicate with hardware
	private static final int interval = 1;
	// Seconds to reach before saying thats the correct value so as to save it
	// in previous measurement
	static final int threshold = (10000 * interval) * 3;
	int seconds = 0;
	// Holds the clients
	static ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	// Bridge between service and bounded activities
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	// THE module
	YModule module;
	// initial values
	String currentMeasurement = "0.00";
	String lastMeasurement = "0.00";
	YVoltage dc_sensor;
	boolean zeroSended = false;

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			// We assume its null so as to be safe
			module = null;
			// Get the first and scan through all modules
			// so as to find the serial and initialize dc_sensor
			
			// TODO initialize ac_sensor too
			module = YModule.FirstModule();
			while (module != null) {
				// User has bought a yocto-volt
				if (module.get_productName().equals("Yocto-Volt")) {
					// save the serial number
					serial = module.get_serialNumber();
					// and initialize the dc_sensor
					dc_sensor = YVoltage.FindVoltage(serial + ".voltage1");
					break;
				}
				module = module.nextModule();
			}
			// this handler is handling the measurements
			measuringController = new Handler();
			// every interval
			measuringController.postDelayed(measurer, interval);
			// Register event in case device is removed
			YAPI.RegisterDeviceRemovalCallback(drc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		// destroy everything
		// and unregister
		if (measuringController != null)
			measuringController.removeCallbacks(measurer);
		YAPI.UnregisterHub("usb");
		YAPI.RegisterDeviceRemovalCallback(null);
	}

	/**
	 *Runnable responsible for passing measurements to activity 
	 */
	Runnable measurer = new Runnable() {
		@Override
		public void run() {
			try {
				// update the device list
				// if changes (=removal) fire the callback registered
				YAPI.UpdateDeviceList();
			} catch (YAPI_Exception yapi) {
				yapi.printStackTrace();
			}
			if (module.isOnline()) {
				try {
					// we currently have measurement
					if (Double.valueOf(currentMeasurement) > 0.01) {
						seconds += 1000;
					}
					// get the last
					lastMeasurement = currentMeasurement;
					// format it to 0.## digits
					currentMeasurement = String.format("%.2f",
							dc_sensor.get_currentValue());
					// discard zero values, its spam
					if (Double.valueOf(currentMeasurement) == 0
							&& zeroSended == true) {
						//rerun
						//retun not necessar
						measuringController.postDelayed(this, interval);
						return;
					} else {
						//values is not zero, we need to catch it
						zeroSended = false;
					}
					//0 was sent
					if (Double.valueOf(currentMeasurement) == 0.0)
						zeroSended = true;
					Bundle bundle = new Bundle();
					// put dc into bundle
					//TODO put ac into bundle too
					bundle.putString("dc", currentMeasurement);
					if (!currentMeasurement.equalsIgnoreCase(lastMeasurement))
						seconds = 0; // zero it if not same
					if (seconds >= threshold // 3sec
							&& currentMeasurement
							// same value
									.equalsIgnoreCase(lastMeasurement)
							// but not zero
							&& Double.valueOf(currentMeasurement) > 0.001) {
						// add it to bundle
						bundle.putString("holded", lastMeasurement);
						seconds = 0;
					}
					// send message of type MEASUREMENT
					Message msg = Message.obtain(null, MSG_MEASUREMENT);
					msg.setData(bundle);
					for (int i = mClients.size() - 1; i >= 0; i--) {
						try {
							mClients.get(i).send(msg);
						} catch (RemoteException e) {
							// The client is dead. Remove it from the list;
							// we are going through the list from back to
							// front
							// so this is safe to do inside the loop.
							mClients.remove(i);
						}
					}
				} catch (YAPI_Exception e) {
					e.printStackTrace();
				}
			}
			// re-run after interval
			measuringController.postDelayed(this, interval); 
		}
	};

	/**
	 * Callback if device is not found (unplugged).
	 */
	DeviceRemovalCallback drc = new DeviceRemovalCallback() {
		// Device not found, panic!
		@Override
		public void yDeviceRemoval(YModule module) {
			//unregister handler
			measuringController.removeCallbacks(measurer);
			//send message to all clients
			Message msg = Message.obtain(null, MSG_DISCONNECT);
			for (int i = mClients.size() - 1; i >= 0; i--) {
				try {
					mClients.get(i).send(msg);
				} catch (RemoteException e) {
					// The client is dead. Remove it from the list;
					// we are going through the list from back to front
					// so this is safe to do inside the loop.
					mClients.remove(i);
				}
			}
			//end send message
		}
	};

	/**
	 * @author chmod
	 * inner class responsible to register and unregister activities
	 *
	 */
	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT: // register client
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT: // unregister client
				mClients.remove(msg.replyTo);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
}
