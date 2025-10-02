// FILE: src/main/java/com/pranav/temple_software/models/Others.java
package com.pranav.temple_software.models;

import java.util.Objects;

public class Others {
	private final int id;
	private String name;
	private int displayOrder;

	public Others(int id, String name, int displayOrder) {
		this.id = id;
		this.name = name;
		this.displayOrder = displayOrder;
	}

	public int getId() { return id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public int getDisplayOrder() { return displayOrder; }

	@Override
	public String toString() { return name; }

	// UPDATED equals and hashCode to correctly check for changes
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Others others = (Others) o;
		return id == others.id && displayOrder == others.displayOrder && Objects.equals(name, others.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, displayOrder);
	}
}