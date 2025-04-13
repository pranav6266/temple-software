package com.pranav.temple_software.models;

public class Seva {
	private final String id;
	private final String name;
	private final double amount;
	private int displayOrder; // *** ADDED FIELD ***


	@Override
	public String toString() {
		return getName() + " - ₹" + String.format("%.2f", getAmount());
	}

	// Constructor might remain the same if order is set later, or updated
	public Seva(String id, String name, double amount) {
		this.id = id;
		this.name = name;
		this.amount = amount;
		// Initialize with a default or invalid value if needed
		this.displayOrder = 0; // Or perhaps parse id if always numeric?
		try {
			this.displayOrder = Integer.parseInt(id); // Default order to ID
		} catch (NumberFormatException e) {
			this.displayOrder = 0; // Default if ID is not numeric
		}
	}

	// Overloaded constructor if order is known at creation
	public Seva(String id, String name, double amount, int displayOrder) {
		this.id = id;
		this.name = name;
		this.amount = amount;
		this.displayOrder = displayOrder;
	}

	// --- Getters ---
	public String getId() { return id; }
	public String getName() { return name; }
	public double getAmount() { return amount; }

	// *** ADDED Getter and Setter for displayOrder ***
	public int getDisplayOrder() { return displayOrder; }
	public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

	// Optional: Override toString, equals, hashCode if needed
}