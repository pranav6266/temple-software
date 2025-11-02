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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationServices {
	MainController controller;
	private final DevoteeRepository devoteeRepository = new DevoteeRepository();
	private double devoteeDailyCashTotal = 0.0;

	// --- NEW FIELDS for 2-way Rashi/Nakshatra binding ---
	private final Map<String, String> nakshatraToRashiMap = new HashMap<>();
	private final ObservableList<String> allRashis;
	private final ObservableList<String> allNakshatras;
	private boolean isUpdatingNakshatra = false; // Flag to prevent infinite loops

	public ValidationServices(MainController controller) {
		this.controller = controller;

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

		// Initialize the reverse map
		initializeNakshatraToRashiMap();
	}

	private void initializeNakshatraToRashiMap() {
		// This map is used when a Nakshatra is selected to find the Rashi
		nakshatraToRashiMap.put("ಅಶ್ವಿನಿ", "ಮೇಷ");
		nakshatraToRashiMap.put("ಭರಣಿ", "ಮೇಷ");
		nakshatraToRashiMap.put("ಕೃತ್ತಿಕ", "ಮೇಷ"); // Note: Krittika is split, default to Mesha
		nakshatraToRashiMap.put("ರೋಹಿಣಿ", "ವೃಷಭ");
		nakshatraToRashiMap.put("ಮೃಗಶಿರ", "ವೃಷಭ"); // Note: Mrigashira is split, default to Vrishabha
		nakshatraToRashiMap.put("ಆರ್ದ್ರ", "ಮಿಥುನ");
		nakshatraToRashiMap.put("ಪುನರ್ವಸು", "ಮಿಥುನ"); // Note: Punarvasu is split, default to Mithuna
		nakshatraToRashiMap.put("ಪುಷ್ಯ", "ಕರ್ಕಾಟಕ");
		nakshatraToRashiMap.put("ಆಶ್ಲೇಷ", "ಕರ್ಕಾಟಕ");
		nakshatraToRashiMap.put("ಮಘ", "ಸಿಂಹ");
		nakshatraToRashiMap.put("ಪೂರ್ವ", "ಸಿಂಹ");
		nakshatraToRashiMap.put("ಉತ್ತರ", "ಸಿಂಹ"); // Note: Uttara is split, default to Simha
		nakshatraToRashiMap.put("ಹಸ್ತ", "ಕನ್ಯಾ");
		nakshatraToRashiMap.put("ಚಿತ್ರ", "ಕನ್ಯಾ"); // Note: Chitra is split, default to Kanya
		nakshatraToRashiMap.put("ಸ್ವಾತಿ", "ತುಲಾ");
		nakshatraToRashiMap.put("ವಿಶಾಖ", "ತುಲಾ"); // Note: Vishakha is split, default to Tula
		nakshatraToRashiMap.put("ಅನುರಾಧ", "ವೃಶ್ಚಿಕ");
		nakshatraToRashiMap.put("ಜೇಷ್ಠ", "ವೃಶ್ಚಿಕ");
		nakshatraToRashiMap.put("ಮೂಲ", "ಧನು");
		nakshatraToRashiMap.put("ಪೂರ್ವಾಷಾಢ", "ಧನು");
		nakshatraToRashiMap.put("ಉತ್ತರಾಷಾಢ", "ಧನು"); // Note: Uttarashadha is split, default to Dhanu
		nakshatraToRashiMap.put("ಶ್ರವಣ", "ಮಕರ");
		nakshatraToRashiMap.put("ಧನಿಷ್ಠ", "ಮಕರ"); // Note: Dhanishtha is split, default to Makara
		nakshatraToRashiMap.put("ಶತಭಿಷ", "ಕುಂಭ");
		nakshatraToRashiMap.put("ಪೂರ್ವಾಭಾದ್ರ", "ಕುಂಭ"); // Note: Purva Bhadrapada is split, default to Kumbha
		nakshatraToRashiMap.put("ಉತ್ತರಾಭಾದ್ರ", "ಮೀನ");
		nakshatraToRashiMap.put("ರೇವತಿ", "ಮೀನ");
	}

	public double getDevoteeDailyCashTotal() {
		return devoteeDailyCashTotal;
	}

	public void checkAndEnforceCashLimit() {
		double currentCartTotal = controller.selectedSevas.stream()
				.mapToDouble(SevaEntry::getTotalAmount)
				.sum();
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

	private void fetchPastTransactionsAndValidate() {
		String phoneNumber = controller.contactField.getText();
		if (phoneNumber != null && phoneNumber.length() == 10) {
			this.devoteeDailyCashTotal = devoteeRepository.getTodaysCashTotalByPhone(phoneNumber);
			checkAndEnforceCashLimit();
		} else {
			this.devoteeDailyCashTotal = 0.0;
			checkAndEnforceCashLimit();
		}
	}

	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		if (controller.mainStage != null) {
			alert.initOwner(controller.mainStage);
		}
		alert.setTitle("Cash Limit Exceeded");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public void setupPhoneValidation() {
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
					// --- MODIFIED: Wrap populateDevoteeDetails in the flag ---
					isUpdatingNakshatra = true;
					devoteeRepository.findLatestDevoteeDetailsByPhone(digitsOnly)
							.ifPresent(details -> Platform.runLater(() -> {
								controller.populateDevoteeDetails(details);
								isUpdatingNakshatra = false;
							}));
					if (!devoteeRepository.findLatestDevoteeDetailsByPhone(digitsOnly).isPresent()) {
						isUpdatingNakshatra = false; // Ensure flag is reset if no devotee is found
					}
					// --- END MODIFICATION ---
					fetchPastTransactionsAndValidate();
				} else {
					this.devoteeDailyCashTotal = 0.0;
					checkAndEnforceCashLimit();
				}
			}
		});
		controller.contactField.focusedProperty().addListener((_, _, newVal) -> {
			if (!newVal) {
				validatePhoneNumber();
			}
		});
	}

	private void validatePhoneNumber() {
		String phone = controller.contactField.getText();
		if (phone != null && !phone.isEmpty() && phone.length() < 10) {
			controller.showAlert("Invalid Phone Number", "Phone number must contain at least 10 digits");
		}
	}

	public void setupPanValidation() {
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
		controller.sevaDatePicker.getEditor().textProperty().addListener((_, _, newText) -> {
			if (newText == null || newText.isEmpty()) {
				controller.sevaDatePicker.setValue(LocalDate.now());
			}
		});
	}

	// --- THIS IS THE OLD, ONE-WAY METHOD. IT IS REPLACED. ---
	public void threeNakshatraForARashi() {
		// This method is now obsolete and replaced by setupNakshatraToRashiListener
	}

	// --- *** NEW, REVERSIBLE LISTENER LOGIC *** ---
	public void setupNakshatraToRashiListener() {
		// 1. Populate both boxes with all items
		controller.raashiComboBox.setItems(allRashis);
		controller.nakshatraComboBox.setItems(allNakshatras);
		controller.raashiComboBox.getSelectionModel().selectFirst();
		controller.nakshatraComboBox.getSelectionModel().selectFirst();

		// 2. Add listener to Rashi ComboBox (Rashi -> filters Nakshatra)
		controller.raashiComboBox.getSelectionModel().selectedItemProperty().addListener(
				(_, _, selectedRashi) -> {
					if (isUpdatingNakshatra) return; // Prevent loop
					isUpdatingNakshatra = true;

					if (selectedRashi == null || selectedRashi.equals("ಆಯ್ಕೆ")) {
						// Reset Nakshatra box to show all
						controller.nakshatraComboBox.setItems(allNakshatras);
						controller.nakshatraComboBox.getSelectionModel().selectFirst();
					} else {
						// Filter Nakshatra list based on Rashi
						List<String> nakshatrasForRashi = controller.rashiNakshatraMap.get(selectedRashi);
						if (nakshatrasForRashi != null) {
							ObservableList<String> nakshatraItems = FXCollections.observableArrayList(nakshatrasForRashi);
							nakshatraItems.addFirst("ಆಯ್ಕೆ");
							controller.nakshatraComboBox.setItems(nakshatraItems);

							// Check if current Nakshatra is valid for this Rashi
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
					if (isUpdatingNakshatra) return; // Prevent loop
					isUpdatingNakshatra = true;

					if (selectedNakshatra == null || selectedNakshatra.equals("ಆಯ್ಕೆ")) {
						// Reset Rashi box to show all
						controller.raashiComboBox.setItems(allRashis);
						controller.raashiComboBox.getSelectionModel().selectFirst();
					} else {
						// Find the Rashi for this Nakshatra
						String rashi = nakshatraToRashiMap.get(selectedNakshatra);
						if (rashi != null) {
							// This Nakshatra has a primary Rashi.
							// We must also check if this Nakshatra is split (e.g., Krittika)
							List<String> mappedRashis = new ArrayList<>();
							for (Map.Entry<String, List<String>> entry : controller.rashiNakshatraMap.entrySet()) {
								if (entry.getValue().contains(selectedNakshatra)) {
									mappedRashis.add(entry.getKey());
								}
							}

							if (mappedRashis.size() > 1) {
								// This Nakshatra is split (e.g., Krittika). Filter the Rashi list.
								mappedRashis.addFirst("ಆಯ್ಕೆ");
								controller.raashiComboBox.setItems(FXCollections.observableArrayList(mappedRashis));
								// Auto-select the *first* matching Rashi as a default
								controller.raashiComboBox.setValue(rashi);
							} else if (mappedRashis.size() == 1) {
								// This Nakshatra belongs to only one Rashi. Auto-select it.
								controller.raashiComboBox.setItems(allRashis); // Show all Rashis
								controller.raashiComboBox.setValue(rashi); // Auto-select the correct one
							}

						}
					}
					isUpdatingNakshatra = false;
				});
	}
	// --- *** END OF NEW LOGIC *** ---


	public void initializeTotalCalculation() {
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