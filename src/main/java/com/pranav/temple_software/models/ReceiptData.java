package com.pranav.temple_software.models;

import com.pranav.temple_software.controllers.MainController.SevaEntry;
import javafx.collections.ObservableList;
import java.time.LocalDate;

public class ReceiptData {
	private final String heading = "ಶ್ರೀ ಶಾಸ್ತಾರ ಸುಬ್ರಹ್ಮಣ್ಯೇಶ್ವರ ದೇವಸ್ಥಾನ ";
	private final String subHeading = "ಸೇವಾ / ದೇಣಿಗೆ ರಶೀದಿ";
	private final String devoteeName;
	private final String phoneNumber;
	private final LocalDate sevaDate;
	private final ObservableList<SevaEntry> sevas;
	private final double totalAmount;

	public ReceiptData(String devoteeName, String phoneNumber, LocalDate sevaDate, ObservableList<SevaEntry> sevas, double totalAmount) {
		this.devoteeName = devoteeName;
		this.phoneNumber = phoneNumber;
		this.sevaDate = sevaDate;
		this.sevas = sevas;
		this.totalAmount = totalAmount;
	}

	// --- Getters for all fields ---
	public String getHeading() { return heading; }
	public String getSubHeading() { return subHeading; }
	public String getDevoteeName() { return devoteeName; }
	public String getPhoneNumber() { return phoneNumber; }
	public LocalDate getSevaDate() { return sevaDate; }
	public ObservableList<SevaEntry> getSevas() { return sevas; }
	public double getTotalAmount() { return totalAmount; }
	public String getFinalLine() { return "ಶ್ರೀ ದೇವರ ಕೃಪೆ ಸದಾ ಇರಲಿ !"; }
}