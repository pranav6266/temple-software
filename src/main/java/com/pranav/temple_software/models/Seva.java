package com.pranav.temple_software.models;

public class Seva {
	private final String id;
	private final String name;
	private final double amount;

	public Seva(String id, String name, double amount) {
		this.id = id;
		this.name = name;
		this.amount = amount;
	}

	// Getters
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public double getAmount() {
		return amount;
	}
}