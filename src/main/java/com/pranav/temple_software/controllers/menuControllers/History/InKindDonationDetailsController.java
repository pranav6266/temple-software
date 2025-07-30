package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.InKindDonation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class InKindDonationDetailsController {

	@FXML private Label receiptIdLabel;
	@FXML private Label devoteeNameLabel;
	@FXML private Label phoneNumberLabel;
	@FXML private Label donationDateLabel;
	@FXML private Label rashiLabel;
	@FXML private Label nakshatraLabel;
	@FXML private Text addressText;
	@FXML private Text itemDescriptionText;

	public void initializeDetails(InKindDonation data) {
		if (data == null) return;

		receiptIdLabel.setText("ರಶೀದಿ ಸಂಖ್ಯೆ: " + data.getInKindReceiptId());
		devoteeNameLabel.setText("ಭಕ್ತರ ಹೆಸರು: " + (data.getDevoteeName().isEmpty() ? "---" : data.getDevoteeName()));
		phoneNumberLabel.setText("ದೂರವಾಣಿ: " + (data.getPhoneNumber().isEmpty() ? "---" : data.getPhoneNumber()));
		donationDateLabel.setText("ದಿನಾಂಕ: " + data.getFormattedDate());
		rashiLabel.setText("ಜನ್ಮ ರಾಶಿ: " + (data.getRashi() != null && !data.getRashi().equals("ಆಯ್ಕೆ") ? data.getRashi() : "---"));
		nakshatraLabel.setText("ಜನ್ಮ ನಕ್ಷತ್ರ: " + (data.getNakshatra() != null ? data.getNakshatra() : "---"));
		addressText.setText("ವಿಳಾಸ: " + (data.getAddress().isEmpty() ? "---" : data.getAddress()));
		itemDescriptionText.setText(data.getItemDescription());
	}

	@FXML
	public void handleClose() {
		Stage stage = (Stage) receiptIdLabel.getScene().getWindow();
		if (stage != null) {
			stage.close();
		}
	}
}