package com.pranav.temple_software.models;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ShashwathaPoojaReceipt {
	private final int receiptId;
	private final String devoteeName;
	private final String phoneNumber;
	private final String address;
	private final String panNumber;
	private final String rashi;
	private final String nakshatra;
	private final LocalDate receiptDate;
	private final String poojaDate;
	private final double amount;
	private final String paymentMode;

	public ShashwathaPoojaReceipt(int receiptId, String devoteeName, String phoneNumber, String address, String panNumber, String rashi, String nakshatra, LocalDate receiptDate, String poojaDate, double amount, String paymentMode) {
		this.receiptId = receiptId;
		this.devoteeName = devoteeName;
		this.phoneNumber = phoneNumber;
		this.address = address;
		this.panNumber = panNumber;
		this.rashi = rashi;
		this.nakshatra = nakshatra;
		this.receiptDate = receiptDate;
		this.poojaDate = poojaDate;
		this.amount = amount;
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
	public String getPoojaDate() { return poojaDate; }
	public double getAmount() { return amount; }
	public String getPaymentMode() { return paymentMode; }

	public String getFormattedReceiptDate() {
		if (receiptDate != null) {
			return receiptDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		}
		return "";
	}
}