package com.pranav.temple_software.services;
import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.SevaReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.SevaReceiptRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.util.Collections;
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
		processSelectedItems(Collections.singletonList(item));
	}

	private void processSelectedItems(List<SevaEntry> itemsToProcess) {
		final String devoteeName = controller.devoteeNameField.getText();
		final String phoneNumber = controller.contactField.getText();
		final String address = controller.addressField.getText();
		final String panNumber = controller.panNumberField.getText();
		final LocalDate date = controller.sevaDatePicker.getValue();
		final String raashi = controller.raashiComboBox.getValue();
		final String nakshatra = controller.nakshatraComboBox.getValue();
		if (date == null || (!controller.cashRadio.isSelected() && !controller.onlineRadio.isSelected())) {
			controller.showAlert("Validation Error", "Please ensure date and payment method are selected.");
			return;
		}
		String paymentMode = controller.cashRadio.isSelected() ? "Cash" : "Online";

		// Mark items as printing
		itemsToProcess.forEach(item -> Platform.runLater(() -> item.setPrintStatus(SevaEntry.PrintStatus.PRINTING)));
		controller.updatePrintStatusLabel();
		handleSevaReceiptWithStatusTracking(devoteeName, phoneNumber, address, panNumber, raashi, nakshatra, date,
				itemsToProcess, paymentMode);
	}

	private void handleSevaReceiptWithStatusTracking(String devoteeName, String phoneNumber, String address, String panNumber,
	                                                 String raashi, String nakshatra, LocalDate date,
	                                                 List<SevaEntry> sevaEntries, String paymentMode) {

		double sevaTotal = sevaEntries.stream().mapToDouble(SevaEntry::getTotalAmount).sum();

		// Create a temporary receipt data object for the print preview
		SevaReceiptData sevaReceiptData = new SevaReceiptData(
				0, devoteeName, phoneNumber, address, panNumber, raashi, nakshatra,
				date, FXCollections.observableArrayList(sevaEntries), sevaTotal, paymentMode, "ಇಲ್ಲ"
		);

		Consumer<Boolean> afterActionCallback = (printSuccess) -> {
			if (printSuccess) {
				// First, save the main receipt to get its ID
				int actualSavedId = controller.sevaReceiptRepository.saveReceipt(
						devoteeName, phoneNumber, address, panNumber, raashi, nakshatra,
						date, sevaTotal, paymentMode
				);

				if (actualSavedId != -1) {
					// Then, save the associated items using the new receipt ID
					boolean itemsSaved = controller.sevaReceiptRepository.saveReceiptItems(actualSavedId, sevaEntries);
					if (itemsSaved) {
						markItemsAsSuccess(sevaEntries);
					} else {
						markItemsAsFailed(sevaEntries, "Saved receipt, but failed to save receipt items.");
					}
				} else {
					markItemsAsFailed(sevaEntries, "Failed to save receipt to database");
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
		controller.receiptPrinter.showPrintPreview(sevaReceiptData, controller.mainStage, afterActionCallback, onDialogClosed);
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
}