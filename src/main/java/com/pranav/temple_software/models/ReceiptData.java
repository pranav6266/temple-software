package com.pranav.temple_software.models;

import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReceiptData {
	private final String heading = "ಶ್ರೀ ಶಾಸ್ತಾರ ಸುಬ್ರಹ್ಮಣ್ಯೇಶ್ವರ ದೇವಸ್ಥಾನ ";
	private final String subHeading = "ಸೇವಾ / ದೇಣಿಗೆ ರಶೀದಿ";
	private final String devoteeName;
	private final String phoneNumber;
	private final LocalDate sevaDate;
	private final ObservableList<SevaEntry> sevas;
	private final double totalAmount;
	private final String rashi;
	private final String nakshatra;
	private final int receiptId;
	private final String paymentMode;
	private final String donationStatus;

	public ReceiptData(int receiptId, String devoteeName, String phoneNumber, String rashi, String nakshatra,
	                   LocalDate sevaDate, ObservableList<SevaEntry> sevas, double totalAmount,String paymentMode, String donationStatus) {
		this.receiptId = receiptId;
		this.devoteeName = devoteeName;
		this.phoneNumber = phoneNumber;
		this.rashi = rashi;
		this.nakshatra = nakshatra;
		this.sevaDate = sevaDate;
		this.sevas = sevas;
		this.totalAmount = totalAmount;
		this.paymentMode = paymentMode;
		this.donationStatus = donationStatus;
	}


	// --- Getters for all fields ---
	public String getHeading() { return heading; }
	public String getSubHeading() { return subHeading; }
	public String getDevoteeName() { return devoteeName; }
	public String getPhoneNumber(){ return phoneNumber;}
	public int getReceiptId() { return receiptId; }
	public String getRaashi() { return rashi;}
	public String getNakshatra() {return nakshatra;}
	public LocalDate getSevaDate() { return sevaDate; }
	public ObservableList<SevaEntry> getSevas() { return sevas; }
	public double getTotalAmount() { return totalAmount; }
	public String getFinalLine() { return "ಶ್ರೀ ದೇವರ ಕೃಪೆ ಸದಾ ಇರಲಿ !"; }
	public String getFormattedDate() {return sevaDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));}
	public String getPaymentMode() {return paymentMode;}
	public String getDonationStatus() {return donationStatus;}
}