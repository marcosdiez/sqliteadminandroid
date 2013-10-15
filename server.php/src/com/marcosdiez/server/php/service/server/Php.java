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
package com.marcosdiez.server.php.service.server;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.marcosdiez.server.php.service.Network;
import com.marcosdiez.server.php.service.Preferences;
import com.marcosdiez.process.Manager;
import java.io.File;
import java.io.IOException;

public class Php extends HandlerThread {
	
	static public final String INTENT_ACTION = "STATUS_SERVER_CHANGED";
	
	private static Php instance = null;
		
	private java.lang.Process process = null;
	
	private File php = null;		
	
	private Handler handler = null;
	
	private Context context = null;
	
	private String address = "";
	
	private boolean startWhenReady = false;
	
	private Preferences preferences = null;
	
	private Network network = null;

	static public Php getInstance(Context context) {
		if (instance == null) {
			instance = new Php(context);
			instance.start();
		}
		return instance;
	}
	
	public File getPhp() {
		return php;
	}
	
	public Php(Context context) {
		super("PhpServer");
		network = new Network();
		preferences = new Preferences(context);
		this.context = context.getApplicationContext();
		php = new File(context.getFilesDir() + File.separator + "php");		
		address = getIPAddress() + ":" + preferences.getString(Preferences.PORT);
	}

	@Override
	protected void onLooperPrepared() {
		super.onLooperPrepared();
		handler = new Handler(getLooper()) {

			@Override
			public void handleMessage(Message message) {
				Bundle data = message.getData();
				if (data.get("action").equals("error")) {
					Intent intent = new Intent(INTENT_ACTION);		
					intent.putExtra("errorLine", data.getString("message"));
					context.sendBroadcast(intent);
				} else {
					if (data.get("action").equals("start")) {
						address = getIPAddress() + ":" + data.getString("port");
						serverStart(data.getString("documentRoot"));
					} else if (data.get("action").equals("stop")) {
						serverStop();
					}
					serverStatus();
				}				
			}
			
		};
		serverStatus();
		if (startWhenReady) {
			sendAction("start");
		}
	}
	
	public Handler getHandler() {
		return handler;
	}
	
	private String getIPAddress() {
		int position = network
			.getPosition(preferences.getString(Preferences.ADDRESS));
		return position == -1 ? "0.0.0.0" : network.getAddress(position);
	}
	
	private void serverStart(String documentRoot) {
		if (process == null) {
			try {
				File file = new File(documentRoot + File.separator + "php.ini");
				process = Runtime.getRuntime().exec(
					new String[] {
						php.getAbsolutePath(), "-S", address, "-t", documentRoot, 
							"-c", file.exists() ? file.getAbsolutePath() : documentRoot
					}
				);
				new StreamReader().execute(process.getErrorStream(), this);
			} catch (IOException ex) {}
		}
	}

	private void serverStop() {
		if (process != null) {
			process.destroy();
			process = null;
		}
		new Manager().killIfFound(php);
	}
	
	private void serverStatus() {
		boolean running = process != null;
		String realAddress = address;
		if (process == null) {
			String[] commandLine = new Manager().getCommandLine(php);			
			if (commandLine != null) {
				boolean next = false;
				for (String part : commandLine) {
					if (part.equals("-S")) {
						next = true;
					} else if (next) {
						realAddress = part;
						break;
					}
				}
				running = true;
			}
		}		
		Intent intent = new Intent(INTENT_ACTION);		
		intent.putExtra("running", running);
		if (running) {
			intent.putExtra("address", realAddress);
		}
		context.sendBroadcast(intent);
	}
	
	private void sendMessage(String action, Bundle bundle) {
		if (handler != null) {
			bundle.putString("action", action);
			Message message = new Message();
			message.setData(bundle);
			handler.sendMessage(message);
		}
	}
	
	public void sendErrorLine(String error) {
		if (handler != null) {
			Bundle bundle = new Bundle();
			bundle.putString("message", error);
			sendMessage("error", bundle);
		}
	}
	
	public void sendAction(String action) {
		if (handler != null) {
			Bundle bundle = new Bundle();			
			bundle.putString(
				Preferences.DOCUMENT_ROOT, 
				preferences.getString(Preferences.DOCUMENT_ROOT)
			);
			bundle.putString(
				Preferences.PORT, preferences.getString(Preferences.PORT)
			);
			sendMessage(action, bundle);
		}
	}
	
	public void startWhenReady() {
		if (handler == null) {
			startWhenReady = true;
		} else {
			sendAction("start");
		}
	}
	
}
