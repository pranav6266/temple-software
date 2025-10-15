// FILE: src/main/java/com/pranav/temple_software/controllers/MainController.java
package com.pranav.temple_software.controllers;
import com.pranav.temple_software.controllers.menuControllers.DonationManager.DonationController;
import com.pranav.temple_software.controllers.menuControllers.DonationManager.DonationManagerController;
import com.pranav.temple_software.controllers.menuControllers.SevaManager.SevaManagerController;
import com.pranav.temple_software.controllers.menuControllers.ShashwathaPoojaManager.ShashwathaPoojaController;
import com.pranav.temple_software.controllers.menuControllers.VisheshaPoojeManager.VisheshaPoojeManagerController;
import com.pranav.temple_software.listeners.SevaListener;
import com.pranav.temple_software.models.DevoteeDetails;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.*;
import com.pranav.temple_software.services.*;
import com.pranav.temple_software.utils.BackupService;
import com.pranav.temple_software.utils.PasswordUtils;
import com.pranav.temple_software.utils.ReceiptPrinter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController {
	private static final Logger logger = LoggerFactory.getLogger(MainController.class);
	public Map<String, List<String>> rashiNakshatraMap = new HashMap<>();
	public ReceiptPrinter receiptPrinter = new ReceiptPrinter();
	public Stage mainStage;
	@FXML public ComboBox<String> raashiComboBox;
	@FXML public ComboBox<String> nakshatraComboBox;
	@FXML public ComboBox<String> visheshaPoojeComboBox;
	@FXML public RadioButton cashRadio;
	@FXML public RadioButton onlineRadio;
	@FXML public TextField devoteeNameField;
	@FXML public TextField contactField;
	@FXML public TextField panNumberField;
	@FXML public DatePicker sevaDatePicker;
	@FXML public VBox sevaCheckboxContainer;
	@FXML public TableView<SevaEntry> sevaTableView;
	@FXML public TableColumn<SevaEntry, String> slNoColumn;
	@FXML public TableColumn<SevaEntry, String> sevaNameColumn;
	@FXML public Button addVisheshaPoojeButton;
	public TextArea addressField;
	public Button closeButton;
	public TableColumn<SevaEntry, Integer> quantityColumn;
	@FXML public TableColumn<SevaEntry, Number> amountColumn;
	@FXML public TableColumn<SevaEntry, Number> totalAmountColumn;
	@FXML public TableColumn<SevaEntry, SevaEntry.PrintStatus> statusColumn;
	@FXML public TableColumn<SevaEntry, Void> actionColumn;
	@FXML public Label totalLabel;
	@FXML private AnchorPane mainPane;
	@FXML private Button smartActionButton;
	@FXML private Button clearFormButton;
	@FXML private Label statusLabel;

	@FXML
	public void initialize() {
		sevaDatePicker.setValue(LocalDate.now());
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		sevaDatePicker.setConverter(new StringConverter<>() {
			@Override
			public String toString(LocalDate date) {
				if (date != null) return dateFormatter.format(date);
				else return "";
			}
			@Override
			public LocalDate fromString(String string) {
				if (string != null && !string.isEmpty()) {
					try {
						return LocalDate.parse(string, dateFormatter);
					} catch (DateTimeParseException e) {
						return null;
					}
				} else return null;
			}
		});
		sevaNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		sevaTableView.setItems(selectedSevas);
		table.setupTableView();
		validationServices.initializeTotalCalculation();
		smartActionButton.setOnAction(_ -> handleSmartAction());
		addVisheshaPoojeButton.setOnAction(_ -> handleAddGeneric("Vishesha Pooja", visheshaPoojeComboBox, VisheshaPoojeRepository.getAllVisheshaPooje()));
		sevaListener.initiateSevaListener();
		validationServices.calenderChecker();
		validationServices.radioCheck();
		validationServices.threeNakshatraForARashi();
		validationServices.setupNameValidation();
		validationServices.setupPhoneValidation();
		validationServices.setupPanValidation();
		sevaListener.rashiNakshatraMap();
		refreshSevaCheckboxes();
		populateRashiComboBox();
		refreshVisheshaPoojeComboBox();

		Platform.runLater(() -> devoteeNameField.requestFocus());

		setupFocusTraversal();
		setupFocusLostHandlers();
		setupBlankAreaFocusHandler();

		devoteeNameField.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
		addressField.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
		panNumberField.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
		selectedSevas.addListener((ListChangeListener<SevaEntry>) change -> {
			while (change.next()) {
				for (SevaEntry entry : change.getAddedSubList()) {
					entry.totalAmountProperty().addListener((
							_, _, _) ->
							validationServices.initializeTotalCalculation()
					);
				}
			}
		});
	}

	@FXML
	public void handleDonationMenuItem() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/DonationManager/DonationView.fxml"));
			Stage donationStage = new Stage();
			donationStage.setTitle("ದೇಣಿಗೆ ಸೇರಿಸಿ");
			Scene scene = new Scene(loader.load());
			donationStage.setScene(scene);

			DonationController controller = loader.getController();
			controller.setReceiptPrinter(this.receiptPrinter);

			donationStage.initModality(Modality.WINDOW_MODAL);
			donationStage.initOwner(mainStage);
			donationStage.setResizable(false);
			donationStage.show();
		} catch (IOException e) {
			logger.error("Failed to load the Donation view", e);
			showAlert("UI Load Error", "Failed to load the Donation view: " + e.getMessage());
		}
	}

	public void handleAddGeneric(String type, ComboBox<String> comboBox, List<SevaEntry> repositoryList) {
		String selected = comboBox.getValue();
		if (selected == null || selected.equals("ಆಯ್ಕೆ")) {
			showAlert("Invalid Selection", "Please select a valid " + type + ".");
			return;
		}

		String nameOnly = selected.contains(" - ₹") ? selected.split(" - ₹")[0].trim() : selected;

		boolean exists = selectedSevas.stream().anyMatch(entry -> entry.getName().equals(nameOnly));
		if (exists) {
			showAlert("Duplicate Entry", "The selected " + type + " is already added.");
			return;
		}

		Optional<SevaEntry> matched = repositoryList.stream()
				.filter(entry -> entry.getName().equals(nameOnly))
				.findFirst();
		matched.ifPresent(seva -> {
			SevaEntry newEntry = new SevaEntry(seva.getName(), seva.getAmount());
			selectedSevas.add(newEntry);
			sevaTableView.refresh();
		});
	}

	public void updatePrintStatusLabel() {
		long pendingCount = selectedSevas.stream().filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.PENDING).count();
		long successCount = selectedSevas.stream().filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.SUCCESS).count();
		long failedCount = selectedSevas.stream().filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.FAILED).count();

		Platform.runLater(() -> {
			statusLabel.setText(String.format("ಬಾಕಿ: %d | ಯಶಸ್ವಿ: %d | ವಿಫಲ: %d", pendingCount, successCount, failedCount));
			if (successCount > 0 && pendingCount == 0 && failedCount == 0) {
				long printingCount = selectedSevas.stream().filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.PRINTING).count();
				if (printingCount == 0) {
					smartActionButton.setText("ಎಲ್ಲಾ ಯಶಸ್ವಿ! ಸ್ವಚ್ಛಗೊಳಿಸಲಾಗುತ್ತಿದೆ...");
					smartActionButton.setDisable(true);
					smartActionButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
					new Timeline(new KeyFrame(Duration.seconds(1.5), _ -> clearForm())).play();
				}
			} else if (pendingCount > 0) {
				smartActionButton.setText("ಮುದ್ರಿಸಿ (" + pendingCount + " ಐಟಂಗಳು)");
				smartActionButton.setDisable(false);
				smartActionButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
			} else if (failedCount > 0) {
				smartActionButton.setText("ವಿಫಲವಾದವುಗಳನ್ನು ಮರುಪ್ರಯತ್ನಿಸಿ (" + failedCount + ")");
				smartActionButton.setDisable(false);
				smartActionButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
			} else {
				smartActionButton.setText("ಐಟಂಗಳನ್ನು ಸೇರಿಸಿ");
				smartActionButton.setDisable(selectedSevas.isEmpty());
				smartActionButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
			}
			clearFormButton.setDisable(selectedSevas.isEmpty());
		});
	}

	@FXML
	public void handleShashwathaPoojaMenuItem() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/ShashwathaPoojaManager/ShashwathaPoojaView.fxml"));
			Stage shashwathaStage = new Stage();
			shashwathaStage.setTitle("ಶಾಶ್ವತ ಪೂಜೆ ಸೇರಿಸಿ");
			Scene scene = new Scene(loader.load());
			shashwathaStage.setScene(scene);
			ShashwathaPoojaController controller = loader.getController();
			controller.setReceiptPrinter(this.receiptPrinter);

			shashwathaStage.initModality(Modality.WINDOW_MODAL);
			shashwathaStage.initOwner(mainStage);
			shashwathaStage.setResizable(false);
			shashwathaStage.show();
		} catch (IOException e) {
			logger.error("Failed to load the Shashwatha Pooja view", e);
			showAlert("UI Load Error", "Failed to load the Shashwatha Pooja view: " + e.getMessage());
		}
	}

	@FXML
	public void handleSmartAction() {
		if (!validatePanRequirement()) return;
		double totalAmount = selectedSevas.stream().mapToDouble(SevaEntry::getTotalAmount).sum();
		if (cashRadio.isSelected() && totalAmount > 2000.0) {
			showAlert("Invalid Payment Method", "Cash payment is not allowed for transactions over ₹2000.\nPlease select the 'Online' payment method to proceed.");
			onlineRadio.setSelected(true);
			return;
		}
		long pendingCount = selectedSevas.stream().filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.PENDING).count();
		long failedCount = selectedSevas.stream().filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.FAILED).count();
		long successCount = selectedSevas.stream().filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.SUCCESS).count();
		if (pendingCount > 0 || failedCount > 0) {
			if (failedCount > 0 && pendingCount == 0) receiptServices.handleRetryFailed();
			else receiptServices.handlePrintAllPending();
		} else if (successCount > 0) receiptServices.handleClearSuccessful();
		else {
			Platform.runLater(() -> devoteeNameField.requestFocus());
			showAlert("Add Items", "Please add seva items to the table to proceed.");
		}
	}

