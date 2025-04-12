package com.pranav.temple_software.services;
import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.SevaEntry;
import javafx.scene.control.Alert;

import java.util.Optional;

public class Donation {
	MainController controller;

	public Donation(MainController mainController) {
		this.controller = mainController;
	}

	//This is the code to disable all donation fields till the checkbox is not checked.
	public void setDisable(){
		// Initially disable donation field and combo box
		controller.donationField.setDisable(true);
		controller.donationComboBox.setDisable(true);

		// Add listener to donation checkbox
		controller.donationCheck.selectedProperty().addListener(
				(observable, oldValue, newValue) -> {
					// Enable/disable donation field and combo box based on checkbox state
					controller.donationField.setDisable(!newValue);
					controller.donationComboBox.setDisable(!newValue);
				});
	}


	public void handleAddDonation() {
		String donationType = controller.donationComboBox.getValue();
		String amountText = controller.donationField.getText();

		if (donationType == null || donationType.equals("ಆಯ್ಕೆ") || amountText.isEmpty()) {
			controller.showAlert(Alert.AlertType.INFORMATION, "Invalid Input", "Please select a donation type and enter amount");
			return;
		}

		try {
			double amount = Double.parseDouble(amountText);
			if (amount <= 0) throw new NumberFormatException();

			String entryName = "ದೇಣಿಗೆ : " + donationType;

			// Find existing donation entry
			Optional<SevaEntry> existingEntry = controller.selectedSevas.stream()
					.filter(entry -> entry.getName().equals(entryName))
					.findFirst();

			if (existingEntry.isPresent()) {
				// Update existing amount
				existingEntry.get().amountProperty().set(amount);
			} else {
				// Add new entry
				controller.selectedSevas.add(new SevaEntry(entryName, amount));
			}

			controller.donationField.clear();
		} catch (NumberFormatException ex) {
			controller.showAlert(Alert.AlertType.INFORMATION, "Invalid Amount", "Please enter a valid positive number");
		}
	}

}
