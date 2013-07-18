/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.multiplayer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MultiplayerBtManager {
	public static final UUID CONNECTION_UUID = UUID.fromString("a4eae22c-ac9d-424b-8e28-8b9fbc3d814f");
	public static final String MULTIPLAYER_BT_CONNECT = "multiplayerBTConnect";

	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private InputStream btInStream = null;
	private boolean connected = false;

	public MultiplayerBtManager() {
		this.btAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public void connectToMACAddress(String mac_address) {
		BluetoothDevice multiplayerDevice = null;
		btAdapter.cancelDiscovery();
		multiplayerDevice = btAdapter.getRemoteDevice(mac_address);

		if (!connected) {
			try {
				btSocket = multiplayerDevice.createRfcommSocketToServiceRecord(CONNECTION_UUID);
				btSocket.connect();
			} catch (IOException e) {
				Log.d("Bluetooth", "socket exeption");
				e.printStackTrace();
				btSocket = null;
			}

			connected = true;
		}

		try {
			btInStream = btSocket.getInputStream();
			//			receiver = new MultiplayerBtReceiver(instream);
			messageReceiver.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createInputOutputStreams(BluetoothSocket btSocket) {
		connected = true;
		this.btSocket = btSocket;

		try {
			btInStream = btSocket.getInputStream();
			//			receiver = new MultiplayerBtReceiver(instream);
			messageReceiver.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void destroyMultiplayerBTManager() {
		connected = false;
		if (btSocket != null) {
			try {
				btSocket.close();
			} catch (IOException e) {
				Log.e("Multiplayer", "Socket pointer not NULL, but couldn't be closed!");
			}
			btSocket = null;
		}
		btAdapter = null;
		btInStream = null;
	}

	public void sendBtMessage(Bundle message) {
		try {
			byte[] bytes = new byte[1024];
			String variableName = message.getString(Multiplayer.SHARED_VARIABLE_NAME) + ":";
			ByteBuffer.wrap(bytes).put(variableName.getBytes());
			ByteBuffer.wrap(bytes).putDouble(variableName.length(),
					message.getDouble(Multiplayer.SHARED_VARIABLE_VALUE));
			btSocket.getOutputStream().write(bytes, 0, variableName.length() + 8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Handler getHandler() {
		return myHandler;
	}

	@SuppressLint("HandlerLeak")
	private final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message myMessage) {
			sendBtMessage(myMessage.getData());
		}
	};

	private final Thread messageReceiver = new Thread() {
		@Override
		public void run() {
			byte[] buffer = new byte[1024];
			int receivedbytes = 0;

			while (true) {
				try {
					receivedbytes = btInStream.read(buffer);
					if (receivedbytes < 0) {
						break;
					}

					String receivedMessage = new String(buffer, 0, receivedbytes, "ASCII");
					int startIndexValue = receivedMessage.indexOf(":") + 1;
					String variableName = new String(receivedMessage.substring(0, startIndexValue - 1));
					Double variableValue = ByteBuffer.wrap(buffer).getDouble(startIndexValue);
					Log.d("BT Receiver", variableName + ":" + variableValue);
					Log.d("BT Receiver", "-" + variableName + "-");
					Log.d("BT Receiver", "-" + variableValue + "-");
					Multiplayer.updateSharedVariable(variableName, variableValue);

				} catch (IOException e) {
					Log.d("Multiplayer", "Receiver Thread END");
					break;
				}
			}
		}
	};

}
