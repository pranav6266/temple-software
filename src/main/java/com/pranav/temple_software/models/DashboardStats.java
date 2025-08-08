package com.pranav.temple_software.models;

import javafx.beans.property.*;

public class DashboardStats {
	private final StringProperty itemName;
	private final StringProperty itemType; // "SEVA", "OTHERS", "DONATION"
	private final IntegerProperty totalCount;
	private final IntegerProperty cashCount;
	private final IntegerProperty onlineCount;
	private final DoubleProperty totalAmount;
	private final StringProperty itemId; // For database queries

	public DashboardStats(String itemId, String itemName, String itemType,
	                      int totalCount, int cashCount, int onlineCount, double totalAmount) {
		this.itemId = new SimpleStringProperty(itemId);
		this.itemName = new SimpleStringProperty(itemName);
		this.itemType = new SimpleStringProperty(itemType);
		this.totalCount = new SimpleIntegerProperty(totalCount);
		this.cashCount = new SimpleIntegerProperty(cashCount);
		this.onlineCount = new SimpleIntegerProperty(onlineCount);
		this.totalAmount = new SimpleDoubleProperty(totalAmount);
	}

	// Getters and Property Methods
	public String getItemId() { return itemId.get(); }
	public StringProperty itemIdProperty() { return itemId; }

	public String getItemName() { return itemName.get(); }
	public StringProperty itemNameProperty() { return itemName; }

	public String getItemType() { return itemType.get(); }
	public StringProperty itemTypeProperty() { return itemType; }

	public int getTotalCount() { return totalCount.get(); }
	public IntegerProperty totalCountProperty() { return totalCount; }

	public int getCashCount() { return cashCount.get(); }
	public IntegerProperty cashCountProperty() { return cashCount; }

	public int getOnlineCount() { return onlineCount.get(); }
	public IntegerProperty onlineCountProperty() { return onlineCount; }

	public double getTotalAmount() { return totalAmount.get(); }
	public DoubleProperty totalAmountProperty() { return totalAmount; }
}
