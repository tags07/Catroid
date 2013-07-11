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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class Multiplayer {
	public static final String SHARED_VARIABLE_NAME = "shared_variable_name";
	public static final String SHARED_VARIABLE_VALUE = "shared_variable_value";
	private MultiplayerBtManager multiplayerBtManager = null;
	private static Handler btHandler;
	private static boolean initialized = false;

	public Multiplayer() {

	}

	public void createBtManager(String mac_address) {

		if (multiplayerBtManager == null) {
			multiplayerBtManager = new MultiplayerBtManager();
			btHandler = multiplayerBtManager.getHandler();
			initialized = true;
		}

		multiplayerBtManager.connectToMACAddress(mac_address);
	}

	public static synchronized void sendBtMessage(String name, double value) {
		if (initialized == false) {
			return;
		}

		Bundle myBundle = new Bundle();
		myBundle.putString(SHARED_VARIABLE_NAME, name);
		myBundle.putDouble(SHARED_VARIABLE_VALUE, value);
		Message myMessage = btHandler.obtainMessage();
		myMessage.setData(myBundle);
		btHandler.sendMessage(myMessage);
	}

}
