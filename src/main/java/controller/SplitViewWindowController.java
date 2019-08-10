package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Credential;
import model.PasswordManager;
import model.PreferencesManager;
import model.PreferencesManager.InvalidColorSchemeException;
import model.PreferencesManager.InvalidViewModeException;
import model.PasswordManager.DuplicatedKeyException;
import util.CryptoUtils.CryptoException;

public class SplitViewWindowController implements Initializable {

	//Bullet char for hiding password
	public static final char BULLET = '\u2022';

	//PasswordManager instance
	private PasswordManager pm;

	//PreferencesManager instance
	private PreferencesManager prefM;

	//ObservableList for the ListView
	private ObservableList<String> ob;

	//MainPanel
	@FXML
	private BorderPane mainPanel;

	//ListView
	@FXML
	private ListView<String> list;

	//Detailed View
	@FXML
	private BorderPane detailedView;
	//General View
	@FXML
	private BorderPane generalView;

	//MenuBar
	@FXML
	private MenuBar menuBar;

	//Credential TextFields
	@FXML
	private TextField siteField;
	@FXML
	private TextField usernameField;
	@FXML
	private TextField passwordField;
	@FXML
	private TextField categoryField;

	//TextField for filtering
	@FXML
	private TextField textField;

	//Buttons
	@FXML
	private Button saveButton;
	@FXML
	private Button editButton;
	@FXML
	private Button deleteButton;
	@FXML
	private Button cancelButton;
	@FXML
	private Button generatePasswordButton;

	//CheckBox for show/hide password
	@FXML
	private CheckBox checkBox;

	//Title label
	@FXML
	private Label titleLabel;

	//View radio menu
	@FXML
	private RadioMenuItem splitView;
	@FXML
	private RadioMenuItem tableView;

	//Color radio menu
	@FXML
	private RadioMenuItem greenScheme;
	@FXML
	private RadioMenuItem blueScheme;
	@FXML
	private RadioMenuItem redScheme;

	public SplitViewWindowController() {
		//Get an instance of PasswordManager
		try {
			this.pm = PasswordManager.getInstance();
		} catch (IOException e) {
			//Create an alert if an error happens while creating database file
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText("An error occurred while creating database file!");
			alert.show();
			e.printStackTrace();
		}

		//Get an instance of PreferencesManager
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

		//Create the ObservableList
		this.ob = FXCollections.observableArrayList();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//Initialize the ListView
		initListView();

		//Initialize radio menu
		initRadioMenu();
	}

