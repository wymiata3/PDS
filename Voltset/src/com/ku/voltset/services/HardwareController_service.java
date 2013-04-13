package com.ku.voltset.services;

import java.util.ArrayList;

import com.yoctopuce.YoctoAPI.YAPI;
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
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	YModule module = null;
	double currentMeasurement = 0.00f;
	double lastMeasurement = 0.00f;

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		hHardwareControler = new Handler();
		hHardwareControler.postDelayed(scanner, interval);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		hHardwareControler.removeCallbacks(scanner);
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

	Runnable scanner = new Runnable() {
		@Override
		public void run() {
			scan(); // Scan
			if (!isSerialNull()) {
				try {
					YVoltage dc_sensor = YVoltage.FindVoltage(getSerial()
							+ ".voltage1");
					if (currentMeasurement > 0.01f)
						seconds += 1;
					lastMeasurement = currentMeasurement;
					currentMeasurement = dc_sensor.getCurrentValue();
					Bundle bundle = new Bundle();
					bundle.putString("dc",
							String.format("%.3f", currentMeasurement));
					if (seconds >= threshold
							&& currentMeasurement == lastMeasurement
							&& currentMeasurement > 0.001f) {
						bundle.putString("holded",
								String.format("%.3f", currentMeasurement));
						seconds = 0;
					}
					for (int i = mClients.size() - 1; i >= 0; i--) {
						try {
							Message msg = Message.obtain(null, MSG_DC_VALUE);
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
			hHardwareControler.postDelayed(scanner, interval);
		}
	};

	public void scan() {
		String serial = null;
		this.serial = null;
		try {
			YAPI.UpdateDeviceList(); // Update device list
			module = YModule.FirstModule(); // Get the first module and
											// loop

			while (module != null) {

				// Product is Yocto-Volt
				if (module.get_productName().equalsIgnoreCase("Yocto-Volt")) {
					serial = module.get_serialNumber(); // Grab the serial
														// number
					this.serial = serial;
					break;
				}
				module = module.nextModule(); // next module if is null
			}
		} catch (YAPI_Exception yapi) {
			yapi.printStackTrace();
		}
		if (!isSerialNull()) {// Device found inform activity about that
			for (int i = mClients.size() - 1; i >= 0; i--) {
				try {
					Bundle bundle = new Bundle();
					bundle.putString("serial", getSerial());
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
		} else { // Device not found, panic!
			for (int i = mClients.size() - 1; i >= 0; i--) {
				try {
					Bundle bundle = new Bundle();
					bundle.putString("serial", "null");
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
	}

	public String getSerial() {// Get the serial number
		return serial;
	}

	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_GET_YOCTO_VALUES:// Activity asks for values
				for (int i = mClients.size() - 1; i >= 0; i--) {
					try {
						// place them in a bundle
						Bundle bundle = new Bundle();
						bundle.putString("LogicalName", module.getLogicalName());
						bundle.putString("Luminosity",
								String.format("%d%%", module.getLuminosity()));
						bundle.putString("UpTime", module.getUpTime() / 1000
								+ " sec");
						bundle.putString("UsbCurrent", module.getUsbCurrent()
								+ " mA");
						bundle.putString(
								"Beacon",
								(module.getBeacon() == YModule.BEACON_ON) ? "on"
										: "off");
						Message message = Message.obtain(null,
								MSG_GET_YOCTO_VALUES);
						// and send
						msg.setData(bundle);
						mClients.get(i).send(message);
					} catch (RemoteException yapi) {
						// The client is dead. Remove it from the list;
						// we are going through the list from back to front
						// so this is safe to do inside the loop.
						mClients.remove(i);
					} catch (YAPI_Exception e) {
						e.printStackTrace();
					}
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public YModule getModule() {
		return module;
	}

}
