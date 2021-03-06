package com.ku.voltset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import android.content.Context;
import android.util.Log;
/**
 * Logging class Saves data to file
 * 
 * @author chmod
 * 
 */
public class Logger {
	private static final String TAG = "Logger";
	File toWrite = null;
	File root = null;
	Context context;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            Application's context for writing files
	 */
	public Logger(Context context) {
		this.context = context;
		// File will be saved to context directory
		root = new File(context.getFilesDir(), "VoltSet");
		if (!root.exists())
			root.mkdirs();
	}

	/**
	 * @param filename
	 *            the name of file to be saved as Default setter.
	 */
	public void setFile(String filename) {
		if (root != null) {
			toWrite = new File(root, filename);
		} else {
			Log.d(TAG, "root is null!");
			return;
		}
	}

	/**
	 * @return File to be written as long default getter
	 */
	public File getFile() {
		return toWrite;
	}

	/**
	 * Log rotate, responsible for deleting log
	 * 
	 * @param daysBack
	 *            defines how old shall be allowed before delete
	 * @param kbSize
	 *            defines how big shall be allowed to growth before delete Log
	 *            Rotate is used to delete log file in order to save space. If
	 *            above criterias are met, then file is deleted.
	 */

	public void logRotate(Long daysBack, int kbSize) {
		if (getFile() != null) {

			long purgeTime = System.currentTimeMillis()
					- (daysBack * 24 * 60 * 60 * 1000);
			if ((getFile().lastModified() < purgeTime)
					|| getFile().length() / (1024) > kbSize) {
				if (getFile().exists())
					getFile().delete();
			}
		}
	}

	/**
	 * Writes data to the lo file
	 * 
	 * @param data
	 *            Log line to be appended to log file
	 */
	public void write(String data) {
		if (getFile() == null) {
			throw new NullPointerException();
		}
		try {
			FileOutputStream fOut = new FileOutputStream(toWrite, true);
			OutputStreamWriter ow = new OutputStreamWriter(fOut);
			BufferedWriter out = new BufferedWriter(ow);
			out.write(data);
			out.newLine();
			// close streams
			out.flush();
			out.close();
			ow.close();
			fOut.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}
}
