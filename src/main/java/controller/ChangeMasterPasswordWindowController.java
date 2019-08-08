package controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import model.PasswordManager;
import util.CryptoUtils.CryptoException;

public class ChangeMasterPasswordWindowController {

	//Current master password field
	@FXML
	private PasswordField currentMasterPasswordField;
	//First new master password field
	@FXML
	private PasswordField firstNewMasterPasswordField;
	//Second new master password field
	@FXML
	private PasswordField secondNewMasterPasswordField;
	//Error label
	@FXML
	private Label errorLabel;

	//PasswordManager instance
	private PasswordManager pm;

	public ChangeMasterPasswordWindowController() {
		//Get an instance of PasswordManager
		try {
			pm = PasswordManager.getInstance();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void ok() {
		//Get current master password from field
		String currentMasterPassword = currentMasterPasswordField.getText();
		//Get first new master password from field
		String firstNewMasterPassword = firstNewMasterPasswordField.getText();
		//Get second new master password from field
		String secondNewMasterPassword = secondNewMasterPasswordField.getText();
		
		/* If currentMasterPasswrod is correct, firstNewMasterPassword is at least 8-character long and equals to
		secondNewMasterPassword, set the new master password, else show an error message */
		if (currentMasterPassword.equals(pm.getMasterPassword()) && firstNewMasterPassword.length() >= 8 &&
				firstNewMasterPassword.equals(secondNewMasterPassword)) {
			//Set new master password
			pm.setMasterPassword(firstNewMasterPassword);
			try {
				//Update database file
				pm.writeToFile();
			} catch (IOException e) {
				//Create an alert if an error happens while saving the new master password
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText(null);
				alert.setContentText("An error is occurred while trying to save the new master password!");
				alert.showAndWait();
				e.printStackTrace();
			} catch (CryptoException e) {
				e.printStackTrace();
			}
			//Create an alert of confirmation
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("New master password set!");
			alert.showAndWait();
			cancel();
		}
		else {
			//Set error label visible
			errorLabel.setVisible(true);
		}
	}

	@FXML
	private void cancel() {
		//Close the window
		Stage stage = (Stage) currentMasterPasswordField.getScene().getWindow();
		stage.close();
	}

}
