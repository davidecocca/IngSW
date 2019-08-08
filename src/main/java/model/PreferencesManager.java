package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

//Handles application preferences file
public class PreferencesManager {

	//View Mode (0 -> SplitView, 1 -> TableView)
	private int viewMode;
	//Color Scheme (0 -> Green, 1 -> Blue, 2 -> Red)
	private int colorScheme;
	//Preferences file
	private File prefFile;
	//PreferencesManager instance
	private static PreferencesManager instance;

	public static synchronized PreferencesManager getInstance() throws IOException {
		if (instance == null) {
			instance = new PreferencesManager();
		}
		return instance;
	}

	private PreferencesManager() throws IOException {
		//Preferences file
		this.prefFile = new File("MyPM_preferences.txt");

		//If preferences file doesn't exist, create it
		if (!prefFile.exists()) {
			prefFile.createNewFile();
			//Set view mode to split view and color scheme to green
			this.viewMode = 0;
			this.colorScheme = 0;
			//Write preferences to file
			writeToFile();
		}
		//Otherwise read preferences from file
		else {
			readFromFile();
		}
	}

	//Get view mode
	public int getViewMode() {
		return this.viewMode;
	}

	//Get color scheme
	public int getColorScheme() {
		return this.colorScheme;
	}

	//Set view mode
	public void setViewMode(int viewMode) throws InvalidViewModeException, IOException {
		//If viewMode is valid, set it and update file
		if (viewMode == 0 || viewMode == 1) {
			this.viewMode = viewMode;
			writeToFile();
		}
		//Otherwise throw an exception
		else {
			throw new InvalidViewModeException();
		}
	}

	//Set color scheme
	public void setColorScheme(int colorScheme) throws InvalidColorSchemeException, IOException {
		//If colorScheme is valid, set it and update file
		if (colorScheme == 0 || colorScheme == 1 || colorScheme == 2) {
			this.colorScheme = colorScheme;
			writeToFile();
		}
		//Otherwise throw an exception
		else {
			throw new InvalidColorSchemeException();
		}
	}

	//Write preferences to file
	private void writeToFile() throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prefFile), "UTF-8"));
		bw.write(Integer.toString(this.viewMode));
		bw.newLine();
		bw.write(Integer.toString(this.colorScheme));
		bw.close();
	}

	//Read preferences from file
	private void readFromFile() throws IOException {
		BufferedReader br = new BufferedReader((new InputStreamReader(new FileInputStream(prefFile), "UTF-8")));
		//Read first line from file
		String vMode = br.readLine();
		//If first line is valid
		if (vMode != null) {
			//Get view mode
			int view = Integer.parseInt(vMode);
			//if view mode is valid
			if (view == 0 || view == 1) {
				//Set view mode
				this.viewMode = view;
				//Read second line from file
				String cScheme = br.readLine();
				//If second line is valid
				if (cScheme != null) {
					//Get color scheme
					int color = Integer.parseInt(cScheme);
					//If color scheme is valid
					if (color == 0 || color == 1 || color == 2) {
						//Set color scheme
						this.colorScheme = color;
						br.close();
					}
					//If color scheme is not valid, set it to green and update preferences file
					else {
						this.colorScheme = 0;
						br.close();
						writeToFile();
					}
				}
				//If second line is not valid, set color scheme to green and update preferences file
				else {
					this.colorScheme = 0;
					br.close();
					writeToFile();
				}
			}
			//if view mode is not valid, set it to split view
			else {
				this.viewMode = 0;
				//Read second line from file
				String cScheme = br.readLine();
				//If second line is valid
				if (cScheme != null) {
					//Get color scheme
					int color = Integer.parseInt(cScheme);
					//If color scheme is valid
					if (color == 0 || color == 1 || color == 2) {
						//Set color scheme
						this.colorScheme = color;
						br.close();
						//Update preferences file
						writeToFile();
					}
					//If color scheme is not valid, set it to green and update preferences file
					else {
						this.colorScheme = 0;
						br.close();
						writeToFile();
					}
				}
				//If second line is not valid, set color scheme to green and update preferences file
				else {
					this.colorScheme = 0;
					br.close();
					writeToFile();
				}
			}
		}
		//If fist line is not valid, set view mode to split view, set color scheme to green and update preferences file
		else {
			this.viewMode = 0;
			this.colorScheme = 0;
			br.close();
			writeToFile();
		}

	}


	//Exception for invalid view mode
	public static class InvalidViewModeException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public InvalidViewModeException() {

		}

		public InvalidViewModeException(String message, Throwable throwable) {
			super(message, throwable);
		}
	}

	//Exception for invalid color scheme
	public static class InvalidColorSchemeException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public InvalidColorSchemeException() {

		}

		public InvalidColorSchemeException(String message, Throwable throwable) {
			super(message, throwable);
		}
	}

}
