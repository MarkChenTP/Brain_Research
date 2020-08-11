/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *  BluetoothService.java
 *  Modified by: Mark Chen
 *  Last Modified: 08/14/2020
 *
 *  Note:
 *  The following codes are reconfigured for Nonin Bluetooth 9560 Pulse Oximeter.
 *  For the original source code, visit https://github.com/android/connectivity-samples
 */

package ucla.erlab.brainresearch;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothService {

	private Context mContext;

	// Name for the SDP record when creating server socket
	private static final String NAME_SECURE = "BluetoothChatSecure";
	private static final String NAME_INSECURE = "BluetoothChatInsecure";

	// Unique UUID for this application
	private static final UUID MY_UUID_SECURE =
			UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final UUID MY_UUID_INSECURE =
			UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Bluetooth Connection Fields
	private final BluetoothAdapter mBluetoothAdapter;
	private AcceptThread mSecureAcceptThread;
	private AcceptThread mInsecureAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;

	// Number codes that indicate the current connection state
	private int mBluetoothState;
	public static final int STATE_NONE = 0;           // Doing nothing
	public static final int STATE_LISTEN = 1;         // Listening for incoming connections
	public static final int STATE_CONNECTING = 2;     // Initiating an outgoing connection
	public static final int STATE_CONNECTED = 3;      // Connected to a remote device
	public static final int STATE_CONNECTFAILED = 4;  // Failed to connect a remote device
	public static final int STATE_CONNECTLOST = 5;    // Lost connection to a remote device
	public static final int STATE_END = 6;            // Service end

	// Message types sent to the Handler
	private final Handler mBluetoothHandler;
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_TOAST = 4;


	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 *
	 * @param context The UI Activity Context
	 * @param handler A Handler to send messages back to the UI Activity
	 */
	public BluetoothService(Context context, Handler handler) {
		mContext = context;
		mBluetoothHandler = handler;

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothState = STATE_NONE;
	}


	/**
	 * Return the current connection state.
	 */
	public synchronized int getBluetoothState() {
		return mBluetoothState;
	}


	/**
	 * Update the current connection state.
	 */
	private synchronized void setBluetoothState(int state) {
		Log.d("BluetoothService", mBluetoothState + " -> " + state);
		mBluetoothState = state;

		// Give the new connection state to the Handler so the UI Activity can update
		mBluetoothHandler.obtainMessage(MESSAGE_STATE_CHANGE, state).sendToTarget();
	}


	/**
	 * Start the bluetooth service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode.
	 */
	public synchronized void start() {
		Log.d("BluetoothService", "BluetoothService Start");

		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to listen on a BluetoothServerSocket
		if (mSecureAcceptThread == null) {
			mSecureAcceptThread = new AcceptThread(true);
			mSecureAcceptThread.start();
		}
		if (mInsecureAcceptThread == null) {
			mInsecureAcceptThread = new AcceptThread(false);
			mInsecureAcceptThread.start();
		}

		// Update bluetooth connection state
		setBluetoothState(STATE_LISTEN);
	}


	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 *
	 * @param device The BluetoothDevice to connect
	 * @param secure Socket Security type - Secure (true) , Insecure (false)
	 */
	public synchronized void connect(BluetoothDevice device, boolean secure) {
		Log.d("BluetoothService", "Connect to: " + device);

		// Cancel any thread attempting to make a connection
		if (mBluetoothState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device, secure);
		mConnectThread.start();

		// Update bluetooth connection state
		setBluetoothState(STATE_CONNECTING);
	}


	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 *
	 * @param socket The BluetoothSocket on which the connection was made
	 * @param device The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device,
									   final String socketType) {
		Log.d("BluetoothService", "Connected, Socket Type:" + socketType);

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Cancel the accept thread because we only want to connect to one device
		if (mSecureAcceptThread != null) {
			mSecureAcceptThread.cancel();
			mSecureAcceptThread = null;
		}
		if (mInsecureAcceptThread != null) {
			mInsecureAcceptThread.cancel();
			mInsecureAcceptThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket, socketType);
		mConnectedThread.start();

		// Send the name of the connected device back to the UI Activity
		Message msg = mBluetoothHandler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString("DeviceName", device.getName());
		bundle.putString("Message", "Connected to " + device.getName());
		bundle.putString("Topic", "Connected");
		msg.setData(bundle);
		mBluetoothHandler.sendMessage(msg);

		// Update bluetooth connection state
		setBluetoothState(STATE_CONNECTED);
	}


	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		Log.d("BluetoothService", "BluetoothService Stop");

		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mSecureAcceptThread != null) {
			mSecureAcceptThread.cancel();
			mSecureAcceptThread = null;
		}

		if (mInsecureAcceptThread != null) {
			mInsecureAcceptThread.cancel();
			mInsecureAcceptThread = null;
		}

		// Update bluetooth connection state
		setBluetoothState(STATE_END);
	}


	/**
	 * Write to the ConnectedThread in an asynchronous manner
	 *
	 * @param out The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (mBluetoothState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		// Perform the write asynchronously
		r.write(out);
	}



	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		Log.d("BluetoothService", "BluetoothService connection failed");

		// Send a failure message back to the Activity
		Message msg = mBluetoothHandler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString("Message", "Unable to connect device");
		bundle.putString("Topic", "Failed");
		msg.setData(bundle);
		mBluetoothHandler.sendMessage(msg);

		if(mBluetoothState != STATE_END) {
			// Update bluetooth connection state
			//setBluetoothState(STATE_NONE);

			// Start the service over to restart listening mode
			//BluetoothService.this.start();

			// Update bluetooth connection state
			setBluetoothState(STATE_CONNECTFAILED);

			// Cancel any thread attempting to make a connection
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}

			// Cancel any thread currently running a connection
			if (mConnectedThread != null) {
				mConnectedThread.cancel();
				mConnectedThread = null;
			}
		}
	}


	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		Log.d("BluetoothService", "BluetoothService connection lost");

		Message msg = mBluetoothHandler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString("Message", "Device connection was lost");
		bundle.putString("Topic", "Lost");
		msg.setData(bundle);
		mBluetoothHandler.sendMessage(msg);

		if(mBluetoothState != STATE_END) {
			// Update bluetooth connection state
			//setBluetoothState(STATE_NONE);

			// Start the service over to restart listening mode
			//BluetoothService.this.start();

			// Update bluetooth connection state
			setBluetoothState(STATE_CONNECTLOST);

			// Cancel any thread attempting to make a connection
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}

			// Cancel any thread currently running a connection
			if (mConnectedThread != null) {
				mConnectedThread.cancel();
				mConnectedThread = null;
			}
		}
	}




	/**
	 * This thread runs while listening for incoming connections. It behaves
	 * like a server-side client. It runs until a connection is accepted
	 * (or until cancelled).
	 */
	private class AcceptThread extends Thread {
		// The local server socket
		private final BluetoothServerSocket mServerSocket;
		private String mSocketType;

		public AcceptThread(boolean secure) {
			Log.d("BluetoothService", "mAcceptThread create()");

			BluetoothServerSocket tmpServerSocket = null;
			mSocketType = secure ? "Secure" : "Insecure";

			// Create a new listening server socket
			try {
				if (secure) {
					tmpServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
							NAME_SECURE, MY_UUID_SECURE);
				} else {
					tmpServerSocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
							NAME_INSECURE, MY_UUID_INSECURE);
				}
			} catch (IOException e) {
				Log.e("BluetoothService", "mAcceptThread create() failed");
				e.printStackTrace();
			}
			mServerSocket = tmpServerSocket;
		}

		public void run() {
			Log.d("BluetoothService", "mAcceptThread run() BEGIN, SocketType:" + mSocketType);
			setName("AcceptThread" + mSocketType);

			BluetoothSocket socket;

			// Listen to the server socket if we're not connected
			while (mBluetoothState != STATE_CONNECTED) {
				try {
					// This is a blocking call and will only return on a
					// successful connection or an exception
					socket = mServerSocket.accept();
				} catch (IOException e) {
					Log.e("BluetoothService", "mServerSocket accept() failed");
					e.printStackTrace();
					break;
				}

				// If a connection was accepted
				if (socket != null) {
					synchronized (BluetoothService.this) {
						switch (mBluetoothState) {
							case STATE_LISTEN:
							case STATE_CONNECTING:
								// Situation normal. Start the connected thread.
								connected(socket, socket.getRemoteDevice(),
										mSocketType);
								break;
							case STATE_NONE:
							case STATE_CONNECTED:
								// Either not ready or already connected. Terminate new socket.
								try {
									socket.close();
								} catch (IOException e) {
									Log.e("BluetoothService", "unable to close unwanted socket");
									e.printStackTrace();
								}
								break;
						}
					}
				}
			}

			Log.d("BluetoothService", "mAcceptThread run() END, SocketType:" + mSocketType);
		}

		public void cancel() {
			Log.d("BluetoothService", "mAcceptThread cancel()");
			// Close the listening server socket
			try {
				mServerSocket.close();
			} catch (IOException e) {
				Log.e("BluetoothService", "mAcceptThread cancel() failed");
				e.printStackTrace();
			}
		}
	}


	/**
	 * This thread runs while attempting to make an outgoing connection
	 * with a device. It runs straight through; the connection either
	 * succeeds or fails.
	 */
	private class ConnectThread extends Thread {
		private BluetoothSocket mSocket;
		private BluetoothDevice mDevice;
		private String mSocketType;

		public ConnectThread(BluetoothDevice device, boolean secure) {
			Log.d("BluetoothService", "mConnectThread create()");
			mDevice = device;
			BluetoothSocket tmpSocket = null;
			mSocketType = secure ? "Secure" : "Insecure";

			// Get a BluetoothSocket for a connection with the given BluetoothDevice
			try {
				if (secure) {
					tmpSocket = device
							.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
				} else {
					tmpSocket = device
							.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
				}
			} catch (IOException e) {
				Log.e("BluetoothService", "mConnectThread create() failed");
				e.printStackTrace();
			}
			mSocket = tmpSocket;
		}

		public void run() {
			Log.d("BluetoothService", "mConnectThread run() BEGIN, SocketType:" + mSocketType);
			setName("ConnectThread" + mSocketType);

			// Always cancel discovery because it will slow down a connection
			mBluetoothAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mSocket.connect();
			} catch (IOException e1) {
				Log.e("BluetoothService", "1st trial of bluetooth connection failed");

				try {
					Thread.sleep(300);
					Class<?> clazz = mSocket.getRemoteDevice().getClass();
					Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};

					Method m = clazz.getMethod("createRfcommSocket", paramTypes);
					Object[] jrParams = new Object[]{Integer.valueOf(1)};

					mSocket = (BluetoothSocket) m.invoke(mSocket.getRemoteDevice(), jrParams);
					assert mSocket != null;
					mSocket.connect();
				} catch (Exception e2) {
					Log.e("BluetoothService", "2nd trial of bluetooth connection failed");
					e2.printStackTrace();
				}

				// Close the socket
				try {
					mSocket.close();
				} catch (IOException e3) {
					Log.e("BluetoothService", "unable to close mSocket after connection failure");
					e3.printStackTrace();
				}
				connectionFailed();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BluetoothService.this) {
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mSocket, mDevice, mSocketType);

			Log.d("BluetoothService", "mConnectThread run() END, SocketType:" + mSocketType);
		}

		public void cancel() {
			Log.d("BluetoothService", "mConnectThread cancel()");

			try {
				mSocket.close();
			} catch (IOException e) {
				Log.e("BluetoothService", "mConnectThread cancel() failed");
				e.printStackTrace();
			}
		}
	}


	/**
	 * This thread runs during a connection with a remote device.
	 * It handles all incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mSocket;
		private final InputStream mInputStream;
		private final OutputStream mOutputStream;
		private String mSocketType;

		// Nonin Device Fields
		private int dataFormatted;

		public ConnectedThread(BluetoothSocket socket, String socketType) {
			Log.d("BluetoothService", "mConnectedThread create()");

			mSocket = socket;
			mSocketType = socketType;
			InputStream tmpInputStream = null;
			OutputStream tmpOutputStream = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpInputStream = socket.getInputStream();
				tmpOutputStream = socket.getOutputStream();
			} catch (IOException e) {
				Log.e("BluetoothService", "mConnectedThread create() failed");
				e.printStackTrace();
			}

			mInputStream = tmpInputStream;
			mOutputStream = tmpOutputStream;
			assert mInputStream != null;
			assert mOutputStream != null;


			// Selecting the Data Format
			byte[] dataFormatCommand = {0x02, 0x70, 0x02, 0x02, 0x08, 0x03}; // Data Format 8
			try{
				mOutputStream.write(dataFormatCommand);
				mOutputStream.flush();
			} catch(Exception e) {
				e.printStackTrace();
			}

			byte[] dataFormatBuffer = new byte[1024];

			try{
				int bytesCanRead = mInputStream.available();
				int bytesRead = mInputStream.read(dataFormatBuffer);
				switch(dataFormatBuffer[0]) {
					case (byte) 0x06:
						dataFormatted = 1;
						break;
					case (byte) 0x15:
					default:
						dataFormatted = 0;
						break;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}

		}

		public void run() {
			Log.d("BluetoothService", "mConnectedThread run() BEGIN, SocketType:" + mSocketType);

			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = mInputStream.read(buffer);

					// Send the obtained bytes to the UI Activity
					mBluetoothHandler.obtainMessage(MESSAGE_READ, dataFormatted, bytes, buffer).sendToTarget();

				} catch (IOException e) {
					Log.e("BluetoothService", "cannot read from mInputStream");
					connectionLost();
					break;
				}
			}

			Log.d("BluetoothService", "mConnectedThread run() END, SocketType:" + mSocketType);
		}

		// write to OutputStream
		public void write(byte[] buffer) {
			try {
				mOutputStream.write(buffer);
				mBluetoothHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
			} catch (IOException e) {
				Log.e("BluetoothService", "cannot write to mOutputStream");
			}
		}

		public void cancel() {
			Log.d("BluetoothService", "mConnectedThread cancel()");

			try {
				mSocket.close();
			} catch (IOException e) {
				Log.e("BluetoothService", "mConnectedThread cancel() failed");
				e.printStackTrace();
			}
		}
	}
}
