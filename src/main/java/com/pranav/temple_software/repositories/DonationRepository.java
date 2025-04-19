package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.Donations;
import com.pranav.temple_software.models.SevaEntry; // You can reuse this if donation data is similar
import java.sql.*;
import java.util.*;

public class DonationRepository {
	private static final String DB_URL = "jdbc:h2:./temple_data";
	private static final String USER = "sa";
	private static final String PASS = "";

	private static final DonationRepository instance = new DonationRepository();
	private final List<Donations> donationList = new ArrayList<>();

	private DonationRepository() {
		loadDonationsFromDB();
	}

	public static DonationRepository getInstance() {
		return instance;
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER, PASS);
	}

	public int getMaxDonationId() {
		String sql = "SELECT MAX(CAST(donation_id AS INT)) FROM Donations";
		int maxId = 0;
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()){
				maxId = rs.getInt(1);
			}
		} catch (SQLException e) {
			System.err.println("Error fetching max Donation ID: " + e.getMessage());
		}
		return maxId;
	}

	// FILE: src/main/java/com/pranav/temple_software/repositories/DonationRepository.java
	public void loadDonationsFromDB() {
		donationList.clear();
		String sql = "SELECT donation_id, donation_name, display_order FROM Donations ORDER BY display_order"; // [cite: 1102]
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				String id = rs.getString("donation_id"); // [cite: 1103]
				String name = rs.getString("donation_name"); // [cite: 1104]
				int order = rs.getInt("display_order"); // [cite: 1104]
				// CORRECTED: Create and add a Donations object
				donationList.add(new Donations(id, name, order)); // Use the Donations constructor [cite: 340]
			}
			System.out.println("Loaded " + donationList.size() + " donations from DB."); // [cite: 1105]
		} catch (SQLException e) {
			System.err.println("Error loading Donations from database: " + e.getMessage()); // [cite: 1106]
		}
	}

	public String getDonationIdByName(String name) {
		String sql = "SELECT donation_id FROM Donations WHERE donation_name = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getString("donation_id");
			}
		} catch (SQLException e) {
			System.err.println("Error fetching donation ID for name: " + name + " - " + e.getMessage());
		}
		return null;
	}

	public boolean addDonationToDB(String donationId, String donationName, double amount) { // Amount param is unused here
		int nextOrder = getMaxDonationId() + 1; // [cite: 390]
		String sql = "INSERT INTO Donations (donation_id, donation_name, display_order) VALUES (?, ?, ?)"; // [cite: 1111]
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, donationId); // [cite: 1112]
			pstmt.setString(2, donationName); // [cite: 1112]
			pstmt.setInt(3, nextOrder); // [cite: 1112]
			int affectedRows = pstmt.executeUpdate(); // [cite: 1113]
			if (affectedRows > 0) {
				// CORRECTED: Add the new Donations object to the internal list
				donationList.add(new Donations(donationId, donationName, nextOrder));
				// Optional: Re-sort the list if order matters immediately
				// donationList.sort(Comparator.comparingInt(Donations::getDisplayOrder));
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error adding Donation to DB (ID: " + donationId + "): " + e.getMessage()); // [cite: 1114]
		}
		return false;
	}

	public boolean deleteDonationFromDB(String donationId) {
		String sql = "DELETE FROM Donations WHERE donation_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, donationId);
			int affectedRows = pstmt.executeUpdate();
			if(affectedRows > 0){
				// Remove from in-memory list if you have a mapping (if using a dedicated Donation model)
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error deleting Donation from DB (ID: " + donationId + "): " + e.getMessage());
		}
		return false;
	}

	public boolean updateDisplayOrder(String donationId, int newOrder) {
		String sql = "UPDATE Donations SET display_order = ? WHERE donation_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, newOrder);
			pstmt.setString(2, donationId);
			int affectedRows = pstmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			System.err.println("Error updating donation display order for " + donationId + ": " + e.getMessage());
			return false;
		}
	}

	public List<Donations> getAllDonations() {
		// Return an unmodifiable list of the correct type
		return Collections.unmodifiableList(new ArrayList<>(this.donationList));
	}

	// You can also add moveDonationUp and moveDonationDown methods similar to those in SevaRepository.
}