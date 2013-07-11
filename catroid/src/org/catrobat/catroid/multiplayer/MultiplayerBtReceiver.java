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

import android.util.Log;

public class MultiplayerBtReceiver extends Thread {
	private InputStream btInStream = null;

	public MultiplayerBtReceiver(InputStream btInStream) {
		this.btInStream = btInStream;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[1024];
		int receivedbytes = 0;

		while (receivedbytes >= 0) {
			try {
				receivedbytes = btInStream.read(buffer);

				String receivedMessage = new String(buffer, 0, receivedbytes, "ASCII");
				int startIndexValue = receivedMessage.indexOf(":") + 1;
				String variableName = new String(receivedMessage.substring(0, startIndexValue - 2));
				Double variableValue = ByteBuffer.wrap(buffer).getDouble(startIndexValue);
				Log.d("BT Receiver", variableName + ":" + variableValue);
				Multiplayer.updateSharedVariable(variableName, variableValue);
				//mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
			} catch (IOException e) {
				// TODO close all sockets
				break;
			}
		}

	}
}
