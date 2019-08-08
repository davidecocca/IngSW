package controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Credential;

public class EditWindowController{

	//Credential TextFields
	@FXML
	private TextField siteField;
	@FXML
	private TextField usernameField;
	@FXML
	private TextField passwordField;
	@FXML
	private TextField categoryField;

	//Edited credential
	private Credential c;

	public EditWindowController() {
		this.c = null;
	}

	//Get edited credential
	public Credential getCredential() {
		return this.c;
	}

	public void setSiteField(String s) {
		siteField.setText(s);
	}

	public void setUsernameField(String u) {
		usernameField.setText(u);
	}

	public void setPasswordField(String p) {
		passwordField.setText(p);
	}

	public void setCategoryField(String c) {
		categoryField.setText(c);
	}

	//Generate random password
	@FXML
	private void generatePassword() {
		//Show GenerateRandomPasswordWindow
		BorderPane root = null;
		FXMLLoader loader = null;
		try {
			loader = new FXMLLoader(getClass().getResource("/view/GenerateRandomPasswordWindow.fxml"));
			root = (BorderPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Stage grpStage = new Stage();
		grpStage.setTitle("GenerateRandomPassword");
		grpStage.setScene(new Scene(root, 350, 480));
		grpStage.setResizable(false);
		grpStage.setWidth(350);
		//Blocks events from being delivered to any other application window
		grpStage.initModality(Modality.APPLICATION_MODAL);
		grpStage.showAndWait();

		GenerateRandomPasswordWindowController controller = loader.getController();
		String pw = controller.getPassword();
		if (pw != null) {
			passwordField.setText(pw);
		}
	}

	@FXML
	private void ok() {
		//Get site from text field
		String site = siteField.getText();
		//Get username from text field
		String username = usernameField.getText();
		//Get password from text field
		String password = passwordField.getText();
		//Get category from text field
		String category = categoryField.getText();
		//If credential info are not valid
		if(username.equals("") || password.equals("") || category.equals("")){
			//Create an alert if some field is empty
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("Please, fill all the fields");
			alert.showAndWait();
		}
		//If credential info are valid
		else {
			c = new Credential(site, username, password, category);
			cancel();
		}
	}

	@FXML
	private void cancel() {
		//Close the window
		Stage stage = (Stage) siteField.getScene().getWindow();
		stage.close();
	}

}
