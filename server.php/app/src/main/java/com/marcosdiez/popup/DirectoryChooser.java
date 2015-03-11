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
package com.marcosdiez.popup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;

public class DirectoryChooser extends AlertDialog {

	public interface OnDirectoryChooserListener {
		
		public void OnDirectoryChosen(File directory);
		
	}
	
	private DirectoryChooserManager manager = new DirectoryChooserManager();
	private File parent = Environment.getExternalStorageDirectory();
	private ListView listView = null;
	private OnShowListener listenerShow = null;
	private OnDirectoryChooserListener listener = null;
	private boolean firstShow = true;
	private int padding = 0;
	
	public DirectoryChooser(Context context) {
		super(context);
		padding = (int)TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics()
		);
		setView(listView = new ListView(context));
		setButton(Dialog.BUTTON_NEUTRAL, "Go up", (OnClickListener)null);
		setButton(Dialog.BUTTON_NEGATIVE, "Cancel", (OnClickListener)null);
		setButton(
			Dialog.BUTTON_POSITIVE, "Choose", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (listener != null) {
						listener.OnDirectoryChosen(parent);
					}
				}
			}
		);				
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(
				AdapterView<?> parent, View view, int position, long id
			) {
				setParent((File)listView.getAdapter().getItem(position));
			}
		});
		listView.setAdapter(
			new ArrayAdapter<File>(context, 0) {				
				
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					if (convertView == null) {
						convertView = new TextView(getContext());
						convertView.setPadding(padding, padding, padding, padding);
					}
					((TextView)convertView).setText(((File)getItem(position)).getName());
					return convertView;
				}
			}
		);		
		super.setOnShowListener(new OnShowListener() {
			public void onShow(DialogInterface dialog) {
				if (firstShow) {
					firstShow = false;
					initialize();
				}
				if (listenerShow != null) {
					listenerShow.onShow(dialog);
				}
			}
		});
		setTitle("?");
	}
	
	private void initialize() {		
		getButton(BUTTON_NEUTRAL).setOnClickListener(
			new View.OnClickListener() {
				public void onClick(View view) {
					setParent(parent.getParentFile());
				}
			}
		);
		setParent(parent);
	}
	
	public void setButtonGoUpTitle(String title) {
		getButton(BUTTON_NEUTRAL).setText(title);
	}
	
	public void setButtonChooseTitle(String title) {
		getButton(BUTTON_POSITIVE).setText(title);
	}
	
	public void setButtonCancelTitle(String title) {
		getButton(BUTTON_NEGATIVE).setText(title);
	}
	
	public void setParent(File parent) {
		this.parent = parent;
		if (!firstShow) {
			getButton(BUTTON_NEUTRAL).setVisibility(
				parent.getParentFile() != null ? View.VISIBLE : View.GONE
			);
			setTitle(parent.getAbsolutePath());
			ArrayAdapter<File> adapter = (ArrayAdapter<File>)listView.getAdapter();
			ArrayList<File> list = manager.getSubdicrectories(parent);
			adapter.clear();
			for (File file : list) {
				adapter.add(file);
			}
			adapter.notifyDataSetChanged();
		}		
	}

	@Override
	public void setOnShowListener(OnShowListener listener) {
		listenerShow = listener;
	}
	
	public void setOnDirectoryChooserListener(
		OnDirectoryChooserListener listener
	) {
		this.listener = listener;
	}
	
}
