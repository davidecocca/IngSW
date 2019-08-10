package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javafx.application.Platform;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
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
import model.PasswordManager.DuplicatedKeyException;
import model.PreferencesManager.InvalidColorSchemeException;
import model.PreferencesManager.InvalidViewModeException;
import util.CryptoUtils.CryptoException;

public class TableViewWindowController implements Initializable {

	//PasswordManager instance
	private PasswordManager pm;

	//PreferencesManager instance
	private PreferencesManager prefM;

	//ObservableList for the TableView
	private ObservableList<Credential> ob;

	//MainPanel
	@FXML
	private BorderPane mainPanel;

	//TableView
	@FXML
	private TableView<Credential> table;

	//TableColumns
	@FXML
	private TableColumn<Credential, String> st;
	@FXML
	private TableColumn<Credential, String> un;
	@FXML
	private TableColumn<Credential, String> pw;
	@FXML
	private TableColumn<Credential, String> ct;

	//TextField for filtering
	@FXML
	private TextField textField;

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

	public TableViewWindowController() {
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
		//Initialize the TableView
		initTableView();

		//Initialize radio menu
		initRadioMenu();
	}

	//TableView initialization
	private void initTableView() {
		st.setCellValueFactory(new PropertyValueFactory<Credential, String>("Site"));
		un.setCellValueFactory(new PropertyValueFactory<Credential, String>("Username"));
		pw.setCellValueFactory(new PropertyValueFactory<Credential, String>("Password"));
		ct.setCellValueFactory(new PropertyValueFactory<Credential, String>("Category"));

		//Get credential TreeMap from PasswordManager
		TreeMap<String, Credential> data = pm.getCredentials();
		//Add credentials to ObservableList
		for(String key : data.keySet()) {
			Credential c = data.get(key);
			ob.add(c);
		}

		//Create the FilteredList
		FilteredList<Credential> filteredData = new FilteredList<Credential>(ob, p -> true);

		//Set the filter predicate whenever the filter changes
		textField.textProperty().addListener((observable, oldValue, newValue) -> {
			//If the textField is empty, display all credentials
			filteredData.setPredicate(credential -> {
				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				//Compare site, username, password and category field with filter
				String lowerCaseFilter = newValue.toLowerCase();

				if (String.valueOf(credential.getSite()).toLowerCase().contains(lowerCaseFilter)) {
					return true; //Filter matches site
				} 
				else if (String.valueOf(credential.getUsername()).toLowerCase().contains(lowerCaseFilter)) {
					return true; //Filter matches username
				} 
				else if (String.valueOf(credential.getPassword()).toLowerCase().contains(lowerCaseFilter)) {
					return true; //Filter matches password
				}
				else if (String.valueOf(credential.getCategory()).toLowerCase().contains(lowerCaseFilter)) {
					return true; //Filter matches category
				}
				return false; //Does not match
			});
		});

		//Wrap the FilteredList into a SortedList
		SortedList<Credential> sortedData = new SortedList<Credential>(filteredData);

		//Bind the SortedList comparator to the TableView comparator
		sortedData.comparatorProperty().bind(table.comparatorProperty());

		//Set default table sorting
		st.setSortType(TableColumn.SortType.ASCENDING);
		table.getSortOrder().add(st);

		//Add sorted (and filtered) data to the table
		table.setItems(sortedData);

		//Create ContexMenu
		createContextMenu();
	}

