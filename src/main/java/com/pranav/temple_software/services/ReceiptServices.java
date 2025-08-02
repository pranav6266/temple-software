// ReceiptServices.java
package com.pranav.temple_software.services;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.DonationReceiptData;
import com.pranav.temple_software.models.SevaReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.DonationReceiptRepository;
import com.pranav.temple_software.repositories.SevaReceiptRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ReceiptServices {
	private final MainController controller;

	// NEW: Enum to define the action type
	private enum ActionType {
		PRINT,
		SAVE_PDF
	}

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

		processSelectedItems(pendingItems, ActionType.PRINT);
	}

	// NEW: Method to handle saving all pending items as PDFs
	public void handleSaveAllPendingAsPdf() {
		List<SevaEntry> pendingItems = controller.selectedSevas.stream()
				.filter(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.PENDING)
				.collect(Collectors.toList());

		if (pendingItems.isEmpty()) {
			controller.showAlert("No Pending Items", "All items have been processed.");
			return;
		}

		processSelectedItems(pendingItems, ActionType.SAVE_PDF);
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

		processSelectedItems(failedItems, ActionType.PRINT); // Default retry action is to print
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
		processSelectedItems(Collections.singletonList(item), ActionType.PRINT);
	}

	// MODIFIED: Added ActionType parameter
	private void processSelectedItems(List<SevaEntry> itemsToProcess, ActionType actionType) {
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
				FXCollections.observableArrayList(itemsToProcess), paymentMode, actionType);
	}

	// MODIFIED: Added ActionType parameter
	private void processReceiptsWithStatusTracking(String devoteeName, String phoneNumber, String address,
	                                               String raashi, String nakshatra, LocalDate date,
	                                               ObservableList<SevaEntry> items, String paymentMode, ActionType actionType) {

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
					sevaEntries, paymentMode, actionType);
		}

		for (SevaEntry donation : donationEntries) {
			handleDonationReceiptWithStatusTracking(devoteeName, phoneNumber, address, raashi, nakshatra, date,
					donation, paymentMode, actionType);
		}
	}

	// MODIFIED: Added ActionType parameter
	private void handleDonationReceiptWithStatusTracking(String devoteeName, String phoneNumber, String address,
	                                                     String raashi, String nakshatra, LocalDate date,
	                                                     SevaEntry donation, String paymentMode, ActionType actionType) {

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

		Consumer<Boolean> afterActionCallback = (success) -> {
			if (success) {
				DonationReceiptRepository repo = new DonationReceiptRepository();
				int actualSavedId = repo.saveSpecificDonationReceipt(
						donationReceiptId, devoteeName, phoneNumber, address, raashi, nakshatra,
						date, donationName, donation.getTotalAmount(), paymentMode
				);

				if (actualSavedId != -1) {
					markItemAsSuccess(donation);
					String successMessage = actionType == ActionType.SAVE_PDF ? "saved" : "printed";
					Platform.runLater(() -> controller.showAlert("Donation Receipt Success",
							"Donation receipt for " + donationName + " " + successMessage + " successfully with ID: " + actualSavedId));
				} else {
					markItemAsFailed(donation, "Failed to save to database");
				}
			} else {
				String failureMessage = actionType == ActionType.SAVE_PDF ? "Save job failed" : "Print job was cancelled or failed";
				markItemAsFailed(donation, failureMessage);
			}
			controller.updatePrintStatusLabel();
		};

		if (actionType == ActionType.PRINT) {
			Runnable onDialogClosed = () -> {
				if (donation.getPrintStatus() == SevaEntry.PrintStatus.PRINTING) {
					markItemAsFailed(donation, "Print preview was cancelled");
				}
			};
			controller.receiptPrinter.showDonationPrintPreviewWithCancelCallback(donationReceiptData, controller.mainStage, afterActionCallback, onDialogClosed);
		} else { // SAVE_PDF
			controller.receiptPrinter.saveDonationReceiptAsPdf(donationReceiptData, afterActionCallback);
		}
	}

	// MODIFIED: Added ActionType parameter
	private void handleSevaReceiptWithStatusTracking(String devoteeName, String phoneNumber, String address,
	                                                 String raashi, String nakshatra, LocalDate date,
	                                                 List<SevaEntry> sevaEntries, String paymentMode, ActionType actionType) {

		int sevaReceiptId = SevaReceiptRepository.getNextReceiptId();
		if (sevaReceiptId <= 0) {
			markItemsAsFailed(sevaEntries, "Could not generate receipt ID");
			return;
		}

		double sevaTotal = sevaEntries.stream().mapToDouble(SevaEntry::getTotalAmount).sum();
		SevaReceiptData sevaReceiptData = new SevaReceiptData(
				sevaReceiptId, devoteeName, phoneNumber, address, raashi, nakshatra,
				date, FXCollections.observableArrayList(sevaEntries), sevaTotal, paymentMode, "ಇಲ್ಲ"
		);

		Consumer<Boolean> afterActionCallback = (success) -> {
			if (success) {
				String sevasDetailsString = formatSevasForDatabase(FXCollections.observableArrayList(sevaEntries));
				int actualSavedId = controller.sevaReceiptRepository.saveSpecificReceipt(
						sevaReceiptId, devoteeName, phoneNumber, address, raashi, nakshatra,
						date, sevasDetailsString, sevaTotal, paymentMode
				);

				if (actualSavedId != -1) {
					markItemsAsSuccess(sevaEntries);
					String successMessage = actionType == ActionType.SAVE_PDF ? "saved" : "printed";
					Platform.runLater(() -> controller.showAlert("Seva Receipt Success",
							"Seva receipt " + successMessage + " successfully with ID: " + actualSavedId));
				} else {
					markItemsAsFailed(sevaEntries, "Failed to save to database");
				}
			} else {
				String failureMessage = actionType == ActionType.SAVE_PDF ? "Save job failed" : "Print job was cancelled or failed";
				markItemsAsFailed(sevaEntries, failureMessage);
			}
			controller.updatePrintStatusLabel();
		};

		if (actionType == ActionType.PRINT) {
			Runnable onDialogClosed = () -> {
				boolean stillPrinting = sevaEntries.stream()
						.anyMatch(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.PRINTING);

				if (stillPrinting) {
					markItemsAsFailed(sevaEntries, "Print preview was cancelled");
				}
			};
			controller.receiptPrinter.showPrintPreviewWithCancelCallback(sevaReceiptData, controller.mainStage, afterActionCallback, onDialogClosed);
		} else { // SAVE_PDF
			controller.receiptPrinter.saveSevaReceiptAsPdf(sevaReceiptData, afterActionCallback);
		}
	}

	private void markItemsAsFailed(List<SevaEntry> items, String reason) {
		Platform.runLater(() -> {
			items.forEach(entry -> entry.setPrintStatus(SevaEntry.PrintStatus.FAILED));
			controller.updatePrintStatusLabel();
			Platform.runLater(() -> controller.showAlert("Action Failed", reason));
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
			Platform.runLater(() -> controller.showAlert("Action Failed", reason + " for: " + item.getName()));
		});
	}

	private String formatSevasForDatabase(ObservableList<SevaEntry> sevas) {
		return sevas.stream()
				.map(seva -> String.join(":",
						seva.getName(),
						String.valueOf(seva.getAmount()),
						String.valueOf(seva.getQuantity())
				))
				.collect(Collectors.joining(";"));
	}
}