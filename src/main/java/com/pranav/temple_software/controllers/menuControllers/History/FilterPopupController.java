package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.Donations;
import com.pranav.temple_software.models.Seva;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.DonationRepository;
import com.pranav.temple_software.repositories.OtherSevaRepository;
import com.pranav.temple_software.repositories.SevaRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FilterPopupController {

	@FXML private ComboBox<String> typeComboBox;
	@FXML private ComboBox<String> itemComboBox;
	@FXML private DatePicker fromDatePicker;
	@FXML private DatePicker toDatePicker;
	@FXML private ComboBox<String> monthComboBox;
	@FXML private ComboBox<String> yearComboBox;
	@FXML private ComboBox<String> paymentModeComboBox;

	private FilterApplyHandler filterApplyHandler;

	// Interface for handling filter apply action
	public interface FilterApplyHandler {
		void handle();
	}

	// Setter for the filter apply handler
	public void setFilterApplyHandler(FilterApplyHandler handler) {
		this.filterApplyHandler = handler;
	}

	@FXML
	public void initialize() {
		setupComboBoxes();
		setupDefaultValues();
	}

	private void setupComboBoxes() {
		// Type ComboBox
		typeComboBox.setItems(FXCollections.observableArrayList(
				"ಎಲ್ಲಾ", "ಸೇವೆ", "ಇತರೆ ಸೇವೆ", "ದೇಣಿಗೆ"
		));
		typeComboBox.setValue("ಎಲ್ಲಾ");

		// Item ComboBox (initially with default option)
		itemComboBox.setItems(FXCollections.observableArrayList("ಎಲ್ಲಾ"));
		itemComboBox.setValue("ಎಲ್ಲಾ");

		// Payment Mode ComboBox
		paymentModeComboBox.setItems(FXCollections.observableArrayList(
				"All", "Cash", "Online"
		));
		paymentModeComboBox.setValue("All");

		// Month ComboBox
		monthComboBox.setItems(FXCollections.observableArrayList(
				"All", "ಜನವರಿ", "ಫೆಬ್ರುವರಿ", "ಮಾರ್ಚ್", "ಏಪ್ರಿಲ್", "ಮೇ", "ಜೂನ್",
				"ಜುಲೈ", "ಆಗಸ್ಟ್", "ಸೆಪ್ಟೆಂಬರ್", "ಅಕ್ಟೋಬರ್", "ನವೆಂಬರ್", "ಡಿಸೆಂಬರ್"
		));
		monthComboBox.setValue("All");

		// Year ComboBox
		List<String> years = new ArrayList<>();
		years.add("");
		int currentYear = LocalDate.now().getYear();
		for (int y = currentYear; y >= 2000; y--) {
			years.add(String.valueOf(y));
		}
		yearComboBox.setItems(FXCollections.observableArrayList(years));
		yearComboBox.setValue("");
	}

	private void setupDefaultValues() {
		// Set default values
		fromDatePicker.setValue(null);
		toDatePicker.setValue(null);
	}

	// Method to initialize with current filter values from main dashboard
	public void initializeWithCurrentFilters(String typeValue, String itemValue,
	                                         LocalDate fromDate, LocalDate toDate,
	                                         String monthValue, String yearValue,
	                                         String paymentModeValue) {
		if (typeValue != null) {
			typeComboBox.setValue(typeValue);
		}
		if (itemValue != null) {
			itemComboBox.setValue(itemValue);
		}
		fromDatePicker.setValue(fromDate);
		toDatePicker.setValue(toDate);
		if(validateFilters()) {
			if (monthValue != null) {
				monthComboBox.setValue(monthValue);
			}
			if (yearValue != null) {
				yearComboBox.setValue(yearValue);
			}
			if (paymentModeValue != null) {
				paymentModeComboBox.setValue(paymentModeValue);
			}
		}
	}

	@FXML
	public void applyFilters() {
		if (filterApplyHandler != null) {
			filterApplyHandler.handle();
		}
	}

	@FXML
	public void clearFilters() {
		// Reset all filter values to defaults
		typeComboBox.setValue("ಎಲ್ಲಾ");
		itemComboBox.setValue("ಎಲ್ಲಾ");
		fromDatePicker.setValue(null);
		toDatePicker.setValue(null);
		monthComboBox.setValue("All");
		yearComboBox.setValue("");
		paymentModeComboBox.setValue("All");
	}

	@FXML
	public void closeWindow() {
		Stage stage = (Stage) typeComboBox.getScene().getWindow();
		if (stage != null) {
			stage.close();
		}
	}

	// Getters for filter values
	public String getTypeValue() {
		return typeComboBox.getValue();
	}

	public String getItemValue() {
		return itemComboBox.getValue();
	}

	public LocalDate getFromDateValue() {
		return fromDatePicker.getValue();
	}

	public LocalDate getToDateValue() {
		return toDatePicker.getValue();
	}

	public String getMonthValue() {
		return monthComboBox.getValue();
	}

	public String getYearValue() {
		return yearComboBox.getValue();
	}

	public String getPaymentModeValue() {
		return paymentModeComboBox.getValue();
	}

	// Method to handle type selection change and update item combo box accordingly
	@FXML
	public void handleTypeSelectionChange() {
		String selectedType = typeComboBox.getValue();

		// Clear current item selection first
		itemComboBox.setValue(null);
		itemComboBox.getSelectionModel().clearSelection();

		// Update the items based on selected type
		updateItemComboBoxBasedOnType(selectedType);

		// Set default selection to "All" after updating items
		Platform.runLater(() -> {
			if (!itemComboBox.getItems().isEmpty()) {
				itemComboBox.setValue("ಎಲ್ಲಾ"); // Set to "All" option
			}
		});
	}

	private void updateItemComboBoxBasedOnType(String selectedType) {
		List<String> items = new ArrayList<>();
		items.add("ಎಲ್ಲಾ");

		switch (selectedType) {
			case "ಸೇವೆ":
				// Get all regular sevas from SevaRepository
				List<Seva> sevaEntries = SevaRepository.getInstance().getAllSevas();
				items.addAll(sevaEntries.stream()
						.map(Seva::getName)
						.toList());
				break;

			case "ಇತರೆ ಸೇವೆ":
				// Get all other sevas from OtherSevaRepository
				List<SevaEntry> otherSevaEntries = OtherSevaRepository.getAllOtherSevas();
				items.addAll(otherSevaEntries.stream()
						.map(SevaEntry::getName)
						.toList());
				break;

			case "ದೇಣಿಗೆ":
				// Get all donations from DonationRepository
				List<Donations> donationEntries = DonationRepository.getInstance().getAllDonations();
				items.addAll(donationEntries.stream()
						.map(Donations::getName)
						.toList());
				break;

			case "ಎಲ್ಲಾ":
			default:
				// For "All" option, keep only the default "ಎಲ್ಲಾ" option
				break;
		}

		// Update the combo box items
		itemComboBox.setItems(FXCollections.observableArrayList(items));
		itemComboBox.setValue("ಎಲ್ಲಾ");
	}


	// Validation method to check if filters are valid
	public boolean validateFilters() {
		LocalDate fromDate = fromDatePicker.getValue();
		LocalDate toDate = toDatePicker.getValue();

		if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
			showValidationAlert("Invalid Date Range",
					"From date cannot be after to date.");
			return false;
		}

		return true;
	}

	private void showValidationAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
