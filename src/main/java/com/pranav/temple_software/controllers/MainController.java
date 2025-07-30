// MainController.java
package com.pranav.temple_software.controllers;


import com.pranav.temple_software.controllers.menuControllers.DonationManager.DonationManagerController;
import com.pranav.temple_software.controllers.menuControllers.OtherSevaManager.OtherSevaManagerController;
import com.pranav.temple_software.controllers.menuControllers.SevaManager.SevaManagerController;
import com.pranav.temple_software.listeners.SevaListener;
import com.pranav.temple_software.models.DevoteeDetails;
import com.pranav.temple_software.models.Donations;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.CredentialsRepository;
import com.pranav.temple_software.repositories.DonationRepository;
import com.pranav.temple_software.repositories.OtherSevaRepository;
import com.pranav.temple_software.repositories.SevaReceiptRepository;
import com.pranav.temple_software.repositories.SevaRepository;
import com.pranav.temple_software.services.*;
import com.pranav.temple_software.utils.PasswordUtils;
import com.pranav.temple_software.utils.ReceiptPrinter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;


public class MainController {
	public Map<String, List<String>> rashiNakshatraMap = new HashMap<>();

	public ReceiptPrinter receiptPrinter = new ReceiptPrinter(this);
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
	public TableColumn<SevaEntry, Integer> quantityColumn;
	@FXML
	public TableColumn<SevaEntry, Number> amountColumn;
	@FXML
	public TableColumn<SevaEntry, Number> totalAmountColumn;

	// ADDED FXML annotation for the new static column
	@FXML
	public TableColumn<SevaEntry, SevaEntry.PrintStatus> statusColumn;

	@FXML
	public TableColumn<SevaEntry, Void> actionColumn;
	@FXML
	private Button addDonationButton1;
	@FXML
	public Label totalLabel;
	@FXML
	private Button printPreviewButton;

	@FXML private AnchorPane mainPane;

	@FXML private Button smartActionButton;
	@FXML private Button clearFormButton;
	@FXML private Label statusLabel;


