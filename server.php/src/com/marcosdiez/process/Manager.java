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
package com.marcosdiez.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class Manager {
	
	protected String[] find(File command) {
		File root = new File(File.separator + "proc");
		if (root.isDirectory()) {
			File[] list = root.listFiles();
			for (File file : list) {
				if (file.isDirectory()) {
					try {
						int pid = Integer.parseInt(file.getName());
						File fileCommandLine = new File(
							file.getAbsolutePath() + File.separator + "cmdline"
						);
						if (!fileCommandLine.isFile() || !fileCommandLine.canRead()) {
							continue;
						}
						BufferedReader reader = null;
						try {														
							reader = new BufferedReader(
								new InputStreamReader(new FileInputStream(fileCommandLine))
							);
							String line;
							while ((line = reader.readLine()) != null) {
								if (line.contains(command.getAbsolutePath())) {
									return new String[] {String.valueOf(pid), line};
								}
							}							
						} catch (IOException ex) {
						} finally {
							try {
								if (reader != null) {
									reader.close();
								}
							} catch (IOException ex) {}
						}
					} catch (NumberFormatException e) {}
				}
			}
		}
		return null;
	}
	
	public String[] getCommandLine(File command) {
		String[] commandLine = find(command);
		if (commandLine == null) {
			return null;
		}
		String part = "";
		List<String> parts = new LinkedList<String>();
		int length = commandLine[1].length();
		for (int i = 0; i < length; i++) {
			char letter = commandLine[1].charAt(i);
			if (i == length - 1 || letter == 0) {
				parts.add(part);
				part = "";
			} else {
				part += letter;
			}
		}
		return parts.toArray(new String[parts.size()]);
	}
	
	public boolean getIsRunning(File command) {
		return find(command) != null;
	}
	
	public void killIfFound(File command) {
		String[] commandLine = find(command);
		if (commandLine != null) {
			kill(Integer.parseInt(commandLine[0]));	
		}
	}
	
	public void kill(int pid) {
		android.os.Process.killProcess(pid);
	}	
	
}
