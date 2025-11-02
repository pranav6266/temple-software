package com.pranav.temple_software.models;

import java.time.LocalDate;

public class HistoryFilterCriteria {
	private String devoteeName;
	private String phoneNumber;
	private LocalDate fromDate;
	private LocalDate toDate;
	private String receiptId;

	// Getters and Setters
	public String getDevoteeName() { return devoteeName; }
	public void setDevoteeName(String devoteeName) { this.devoteeName = devoteeName; }
	public String getPhoneNumber() { return phoneNumber; }
	public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
	public LocalDate getFromDate() { return fromDate; }
	public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
	public LocalDate getToDate() { return toDate; }
	public void setToDate(LocalDate toDate) { this.toDate = toDate; }
	public String getReceiptId() { return receiptId; }
	public void setReceiptId(String receiptId) { this.receiptId = receiptId; }
}