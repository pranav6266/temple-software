package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.Donations;
import java.sql.*;
import java.util.*;

public class DonationRepository {
	private static final String DB_URL = "jdbc:h2:./temple_data";
	private static final String USER = "sa";
	private static final String PASS = "";

	private static final DonationRepository instance = new DonationRepository();
	private final List<Donations> donationList = new ArrayList<>();
	// A flag to ensure we only load from the DB once.
	private boolean isDataLoaded = false;

	private DonationRepository() {
		// Data is no longer loaded in the constructor.
	}

	public static DonationRepository getInstance() {
		return instance;
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER, PASS);
	}

	public synchronized void loadDonationsFromDB() {
		// Only load if data hasn't been loaded yet.
		if (isDataLoaded) {
			return;
		}
		donationList.clear();
		String sql = "SELECT donation_id, donation_name, display_order FROM Donations ORDER BY display_order";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				String id = rs.getString("donation_id");
				String name = rs.getString("donation_name");
				int order = rs.getInt("display_order");
				donationList.add(new Donations(id, name, order));
			}
			isDataLoaded = true; // Mark data as loaded
			System.out.println("Loaded " + donationList.size() + " donations from DB.");
		} catch (SQLException e) {
			System.err.println("Error loading Donations from database: " + e.getMessage());
		}
	}

	public List<Donations> getAllDonations() {
		// *** KEY CHANGE ***
		// If data is not loaded, load it first (lazy-loading).
		if (!isDataLoaded) {
			loadDonationsFromDB();
		}
		return Collections.unmodifiableList(new ArrayList<>(this.donationList));
	}

	// ... The rest of your DonationRepository methods remain the same ...
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

	public boolean addDonationToDB(String donationId, String donationName, double amount) {
		int nextOrder = getMaxDonationId() + 1;
		String sql = "INSERT INTO Donations (donation_id, donation_name, display_order) VALUES (?, ?, ?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, donationId);
			pstmt.setString(2, donationName);
			pstmt.setInt(3, nextOrder);
			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				donationList.add(new Donations(donationId, donationName, nextOrder));
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
}