	//ListView initialization
	private void initListView() {

		//Get credential TreeMap from PasswordManager
		TreeMap<String, Credential> data = pm.getCredentials();
		//Add credentials to ObservableList
		for(String key : data.keySet()) {
			ob.add(key);
		}

		//Create the FilteredList
		FilteredList<String> filteredData = new FilteredList<String>(ob, p -> true);

		//Set the filter predicate whenever the filter changes
		textField.textProperty().addListener((observable, oldValue, newValue) -> {
			//If the textField is empty, display all credentials
			filteredData.setPredicate(credential -> {
				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				//Compare credential key with filter
				String lowerCaseFilter = newValue.toLowerCase();

				if (credential.toLowerCase().contains(lowerCaseFilter))
					return true; //Filter matches credential key

				return false; //Does not match
			});
		});

		//Wrap the FilteredList into a SortedList
		SortedList<String> sortedData = new SortedList<String>(filteredData);

		//Add sorted (and filtered) data to the list
		list.setItems(sortedData);

		//Create ListView listener
		list.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				//If a row is selected, show credential info
				if (newValue != null) {
					//If detailed view is hidden, show it
					if (detailedView.isDisabled())
						detailedView.setDisable(false);

					//If 'show password' checkBox is selected, deselect it
					if(checkBox.isSelected()) {
						checkBox.setSelected(false);
					}

					//Get credential info and add them to detailedView
					Credential c = pm.getCredential(newValue);
					siteField.setText(c.getSite());
					usernameField.setText(c.getUsername());
					String password = hidePassword(c.getPassword());	//Hide password
					passwordField.setText(password);
					categoryField.setText(c.getCategory());
				}

				//If no row is selected, hide detailed view and clear credential info
				else {
					if (!detailedView.isDisabled())
						detailedView.setDisable(true);
					siteField.setText("");
					usernameField.setText("");
					passwordField.setText("");
					categoryField.setText("");
				}
			}
		});

		//Create ContexMenu
		createContextMenu();
	}

	//Create the context menu for ListView items
	private void createContextMenu() {
		list.setCellFactory(lv -> {
			ListCell<String> cell = new ListCell<String>();

			ContextMenu contextMenu = new ContextMenu();

			//Edit option
			MenuItem editItem = new MenuItem("Edit");
			editItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					editCredential();
				}
			});

			//Delete option
			MenuItem deleteItem = new MenuItem("Delete");
			deleteItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					deleteCredential();
				}
			});

			//Copy password option
			MenuItem copyPasswordItem = new MenuItem("Copy password");

			copyPasswordItem.setOnAction(new EventHandler<ActionEvent>(){
				@Override
				public void handle(ActionEvent event) {
					String c = list.getSelectionModel().getSelectedItem();
					ClipboardContent content = new ClipboardContent();
					content.putString(pm.getCredential(c).getPassword());
					content.putHtml("<b>Bold</b> text");
					Clipboard.getSystemClipboard().setContent(content);

					//Create an alert for confirmation
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Copy Password");
					alert.setHeaderText(null);
					alert.setContentText("Password copied into clipboard!");
					alert.show();
				}
			});

			contextMenu.getItems().add(copyPasswordItem);
			contextMenu.getItems().add(editItem);
			contextMenu.getItems().add(deleteItem);

			cell.textProperty().bind(cell.itemProperty());

			cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
				if (isNowEmpty) {
					cell.setContextMenu(null);
				} else {
					cell.setContextMenu(contextMenu);
				}
			});
			return cell ;
		});
	}

	//Hide password (replace every character with a bullet)
	private static String hidePassword(String password) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < password.length(); i++ ) {
			sb.append(BULLET);
		}
		return sb.toString();
	}

	//Init radio menu
	private void initRadioMenu() {
		//Create ViewMode toggle group
		ToggleGroup viewTGroup = new ToggleGroup();
		splitView.setToggleGroup(viewTGroup);
		tableView.setToggleGroup(viewTGroup);

		//Create ColorScheme toggle group
		ToggleGroup colorTGroup = new ToggleGroup();
		greenScheme.setToggleGroup(colorTGroup);
		blueScheme.setToggleGroup(colorTGroup);
		redScheme.setToggleGroup(colorTGroup);

		//Get color scheme
		int colorScheme = prefM.getColorScheme();

		//Select right color scheme
		if (colorScheme == 0) {
			greenScheme.setSelected(true);
		}
		else if (colorScheme == 1) {
			blueScheme.setSelected(true);
		}
		else {
			redScheme.setSelected(true);
		}
	}

	//Delete credential
	@FXML
	private void deleteCredential() {
		//Get selected credential from ListView
		String selectedCredential = list.getSelectionModel().getSelectedItem();

		//If a credential is selected
		if (selectedCredential != null) {
			//Create an alert for get confirmation
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Delete credential");
			alert.setHeaderText("This will delete the selected credential");
			alert.setContentText("Do you want to continue?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				//Remove credential from the ObservableList
				ob.remove(selectedCredential);
				try {
					//Remove credential from PasswordManager
					pm.removeCredential(selectedCredential);
				} catch (IOException | CryptoException e) {
					e.printStackTrace();
				}
			}	
		}

		//If no credential is selected
		else {
			//Create an alert if no credential is selected
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText("Please, select first the credential to remove!");
			alert.show();
		}

	}

	//Edit credential
	@FXML
	private void editCredential() {
		//Get selected credential from ListView
		String selectedCredential = list.getSelectionModel().getSelectedItem();

		//If a credential is selected
		if (selectedCredential != null) {
			//Set title label
			titleLabel.setText("Edit credential");
			//Disable delete and edit buttons
			deleteButton.setDisable(true);
			editButton.setDisable(true);
			//Show save and cancel buttons
			saveButton.setVisible(true);
			cancelButton.setVisible(true);
			//Enable generate password button
			generatePasswordButton.setDisable(false);
			//Make username, password and category fields editable
			usernameField.setEditable(true);
			passwordField.setEditable(true);
			categoryField.setEditable(true);
			//Show password and disable 'show password' CheckBox
			checkBox.setSelected(true);
			showPassword();
			checkBox.setDisable(true);
			//Disable general view and menubar
			generalView.setDisable(true);
			menuBar.setDisable(true);
		}

		//If no credential is selected
		else {
			//Create an alert if no credential is selected
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText("Please, select first the credential to edit!");
			alert.show();
		}

	}

	//Add a new credential
	@FXML
	private void addCredential() { 
		//Deselect item
		list.getSelectionModel().clearSelection();

		//If detailed view is hidden, show it
		if (detailedView.isDisabled())
			detailedView.setDisable(false);

		//Set title label
		titleLabel.setText("Add credential");
		//Disable delete and edit buttons
		deleteButton.setDisable(true);
		editButton.setDisable(true);
		//Show save and cancel button
		saveButton.setVisible(true);
		cancelButton.setVisible(true);
		//Enable generate password button
		generatePasswordButton.setDisable(false);
		//Make username, password and category fields editable
		usernameField.setEditable(true);
		passwordField.setEditable(true);
		categoryField.setEditable(true);
		//Select and disable 'show password' CheckBox
		checkBox.setSelected(true);
		checkBox.setDisable(true);
		//Disable general view and menubar
		generalView.setDisable(true);
		menuBar.setDisable(true);
	}

	//Save added or edited credential
	@FXML
	private void save() {

		//If credential has been edited
		if (list.getSelectionModel().getSelectedItem() != null) {
			String site = siteField.getText();
			String username = usernameField.getText();
			String password = passwordField.getText();
			String category = categoryField.getText();

			//Create an alert if some field is empty
			if(username.equals("") || password.equals("") || category.equals("")){
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning");
				alert.setHeaderText(null);
				alert.setContentText("Please, fill all the fields");
				alert.showAndWait();
			}
			else {
				try {
					pm.editCredential(site, username, password, category);
					cancel();
				} catch (IOException | CryptoException e) {
					//Create an alert if an error happens while updating the database file
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText(null);
					alert.setContentText("An error occurred while updating the database file!");
					alert.show();
					e.printStackTrace();
				}
			}
		}

		//If a new credential has been added
		else {
			String site = siteField.getText();
			String username = usernameField.getText();
			String password = passwordField.getText();
			String category = categoryField.getText();

			//Create an alert if some field is empty
			if(site.equals("") || username.equals("") || password.equals("") || category.equals("")){
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning");
				alert.setHeaderText(null);
				alert.setContentText("Please, fill all the fields");
				alert.show();		
			}
			else {
				try {
					//Add the credential to PasswordManager and to ObservableList
					pm.addCredential(site, username, password, category);
					ob.add(site);
					//Sort ObservableList
					FXCollections.sort(ob);
					//Select added credential
					list.getSelectionModel().select(site);
				} catch (IOException | CryptoException e) {
					e.printStackTrace();
				} catch (DuplicatedKeyException e) {
					//Create an alert if the credential is already in the table
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText(null);
					alert.setContentText("Credential is already in the table!");
					alert.show();
					e.printStackTrace();
				}
				cancel();
			}
		}

	}

	//Cancel adding or editing operation
	@FXML
	private void cancel() {
		//Set title label
		titleLabel.setText("Credential");
		//Enable delete and edit buttons
		deleteButton.setDisable(false);
		editButton.setDisable(false);
		//Hide save and cancel buttons
		saveButton.setVisible(false);
		cancelButton.setVisible(false);
		//Disable 'generate password' button
		generatePasswordButton.setDisable(true);
		//Make username, password and category fields not editable
		usernameField.setEditable(false);
		passwordField.setEditable(false);
		categoryField.setEditable(false);
		//Deselect and disable 'show password' CheckBox
		checkBox.setSelected(false);
		checkBox.setDisable(false);
		//Enable general view and menubar
		generalView.setDisable(false);
		menuBar.setDisable(false);

		//Fill detailed view
		//If credential has been edited
		if (list.getSelectionModel().getSelectedItem() != null) {
			Credential c = pm.getCredential(siteField.getText());
			usernameField.setText(c.getUsername());
			showPassword();
			categoryField.setText(c.getCategory());
		}

		//If a new credential has been added
		else {
			siteField.setText("");
			usernameField.setText("");
			passwordField.setText("");
			categoryField.setText("");
			detailedView.setDisable(true);
		}

	}

	//Clear all the data
	@FXML
	public void clearData() {
		//Create an alert for get confirmation
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Clear Data");
		alert.setHeaderText(null);
		alert.setContentText("Do you really want to continue?");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK){
			try {
				this.pm.clearData();
			} catch (IOException | CryptoException e) {
				//Create an alert if an error happens while updating the database file
				alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText(null);
				alert.setContentText("An error occurred while updating the database file!");
				alert.show();
				e.printStackTrace();
			}
			ob.clear();
		}

	}

	//Change master password
	@FXML
	private void changeMasterPassword() {
		//Get color scheme
		int colorScheme = prefM.getColorScheme();

		//Show change master password window
		AnchorPane root = null;
		FXMLLoader loader = null;
		try {
			loader = new FXMLLoader(getClass().getResource("/view/ChangeMasterPasswordWindow.fxml"));
			root = (AnchorPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Stage cmpStage = new Stage();
		cmpStage.setTitle("Change master password");
		cmpStage.setScene(new Scene(root, 300, 400));
		cmpStage.setResizable(false);
		cmpStage.setWidth(300);

		//Clear color scheme
		cmpStage.getScene().getStylesheets().clear();

		//Set color scheme
		if (colorScheme == 0) {
			cmpStage.getScene().getStylesheets().add("/view/green_application.css");
		}
		else if (colorScheme == 1) {
			cmpStage.getScene().getStylesheets().add("/view/blue_application.css");
		}
		else {
			cmpStage.getScene().getStylesheets().add("/view/red_application.css");
		}

		//Blocks events from being delivered to any other application window
		cmpStage.initModality(Modality.APPLICATION_MODAL);
		cmpStage.showAndWait();
	}

	//Import credentials from .csv, .xls or .xlsx file
	@FXML
	private void importFromFile() {
		//Show file chooser
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Import from file");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter(".csv file", "*.csv"));
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Excel .xls file", "*.xls"));
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Excel .xlsx file", "*.xlsx"));
		File selectedFile = fileChooser.showOpenDialog(list.getScene().getWindow());
		if(selectedFile != null) {
			//Create an alert for get confirmation
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Import data");
			alert.setHeaderText("Credentials with the same site will be overwritten");
			alert.setContentText("Do you really want to continue?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){

				//Import credentials from .csv
				if(selectedFile.getName().endsWith(".csv")) {
					try {
						pm.importFromFile(selectedFile);
					} catch (CryptoException | IOException e) {
						e.printStackTrace();
					}
				}

				//Import credentials from .xls or .xlsx
				else {
					try {
						pm.importFromExcel(selectedFile);
					} catch (CryptoException | IOException e) {
						e.printStackTrace();
					}
				}

				TreeMap<String, Credential> data = pm.getCredentials();
				//Clear ObservableList
				ob.clear();

				//Update ObservableList
				for(String key : data.keySet()) {
					ob.add(key);
				}
			}
		}
	}

	//Export credentials to .csv file
	@FXML
	private void exportToFile() {
		//Show file chooser
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export to file");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter(".csv file", "*.csv"));
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Excel .xls file", "*.xls"));
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Excel .xlsx file", "*.xlsx"));
		File file = fileChooser.showSaveDialog(list.getScene().getWindow());

		if (file != null) {
			//Export credentials to .csv
			if(file.getName().endsWith(".csv")) {
				try {
					pm.writeToFileNoEncoding(file);
					//Create an alert for confirmation
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Export completed");
					alert.setHeaderText(null);
					alert.setContentText("Export has been completed successfully!");
					alert.show();
				} catch (IOException e) {
					//Show an alert if an error happens while exporting credentials to file
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText("An error is occured during the export.");
					alert.setContentText("File was not exported.");
					alert.show();
					e.printStackTrace();
				}
			}

			//Export credentials to .xls
			else if(file.getName().endsWith(".xls")) {
				try {
					pm.exportToXlsExcel(file);
					//Create an alert for confirmation
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Export completed");
					alert.setHeaderText(null);
					alert.setContentText("Export has been completed successfully!");
					alert.show();
				} catch (IOException e) {
					//Show an alert if an error happens while exporting credentials to file
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText("An error is occured during the export.");
					alert.setContentText("File was not exported.");
					alert.show();
					e.printStackTrace();
				}
			}

			//Export credentials to .xlsx
			else {
				try {
					pm.exportToXlsxExcel(file);
					//Create an alert for confirmation
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Export completed");
					alert.setHeaderText(null);
					alert.setContentText("Export has been completed successfully!");
					alert.show();
				} catch (IOException e) {
					//Show an alert if an error happens while exporting credentials to file
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText("An error is occured during the export.");
					alert.setContentText("File was not exported.");
					alert.show();
					e.printStackTrace();
				}
			}
		}
	}

	//Show about window
	@FXML
	private void showAbout() {
		//Get color scheme
		int colorScheme = prefM.getColorScheme();

		BorderPane root = null;
		FXMLLoader loader = null;
		try {
			loader = new FXMLLoader(getClass().getResource("/view/AboutWindow.fxml"));
			root = (BorderPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Stage aboutStage = new Stage();
		aboutStage.setTitle("About MyPM");
		aboutStage.setScene(new Scene(root, 350, 450));
		aboutStage.setResizable(false);
		aboutStage.setWidth(350);

		//Get imageView
		ImageView imageView = (ImageView) aboutStage.getScene().lookup("#imageView");

		//Set application logo color
		if (colorScheme == 0) {
			imageView.setImage(new Image("/resource/green_logo_156x156.png"));
		}
		else if (colorScheme == 1) {
			imageView.setImage(new Image("/resource/blue_logo_156x156.png"));
		}
		else {
			imageView.setImage(new Image("/resource/red_logo_156x156.png"));
		}

		//Blocks events from being delivered to any other application window
		aboutStage.initModality(Modality.APPLICATION_MODAL);
		aboutStage.showAndWait();
	}

	//Show password field
	@FXML
	private void showPassword() {
		//Get selected credential
		String selectedItem = list.getSelectionModel().getSelectedItem();
		//Get credential password
		String password = pm.getCredential(selectedItem).getPassword();

		//If 'show password' CheckBox is selected, show password field
		if (checkBox.isSelected()) {
			passwordField.setText(password);
		}
		//Else hide it
		else {
			String pw = hidePassword(password);
			passwordField.setText(pw);
		}
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

	//Exit application
	@FXML
	private void exit() {
		Platform.exit();
	}

	//Switch to Table View mode
	@FXML
	private void switchToTableView() {
		//Update preferences file
		try {
			prefM.setViewMode(1);
		} catch (InvalidViewModeException | IOException e) {
			//Show an alert if an error happens while updating preferences file
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("An error is occured while updating preferences file.");
			alert.setContentText("Preferences not updated.");
			alert.show();
			e.printStackTrace();
		}

		//Get color scheme
		int colorScheme = prefM.getColorScheme();

		//Show table view window
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
		mainStage.setMinHeight(500);

		//Clear stylesheet
		mainStage.getScene().getStylesheets().clear();

		//Set stylesheet CSS
		if (colorScheme == 0) {
			mainStage.getScene().getStylesheets().add("/view/green_application.css");
		}
		else if (colorScheme == 1) {
			mainStage.getScene().getStylesheets().add("/view/blue_application.css");
		}
		else {
			mainStage.getScene().getStylesheets().add("/view/red_application.css");
		}

		mainStage.show();
		root.requestFocus(); //makes focus go to the root of the scene

		//Close split view window
		Stage stage = (Stage) list.getScene().getWindow();
		stage.close();
	}

	//Switch to blue scheme
	@FXML
	private void switchToBlueScheme() {
		//Clear stylesheet
		mainPanel.getStylesheets().clear();
		//Set stylesheet CSS
		mainPanel.getStylesheets().add("/view/blue_application.css");

		//Update preferences file
		try {
			prefM.setColorScheme(1);;
		} catch (IOException | InvalidColorSchemeException e) {
			//Show an alert if an error happens while updating preferences file
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("An error is occured while updating preferences file.");
			alert.setContentText("Preferences not updated.");
			alert.show();
			e.printStackTrace();
		}
	}

	//Switch to green scheme
	@FXML
	private void switchToGreenScheme() {
		//Clear stylesheet
		mainPanel.getStylesheets().clear();
		//Set stylesheet CSS
		mainPanel.getStylesheets().add("/view/green_application.css");

		//Update preferences file
		try {
			prefM.setColorScheme(0);;
		} catch (IOException | InvalidColorSchemeException e) {
			//Show an alert if an error happens while updating preferences file
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("An error is occured while updating preferences file.");
			alert.setContentText("Preferences not updated.");
			alert.show();
			e.printStackTrace();
		}
	}

	//Switch to red scheme
	@FXML
	private void switchToRedScheme() {
		//Clear stylesheet
		mainPanel.getStylesheets().clear();
		//Set stylesheet CSS
		mainPanel.getStylesheets().add("/view/red_application.css");

		//Update preferences file
		try {
			prefM.setColorScheme(2);
		} catch (IOException | InvalidColorSchemeException e) {
			//Show an alert if an error happens while updating preferences file
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("An error is occured while updating preferences file.");
			alert.setContentText("Preferences not updated.");
			alert.show();
			e.printStackTrace();
		}
	}

}

