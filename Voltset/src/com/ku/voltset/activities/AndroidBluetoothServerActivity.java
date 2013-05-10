package com.ku.voltset.activities;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import com.ku.voltset.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * @author chmod Activity that listens for bluetooth messages
 */
public class AndroidBluetoothServerActivity extends Activity {
	LinearLayout layout;
	BluetoothServer bluetoothServer;
	EditText message;
	final Handler handler = new Handler();

	/**
	 * Runnable to append data to edittext widget. Called from inner class as
	 * handler.
	 */
	final Runnable updateUI = new Runnable() {
		public void run() {
			message.append(bluetoothServer.getBluetoothServer() + "\n");
		}
	};

	@SuppressWarnings("static-access")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth);
		message = (EditText) findViewById(R.id.txtMSG);
		AlertDialog ad = new AlertDialog.Builder(this).create();
		ad.setTitle("Notice");
		ad.setMessage("You need to already have devices paired. Do you want me to open Bluetooth settings for you?");
		ad.setButton(ad.BUTTON_POSITIVE, "Yes",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intentBluetooth = new Intent();
						intentBluetooth
								.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
						startActivity(intentBluetooth);
					}
				});
		ad.setButton(ad.BUTTON_NEGATIVE, "No",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Do nothing
					}
				});
		ad.show();
		bluetoothServer = new BluetoothServer(handler, updateUI);
		bluetoothServer.start();
	}
}

/**
 * @author chmod Threaded class to process bluetooth connection and messages.
 */
class BluetoothServer extends Thread {
	BluetoothAdapter mBluetoothAdapter = null;
	String data = null;
	final Handler handler;
	final Runnable updateUI;
	BluetoothServerSocket serverSocket;

	public BluetoothServer(Handler handler, Runnable updateUI) {
		BluetoothServerSocket tmp = null;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		try {
			tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
					"btService",
					UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
		} catch (IOException e) {
		}
		serverSocket = tmp;
		this.handler = handler;
		this.updateUI = updateUI;
	}

	/**
	 * Gets the data from bluetooth. Used in the updateUI.
	 * 
	 * @return String parsed through bluetooth connect
	 */
	public String getBluetoothServer() {
		return data;
	}

	public void run() {

		BluetoothSocket socket = null;
		while (true) {
			try {
				Log.e("THREAD", "SERVER SOCKET LISTENING");
				socket = serverSocket.accept();
			} catch (IOException e) {
				break;
			}
			if (socket != null) {
				try {
					// Read the incoming string.

					DataInputStream in = new DataInputStream(
							socket.getInputStream());

					data = in.readUTF();
					handler.post(updateUI);
					Log.e("THREAD", data);
				} catch (IOException e) {
					Log.e("THREAD", "Error obtaining InputStream from socket");
					e.printStackTrace();
				}
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}
}