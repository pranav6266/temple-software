// Optional: Create a dedicated Donation model
package com.pranav.temple_software.models;

import java.util.Objects;

public class Donations {
	private final String id; // Or use name as ID if ID isn't strictly needed in logic
	private final String name;

	public Donations(String id, String name) {
		this.id = id;
		this.name = name;
	}


	public String getId() { return id; }
	public String getName() { return name; }

	// Override equals and hashCode if using in HashMaps/Sets based on name/id
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Donations donation = (Donations) o;
		return Objects.equals(name, donation.name); // Example: equals based on name
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(name); // Example: hashCode based on name
	}
}