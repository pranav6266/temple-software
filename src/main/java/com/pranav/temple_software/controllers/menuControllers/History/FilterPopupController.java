// FILE: src\main\java\com\pranav\temple_software\controllers\menuControllers\History\FilterPopupController.java
package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.ReceiptData;
import com.pranav.temple_software.repositories.ReceiptRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // Import Collectors

public class FilterPopupController {

	@FXML private ComboBox<String> sevaTypeComboBox;
	@FXML private DatePicker datePicker;
	@FXML private ComboBox<String> monthComboBox;
	@FXML private ComboBox<String> yearComboBox;
	@FXML private RadioButton onlineRadio;
	@FXML private RadioButton offlineRadio;
	@FXML private Button applyButton;
	@FXML private Button clearButton; // Assuming you add fx:id="clearButton" to your FXML

	private final ReceiptRepository receiptRepository = new ReceiptRepository();
	private FilterListener listener;

	// --- State variables to hold the filter values ---
	private String initialSevaType = "ಎಲ್ಲಾ"; // Default to "All" in Kannada [cite: 159]
	private LocalDate initialDate = null;
	private String initialMonth = "All";
	private String initialYear = "";
	private boolean initialOnline = false;
	private boolean initialOffline = false;

	// --- Interface for the listener ---
	public interface FilterListener {
		void onFiltersApplied(List<ReceiptData> filteredReceipts, String sevaType, LocalDate date, String month, String year, boolean online, boolean offline);
		void onFiltersCleared(); // Add method for clearing
	}

	// --- Setter for the listener ---
	public void setFilterListener(FilterListener listener) {
		this.listener = listener;
	}

	// --- Method to receive the initial/saved filter state from HistoryController ---
	public void setInitialFilterState(String sevaType, LocalDate date, String month, String year, boolean online, boolean offline) {
		// Store the passed-in state
		this.initialSevaType = (sevaType == null || sevaType.isEmpty()) ? "ಎಲ್ಲಾ" : sevaType;
		this.initialDate = date;
		this.initialMonth = (month == null || month.isEmpty()) ? "All" : month;
		this.initialYear = (year == null) ? "" : year;
		this.initialOnline = online;
		this.initialOffline = offline;

		// Apply the initial state to the UI controls
		applyStateToUI();
	}

	// --- Apply the stored state variables to the UI controls ---
	private void applyStateToUI() {
		sevaTypeComboBox.setValue(initialSevaType);
		datePicker.setValue(initialDate);
		monthComboBox.setValue(initialMonth);
		yearComboBox.setValue(initialYear);
		onlineRadio.setSelected(initialOnline);
		offlineRadio.setSelected(initialOffline);
	}


	@FXML
	public void initialize() {
		// Initialize ComboBox options
		sevaTypeComboBox.setItems(FXCollections.observableArrayList("ಎಲ್ಲಾ", "ಸೇವಾ", "ದೇಣಿಗೆ")); // [cite: 159]
		monthComboBox.setItems(FXCollections.observableArrayList("All", "JANUARY", "FEBRUARY", "MARCH", "APRIL",
				"MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER")); // [cite: 160]

		List<String> years = new ArrayList<>();
		years.add(""); // Option for no year filter
		int currentYear = LocalDate.now().getYear();
		for (int y = currentYear; y >= 2000; y--) { // [cite: 161]
			years.add(String.valueOf(y));
		}
		yearComboBox.setItems(FXCollections.observableArrayList(years)); // [cite: 161]

		// Set default values initially (will be overridden by applyStateToUI if setInitialFilterState is called)
		sevaTypeComboBox.setValue(initialSevaType);
		monthComboBox.setValue(initialMonth);
		yearComboBox.setValue(initialYear);
		datePicker.setValue(initialDate);
		onlineRadio.setSelected(initialOnline);
		offlineRadio.setSelected(initialOffline);

		// Ensure radio buttons are mutually exclusive (optional, can be done in FXML with ToggleGroup)
		ToggleGroup paymentGroup = new ToggleGroup();
		onlineRadio.setToggleGroup(paymentGroup);
		offlineRadio.setToggleGroup(paymentGroup);
	}

