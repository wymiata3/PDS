package com.ku.voltset;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.widget.Toast;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YModule;
import com.yoctopuce.YoctoAPI.YVoltage;

//TODO Serializable or Parceable
public class HardwareController {
	private String serial;
	Context context;
	Handler hHardwareControler;
	StartupActivity activity;
	public HardwareController(Context context,StartupActivity activity) {
		this.context = context; // Application context
		hHardwareControler = new Handler(); // handler to scan device
		hHardwareControler.postDelayed(scanner, 2000); // run every 2 seconds
		this.activity=activity;
	}

	/**
	 * scans for device by re-registering hub and checks for module in available
	 * ymodules
	 */
	protected void scan() {
		String serial = null;
		try {
			YAPI.EnableUSBHost(context); // Enable usb host mode
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
					activity.infoIcon.setEnabled(true);
					break;
				}
				module = module.nextModule(); // next module if is null
			}
		} catch (YAPI_Exception yapi) {
			yapi.printStackTrace();
		}
		if (this.serial != null) {// Device found stop callbacks
			hHardwareControler.removeCallbacks(scanner);
			Toast.makeText(context, "Device found", Toast.LENGTH_SHORT).show();
		}
	}

	protected String getSerial() {// Get the serial number
		return serial;
	}

	protected static YVoltage getdcSensor(String serial) {// get the DC sensor
		YVoltage dc_sensor = YVoltage.FindVoltage(serial + ".voltage1");
		return dc_sensor;
	}

	protected static YVoltage getacSensor(String serial) { // get the AC sensor
		YVoltage ac_sensor = YVoltage.FindVoltage(serial + ".voltage2");
		return ac_sensor;
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
				hHardwareControler.removeCallbacks(scanner);// Device found,
															// remove callbacks
			} else {
				if(isRunning(context))
				showRescanDialog(); // Device not found, prompt for scanning
			}
		}
	};

	public static boolean isRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (RunningTaskInfo task : tasks) {
            if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) 
                return true;                                  
        }

        return false;
    }

	public void showRescanDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.dialog_rescan)
				.setNegativeButton(R.string.dialog_negative,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								hHardwareControler.removeCallbacks(scanner); // User
																				// wants
																				// not
																				// to
																				// scan,
																				// abort
								return;
							}
						})
				.setPositiveButton(R.string.dialog_positive,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								hHardwareControler.postDelayed(scanner, 2000);// User
																				// wants
																				// to
																				// scan,
																				// then..
																				// scan
							}
						}).show();

	}

	public void terminate() {
		if (hHardwareControler != null) {
			hHardwareControler.removeCallbacks(scanner);
			hHardwareControler = null;
		}
	}
}
