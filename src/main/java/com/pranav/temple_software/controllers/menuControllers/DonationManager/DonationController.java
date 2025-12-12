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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	@FXML private Button saveButton;
	@FXML private Button cancelButton;

	private ReceiptPrinter receiptPrinter;
	private final DonationRepository donationRepository = DonationRepository.getInstance();
	private final DonationReceiptRepository donationReceiptRepository = new DonationReceiptRepository();
	private final DevoteeRepository devoteeRepository = new DevoteeRepository();

	// --- NEW FIELDS for 2-way Rashi/Nakshatra binding ---
	private final Map<String, List<String>> rashiToNakshatraMap = new HashMap<>();
	private final Map<String, String> nakshatraToRashiMap = new HashMap<>();
	private final ObservableList<String> allRashis;
	private final ObservableList<String> allNakshatras;
	private boolean isUpdatingNakshatra = false; // Flag to prevent infinite loops

	public DonationController() {
		// --- NEW: Initialize master lists ---
		allRashis = FXCollections.observableArrayList(
				"ಆಯ್ಕೆ", "ಮೇಷ", "ವೃಷಭ", "ಮಿಥುನ", "ಕರ್ಕಾಟಕ", "ಸಿಂಹ", "ಕನ್ಯಾ",
				"ತುಲಾ", "ವೃಶ್ಚಿಕ", "ಧನು", "ಮಕರ", "ಕುಂಭ", "ಮೀನ"
		);
		allNakshatras = FXCollections.observableArrayList(
				"ಆಯ್ಕೆ", "ಅಶ್ವಿನಿ", "ಭರಣಿ", "ಕೃತ್ತಿಕ", "ರೋಹಿಣಿ", "ಮೃಗಶಿರ", "ಆರ್ದ್ರ",
				"ಪುನರ್ವಸು", "ಪುಷ್ಯ", "ಆಶ್ಲೇಷ", "ಮಘ", "ಪೂರ್ವ", "ಉತ್ತರ", "ಹಸ್ತ", "ಚಿತ್ರ",
				"ಸ್ವಾತಿ", "ವಿಶಾಖ", "ಅನುರಾಧ", "ಜೇಷ್ಠ", "ಮೂಲ", "ಪೂರ್ವಾಷಾಢ", "ಉತ್ತರಾಷಾಢ",
				"ಶ್ರವಣ", "ಧನಿಷ್ಠ", "ಶತಭಿಷ", "ಪೂರ್ವಾಭಾದ್ರ", "ಉತ್ತರಾಭಾದ್ರ", "ರೇವತಿ"
		);

		// Initialize both maps
		initializeRashiMaps();
	}

	public void setReceiptPrinter(ReceiptPrinter printer) {
		this.receiptPrinter = printer;
	}

	@FXML
	public void initialize() {
		donationDatePicker.setValue(LocalDate.now());
		populateDonationComboBox();

		// --- MODIFIED: Call new listener setup ---
		setupNakshatraToRashiListener();

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
		amountField.textProperty().addListener((_, oldValue, newValue) -> {
			if (newValue != null && !newValue.matches("\\d*(\\.\\d*)?")) {
				amountField.setText(oldValue);
			}
		});
		addressField.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
	}

	private void setupPhoneNumberListener() {
		contactField.textProperty().addListener((_, _, newValue) -> {
			if (newValue != null) {
				String digitsOnly = newValue.replaceAll("\\D", "");
				if (digitsOnly.length() > 10) {
					digitsOnly = digitsOnly.substring(0, 10);
				}

				if (!digitsOnly.equals(newValue)) {
					contactField.setText(digitsOnly);
				}

				if (digitsOnly.length() == 10) {
					// --- MODIFIED: Wrap populateDevoteeDetails in the flag ---
					isUpdatingNakshatra = true;
					devoteeRepository.findLatestDevoteeDetailsByPhone(digitsOnly)
							.ifPresent(details -> Platform.runLater(() -> {
								populateDevoteeDetails(details);
								isUpdatingNakshatra = false;
							}));
					if (!devoteeRepository.findLatestDevoteeDetailsByPhone(digitsOnly).isPresent()) {
						isUpdatingNakshatra = false; // Ensure flag is reset if no devotee is found
					}
					// --- END MODIFICATION ---
				}
			}
		});
	}

	private void populateDevoteeDetails(DevoteeDetails details) {
		if (details == null) return;
		devoteeNameField.setText(details.getName() != null ? details.getName() : "");
		addressField.setText(details.getAddress() != null ? details.getAddress() : "");
		panNumberField.setText(details.getPanNumber() != null ? details.getPanNumber() : "");

		// --- MODIFIED: Set Nakshatra first, then Rashi ---
		if (details.getNakshatra() != null && !details.getNakshatra().isEmpty()) {
			nakshatraComboBox.setValue(details.getNakshatra());
		} else {
			nakshatraComboBox.getSelectionModel().selectFirst();
		}

		if (details.getRashi() != null && !details.getRashi().isEmpty()) {
			raashiComboBox.setValue(details.getRashi());
		} else {
			// If Rashi is empty, let the Nakshatra listener auto-populate it
			if (details.getNakshatra() == null || details.getNakshatra().isEmpty()) {
				raashiComboBox.getSelectionModel().selectFirst();
			}
		}
	}

	// Add this field at the top of the class
	private final com.pranav.temple_software.services.ValidationServices validationServices = new com.pranav.temple_software.services.ValidationServices();

	@FXML
	private void handleSaveAndPrint() {
		if (!validateInput()) {
			return;
		}

		double amount = Double.parseDouble(amountField.getText());
		String paymentMode = cashRadio.isSelected() ? "Cash" : "Online";

		// --- CENTRALIZED VALIDATION CHECK ---
		String errorMsg = validationServices.validateTransaction(
				contactField.getText(),
				amount,
				paymentMode,
				panNumberField.getText()
		);

		if (errorMsg != null) {
			showAlert(Alert.AlertType.WARNING, "Transaction Validation Failed", errorMsg);
			// If cash limit error, switch to online automatically for convenience
			if (errorMsg.contains("Cash Limit Exceeded")) {
				onlineRadio.setSelected(true);
			}
			return; // Stop saving
		}
		// ------------------------------------

		String rashiValue = raashiComboBox.getValue();
		String finalRashi = (rashiValue != null && rashiValue.equals("ಆಯ್ಕೆ")) ? "" : rashiValue;
		String nakshatra = nakshatraComboBox.getValue();
		String finalNakshatra = (nakshatra != null && nakshatra.equals("ಆಯ್ಕೆ")) ? "" : nakshatra;

		// 1. Define Lazy Save Action
		java.util.function.Supplier<Integer> lazySaveAction = () -> {
			return donationReceiptRepository.saveDonationReceipt(
					devoteeNameField.getText(), contactField.getText(), addressField.getText(),
					panNumberField.getText(), finalRashi, finalNakshatra,
					donationDatePicker.getValue(), donationComboBox.getValue(), amount, paymentMode
			);
		};

		// 2. Create Temp Data Object
		DonationReceiptData tempReceipt = new DonationReceiptData(
				0, devoteeNameField.getText(), contactField.getText(), addressField.getText(), panNumberField.getText(),
				finalRashi, finalNakshatra, donationDatePicker.getValue(),
				donationComboBox.getValue(), amount, paymentMode
		);

		// 3. Setup Callbacks
		if (receiptPrinter != null) {
			java.util.function.Consumer<Boolean> afterActionCallback = (success) -> {
				if (success) {
					Platform.runLater(this::closeWindow);
				}
			};

			Runnable onDialogClosed = () -> {};

			Stage ownerStage = (Stage) saveButton.getScene().getWindow();
			receiptPrinter.showDonationPrintPreview(tempReceipt, ownerStage, afterActionCallback, onDialogClosed, lazySaveAction);

		} else {
			showAlert(Alert.AlertType.INFORMATION, "Success", "Printer is not configured.");
			closeWindow();
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

	private void populateDonationComboBox() {
		donationRepository.loadDonationsFromDB();
		List<String> donationNames = donationRepository.getAllDonations().stream()
				.map(Donations::getName)
				.collect(Collectors.toList());
		donationNames.addFirst("ಆಯ್ಕೆ");
		donationComboBox.setItems(FXCollections.observableArrayList(donationNames));
	}

	// --- THIS METHOD IS NOW OBSOLETE ---
	private void populateRashiComboBox() {
		// This is now handled by setupNakshatraToRashiListener
	}

	// --- THIS METHOD IS REPLACED ---
	private void setupRashiNakshatraListener() {
		// This method is now obsolete and replaced by setupNakshatraToRashiListener
	}

	// --- *** NEW, REVERSIBLE LISTENER LOGIC *** ---
	private void initializeRashiMaps() {
		// Forward Map (Rashi -> Nakshatras)
		rashiToNakshatraMap.put("ಮೇಷ", Arrays.asList("ಅಶ್ವಿನಿ", "ಭರಣಿ", "ಕೃತ್ತಿಕ"));
		rashiToNakshatraMap.put("ವೃಷಭ", Arrays.asList("ಕೃತ್ತಿಕ", "ರೋಹಿಣಿ", "ಮೃಗಶಿರ"));
		rashiToNakshatraMap.put("ಮಿಥುನ", Arrays.asList("ಮೃಗಶಿರ", "ಆರ್ದ್ರ", "ಪುನರ್ವಸು"));
		rashiToNakshatraMap.put("ಕರ್ಕಾಟಕ", Arrays.asList("ಪುನರ್ವಸು", "ಪುಷ್ಯ", "ಆಶ್ಲೇಷ"));
		rashiToNakshatraMap.put("ಸಿಂಹ", Arrays.asList("ಮಘ", "ಪೂರ್ವ", "ಉತ್ತರ"));
		rashiToNakshatraMap.put("ಕನ್ಯಾ", Arrays.asList("ಉತ್ತರ", "ಹಸ್ತ", "ಚಿತ್ರ"));
		rashiToNakshatraMap.put("ತುಲಾ", Arrays.asList("ಚಿತ್ರ", "ಸ್ವಾತಿ", "ವಿಶಾಖ"));
		rashiToNakshatraMap.put("ವೃಶ್ಚಿಕ", Arrays.asList("ವಿಶಾಖ", "ಅನುರಾಧ", "ಜೇಷ್ಠ"));
		rashiToNakshatraMap.put("ಧನು", Arrays.asList("ಮೂಲ", "ಪೂರ್ವಾಷಾಢ", "ಉತ್ತರಾಷಾಢ"));
		rashiToNakshatraMap.put("ಮಕರ", Arrays.asList("ಉತ್ತರಾಷಾಢ", "ಶ್ರವಣ", "ಧನಿಷ್ಠ"));
		rashiToNakshatraMap.put("ಕುಂಭ", Arrays.asList("ಧನಿಷ್ಠ", "ಶತಭಿಷ", "ಪೂರ್ವಾಭಾದ್ರ"));
		rashiToNakshatraMap.put("ಮೀನ", Arrays.asList("ಪೂರ್ವಾಭಾದ", "ಉತ್ತರಾಭಾದ್ರ", "ರೇವತಿ"));

		// Reverse Map (Nakshatra -> Rashi)
		nakshatraToRashiMap.put("ಅಶ್ವಿನಿ", "ಮೇಷ");
		nakshatraToRashiMap.put("ಭರಣಿ", "ಮೇಷ");
		nakshatraToRashiMap.put("ಕೃತ್ತಿಕ", "ಮೇಷ");
		nakshatraToRashiMap.put("ರೋಹಿಣಿ", "ವೃಷಭ");
		nakshatraToRashiMap.put("ಮೃಗಶಿರ", "ವೃಷಭ");
		nakshatraToRashiMap.put("ಆರ್ದ್ರ", "ಮಿಥುನ");
		nakshatraToRashiMap.put("ಪುನರ್ವಸು", "ಮಿಥುನ");
		nakshatraToRashiMap.put("ಪುಷ್ಯ", "ಕರ್ಕಾಟಕ");
		nakshatraToRashiMap.put("ಆಶ್ಲೇಷ", "ಕರ್ಕಾಟಕ");
		nakshatraToRashiMap.put("ಮಘ", "ಸಿಂಹ");
		nakshatraToRashiMap.put("ಪೂರ್ವ", "ಸಿಂಹ");
		nakshatraToRashiMap.put("ಉತ್ತರ", "ಸಿಂಹ");
		nakshatraToRashiMap.put("ಹಸ್ತ", "ಕನ್ಯಾ");
		nakshatraToRashiMap.put("ಚಿತ್ರ", "ಕನ್ಯಾ");
		nakshatraToRashiMap.put("ಸ್ವಾತಿ", "ತುಲಾ");
		nakshatraToRashiMap.put("ವಿಶಾಖ", "ತುಲಾ");
		nakshatraToRashiMap.put("ಅನುರಾಧ", "ವೃಶ್ಚಿಕ");
		nakshatraToRashiMap.put("ಜೇಷ್ಠ", "ವೃಶ್ಚಿಕ");
		nakshatraToRashiMap.put("ಮೂಲ", "ಧನು");
		nakshatraToRashiMap.put("ಪೂರ್ವಾಷಾಢ", "ಧನು");
		nakshatraToRashiMap.put("ಉತ್ತರಾಷಾಢ", "ಧನು");
		nakshatraToRashiMap.put("ಶ್ರವಣ", "ಮಕರ");
		nakshatraToRashiMap.put("ಧನಿಷ್ಠ", "ಮಕರ");
		nakshatraToRashiMap.put("ಶತಭಿಷ", "ಕುಂಭ");
		nakshatraToRashiMap.put("ಪೂರ್ವಾಭಾದ್ರ", "ಕುಂಭ");
		nakshatraToRashiMap.put("ಉತ್ತರಾಭಾದ್ರ", "ಮೀನ");
		nakshatraToRashiMap.put("ರೇವತಿ", "ಮೀನ");
	}

	private void setupNakshatraToRashiListener() {
		// 1. Populate both boxes with all items
		raashiComboBox.setItems(allRashis);
		nakshatraComboBox.setItems(allNakshatras);
		raashiComboBox.getSelectionModel().selectFirst();
		nakshatraComboBox.getSelectionModel().selectFirst();

		// 2. Add listener to Rashi ComboBox (Rashi -> filters Nakshatra)
		raashiComboBox.getSelectionModel().selectedItemProperty().addListener(
				(_, _, selectedRashi) -> {
					if (isUpdatingNakshatra) return;
					isUpdatingNakshatra = true;

					if (selectedRashi == null || selectedRashi.equals("ಆಯ್ಕೆ")) {
						nakshatraComboBox.setItems(allNakshatras);
						nakshatraComboBox.getSelectionModel().selectFirst();
					} else {
						List<String> nakshatrasForRashi = rashiToNakshatraMap.get(selectedRashi);
						if (nakshatrasForRashi != null) {
							ObservableList<String> nakshatraItems = FXCollections.observableArrayList(nakshatrasForRashi);
							nakshatraItems.addFirst("ಆಯ್ಕೆ");
							nakshatraComboBox.setItems(nakshatraItems);
							String currentNakshatra = nakshatraComboBox.getValue();
							if (!nakshatraItems.contains(currentNakshatra)) {
								nakshatraComboBox.getSelectionModel().selectFirst();
							}
						}
					}
					isUpdatingNakshatra = false;
				});

		// 3. Add listener to Nakshatra ComboBox (Nakshatra -> selects Rashi)
		nakshatraComboBox.getSelectionModel().selectedItemProperty().addListener(
				(_, _, selectedNakshatra) -> {
					if (isUpdatingNakshatra) return;
					isUpdatingNakshatra = true;

					if (selectedNakshatra == null || selectedNakshatra.equals("ಆಯ್ಕೆ")) {
						raashiComboBox.setItems(allRashis);
						raashiComboBox.getSelectionModel().selectFirst();
					} else {
						String rashi = nakshatraToRashiMap.get(selectedNakshatra);
						if (rashi != null) {
							List<String> mappedRashis = new ArrayList<>();
							for (Map.Entry<String, List<String>> entry : rashiToNakshatraMap.entrySet()) {
								if (entry.getValue().contains(selectedNakshatra)) {
									mappedRashis.add(entry.getKey());
								}
							}

							if (mappedRashis.size() > 1) {
								mappedRashis.addFirst("ಆಯ್ಕೆ");
								raashiComboBox.setItems(FXCollections.observableArrayList(mappedRashis));
								raashiComboBox.setValue(rashi);
							} else if (mappedRashis.size() == 1) {
								raashiComboBox.setItems(allRashis);
								raashiComboBox.setValue(rashi);
							}
						}
					}
					isUpdatingNakshatra = false;
				});
	}
	// --- *** END OF NEW LOGIC *** ---

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