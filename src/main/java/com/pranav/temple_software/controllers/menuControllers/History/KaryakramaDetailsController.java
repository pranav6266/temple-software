package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.KaryakramaReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.utils.ReceiptPrinter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class KaryakramaDetailsController {
	@FXML private Label receiptIdLabel;
	@FXML private Label devoteeNameLabel;
	@FXML private Label phoneNumberLabel;
	@FXML private Label panNumberLabel;
	@FXML private Label rashiLabel;
	@FXML private Label nakshatraLabel;
	@FXML private Text addressText;
	@FXML private Label karyakramaNameLabel; // <-- NEW FIELD
	@FXML private Label receiptDateLabel;
	@FXML private Label totalAmountLabel;
	@FXML private TableView<SevaEntry> sevaTableView;
	@FXML private TableColumn<SevaEntry, String> sevaNameColumn;
	@FXML private TableColumn<SevaEntry, Number> priceColumn;
	@FXML private TableColumn<SevaEntry, Number> quantityColumn;
	@FXML private TableColumn<SevaEntry, Number> totalColumn;
	@FXML private Button reprintButton;
	@FXML private Label paymentModeLabel;

	private KaryakramaReceiptData currentReceiptData;
	private final ReceiptPrinter receiptPrinter = new ReceiptPrinter();

	public void initializeDetails(KaryakramaReceiptData data) {
		if (data == null) return;
		this.currentReceiptData = data;

		receiptIdLabel.setText("ರಶೀದಿ ಸಂಖ್ಯೆ: " + data.getReceiptId());
		devoteeNameLabel.setText("ಭಕ್ತರ ಹೆಸರು: " + data.getDevoteeName());
		phoneNumberLabel.setText("ದೂರವಾಣಿ: " + data.getPhoneNumber());
		panNumberLabel.setText("PAN ಸಂಖ್ಯೆ: " + data.getPanNumber());
		rashiLabel.setText("ಜನ್ಮ ರಾಶಿ: " + data.getRashi());
		nakshatraLabel.setText("ಜನ್ಮ ನಕ್ಷತ್ರ: " + data.getNakshatra());
		addressText.setText("ವಿಳಾಸ: " + (data.getAddress() != null && !data.getAddress().isEmpty() ? data.getAddress() : "---"));
		paymentModeLabel.setText("ಪಾವತಿ ವಿಧಾನ: " + (data.getPaymentMode() != null ? data.getPaymentMode() : "---"));
		// Set Karyakrama Details
		karyakramaNameLabel.setText("ಕಾರ್ಯಕ್ರಮ: " + data.getKaryakramaName());
		receiptDateLabel.setText("ದಿನಾಂಕ: " + data.getFormattedReceiptDate());
		totalAmountLabel.setText("ಒಟ್ಟು ಮೊತ್ತ: ₹" + String.format("%.2f", data.getTotalAmount()));

		// Table Setup
		sevaNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		priceColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
		quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
		sevaTableView.setItems(FXCollections.observableArrayList(data.getSevas()));
	}

	@FXML
	private void handleReprint() {
		if (currentReceiptData != null) {
			Stage stage = (Stage) reprintButton.getScene().getWindow();
			receiptPrinter.showKaryakramaPrintPreview(currentReceiptData, stage, success -> System.out.println("Reprint job from preview status: " + (success ? "Success" : "Failed/Cancelled")), () -> System.out.println("Reprint preview was closed without action."));
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