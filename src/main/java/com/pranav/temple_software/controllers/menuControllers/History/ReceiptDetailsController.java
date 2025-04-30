package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.ReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;

public class ReceiptDetailsController {

	@FXML private Label receiptIdLabel;
	@FXML private Label devoteeNameLabel;
	@FXML private Label phoneNumberLabel;
	@FXML private Label sevaDateLabel;
	@FXML private Label rashiLabel;
	@FXML private Label nakshatraLabel;
	@FXML private Label totalAmountLabel;
	@FXML private TableView<SevaEntry> sevaTableView;
	@FXML private TableColumn<SevaEntry, String> sevaNameColumn;
	@FXML private TableColumn<SevaEntry, Number> priceColumn;
	@FXML private TableColumn<SevaEntry, Number> quantityColumn;
	@FXML private TableColumn<SevaEntry, Number> totalColumn;

	public void initializeDetails(ReceiptData data) {
		if (data == null) return;

		receiptIdLabel.setText("Receipt ID: " + data.getReceiptId());
		devoteeNameLabel.setText("Name: " + data.getDevoteeName());
		phoneNumberLabel.setText("Phone: " + (data.getPhoneNumber() != null ? data.getPhoneNumber() : "N/A"));
		sevaDateLabel.setText("Date: " + data.getFormattedDate());
		rashiLabel.setText("Rashi: " + (data.getRaashi() != null ? data.getRaashi() : "Not specified"));
		nakshatraLabel.setText("Nakshatra: " + (data.getNakshatra() != null ? data.getNakshatra() : "Not specified"));
		totalAmountLabel.setText(String.format("Total Amount: ₹%.2f", data.getTotalAmount()));

		sevaNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		priceColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty());
		quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());
		totalColumn.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty());

		sevaTableView.setItems(FXCollections.observableArrayList(data.getSevas()));
	}
}