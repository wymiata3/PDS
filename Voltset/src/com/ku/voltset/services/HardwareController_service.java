package com.ku.voltset.services;

import java.util.ArrayList;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YModule;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class HardwareController_service extends Service {
	private static final String TAG = "YOCTOPUS_SERVICE";
	private String serial;
	Handler hHardwareControler;
	public static final int MSG_SET_VALUE = 3;
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// Display a notification about us starting.
	}

	@Override
	public void onDestroy() {
		Toast.makeText(getApplicationContext(), "My Service Stopped",
				Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		hHardwareControler.removeCallbacks(scanner);
	}

	@Override
	public void onStart(Intent intent, int startid) {
		Log.d(TAG, "onStartHCService");
		hHardwareControler = new Handler();
		hHardwareControler.postDelayed(scanner, 2000);
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
			hHardwareControler.postDelayed(scanner, 2000);

		}
	};

	public void scan() {
		String serial = null;
		try {
			YAPI.EnableUSBHost(this); // Enable usb host mode
			YAPI.RegisterHub("usb");
			YModule module = YModule.FirstModule(); // Get the first module and
													// loop
			while (module != null) {
				if (module.get_productName().equalsIgnoreCase("Yocto-Volt")) { // Product
																				// is
																				// Yocto-Volt
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
		if (this.serial != null) {// Device found stop callbacks
			hHardwareControler.removeCallbacks(scanner);
			Toast.makeText(this, "Device found", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Device not found", Toast.LENGTH_SHORT).show();
			for (int i = mClients.size() - 1; i >= 0; i--) {
				try {
					Log.d("YOCTOPUS_SERVICE","MPIKE");
					mClients.get(i).send(
							Message.obtain(null, MSG_SET_VALUE, 54545, 2220));
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
			case MSG_SET_VALUE:
				if (isSerialNull())
					mValue = 0;
				else
					mValue = 1;
				for (int i = mClients.size() - 1; i >= 0; i--) {
					try {
						mClients.get(i).send(
								Message.obtain(null, MSG_SET_VALUE, mValue, 0));
					} catch (RemoteException e) {
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

	int mValue = 0;
	final Messenger mMessenger = new Messenger(new IncomingHandler());

}
