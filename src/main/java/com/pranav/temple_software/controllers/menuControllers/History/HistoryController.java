// HistoryController.java
package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.DonationReceiptData;
import com.pranav.temple_software.models.ReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.DonationReceiptRepository;
import com.pranav.temple_software.repositories.OtherSevaRepository;
import com.pranav.temple_software.repositories.ReceiptRepository;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class HistoryController {
	@FXML public Button applyFilterButton;
	@FXML public Button resetFilterButton;
	public TableColumn<ReceiptData, String> paymentMode;
	@FXML private Button filterButton;
	@FXML private AnchorPane filterPanel;
	@FXML private ComboBox<String> sevaTypeComboBox;
	@FXML private DatePicker datePicker;
	@FXML private ComboBox<String> monthComboBox;
	@FXML private ComboBox<String> yearComboBox;
	@FXML private RadioButton onlineRadio;
	@FXML private RadioButton offlineRadio;
	@FXML private TableView<ReceiptData> historyTable;
	@FXML
	public TableColumn<ReceiptData, String> otherSevaColumn;
	@FXML
	public TableColumn<ReceiptData, String> sevaColumn;
	@FXML
	public TableColumn<ReceiptData, Double> totalAmountColumn;
	public TableColumn<ReceiptData, Void> detailsColumn;
	@FXML
	private TableColumn<ReceiptData, String> donationColumn;
	@FXML
	private TableColumn<ReceiptData, Integer> receiptIdColumn;
	@FXML
	private TableColumn<ReceiptData, String> devoteeNameColumn;
	@FXML
	private TableColumn<ReceiptData, String> sevaDateColumn;
	@FXML
	private TableColumn<ReceiptData, String> isDonationColumn;
	@FXML private Button toggleViewButton;
	@FXML private Label currentViewLabel;
	// Add donation table column fields
	@FXML public TableView<DonationReceiptData> donationHistoryTable;
	@FXML public TableColumn<DonationReceiptData, Integer> donationReceiptIdColumn;
	@FXML public TableColumn<DonationReceiptData, String> donationDevoteeNameColumn;
	@FXML public TableColumn<DonationReceiptData, String> donationDateColumn;
	@FXML public TableColumn<DonationReceiptData, String> donationNameColumn;
	@FXML public TableColumn<DonationReceiptData, Double> donationAmountColumn;
	@FXML public TableColumn<DonationReceiptData, String> donationPaymentModeColumn;
	@FXML public TableColumn<DonationReceiptData, Void> donationDetailsColumn;
	private boolean isShowingDonations = false;

	private final ReceiptRepository receiptRepository = new ReceiptRepository();
	private final DonationReceiptRepository donationReceiptRepository = new DonationReceiptRepository();
	private String savedSevaType = "All";
	private LocalDate savedDate = null;
	private String savedMonth = "All";
	private String savedYear = "";
	private boolean savedOnline = false;
	private boolean savedOffline = false;


	@FXML
	public void initialize() {
		loadHistory();
		// Initialize the toggle button and label
		toggleViewButton.setText("ದೇಣಿಗೆ ರಶೀದಿಗಳನ್ನು ನೋಡಿ"); // "View Donation Receipts"
		currentViewLabel.setText("ಸೇವಾ ರಶೀದಿ ಇತಿಹಾಸ"); // "Seva Receipt History"

		// Setup donation table columns if using separate table
		setupDonationTableColumns();

		// Existing setup methods...
		isDonationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDonationStatus()));
		isDonationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDonationStatus()));
		receiptIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getReceiptId()).asObject());
		devoteeNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDevoteeName()));
		sevaDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
		totalAmountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());

		setupDetailsColumn();
		setDonationAmountColumn();
		setOtherSevaColumn();
		setSevaColumn();
		setPaymentModeColumn();
	}

	// Add this method to switch between views
	@FXML
	public void handleToggleView() {
		isShowingDonations = !isShowingDonations;

		if (isShowingDonations) {
			loadDonationHistory();
			toggleViewButton.setText("ಸೇವಾ ರಶೀದಿಗಳನ್ನು ನೋಡಿ"); // "View Seva Receipts"
			currentViewLabel.setText("ದೇಣಿಗೆ ರಶೀದಿ ಇತಿಹಾಸ"); // "Donation Receipt History"
		} else {
			loadHistory();
			toggleViewButton.setText("ದೇಣಿಗೆ ರಶೀದಿಗಳನ್ನು ನೋಡಿ"); // "View Donation Receipts"
			currentViewLabel.setText("ಸೇವಾ ರಶೀದಿ ಇತಿಹಾಸ"); // "Seva Receipt History"
		}
	}

	// Add this method to load donation history
	private void loadDonationHistory() {
		List<DonationReceiptData> donationReceipts = donationReceiptRepository.getAllDonationReceipts();
		// Convert DonationReceiptData to a format compatible with the table
		ObservableList<DonationReceiptData> donationList = FXCollections.observableArrayList(donationReceipts);
		// You'll need to create a separate TableView for donations or modify the existing one
		donationHistoryTable.setItems(donationList);
	}

	private void setupDetailsColumn() {
		detailsColumn.setCellFactory(param -> new TableCell<ReceiptData, Void>() {
			private final Button viewButton = new Button("ವಿವರ ನೋಡಿ");

			{
				viewButton.setOnAction(event -> {
					ReceiptData selectedReceipt = getTableView().getItems().get(getIndex());
					showReceiptDetails(selectedReceipt);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					setGraphic(viewButton);
					setAlignment(Pos.CENTER);
				}
			}
		});
	}


	private void setupDonationDetailsColumn() {
		donationDetailsColumn.setCellFactory(param -> new TableCell<>() {
			private final Button viewButton = new Button("ವಿವರ ನೋಡಿ"); // "View Details"

			{
				viewButton.setOnAction(event -> {
					DonationReceiptData selectedDonation = getTableView().getItems().get(getIndex());
					showDonationDetails(selectedDonation);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					setGraphic(viewButton);
					setAlignment(Pos.CENTER);
				}
			}
		});
	}

	private void showDonationDetails(DonationReceiptData donationData) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/History/DonationDetailsView.fxml"));
			Stage detailsStage = new Stage();
			detailsStage.setTitle("ದೇಣಿಗೆ ರಶೀದಿ ವಿವರಗಳು"); // "Donation Receipt Details"
			detailsStage.initModality(Modality.WINDOW_MODAL);
			detailsStage.initOwner(donationHistoryTable.getScene().getWindow());

			Scene scene = new Scene(loader.load());
			detailsStage.setScene(scene);

			DonationDetailsController detailsController = loader.getController();
			detailsController.initializeDonationDetails(donationData);

			detailsStage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();
			showAlert("Error", "Could not load donation details view.");
		}
	}
	// Add method to set up donation table columns
	private void setupDonationTableColumns() {
		// You can either create a separate TableView for donations or modify the existing one
		// Here's an example of setting up columns for donation-specific data
		if (donationHistoryTable != null) {
			donationReceiptIdColumn.setCellValueFactory(cellData ->
					new SimpleIntegerProperty(cellData.getValue().getDonationReceiptId()).asObject());
			donationDevoteeNameColumn.setCellValueFactory(cellData ->
					new SimpleStringProperty(cellData.getValue().getDevoteeName()));
			donationDateColumn.setCellValueFactory(cellData ->
					new SimpleStringProperty(cellData.getValue().getFormattedDate()));
			donationAmountColumn.setCellValueFactory(cellData ->
					new SimpleDoubleProperty(cellData.getValue().getDonationAmount()).asObject());
			donationNameColumn.setCellValueFactory(cellData ->
					new SimpleStringProperty(cellData.getValue().getDonationName()));
			donationPaymentModeColumn.setCellValueFactory(cellData ->
					new SimpleStringProperty(cellData.getValue().getPaymentMode()));
		}
	}
	// *** ADD THIS: New method to load and show the details window ***
	private void showReceiptDetails(ReceiptData receiptData) {
		try {
			// Load the FXML file for the details view (You'll need to create this)
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/History/ReceiptDetailsView.fxml")); // Adjust path if needed
			Stage detailsStage = new Stage();
			detailsStage.setTitle("ರಶೀದಿ ವಿವರಗಳು"); // "Receipt Details"
			detailsStage.initModality(Modality.WINDOW_MODAL); // Block interaction with the main window
			// Set the owner window (optional but good practice)
			 detailsStage.initOwner(historyTable.getScene().getWindow());

			// Load the scene
			Scene scene = new Scene(loader.load());
			detailsStage.setScene(scene);

			// Get the controller for the details view (You'll need to create this controller)
			ReceiptDetailsController detailsController = loader.getController();
			// Pass the selected receipt data to the details controller
			detailsController.initializeDetails(receiptData);

			// Show the details window and wait for it to be closed if needed
			detailsStage.showAndWait();

		} catch (IOException e) {
			// Handle errors loading the FXML or controller
			e.printStackTrace(); // Log the error
			// Optionally show an alert to the user
			showAlert("Error", "Could not load receipt details view.");
		} catch (NullPointerException e) {
			// Handle potential NullPointerException if the FXML or controller isn't found correctly
			e.printStackTrace();
			showAlert("Error", "Could not find the details view FXML or Controller. Check the path.");
		}
	}

	// *** ADD THIS: Helper method for showing alerts (if you don't already have one) ***
	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR); // Or WARNING/INFORMATION
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}


	private void setDonationAmountColumn(){
		// Setup for the new Donation Amount column
		donationColumn.setCellValueFactory(cellData -> {
			ReceiptData receipt = cellData.getValue();
			ObservableList<SevaEntry> sevas = receipt.getSevas(); // Get the list of sevas/donations [cite: 42]

			// Search for a donation entry
			for (SevaEntry entry : sevas) {
				if (entry.getName() != null && entry.getName().startsWith("ದೇಣಿಗೆ ")) { // Check if it's a donation [cite: 118]
					// Format the amount as needed (e.g., currency)
					return new SimpleStringProperty(String.format("%.2f", entry.getAmount())); // Return amount [cite: 50]
				}
			}

			// If no donation entry is found
			return new SimpleStringProperty("N/A"); // Return "N/A"
		});

	}

	private void setOtherSevaColumn() {
		otherSevaColumn.setCellValueFactory(cellData -> {
			ReceiptData receipt = cellData.getValue();
			ObservableList<SevaEntry> sevas = receipt.getSevas();

			// ✅ **Fetch actual Other Seva list from repository**
			List<String> validOtherSevas = OtherSevaRepository.getAllOtherSevas().stream()
					.map(SevaEntry::getName)
					.toList();

			double otherSevaAmount = sevas.stream()
					.filter(entry -> validOtherSevas.contains(entry.getName()))
					.mapToDouble(SevaEntry::getAmount)
					.sum();

			return new SimpleStringProperty(otherSevaAmount > 0 ? String.format("₹%.2f", otherSevaAmount) : "N/A");
		});
	}

	private void  setSevaColumn(){
		sevaColumn.setCellValueFactory(cellData -> {
			ReceiptData receipt = cellData.getValue();
			ObservableList<SevaEntry> sevas = receipt.getSevas(); // Get the list of sevas/donations [cite: 42]
			double amount = 0;
			boolean validEntryFound = false;
			// Search for a donation entry
			for (SevaEntry entry : sevas) {
				if (entry.getName() != null && !entry.getName().startsWith("ಇತರೆ ") &&
						!entry.getName().startsWith("ದೇಣಿಗೆ ") ) {// Check if it's a donation [cite: 118]
					amount += entry.getAmount();
					validEntryFound = true;
				}
			}
			if(validEntryFound){
				return new SimpleStringProperty(String.format("%.2f",amount)); // Return amount [cite: 50]
			}else {
				return new SimpleStringProperty("N/A"); // Return "N/A"
			}
		});
	}

	private void setPaymentModeColumn() {
		paymentMode.setCellValueFactory(cellData -> {
			String mode = cellData.getValue().getPaymentMode();
			return new SimpleStringProperty(mode != null ? mode : "N/A");
		});
	}

	private void loadHistory() {
		historyTable.setItems(FXCollections.observableArrayList(receiptRepository.getAllReceipts()));
	}




	@FXML
	public void openFilterPopup() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/History/FilterPopup.fxml")); // [cite: 198]
			AnchorPane popupContent = loader.load(); // [cite: 198]
			FilterPopupController controller = getFilterPopupController(loader);
			// --- MODIFICATION END ---


			Stage popupStage = new Stage();
			popupStage.setScene(new Scene(popupContent)); // [cite: 199]
			popupStage.setTitle("Apply Filters"); // [cite: 199]
			popupStage.initModality(Modality.APPLICATION_MODAL); // [cite: 199]
			// Set the owner if needed: popupStage.initOwner(historyTable.getScene().getWindow());


			// --- MODIFICATION: Call setInitialFilterState BEFORE showing the popup ---
			controller.setInitialFilterState(
					savedSevaType,
					savedDate,
					savedMonth,
					savedYear,
					savedOnline,
					savedOffline
			); // [cite: 199]


			popupStage.showAndWait(); // [cite: 199]

		} catch (IOException e) {
			e.printStackTrace(); // [cite: 201]
			// Show an error alert to the user
			showAlert("Error", "Could not open the filter popup."); // [cite: 190]
		}
	}

	private FilterPopupController getFilterPopupController(FXMLLoader loader) {
		FilterPopupController controller = loader.getController(); // [cite: 199]

		// --- MODIFICATION START: Implement the updated FilterListener ---
		controller.setFilterListener(new FilterPopupController.FilterListener() {
			@Override
			public void onFiltersApplied(List<ReceiptData> filteredList, String sevaType, LocalDate date, String month, String year, boolean online, boolean offline) {
				// Update the table view
				historyTable.setItems(FXCollections.observableArrayList(filteredList)); // [cite: 200]

				// Save the applied filter state
				savedSevaType = sevaType;
				savedDate = date;
				savedMonth = month;
				savedYear = year;
				savedOnline = online;
				savedOffline = offline;
			}

			@Override
			public void onFiltersCleared() {
				// Reset saved state
				savedSevaType = "ಎಲ್ಲಾ";
				savedDate = null;
				savedMonth = "All";
				savedYear = "";
				savedOnline = false;
				savedOffline = false;

				// Reload the full history list
				loadHistory(); // Assumes loadHistory() re-fetches all receipts
			}
		});
		return controller;
	}

}



