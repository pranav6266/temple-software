package com.pranav.temple_software.controllers.menuControllers.InKindDonationManager;

import ch.qos.logback.classic.Logger;
import com.pranav.temple_software.models.DevoteeDetails;
import com.pranav.temple_software.models.InKindDonation;
import com.pranav.temple_software.repositories.DevoteeRepository;
import com.pranav.temple_software.repositories.InKindDonationRepository;
import com.pranav.temple_software.utils.ReceiptPrinter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InKindDonationController {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(InKindDonationController.class);
	@FXML private TextField devoteeNameField;
	@FXML private TextField contactField;
	@FXML private DatePicker donationDatePicker;
	@FXML private ComboBox<String> raashiComboBox;
	@FXML private ComboBox<String> nakshatraComboBox;
	@FXML private TextArea addressField;
	@FXML private TextField panNumberField;
	@FXML private TextArea itemDescriptionArea;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;

	private final InKindDonationRepository repository = new InKindDonationRepository();
	private final Map<String, List<String>> rashiNakshatraMap = new HashMap<>();
	private final DevoteeRepository devoteeRepository = new DevoteeRepository();

	@FXML
	public void initialize() {
		donationDatePicker.setValue(LocalDate.now());
		populateRashiComboBox();
		setupRashiNakshatraListener();
		setupPhoneNumberListener();
		devoteeNameField.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
		itemDescriptionArea.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
		panNumberField.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
		addressField.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
	}

	private void setupPhoneNumberListener() {
		contactField.textProperty().addListener((
				_, _, newValue) -> {
			if (newValue != null) {
				String digitsOnly = newValue.replaceAll("\\D", "");
				if (digitsOnly.length() > 10) {
					digitsOnly = digitsOnly.substring(0, 10);
				}

				if (!digitsOnly.equals(newValue)) {
					contactField.setText(digitsOnly);
				}

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
	public void handleSave() {
		if (!validateInput()) {
			return;
		}
		String rashiValue = raashiComboBox.getValue();
		String finalRashi = (rashiValue != null && rashiValue.equals("ಆಯ್ಕೆ")) ? "" : rashiValue;

		InKindDonation newDonation = new InKindDonation(
				0, devoteeNameField.getText(), contactField.getText(), addressField.getText(),
				panNumberField.getText(), finalRashi, nakshatraComboBox.getValue(),
				donationDatePicker.getValue(), itemDescriptionArea.getText()
		);
		try {
			// --- MODIFIED LOGIC: SAVE FIRST ---
			int actualSavedId = repository.saveInKindDonation(newDonation);

			if (actualSavedId == -1) {
				showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save in-kind donation.");
				return;
			}
			// --- END MODIFIED LOGIC ---

			ReceiptPrinter receiptPrinter = new ReceiptPrinter();
			Consumer<Boolean> afterActionCallback = (success) -> {
				// The save is already done, so we just close the window
				Platform.runLater(this::closeWindow);
			};

			Runnable onDialogClosed = this::closeWindow;
			Stage ownerStage = (Stage) saveButton.getScene().getWindow();

			// Create the object for the preview using the *actual* ID
			InKindDonation previewDonation = new InKindDonation(
					actualSavedId, newDonation.getDevoteeName(), newDonation.getPhoneNumber(), newDonation.getAddress(), newDonation.getPanNumber(),
					newDonation.getRashi(), newDonation.getNakshatra(),
					newDonation.getDonationDate(), newDonation.getItemDescription()
			);
			receiptPrinter.showInKindDonationPrintPreview(previewDonation, ownerStage, afterActionCallback, onDialogClosed);
		} catch (Exception e) {
			logger.error("Failed to open print preview", e);
			showAlert(Alert.AlertType.ERROR, "Print Error", "Failed to open print preview: " + e.getMessage());
			closeWindow();
		}
	}

	private boolean validateInput() {
		if (devoteeNameField.getText() == null || devoteeNameField.getText().trim().isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Devotee Name is required.");
			return false;
		}
		if (itemDescriptionArea.getText() == null || itemDescriptionArea.getText().trim().isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Item Description is required.");
			return false;
		}
		if (donationDatePicker.getValue() == null) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Donation Date is required.");
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
		rashiNakshatraMap.put("ಮೇಷ", Arrays.asList("ಅಶ್ವಿನಿ", "ಭರಣಿ", "ಕೃತ್ತಿಕ"));
		rashiNakshatraMap.put("ವೃಷಭ", Arrays.asList("ಕೃತ್ತಿಕ", "ರೋಹಿಣಿ", "ಮೃಗಶಿರ"));
		rashiNakshatraMap.put("ಮಿಥುನ", Arrays.asList("ಮೃಗಶಿರ", "ಆರ್ದ್ರ", "ಪುನರ್ವಸು"));
		rashiNakshatraMap.put("ಕರ್ಕಾಟಕ", Arrays.asList("ಪುನರ್ವಸು", "ಪುಷ್ಯ", "ಆಶ್ಲೇಷ"));
		rashiNakshatraMap.put("ಸಿಂಹ", Arrays.asList("ಮಘ", "ಪೂರ್ವ", "ಉತ್ತರ"));
		rashiNakshatraMap.put("ಕನ್ಯಾ", Arrays.asList("ಉತ್ತರ", "ಹಸ್ತ", "ಚಿತ್ರ"));
		rashiNakshatraMap.put("ತುಲಾ", Arrays.asList("ಚಿತ್ರ", "ಸ್ವಾತಿ", "ವಿಶಾಖ"));
		rashiNakshatraMap.put("ವೃಶ್ಚಿಕ", Arrays.asList("ವಿಶಾಖ", "ಅನುರಾಧ", "ಜೇಷ್ಠ"));
		rashiNakshatraMap.put("ಧನು", Arrays.asList("ಮೂಲ", "ಪೂರ್ವಾಷಾಢ", "ಉತ್ತರಾಷಾಢ"));
		rashiNakshatraMap.put("ಮಕರ", Arrays.asList("ಉತ್ತರಾಷಾಢ", "ಶ್ರವಣ", "ಧನಿಷ್ಠ"));
		rashiNakshatraMap.put("ಕುಂಭ", Arrays.asList("ಧನಿಷ್ಠ", "ಶತಭಿಷ", "ಪೂರ್ವಾಭಾದ್ರ"));
		rashiNakshatraMap.put("ಮೀನ", Arrays.asList("ಪೂರ್ವಾಭಾದ", "ಉತ್ತರಾಭಾದ್ರ", "ರೇವತಿ"));

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