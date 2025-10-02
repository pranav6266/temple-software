package com.pranav.temple_software.models;

import javafx.beans.property.*;

public class DashboardStats {
	private final StringProperty itemName;
	private final StringProperty itemType; // "SEVA", "OTHERS", "DONATION"
	private final IntegerProperty totalCount;
	private final IntegerProperty cashCount;
	private final IntegerProperty onlineCount;
	private final DoubleProperty totalAmount;

	public DashboardStats(String itemId, String itemName, String itemType,
	                      int totalCount, int cashCount, int onlineCount, double totalAmount) {
		// For database queries
		this.itemName = new SimpleStringProperty(itemName);
		this.itemType = new SimpleStringProperty(itemType);
		this.totalCount = new SimpleIntegerProperty(totalCount);
		this.cashCount = new SimpleIntegerProperty(cashCount);
		this.onlineCount = new SimpleIntegerProperty(onlineCount);
		this.totalAmount = new SimpleDoubleProperty(totalAmount);
	}

	public String getItemName() { return itemName.get(); }

	public String getItemType() { return itemType.get(); }

	public int getTotalCount() { return totalCount.get(); }

	public int getCashCount() { return cashCount.get(); }

	public int getOnlineCount() { return onlineCount.get(); }

	public double getTotalAmount() { return totalAmount.get(); }
}
