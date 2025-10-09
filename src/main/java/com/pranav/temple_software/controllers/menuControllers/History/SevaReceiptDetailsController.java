package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.SevaReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.pranav.temple_software.utils.ReceiptPrinter;

public class SevaReceiptDetailsController {

	@FXML private Label receiptIdLabel;
	@FXML private Label devoteeNameLabel;
	@FXML private Label phoneNumberLabel;
	@FXML private Label panNumberLabel; // ADDED
	@FXML private Label rashiLabel;
	@FXML private Label nakshatraLabel;
	@FXML private Text addressText;
	@FXML private Label paymentModeLabel;
	@FXML private Label sevaDateLabel;
	@FXML private Label totalAmountLabel;

	@FXML private TableView<SevaEntry> sevaTableView;
	@FXML private TableColumn<SevaEntry, String> sevaNameColumn;
	@FXML private TableColumn<SevaEntry, Number> priceColumn;
	@FXML private TableColumn<SevaEntry, Number> quantityColumn;
	@FXML private TableColumn<SevaEntry, Number> totalColumn;

	@FXML private Button reprintButton;

	private SevaReceiptData currentReceiptData;
	private final ReceiptPrinter receiptPrinter = new ReceiptPrinter();

	public void initializeDetails(SevaReceiptData data) {
		if (data == null) return;
		this.currentReceiptData = data;

		receiptIdLabel.setText("ರಶೀದಿ ಸಂಖ್ಯೆ: " + data.getReceiptId());

		// Devotee Details
		devoteeNameLabel.setText("ಭಕ್ತರ ಹೆಸರು: " + (data.getDevoteeName() != null && !data.getDevoteeName().isEmpty() ? data.getDevoteeName() : "---"));
		phoneNumberLabel.setText("ದೂರವಾಣಿ: " + (data.getPhoneNumber() != null && !data.getPhoneNumber().isEmpty() ? data.getPhoneNumber() : "---"));
		panNumberLabel.setText("PAN ಸಂಖ್ಯೆ: " + (data.getPanNumber() != null && !data.getPanNumber().isEmpty() ? data.getPanNumber() : "---"));
		rashiLabel.setText("ಜನ್ಮ ರಾಶಿ: " + (data.getRashi() != null && !data.getRashi().isEmpty() ? data.getRashi() : "---"));
		nakshatraLabel.setText("ಜನ್ಮ ನಕ್ಷತ್ರ: " + (data.getNakshatra() != null && !data.getNakshatra().isEmpty() ? data.getNakshatra() : "---"));
		addressText.setText("ವಿಳಾಸ: "+(!data.getAddress().isEmpty() ? data.getAddress() : "---"));
		paymentModeLabel.setText("ಪಾವತಿ ವಿಧಾನ: " + (data.getPaymentMode() != null ? data.getPaymentMode() : "---"));
		// Seva Details
		sevaDateLabel.setText("ದಿನಾಂಕ: " + data.getFormattedDate());
		totalAmountLabel.setText("ಒಟ್ಟು ಮೊತ್ತ: ₹" + String.format("%.2f", data.getTotalAmount()));

		// Table Setup
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
			receiptPrinter.showPrintPreview(currentReceiptData, stage, success -> System.out.println("Reprint job from preview status: " + (success ? "Success" : "Failed/Cancelled")), () -> System.out.println("Reprint preview was closed without action."));
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