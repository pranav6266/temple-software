package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.ReceiptData;
import com.pranav.temple_software.models.SevaEntry; // Import SevaEntry
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView; // Or TextArea
import javafx.collections.FXCollections; // Import FXCollections
import javafx.collections.ObservableList; // Import ObservableList


public class ReceiptDetailsController {

	@FXML private Label receiptIdLabel;
	@FXML private Label devoteeNameLabel;
	@FXML private Label phoneNumberLabel;
	@FXML private Label sevaDateLabel;
	@FXML private Label rashiLabel;
	@FXML private Label nakshatraLabel;
	@FXML private Label totalAmountLabel;
	@FXML private ListView<String> sevasListView; // Or TextArea sevasTextArea;

	public void initializeDetails(ReceiptData data) {
		if (data == null) return;

		receiptIdLabel.setText("ರಶೀದಿ ಸಂಖ್ಯೆ: " + data.getReceiptId());
		devoteeNameLabel.setText("ಭಕ್ತರ ಹೆಸರು: " + data.getDevoteeName());
		phoneNumberLabel.setText("ದೂರವಾಣಿ ಸಂಖ್ಯೆ: " + (data.getPhoneNumber() != null ? data.getPhoneNumber() : "N/A"));
		sevaDateLabel.setText("ಸೇವಾ ದಿನಾಂಕ: " + data.getFormattedDate()); // Use formatted date
		rashiLabel.setText("ರಾಶಿ: " + (data.getRaashi() != null && !data.getRaashi().isEmpty() ? data.getRaashi() : "ನಿರ್ದಿಷ್ಟಪಡಿಸಿಲ್ಲ"));
		nakshatraLabel.setText("ನಕ್ಷತ್ರ: " + (data.getNakshatra() != null && !data.getNakshatra().isEmpty() ? data.getNakshatra() : "ನಿರ್ದಿಷ್ಟಪಡಿಸಿಲ್ಲ"));
		totalAmountLabel.setText(String.format("ಒಟ್ಟು ಮೊತ್ತ: ₹%.2f", data.getTotalAmount()));

		// Populate the ListView/TextArea with Seva details
		ObservableList<String> sevaDetails = FXCollections.observableArrayList();
		if (data.getSevas() != null) { // Check if sevas list is not null
			for (SevaEntry entry : data.getSevas()) { // Iterate through sevas
				sevaDetails.add(String.format("%s : ₹%.2f", entry.getName(), entry.getAmount()));
			}
		}
		sevasListView.setItems(sevaDetails);

		// If using TextArea:
		// StringBuilder sb = new StringBuilder();
		// if (data.getSevas() != null) {
		//     for (SevaEntry entry : data.getSevas()) {
		//         sb.append(String.format("%s : ₹%.2f\n", entry.getName(), entry.getAmount()));
		//     }
		// }
		// sevasTextArea.setText(sb.toString());
	}
}