package com.ku.voltset.services;

import java.util.ArrayList;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI.DeviceArrivalCallback;
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
import android.util.Log;
import android.widget.Toast;

public class HardwareController_service extends Service {
	private static final String TAG = "YOCTOPUS_SERVICE";
	String serial;
	Handler hHardwareControler;
	Handler measuringController;
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SET_STRING_VALUE = 3;
	public static final int MSG_GET_YOCTO_VALUES = 4;
	public static final int MSG_DC_VALUE = 5;
	private static final int interval = 500; // Scan for device every 500 ms. We
												// need to ensure each time
												// device is connected.
	static final int threshold = (1000 / interval) * 3; // Seconds to reach
														// before saying thats
														// the correct value so
														// as to save it in
														// previous measurement
	int seconds = 0;
	boolean dcVolt = false;
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	YModule module = null;
	String currentMeasurement = "0.00";
	String lastMeasurement = "0.00";
	YVoltage dc_sensor;
	Bundle bundle;

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		hHardwareControler = new Handler();
		measuringController = new Handler();
		hHardwareControler.postDelayed(scanner, 0);
		// Register handlers
		YAPI.RegisterDeviceRemovalCallback(drc);
		YAPI.RegisterDeviceArrivalCallback(dac);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		hHardwareControler.removeCallbacks(scanner);
		if (measuringController != null)
			measuringController.removeCallbacks(measurer);
	}

	@Override
	public void onStart(Intent intent, int startid) {
		Log.d(TAG, "onStartHCService");
	}

	/**
	 * If serial is null, yocto device is not identified. Refresh.
	 * 
	 * @return true for null, false for not null
	 */
	protected boolean isSerialNull() {
		return serial == null;
	}

	Runnable measurer = new Runnable() {
		@Override
		public void run() {
			if (!isSerialNull()) {
				try {
					if (Double.valueOf(currentMeasurement) > 0.01)// we
																	// currently
																	// have
																	// measurement
						seconds += 2;
					lastMeasurement = currentMeasurement; // get the last
					currentMeasurement = String.format("%.3f", // format it to
																// 0.### digits
							dc_sensor.getCurrentValue());
					bundle = new Bundle();
					bundle.putString("dc", currentMeasurement); // put dc into
																// bundle
					if (!currentMeasurement.equalsIgnoreCase(lastMeasurement))
						seconds = 0; // zero it if not same
					if (seconds == threshold // 3sec
							&& currentMeasurement
									.equalsIgnoreCase(lastMeasurement) // same
																		// value
							&& Double.valueOf(currentMeasurement) > 0.001) { // but
																				// not
																				// zero
						bundle.putString("holded", lastMeasurement); // add it
																		// to
																		// bundle
						seconds = 0;
					}
					for (int i = mClients.size() - 1; i >= 0; i--) {
						try {
							Message msg = Message.obtain(null, MSG_DC_VALUE); // send
																				// message
																				// of
																				// type
																				// DC_VALUE
							msg.setData(bundle);
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
			measuringController.postDelayed(measurer, interval); // re-run after
																	// 500ms
		}
	};
	Runnable scanner = new Runnable() {
		@Override
		public void run() {
			scan(); // scan for maybe disconnected module
			hHardwareControler.postDelayed(scanner, interval); // and re run
																// runnable
																// after 500ms
		}
	};

	DeviceRemovalCallback drc = new DeviceRemovalCallback() {
		// Device not found, panic!
		@Override
		public void yDeviceRemoval(YModule module) {
			serial = null; // nullify

			measuringController.removeCallbacks(measurer);
			for (int i = mClients.size() - 1; i >= 0; i--) {
				try {
					Bundle bundle = new Bundle();
					bundle.putString("serial", "null");// inform activity that
														// is null
					Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
					msg.setData(bundle);
					mClients.get(i).send(msg);
				} catch (RemoteException e) {
					// The client is dead. Remove it from the list;
					// we are going through the list from back to front
					// so this is safe to do inside the loop.
					mClients.remove(i);
				}
			}
		}
	};
	DeviceArrivalCallback dac = new DeviceArrivalCallback() {
		// we got new device plugged in
		@Override
		public void yDeviceArrival(YModule lmodule) {
			try {
				if (lmodule.get_productName().equalsIgnoreCase("Yocto-Volt")) {// is
																				// it
																				// really
																				// a
																				// yocto-volt
																				// device?
					serial = lmodule.get_serialNumber(); // if yes grab the
															// serial
					dcVolt = false;
					hHardwareControler.postDelayed(scanner, interval); // and
																		// enable
																		// the
																		// scanner
					module=lmodule;
				}
			} catch (YAPI_Exception yapi) {
				yapi.printStackTrace();
			}
		}
	};

	public void scan() {
		try {
			YAPI.UpdateDeviceList(); // Update device list
			if (serial != null) {// Device found inform activity about that
				if (dcVolt == false) { // no need to make new instances all time
					dc_sensor = YVoltage.FindVoltage(getSerial() + ".voltage1");
					measuringController.postDelayed(measurer, interval); // start
																			// the
																			// controller
					dcVolt = true;
				}
				for (int i = mClients.size() - 1; i >= 0; i--) {
					try {
						bundle = new Bundle();
						bundle.putString("serial", getSerial()); // give the
																	// serial to
																	// activity
						Message msg = Message
								.obtain(null, MSG_SET_STRING_VALUE);
						msg.setData(bundle);
						mClients.get(i).send(msg);

					} catch (RemoteException e) {
						// The client is dead. Remove it from the list;
						// we are going through the list from back to front
						// so this is safe to do inside the loop.
						mClients.remove(i);
					}
				}
			}
		} catch (YAPI_Exception yapi) {
			yapi.printStackTrace();
		}
	}

	public String getSerial() {// Get the serial number
		return serial;
	}

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
			case MSG_GET_YOCTO_VALUES:// Activity asks for values
				Bundle values = new Bundle();
				// place them in a bundle
				try {
					values.putString("Luminosity",
							String.format("%d%%", module.get_luminosity()));
					values.putString("UpTime", module.get_upTime() / 1000
							+ " sec");
					values.putString("UsbCurrent", module.get_usbCurrent()
							+ " mA");
					values.putString("Beacon",
							(module.get_beacon() == YModule.BEACON_ON) ? "on"
									: "off");
					
					values.putString("serial", module.getSerialNumber());
				} catch (YAPI_Exception e) {
					e.printStackTrace();
				}
				for (int i = mClients.size() - 1; i >= 0; i--) {
					try {
						Message message = Message.obtain(null,
								MSG_GET_YOCTO_VALUES);
						// and send
						message.setData(values);
						mClients.get(i).send(message);
					} catch (RemoteException yapi) {
						// The client is dead. Remove it from the list;
						// we are going through the list from back to front
						// so this is safe to do inside the loop.
						mClients.remove(i);
					}
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
}
