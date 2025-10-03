package com.pranav.temple_software.controllers.menuControllers.ShashwathaPoojaManager;

import com.pranav.temple_software.models.DevoteeDetails;
import com.pranav.temple_software.models.ShashwathaPoojaReceipt;
import com.pranav.temple_software.repositories.CredentialsRepository;
import com.pranav.temple_software.repositories.DevoteeRepository;
import com.pranav.temple_software.repositories.ShashwathaPoojaRepository;
import com.pranav.temple_software.utils.ReceiptPrinter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ShashwathaPoojaController {

	private static final Logger logger = LoggerFactory.getLogger(ShashwathaPoojaController.class);

	@FXML private TextField devoteeNameField;
	@FXML private TextField contactField;
	@FXML private DatePicker receiptDatePicker;
	@FXML private TextField poojaDateField;
	@FXML private ComboBox<String> raashiComboBox;
	@FXML private ComboBox<String> nakshatraComboBox;
	@FXML private TextArea addressField;
	@FXML private TextField panNumberField;
	@FXML private Label amountLabel;
	@FXML private RadioButton cashRadio;
	@FXML private RadioButton onlineRadio;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;

	private final ShashwathaPoojaRepository repository = new ShashwathaPoojaRepository();
	private final Map<String, List<String>> rashiNakshatraMap = new HashMap<>();
	private ReceiptPrinter receiptPrinter;
	private final DevoteeRepository devoteeRepository = new DevoteeRepository();
	private final CredentialsRepository credentialsRepository = new CredentialsRepository();
	private double currentPoojaAmount = 1000.0;

	public void setReceiptPrinter(ReceiptPrinter printer) {
		this.receiptPrinter = printer;
	}

	@FXML
	public void initialize() {
		receiptDatePicker.setValue(LocalDate.now());
		populateRashiComboBox();
		setupRashiNakshatraListener();
		setupPhoneNumberListener();
		devoteeNameField.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
		panNumberField.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
		loadAndDisplayAmount();
	}

	private void loadAndDisplayAmount() {
		Optional<String> amountOpt = credentialsRepository.getCredential("SHASHWATHA_POOJA_PRICE");
		amountOpt.ifPresent(s -> {
			try {
				currentPoojaAmount = Double.parseDouble(s);
			} catch (NumberFormatException e) {
				System.err.println("Could not parse Shashwatha Pooja price from DB, using default.");
			}
		});
		amountLabel.setText(String.format("₹%.2f", currentPoojaAmount));
	}

	// Replace the existing setupPhoneNumberListener method with this one
	private void setupPhoneNumberListener() {
		contactField.textProperty().addListener((_, _, newValue) -> {
			if (newValue != null) {
				// First, ensure only up to 10 digits can be entered
				String digitsOnly = newValue.replaceAll("\\D", "");
				if (digitsOnly.length() > 10) {
					digitsOnly = digitsOnly.substring(0, 10);
				}

				// prevent listener recursion
				if (!digitsOnly.equals(newValue)) {
					contactField.setText(digitsOnly);
				}

				// --- NEW LOGIC ---
				// If the new value has exactly 10 digits, trigger the search
				if (digitsOnly.length() == 10) {
					devoteeRepository.findLatestDevoteeDetailsByPhone(digitsOnly)
							.ifPresent(this::populateDevoteeDetails);
				}
			}
		});
	}
	private void populateDevoteeDetails(DevoteeDetails details) {
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
	private void handleSaveAndPrint() {
		if (!validateInput()) {
			return;
		}
		// Create the data object, but DON'T save it yet
		String paymentMode = cashRadio.isSelected() ? "Cash" : "Online";
		ShashwathaPoojaReceipt newReceipt = new ShashwathaPoojaReceipt(
				0, devoteeNameField.getText(), contactField.getText(), addressField.getText(),
				panNumberField.getText(), raashiComboBox.getValue(), nakshatraComboBox.getValue(),
				receiptDatePicker.getValue(), poojaDateField.getText(), currentPoojaAmount, paymentMode
		);

		if (receiptPrinter != null) {
			// FIX: Database saving logic is now inside this callback
			Consumer<Boolean> afterActionCallback = (success) -> {
				if (success) {
					repository.saveShashwathaPooja(newReceipt);
					Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Success", "Shashwatha Pooja receipt saved successfully!"));
				}
				Platform.runLater(this::closeWindow);
			};

			Runnable onDialogClosed = this::closeWindow;
			try {
				Stage ownerStage = (Stage) saveButton.getScene().getWindow();
				// Pass the unsaved object for preview.
				int provisionalId = repository.getNextReceiptId();
				ShashwathaPoojaReceipt previewReceipt = new ShashwathaPoojaReceipt(provisionalId, newReceipt.getDevoteeName(), newReceipt.getPhoneNumber(), newReceipt.getAddress(), newReceipt.getPanNumber(), newReceipt.getRashi(), newReceipt.getNakshatra(), newReceipt.getReceiptDate(), newReceipt.getPoojaDate(), newReceipt.getAmount(), newReceipt.getPaymentMode());
				receiptPrinter.showShashwathaPoojaPrintPreview(previewReceipt, ownerStage, afterActionCallback, onDialogClosed);
			} catch (Exception e) {
				logger.error("Failed to open print preview for Shashwatha Pooja", e);
				showAlert(Alert.AlertType.ERROR, "Print Error", "Failed to open print preview: " + e.getMessage());
				closeWindow();
			}
		} else {
			showAlert(Alert.AlertType.ERROR, "Print Error", "Receipt printer is not initialized.");
			closeWindow();
		}
	}

	private boolean validateInput() {
		if (devoteeNameField.getText() == null || devoteeNameField.getText().trim().isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Devotee Name is required.");
			return false;
		}
		if (poojaDateField.getText() == null || poojaDateField.getText().trim().isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Pooja Date/Detail is required.");
			return false;
		}
		if (receiptDatePicker.getValue() == null) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Receipt Date is required.");
			return false;
		}
		if (!cashRadio.isSelected() && !onlineRadio.isSelected()) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a payment method.");
			return false;
		}
		String pan = panNumberField.getText();
		if (pan != null && !pan.trim().isEmpty() && !isValidPanFormat(pan.trim())) {
			showAlert(Alert.AlertType.WARNING, "Invalid PAN Format", "Please enter a valid PAN number format (e.g., AAAPL1234C)");
			return false;
		}
		return true;
	}

	private boolean isValidPanFormat(String pan) {
		if (pan == null || pan.length() != 10) {
			return false;
		}
		return pan.matches("[A-Z]{5}[0-9]{4}[A-Z]");
	}

	@FXML
	private void handleCancel() {
		closeWindow();
	}

	private void closeWindow() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
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

	private void setupRashiNakshatraListener() {
		rashiNakshatraMap.put("ಮೇಷ", Arrays.asList("ಅಶ್ವಿನಿ", "ಭರಣಿ", "ಕೃತಿಕ"));
		rashiNakshatraMap.put("ವೃಷಭ", Arrays.asList("ಕೃತಿಕ", "ರೋಹಿಣಿ", "ಮೃಗಶಿರ"));
		rashiNakshatraMap.put("ಮಿಥುನ", Arrays.asList("ಮೃಗಶಿರ", "ಆರ್ದ್ರ", "ಪುನರ್ವಸು"));
		rashiNakshatraMap.put("ಕರ್ಕ", Arrays.asList("ಪುನರ್ವಸು", "ಪುಷ್ಯ", "ಆಶ್ಲೇಷ"));
		rashiNakshatraMap.put("ಸಿಂಹ", Arrays.asList("ಮಘ", "ಪೂರ್ವ ಫಲ್ಗುನಿ", "ಉತ್ತರ ಫಲ್ಗುನಿ"));
		rashiNakshatraMap.put("ಕನ್ಯಾ", Arrays.asList("ಉತ್ತರ ಫಲ್ಗುನಿ", "ಹಸ್ತ", "ಚಿತ್ರ"));
		rashiNakshatraMap.put("ತುಲಾ", Arrays.asList("ಚಿತ್ರ", "ಸ್ವಾತಿ", "ವಿಶಾಖ"));
		rashiNakshatraMap.put("ವೃಶ್ಚಿಕ", Arrays.asList("ವಿಶಾಖ", "ಅನುರಾಧ", "ಜ್ಯೇಷ್ಠ"));
		rashiNakshatraMap.put("ಧನುಸ್", Arrays.asList("ಮೂಲ", "ಪೂರ್ವ ಆಷಾಢ", "ಉತ್ತರ ಆಷಾಢ"));
		rashiNakshatraMap.put("ಮಕರ", Arrays.asList("ಉತ್ತರ ಆಷಾಢ", "ಶ್ರವಣ", "ಧನಿಷ್ಠ"));
		rashiNakshatraMap.put("ಕುಂಭ", Arrays.asList("ಧನಿಷ್ಠ", "ಶತಭಿಷ", "ಪೂರ್ವ ಭಾದ್ರಪದ"));
		rashiNakshatraMap.put("ಮೀನ", Arrays.asList("ಪೂರ್ವ ಭಾದ್ರಪದ", "ಉತ್ತರ ಭಾದ್ರಪದ", "ರೇವತಿ"));

		nakshatraComboBox.setDisable(true);
		raashiComboBox.getSelectionModel().selectedItemProperty().addListener(
				(_, _, newVal) -> {
					if (newVal == null || newVal.equals("ಆಯ್ಕೆ")) {
						nakshatraComboBox.setDisable(true);
						nakshatraComboBox.getItems().clear();
					} else {
						List<String> nakshatras = rashiNakshatraMap.get(newVal);
						if (nakshatras != null) {
							nakshatraComboBox.setItems(FXCollections.observableArrayList(nakshatras));
							nakshatraComboBox.setDisable(false);
						}
					}
				});
	}

	private void showAlert(Alert.AlertType alertType, String title, String message) {
		Alert alert = new Alert(alertType);
		if (saveButton != null && saveButton.getScene() != null) {
			alert.initOwner(saveButton.getScene().getWindow());
		}
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}