	//Create the context menu for TableView rows
	private void createContextMenu() {
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
				Credential c = table.getSelectionModel().getSelectedItem();
				ClipboardContent content = new ClipboardContent();
				content.putString(c.getPassword());
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

		//Create event handler for context menu
		table.setRowFactory( tv -> {
			TableRow<Credential> row = new TableRow<Credential>();
			row.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.SECONDARY && !row.isEmpty()) {
					contextMenu.show(table, event.getScreenX(), event.getScreenY());
				}
				if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
					editCredential();
				}
				//Hide context menu when a row is selected
				if (event.getButton() == MouseButton.PRIMARY) {
					if(contextMenu.isShowing()) {
						contextMenu.hide();
					}
				}
			});
			return row ;
		});
	}

	//Init radio menu
	private void initRadioMenu() {
		//Create ViewMode toggle group
		ToggleGroup tGroup = new ToggleGroup();
		splitView.setToggleGroup(tGroup);
		tableView.setToggleGroup(tGroup);

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
		//Get selected credential from TableView
		Credential c = table.getSelectionModel().getSelectedItem();

		//If a credential is selected
		if (c != null) {
			//Create an alert for get confirmation
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Delete credential");
			alert.setHeaderText("This will delete the selected credential");
			alert.setContentText("Do you want to continue?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				//Remove credential from the ObservableList
				ob.remove(c);
				try {
					//Remove credential from PasswordManager
					pm.removeCredential(c.getSite());
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
		//Get color scheme
		int colorScheme = prefM.getColorScheme();

		//Get selected credential from TableView
		Credential selectedCredential = table.getSelectionModel().getSelectedItem();

		//If a credential is selected
		if (selectedCredential != null) {
			//Show edit credential window
			BorderPane root = null;
			FXMLLoader loader = null;

			try {
				loader = new FXMLLoader(getClass().getResource("/view/EditCredentialWindow.fxml"));
				root = (BorderPane) loader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}

			//Pass credential info to edit window controller
			EditWindowController controller = loader.getController();
			controller.setSiteField(selectedCredential.getSite());
			controller.setUsernameField(selectedCredential.getUsername());
			controller.setPasswordField(selectedCredential.getPassword());
			controller.setCategoryField(selectedCredential.getCategory());

			Stage editStage = new Stage();
			editStage.setTitle("Edit Credential");
			editStage.setScene(new Scene(root, 400, 480));
			editStage.setResizable(false);
			editStage.setWidth(400);
			root.requestFocus();

			//Clear color scheme
			editStage.getScene().getStylesheets().clear();

			//Set color scheme
			if (colorScheme == 0) {
				editStage.getScene().getStylesheets().add("/view/green_application.css");
			}
			else if (colorScheme == 1) {
				editStage.getScene().getStylesheets().add("/view/blue_application.css");
			}
			else {
				editStage.getScene().getStylesheets().add("/view/red_application.css");
			}

			//Blocks events from being delivered to any other application window
			editStage.initModality(Modality.APPLICATION_MODAL);
			editStage.showAndWait();

			//Get edited credential from controller
			Credential credential = controller.getCredential();
			if (credential != null) {
				try {
					pm.editCredential(credential.getSite(), credential.getUsername(), credential.getPassword(), credential.getCategory());
				} catch (IOException | CryptoException e) {
					e.printStackTrace();
				}
				//Remove the old credential from the ObservableList and add the new credential
				ob.remove(selectedCredential);
				ob.add(credential);
			}
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
		//Get color scheme
		int colorScheme = prefM.getColorScheme();

		//Show AddWCredentialWindow
		BorderPane root = null;
		FXMLLoader loader = null;

		try {
			loader = new FXMLLoader(getClass().getResource("/view/AddCredentialWindow.fxml"));
			root = (BorderPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Stage addStage = new Stage();
		addStage.setTitle("Add Credential");
		addStage.setScene(new Scene(root, 400, 480));
		addStage.setResizable(false);
		addStage.setWidth(400);

		//Clear color scheme
		addStage.getScene().getStylesheets().clear();

		//Set color scheme
		if (colorScheme == 0) {
			addStage.getScene().getStylesheets().add("/view/green_application.css");
		}
		else if (colorScheme == 1) {
			addStage.getScene().getStylesheets().add("/view/blue_application.css");
		}
		else {
			addStage.getScene().getStylesheets().add("/view/red_application.css");
		}

		//Blocks events from being delivered to any other application window
		addStage.initModality(Modality.APPLICATION_MODAL);
		addStage.showAndWait();

		//Get new credential from controller
		AddWindowController controller = loader.getController();
		Credential credential = controller.getCredential();
		if (credential != null) {
			try {
				//Add the credential to PasswordManager and to ObservableList
				pm.addCredential(credential.getSite(), credential.getUsername(), credential.getPassword(), credential.getCategory());
				ob.add(credential);
			} catch (IOException | CryptoException e) {
				e.printStackTrace();
			} catch (DuplicatedKeyException e) {
				//Create an alert if the credential is already in the table
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText(null);
				alert.setContentText("Credential is already in the table!");
				alert.show();
				//e.printStackTrace();
			}
		}
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
		File selectedFile = fileChooser.showOpenDialog(table.getScene().getWindow());
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
					Credential c = data.get(key);
					ob.add(c);
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
		File file = fileChooser.showSaveDialog(table.getScene().getWindow());

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

	//Exit application
	@FXML
	private void exit() {
		Platform.exit();
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

	//Switch to Split View mode
	@FXML
	private void switchToSplitView() {
		//Update preferences file
		try {
			prefM.setViewMode(0);
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

		//Show split view window
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
		Stage stage = (Stage) table.getScene().getWindow();
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
			prefM.setColorScheme(2);;
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

