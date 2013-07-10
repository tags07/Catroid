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
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

public class MultiplayerBtManager {
	private static final UUID CONNECTION_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final String MULTIPLAYER_BT_CONNECT = "multiplayerBTConnect";

	private MultiplayerBtReceiver receiver = null;
	private BluetoothAdapter btAdapter = null;
	private BluetoothServerSocket btServerSocket = null;
	private BluetoothSocket btSocket = null;

	public MultiplayerBtManager() {
		this.btAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public void connectToMACAddress(String mac_address) {
		BluetoothDevice multiplayerDevice = null;
		multiplayerDevice = btAdapter.getRemoteDevice(mac_address);
		try {
			btSocket = multiplayerDevice.createRfcommSocketToServiceRecord(CONNECTION_UUID);
			btSocket.connect();
		} catch (IOException e) {
			e.printStackTrace();
			btSocket = null;
		}
		if (btSocket == null) {
			try {
				btServerSocket = btAdapter.listenUsingRfcommWithServiceRecord(MULTIPLAYER_BT_CONNECT, CONNECTION_UUID);
				btSocket = btServerSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			InputStream instream = btSocket.getInputStream();
			receiver = new MultiplayerBtReceiver(instream);
			receiver.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String message) {
		try {
			btSocket.getOutputStream().write(message.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
