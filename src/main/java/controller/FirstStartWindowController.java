package controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.PasswordManager;
import model.PreferencesManager;

import util.CryptoUtils.CryptoException;

public class FirstStartWindowController {

	//First password field
	@FXML
	private PasswordField firstPasswordField;
	//Second password field
	@FXML
	private PasswordField secondPasswordField;
	//Invalid password label
	@FXML
	private Label validPasswordLabel;

	//PasswordManager instance
	private PasswordManager pm;

	//PreferencesManager instance
	private PreferencesManager prefM;

	public FirstStartWindowController() {
		//Get an instance of PasswordManager
		try {
			pm = PasswordManager.getInstance();
		} catch (IOException e) {
			//Create an alert if an error happens while creating database file
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText("An error occurred while creating database file!");
			alert.show();
			e.printStackTrace();
		}

		//Get an instance of PreferencesManager()
		try {
			prefM = PreferencesManager.getInstance();
		} catch (IOException e) {
			//Create an alert if an error happens while creating preferences file
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText("An error occurred while creating preferences file!");
			alert.show();
			e.printStackTrace();
		}
	}

	@FXML
	private void start() {
		//Get first password string
		String firstPassword = firstPasswordField.getText();
		//Get second password string
		String secondPassword = secondPasswordField.getText();

		/* Check if the password is at least 8-character long and firstPassword is equals to secondPassword,
		 otherwise show an error message */
		if(firstPassword.length() >= 8 && firstPassword.equals(secondPassword)) {
			try {

				//Init PasswordManager
				pm.init(firstPassword);

				//Get view mode
				int viewMode = prefM.getViewMode();
				//Get color scheme
				int colorScheme = prefM.getColorScheme();

				//Show split view
				if (viewMode == 0) {
					//Green color scheme
					if (colorScheme == 0) {
						showSplitViewWindow("/view/green_application.css");
					}
					//Blue color scheme
					else if(colorScheme == 1) {
						showSplitViewWindow("/view/blue_application.css");
					}
					//Red color scheme
					else {
						showSplitViewWindow("/view/red_application.css");
					}
				}

				//Show table view
				else {
					//Green color scheme
					if (colorScheme == 0) {
						showTableViewWindow("/view/green_application.css");
					}
					//Blue color scheme
					else if(colorScheme == 1) {
						showTableViewWindow("/view/blue_application.css");
					}
					//Red color scheme
					else {
						showTableViewWindow("/view/red_application.css");
					}
				}

			} catch (CryptoException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			validPasswordLabel.setVisible(true);
		}
	}

	@FXML
	private void cancel() {
		//Close the window
		Stage stage = (Stage) firstPasswordField.getScene().getWindow();
		stage.close();
	}

	//Show split view window
	private void showSplitViewWindow(String colorScheme) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SplitViewWindow.fxml"));
		BorderPane root = null;
		try {
			root = (BorderPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Stage mainStage = new Stage();
		mainStage.setScene(new Scene(root, 700, 500));
		mainStage.setTitle("MyPM");
		mainStage.setMinWidth(700);
		mainStage.setMinHeight(520);
		//Set stylesheet CSS
		mainStage.getScene().getStylesheets().add(colorScheme);
		mainStage.show();
		root.requestFocus(); //makes focus go to the root of the scene
		cancel();
	}

	//Show table view window
	private void showTableViewWindow(String colorScheme) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TableViewWindow.fxml"));
		BorderPane root = null;
		try {
			root = (BorderPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Stage mainStage = new Stage();
		mainStage.setScene(new Scene(root, 700, 500));
		mainStage.setTitle("MyPM");
		mainStage.setMinWidth(700);
		mainStage.setMinHeight(520);
		//Set stylesheet CSS
		mainStage.getScene().getStylesheets().add(colorScheme);
		mainStage.show();
		root.requestFocus(); //makes focus go to the root of the scene
		cancel();
	}

}