	@FXML
	public void initialize() {

		sevaDatePicker.setValue(LocalDate.now());
		// Configure date format to DD-MM-YYYY
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		sevaDatePicker.setConverter(new StringConverter<>() {
			@Override
			public String toString(LocalDate date) {
				if (date != null) {
					return dateFormatter.format(date);
				} else {
					return "";
				}
			}

			@Override
			public LocalDate fromString(String string) {
				if (string != null && !string.isEmpty()) {
					try {
						return LocalDate.parse(string, dateFormatter);
					} catch (DateTimeParseException e) {
						return null;
					}
				} else {
					return null;
				}
			}
		});

		sevaNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		sevaTableView.setItems(selectedSevas);
		table.setupTableView();
		validationServices.initializeTotalCalculation();
		smartActionButton.setOnAction(e -> handleSmartAction());
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
		sevaListener.rashiNakshatraMap();
		refreshSevaCheckboxes();
		populateRashiComboBox();
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

	// ... (rest of the MainController.java file remains exactly the same) ...

	public void updatePrintStatusLabel() {
		long pendingCount = selectedSevas.stream()
				.mapToLong(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.PENDING ? 1 : 0)
				.sum();
		long successCount = selectedSevas.stream()
				.mapToLong(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.SUCCESS ? 1 : 0)
				.sum();
		long failedCount = selectedSevas.stream()
				.mapToLong(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.FAILED ? 1 : 0)
				.sum();

		Platform.runLater(() -> {
			// Update status label
			statusLabel.setText(String.format("ಬಾಕಿ: %d | ಯಶಸ್ವಿ: %d | ವಿಫಲ: %d",
					pendingCount, successCount, failedCount));

			if (successCount > 0 && pendingCount == 0 && failedCount == 0) {
				long printingCount = selectedSevas.stream()
						.mapToLong(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.PRINTING ? 1 : 0)
						.sum();

				if (printingCount == 0) {
					smartActionButton.setText("ಎಲ್ಲಾ ಯಶಸ್ವಿ! ಸ್ವಚ್ಛಗೊಳಿಸಲಾಗುತ್ತಿದೆ...");
					smartActionButton.setDisable(true);
					smartActionButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

					Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
						clearForm();
					}));
					timeline.play();
				}
			}
			else if (pendingCount > 0) {
				smartActionButton.setText("ಮುದ್ರಿಸಿ (" + pendingCount + " ಐಟಂಗಳು)");
				smartActionButton.setDisable(false);
				smartActionButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
			} else if (failedCount > 0) {
				smartActionButton.setText("ವಿಫಲವಾದವುಗಳನ್ನು ಮರುಪ್ರಯತ್ನಿಸಿ (" + failedCount + ")");
				smartActionButton.setDisable(false);
				smartActionButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
			} else if (successCount > 0) {
				smartActionButton.setText("ಯಶಸ್ವಿಯಾದವುಗಳನ್ನು ತೆರವುಮಾಡಿ (" + successCount + ")");
				smartActionButton.setDisable(false);
				smartActionButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
			} else {
				smartActionButton.setText("ಐಟಂಗಳನ್ನು ಸೇರಿಸಿ");
				smartActionButton.setDisable(selectedSevas.isEmpty());
				smartActionButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
			}
			clearFormButton.setDisable(selectedSevas.isEmpty());
		});
	}

	@FXML
	public void handleSmartAction() {
		long pendingCount = selectedSevas.stream()
				.filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.PENDING)
				.count();
		long failedCount = selectedSevas.stream()
				.filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.FAILED)
				.count();
		long successCount = selectedSevas.stream()
				.filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.SUCCESS)
				.count();

		if (pendingCount > 0 || failedCount > 0) {
			showPrintOrSaveDialog();
		} else if (successCount > 0) {
			receiptServices.handleClearSuccessful();
		} else {
			Platform.runLater(() -> devoteeNameField.requestFocus());
			showAlert("Add Items", "Please add seva items to the table to proceed.");
		}
	}

	private void showPrintOrSaveDialog() {
		List<String> choices = new ArrayList<>();
		choices.add("Save as PDF");
		choices.add("Print as Receipt");

		ChoiceDialog<String> dialog = new ChoiceDialog<>("Print as Receipt", choices);
		dialog.setTitle("Choose Action");
		dialog.setHeaderText("Select how you want to process the receipt(s).");
		dialog.setContentText("Choose your option:");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(selected -> {
			if ("Save as PDF".equals(selected)) {
				receiptServices.handleSaveAllPendingAsPdf();
			} else if ("Print as Receipt".equals(selected)) {
				if (selectedSevas.stream().anyMatch(e -> e.getPrintStatus() == SevaEntry.PrintStatus.FAILED)) {
					receiptServices.handleRetryFailed();
				} else {
					receiptServices.handlePrintAllPending();
				}
			}
		});
	}

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
		Runnable openDonationManager = () -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/DonationManager/DonationManagerView.fxml"));
				Stage donationStage = new Stage();
				donationStage.setTitle("ದೇಣಿಗೆಯನ್ನು ನಿರ್ವಹಿಸಿ");
				Scene scene = new Scene(loader.load());
				donationStage.setScene(scene);
				DonationManagerController donationManagerController = loader.getController();
				if (donationManagerController != null) {
					donationManagerController.setMainController(this);
				} else {
					System.err.println("Error: Could not get DonationManagerView instance.");
					return;
				}
				donationStage.initModality(Modality.WINDOW_MODAL);
				donationStage.initOwner(mainStage);
				donationStage.setMaxHeight(650);
				donationStage.setMaxWidth(800);
				donationStage.show();
			} catch (Exception e) {
				e.printStackTrace();
				showAlert("Error", "Failed to load Donation Manager view: " + e.getMessage());
			}
		};
		promptForSpecialPassword(openDonationManager);
	}

	@FXML
	public void handleSevaManagerButton() {
		Runnable openSevaManager = () -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/SevaManager/SevaManagerView.fxml"));
				Stage sevaStage = new Stage();
				sevaStage.setTitle("ಸೇವೆಯನ್ನು ನಿರ್ವಹಿಸಿ");
				Scene scene = new Scene(loader.load());
				sevaStage.setScene(scene);

				SevaManagerController sevaManagerController = loader.getController();
				if (sevaManagerController != null) {
					sevaManagerController.setMainController(this);
				} else {
					System.err.println("Error: Could not get SevaManagerController instance.");
					return;
				}

				sevaStage.initModality(Modality.WINDOW_MODAL);
				sevaStage.initOwner(mainStage);
				sevaStage.setMaxHeight(650);
				sevaStage.setMaxWidth(800);
				sevaStage.show();
			} catch (Exception e) {
				e.printStackTrace();
				showAlert("Error", "Failed to load Seva Manager view: " + e.getMessage());
			}
		};
		promptForSpecialPassword(openSevaManager);
	}

	@FXML
	public void handleOtherSevaManagerButton() {
		Runnable openOtherSevaManager = () -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/OtherSevaManager/OtherSevaManagerView.fxml"));
				Stage otherSevaStage = new Stage();
				otherSevaStage.setTitle("ಇತರೆ ಸೇವೆಗಳ ನಿರ್ವಹಣೆ");
				Scene scene = new Scene(loader.load());
				otherSevaStage.setScene(scene);

				OtherSevaManagerController otherSevaManagerController = loader.getController();
				if (otherSevaManagerController != null) {
					otherSevaManagerController.setMainController(this);
				} else {
					System.err.println("Error: Could not get SevaManagerController instance.");
					return;
				}
				otherSevaStage.initOwner(mainStage);
				otherSevaStage.initModality(Modality.WINDOW_MODAL);
				otherSevaStage.setMaxWidth(800);
				otherSevaStage.setMaxHeight(650);
				otherSevaStage.show();
			} catch (IOException e) {
				e.printStackTrace();
				showAlert("Error", "Unable to load Other Seva Manager: " + e.getMessage());
			}
		};
		promptForSpecialPassword(openOtherSevaManager);
	}

	private void promptForSpecialPassword(Runnable onPasswordSuccess) {
		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Access Required");
		dialog.setHeaderText("Please enter the special password to access manager views.");
		dialog.initOwner(mainStage);
		ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		PasswordField password = new PasswordField();
		password.setPromptText("Password");
		grid.add(new Label("Password:"), 0, 0);
		grid.add(password, 1, 0);
		dialog.getDialogPane().setContent(grid);
		Platform.runLater(password::requestFocus);
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == loginButtonType) {
				return password.getText();
			}
			return null;
		});
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(enteredPassword -> {
			CredentialsRepository credentialsRepo = new CredentialsRepository();
			Optional<String> storedHashOpt = credentialsRepo.getCredential("SPECIAL_PASSWORD");
			if (storedHashOpt.isEmpty()) {
				showAlert("Security Error", "Could not find special password in the database.");
				return;
			}
			if (PasswordUtils.checkPassword(enteredPassword, storedHashOpt.get())) {
				onPasswordSuccess.run();
			} else {
				showAlert("Access Denied", "The special password you entered is incorrect.");
			}
		});
	}

	@FXML
	public void clearForm() {
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
		updatePrintStatusLabel();
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

	public SevaReceiptRepository sevaReceiptRepository = new SevaReceiptRepository();
	public SevaRepository sevaRepository = SevaRepository.getInstance();
	ValidationServices validationServices = new ValidationServices(this);
	public ReceiptServices receiptServices = new ReceiptServices(this);
	OtherSevas otherSevas = new OtherSevas(this);
	public Donation donation = new Donation(this);
	Tables table = new Tables(this);
	public SevaListener sevaListener = new SevaListener(this, this.sevaRepository);

	public void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void populateRashiComboBox() {
		ObservableList<String> rashiOptions = FXCollections.observableArrayList();
		rashiOptions.add("ಆಯ್ಕೆ");
		rashiOptions.addAll(
				"ಮೇಷ", "ವೃಷಭ", "ಮಿಥುನ", "ಕರ್ಕಾಟಕ", "ಸಿಂಹ", "ಕನ್ಯಾ",
				"ತುಲಾ", "ವೃಶ್ಚಿಕ", "ಧನು", "ಮಕರ", "ಕುಂಭ", "ಮೀನ"
		);
		raashiComboBox.setItems(rashiOptions);
		raashiComboBox.getSelectionModel().selectFirst();
	}

	private void setupBlankAreaFocusHandler() {
		mainPane.setOnMousePressed(event -> {
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
			try {
				sevaRepository.loadSevasFromDB();
				sevaCheckboxContainer.getChildren().clear();
				sevaCheckboxMap.clear();
				sevaListener.setupSevaCheckboxes();
				System.out.println("DEBUG: Seva checkboxes refreshed with " +
						sevaRepository.getAllSevas().size() + " sevas.");
			} catch (Exception e) {
				System.err.println("Error during refreshSevaCheckboxes: " + e.getMessage());
				e.printStackTrace();
				showAlert("Refresh Error", "Could not refresh Seva list in main view.");
			}
		} else {
			System.err.println("Cannot refresh checkboxes: SevaListener or Container is null.");
			if(sevaListener == null) System.err.println("sevaListener is null");
			if(sevaCheckboxContainer == null) System.err.println("sevaCheckboxContainer is null");
		}
	}

	public void refreshDonationComboBox() {
		DonationRepository.getInstance().loadDonationsFromDB();
		List<Donations> donationEntries = DonationRepository.getInstance().getAllDonations();
		ObservableList<String> donationNames = FXCollections.observableArrayList(
				donationEntries.stream().map(Donations::getName).collect(Collectors.toList())
		);
		donationNames.add(0, "ಆಯ್ಕೆ");
		donationComboBox.setItems(donationNames);
		System.out.println("DEBUG: Donation ComboBox refreshed with " + donationEntries.size() + " donations.");
	}

	public void refreshOtherSevaComboBox() {
		OtherSevaRepository.loadOtherSevasFromDB();
		List<SevaEntry> otherSevaEntries = OtherSevaRepository.getAllOtherSevas();
		ObservableList<String> otherSevaNames = FXCollections.observableArrayList(
				otherSevaEntries.stream()
						.map(seva -> seva.getName() + " - ₹" + String.format("%.2f", seva.getAmount()))
						.collect(Collectors.toList())
		);
		otherSevaNames.add(0, "ಆಯ್ಕೆ");
		otherServicesComboBox.setItems(otherSevaNames);
		System.out.println("DEBUG: Other Seva ComboBox refreshed with " + otherSevaEntries.size() + " other sevas.");
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

	@FXML
	public void handleInKindDonationMenuItem() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/InKindDonationManager/InKindDonationView.fxml"));
			Stage inKindStage = new Stage();
			inKindStage.setTitle("ವಸ್ತು ದೇಣಿಗೆಯನ್ನು ಸೇರಿಸಿ");
			Scene scene = new Scene(loader.load());
			inKindStage.setScene(scene);
			inKindStage.initModality(Modality.WINDOW_MODAL);
			inKindStage.initOwner(mainStage);
			inKindStage.setResizable(false);
			inKindStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			showAlert("Error", "Failed to load the In-Kind Donation view: " + e.getMessage());
		}
	}

	public void populateDevoteeDetails(DevoteeDetails details) {
		if (details == null) return;
		devoteeNameField.setText(details.getName() != null ? details.getName() : "");
		addressField.setText(details.getAddress() != null ? details.getAddress() : "");
		if (details.getRashi() != null && !details.getRashi().isEmpty()) {
			raashiComboBox.setValue(details.getRashi());
		} else {
			raashiComboBox.getSelectionModel().selectFirst();
		}
		Platform.runLater(() -> {
			if (details.getNakshatra() != null && !details.getNakshatra().isEmpty()) {
				if (nakshatraComboBox.getItems().contains(details.getNakshatra())) {
					nakshatraComboBox.setValue(details.getNakshatra());
				}
			}
		});
	}
}
