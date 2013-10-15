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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {

	static public String DOCUMENT_ROOT = "documentRoot";
	
	static public String ADDRESS = "address";
	
	static public String PORT = "port";
	
	static public String START_ON_BOOT = "startOnBoot";
	
	private SharedPreferences preferences = null;
	
	public Preferences(Context context) {
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public void set(String name, boolean value) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(name, value);
		editor.commit();
	}
	
	public void set(String name, String value) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(name, value);
		editor.commit();
	}
	
	public String getString(String name) {
		return preferences.getString(name, "");
	}
	
	public boolean getBoolean(String name) {
		return preferences.getBoolean(name, false);
	}
	
	public boolean contains(String name) {
		return preferences.contains(name);
	}
	
}
