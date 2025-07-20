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
		void onFiltersApplied(LocalDate date, String month, String year, boolean online, boolean offline);
		void onFiltersCleared();
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

		datePicker.setValue(initialDate);
		monthComboBox.setValue(initialMonth);
		yearComboBox.setValue(initialYear);
		onlineRadio.setSelected(initialOnline);
		offlineRadio.setSelected(initialOffline);
	}


	@FXML
	public void initialize() {
		monthComboBox.setItems(FXCollections.observableArrayList("All", "JANUARY", "FEBRUARY", "MARCH", "APRIL",
				"MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"));

		List<String> years = new ArrayList<>();
		years.add(""); // Option for no year filter
		int currentYear = LocalDate.now().getYear();
		for (int y = currentYear; y >= 2000; y--) {
			years.add(String.valueOf(y));
		}
		yearComboBox.setItems(FXCollections.observableArrayList(years));

		// Set default values
		monthComboBox.setValue("All");
		yearComboBox.setValue("");
		datePicker.setValue(null);
		onlineRadio.setSelected(false);
		offlineRadio.setSelected(false);

		// Setup radio button toggle group
		ToggleGroup paymentGroup = new ToggleGroup();
		onlineRadio.setToggleGroup(paymentGroup);
		offlineRadio.setToggleGroup(paymentGroup);
	}


	@FXML
	public void handleApplyFilters() {
		// Get current selections from UI (remove seva type)
		LocalDate selectedDate = datePicker.getValue();
		String selectedMonth = monthComboBox.getValue();
		String selectedYear = yearComboBox.getValue();
		boolean isOnlineSelected = onlineRadio.isSelected();
		boolean isOfflineSelected = offlineRadio.isSelected();

		// Pass the filter criteria back to the listener without any data filtering
		if (listener != null) {
			listener.onFiltersApplied( selectedDate, selectedMonth, selectedYear, isOnlineSelected, isOfflineSelected);
		}

		// Close the popup stage
		Stage stage = (Stage) applyButton.getScene().getWindow();
		stage.close();
	}



	@FXML
	private void handleClearFiltersAndClose() {
		// Reset UI components (remove seva type reset)
		datePicker.setValue(null);
		monthComboBox.setValue("All");
		yearComboBox.setValue("");
		onlineRadio.setSelected(false);
		offlineRadio.setSelected(false);

		// Reset internal state variables (remove seva type)
		this.initialDate = null;
		this.initialMonth = "All";
		this.initialYear = "";
		this.initialOnline = false;
		this.initialOffline = false;

		// Notify listener that filters were cleared
		if (listener != null) {
			listener.onFiltersCleared();
		}

		// Close the popup
		Stage stage = (Stage) applyButton.getScene().getWindow();
		stage.close();
	}


	// --- Getters for the current UI state (optional, if needed elsewhere) ---
	public LocalDate getSelectedDate() { return datePicker.getValue(); }
	public String getSelectedMonth() { return monthComboBox.getValue(); }
	public String getSelectedYear() { return yearComboBox.getValue(); }
	public boolean isOnlineSelected() { return onlineRadio.isSelected(); }
	public boolean isOfflineSelected() { return offlineRadio.isSelected(); }
}