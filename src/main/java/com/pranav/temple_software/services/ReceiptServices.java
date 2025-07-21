package com.pranav.temple_software.services;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.DonationReceiptData;
import com.pranav.temple_software.models.ReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.DonationReceiptRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ReceiptServices {
	private final MainController controller;
	private int pendingReceiptId = -1;
	private ReceiptData pendingReceiptData = null;
	private String pendingPaymentMode = "N/A";

	public ReceiptServices(MainController controller) {
		this.controller = controller;
	}

	public void handlePrintPreview() {
		// Reset all statuses to pending
		controller.resetPrintStatuses();

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
		double total;
		try {
			String totalText = controller.totalLabel.getText().replaceAll("[^\\d.]", "");
			total = Double.parseDouble(totalText);
		} catch (NumberFormatException | NullPointerException ex) {
			total = currentSevas.stream().mapToDouble(SevaEntry::getTotalAmount).sum();
			System.err.println("Could not parse total from label, recalculating.");
		}

		this.pendingPaymentMode = controller.cashRadio.isSelected() ? "Cash" :
				(controller.onlineRadio.isSelected() ? "Online" : "N/A");

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

		// Process receipts with status tracking
		processReceiptsWithStatusTracking(devoteeName, phoneNumber, address, raashi, nakshatra, date, currentSevas, pendingPaymentMode);
	}

	public void handlePrintAllPending() {
		List<SevaEntry> pendingItems = controller.selectedSevas.stream()
				.filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.PENDING)
				.collect(Collectors.toList());

		if (pendingItems.isEmpty()) {
			controller.showAlert("No Pending Items", "All items have been processed.");
			return;
		}

		processSelectedItems(pendingItems);
	}

	public void handleRetryFailed() {
		List<SevaEntry> failedItems = controller.selectedSevas.stream()
				.filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.FAILED)
				.collect(Collectors.toList());

		if (failedItems.isEmpty()) {
			controller.showAlert("No Failed Items", "No failed items to retry.");
			return;
		}

		// Reset failed items to pending
		Platform.runLater(() -> {
			failedItems.forEach(entry -> entry.setPrintStatus(SevaEntry.PrintStatus.PENDING));
			controller.updatePrintStatusLabel();
		});

		processSelectedItems(failedItems);
	}

	public void handleClearSuccessful() {
		List<SevaEntry> successfulItems = controller.selectedSevas.stream()
				.filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.SUCCESS)
				.collect(Collectors.toList());

		if (successfulItems.isEmpty()) {
			controller.showAlert("No Successful Items", "No successful items to clear.");
			return;
		}

		Platform.runLater(() -> {
			controller.selectedSevas.removeAll(successfulItems);
			controller.updatePrintStatusLabel();

			// Check if form should be cleared completely
			if (controller.selectedSevas.isEmpty()) {
				controller.clearForm();
			}
		});
	}

	public void retryIndividualItem(SevaEntry item) {
		Platform.runLater(() -> item.setPrintStatus(SevaEntry.PrintStatus.PENDING));
		processSelectedItems(Arrays.asList(item));
	}

	private void processSelectedItems(List<SevaEntry> itemsToProcess) {
		final String devoteeName = controller.devoteeNameField.getText();
		final String phoneNumber = controller.contactField.getText();
		final String address = controller.addressField.getText();
		final LocalDate date = controller.sevaDatePicker.getValue();
		final String raashi = controller.raashiComboBox.getValue();
		final String nakshatra = controller.nakshatraComboBox.getValue();

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
				Platform.runLater(() -> {
					entry.setPrintStatus(SevaEntry.PrintStatus.PRINTING);
					controller.updatePrintStatusLabel();
				});

				if (entry.getName().startsWith("ದೇಣಿಗೆ ")) {
					donationEntries.add(entry);
				} else {
					sevaEntries.add(entry);
				}
			}
		}

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

		// **KEY FIX: Add cancellation callback**
		Runnable onDialogClosed = () -> {
			if (donation.getPrintStatus() == SevaEntry.PrintStatus.PRINTING) {
				markItemAsFailed(donation, "Print preview was cancelled");
			}
		};

		controller.receiptPrinter.showDonationPrintPreviewWithCancelCallback(donationReceiptData, controller.mainStage, donationAfterPrintAction, onDialogClosed);
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

		// **KEY FIX: Add a cancellation callback for when dialog is closed**
		Runnable onDialogClosed = () -> {
			// Check if items are still in PRINTING status when dialog closes
			boolean stillPrinting = sevaEntries.stream()
					.anyMatch(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.PRINTING);

			if (stillPrinting) {
				markItemsAsFailed(sevaEntries, "Print preview was cancelled");
			}
		};

		controller.receiptPrinter.showPrintPreviewWithCancelCallback(sevaReceiptData, controller.mainStage, sevaAfterPrintAction, onDialogClosed);
	}


	private void markItemsAsSuccess(List<SevaEntry> items) {
		Platform.runLater(() -> {
			items.forEach(entry -> entry.setPrintStatus(SevaEntry.PrintStatus.SUCCESS));
			controller.updatePrintStatusLabel();
		});
	}

	private void markItemsAsFailed(List<SevaEntry> items, String reason) {
		Platform.runLater(() -> {
			items.forEach(entry -> entry.setPrintStatus(SevaEntry.PrintStatus.FAILED));
			controller.updatePrintStatusLabel();
			controller.showAlert("Print Failed", reason);
		});
	}

	private void markItemAsSuccess(SevaEntry item) {
		Platform.runLater(() -> {
			item.setPrintStatus(SevaEntry.PrintStatus.SUCCESS);
			controller.updatePrintStatusLabel();
		});
	}

	private void markItemAsFailed(SevaEntry item, String reason) {
		Platform.runLater(() -> {
			item.setPrintStatus(SevaEntry.PrintStatus.FAILED);
			controller.updatePrintStatusLabel();
			controller.showAlert("Print Failed", reason + " for: " + item.getName());
		});
	}

	private String formatSevasForDatabase(ObservableList<SevaEntry> sevas) {
		return sevas.stream()
				.map(seva -> seva.getName() + " x " + seva.getQuantity() + " = ₹" + String.format("%.2f", seva.getTotalAmount()))
				.collect(Collectors.joining(", "));
	}
}
