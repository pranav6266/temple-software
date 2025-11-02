package com.pranav.temple_software.services;
import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.SevaReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.utils.DatabaseManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;

import java.sql.Connection;
import java.sql.SQLException;
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
		String rashiValue = controller.raashiComboBox.getValue();
		final String raashi = (rashiValue != null && rashiValue.equals("ಆಯ್ಕೆ")) ?
				"" : rashiValue;
		final String nakshatra = controller.nakshatraComboBox.getValue();

		if (date == null || (!controller.cashRadio.isSelected() && !controller.onlineRadio.isSelected())) {
			controller.showAlert("Validation Error", "Please ensure date and payment method are selected.");
			return;
		}
		String paymentMode = controller.cashRadio.isSelected() ? "Cash" : "Online";
		double sevaTotal = itemsToProcess.stream().mapToDouble(SevaEntry::getTotalAmount).sum();

		// Mark items as printing
		itemsToProcess.forEach(item -> Platform.runLater(() -> item.setPrintStatus(SevaEntry.PrintStatus.PRINTING)));
		controller.updatePrintStatusLabel();

		// --- NEW LOGIC: SAVE TO DB FIRST ---
		int actualSavedId = -1;
		Connection conn = null;
		try {
			conn = DatabaseManager.getConnection();
			conn.setAutoCommit(false); // Start transaction

			actualSavedId = controller.sevaReceiptRepository.saveReceipt(
					conn, devoteeName, phoneNumber, address, panNumber, raashi, nakshatra,
					date, sevaTotal, paymentMode
			);

			if (actualSavedId != -1) {
				boolean itemsSaved = controller.sevaReceiptRepository.saveReceiptItems(conn, actualSavedId, itemsToProcess);
				if (itemsSaved) {
					conn.commit(); // All good, commit the transaction
				} else {
					conn.rollback(); // Something failed, rollback
					markItemsAsFailed(itemsToProcess, "Failed to save receipt items.");
					return; // Stop here
				}
			} else {
				conn.rollback(); // Something failed, rollback
				markItemsAsFailed(itemsToProcess, "Failed to save receipt to database.");
				return; // Stop here
			}
		} catch (SQLException e) {
			if (conn != null) {
				try { conn.rollback(); } catch (SQLException ex) { /* Log rollback failure */ }
			}
			markItemsAsFailed(itemsToProcess, "A database error occurred: " + e.getMessage());
			return; // Stop here
		} finally {
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { /* Log connection closing failure */ }
			}
		}
		// --- END OF NEW DB LOGIC ---

		// If we reach here, the DB save was successful and actualSavedId is valid
		// Now, show the print preview with the *correct* ID

		SevaReceiptData sevaReceiptData = new SevaReceiptData(
				actualSavedId, devoteeName, phoneNumber, address, panNumber, raashi, nakshatra,
				date, FXCollections.observableArrayList(itemsToProcess), sevaTotal, paymentMode
		);

		// This callback now *only* handles the result of the *print*
		Consumer<Boolean> afterPrintCallback = (printSuccess) -> {
			if (printSuccess) {
				markItemsAsSuccess(itemsToProcess);
			} else {
				markItemsAsFailed(itemsToProcess, "Print job was cancelled or failed");
			}
			controller.updatePrintStatusLabel();
		};

		// This callback handles if the preview dialog is closed
		Runnable onDialogClosed = () -> {
			boolean stillPrinting = itemsToProcess.stream()
					.anyMatch(entry -> entry.getPrintStatus() == SevaEntry.PrintStatus.PRINTING);
			if (stillPrinting) {
				markItemsAsFailed(itemsToProcess, "Print preview was cancelled");
			}
		};

		controller.receiptPrinter.showPrintPreview(sevaReceiptData, controller.mainStage, afterPrintCallback, onDialogClosed);
	}

	private void markItemsAsFailed(List<SevaEntry> items, String reason) {
		Platform.runLater(() -> {
			items.forEach(entry -> entry.setPrintStatus(SevaEntry.PrintStatus.FAILED));
			controller.updatePrintStatusLabel();
		});
	}

	private void markItemsAsSuccess(List<SevaEntry> items) {
		Platform.runLater(() -> {
			items.forEach(entry -> entry.setPrintStatus(SevaEntry.PrintStatus.SUCCESS));
			controller.updatePrintStatusLabel();
		});
	}
}