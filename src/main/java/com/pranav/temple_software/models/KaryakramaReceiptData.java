package com.pranav.temple_software.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class KaryakramaReceiptData {
	private final int receiptId;
	private final String devoteeName;
	private final String phoneNumber;
	private final String address;
	private final String panNumber;
	private final String rashi;
	private final String nakshatra;
	private final LocalDate receiptDate;
	private final List<SevaEntry> sevas;
	private final double totalAmount;
	private final String paymentMode;

	public KaryakramaReceiptData(int receiptId, String devoteeName, String phoneNumber, String address, String panNumber, String rashi, String nakshatra, LocalDate receiptDate, List<SevaEntry> sevas, double totalAmount, String paymentMode) {
		this.receiptId = receiptId;
		this.devoteeName = devoteeName;
		this.phoneNumber = phoneNumber;
		this.address = address;
		this.panNumber = panNumber;
		this.rashi = rashi;
		this.nakshatra = nakshatra;
		this.receiptDate = receiptDate;
		this.sevas = sevas;
		this.totalAmount = totalAmount;
		this.paymentMode = paymentMode;
	}

	// Getters
	public int getReceiptId() { return receiptId; }
	public String getDevoteeName() { return devoteeName; }
	public String getPhoneNumber() { return phoneNumber; }
	public String getAddress() { return address; }
	public String getPanNumber() { return panNumber; }
	public String getRashi() { return rashi; }
	public String getNakshatra() { return nakshatra; }
	public LocalDate getReceiptDate() { return receiptDate; }
	public List<SevaEntry> getSevas() { return sevas; }
	public double getTotalAmount() { return totalAmount; }
	public String getPaymentMode() { return paymentMode; }
	public String getFormattedReceiptDate() {
		return receiptDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
	}
}