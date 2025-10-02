package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.DonationReceiptData;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.pranav.temple_software.utils.ReceiptPrinter;

public class DonationDetailsController {

	@FXML private Label receiptIdLabel;
	@FXML private Label devoteeNameLabel;
	@FXML private Label phoneNumberLabel;
	@FXML private Label panNumberLabel; // ADDED
	@FXML private Label rashiLabel;
	@FXML private Label nakshatraLabel;
	@FXML private Text addressText;

	@FXML private Label donationDateLabel;
	@FXML private Label donationNameLabel;
	@FXML private Label donationPaymentModeLabel;
	@FXML private Label donationAmountLabel;

	@FXML private Button reprintButton;
	private DonationReceiptData currentDonationData;
	private final ReceiptPrinter receiptPrinter = new ReceiptPrinter(null);

	public void initializeDonationDetails(DonationReceiptData donationData) {
		if (donationData == null) {
			System.err.println("Warning: Donation data is null, cannot initialize details view.");
			return;
		}
		this.currentDonationData = donationData;

		receiptIdLabel.setText("ದೇಣಿಗೆ ರಶೀದಿ ಸಂಖ್ಯೆ: " + donationData.getDonationReceiptId());

		// Devotee Details
		devoteeNameLabel.setText("ಭಕ್ತರ ಹೆಸರು: " + (donationData.getDevoteeName() != null && !donationData.getDevoteeName().isEmpty() ? donationData.getDevoteeName() : "---"));
		phoneNumberLabel.setText("ದೂರವಾಣಿ: " + (donationData.getPhoneNumber() != null && !donationData.getPhoneNumber().isEmpty() ? donationData.getPhoneNumber() : "---"));
		panNumberLabel.setText("PAN ಸಂಖ್ಯೆ: " + (donationData.getPanNumber() != null && !donationData.getPanNumber().isEmpty() ? donationData.getPanNumber() : "---"));
		rashiLabel.setText("ಜನ್ಮ ರಾಶಿ: " + (donationData.getRashi() != null && !donationData.getRashi().isEmpty() ? donationData.getRashi() : "---"));
		nakshatraLabel.setText("ಜನ್ಮ ನಕ್ಷತ್ರ: " + (donationData.getNakshatra() != null && !donationData.getNakshatra().isEmpty() ? donationData.getNakshatra() : "---"));
		addressText.setText("ವಿಳಾಸ: "+(!donationData.getAddress().isEmpty() ? donationData.getAddress() : "---"));

		// Donation Details
		donationDateLabel.setText("ದಿನಾಂಕ: " + donationData.getFormattedDate());
		donationNameLabel.setText("ದೇಣಿಗೆ ವಿಧ: " + donationData.getDonationName());
		donationPaymentModeLabel.setText("ಪಾವತಿ ವಿಧಾನ: " + (donationData.getPaymentMode() != null ? donationData.getPaymentMode() : "---"));
		donationAmountLabel.setText("ದೇಣಿಗೆ ಮೊತ್ತ: ₹" + String.format("%.2f", donationData.getDonationAmount()));
	}

	@FXML
	public void handleClose() {
		Stage stage = (Stage) receiptIdLabel.getScene().getWindow();
		if (stage != null) {
			stage.close();
		}
	}

	@FXML
	private void handleReprint() {
		if (currentDonationData != null) {
			Stage stage = (Stage) reprintButton.getScene().getWindow();
			receiptPrinter.showDonationPrintPreview(currentDonationData, stage, success -> System.out.println("Reprint job from preview status: " + (success ? "Success" : "Failed/Cancelled")), () -> System.out.println("Reprint preview was closed without action."));
		}
	}
}