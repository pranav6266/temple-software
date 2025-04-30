// HistoryController.java
package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.ReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.ReceiptRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class HistoryController {
	@FXML
	private HBox historyHeader;
	@FXML
	private VBox historyContainer;
	@FXML
	public TableColumn<ReceiptData, String> otherSevaColumn;
	@FXML
	public TableColumn<ReceiptData, String> sevaColumn;
	@FXML
	public TableColumn<ReceiptData, Double> totalAmountCoulum;
	public TableColumn<ReceiptData, Void> detailsColumn;
	@FXML
	private TableView<ReceiptData> historyTable;
	@FXML
	private TableColumn<ReceiptData, String> donationColumn;
	@FXML
	private TableColumn<ReceiptData, Integer> receiptIdColumn;
	@FXML
	private TableColumn<ReceiptData, String> devoteeNameColumn;
	@FXML
	private TableColumn<ReceiptData, String> sevaDateColumn;


	private final ReceiptRepository receiptRepository = new ReceiptRepository();


	@FXML
	public void initialize() {
		loadHistory();
		receiptIdColumn.setCellValueFactory(cellData ->
				new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getReceiptId()).asObject()
		); // Or SimpleStringProperty if you want it as text

		devoteeNameColumn.setCellValueFactory(cellData ->
				new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDevoteeName())
		);

		sevaDateColumn.setCellValueFactory(cellData ->
				// Use the getFormattedDate() method from ReceiptData
				new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDate())
		);

		totalAmountCoulum.setCellValueFactory(cellData ->
				new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject()
		);
		
		setupDetailsColumn();
		setDonationAmountColumn();
		setOtherSevaColumn();
		setSevaColumn();
	}

	private void setupDetailsColumn() {
		// Define how each cell in the 'detailsColumn' should be rendered
		detailsColumn.setCellFactory(param -> new TableCell<>() {
			// Create a button for each row
			private final Button viewButton = new Button("ವಿವರ ನೋಡಿ"); // "View Details"

			{
				// Define what happens when the button is clicked
				viewButton.setOnAction(event -> {
					// Get the ReceiptData object for the row where the button was clicked
					ReceiptData selectedReceipt = getTableView().getItems().get(getIndex());
					// Call the method to show the details window
					showReceiptDetails(selectedReceipt);
				});
			}

			// This method updates the cell's content
			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				// If the cell is not empty, display the button
				if (empty) {
					setGraphic(null); // Don't show anything in empty rows
				} else {
					setGraphic(viewButton); // Show the button
					setAlignment(Pos.CENTER); // Center the button in the cell
				}
			}
		});
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

	private void setOtherSevaColumn(){
		otherSevaColumn.setCellValueFactory(cellData -> {
			ReceiptData receipt = cellData.getValue();
			ObservableList<SevaEntry> sevas = receipt.getSevas(); // Get the list of sevas/donations [cite: 42]

			// Search for a donation entry
			for (SevaEntry entry : sevas) {
				if (entry.getName() != null && entry.getName().startsWith("ಇತರೆ ")) { // Check if it's a donation [cite: 118]
					// Format the amount as needed (e.g., currency)
					return new SimpleStringProperty(String.format("%.2f", entry.getAmount())); // Return amount [cite: 50]
				}
			}

			// If no donation entry is found
			return new SimpleStringProperty("N/A"); // Return "N/A"
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
	private void loadHistory() {
		List<ReceiptData> receipts = receiptRepository.getAllReceipts();
		historyTable.setItems(FXCollections.observableArrayList(receipts));

	}

}
