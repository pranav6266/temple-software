package com.pranav.temple_software.models;

/**
 * A simple data class to hold devotee information fetched from the database.
 */
public class DevoteeDetails {
	private final String name;
	private final String address;
	private final String rashi;
	private final String nakshatra;

	public DevoteeDetails(String name, String address, String rashi, String nakshatra) {
		this.name = name;
		this.address = address;
		this.rashi = rashi;
		this.nakshatra = nakshatra;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getRashi() {
		return rashi;
	}

	public String getNakshatra() {
		return nakshatra;
	}
}