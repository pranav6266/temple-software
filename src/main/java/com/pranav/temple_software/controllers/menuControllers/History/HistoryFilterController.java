package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.HistoryFilterCriteria;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HistoryFilterController {

	@FXML private TextField devoteeNameField;
	@FXML private TextField phoneField;
	@FXML private TextField receiptIdField;

	// NEW FIELDS replacing range pickers
	@FXML private DatePicker specificDatePicker;
	@FXML private ComboBox<String> monthComboBox;
	@FXML private ComboBox<String> yearComboBox;
	@FXML private ComboBox<String> paymentModeComboBox;

	private Consumer<HistoryFilterCriteria> onApplyFilter;
	private HistoryFilterCriteria currentCriteria;

	// Map for Kannada Month conversion
	private static final Map<String, Integer> KANNADA_MONTHS_MAP = Map.ofEntries(
			Map.entry("ಜನವರಿ", 1), Map.entry("ಫೆಬ್ರುವರಿ", 2), Map.entry("ಮಾರ್ಚ್", 3),
			Map.entry("ಏಪ್ರಿಲ್", 4), Map.entry("ಮೇ", 5), Map.entry("ಜೂನ್", 6),
			Map.entry("ಜುಲೈ", 7), Map.entry("ಆಗಸ್ಟ್", 8), Map.entry("ಸೆಪ್ಟೆಂಬರ್", 9),
			Map.entry("ಅಕ್ಟೋಬರ್", 10), Map.entry("ನವೆಂಬರ್", 11), Map.entry("ಡಿಸೆಂಬರ್", 12)
	);

	public void initialize(HistoryFilterCriteria criteria, Consumer<HistoryFilterCriteria> onApplyFilter) {
		this.currentCriteria = criteria;
		this.onApplyFilter = onApplyFilter;

		devoteeNameField.setText(criteria.getDevoteeName());
		phoneField.setText(criteria.getPhoneNumber());
		receiptIdField.setText(criteria.getReceiptId());

		// Initialize ComboBoxes
		setupDateComboBoxes();

		// Restore previous date state (basic logic: if start == end, it's specific date)
		if (criteria.getFromDate() != null && criteria.getFromDate().equals(criteria.getToDate())) {
			specificDatePicker.setValue(criteria.getFromDate());
		}
		// (Advanced restoration of month/year state is skipped for simplicity, defaulting to empty)

		// Initialize Payment Mode Combo
		paymentModeComboBox.setItems(FXCollections.observableArrayList("All", "Cash", "Online"));
		if (criteria.getPaymentMode() != null) {
			paymentModeComboBox.setValue(criteria.getPaymentMode());
		} else {
			paymentModeComboBox.getSelectionModel().selectFirst();
		}
	}

	private void setupDateComboBoxes() {
		// Month ComboBox
		monthComboBox.setItems(FXCollections.observableArrayList(
				"All", "ಜನವರಿ", "ಫೆಬ್ರುವರಿ", "ಮಾರ್ಚ್", "ಏಪ್ರಿಲ್", "ಮೇ", "ಜೂನ್",
				"ಜುಲೈ", "ಆಗಸ್ಟ್", "ಸೆಪ್ಟೆಂಬರ್", "ಅಕ್ಟೋಬರ್", "ನವೆಂಬರ್", "ಡಿಸೆಂಬರ್"
		));
		monthComboBox.setValue("All");

		// Year ComboBox
		List<String> years = new ArrayList<>();
		years.add(""); // Empty option for All Years
		int currentYear = LocalDate.now().getYear();
		for (int y = currentYear; y >= 2000; y--) {
			years.add(String.valueOf(y));
		}
		yearComboBox.setItems(FXCollections.observableArrayList(years));
		yearComboBox.setValue("");
	}

	@FXML
	void applyFilters() {
		currentCriteria.setDevoteeName(getTextFieldValue(devoteeNameField));
		currentCriteria.setPhoneNumber(getTextFieldValue(phoneField));
		currentCriteria.setReceiptId(getTextFieldValue(receiptIdField));
		currentCriteria.setPaymentMode(paymentModeComboBox.getValue());

		// --- NEW DATE LOGIC ---
		LocalDate fromDate = null;
		LocalDate toDate = null;

		String monthValue = monthComboBox.getValue();
		String yearValue = yearComboBox.getValue();
		LocalDate specificDate = specificDatePicker.getValue();

		// Priority 1: Month & Year Selection
		if (monthValue != null && !monthValue.equals("All")) {
			int monthNum = KANNADA_MONTHS_MAP.getOrDefault(monthValue, -1);
			if (monthNum != -1) {
				// If year is not selected, default to current year
				int year = (yearValue != null && !yearValue.isEmpty()) ? Integer.parseInt(yearValue) : LocalDate.now().getYear();
				fromDate = LocalDate.of(year, monthNum, 1);
				toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
			}
		}
		// Priority 2: Year Only Selection
		else if (yearValue != null && !yearValue.isEmpty()) {
			int year = Integer.parseInt(yearValue);
			fromDate = LocalDate.of(year, Month.JANUARY, 1);
			toDate = LocalDate.of(year, Month.DECEMBER, 31);
		}
		// Priority 3: Specific Date Picker
		else if (specificDate != null) {
			fromDate = specificDate;
			toDate = specificDate;
		}

		currentCriteria.setFromDate(fromDate);
		currentCriteria.setToDate(toDate);

		if (onApplyFilter != null) {
			onApplyFilter.accept(currentCriteria);
		}
		closeWindow();
	}

	@FXML
	void clearFilters() {
		devoteeNameField.clear();
		phoneField.clear();
		receiptIdField.clear();

		// Reset Date Controls
		specificDatePicker.setValue(null);
		monthComboBox.setValue("All");
		yearComboBox.setValue("");

		paymentModeComboBox.getSelectionModel().selectFirst();

		// Apply the cleared filters
		applyFilters();
	}

	private String getTextFieldValue(TextField field) {
		return (field.getText() == null || field.getText().trim().isEmpty()) ? null : field.getText().trim();
	}

	private void closeWindow() {
		((Stage) devoteeNameField.getScene().getWindow()).close();
	}
}