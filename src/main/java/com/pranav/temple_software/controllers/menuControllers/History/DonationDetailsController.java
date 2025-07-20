package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.DonationReceiptData;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class DonationDetailsController {

	@FXML private Label donationReceiptIdLabel;
	@FXML private Label donationDevoteeNameLabel;
	@FXML private Label donationPhoneNumberLabel;
	@FXML private Label donationDateLabel;
	@FXML private Label donationRashiLabel;
	@FXML private Label donationNakshatraLabel;
	@FXML private Label donationNameLabel;
	@FXML private Label donationAmountLabel;
	@FXML private Label donationPaymentModeLabel;
	@FXML private Text donationAddressText;

	/**
	 * Initializes the donation details view with the provided donation receipt data.
	 * This method populates all the UI elements with donation-specific information.
	 *
	 * @param donationData The DonationReceiptData object containing all donation details
	 */
	public void initializeDonationDetails(DonationReceiptData donationData) {
		if (donationData == null) {
			System.err.println("Warning: Donation data is null, cannot initialize details view.");
			return;
		}

		// Set receipt identification
		donationReceiptIdLabel.setText("ದೇಣಿಗೆ ರಶೀದಿ ಸಂಖ್ಯೆ: " + donationData.getDonationReceiptId());

		// Set devotee personal information
		donationDevoteeNameLabel.setText("ಭಕ್ತರ ಹೆಸರು: " +
				(donationData.getDevoteeName() != null && !donationData.getDevoteeName().isEmpty()
						? donationData.getDevoteeName() : "---"));

		donationPhoneNumberLabel.setText("ದೂರವಾಣಿ: " +
				(donationData.getPhoneNumber() != null && !donationData.getPhoneNumber().isEmpty()
						? donationData.getPhoneNumber() : "---"));

		donationAddressText.setText("ವಿಳಾಸ: " +
				(donationData.getAddress() != null && !donationData.getAddress().isEmpty()
						? donationData.getAddress() : "---"));

		// Set astrological information
		donationRashiLabel.setText("ಜನ್ಮ ರಾಶಿ: " +
				(donationData.getRashi() != null && !donationData.getRashi().isEmpty()
						? donationData.getRashi() : "---"));

		donationNakshatraLabel.setText("ಜನ್ಮ ನಕ್ಷತ್ರ: " +
				(donationData.getNakshatra() != null && !donationData.getNakshatra().isEmpty()
						? donationData.getNakshatra() : "---"));

		// Set donation transaction details
		donationDateLabel.setText("ದಿನಾಂಕ: " + donationData.getFormattedDate());
		donationNameLabel.setText("ದೇಣಿಗೆ ವಿಧ: " + donationData.getDonationName());
		donationAmountLabel.setText("ದೇಣಿಗೆ ಮೊತ್ತ: ₹" + String.format("%.2f", donationData.getDonationAmount()));
		donationPaymentModeLabel.setText("ಪಾವತಿ ವಿಧಾನ: " +
				(donationData.getPaymentMode() != null ? donationData.getPaymentMode() : "---"));
	}

	/**
	 * Handles the close button action to close the donation details window.
	 * This method can be called from a close button in the FXML.
	 */
	@FXML
	public void handleClose() {
		Stage stage = (Stage) donationReceiptIdLabel.getScene().getWindow();
		if (stage != null) {
			stage.close();
		}
	}

	/**
	 * Alternative method to get the current stage for external closing.
	 * Useful when the close operation is handled from parent controllers.
	 *
	 * @return The current Stage object or null if not available
	 */
	public Stage getCurrentStage() {
		if (donationReceiptIdLabel != null && donationReceiptIdLabel.getScene() != null) {
			return (Stage) donationReceiptIdLabel.getScene().getWindow();
		}
		return null;
	}

	/**
	 * Validates if the controller has been properly initialized with FXML elements.
	 * This can be used for debugging purposes.
	 *
	 * @return true if all required FXML elements are loaded, false otherwise
	 */
	public boolean isProperlyInitialized() {
		return donationReceiptIdLabel != null &&
				donationDevoteeNameLabel != null &&
				donationDateLabel != null &&
				donationAmountLabel != null &&
				donationNameLabel != null;
	}
}