	@FXML
	public void handleApplyFilters() {
		// 1. Get all receipts
		List<ReceiptData> allReceipts = receiptRepository.getAllReceipts(); // [cite: 162]
		List<ReceiptData> filteredReceipts = new ArrayList<>(allReceipts);

		// 2. Get current selections from UI
		String selectedType = sevaTypeComboBox.getValue(); // [cite: 162]
		LocalDate selectedDate = datePicker.getValue(); // [cite: 163]
		String selectedMonth = monthComboBox.getValue(); // [cite: 164]
		String selectedYear = yearComboBox.getValue(); // [cite: 165]
		boolean isOnlineSelected = onlineRadio.isSelected(); // [cite: 166]
		boolean isOfflineSelected = offlineRadio.isSelected(); // [cite: 166]

		// 3. Apply filters based on UI selections

		// Filter 1: Donation Status (based on isDonation column via donationStatus field)
		if (selectedType != null && !selectedType.equals("ಎಲ್ಲಾ")) { // [cite: 159]
			filteredReceipts.removeIf(receipt -> {
				// donationStatus is "ಹೌದು" for donation, "ಇಲ್ಲ" otherwise [cite: 391, 511]
				String status = receipt.getDonationStatus().trim();
				if (selectedType.equals("ದೇಣಿಗೆ")) { // Filter for Donations [cite: 159]
					return !status.equals("ಹೌದು"); // Keep if status is "ಹೌದು"
				} else if (selectedType.equals("ಸೇವಾ")) { // Filter for Sevas (Non-Donations) [cite: 159]
					return !status.equals("ಇಲ್ಲ"); // Keep if status is "ಇಲ್ಲ"
				}
				return false; // Should not happen with current options
			});
		}

		// Filter 2: Date
		if (selectedDate != null) {
			filteredReceipts.removeIf(receipt -> !receipt.getSevaDate().equals(selectedDate)); // [cite: 164]
		}

		// Filter 2.1: Month (Only if Date is not selected)
		if (selectedDate == null && selectedMonth != null && !selectedMonth.equals("All")) { // [cite: 164]
			filteredReceipts.removeIf(receipt ->
					!receipt.getSevaDate().getMonth().name().equalsIgnoreCase(selectedMonth)); // [cite: 164]
		}

		// Filter 2.2: Year (Only if Date is not selected)
		if (selectedDate == null && selectedYear != null && !selectedYear.isEmpty()) { // [cite: 165]
			filteredReceipts.removeIf(receipt ->
					receipt.getSevaDate().getYear() != Integer.parseInt(selectedYear)); // [cite: 165]
		}


		// Filter 3: Payment Mode (based on paymentMode column)
		if (isOnlineSelected && !isOfflineSelected) { // Online only
			filteredReceipts.removeIf(receipt -> !("Online".equalsIgnoreCase(receipt.getPaymentMode()))); // [cite: 166]
		} else if (!isOnlineSelected && isOfflineSelected) { // Cash only
			filteredReceipts.removeIf(receipt -> !("Cash".equalsIgnoreCase(receipt.getPaymentMode()))); // [cite: 166]
		}
		// If both or neither are selected, no payment mode filter is applied [cite: 167]

		// 4. Pass the filtered list AND the current filter state back to the listener
		if (listener != null) {
			listener.onFiltersApplied(filteredReceipts, selectedType, selectedDate, selectedMonth, selectedYear, isOnlineSelected, isOfflineSelected); // [cite: 168]
		}

		// 5. Close the popup stage
		Stage stage = (Stage) applyButton.getScene().getWindow(); // [cite: 168]
		stage.close(); // [cite: 168]
	}


	@FXML
	private void handleClearFiltersAndClose() {
		// Reset UI components to default state
		sevaTypeComboBox.setValue("ಎಲ್ಲಾ");
		datePicker.setValue(null);
		monthComboBox.setValue("All");
		yearComboBox.setValue("");
		onlineRadio.setSelected(false);
		offlineRadio.setSelected(false);

		// Reset internal state variables
		this.initialSevaType = "ಎಲ್ಲಾ";
		this.initialDate = null;
		this.initialMonth = "All";
		this.initialYear = "";
		this.initialOnline = false;
		this.initialOffline = false;

		// Notify listener that filters were cleared (sends back the full list)
		if (listener != null) {
			listener.onFiltersCleared(); // Call the new clear method
		}

		// Close the popup
		Stage stage = (Stage) applyButton.getScene().getWindow(); // Use applyButton or clearButton fx:id
		stage.close();
	}

	// --- Getters for the current UI state (optional, if needed elsewhere) ---
	public String getSelectedSevaType() { return sevaTypeComboBox.getValue(); }
	public LocalDate getSelectedDate() { return datePicker.getValue(); }
	public String getSelectedMonth() { return monthComboBox.getValue(); }
	public String getSelectedYear() { return yearComboBox.getValue(); }
	public boolean isOnlineSelected() { return onlineRadio.isSelected(); }
	public boolean isOfflineSelected() { return offlineRadio.isSelected(); }
}