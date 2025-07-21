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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer; // Import Consumer
import java.util.stream.Collectors;

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

		// **NEW: Success tracking for ALL receipts**
		final AtomicInteger totalReceipts = new AtomicInteger(0);
		final AtomicInteger successfulReceipts = new AtomicInteger(0);

		// Calculate total receipts that will be generated
		if (!sevaEntries.isEmpty()) totalReceipts.incrementAndGet();
		totalReceipts.addAndGet(donationEntries.size());

		// Success callback to track individual receipt printing
		Consumer<Boolean> trackPrintSuccess = (success) -> {
			if (success) {
				int completed = successfulReceipts.incrementAndGet();
				System.out.println("Receipt " + completed + " of " + totalReceipts.get() + " printed successfully");

				// Clear form ONLY when ALL receipts are printed successfully
				if (completed == totalReceipts.get()) {
					Platform.runLater(() -> {
						controller.clearForm();
						Platform.runLater(() -> controller.devoteeNameField.requestFocus());
						controller.showAlert("All Receipts Printed",
								"All " + totalReceipts.get() + " receipts have been printed and saved successfully!");
					});
				}
			} else {
				Platform.runLater(() -> {
					controller.showAlert("Print Failed",
							"One or more receipts failed to print. Form will not be cleared.");
				});
			}
		};

		// Handle seva receipt if sevas exist
		if (!sevaEntries.isEmpty()) {
			handleSevaReceiptWithCallback(devoteeName, phoneNumber, address, raashi, nakshatra, date,
					sevaEntries, pendingPaymentMode, trackPrintSuccess);
		}

		// Handle donation receipts (one per donation)
		if (!donationEntries.isEmpty()) {
			handleDonationReceiptsWithCallback(devoteeName, phoneNumber, address, raashi, nakshatra, date,
					donationEntries, pendingPaymentMode, trackPrintSuccess);
		}

		processReceiptsWithStatusTracking(devoteeName, phoneNumber, address, raashi, nakshatra, date, currentSevas, pendingPaymentMode);
	}


	public void handlePrintAllPending() {
		// Get only pending items
		List<SevaEntry> pendingItems = controller.selectedSevas.stream()
				.filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.PENDING)
				.collect(Collectors.toList());

		if (pendingItems.isEmpty()) {
			controller.showAlert("No Pending Items", "All items have been processed.");
			return;
		}

		// Process only pending items
		processSelectedItems(pendingItems);
	}

	public void handleRetryFailed() {
		// Get only failed items and reset them to pending
		List<SevaEntry> failedItems = controller.selectedSevas.stream()
				.filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.FAILED)
				.collect(Collectors.toList());

		if (failedItems.isEmpty()) {
			controller.showAlert("No Failed Items", "No failed items to retry.");
			return;
		}

		// Reset failed items to pending
		failedItems.forEach(entry -> entry.setPrintStatus(SevaEntry.PrintStatus.PENDING));
		controller.updatePrintStatusLabel();

		// Process failed items
		processSelectedItems(failedItems);
	}

	public void handleClearSuccessful() {
		// Remove only successful items
		List<SevaEntry> successfulItems = controller.selectedSevas.stream()
				.filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.SUCCESS)
				.collect(Collectors.toList());

		if (successfulItems.isEmpty()) {
			controller.showAlert("No Successful Items", "No successful items to clear.");
			return;
		}

		controller.selectedSevas.removeAll(successfulItems);
		controller.updatePrintStatusLabel();

		// Check if form should be cleared completely
		if (controller.selectedSevas.isEmpty()) {
			Platform.runLater(() -> controller.clearForm());
		}
	}

	public void retryIndividualItem(SevaEntry item) {
		processSelectedItems(Collections.singletonList(item));
	}

	private void processSelectedItems(List<SevaEntry> itemsToProcess) {
		// Get form data
		final String devoteeName = controller.devoteeNameField.getText();
		final String phoneNumber = controller.contactField.getText();
		final String address = controller.addressField.getText();
		final LocalDate date = controller.sevaDatePicker.getValue();
		final String raashi = controller.raashiComboBox.getValue();
		final String nakshatra = controller.nakshatraComboBox.getValue();

		// Validate required fields
		if (date == null || (!controller.cashRadio.isSelected() && !controller.onlineRadio.isSelected())) {
			controller.showAlert("Validation Error", "Please ensure date and payment method are selected.");
			return;
		}

		String paymentMode = controller.cashRadio.isSelected() ? "Cash" : "Online";

		processReceiptsWithStatusTracking(devoteeName, phoneNumber, address, raashi, nakshatra, date,
				FXCollections.observableArrayList(itemsToProcess), paymentMode);
	}

	private void processReceiptsWithStatusTracking(String devoteeName, String phoneNumber, String address,
	                                               String raashi, String nakshatra, LocalDate date,
	                                               ObservableList<SevaEntry> items, String paymentMode) {

		// Separate seva and donation entries
		List<SevaEntry> sevaEntries = new ArrayList<>();
		List<SevaEntry> donationEntries = new ArrayList<>();

		for (SevaEntry entry : items) {
			if (entry.getPrintStatus() == SevaEntry.PrintStatus.PENDING) {
				entry.setPrintStatus(SevaEntry.PrintStatus.PRINTING);
				if (entry.getName().startsWith("ದೇಣಿಗೆ ")) {
					donationEntries.add(entry);
				} else {
					sevaEntries.add(entry);
				}
			}
		}

		controller.updatePrintStatusLabel();

		// Handle seva receipt if sevas exist
		if (!sevaEntries.isEmpty()) {
			handleSevaReceiptWithStatusTracking(devoteeName, phoneNumber, address, raashi, nakshatra, date,
					sevaEntries, paymentMode);
		}

		// Handle donation receipts (one per donation)
		for (SevaEntry donation : donationEntries) {
			handleDonationReceiptWithStatusTracking(devoteeName, phoneNumber, address, raashi, nakshatra, date,
					donation, paymentMode);
		}
	}

	private void handleSevaReceiptWithStatusTracking(String devoteeName, String phoneNumber, String address,
	                                                 String raashi, String nakshatra, LocalDate date,
	                                                 List<SevaEntry> sevaEntries, String paymentMode) {

		int sevaReceiptId = controller.receiptRepository.getNextReceiptId();
		if (sevaReceiptId <= 0) {
			markItemsAsFailed(sevaEntries, "Could not generate receipt ID");
			return;
		}

		double sevaTotal = sevaEntries.stream().mapToDouble(SevaEntry::getTotalAmount).sum();
		ReceiptData sevaReceiptData = new ReceiptData(
				sevaReceiptId, devoteeName, phoneNumber, address, raashi, nakshatra,
				date, FXCollections.observableArrayList(sevaEntries), sevaTotal, paymentMode, "ಇಲ್ಲ"
		);

		Consumer<Boolean> sevaAfterPrintAction = (printSuccess) -> {
			if (printSuccess) {
				String sevasDetailsString = formatSevasForDatabase(FXCollections.observableArrayList(sevaEntries));
				int actualSavedId = controller.receiptRepository.saveSpecificReceipt(
						sevaReceiptId, devoteeName, phoneNumber, address, raashi, nakshatra,
						date, sevasDetailsString, sevaTotal, paymentMode
				);

				if (actualSavedId != -1) {
					markItemsAsSuccess(sevaEntries);
					Platform.runLater(() -> controller.showAlert("Seva Receipt Success",
							"Seva receipt printed and saved successfully with ID: " + actualSavedId));
				} else {
					markItemsAsFailed(sevaEntries, "Failed to save to database");
				}
			} else {
				markItemsAsFailed(sevaEntries, "Print job was cancelled or failed");
			}
			controller.updatePrintStatusLabel();
		};

		controller.receiptPrinter.showPrintPreview(sevaReceiptData, controller.mainStage, sevaAfterPrintAction);
	}

	private void handleDonationReceiptWithStatusTracking(String devoteeName, String phoneNumber, String address,
	                                                     String raashi, String nakshatra, LocalDate date,
	                                                     SevaEntry donation, String paymentMode) {

		int donationReceiptId = DonationReceiptRepository.getNextDonationReceiptId();
		if (donationReceiptId <= 0) {
			markItemAsFailed(donation, "Could not generate receipt ID");
			return;
		}

		String donationName = donation.getName().replace("ದೇಣಿಗೆ : ", "");
		DonationReceiptData donationReceiptData = new DonationReceiptData(
				donationReceiptId, devoteeName, phoneNumber, address, raashi, nakshatra,
				date, donationName, donation.getTotalAmount(), paymentMode
		);

		Consumer<Boolean> donationAfterPrintAction = (printSuccess) -> {
			if (printSuccess) {
				DonationReceiptRepository repo = new DonationReceiptRepository();
				int actualSavedId = repo.saveSpecificDonationReceipt(
						donationReceiptId, devoteeName, phoneNumber, address, raashi, nakshatra,
						date, donationName, donation.getTotalAmount(), paymentMode
				);

				if (actualSavedId != -1) {
					markItemAsSuccess(donation);
					Platform.runLater(() -> controller.showAlert("Donation Receipt Success",
							"Donation receipt for " + donationName + " printed and saved successfully with ID: " + actualSavedId));
				} else {
					markItemAsFailed(donation, "Failed to save to database");
				}
			} else {
				markItemAsFailed(donation, "Print job was cancelled or failed");
			}
			controller.updatePrintStatusLabel();
		};

		controller.receiptPrinter.showDonationPrintPreview(donationReceiptData, controller.mainStage, donationAfterPrintAction);
	}

	private void markItemsAsSuccess(List<SevaEntry> items) {
		Platform.runLater(() -> {
			items.forEach(entry -> entry.setPrintStatus(SevaEntry.PrintStatus.SUCCESS));
		});
	}

	private void markItemsAsFailed(List<SevaEntry> items, String reason) {
		Platform.runLater(() -> {
			items.forEach(entry -> entry.setPrintStatus(SevaEntry.PrintStatus.FAILED));
			controller.showAlert("Print Failed", reason);
		});
	}

	private void markItemAsSuccess(SevaEntry item) {
		Platform.runLater(() -> item.setPrintStatus(SevaEntry.PrintStatus.SUCCESS));
	}

	private void markItemAsFailed(SevaEntry item, String reason) {
		Platform.runLater(() -> {
			item.setPrintStatus(SevaEntry.PrintStatus.FAILED);
			controller.showAlert("Print Failed", reason + " for: " + item.getName());
		});
	}

	private void handleSevaReceiptWithCallback(String devoteeName, String phoneNumber, String address,
	                                           String raashi, String nakshatra, LocalDate date,
	                                           List<SevaEntry> sevaEntries, String paymentMode,
	                                           Consumer<Boolean> successCallback) {
		// Get next seva receipt ID
		int sevaReceiptId = controller.receiptRepository.getNextReceiptId();
		if (sevaReceiptId <= 0) {
			controller.showAlert("Database Error", "Could not determine the next seva receipt ID.");
			successCallback.accept(false); // Report failure
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
					successCallback.accept(true); // Report success
				} else {
					Platform.runLater(() -> controller.showAlert("Database Error", "Seva receipt printed but failed to save to database."));
					successCallback.accept(false); // Report failure
				}
			} else {
				Platform.runLater(() -> controller.showAlert("Print Failed", "Seva receipt print job was cancelled or failed."));
				successCallback.accept(false); // Report failure
			}
		};

		controller.receiptPrinter.showPrintPreview(sevaReceiptData, controller.mainStage, sevaAfterPrintAction);
	}


	private void handleDonationReceiptsWithCallback(String devoteeName, String phoneNumber, String address,
	                                                String raashi, String nakshatra, LocalDate date,
	                                                List<SevaEntry> donationEntries, String paymentMode,
	                                                Consumer<Boolean> successCallback) {

		// Get the initial donation receipt ID ONCE before the loop
		int donationReceiptId = DonationReceiptRepository.getNextDonationReceiptId();

		// Create separate receipt for each donation
		for (SevaEntry donation : donationEntries) {
			if (donationReceiptId <= 0) {
				controller.showAlert("Database Error", "Could not determine the next donation receipt ID.");
				successCallback.accept(false); // Report failure
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
						successCallback.accept(true); // Report success
					} else {
						Platform.runLater(() -> controller.showAlert("Database Error",
								"Donation receipt printed but failed to save to database."));
						successCallback.accept(false); // Report failure
					}
				} else {
					Platform.runLater(() -> controller.showAlert("Print Failed",
							"Donation receipt print job was cancelled or failed."));
					successCallback.accept(false); // Report failure
				}
			};

			controller.receiptPrinter.showDonationPrintPreview(donationReceiptData, controller.mainStage, donationAfterPrintAction);

			// INCREMENT the ID for the next donation receipt
			donationReceiptId++;
		}

		// Remove the old form clearing code from here - it's now handled by the success callback
	}



}