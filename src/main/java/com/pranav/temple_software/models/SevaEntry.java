package com.pranav.temple_software.models;

import javafx.beans.property.*;

public class SevaEntry {
	private final StringProperty name;
	private DoubleProperty amount;
	private final IntegerProperty quantity;
	private final DoubleProperty totalAmount;

	public SevaEntry( String name, double amount) {
		this.name = new SimpleStringProperty(name);
		this.amount = new SimpleDoubleProperty(amount);
		this.quantity = new SimpleIntegerProperty(1); // Default quantity
		this.totalAmount = new SimpleDoubleProperty(amount * 1);

		// Bind totalAmount to quantity * amount
		totalAmount.bind(quantity.multiply(amount));

	}

	// Getters and property methods
	public String getName() { return name.get(); }
	public double getAmount() { return amount.get(); }
	public StringProperty nameProperty() { return name; }
	public DoubleProperty amountProperty() { return amount; }
	public void setAmount(double amount){this.amount = new SimpleDoubleProperty(amount);}
	// Inside SevaEntry.java
	public int getQuantity() {
		return quantity.get();
	}

	public void setQuantity(int quantity) {
		this.quantity.set(quantity);
	}

	public IntegerProperty quantityProperty() {
		return quantity;
	}

	public DoubleProperty totalAmountProperty() { return totalAmount; }

	public double getTotalAmount() {
		return totalAmount.get();
	}
}