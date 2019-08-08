package controller;

import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import model.PasswordManager;
import model.PreferencesManager;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

public class Main extends Application {

	//PasswordManager instance
	PasswordManager pm;

	//PreferencesManager instance
	private PreferencesManager prefM;

	@Override
	public void start(Stage primaryStage) {

		//Get a PasswordManager instance
		try {
			pm = PasswordManager.getInstance();
		} catch (IOException e) {
			//Create alert if an error happens while creating database file
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

		//Get the database file
		File dbFile = pm.getDbFile();

		//If the database file is empty show the first start window
		if (dbFile.length() == 0) {
			try {
				BorderPane root = FXMLLoader.load(getClass().getResource("/view/FirstStartWindow.fxml"));
				Scene scene = new Scene(root,600,400);
				scene.getStylesheets().add(getClass().getResource("/view/green_application.css").toExternalForm());
				primaryStage.setScene(scene);
				primaryStage.setTitle("First start");
				primaryStage.setResizable(false);
				primaryStage.show();
				root.requestFocus();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		//If the database file is not empty show the login window
		else {
			//Get color scheme
			int colorScheme = prefM.getColorScheme();

			try {
				AnchorPane root = FXMLLoader.load(getClass().getResource("/view/LoginWindow.fxml"));
				Scene scene = new Scene(root,300,400);
				primaryStage.setScene(scene);
				primaryStage.setTitle("Login");
				primaryStage.setResizable(false);
				primaryStage.setWidth(300);

				//Clear color scheme
				primaryStage.getScene().getStylesheets().clear();

				//Set color scheme
				if (colorScheme == 0) {
					primaryStage.getScene().getStylesheets().add("/view/green_application.css");
				}
				else if (colorScheme == 1) {
					primaryStage.getScene().getStylesheets().add("/view/blue_application.css");
				}
				else {
					primaryStage.getScene().getStylesheets().add("/view/red_application.css");
				}

				primaryStage.show();
				root.requestFocus();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		launch(args);
	}
}
