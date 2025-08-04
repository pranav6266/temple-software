// FILE: src/main/java/com/pranav/temple_software/models/ShashwathaPoojaReceipt.java
package com.pranav.temple_software.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ShashwathaPoojaReceipt {
	private final int receiptId;
	private final String devoteeName;
	private final String phoneNumber;
	private final String address;
	private final String rashi;
	private final String nakshatra;
	private final LocalDate receiptDate;
	private final String poojaDate; // String type for special dates

	public ShashwathaPoojaReceipt(int receiptId, String devoteeName, String phoneNumber, String address, String rashi, String nakshatra, LocalDate receiptDate, String poojaDate) {
		this.receiptId = receiptId;
		this.devoteeName = devoteeName;
		this.phoneNumber = phoneNumber;
		this.address = address;
		this.rashi = rashi;
		this.nakshatra = nakshatra;
		this.receiptDate = receiptDate;
		this.poojaDate = poojaDate;
	}

	// Getters
	public int getReceiptId() { return receiptId; }
	public String getDevoteeName() { return devoteeName; }
	public String getPhoneNumber() { return phoneNumber; }
	public String getAddress() { return address; }
	public String getRashi() { return rashi; }
	public String getNakshatra() { return nakshatra; }
	public LocalDate getReceiptDate() { return receiptDate; }
	public String getPoojaDate() { return poojaDate; }

	public String getFormattedReceiptDate() {
		if (receiptDate != null) {
			return receiptDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		}
		return "";
	}
}