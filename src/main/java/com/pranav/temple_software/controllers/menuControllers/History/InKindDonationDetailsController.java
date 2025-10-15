package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.InKindDonation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.pranav.temple_software.utils.ReceiptPrinter;

public class InKindDonationDetailsController {

	@FXML private Label receiptIdLabel;
	@FXML private Label devoteeNameLabel;
	@FXML private Label phoneNumberLabel;
	@FXML private Label panNumberLabel; // ADDED
	@FXML private Label rashiLabel;
	@FXML private Label nakshatraLabel;
	@FXML private Text addressText;
	@FXML private Label paymentModeLabel;
	@FXML private Label donationDateLabel;
	@FXML private Text itemDescriptionText;

	@FXML private Button reprintButton;

	private InKindDonation currentDonationData;
	private final ReceiptPrinter receiptPrinter = new ReceiptPrinter();

	public void initializeDetails(InKindDonation donationData) {
		if (donationData == null) {
			System.err.println("Warning: In-Kind donation data is null.");
			return;
		}
		this.currentDonationData = donationData;

		receiptIdLabel.setText("ರಶೀದಿ ಸಂಖ್ಯೆ: " + donationData.getInKindReceiptId());

		// Devotee Details
		devoteeNameLabel.setText("ಭಕ್ತರ ಹೆಸರು: " + (donationData.getDevoteeName() != null && !donationData.getDevoteeName().isEmpty() ? donationData.getDevoteeName() : "---"));
		phoneNumberLabel.setText("ದೂರವಾಣಿ: " + (donationData.getPhoneNumber() != null && !donationData.getPhoneNumber().isEmpty() ? donationData.getPhoneNumber() : "---"));
		panNumberLabel.setText("PAN ಸಂಖ್ಯೆ: " + (donationData.getPanNumber() != null && !donationData.getPanNumber().isEmpty() ? donationData.getPanNumber() : "---"));
		rashiLabel.setText("ಜನ್ಮ ರಾಶಿ: " + (donationData.getRashi() != null && !donationData.getRashi().isEmpty() ? donationData.getRashi() : "---"));
		nakshatraLabel.setText("ಜನ್ಮ ನಕ್ಷತ್ರ: " + (donationData.getNakshatra() != null && !donationData.getNakshatra().isEmpty() ? donationData.getNakshatra() : "---"));
		addressText.setText("ವಿಳಾಸ: " + (donationData.getAddress() != null && !donationData.getAddress().isEmpty() ? donationData.getAddress() : "---"));		paymentModeLabel.setText("ಪಾವತಿ ವಿಧಾನ: " + (donationData.getPaymentMode() != null ? donationData.getPaymentMode() : "---"));
		// Donation Details
		donationDateLabel.setText("ದಿನಾಂಕ: " + donationData.getFormattedDate());
		itemDescriptionText.setText(donationData.getItemDescription());
	}

	@FXML
	private void handleReprint() {
		if (currentDonationData != null) {
			Stage stage = (Stage) reprintButton.getScene().getWindow();
			receiptPrinter.showInKindDonationPrintPreview(currentDonationData, stage, success -> System.out.println("Reprint job from preview status: " + (success ? "Success" : "Failed/Cancelled")), () -> System.out.println("Reprint preview was closed without action."));
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