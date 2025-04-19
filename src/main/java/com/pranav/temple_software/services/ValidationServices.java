package com.pranav.temple_software.services;
import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.SevaEntry;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextFormatter;

import java.time.LocalDate;
import java.util.List;

public class ValidationServices {
MainController controller;

	public ValidationServices(MainController controller) {
		this.controller = controller;
	}

	public void setupPhoneValidation() {
		controller.contactField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				// Allow only digits and limit to 10 characters
				if (!newValue.matches("\\d*")) {
					controller.contactField.setText(newValue.replaceAll("[^\\d]", ""));
				}
				if (newValue.length() > 10) {
					controller.contactField.setText(newValue.substring(0, 10));
				} else if (newValue.length() < 10) {
				}
			}
		});

		// Add phone number validation on focus loss
		controller.contactField.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal) { // When focus is lost
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
		controller.sevaListener.raashiNakshatraMap();
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
