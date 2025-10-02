// FILE: src/main/java/com/pranav/temple_software/controllers/menuControllers/History/ShashwathaPoojaDetailsController.java
package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.ShashwathaPoojaReceipt;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.pranav.temple_software.utils.ReceiptPrinter;

public class ShashwathaPoojaDetailsController {

	@FXML private Label receiptIdLabel;
	@FXML private Label devoteeNameLabel;
	@FXML private Label phoneNumberLabel;
	@FXML private Label panNumberLabel;
	@FXML private Label rashiLabel;
	@FXML private Label nakshatraLabel;
	@FXML private Text addressText;
	@FXML private Label amountLabel; // Added
	@FXML private Label receiptDateLabel;
	@FXML private Text poojaDateText;

	@FXML private Button reprintButton;
	private ShashwathaPoojaReceipt currentPoojaData;
	private final ReceiptPrinter receiptPrinter = new ReceiptPrinter();
	public void initializeDetails(ShashwathaPoojaReceipt data) {
		if (data == null) return;
		this.currentPoojaData = data;
		receiptIdLabel.setText("ರಶೀದಿ ಸಂಖ್ಯೆ: " + data.getReceiptId());
		// Devotee Details
		devoteeNameLabel.setText("ಭಕ್ತರ ಹೆಸರು: " + (data.getDevoteeName() != null && !data.getDevoteeName().isEmpty() ? data.getDevoteeName() : "---"));
		phoneNumberLabel.setText("ದೂರವಾಣಿ: " + (data.getPhoneNumber() != null && !data.getPhoneNumber().isEmpty() ? data.getPhoneNumber() : "---"));
		panNumberLabel.setText("PAN ಸಂಖ್ಯೆ: " + (data.getPanNumber() != null && !data.getPanNumber().isEmpty() ? data.getPanNumber() : "---"));
		rashiLabel.setText("ಜನ್ಮ ರಾಶಿ: " + (data.getRashi() != null && !data.getRashi().isEmpty() ? data.getRashi() : "---"));
		nakshatraLabel.setText("ಜನ್ಮ ನಕ್ಷತ್ರ: " + (data.getNakshatra() != null && !data.getNakshatra().isEmpty() ? data.getNakshatra() : "---"));
		addressText.setText("ವಿಳಾಸ:"+(data.getAddress() != null && !data.getAddress().isEmpty() ? data.getAddress() : "---"));

		// Pooja Details
		receiptDateLabel.setText("ರಶೀದಿ ದಿನಾಂಕ: " + data.getFormattedReceiptDate());
		poojaDateText.setText(data.getPoojaDate());
		amountLabel.setText("ಪೂಜಾ ಮೊತ್ತ: ₹" + String.format("%.2f", data.getAmount()));
	}

	@FXML
	private void handleReprint() {
		if (currentPoojaData != null) {
			Stage stage = (Stage) reprintButton.getScene().getWindow();
			receiptPrinter.showShashwathaPoojaPrintPreview(currentPoojaData, stage, success -> System.out.println("Reprint job from preview status: " + (success ? "Success" : "Failed/Cancelled")), () -> System.out.println("Reprint preview was closed without action."));
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