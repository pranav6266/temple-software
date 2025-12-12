package com.pranav.temple_software.services;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.DevoteeRepository;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TextFormatter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationServices {
	MainController controller;
	private final DevoteeRepository devoteeRepository = new DevoteeRepository();
	private double devoteeDailyCashTotal = 0.0;

	// --- Rashi/Nakshatra Data ---
	private final Map<String, String> nakshatraToRashiMap = new HashMap<>();
	private final ObservableList<String> allRashis;
	private final ObservableList<String> allNakshatras;
	private boolean isUpdatingNakshatra = false;

	// --- CONSTRUCTOR 1: For MainController (Preserves existing functionality) ---
	public ValidationServices(MainController controller) {
		this.controller = controller;

		// Initialize Lists
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
		initializeNakshatraToRashiMap();
	}

	// --- CONSTRUCTOR 2: Default (For Donation, Karyakrama, etc.) ---
	public ValidationServices() {
		this.controller = null; // No controller needed for pure validation logic

		// Initialize Lists (Same as above)
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
		initializeNakshatraToRashiMap();
	}

	// --- CENTRALIZED VALIDATION METHOD (Used by all modules) ---
	public String validateTransaction(String phoneNumber, double currentAmount, String paymentMode, String panNumber) {
		// 1. Validate Phone (Basic)
		if (phoneNumber == null || phoneNumber.length() != 10) {
			return null; // Skip complex checks if phone is invalid (handled elsewhere)
		}

		// 2. Get Today's History
		double pastCashTotal = devoteeRepository.getTodaysCashTotalByPhone(phoneNumber);
		double totalCashExposure = pastCashTotal + (paymentMode.equalsIgnoreCase("Cash") ? currentAmount : 0);
		double totalDailyValue = pastCashTotal + currentAmount;

		// 3. Rule: No Cash > 2000 (Daily Cumulative)
		if (paymentMode.equalsIgnoreCase("Cash") && totalCashExposure > 2000) {
			return String.format(
					"Cash Limit Exceeded!\n\n" +
							"Past Cash Today: ₹%.2f\n" +
							"Current Amount: ₹%.2f\n" +
							"Total Cash: ₹%.2f\n\n" +
							"Transactions exceeding ₹2000 in cash per day are not allowed.\n" +
							"Please switch Payment Mode to 'Online'.",
					pastCashTotal, currentAmount, totalCashExposure
			);
		}

		// 4. Rule: PAN Mandatory if Daily Total > 2000
		if (totalDailyValue > 2000) {
			if (panNumber == null || panNumber.trim().isEmpty()) {
				return String.format(
						"PAN Required!\n\n" +
								"Total transaction value for today (₹%.2f) exceeds ₹2000.\n" +
								"Please enter a valid PAN number.",
						totalDailyValue
				);
			}
			if (!isValidPanFormat(panNumber.trim())) {
				return "Invalid PAN Number!\n\nPlease enter a valid PAN (e.g., ABCDE1234F).";
			}
		}

		return null; // No errors
	}

	// --- Helper Methods ---

	private void initializeNakshatraToRashiMap() {
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

	public double getDevoteeDailyCashTotal() {
		return devoteeDailyCashTotal;
	}

	// --- Logic for MainController (Legacy Support) ---

	public void checkAndEnforceCashLimit() {
		if (controller == null) return;

		double currentCartTotal = controller.selectedSevas.stream()
				.mapToDouble(SevaEntry::getTotalAmount)
				.sum();

		// Refresh past total just in case
		String phone = controller.contactField.getText();
		if(phone != null && phone.length() == 10) {
			this.devoteeDailyCashTotal = devoteeRepository.getTodaysCashTotalByPhone(phone);
		}

		double grandTotal = devoteeDailyCashTotal + currentCartTotal;
		if (grandTotal > 2000.0) {
			if (!controller.onlineRadio.isSelected()) {
				Platform.runLater(() -> {
					controller.cashRadio.setSelected(false);
					controller.onlineRadio.setSelected(true);
					showAlert(
							String.format("Today's cash total for this devotee (₹%.2f) plus the current cart total (₹%.2f) exceeds ₹2000.\nPayment must be made online.",
									devoteeDailyCashTotal, currentCartTotal));
				});
			}
			controller.cashRadio.setDisable(true);
		} else {
			controller.cashRadio.setDisable(false);
		}
	}

	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		if (controller != null && controller.mainStage != null) {
			alert.initOwner(controller.mainStage);
		}
		alert.setTitle("Cash Limit Exceeded");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public void setupPhoneValidation() {
		if (controller == null) return;
		controller.contactField.textProperty().addListener((_, _, newValue) -> {
			if (newValue != null) {
				String digitsOnly = newValue.replaceAll("\\D", "");
				if (digitsOnly.length() > 10) {
					digitsOnly = digitsOnly.substring(0, 10);
				}

				if (!digitsOnly.equals(newValue)) {
					controller.contactField.setText(digitsOnly);
					return;
				}

				if (digitsOnly.length() == 10) {
					// Update main controller logic
					devoteeRepository.findLatestDevoteeDetailsByPhone(digitsOnly)
							.ifPresent(details -> Platform.runLater(() -> {
								controller.populateDevoteeDetails(details);
							}));

					// Update local total for checkAndEnforceCashLimit
					this.devoteeDailyCashTotal = devoteeRepository.getTodaysCashTotalByPhone(digitsOnly);
					checkAndEnforceCashLimit();
				} else {
					this.devoteeDailyCashTotal = 0.0;
					checkAndEnforceCashLimit();
				}
			}
		});

		controller.contactField.focusedProperty().addListener((_, _, newVal) -> {
			if (!newVal) {
				String phone = controller.contactField.getText();
				if (phone != null && !phone.isEmpty() && phone.length() < 10) {
					controller.showAlert("Invalid Phone Number", "Phone number must contain at least 10 digits");
				}
			}
		});
	}

	public void setupPanValidation() {
		if (controller == null) return;
		controller.panNumberField.textProperty().addListener((_, _, newValue) -> {
			if (newValue != null) {
				String upperCase = newValue.toUpperCase();
				if (upperCase.length() > 10) {
					upperCase = upperCase.substring(0, 10);
				}
				if (!upperCase.equals(newValue)) {
					controller.panNumberField.setText(upperCase);
				}
			}
		});

		controller.panNumberField.focusedProperty().addListener((_, _, newVal) -> {
			if (!newVal) {
				String pan = controller.panNumberField.getText();
				if (pan != null && !pan.trim().isEmpty()) {
					if (!isValidPanFormat(pan.trim())) {
						controller.showAlert("Invalid PAN Format",
								"PAN should be in format: AAAPL1234C\n" +
										"(5 letters + 4 numbers + 1 letter)");
					}
				}
			}
		});
	}

	private boolean isValidPanFormat(String pan) {
		if (pan == null || pan.length() != 10) {
			return false;
		}
		return pan.matches("[A-Z]{5}[0-9]{4}[A-Z]");
	}

	public void setupNameValidation() {
		if (controller == null) return;
		TextFormatter<String> formatter = new TextFormatter<>(change -> {
			String newText = change.getControlNewText();
			if (newText.matches("[\\p{L} ]*")) {
				return change;
			} else {
				return null;
			}
		});
		controller.devoteeNameField.setTextFormatter(formatter);
	}

	public void radioCheck(){
		if (controller == null) return;
		controller.cashRadio.selectedProperty().addListener(
				(_, _, newValue) -> {
					if (newValue) {
						controller.onlineRadio.setSelected(false);
					}
				});
		controller.onlineRadio.selectedProperty().addListener(
				(_, _, newValue) -> {
					if (newValue) {
						controller.cashRadio.setSelected(false);
					}
				});
	}

	public void calenderChecker() {
		if (controller == null) return;
		controller.sevaDatePicker.getEditor().textProperty().addListener((_, _, newText) -> {
			if (newText == null || newText.isEmpty()) {
				controller.sevaDatePicker.setValue(LocalDate.now());
			}
		});
	}

	public void setupNakshatraToRashiListener() {
		if (controller == null) return;
		// 1. Populate both boxes with all items
		controller.raashiComboBox.setItems(allRashis);
		controller.nakshatraComboBox.setItems(allNakshatras);
		controller.raashiComboBox.getSelectionModel().selectFirst();
		controller.nakshatraComboBox.getSelectionModel().selectFirst();

		// 2. Add listener to Rashi ComboBox (Rashi -> filters Nakshatra)
		controller.raashiComboBox.getSelectionModel().selectedItemProperty().addListener(
				(_, _, selectedRashi) -> {
					if (isUpdatingNakshatra) return;
					isUpdatingNakshatra = true;

					if (selectedRashi == null || selectedRashi.equals("ಆಯ್ಕೆ")) {
						controller.nakshatraComboBox.setItems(allNakshatras);
						controller.nakshatraComboBox.getSelectionModel().selectFirst();
					} else {
						List<String> nakshatrasForRashi = controller.rashiNakshatraMap.get(selectedRashi);
						if (nakshatrasForRashi != null) {
							ObservableList<String> nakshatraItems = FXCollections.observableArrayList(nakshatrasForRashi);
							nakshatraItems.addFirst("ಆಯ್ಕೆ");
							controller.nakshatraComboBox.setItems(nakshatraItems);

							String currentNakshatra = controller.nakshatraComboBox.getValue();
							if (!nakshatraItems.contains(currentNakshatra)) {
								controller.nakshatraComboBox.getSelectionModel().selectFirst();
							}
						}
					}
					isUpdatingNakshatra = false;
				});

		// 3. Add listener to Nakshatra ComboBox (Nakshatra -> selects Rashi)
		controller.nakshatraComboBox.getSelectionModel().selectedItemProperty().addListener(
				(_, _, selectedNakshatra) -> {
					if (isUpdatingNakshatra) return;
					isUpdatingNakshatra = true;

					if (selectedNakshatra == null || selectedNakshatra.equals("ಆಯ್ಕೆ")) {
						controller.raashiComboBox.setItems(allRashis);
						controller.raashiComboBox.getSelectionModel().selectFirst();
					} else {
						String rashi = nakshatraToRashiMap.get(selectedNakshatra);
						if (rashi != null) {
							List<String> mappedRashis = new ArrayList<>();
							for (Map.Entry<String, List<String>> entry : controller.rashiNakshatraMap.entrySet()) {
								if (entry.getValue().contains(selectedNakshatra)) {
									mappedRashis.add(entry.getKey());
								}
							}

							if (mappedRashis.size() > 1) {
								mappedRashis.addFirst("ಆಯ್ಕೆ");
								controller.raashiComboBox.setItems(FXCollections.observableArrayList(mappedRashis));
								controller.raashiComboBox.setValue(rashi);
							} else if (mappedRashis.size() == 1) {
								controller.raashiComboBox.setItems(allRashis);
								controller.raashiComboBox.setValue(rashi);
							}
						}
					}
					isUpdatingNakshatra = false;
				});
	}

	public void initializeTotalCalculation() {
		if (controller == null) return;
		DoubleBinding totalBinding = Bindings.createDoubleBinding(() ->
						controller.selectedSevas.stream()
								.mapToDouble(SevaEntry::getTotalAmount)
								.sum(),
				controller.selectedSevas
		);
		totalBinding.addListener((_, _, _) -> checkAndEnforceCashLimit());
		controller.totalLabel.textProperty().bind(Bindings.createStringBinding(() ->
						String.format("₹%.2f", totalBinding.get()),
				totalBinding
		));
	}
}