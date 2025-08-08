package com.pranav.temple_software.services;
import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.DevoteeDetails;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.DevoteeRepository;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextFormatter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ValidationServices {
	MainController controller;
	private final DevoteeRepository devoteeRepository = new DevoteeRepository();

	public ValidationServices(MainController controller) {
		this.controller = controller;
	}

	public void setupPhoneValidation() {
		controller.contactField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				if (!newValue.matches("\\d*")) {
					controller.contactField.setText(newValue.replaceAll("[^\\d]", ""));
				}
				if (newValue.length() > 10) {
					controller.contactField.setText(newValue.substring(0, 10));
				}
			}
		});

		// **MODIFIED: Add focus listener to trigger auto-fill**
		controller.contactField.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal) { // When focus is lost
				String phoneNumber = controller.contactField.getText();
				// Proceed only if it's a valid 10-digit number
				if (phoneNumber != null && phoneNumber.length() == 10) {
					// Search for devotee details in the database
					Optional<DevoteeDetails> detailsOpt = devoteeRepository.findLatestDevoteeDetailsByPhone(phoneNumber);
					// If details are found, populate the form
					detailsOpt.ifPresent(details -> {
						// Use Platform.runLater to ensure UI updates happen on the JavaFX Application Thread
						Platform.runLater(() -> controller.populateDevoteeDetails(details));
					});
				} else if (phoneNumber != null && !phoneNumber.isEmpty()) {
					// Show validation alert if the number is incomplete
					validatePhoneNumber();
				}
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
		// PAN number should be uppercase and match PAN format
		controller.panNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				// Convert to uppercase and limit to 10 characters
				String upperCase = newValue.toUpperCase();
				if (upperCase.length() > 10) {
					upperCase = upperCase.substring(0, 10);
				}
				// Only allow alphanumeric characters
				upperCase = upperCase.replaceAll("[^A-Z0-9]", "");
				controller.panNumberField.setText(upperCase);
			}
		});

		// Validate PAN format when focus is lost
		controller.panNumberField.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal) { // When focus is lost
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

	/**
	 * Validates PAN number format
	 */
	private boolean isValidPanFormat(String pan) {
		if (pan == null || pan.length() != 10) {
			return false;
		}
		// PAN format: 5 letters, 4 digits, 1 letter
		return pan.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}");
	}

	public void setupAmountValidation() {
		controller.donationField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null && !newValue.matches("\\d*(\\.\\d*)?")) {
				// Allow only digits and at most one decimal point
				controller.donationField.setText(oldValue);
			}
		});
	}

	//This code implements what values the text boxes should accept.
	public void setupNameValidation() {
		// Create a TextFormatter with a filter to allow only letters and spaces
		TextFormatter<String> formatter = new TextFormatter<>(change -> {
			String newText = change.getControlNewText();
			// Allow empty input or strings containing only letters/spaces
			if (newText.matches("[\\p{L} ]*")) {
				return change; // Accept the change
			} else {
				return null; // Reject the change
			}
		});
		controller.devoteeNameField.setTextFormatter(formatter);
	}

	//This is the code to check only one of the radio buttons in cash and online
	public void radioCheck(){
		// Set up the checkboxes to act like radio buttons
		controller.cashRadio.selectedProperty().addListener(
				(observable, oldValue, newValue) -> {
					if (newValue) {
						controller.onlineRadio.setSelected(false);
					}
				});

		controller.onlineRadio.selectedProperty().addListener(
				(observable, oldValue, newValue) -> {
					if (newValue) {
						controller.cashRadio.setSelected(false);
					}
				});
	}

	// Force today's date if field is left empty or invalid
	public void calenderChecker() {
		controller.sevaDatePicker.getEditor().textProperty().addListener((obs, oldVal, newText) -> {
			if (newText == null || newText.isEmpty()) {
				controller.sevaDatePicker.setValue(LocalDate.now());
			}
		});
	}

	//This below is to select only the 3 nakshatras for a given raashi
	public void threeNakshatraForARashi() {
		controller.nakshatraComboBox.setDisable(true);
		controller.sevaListener.rashiNakshatraMap();
		controller.raashiComboBox.getSelectionModel().selectedItemProperty().addListener(
				(obs, oldVal, newVal) -> {
					if (newVal == null || newVal.equals("ಆಯ್ಕೆ")) {
						controller.nakshatraComboBox.setDisable(true);
						controller.nakshatraComboBox.getItems().clear();
						controller.nakshatraComboBox.getSelectionModel().clearSelection();
						controller.nakshatraComboBox.setPromptText("ನಕ್ಷತ್ರವನ್ನು ಆಯ್ಕೆಮಾಡಿ");
					} else {
						List<String> nakshatrasForRashi = controller.rashiNakshatraMap.get(newVal);
						if (nakshatrasForRashi != null) {
							ObservableList<String> nakshatraItems = FXCollections.observableArrayList(nakshatrasForRashi);
							nakshatraItems.add(0, "ಆಯ್ಕೆ");
							controller.nakshatraComboBox.setItems(nakshatraItems);
							controller.nakshatraComboBox.setDisable(false);
							controller.nakshatraComboBox.setPromptText("ನಕ್ಷತ್ರವನ್ನು ಆಯ್ಕೆಮಾಡಿ");
						}
					}
				});
	}

	public void initializeTotalCalculation() {
		// Create binding for total amount
		DoubleBinding totalBinding = Bindings.createDoubleBinding(() ->
						controller.selectedSevas.stream()
								.mapToDouble(SevaEntry::getTotalAmount)
								.sum(),
				controller.selectedSevas
		);

		// Update total label with currency format
		controller.totalLabel.textProperty().bind(Bindings.createStringBinding(() ->
						String.format("₹%.2f", totalBinding.get()),
				totalBinding
		));
	}
}
