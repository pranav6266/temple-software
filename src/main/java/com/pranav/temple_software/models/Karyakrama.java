// FILE: src/main/java/com/pranav/temple_software/models/Karyakrama.java
package com.pranav.temple_software.models;

import java.util.Objects;

public class Karyakrama {
	private final int id;
	private String name;
	private boolean isActive;

	public Karyakrama(int id, String name, boolean isActive) {
		this.id = id;
		this.name = name;
		this.isActive = isActive;
	}

	public int getId() { return id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public boolean isActive() { return isActive; }
	public void setActive(boolean active) { isActive = active; }

	@Override
	public String toString() { return name; }

	// UPDATED equals and hashCode to correctly check for changes
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Karyakrama that = (Karyakrama) o;
		return id == that.id && isActive == that.isActive && Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, isActive);
	}
}