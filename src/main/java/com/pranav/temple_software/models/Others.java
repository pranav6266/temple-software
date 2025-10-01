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

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	// This is crucial for displaying the name in ComboBoxes
	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Others others = (Others) o;
		return id == others.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}