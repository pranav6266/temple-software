package com.pranav.temple_software.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SevaEntry {
	private final StringProperty name;
	private final DoubleProperty amount;

	public SevaEntry( String name, double amount) {
		this.name = new SimpleStringProperty(name);
		this.amount = new SimpleDoubleProperty(amount);
	}

	// Getters and property methods
	public String getName() { return name.get(); }
	public double getAmount() { return amount.get(); }
	public StringProperty nameProperty() { return name; }
	public DoubleProperty amountProperty() { return amount; }
}