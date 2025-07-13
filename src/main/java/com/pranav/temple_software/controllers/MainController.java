//MainController.java
package com.pranav.temple_software.controllers;


import com.pranav.temple_software.controllers.menuControllers.DonationManager.DonationManagerController;
import com.pranav.temple_software.controllers.menuControllers.OtherSevaManager.OtherSevaManagerController;
import com.pranav.temple_software.controllers.menuControllers.SevaManager.SevaManagerController;
import com.pranav.temple_software.listeners.SevaListener;
import com.pranav.temple_software.models.Donations;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.DonationRepository;
import com.pranav.temple_software.repositories.OtherSevaRepository;
import com.pranav.temple_software.repositories.ReceiptRepository;
import com.pranav.temple_software.repositories.SevaRepository;
import com.pranav.temple_software.services.*;
import com.pranav.temple_software.utils.ReceiptPrinter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


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
	public TextArea addressField;
	public Button closeButton;
	@FXML
	private Button addDonationButton1;
	@FXML
	public Label totalLabel;
	@FXML
	private Button printPreviewButton;
	@FXML
	public Label receiptNumberLabel;
	@FXML private AnchorPane mainPane;



	@FXML
	private void handleCloseApp() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Exit Confirmation");
		alert.setHeaderText(null);
		alert.setContentText("Are you sure you want to exit the application?");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			Platform.exit();
		}
	}

	@FXML
	public void handleHistoryButton() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/History/History.fxml"));
			Stage historyStage = new Stage();
			historyStage.setTitle("ರಶೀದಿ ಇತಿಹಾಸ");
			historyStage.setScene(new Scene(loader.load()));
			historyStage.initModality(Modality.WINDOW_MODAL);
			historyStage.initOwner(mainStage);
			historyStage.setMaximized(true);
			historyStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			showAlert("Error", "Failed to load history view");
		}
	}

    @FXML
    public void handleDonationManagerButton(){
	    try {
		    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/DonationManager/DonationManagerView.fxml"));
		    Stage donationStage = new Stage();
		    donationStage.setTitle("ದೇಣಿಗೆಯನ್ನು ನಿರ್ವಹಿಸಿ");
		    Scene scene = new Scene(loader.load());
		    donationStage.setScene(scene);
		    DonationManagerController donationManagerController = loader.getController();
		    if (donationManagerController != null) {
			    donationManagerController.setMainController(this); // Pass this instance
		    } else {
			    System.err.println("Error: Could not get DonationManagerView instance.");
			    return;
		    }
			donationStage.initModality(Modality.WINDOW_MODAL);
		    donationStage.initOwner(mainStage);
		    donationStage.setMaxHeight(800);
		    donationStage.setMaxWidth(950);
		    donationStage.show();
	    } catch (Exception e) { // Catch broader exceptions
		    e.printStackTrace();
		    showAlert("Error", "Failed to load Donation Manager view: " + e.getMessage());
	    }
    }

	@FXML
	public void handleSevaManagerButton() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/SevaManager/SevaManagerView.fxml"));
			Stage sevaStage = new Stage();
			sevaStage.setTitle("ಸೇವೆಯನ್ನು ನಿರ್ವಹಿಸಿ");
			Scene scene = new Scene(loader.load());
			sevaStage.setScene(scene);

			SevaManagerController sevaManagerController = loader.getController();
			if (sevaManagerController != null) {
				// *** PASS both the repository (if needed) AND the MainController instance ***
				// If using Singleton for repo, you only need to pass mainController
				// sevaManagerController.setSevaRepository(this.sevaRepository); // Uncomment if using DI for repo
				sevaManagerController.setMainController(this); // Pass this instance
			} else {
				System.err.println("Error: Could not get SevaManagerController instance.");
				return;
			}

			sevaStage.initModality(Modality.WINDOW_MODAL);
			sevaStage.initOwner(mainStage);
			sevaStage.setMaxHeight(800);
			sevaStage.setMaxWidth(950);
			sevaStage.show();
		} catch (Exception e) { // Catch broader exceptions
			e.printStackTrace();
			showAlert("Error", "Failed to load Seva Manager view: " + e.getMessage());
		}
	}

	@FXML
	public void handleOtherSevaManagerButton() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/OtherSevaManager/OtherSevaManagerView.fxml"));
			Stage otherSevaStage = new Stage();
			otherSevaStage.setTitle("ಇತರೆ ಸೇವೆಗಳ ನಿರ್ವಹಣೆ");
			Scene scene = new Scene(loader.load());
			otherSevaStage.setScene(scene);

			OtherSevaManagerController otherSevaManagerController = loader.getController();
			if (otherSevaManagerController != null) {
				// *** PASS both the repository (if needed) AND the MainController instance ***
				// If using Singleton for repo, you only need to pass mainController
				// sevaManagerController.setSevaRepository(this.sevaRepository); // Uncomment if using DI for repo
				otherSevaManagerController.setMainController(this); // Pass this instance
			} else {
				System.err.println("Error: Could not get SevaManagerController instance.");
				return;
			}
			otherSevaStage.initOwner(mainStage);
			otherSevaStage.initModality(Modality.WINDOW_MODAL);
			otherSevaStage.setMaxWidth(950);
			otherSevaStage.setMaxHeight(800);
			otherSevaStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			showAlert("Error", "Unable to load Other Seva Manager: " + e.getMessage());
		}
	}


	@FXML
	public void clearForm() {
		// Clear all fields EXCEPT bound labels
		devoteeNameField.clear();
		contactField.clear();
		raashiComboBox.getSelectionModel().selectFirst();
		nakshatraComboBox.getSelectionModel().clearSelection();
		sevaDatePicker.setValue(LocalDate.now());
		selectedSevas.clear();
		donationCheck.setSelected(false);
		donationField.clear();
		donationComboBox.getSelectionModel().selectFirst();
		cashRadio.setSelected(false);
		onlineRadio.setSelected(false);
		otherServicesComboBox.getSelectionModel().selectFirst();
		addressField.clear();
		Platform.runLater(() -> devoteeNameField.requestFocus());
	}

	@FXML
	public void clearFormAfterChk(){
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Confirmation");
		alert.setHeaderText(null);
		alert.setContentText("Are you sure you want to clear the form?");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			clearForm();
		}

	}

	public final Map<String, CheckBox> sevaCheckboxMap = new HashMap<>();
	public ObservableList<SevaEntry> selectedSevas = FXCollections.observableArrayList();

	public void setMainStage(Stage stage) {
		this.mainStage = stage;
	}

	public ReceiptRepository receiptRepository = new ReceiptRepository();
	public SevaRepository sevaRepository = SevaRepository.getInstance();
	ValidationServices validationServices = new ValidationServices(this);
	ReceiptServices receiptServices = new ReceiptServices(this);
	OtherSevas otherSevas = new OtherSevas(this);
	public Donation donation = new Donation(this);
	Tables table = new Tables(this);
	public SevaListener sevaListener = new SevaListener(this, this.sevaRepository);

	public void showAlert(String title, String message) { //
		Alert alert = new Alert(Alert.AlertType.WARNING); //
		alert.setTitle(title); //
		alert.setHeaderText(null); //
		alert.setContentText(message); //
		alert.showAndWait(); //
	}


	@FXML
	public void initialize() {
		receiptNumberLabel.setText(String.valueOf(ReceiptRepository.getNextReceiptId()));
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
		refreshSevaCheckboxes();
		refreshDonationComboBox();
		refreshOtherSevaComboBox();

		Platform.runLater(() -> devoteeNameField.requestFocus());

		setupFocusTraversal();
		setupFocusLostHandlers();
		setupBlankAreaFocusHandler();

		// Force uppercase for Name field
		devoteeNameField.setTextFormatter(new TextFormatter<String>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));

	// Force uppercase for Address field
		addressField.setTextFormatter(new TextFormatter<String>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));


		selectedSevas.addListener((ListChangeListener<SevaEntry>) change -> {
			while (change.next()) {
				for (SevaEntry entry : change.getAddedSubList()) {
					entry.totalAmountProperty().addListener((obs, oldVal, newVal) ->
							validationServices.initializeTotalCalculation()
					);
				}
			}
		});

	}

	private void setupBlankAreaFocusHandler() {
		mainPane.setOnMousePressed(event -> {
			// If click target is not a text input, force focus to this pane
			if (!(event.getTarget() instanceof TextInputControl) &&
					!(event.getTarget() instanceof ComboBox) &&
					!(event.getTarget() instanceof DatePicker)) {
				mainPane.requestFocus();
			}
		});
	}

	private void setupFocusLostHandlers() {
		contactField.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal) {
				String phone = contactField.getText();
				if (!phone.matches("\\d{10}")) {
					showAlert("Invalid Input", "Enter a valid 10-digit mobile number.");
				}
			}
		});

		donationField.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal) {
				try {
					double amount = Double.parseDouble(donationField.getText());
					donationField.setText(String.format("%.2f", amount));
				} catch (NumberFormatException e) {
					donationField.clear();
					showAlert("Invalid Input", "Please enter a valid donation amount.");
				}
			}
		});
	}


	public void refreshSevaCheckboxes() {
		System.out.println("DEBUG: MainController refreshSevaCheckboxes() called.");
		if (sevaListener != null && sevaCheckboxContainer != null) {
			// Optional: Add error handling or checks if needed
			try {
				// Clear container before regenerating checkboxes
				sevaCheckboxContainer.getChildren().clear();
				// Ask the listener to rebuild checkboxes based on current repo data
				sevaListener.setupSevaCheckboxes();
				System.out.println("DEBUG: Seva checkboxes refreshed.");
			} catch (Exception e) {
				System.err.println("Error during refreshSevaCheckboxes: " + e.getMessage());
				e.printStackTrace(); // Log detailed error
				showAlert("Refresh Error", "Could not refresh Seva list in main view.");
			}
		} else {
			System.err.println("Cannot refresh checkboxes: SevaListener or Container is null.");
			if(sevaListener == null) System.err.println("sevaListener is null");
			if(sevaCheckboxContainer == null) System.err.println("sevaCheckboxContainer is null");
		}


	}



	// ... inside MainController class:
	public void refreshDonationComboBox() {
		List<Donations> donationEntries = DonationRepository.getInstance().getAllDonations();
		ObservableList<String> donationNames = FXCollections.observableArrayList(
				donationEntries.stream().map(Donations::getName).collect(Collectors.toList())
		);
		donationNames.add(0, "ಆಯ್ಕೆ");  // Optional default prompt
		donationComboBox.setItems(donationNames);
	}

	public void refreshOtherSevaComboBox() {
		// Reload from DB if necessary
		OtherSevaRepository.loadOtherSevasFromDB();

		// Get all Other Sevas (name + amount)
		List<SevaEntry> otherSevaEntries = OtherSevaRepository.getAllOtherSevas();

		// Format combo box entries to show: Name - ₹Amount
		ObservableList<String> otherSevaNames = FXCollections.observableArrayList(
				otherSevaEntries.stream()
						.map(seva -> seva.getName() + " - ₹" + String.format("%.2f", seva.getAmount()))
						.collect(Collectors.toList())
		);

		// Optional: add a default placeholder
		otherSevaNames.add(0, "ಆಯ್ಕೆ");

		// Set items in the combo box
		otherServicesComboBox.setItems(otherSevaNames);
	}


	private void setupFocusTraversal() {
		List<Control> formControls = List.of(
				devoteeNameField,
				contactField,
				raashiComboBox,
				nakshatraComboBox,
				sevaDatePicker,
				donationCheck,
				donationField,
				donationComboBox
		);

		for (int i = 0; i < formControls.size(); i++) {
			Control current = formControls.get(i);
			int nextIndex = i + 1;

			current.setOnKeyPressed(e -> {
				if (e.getCode().toString().equals("ENTER") && nextIndex < formControls.size()) {
					formControls.get(nextIndex).requestFocus();
				}
			});
		}
	}


}





