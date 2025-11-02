// PASTE THIS CODE INTO THE NEW FILE

package com.pranav.temple_software.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InKindDonation {
	private final int inKindReceiptId;
	private final String devoteeName;
	private final String phoneNumber;
	private final String address;
	private final String panNumber; // ADDED
	private final String rashi;
	private final String nakshatra;
	private final LocalDate donationDate;
	private final String itemDescription;

	public InKindDonation(int inKindReceiptId, String devoteeName,
	                      String phoneNumber, String address, String panNumber,
	                      String rashi, String nakshatra, LocalDate donationDate,
	                      String itemDescription) {
		this.inKindReceiptId = inKindReceiptId;
		this.devoteeName = devoteeName;
		this.phoneNumber = phoneNumber;
		this.address = address;
		this.panNumber = panNumber; // ADDED
		this.rashi = rashi;
		this.nakshatra = nakshatra;
		this.donationDate = donationDate;
		this.itemDescription = itemDescription;
	}

	// Getters
	public int getInKindReceiptId() {
		return inKindReceiptId;
	}

	public String getDevoteeName() {
		return devoteeName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getAddress() {
		return address;
	}

	public String getPanNumber() { // ADDED
		return panNumber;
	}

	public String getRashi() {
		return rashi;
	}

	public String getNakshatra() {
		return nakshatra;
	}

	public LocalDate getDonationDate() {
		return donationDate;
	}

	public String getItemDescription() {
		return itemDescription;
	}

	public String getFormattedDate() {
		if (donationDate != null) {
			return donationDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		}
		return "";
	}
}