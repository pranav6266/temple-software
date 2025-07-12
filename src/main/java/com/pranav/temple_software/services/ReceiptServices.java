// Inside Temple_Software/src/main/java/com/pranav/temple_software/services/ReceiptServices.java
package com.pranav.temple_software.services;

import com.pranav.temple_software.models.ReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.pranav.temple_software.controllers.MainController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer; // Import Consumer

public class ReceiptServices {

	MainController controller;
	// Add state variables to hold pending data
	private int pendingReceiptId = -1;
	private ReceiptData pendingReceiptData = null;
	private String pendingPaymentMode = "N/A";


	public ReceiptServices(MainController mainController) {
		this.controller = mainController;
	}

	private String formatSevasForDatabase(ObservableList<SevaEntry> sevas) {
		if (sevas == null || sevas.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sevas.size(); i++) {
			SevaEntry entry = sevas.get(i);
			String safeName = entry.getName().replace(":", "").replace(";", "");
			sb.append(safeName).append(":")
					.append(entry.getAmount())
					.append(":")
					.append(entry.getQuantity());
			if (i < sevas.size() - 1) {
				sb.append(";");
			}
		}
		return sb.toString();
	}


	public void handlePrintPreview() {

		// Clear previous pending state if any (e.g., if preview was cancelled before)
		this.pendingReceiptId = -1;
		this.pendingReceiptData = null;
		this.pendingPaymentMode = "N/A";


		// 1. Get the NEXT POTENTIAL Receipt ID *before* gathering other data
		this.pendingReceiptId = controller.receiptRepository.getNextReceiptId();
		if (this.pendingReceiptId <= 0) {
			controller.showAlert("Database Error", "Could not determine the next receipt ID. Please check database connection or logs.");
			return;
		}

		// 2. Gather Data (as before, use final where needed for lambda)

		final String devoteeName = controller.devoteeNameField.getText();
		final String phoneNumber = controller.contactField.getText();
		final String address = controller.addressField.getText();
		final LocalDate date = controller.sevaDatePicker.getValue();
		final ObservableList<SevaEntry> currentSevas = FXCollections.observableArrayList(controller.sevaTableView.getItems());
		final double total;
		double total1;
		try {
			String totalText = controller.totalLabel.getText().replaceAll("[^\\d.]", "");
			total1 = Double.parseDouble(totalText);
		} catch (NumberFormatException | NullPointerException ex) {
			total1 = currentSevas.stream().mapToDouble(SevaEntry::getTotalAmount).sum();
			System.err.println("Could not parse total from label, recalculating.");
		}
		// Store pending payment mode
		total = total1;
		this.pendingPaymentMode = controller.cashRadio.isSelected() ? "Cash" : (controller.onlineRadio.isSelected() ? "Online" : "N/A");
		final String raashi = controller.raashiComboBox.getValue();
		final String nakshatra = controller.nakshatraComboBox.getValue();


		// 3. Perform Validation (as before)
		List<String> errors = new ArrayList<>();
		// --- Add all your validation checks here ---
		if (devoteeName == null || devoteeName.trim().isEmpty()) { errors.add("Please enter devotee name"); }
		if (date == null) { errors.add("Please select a seva date"); }
		if (currentSevas.isEmpty()) { errors.add("Please add at least one seva or donation"); }
		if (!controller.cashRadio.isSelected() && !controller.onlineRadio.isSelected()) { errors.add("Please select payment mode (Cash/Online)"); }
		if (phoneNumber != null && !phoneNumber.isEmpty() && phoneNumber.length() < 10) { errors.add("Phone number must contain at least 10 digits"); }
		// --- End of validation checks ---


		if (!errors.isEmpty()) {
			controller.showAlert("Validation Error", String.join("\n", errors));
			this.pendingReceiptId = -1; // Reset pending ID if validation fails
			return;
		}

		// --- REMOVE Database Save from here ---


		// 4. Create ReceiptData with the POTENTIAL ID for preview/print
		//    Store this in the pending state variable
		boolean hasDonation = currentSevas.stream().anyMatch(seva -> seva.getName().startsWith("ದೇಣಿಗೆ"));
		this.pendingReceiptData = new ReceiptData(
				this.pendingReceiptId, // Use potential ID
				devoteeName, phoneNumber,address, raashi, nakshatra, date, currentSevas, total, pendingPaymentMode,hasDonation ? "ಹೌದು" : "ಇಲ್ಲ"
		);

		// 5. Update UI Label with the POTENTIAL ID
		controller.receiptNumberLabel.setText(String.valueOf(this.pendingReceiptId));


		// 6. Define the callback function for AFTER printing attempt
		Consumer<Boolean> afterPrintAction = (printSuccess) -> {
			try { // Use try-finally to ensure cleanup
				if (printSuccess) {
					// 7. If PRINT was successful, attempt to save with SPECIFIC ID
					if (this.pendingReceiptData == null || this.pendingReceiptId <= 0) {
						// Should not happen if validation passed, but check defensively
						Platform.runLater(() -> controller.showAlert("Internal Error", "Pending receipt data is missing. Cannot save."));
						return;
					}

					String sevasDetailsString = formatSevasForDatabase(this.pendingReceiptData.getSevas());
					int actualSavedId = controller.receiptRepository.saveSpecificReceipt(
							this.pendingReceiptId, // The ID we tried to use
							this.pendingReceiptData.getDevoteeName(),
							this.pendingReceiptData.getPhoneNumber(),
							this.pendingReceiptData.getAddress(),
							this.pendingReceiptData.getRashi(),
							this.pendingReceiptData.getNakshatra(),
							this.pendingReceiptData.getSevaDate(),
							sevasDetailsString,
							this.pendingReceiptData.getTotalAmount(),
							this.pendingPaymentMode // Use stored pending payment mode
					);

					if (actualSavedId != -1) {
						// 8. If SAVE was successful (with specific or fallback ID)
						final int finalSavedId = actualSavedId; // Final variable for lambda
						Platform.runLater(() -> {
							// Update label ONLY if the saved ID differs from the previewed one
							if (finalSavedId != this.pendingReceiptId) {
								controller.receiptNumberLabel.setText(" "+finalSavedId);
								controller.showAlert("Save Successful (ID Changed)", "Receipt printed and saved successfully.");
							} else {
								controller.receiptNumberLabel.setText(" " + finalSavedId);
								controller.showAlert("Success", "Receipt printed and saved successfully with ID: " + finalSavedId);
							}
							controller.clearForm(); // Clear form ONLY on full success
						});
					} else {
						// 9. If PRINT succeeded but SAVE ultimately failed
						Platform.runLater(() -> {
							controller.showAlert("Database Error", "Receipt was printed, but failed to save to the database even after fallback. Please record details manually.\nAttempted ID: " + this.pendingReceiptId + "\nName: " + this.pendingReceiptData.getDevoteeName());
							controller.receiptNumberLabel.setText("Save Failed!"); // Indicate save failure
						});
					}
				} else {
					// 10. If PRINT failed or was cancelled
					Platform.runLater(() -> {
						controller.showAlert("Print Failed/Cancelled", "The print job was cancelled or failed. Receipt not saved.");
						// Optional: Clear the label if desired, or leave the potential ID
						// controller.receiptNumberLabel.setText("");
					});
				}
			} finally {
				// 11. Cleanup pending state regardless of success/failure
				this.pendingReceiptId = -1;
				this.pendingReceiptData = null;
				this.pendingPaymentMode = "N/A";
			}
		};


		// 12. Show the print preview, passing the data (with POTENTIAL ID) and the callback
		// Ensure ReceiptPrinter still takes the Consumer<Boolean> callback
		controller.receiptPrinter.showPrintPreview(this.pendingReceiptData, controller.mainStage, afterPrintAction);
	}
}