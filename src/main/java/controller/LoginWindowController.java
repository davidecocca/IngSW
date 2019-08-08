package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.PasswordManager;
import model.PreferencesManager;
import util.CryptoUtils.CryptoException;

public class LoginWindowController implements Initializable {

	//Password field
	@FXML
	private PasswordField passwordField;
	//Invalid password label
	@FXML
	private Label wrongPasswordLabel;
	//Logo image
	@FXML
	private ImageView imageView;

	//PasswordManager instance
	private PasswordManager pm;

	//PreferencesManager instance
	private PreferencesManager prefM;

	public LoginWindowController() {
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

	//Set application icon color
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//Get color scheme
		int colorScheme = prefM.getColorScheme();

		//Set logo image
		if (colorScheme == 0) {
			imageView.setImage(new Image("/resource/green_logo_64*64.png"));
		}
		else if (colorScheme == 1) {
			imageView.setImage(new Image("/resource/blue_logo_64*64.png"));
		}
		else {
			imageView.setImage(new Image("/resource/red_logo_64*64.png"));
		}
	}

	@FXML
	private void login() {
		//Get password string
		String password = passwordField.getText();

		//Check if the password is not null
		if (!password.equals("")) {
			try {
				//Init PasswordManager
				pm.init(password);

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
				//Show an error message if the password wrong
				wrongPasswordLabel.setVisible(true);
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}

	}

	@FXML
	private void cancel() {
		//Close the window
		Stage stage = (Stage) passwordField.getScene().getWindow();
		stage.close();
	}

	@FXML
	private void clearPasswordField() {
		//Clear the password text field
		this.passwordField.clear();
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
