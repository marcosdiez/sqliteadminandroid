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
package com.marcosdiez.server.php;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import com.marcosdiez.popup.DirectoryChooser;
import com.marcosdiez.server.php.service.Network;
import com.marcosdiez.server.php.service.server.Php;
import com.marcosdiez.server.php.service.Preferences;
import com.marcosdiez.server.php.service.install.InstallServer;
import java.io.File;

public class 
	MainActivity extends Activity implements InstallServer.OnInstallListener
{	
	
	static private int REQUEST_DIRECTORY = 1;
	
	private BroadcastReceiver receiver = null;
	
	private Preferences preferences = null;
	
	private Network network = null;
	
	private boolean requestResultView = false;
	
	private boolean requestResultViewSuccess = false;
	
	private boolean paused = true;
	
	private Preferences getPreferences() {
		if (preferences == null) {
			preferences = new Preferences(this);
		}
		return preferences;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);		
		if (savedInstanceState != null) {
			TextView text = (TextView)findViewById(R.id.error);
			text.setText(savedInstanceState.getString("errors"));
		}
		new InstallServer(this).installIfNeeded(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(
			"errors", ((TextView)findViewById(R.id.error)).getText().toString()
		);
	}
	
	private void setLabel(String label) {
		((TextView)findViewById(R.id.label)).setText(label);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		paused = false;
		if (requestResultView) {
			requestResultView = false;
			resultView();
		}
		if (receiver != null) {
			registerReceiver(receiver, new IntentFilter(Php.INTENT_ACTION));
		}
		Php.getInstance(MainActivity.this).sendAction("status");
	}

	@Override
	protected void onPause() {
		super.onPause();
		paused = true;
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}

	private void requestResultView() {
		if (paused) {
			requestResultView = true;
		} else {
			resultView();
		}
	}
	
	private void resultView() {
		if (requestResultViewSuccess) {
			startup();
		}
		findViewById(R.id.preloader).setVisibility(View.GONE);
		findViewById(R.id.preloader_container).setVisibility(View.GONE);
		findViewById(R.id.container).setVisibility(View.VISIBLE);
	}
	
	private void startup() {
		network = new Network();
		
		Spinner spinner = (Spinner)findViewById(R.id.server_interface);
		spinner.setAdapter(
			new ArrayAdapter(
				this, android.R.layout.simple_spinner_dropdown_item, network.getTitles()
			)
		);
        spinner.setSelection(network.getTitles().size()-1);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(
				AdapterView<?> parent, View view, int position, long id
			) {
				getPreferences().set(Preferences.ADDRESS, network.getName(position));
			}

			public void onNothingSelected(AdapterView<?> parent) {}

		});
		spinner.setSelection(
			network.getPosition(getPreferences().getString(Preferences.ADDRESS))
		);

		TextView text = (TextView)findViewById(R.id.server_root);		
		text.setText(getPreferences().getString(Preferences.DOCUMENT_ROOT));	
		text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				DirectoryChooser chooser = new DirectoryChooser(MainActivity.this);
				chooser.setParent(
					new File(getPreferences().getString(Preferences.DOCUMENT_ROOT))
				);
				chooser.setOnDirectoryChooserListener(
					new DirectoryChooser.OnDirectoryChooserListener() {
						public void OnDirectoryChosen(File directory) {
							getPreferences().set(
								Preferences.DOCUMENT_ROOT, directory.getAbsolutePath()
							);
							((TextView)findViewById(R.id.server_root)).setText(
								getPreferences().getString(Preferences.DOCUMENT_ROOT)
							);
						}
					}
				);
				chooser.show();				
			}
		});
		text = (TextView)findViewById(R.id.server_port);
		text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView text, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(text.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});
		text.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

			public void afterTextChanged(Editable text) {
				String portPreference = getPreferences().getString(Preferences.PORT);
				int port = portPreference.isEmpty() ? 
					8080 : Integer.parseInt(portPreference);
				try {
					port = Integer.parseInt(text.toString());					
				} catch (NumberFormatException e) {}
				boolean error = true;
				if (port >= 1024 && port <= 65535) {
					getPreferences().set(Preferences.PORT, String.valueOf(port));
					error = false;
				}
				((TextView)findViewById(R.id.server_port))
					.setTextColor(error ? Color.RED : Color.BLACK);
			}
		});
		text.setText(getPreferences().getString(Preferences.PORT));
		CheckBox checkbox = (CheckBox)findViewById(R.id.server_start_on_boot);
		checkbox.setOnCheckedChangeListener(
			new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(
					CompoundButton checkox, boolean checked
				) {
					getPreferences().set(Preferences.START_ON_BOOT, checked);
				}
			}
		);
		checkbox.setChecked(
			getPreferences().getBoolean(Preferences.START_ON_BOOT)
		);

		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(Php.INTENT_ACTION)) {
					Bundle extras = intent.getExtras();
					if (extras.containsKey("errorLine")) {
						TextView text = (TextView)findViewById(R.id.error);
						text.append(
							(text.getText().length() > 0 ? "\n" : "") + 
								extras.getString("errorLine")
						);
					} else {
						findViewById(R.id.start).setVisibility(View.GONE);
						findViewById(R.id.stop).setVisibility(View.GONE);
						if (extras.getBoolean("running")) {
							findViewById(R.id.stop).setVisibility(View.VISIBLE);
							setLabel(
								String.format(
									getString(R.string.server_running), extras.getString("address")
								)
							);
						} else {
							findViewById(R.id.start).setVisibility(View.VISIBLE);
							setLabel(getString(R.string.server_stopped));
						}
					}					
				}
			}

		};

		findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				((TextView)findViewById(R.id.error)).setText("");
				Php.getInstance(MainActivity.this).sendAction("start");
			}
		});

		findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Php.getInstance(MainActivity.this).sendAction("stop");
			}
		});
		
		registerReceiver(receiver, new IntentFilter(Php.INTENT_ACTION));
		Php.getInstance(MainActivity.this).sendAction("status");
	}
	
	public void OnInstallStart() {}

	public void OnInstallEnd(boolean success) {
		requestResultViewSuccess = success;
		requestResultView();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_about) {
			final Handler handler = new Handler();
			final View view = getLayoutInflater().inflate(R.layout.about, null);
			WebView htmlView = (WebView)view.findViewById(R.id.text);
			htmlView.getSettings().setJavaScriptEnabled(true);
			htmlView.setWebViewClient(new WebViewClient() {

				@Override
				public void onPageFinished(WebView v, String url) {
					handler.postDelayed(
						new Runnable() {
							public void run() {
								view.findViewById(R.id.text).setVisibility(View.VISIBLE);
								view.findViewById(R.id.preloader).setVisibility(View.GONE);
								view.findViewById(R.id.preloader_container).setVisibility(View.GONE);
							}
						}, 1500
					);
				}
				
				@Override
				public boolean shouldOverrideUrlLoading(WebView v, String url) {
					v.getContext().startActivity(
						new Intent(Intent.ACTION_VIEW, Uri.parse(url))
					);
					return true;
				}
				
			});
			htmlView.loadUrl("file:///android_asset/About.html");
			new AlertDialog.Builder(this)
				.setNegativeButton(getString(R.string.close), null)
				.setView(view)
				.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
