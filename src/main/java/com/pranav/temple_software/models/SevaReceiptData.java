package com.pranav.temple_software.models;

import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SevaReceiptData {

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
	private final String address;

	public SevaReceiptData(int receiptId, String devoteeName, String phoneNumber, String address, String rashi, String nakshatra,
	                       LocalDate sevaDate, ObservableList<SevaEntry> sevas, double totalAmount, String paymentMode, String donationStatus) {
		this.receiptId = receiptId;
		this.devoteeName = devoteeName;
		this.phoneNumber = phoneNumber;
		this.address = address;
		this.rashi = rashi;
		this.nakshatra = nakshatra;
		this.sevaDate = sevaDate;
		this.sevas = sevas;
		this.totalAmount = totalAmount;
		this.paymentMode = paymentMode;
		this.donationStatus = donationStatus;
	}


	// --- Getters for all fields ---
	public String getDevoteeName() { return devoteeName; }
	public String getPhoneNumber(){ return phoneNumber;}
	public int getReceiptId() { return receiptId; }
	public String getRashi() { return rashi;}
	public String getNakshatra() {return nakshatra;}
	public LocalDate getSevaDate() { return sevaDate; }
	public ObservableList<SevaEntry> getSevas() { return sevas; }
	public double getTotalAmount() { return totalAmount; }
	public String getFormattedDate() {return sevaDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));}
	public String getPaymentMode() {return paymentMode;}
	public String getDonationStatus() {return donationStatus;}
	public String getAddress(){return address;}
}