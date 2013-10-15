/**
 * Copyright 2013 Tautvydas Andrikys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.marcosdiez.server.php.service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.conn.util.InetAddressUtils;

public class Network {
	
	private List<String> names = null;
	
	private List<String> adresses = null;
	
	private List<String> titles = null;
	
	public Network() {
		names = new LinkedList<String>();
		titles = new LinkedList<String>();		
		adresses = new LinkedList<String>();		
		try {
			List<NetworkInterface> list = Collections.list(
				NetworkInterface.getNetworkInterfaces()
			);
			for (NetworkInterface iface : list) {
				List<InetAddress> addresses = Collections.list(
					iface.getInetAddresses()
				);
				for (InetAddress address : addresses) {
					if (!address.isLoopbackAddress()) {
						String host = address.getHostAddress().toUpperCase();
						if (InetAddressUtils.isIPv4Address(host)) {
							names.add(iface.getName());
							titles.add(host + "(" + iface.getDisplayName() + ")");
							adresses.add(host);
						}
					}
				}
			}
			for (NetworkInterface iface : list) {
				List<InetAddress> addresses = Collections.list(
					iface.getInetAddresses()
				);
				for (InetAddress address : addresses) {
					if (address.isLoopbackAddress()) {
						String host = address.getHostAddress().toUpperCase();
						if (InetAddressUtils.isIPv4Address(host)) {
							names.add(iface.getName());
							titles.add(host + "(" + iface.getDisplayName() + ")");
							adresses.add(host);
						}
					}
				}
			}
		} catch (Exception ex) {}
		names.add("all");
		adresses.add("0.0.0.0");
		titles.add("0.0.0.0 (all)");
	}
	
	public int getPosition(String name) {
		for (int i = 0; i < names.size(); i++) {
			if (names.get(i).equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	public List<String> getTitles() {
		return titles;
	}
	
	public String getTitle(int position) {
		return titles.get(position);
	}
	
	public List<String> getNames() {
		return names;
	}
	
	public String getName(int position) {
		return names.get(position);
	}
	
	public List<String> getAdresses() {
		return adresses;
	}
	
	public String getAddress(int position) {
		return adresses.get(position);
	}
	
}
