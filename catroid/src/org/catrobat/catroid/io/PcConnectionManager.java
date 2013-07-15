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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.bricks.SendToPcBrick;
import org.catrobat.catroid.content.bricks.SendToPcBrick.ConnectionRequest;
import org.catrobat.catroid.io.Connection.connectionState;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class PcConnectionManager implements ConnectionRequest {
	private static PcConnectionManager instance = null;
	private final static int defaultSocketTimeout = 30;
	private final static int port = 64000;
	private static HashMap<String, Connection> connectionList;//zusammenlegen
	private static ArrayList<SendToPcBrick> connectionRequestList;
	private static ArrayList<String> ipRequestList;//zusammenlegen
	private static ArrayList<String> availableIpList;
	private static Context context;
	private static int socketTimeout;
	private ProgressDialog connectingProgressDialog;

	private PcConnectionManager() {
		initialize();
		socketTimeout = defaultSocketTimeout;
	}

	public static PcConnectionManager getInstance(Context context_) {
		if (instance == null) {
			instance = new PcConnectionManager();
		}
		if (context_ != null) {
			context = context_;
		}
		return instance;
	}

	public static void initialize() {
		connectionList = new HashMap<String, Connection>();
		connectionRequestList = new ArrayList<SendToPcBrick>();
		ipRequestList = new ArrayList<String>();
		availableIpList = new ArrayList<String>();
	}

	public void broadcast() {
		BroadcastThread broadcastThread = new BroadcastThread();
		broadcastThread.start();
	}

	public class BroadcastThread extends Thread {
		@Override
		public void run() {
			Activity activity = (Activity) context;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					connectingProgressDialog = ProgressDialog.show(context, "", context.getString(R.string.scan_wait),
							true);
				}
			});

			// BROADCAST----------------------------------------------------------------
			byte[] message = new byte[1];
			String broadcastAdressString = getBroadcastAddress();
			InetAddress broadcastAddressInet = stringToInetAddress(broadcastAdressString);
			DatagramPacket dataPacket = null;
			if (broadcastAddressInet != null) {
				dataPacket = new DatagramPacket(message, message.length, broadcastAddressInet, port);
			}
			HashMap<String, String> brickIpList = new HashMap<String, String>();
			DatagramSocket dataSocket = null;
			try {
				dataSocket = new DatagramSocket();
				dataSocket.setSoTimeout(3000);
			} catch (SocketException e) {
				e.printStackTrace();
				finishBroadcast(brickIpList);
				return;
			}
			try {
				dataSocket.setBroadcast(true);
				dataSocket.send(dataPacket);
			} catch (IOException e) {
				e.printStackTrace();
				dataSocket.close();
				finishBroadcast(brickIpList);
				return;
			}

			// RECEIVE-------------------------------------------------------------------
			byte[] ip_addr_server = new byte[64];
			DatagramPacket data_rec = new DatagramPacket(ip_addr_server, ip_addr_server.length);
			while (true) {
				try {
					dataSocket.receive(data_rec);
					String ip_server = new String(ip_addr_server, 0, data_rec.getLength());
					brickIpList.put(ip_server, data_rec.getSocketAddress().toString());
				} catch (IOException e) {
					//e.printStackTrace();
					if (brickIpList.size() == 0) {
						activity = (Activity) context;
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(context, R.string.no_devices_found, Toast.LENGTH_SHORT).show();
							}
						});
					} else {
					}
					dataSocket.close();
					break;
				}
			}
			finishBroadcast(brickIpList);
		}
	};

	public void finishBroadcast(HashMap<String, String> brickIpList) {
		Activity act = (Activity) context;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				connectingProgressDialog.dismiss();
			}
		});
		updateBricks(brickIpList);
		fillActualIpList(brickIpList);
	}

	public String getBroadcastAddress() {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		int ipAddress = 0;
		int netmask = 0;
		ipAddress = wifiManager.getConnectionInfo().getIpAddress();
		//when hotspot activated, we need to hack a little to get ipadress and netmask
		if (ipAddress == 0) {
			InetAddress inetAddress = getIpForHotspot();
			if (inetAddress != null) {
				ipAddress = InetAddrToInt(inetAddress);
				netmask = getNetMaskForHotspot(inetAddress);
			} else {
				return null;
			}
		} else {
			netmask = wifiManager.getDhcpInfo().netmask;
		}
		int hostBits = ~netmask;
		int broadcastAddressInt = hostBits | ipAddress;
		String broadcastAddress = intToIPAddress(broadcastAddressInt);
		return broadcastAddress;
	}

	@SuppressLint("DefaultLocale")
	public String intToIPAddress(int ipInt) {
		return String.format("%d.%d.%d.%d", (ipInt & 0xff), (ipInt >> 8 & 0xff), (ipInt >> 16 & 0xff),
				(ipInt >> 24 & 0xff));
	}

	@SuppressLint("NewApi")
	public int getNetMaskForHotspot(InetAddress inetAddress) {
		NetworkInterface netWork;
		netWork = null;
		try {
			netWork = NetworkInterface.getByInetAddress(inetAddress);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		int numOfHostbits = netWork.getInterfaceAddresses().get(0).getNetworkPrefixLength();
		int maskTemp = 0xFFFFFFFF << (32 - numOfHostbits);
		int mask = 0;
		mask |= (((maskTemp & 0xFF000000) >> 24 & 0xFF));
		mask |= ((maskTemp & 0x00FF0000) >> 8 & 0xFF00);
		mask |= ((maskTemp & 0x0000FF00) << 8);
		mask |= ((maskTemp & 0x000000FF) << 24);
		return mask;
	}

	public int InetAddrToInt(InetAddress inetAddress) {
		int inetAddressInt = 0;
		byte[] inet_byte = inetAddress.getAddress();
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			inetAddressInt += (inet_byte[3 - i] & 0x000000FF) << shift;
		}
		return inetAddressInt;
	}

	public InetAddress getIpForHotspot() {
		InetAddress hotspotAddress = null;
		try {
			InetAddress inetAddress = null;
			for (Enumeration<NetworkInterface> networkInterface = NetworkInterface.getNetworkInterfaces(); networkInterface
					.hasMoreElements();) {
				NetworkInterface singleInterface = networkInterface.nextElement();
				for (Enumeration<InetAddress> IpAddresses = singleInterface.getInetAddresses(); IpAddresses
						.hasMoreElements();) {
					inetAddress = IpAddresses.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& (singleInterface.getDisplayName().contains("wlan0") || singleInterface.getDisplayName()
									.contains("eth0"))) {
						hotspotAddress = inetAddress;
					}
				}
			}
		} catch (SocketException ex) {
			ex.getMessage();
		}
		return hotspotAddress;
	}

	public InetAddress stringToInetAddress(String broadcastAddress) {
		InetAddress broadcastAddressInet = null;
		try {
			broadcastAddressInet = InetAddress.getByName(broadcastAddress);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		return broadcastAddressInet;
	}

	public void updateBricks(HashMap<String, String> brickIpList) {
		Iterator<SendToPcBrick> it = connectionRequestList.iterator();
		SendToPcBrick brick;
		while (it.hasNext()) {
			brick = it.next();
			brick.setIpList(brickIpList);
		}
	}

	public void getRequestedIps() {

		Iterator<SendToPcBrick> it = connectionRequestList.iterator();
		ipRequestList.clear();
		SendToPcBrick brick = null;
		String ip;
		while (it.hasNext()) {
			brick = it.next();
			ip = brick.getIp();
			if (!ipRequestList.contains(ip)) {
				ipRequestList.add(ip);
			}
		}

	}

	public boolean setUpConnections() {
		Iterator<String> it = ipRequestList.iterator();
		String ip;
		Connection newConnection;
		boolean returnValue = false;
		while (it.hasNext()) {
			ip = it.next();
			newConnection = new Connection(ip, this);
			newConnection.start();
			while (true) {
				connectionState state = newConnection.getConnectionState();
				switch (state) {
					case UNDEFINED:
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						continue;
					case CONNECTED:
						returnValue = true;
						break;
					case UNCONNECTED:
						returnValue = false;
						cancelConnections();
						break;
					default:
						Log.w("PcConnectionManager", "Not handled ConnectionState");
				}
				break;
			}
			connectionList.put(ip, newConnection);
		}
		return returnValue;
	}

	public void cancelConnections() {
		Iterator<Entry<String, Connection>> it = connectionList.entrySet().iterator();
		Connection connection;
		while (it.hasNext()) {
			String ip = it.next().getKey();
			connection = connectionList.get(ip);
			connection.stopThread();
		}
		connectionList.clear();
	}

	public boolean addConnectionsToBricks() {
		Iterator<SendToPcBrick> it = connectionRequestList.iterator();
		SendToPcBrick brick;
		while (it.hasNext()) {
			brick = it.next();
			Connection connection = findCconnectionByIp(brick.getIp());
			if (connection != null) {
				brick.setConnection(connection);
			} else {
				return false;
			}
		}
		return true;
	}

	public Connection findCconnectionByIp(String ip) {
		return connectionList.get(ip);
	}

	public void fillActualIpList(HashMap<String, String> brickIpList) {
		Iterator<Entry<String, String>> it = brickIpList.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> pairs = it.next();
			String this_element = pairs.getKey();
			if (!availableIpList.contains(this_element)) {
				availableIpList.add(this_element);
			}
		}
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void increaseSocketTimeout() {
		if (socketTimeout < 3000) {
			socketTimeout *= 10;
		}
	}

	@Override
	public void addToConnectionRequestList(SendToPcBrick brick) {
		if (!connectionRequestList.contains(brick)) {
			connectionRequestList.add(brick);
		}
	}

	@Override
	public ArrayList<String> getActualIpList() {
		return availableIpList;
	}

	public interface Broadcast {
		public void setIpList(HashMap<String, String> ip_list);

		public void setConnection(Connection connection);
	}
}