// In MainController.java

	private boolean validatePanRequirement() {
		// Get the total from the current items in the table
		double currentCartTotal = selectedSevas.stream().mapToDouble(SevaEntry::getTotalAmount).sum();

		// Get the past daily total calculated by ValidationServices
		double pastDailyTotal = validationServices.getDevoteeDailyCashTotal();

		double grandTotal = currentCartTotal + pastDailyTotal;

		if (grandTotal > 2000.0) {
			String panNumber = panNumberField.getText();
			if (panNumber == null || panNumber.trim().isEmpty()) {
				showAlert("PAN Required", "The devotee's total transactions for today will exceed ₹2000. PAN number is mandatory.\n\nCurrent total: ₹" + String.format("%.2f", grandTotal));
				Platform.runLater(() -> panNumberField.requestFocus());
				return false;
			}
			if (!isValidPanFormat(panNumber.trim())) {
				showAlert("Invalid PAN", "Please enter a valid PAN number format (e.g., AAAPL1234C)");
				Platform.runLater(() -> panNumberField.requestFocus());
				return false;
			}
		}
		return true;
	}

	private boolean isValidPanFormat(String pan) {
		if (pan == null || pan.length() != 10) return false;
		return pan.matches("[A-Z]{5}[0-9]{4}[A-Z]");
	}

	@FXML
	private void handleCloseApp() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		// FIX: Add owner to the Alert
		alert.initOwner(mainStage);
		alert.setTitle("Exit Confirmation");
		alert.setHeaderText(null);
		alert.setContentText("Are you sure you want to exit the application?");
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			logger.info("Application is closing. Performing automatic backup...");
			BackupService.createAutomaticBackup();
			Platform.exit();
			System.exit(0);
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
			logger.error("Failed to load history view", e);
			showAlert("UI Load Error", "Failed to load history view: " + e.getMessage());
		}
	}

	@FXML
	public void handleDonationManagerButton() {
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
					logger.error("Could not get DonationManagerView instance.");
					return;
				}
				donationStage.initModality(Modality.WINDOW_MODAL);
				donationStage.initOwner(mainStage);
				donationStage.setResizable(true);
				donationStage.show();
			} catch (Exception e) {
				logger.error("Failed to load Donation Manager view", e);
				showAlert("UI Load Error", "Failed to load Donation Manager view: " + e.getMessage());
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
					logger.error("Could not get SevaManagerController instance.");
					return;
				}

				sevaStage.initModality(Modality.WINDOW_MODAL);
				sevaStage.initOwner(mainStage);
				sevaStage.setMaxHeight(650);
				sevaStage.setMaxWidth(800);
				sevaStage.show();
			} catch (Exception e) {
				logger.error("Failed to load Seva Manager view", e);
				showAlert("UI Load Error", "Failed to load Seva Manager view: " + e.getMessage());
			}
		};
		promptForSpecialPassword(openSevaManager);
	}

	@FXML
	public void handleVisheshaPoojeManagerButton() {
		Runnable openManager = () -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/VisheshaPoojeManager/VisheshaPoojeManagerView.fxml"));
				Stage stage = new Stage();
				stage.setTitle("ವಿಶೇಷ ಪೂಜೆ ನಿರ್ವಹಿಸಿ");
				Scene scene = new Scene(loader.load());
				stage.setScene(scene);

				VisheshaPoojeManagerController controller = loader.getController();
				if (controller != null) {
					controller.setMainController(this);
				} else {
					logger.error("Could not get VisheshaPoojeManagerController instance.");
					return;
				}
				stage.initOwner(mainStage);
				stage.initModality(Modality.WINDOW_MODAL);
				stage.setMaxWidth(800);
				stage.setMaxHeight(650);
				stage.show();
			} catch (IOException e) {
				logger.error("Unable to load Vishesha Pooja Manager", e);
				showAlert("UI Load Error", "Unable to load Vishesha Pooja Manager: " + e.getMessage());
			}
		};
		promptForSpecialPassword(openManager);
	}

	@FXML
	public void handleShashwathaPoojaManagerButton() {
		Runnable openManager = () -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/ShashwathaPoojaManager/ShashwathaPoojaManagerView.fxml"));
				Stage stage = new Stage();
				stage.setTitle("ಶಾಶ್ವತ ಪೂಜೆ ನಿರ್ವಹಿಸಿ");
				Scene scene = new Scene(loader.load());
				stage.setScene(scene);

				stage.initOwner(mainStage);
				stage.initModality(Modality.WINDOW_MODAL);
				stage.setResizable(false);
				stage.show();
			} catch (IOException e) {
				logger.error("Unable to load Shashwatha Pooja Manager", e);
				showAlert("UI Load Error", "Unable to load Shashwatha Pooja Manager: " + e.getMessage());
			}
		};
		promptForSpecialPassword(openManager);
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
			if (dialogButton == loginButtonType) return password.getText();
			return null;
		});
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(enteredPassword -> {
			CredentialsRepository credentialsRepo = new CredentialsRepository();
			Optional<String> storedHashOpt = credentialsRepo.getCredential("SPECIAL_PASSWORD");
			if (storedHashOpt.isEmpty()) {
				showAlert("Security Error", "Could not find special password in the database.");
				logger.error("SPECIAL_PASSWORD not found in credentials table.");
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
		panNumberField.clear();
		raashiComboBox.getSelectionModel().selectFirst();
		nakshatraComboBox.getSelectionModel().clearSelection();
		sevaDatePicker.setValue(LocalDate.now());
		selectedSevas.clear();
		cashRadio.setSelected(false);
		onlineRadio.setSelected(false);
		visheshaPoojeComboBox.getSelectionModel().selectFirst();
		addressField.clear();
		updatePrintStatusLabel();
		Platform.runLater(() -> devoteeNameField.requestFocus());
	}

	@FXML
	public void clearFormAfterChk() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		// FIX: Add owner to the Alert
		alert.initOwner(mainStage);
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
	public void setMainStage(Stage stage) { this.mainStage = stage; }
	public SevaReceiptRepository sevaReceiptRepository = new SevaReceiptRepository();
	public SevaRepository sevaRepository = SevaRepository.getInstance();
	ValidationServices validationServices = new ValidationServices(this);
	public ReceiptServices receiptServices = new ReceiptServices(this);
	Tables table = new Tables(this);
	public SevaListener sevaListener = new SevaListener(this, this.sevaRepository);
	public void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		// FIX: Add owner to the Alert
		alert.initOwner(mainStage);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void populateRashiComboBox() {
		ObservableList<String> rashiOptions = FXCollections.observableArrayList();
		rashiOptions.add("ಆಯ್ಕೆ");
		rashiOptions.addAll("ಮೇಷ", "ವೃಷಭ", "ಮಿಥುನ", "ಕರ್ಕಾಟಕ", "ಸಿಂಹ", "ಕನ್ಯಾ", "ತುಲಾ", "ವೃಶ್ಚಿಕ", "ಧನು", "ಮಕರ", "ಕುಂಭ", "ಮೀನ");
		raashiComboBox.setItems(rashiOptions);
		raashiComboBox.getSelectionModel().selectFirst();
	}

	private void setupBlankAreaFocusHandler() {
		mainPane.setOnMousePressed(event -> {
			if (!(event.getTarget() instanceof TextInputControl) && !(event.getTarget() instanceof ComboBox) && !(event.getTarget() instanceof DatePicker)) {
				mainPane.requestFocus();
			}
		});
	}

	private void setupFocusLostHandlers() {
		contactField.focusedProperty().addListener((
				_, _, newVal) -> {
			if (!newVal) {
				String phone = contactField.getText();
				if (!phone.matches("\\d{10}")) {
					showAlert("Invalid Input", "Enter a valid 10-digit mobile number.");
				}
			}
		});
	}

	public void refreshSevaCheckboxes() {
		logger.debug("MainController refreshSevaCheckboxes() called.");
		if (sevaListener != null && sevaCheckboxContainer != null) {
			try {
				sevaRepository.loadSevasFromDB();
				sevaCheckboxContainer.getChildren().clear();
				sevaCheckboxMap.clear();
				sevaListener.setupSevaCheckboxes();
				logger.debug("Seva checkboxes refreshed with {} sevas.", sevaRepository.getAllSevas().size());
			} catch (Exception e) {
				logger.error("Error during refreshSevaCheckboxes", e);
				showAlert("Refresh Error", "Could not refresh Seva list in main view.");
			}
		} else {
			logger.error("Cannot refresh checkboxes: SevaListener or Container is null.");
			if (sevaListener == null) logger.error("sevaListener is null");
			if (sevaCheckboxContainer == null) logger.error("sevaCheckboxContainer is null");
		}
	}

	public void refreshVisheshaPoojeComboBox() {
		VisheshaPoojeRepository.loadVisheshaPoojeFromDB();
		List<SevaEntry> entries = VisheshaPoojeRepository.getAllVisheshaPooje();
		ObservableList<String> names = FXCollections.observableArrayList(
				entries.stream()
						.map(seva -> seva.getName() + " - ₹" + String.format("%.2f", seva.getAmount()))
						.collect(Collectors.toList())
		);
		names.addFirst("ಆಯ್ಕೆ");
		visheshaPoojeComboBox.setItems(names);
		logger.debug("Vishesha Pooja ComboBox refreshed with {} items.", entries.size());
	}

	private void setupFocusTraversal() {
		List<Control> formControls = List.of(devoteeNameField, contactField, panNumberField, raashiComboBox, nakshatraComboBox, sevaDatePicker);
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
			logger.error("Failed to load the In-Kind Donation view", e);
			showAlert("UI Load Error", "Failed to load the In-Kind Donation view: " + e.getMessage());
		}
	}

	public void populateDevoteeDetails(DevoteeDetails details) {
		if (details == null) return;
		devoteeNameField.setText(details.getName() != null ? details.getName() : "");
		addressField.setText(details.getAddress() != null ? details.getAddress() : "");
		panNumberField.setText(details.getPanNumber() != null ? details.getPanNumber() : "");
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

	@FXML
	public void handleBackupAndRestore() {
		BackupService.showBackupRestoreDialog(mainStage);
	}

	@FXML
	public void handleKaryakramaMenuItem() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/Karyakrama/KaryakramaView.fxml"));
			Stage stage = new Stage();
			stage.setTitle("ಕಾರ್ಯಕ್ರಮದ ಇತರೆ ರಶೀದಿ");
			stage.setScene(new Scene(loader.load()));
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(mainStage);
			stage.setResizable(false);
			stage.show();
		} catch (IOException e) {
			logger.error("Failed to load the Karyakrama view", e);
			showAlert("UI Load Error", "Failed to load the Karyakrama view: " + e.getMessage());
		}
	}

	@FXML
	public void handleKaryakramaManagerButton() {
		Runnable openManager = () -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/KaryakramaManager/KaryakramaManagerView.fxml"));
				Stage stage = new Stage();
				stage.setTitle("ಕಾರ್ಯಕ್ರಮ ನಿರ್ವಹಿಸಿ");
				stage.setScene(new Scene(loader.load()));
				stage.initOwner(mainStage);
				stage.initModality(Modality.WINDOW_MODAL);
				stage.setWidth(800);
				stage.setHeight(500);
				stage.show();
			} catch (IOException e) {
				logger.error("Unable to load Karyakrama Manager", e);
				showAlert("UI Load Error", "Unable to load Karyakrama Manager: " + e.getMessage());
			}
		};
		promptForSpecialPassword(openManager);
	}

	@FXML
	public void handleOthersManagerButton() {
		Runnable openManager = () -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/KaryakramaManager/OthersManagerView.fxml"));
				Stage stage = new Stage();
				stage.setTitle("ಇತರೆ ನಿರ್ವಹಿಸಿ (Manage Others)");
				stage.setScene(new Scene(loader.load()));
				stage.initOwner(mainStage);
				stage.initModality(Modality.WINDOW_MODAL);
				stage.setWidth(700);
				stage.setHeight(600);
				stage.show();
			} catch (IOException e) {
				logger.error("Unable to load Others Manager", e);
				showAlert("UI Load Error", "Unable to load Others Manager: " + e.getMessage());
			}
		};
		promptForSpecialPassword(openManager);
	}

	@FXML
	public void handlePrinterSettings() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/PrinterSettingsView.fxml"));
			Stage settingsStage = new Stage();
			settingsStage.setTitle("Printer Settings");
			settingsStage.initModality(Modality.WINDOW_MODAL);
			settingsStage.initOwner(mainStage);
			settingsStage.setScene(new Scene(loader.load()));
			settingsStage.setResizable(false);
			settingsStage.showAndWait();
		} catch (IOException e) {
			logger.error("Could not load printer settings view", e);
			showAlert("UI Load Error", "Could not load printer settings view.");
		}
	}
}