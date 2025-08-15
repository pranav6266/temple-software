package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.SevaReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import com.pranav.temple_software.utils.ReceiptPrinter;
import javafx.scene.Node;

public class SevaReceiptDetailsController {

	public Button reprintButton;
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
	@FXML private Label addressText;

	private SevaReceiptData currentReceiptData;
	private final ReceiptPrinter receiptPrinter = new ReceiptPrinter(null);

	public void initializeDetails(SevaReceiptData data) {
		if (data == null) return;

		receiptIdLabel.setText("Receipt ID: " + data.getReceiptId());
		devoteeNameLabel.setText("ಭಕ್ತರ ಹೆಸರು: " + data.getDevoteeName());
		phoneNumberLabel.setText("ದೂರವಾಣಿ: " + (data.getPhoneNumber() != null ? data.getPhoneNumber() : "N/A"));
		sevaDateLabel.setText("ದಿನಾಂಕ: " + data.getFormattedDate());
		rashiLabel.setText("ಜನ್ಮ ರಾಶಿ: " + (data.getRashi() != null ? data.getRashi() : "Not specified"));
		nakshatraLabel.setText("ಜನ್ಮ ನಕ್ಷತ್: " + (data.getNakshatra() != null ? data.getNakshatra() : "Not specified"));
		addressText.setText("ವಿಳಾಸ: "+ (data.getAddress() != null ? data.getAddress() : "N/A"));
		totalAmountLabel.setText(String.format("Total Amount: ₹%.2f", data.getTotalAmount()));

		sevaNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		priceColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty());
		quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());
		totalColumn.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty());

		sevaTableView.setItems(FXCollections.observableArrayList(data.getSevas()));
	}

	@FXML
	private void handleReprint() {
		if (currentReceiptData != null) {
			Stage stage = (Stage) reprintButton.getScene().getWindow();
			// This shows the preview window
			receiptPrinter.showPrintPreview(currentReceiptData, stage, success -> {
				System.out.println("Reprint job from preview status: " + (success ? "Success" : "Failed/Cancelled"));
			}, () -> {
				System.out.println("Reprint preview was closed without action.");
			});
		}
	}
	@FXML
	public void handleClose() {
		Stage stage = (Stage) receiptIdLabel.getScene().getWindow();
		if (stage != null) {
			stage.close();
		}
	}
}