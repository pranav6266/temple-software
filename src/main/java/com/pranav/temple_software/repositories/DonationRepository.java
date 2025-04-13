package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.SevaEntry; // You can reuse this if donation data is similar
import java.sql.*;
import java.util.*;

public class DonationRepository {
	private static final String DB_URL = "jdbc:h2:./temple_data";
	private static final String USER = "sa";
	private static final String PASS = "";

	private static final DonationRepository instance = new DonationRepository();
	private final List<SevaEntry> donationList = new ArrayList<>();

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

	public void loadDonationsFromDB() {
		donationList.clear();
		String sql = "SELECT donation_id, donation_name, display_order FROM Donations ORDER BY display_order";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				String id = rs.getString("donation_id");
				String name = rs.getString("donation_name");
				int order = rs.getInt("display_order");
				// Add to donation list
				donationList.add(new SevaEntry(name, order)); // Amount is no longer used, set it as `0` or any placeholder
			}
			System.out.println("Loaded " + donationList.size() + " donations from DB.");
		} catch (SQLException e) {
			System.err.println("Error loading Donations from database: " + e.getMessage());
		}
	}


	public boolean addDonationToDB(String donationId, String donationName, double amount) {
		// Use the next display order (could be the maxDonationId+1)
		int nextOrder = getMaxDonationId() + 1;
		String sql = "INSERT INTO Donations (donation_id, donation_name, display_order) VALUES (?, ?, ?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, donationId);
			pstmt.setString(2, donationName);

			pstmt.setInt(3, nextOrder);

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				donationList.add(new SevaEntry(donationName, amount));
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error adding Donation to DB (ID: " + donationId + "): " + e.getMessage());
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

	public List<SevaEntry> getAllDonations() {
		return Collections.unmodifiableList(new ArrayList<>(donationList));
	}

	// You can also add moveDonationUp and moveDonationDown methods similar to those in SevaRepository.
}