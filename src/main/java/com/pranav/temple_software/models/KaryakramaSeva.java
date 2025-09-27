package com.pranav.temple_software.models;

import java.util.Objects;

public class KaryakramaSeva {
	private final int id;
	private final int karyakramaId;
	private String name;
	private double amount;

	public KaryakramaSeva(int id, int karyakramaId, String name, double amount) {
		this.id = id;
		this.karyakramaId = karyakramaId;
		this.name = name;
		this.amount = amount;
	}

	public int getId() { return id; }
	public int getKaryakramaId() { return karyakramaId; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public double getAmount() { return amount; }
	public void setAmount(double amount) { this.amount = amount; }

	// For display in ComboBoxes
	@Override
	public String toString() {
		return name + " - ₹" + String.format("%.2f", amount);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		KaryakramaSeva that = (KaryakramaSeva) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}