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

	public ReceiptServices(MainController controller) {
		this.controller = controller;
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

		Platform.runLater(() -> {
			failedItems.forEach(entry -> entry.setPrintStatus(SevaEntry.PrintStatus.PENDING));
			controller.updatePrintStatusLabel();
		});

		processSelectedItems(failedItems);
	}

	public void handleClearSuccessful() {
		List<SevaEntry> successfulItems = controller.selectedSevas.stream()
				.filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.SUCCESS)
				.toList();

		if (successfulItems.isEmpty()) {
			controller.showAlert("No Successful Items", "No successful items to clear.");
			return;
		}

		Platform.runLater(() -> {
			controller.selectedSevas.removeAll(successfulItems);
			controller.updatePrintStatusLabel();

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

		if (!sevaEntries.isEmpty()) {
			handleSevaReceiptWithStatusTracking(devoteeName, phoneNumber, address, raashi, nakshatra, date,
					sevaEntries, paymentMode);
		}

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

		Runnable onDialogClosed = () -> {
			boolean stillPrinting = sevaEntries.stream()
					.anyMatch(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.PRINTING);

			if (stillPrinting) {
				markItemsAsFailed(sevaEntries, "Print preview was cancelled");
			}
		};

		controller.receiptPrinter.showPrintPreviewWithCancelCallback(sevaReceiptData, controller.mainStage, sevaAfterPrintAction, onDialogClosed);
	}

	private void markItemsAsFailed(List<SevaEntry> items, String reason) {
		Platform.runLater(() -> {
			items.forEach(entry -> entry.setPrintStatus(SevaEntry.PrintStatus.FAILED));
			controller.updatePrintStatusLabel();
			Platform.runLater(() -> controller.showAlert("Print Failed", reason));
		});
	}

	private void markItemsAsSuccess(List<SevaEntry> items) {
		Platform.runLater(() -> {
			items.forEach(entry -> entry.setPrintStatus(SevaEntry.PrintStatus.SUCCESS));
			controller.updatePrintStatusLabel();
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
			Platform.runLater(() -> controller.showAlert("Print Failed", reason + " for: " + item.getName()));
		});
	}

	/**
	 * *** BUG FIX ***
	 * Corrected the format to match what the parseSevas method expects.
	 * It now uses ":" to separate parts and ";" to separate entries.
	 * Format: "SevaName:BaseAmount:Quantity;AnotherSeva:BaseAmount:Quantity"
	 */
	private String formatSevasForDatabase(ObservableList<SevaEntry> sevas) {
		return sevas.stream()
				.map(seva -> String.join(":",
						seva.getName(),
						String.valueOf(seva.getAmount()), // Base amount per unit
						String.valueOf(seva.getQuantity())
				))
				.collect(Collectors.joining(";"));
	}
}
