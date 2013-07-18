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

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.formulaeditor.UserVariableShared;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Multiplayer {
	public static final String SHARED_VARIABLE_NAME = "shared_variable_name";
	public static final String SHARED_VARIABLE_VALUE = "shared_variable_value";
	public static final int STATE_CONNECTED = 1001;

	private static Multiplayer instance = null;
	private MultiplayerBtManager multiplayerBtManager = null;
	private static Handler btHandler;
	private static boolean initialized = false;
	private Handler receiverHandler;

	private Multiplayer() {
	}

	public static Multiplayer getInstance() {
		if (instance == null) {
			instance = new Multiplayer();
		}
		return instance;
	}

	public void setReceiverHandler(Handler recieverHandler) {
		this.receiverHandler = recieverHandler;
	}

	public void createBtManager(String mac_address) {

		if (!initialized) {
			if (multiplayerBtManager == null) {
				multiplayerBtManager = new MultiplayerBtManager();
				btHandler = multiplayerBtManager.getHandler();
				initialized = true;
			}

			multiplayerBtManager.connectToMACAddress(mac_address);
		}
		//move to multiplayerBtManger
		// everything was OK
		if (receiverHandler != null) {
			sendState(STATE_CONNECTED);
		}
	}

	public void createBtManager(BluetoothSocket btSocket) {
		if (multiplayerBtManager == null) {
			multiplayerBtManager = new MultiplayerBtManager();
			btHandler = multiplayerBtManager.getHandler();
			initialized = true;
		}

		multiplayerBtManager.createInputOutputStreams(btSocket);
	}

	public void destroyMultiplayerManager() {
		if (multiplayerBtManager != null) {
			multiplayerBtManager.destroyMultiplayerBTManager();
			multiplayerBtManager = null;
			initialized = false;
		}
	}

	public static synchronized void sendBtMessage(String name, double value) {
		if (initialized == false) {
			Log.e("Multiplayer", "not initialized yet");
			return;
		}

		Bundle myBundle = new Bundle();
		myBundle.putString(SHARED_VARIABLE_NAME, name);
		myBundle.putDouble(SHARED_VARIABLE_VALUE, value);
		Message myMessage = btHandler.obtainMessage();
		myMessage.setData(myBundle);
		btHandler.sendMessage(myMessage);
	}

	public static void updateSharedVariable(String name, Double value) {
		UserVariableShared sharedVariable = ProjectManager.getInstance().getCurrentProject().getUserVariables()
				.getSharedVariabel(name);
		if (sharedVariable != null) {
			sharedVariable.setValueWithoutSend(value);
		}
	}

	protected void sendState(int message) {
		Bundle myBundle = new Bundle();
		myBundle.putInt("message", message);
		Message myMessage = receiverHandler.obtainMessage();
		myMessage.setData(myBundle);
		receiverHandler.sendMessage(myMessage);
	}

}
