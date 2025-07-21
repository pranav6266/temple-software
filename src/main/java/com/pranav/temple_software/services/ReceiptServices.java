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
import com.pranav.temple_software.models.DonationReceiptData;
import com.pranav.temple_software.repositories.DonationReceiptRepository;

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
		// Clear previous pending state
		this.pendingReceiptId = -1;
		this.pendingReceiptData = null;
		this.pendingPaymentMode = "N/A";

		// Gather form data
		final String devoteeName = controller.devoteeNameField.getText();
		final String phoneNumber = controller.contactField.getText();
		final String address = controller.addressField.getText();
		final LocalDate date = controller.sevaDatePicker.getValue();
		final ObservableList<SevaEntry> currentSevas = FXCollections.observableArrayList(controller.sevaTableView.getItems());
		final String raashi = controller.raashiComboBox.getValue();
		final String nakshatra = controller.nakshatraComboBox.getValue();

		// Calculate total
		final double total;
		double total1;
		try {
			String totalText = controller.totalLabel.getText().replaceAll("[^\\d.]", "");
			total1 = Double.parseDouble(totalText);
		} catch (NumberFormatException | NullPointerException ex) {
			total1 = currentSevas.stream().mapToDouble(SevaEntry::getTotalAmount).sum();
			System.err.println("Could not parse total from label, recalculating.");
		}
		total = total1;
		this.pendingPaymentMode = controller.cashRadio.isSelected() ? "Cash" : (controller.onlineRadio.isSelected() ? "Online" : "N/A");

		// Validation
		List<String> errors = new ArrayList<>();
		if (date == null) { errors.add("Please select a seva date"); }
		if (currentSevas.isEmpty()) { errors.add("Please add at least one seva or donation"); }
		if (!controller.cashRadio.isSelected() && !controller.onlineRadio.isSelected()) {
			errors.add("Please select payment mode (Cash/Online)");
		}
		if (phoneNumber != null && !phoneNumber.isEmpty() && phoneNumber.length() < 10) {
			errors.add("Phone number must contain at least 10 digits");
		}

		if (!errors.isEmpty()) {
			controller.showAlert("Validation Error", String.join("\n", errors));
			return;
		}

		// Separate seva and donation entries
		List<SevaEntry> sevaEntries = new ArrayList<>();
		List<SevaEntry> donationEntries = new ArrayList<>();

		for (SevaEntry entry : currentSevas) {
			if (entry.getName().startsWith("ದೇಣಿಗೆ ")) {
				donationEntries.add(entry);
			} else {
				sevaEntries.add(entry);
			}
		}

		// Handle seva receipt if sevas exist
		if (!sevaEntries.isEmpty()) {
			handleSevaReceipt(devoteeName, phoneNumber, address, raashi, nakshatra, date,
					sevaEntries, pendingPaymentMode);
		}

		// Handle donation receipts (one per donation)
		if (!donationEntries.isEmpty()) {
			handleDonationReceipts(devoteeName, phoneNumber, address, raashi, nakshatra, date,
					donationEntries, pendingPaymentMode);
		}
	}

	private void handleSevaReceipt(String devoteeName, String phoneNumber, String address,
	                               String raashi, String nakshatra, LocalDate date,
	                               List<SevaEntry> sevaEntries, String paymentMode) {
		// Get next seva receipt ID
		int sevaReceiptId = controller.receiptRepository.getNextReceiptId();
		if (sevaReceiptId <= 0) {
			controller.showAlert("Database Error", "Could not determine the next seva receipt ID.");
			return;
		}

		// Calculate seva total
		double sevaTotal = sevaEntries.stream().mapToDouble(SevaEntry::getTotalAmount).sum();

		// Create seva receipt data
		ReceiptData sevaReceiptData = new ReceiptData(
				sevaReceiptId, devoteeName, phoneNumber, address, raashi, nakshatra,
				date, FXCollections.observableArrayList(sevaEntries), sevaTotal, paymentMode, "ಇಲ್ಲ"
		);

		// Show seva receipt preview
		Consumer<Boolean> sevaAfterPrintAction = (printSuccess) -> {
			if (printSuccess) {
				String sevasDetailsString = formatSevasForDatabase(FXCollections.observableArrayList(sevaEntries));
				int actualSavedId = controller.receiptRepository.saveSpecificReceipt(
						sevaReceiptId, devoteeName, phoneNumber, address, raashi, nakshatra,
						date, sevasDetailsString, sevaTotal, paymentMode
				);

				if (actualSavedId != -1) {
					Platform.runLater(() -> {
						controller.showAlert("Seva Receipt Success", "Seva receipt printed and saved successfully with ID: " + actualSavedId);

					});
				} else {
					Platform.runLater(() -> controller.showAlert("Database Error", "Seva receipt printed but failed to save to database."));
				}
			} else {
				Platform.runLater(() -> controller.showAlert("Print Failed", "Seva receipt print job was cancelled or failed."));
			}
		};

		controller.receiptPrinter.showPrintPreview(sevaReceiptData, controller.mainStage, sevaAfterPrintAction);
	}

	private void handleDonationReceipts(String devoteeName, String phoneNumber, String address,
	                                    String raashi, String nakshatra, LocalDate date,
	                                    List<SevaEntry> donationEntries, String paymentMode) {

		// ✅ Get the initial donation receipt ID ONCE before the loop
		int donationReceiptId = DonationReceiptRepository.getNextDonationReceiptId();

		// Create separate receipt for each donation
		for (SevaEntry donation : donationEntries) {
			if (donationReceiptId <= 0) {
				controller.showAlert("Database Error", "Could not determine the next donation receipt ID.");
				continue;
			}

			// Extract donation name (remove "ದೇಣಿಗೆ : " prefix)
			String donationName = donation.getName().replace("ದೇಣಿಗೆ : ", "");

			// Create donation receipt data
			DonationReceiptData donationReceiptData = new DonationReceiptData(
					donationReceiptId, devoteeName, phoneNumber, address, raashi, nakshatra,
					date, donationName, donation.getTotalAmount(), paymentMode
			);

			// Show donation receipt preview
			int finalDonationReceiptId = donationReceiptId;
			Consumer<Boolean> donationAfterPrintAction = (printSuccess) -> {
				if (printSuccess) {
					DonationReceiptRepository repo = new DonationReceiptRepository();
					int actualSavedId = repo.saveSpecificDonationReceipt(
							finalDonationReceiptId, devoteeName, phoneNumber, address, raashi, nakshatra,
							date, donationName, donation.getTotalAmount(), paymentMode
					);

					if (actualSavedId != -1) {
						Platform.runLater(() -> controller.showAlert("Donation Receipt Success",
								"Donation receipt for " + donationName + " printed and saved successfully with ID: " + actualSavedId));
					} else {
						Platform.runLater(() -> controller.showAlert("Database Error",
								"Donation receipt printed but failed to save to database."));
					}
				} else {
					Platform.runLater(() -> controller.showAlert("Print Failed",
							"Donation receipt print job was cancelled or failed."));
				}
			};

			controller.receiptPrinter.showDonationPrintPreview(donationReceiptData, controller.mainStage, donationAfterPrintAction);

			// ✅ INCREMENT the ID for the next donation receipt
			donationReceiptId++;
		}

		// Clear form only after all receipts are processed
		Platform.runLater(() -> controller.clearForm());
	}


}