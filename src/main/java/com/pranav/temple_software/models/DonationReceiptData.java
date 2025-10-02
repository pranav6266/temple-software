package com.pranav.temple_software.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DonationReceiptData {
	private final int donationReceiptId;
	private final String devoteeName;
	private final String phoneNumber;
	private final String address;
	private final String panNumber; // Added PAN
	private final String rashi;
	private final String nakshatra;
	private final LocalDate sevaDate;
	private final String donationName;
	private final double donationAmount;
	private final String paymentMode;

	public DonationReceiptData(int donationReceiptId, String devoteeName, String phoneNumber, String address, String panNumber,
	                           String rashi, String nakshatra, LocalDate sevaDate, String donationName,
	                           double donationAmount, String paymentMode) {
		this.donationReceiptId = donationReceiptId;
		this.devoteeName = devoteeName;
		this.phoneNumber = phoneNumber;
		this.address = address;
		this.panNumber = panNumber; // Set PAN
		this.rashi = rashi;
		this.nakshatra = nakshatra;
		this.sevaDate = sevaDate;
		this.donationName = donationName;
		this.donationAmount = donationAmount;
		this.paymentMode = paymentMode;
	}

	// Getters
	public int getDonationReceiptId() { return donationReceiptId; }
	public String getDevoteeName() { return devoteeName; }
	public String getPhoneNumber() { return phoneNumber; }
	public String getAddress() { return address; }
	public String getPanNumber() { return panNumber; } // Add PAN getter
	public String getRashi() { return rashi; }
	public String getNakshatra() { return nakshatra; }

	public String getDonationName() { return donationName; }
	public double getDonationAmount() { return donationAmount; }
	public String getPaymentMode() { return paymentMode; }
	public String getFormattedDate() {
		return sevaDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
	}
}
