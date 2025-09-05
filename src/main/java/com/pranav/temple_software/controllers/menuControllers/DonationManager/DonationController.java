package com.pranav.temple_software.controllers.menuControllers.DonationManager;

import com.pranav.temple_software.models.DevoteeDetails;
import com.pranav.temple_software.models.DonationReceiptData;
import com.pranav.temple_software.models.Donations;
import com.pranav.temple_software.repositories.DevoteeRepository;
import com.pranav.temple_software.repositories.DonationReceiptRepository;
import com.pranav.temple_software.repositories.DonationRepository;
import com.pranav.temple_software.utils.ReceiptPrinter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DonationController {

	@FXML private TextField devoteeNameField;
	@FXML private TextField contactField;
	@FXML private DatePicker donationDatePicker;
	@FXML private ComboBox<String> donationComboBox;
	@FXML private TextField amountField;
	@FXML private ComboBox<String> raashiComboBox;
	@FXML private ComboBox<String> nakshatraComboBox;
	@FXML private TextArea addressField;
	@FXML private TextField panNumberField;
	@FXML private RadioButton cashRadio;
	@FXML private RadioButton onlineRadio;
	@FXML private ToggleGroup paymentGroup;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;

	private ReceiptPrinter receiptPrinter;
	private final DonationRepository donationRepository = DonationRepository.getInstance();
	private final DonationReceiptRepository donationReceiptRepository = new DonationReceiptRepository();
	private final DevoteeRepository devoteeRepository = new DevoteeRepository();
	private final Map<String, List<String>> rashiNakshatraMap = new HashMap<>();

	public void setReceiptPrinter(ReceiptPrinter printer) {
		this.receiptPrinter = printer;
	}

	@FXML
	public void initialize() {
		donationDatePicker.setValue(LocalDate.now());
		populateDonationComboBox();
		populateRashiComboBox();
		setupRashiNakshatraListener();
		setupPhoneNumberListener();
		setupFieldFormatters();
	}

	private void setupFieldFormatters() {
		devoteeNameField.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
		panNumberField.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
		amountField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null && !newValue.matches("\\d*(\\.\\d*)?")) {
				amountField.setText(oldValue);
			}
		});
	}

	private void setupPhoneNumberListener() {
		contactField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				if (!newValue.matches("\\d*")) {
					contactField.setText(newValue.replaceAll("[^\\d]", ""));
				}
				if (newValue.length() > 10) {
					contactField.setText(newValue.substring(0, 10));
				}
			}
		});

		contactField.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal) {
				String phoneNumber = contactField.getText();
				if (phoneNumber != null && phoneNumber.length() == 10) {
					Optional<DevoteeDetails> detailsOpt = devoteeRepository.findLatestDevoteeDetailsByPhone(phoneNumber);
					detailsOpt.ifPresent(this::populateDevoteeDetails);
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

		String paymentMode = cashRadio.isSelected() ? "Cash" : "Online";
		double amount = Double.parseDouble(amountField.getText());
		int receiptId = DonationReceiptRepository.getNextDonationReceiptId();

		DonationReceiptData newReceipt = new DonationReceiptData(
				receiptId,
				devoteeNameField.getText(),
				contactField.getText(),
				addressField.getText(),
				panNumberField.getText(),
				raashiComboBox.getValue(),
				nakshatraComboBox.getValue(),
				donationDatePicker.getValue(),
				donationComboBox.getValue(),
				amount,
				paymentMode
		);

		int savedId = donationReceiptRepository.saveSpecificDonationReceipt(
				receiptId, newReceipt.getDevoteeName(), newReceipt.getPhoneNumber(), newReceipt.getAddress(),
				newReceipt.getPanNumber(), newReceipt.getRashi(), newReceipt.getNakshatra(), newReceipt.getSevaDate(),
				newReceipt.getDonationName(), newReceipt.getDonationAmount(), newReceipt.getPaymentMode()
		);

		if (savedId != -1) {
			if (receiptPrinter != null) {
				Consumer<Boolean> onPrintComplete = (printSuccess) -> Platform.runLater(this::closeWindow);
				Runnable onDialogClosed = this::closeWindow;
				Stage ownerStage = (Stage) saveButton.getScene().getWindow();
				receiptPrinter.showDonationPrintPreview(newReceipt, ownerStage, onPrintComplete, onDialogClosed);
			} else {
				showAlert(Alert.AlertType.INFORMATION, "Success", "Receipt saved successfully, but printer is not configured.");
				closeWindow();
			}
		} else {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save the donation receipt.");
		}
	}

	private boolean validateInput() {
		if (devoteeNameField.getText() == null || devoteeNameField.getText().trim().isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Devotee Name is required.");
			return false;
		}
		if (donationComboBox.getValue() == null || donationComboBox.getValue().equals("ಆಯ್ಕೆ")) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a donation type.");
			return false;
		}
		try {
			double amount = Double.parseDouble(amountField.getText());
			if (amount <= 0) {
				showAlert(Alert.AlertType.WARNING, "Validation Error", "Amount must be greater than zero.");
				return false;
			}
		} catch (NumberFormatException e) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a valid amount.");
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
		return pan.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}");
	}

	@FXML
	private void handleCancel() {
		closeWindow();
	}

	private void closeWindow() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}

	private void populateDonationComboBox() {
		donationRepository.loadDonationsFromDB();
		List<String> donationNames = donationRepository.getAllDonations().stream()
				.map(Donations::getName)
				.collect(Collectors.toList());
		donationNames.add(0, "ಆಯ್ಕೆ");
		donationComboBox.setItems(FXCollections.observableArrayList(donationNames));
	}

	private void populateRashiComboBox() {
		ObservableList<String> rashiOptions = FXCollections.observableArrayList();
		rashiOptions.add("ಆಯ್ಕೆ");
		rashiOptions.addAll("ಮೇಷ", "ವೃಷಭ", "ಮಿಥುನ", "ಕರ್ಕಾಟಕ", "ಸಿಂಹ", "ಕನ್ಯಾ", "ತುಲಾ", "ವೃಶ್ಚಿಕ", "ಧನು", "ಮಕರ", "ಕುಂಭ", "ಮೀನ");
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
		raashiComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
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
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}