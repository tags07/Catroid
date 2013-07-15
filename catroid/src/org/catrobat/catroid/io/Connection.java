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
package org.catrobat.catroid.io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Connection extends Thread {
	public static enum connectionState {
		UNDEFINED, CONNECTED, UNCONNECTED
	};

	private String ip;
	private Socket client;
	private OutputStream output;
	private ObjectOutputStream objectOutput;
	private ArrayList<Command> commandList;
	private connectionState state;
	private PcConnectionManager connectionManager;
	private Connection thisThread;

	public Connection(String ip_, PcConnectionManager connect) {
		ip = ip_;
		client = null;
		state = connectionState.UNDEFINED;
		connectionManager = connect;
		thisThread = this;
	}

	@Override
	public void run() {
		initialize();
		while (thisThread == this) {
			if (commandList.size() > 0) {
				sendCommand();
			} else {
				Thread.yield();
			}

			//TODO handle in/output
		}
	}

	public void initialize() {
		output = null;
		objectOutput = null;
		commandList = new ArrayList<Command>();
		try {
			if (ip != null) {
				client = new Socket();
				client.connect(new InetSocketAddress(ip, 63000), connectionManager.getSocketTimeout());
			} else {
				state = connectionState.UNCONNECTED;
				return;
			}
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			state = connectionState.UNCONNECTED;
			return;
		} catch (IOException e1) {
			e1.printStackTrace();
			state = connectionState.UNCONNECTED;
			return;
		}
		state = connectionState.CONNECTED;
		try {
			output = client.getOutputStream();
			objectOutput = new ObjectOutputStream(output);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

	public connectionState getConnectionState() {
		return state;
	}

	public void sendCommand() {
		Command actual_command = commandList.get(0);
		try {
			objectOutput.writeObject(actual_command);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		commandList.remove(actual_command);
	}

	public void stopThread() {
		thisThread = null;
		try {
			if (output != null) {
				output.close();
			}
			if (objectOutput != null) {
				objectOutput.close();
			}
			if (client != null) {
				client.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
