// HistoryController.java
package com.pranav.temple_software.controllers;

import com.pranav.temple_software.models.ReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.ReceiptRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class HistoryController {
	@FXML
	public TableColumn<ReceiptData, String> otherSevaColumn;
	@FXML
	public TableColumn<ReceiptData, String> sevaColumn;
	@FXML
	public TableColumn<ReceiptData, Double> totalAmountCoulum;
	public TableColumn<FXML, Button> details;
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

		setDonationAmountColumn();
		setOtherSevaColumn();
		setSevaColumn();
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
