package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import model.PasswordManager;

public class GenerateRandomPasswordWindowController {

	//Generated password
	private String password;

	//Slider
	@FXML
	private Slider slider;
	//Generated password TextField
	@FXML
	private TextField textField;
	//CheckBoxes for password generation options
	@FXML
	private CheckBox mixedCaseLetters;
	@FXML
	private CheckBox digits;
	@FXML
	private CheckBox symbols;
	//Label for showing number of characters of generated password
	@FXML
	private Label charNumber;

	public void initialize() {
		//Set label text when slider value changes
		slider.valueProperty().addListener((ov, old_val, new_val) -> {
			int value = (int) Math.round(new_val.doubleValue());
			charNumber.setText(Integer.toString(value));
		});
	}

	//Get generated password
	public String getPassword() {
		return this.password;
	}

	//Generate password
	@FXML
	private void generate() {
		//Get number of characters of new password
		int length = (int) Math.round(slider.getValue());
		//Generate new password
		String pw = PasswordManager.generateRandomPassword(length, mixedCaseLetters.isSelected(), 
				digits.isSelected(), symbols.isSelected());
		//Set password TextField
		this.textField.setText(pw);
	}

	@FXML
	private void ok() {
		//Get generated password from TextField
		String pw = textField.getText();
		//If generated password is valid
		if (!pw.equals("")) {
			//Set password
			this.password = this.textField.getText();
			cancel();
		}
		//If generated password is not valid
		else {
			//Create an alert if password field is empty
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning");
			alert.setHeaderText(null);
			alert.setContentText("Please, press 'generate' button first!");
			alert.show();
		}
	}

	@FXML
	private void cancel() {
		//Close the window
		Stage stage = (Stage) slider.getScene().getWindow();
		stage.close();
	}
}
