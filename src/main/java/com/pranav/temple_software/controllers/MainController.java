//MainController.java
package com.pranav.temple_software.controllers;


import com.pranav.temple_software.listeners.SevaListener;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.ReceiptRepository;
import com.pranav.temple_software.repositories.SevaRepository;
import com.pranav.temple_software.services.*;
import com.pranav.temple_software.utils.ReceiptPrinter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class MainController {
	public Map<String, List<String>> rashiNakshatraMap = new HashMap<>();

	public ReceiptPrinter receiptPrinter = new ReceiptPrinter();
	public Stage mainStage;
	@FXML
	public ComboBox<String> raashiComboBox;
	@FXML
	public ComboBox<String> nakshatraComboBox;
	@FXML
	public ComboBox<String> otherServicesComboBox;
	@FXML
	public ComboBox<String> donationComboBox;
	@FXML
	public TextField donationField;
	@FXML
	public CheckBox donationCheck;
	@FXML
	public RadioButton cashRadio;
	@FXML
	public RadioButton onlineRadio;
	@FXML
	public TextField devoteeNameField;
	@FXML
	public TextField contactField;
	@FXML
	public DatePicker sevaDatePicker;
	@FXML
	public VBox sevaCheckboxContainer;
	@FXML
	public TableView<SevaEntry> sevaTableView;
	@FXML
	public TableColumn<SevaEntry, String> slNoColumn;
	@FXML
	public TableColumn<SevaEntry, String> sevaNameColumn;
	@FXML
	public Button addDonationButton;
	@FXML
	private Button addDonationButton1;
	@FXML
	public Label totalLabel;
	@FXML
	private Button printPreviewButton;
	@FXML
	public Label receiptNumberLabel;

	// MainController.java
	@FXML
	public void handleHistoryButton() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/History.fxml"));
			Stage historyStage = new Stage();
			historyStage.setTitle("ರಶೀದಿ ಇತಿಹಾಸ");
			historyStage.setScene(new Scene(loader.load()));
			historyStage.initModality(Modality.WINDOW_MODAL);
			historyStage.initOwner(mainStage);
			historyStage.setMaximized(true);
			historyStage.show();
		} catch (IOException e) {
			showAlert("Error", "Failed to load history view");
		}
	}

	@FXML
	public void clearForm() {
		// Clear all fields EXCEPT bound labels
		devoteeNameField.clear();
		contactField.clear();
		raashiComboBox.getSelectionModel().clearSelection();
		nakshatraComboBox.getSelectionModel().clearSelection();
		sevaDatePicker.setValue(LocalDate.now());
		selectedSevas.clear();
		donationCheck.setSelected(false);
		donationField.clear();
		donationComboBox.getSelectionModel().clearSelection();
		cashRadio.setSelected(false);
		onlineRadio.setSelected(false);
		receiptNumberLabel.setText("");
	}


	public final Map<String, CheckBox> sevaCheckboxMap = new HashMap<>();
	public ObservableList<SevaEntry> selectedSevas = FXCollections.observableArrayList();

	public void setMainStage(Stage stage) {
		this.mainStage = stage;
	}

	public ReceiptRepository receiptRepository = new ReceiptRepository();
	public SevaRepository sevaRepository = new SevaRepository();
	ValidationServices validationServices = new ValidationServices(this);
	ReceiptServices receiptServices = new ReceiptServices(this);
	OtherSevas otherSevas = new OtherSevas(this);
	public Donation donation = new Donation(this);
	Tables table = new Tables(this);
	public SevaListener sevaListener = new SevaListener(this);


	public void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}


	@FXML
	public void initialize() {
		sevaDatePicker.setValue(LocalDate.now());
		sevaNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		sevaTableView.setItems(selectedSevas);
		table.setupTableView();
		validationServices.initializeTotalCalculation();
		printPreviewButton.setOnAction(e -> receiptServices.handlePrintPreview());
		addDonationButton1.setOnAction(e -> otherSevas.handleAddOtherSeva());
		donation.setDisable();
		donationField.setDisable(true);
		donationComboBox.setDisable(true);
		addDonationButton.setDisable(true);
		sevaListener.initiateSevaListener();
		table.donationListener();
		validationServices.calenderChecker();
		validationServices.radioCheck();
		validationServices.threeNakshatraForARashi();
		validationServices.setupNameValidation();
		validationServices.setupPhoneValidation();
		validationServices.setupAmountValidation();

	}
}





