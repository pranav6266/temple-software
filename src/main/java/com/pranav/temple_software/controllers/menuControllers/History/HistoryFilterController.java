package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.HistoryFilterCriteria;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class HistoryFilterController {

	@FXML private TextField devoteeNameField;
	@FXML private TextField phoneField;
	@FXML private TextField receiptIdField;
	@FXML private DatePicker fromDatePicker;
	@FXML private DatePicker toDatePicker;

	private Consumer<HistoryFilterCriteria> onApplyFilter;
	private HistoryFilterCriteria currentCriteria;

	public void initialize(HistoryFilterCriteria criteria, Consumer<HistoryFilterCriteria> onApplyFilter) {
		this.currentCriteria = criteria;
		this.onApplyFilter = onApplyFilter;

		devoteeNameField.setText(criteria.getDevoteeName());
		phoneField.setText(criteria.getPhoneNumber());
		receiptIdField.setText(criteria.getReceiptId());
		fromDatePicker.setValue(criteria.getFromDate());
		toDatePicker.setValue(criteria.getToDate());
	}

	@FXML
	void applyFilters() {
		currentCriteria.setDevoteeName(getTextFieldValue(devoteeNameField));
		currentCriteria.setPhoneNumber(getTextFieldValue(phoneField));
		currentCriteria.setReceiptId(getTextFieldValue(receiptIdField));
		currentCriteria.setFromDate(fromDatePicker.getValue());
		currentCriteria.setToDate(toDatePicker.getValue());

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
		fromDatePicker.setValue(null);
		toDatePicker.setValue(null);

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