// FILE: src/main/java/com/pranav/temple_software/controllers/menuControllers/History/ShashwathaPoojaDetailsController.java
package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.ShashwathaPoojaReceipt;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ShashwathaPoojaDetailsController {

	@FXML private Label receiptIdLabel;
	@FXML private Label devoteeNameLabel;
	@FXML private Label phoneNumberLabel;
	@FXML private Label receiptDateLabel;
	@FXML private Label rashiLabel;
	@FXML private Label nakshatraLabel;
	@FXML private Text addressText;
	@FXML private Text poojaDateText;

	public void initializeDetails(ShashwathaPoojaReceipt data) {
		if (data == null) return;

		receiptIdLabel.setText("ರಶೀದಿ ಸಂಖ್ಯೆ: " + data.getReceiptId());
		devoteeNameLabel.setText("ಭಕ್ತರ ಹೆಸರು: " + (data.getDevoteeName().isEmpty() ? "---" : data.getDevoteeName()));
		phoneNumberLabel.setText("ದೂರವಾಣಿ: " + (data.getPhoneNumber().isEmpty() ? "---" : data.getPhoneNumber()));
		receiptDateLabel.setText("ರಶೀದಿ ದಿನಾಂಕ: " + data.getFormattedReceiptDate());
		rashiLabel.setText("ಜನ್ಮ ರಾಶಿ: " + (data.getRashi() != null && !data.getRashi().equals("ಆಯ್ಕೆ") ? data.getRashi() : "---"));
		nakshatraLabel.setText("ಜನ್ಮ ನಕ್ಷತ್ರ: " + (data.getNakshatra() != null ? data.getNakshatra() : "---"));
		addressText.setText("ವಿಳಾಸ: " + (data.getAddress().isEmpty() ? "---" : data.getAddress()));
		poojaDateText.setText(data.getPoojaDate());
	}

	@FXML
	public void handleClose() {
		Stage stage = (Stage) receiptIdLabel.getScene().getWindow();
		if (stage != null) {
			stage.close();
		}
	}
}