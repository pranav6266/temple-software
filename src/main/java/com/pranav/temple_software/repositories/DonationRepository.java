package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.Donations;
import com.pranav.temple_software.utils.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class DonationRepository {
	private static final Logger logger = LoggerFactory.getLogger(DonationRepository.class);

	private static final DonationRepository instance = new DonationRepository();
	private final List<Donations> donationList = new ArrayList<>();
	private boolean isDataLoaded = false;

	private DonationRepository() {
	}

	public static DonationRepository getInstance() {
		return instance;
	}

	private Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	public synchronized void loadDonationsFromDB() {
		donationList.clear();
		isDataLoaded = false;

		String sql = "SELECT donation_id, donation_name, display_order FROM Donations ORDER BY display_order";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				String id = rs.getString("donation_id");
				String name = rs.getString("donation_name");
				rs.getInt("display_order");
				donationList.add(new Donations(id, name));
			}
			isDataLoaded = true;
			logger.info("✅ Loaded {} donations from database.", donationList.size());
		} catch (SQLException e) {
			logger.error("❌ Error loading Donations from database", e);
		}
	}

	public synchronized List<Donations> getAllDonations() {
		if (!isDataLoaded) {
			loadDonationsFromDB();
		}
		return List.copyOf(this.donationList);
	}

	public synchronized int getMaxDonationId() {
		String sql = "SELECT MAX(CAST(donation_id AS INT)) FROM Donations";
		int maxId = 0;
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()){
				maxId = rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("Error fetching max Donation ID", e);
		}
		return maxId;
	}

	public synchronized String getDonationIdByName(String name) {
		String sql = "SELECT donation_id FROM Donations WHERE donation_name = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getString("donation_id");
			}
		} catch (SQLException e) {
			logger.error("Error fetching donation ID for name: {}", name, e);
		}
		return null;
	}

	public synchronized boolean addDonationToDB(String donationId, String donationName) {
		int nextOrder = getMaxDonationId() + 1;
		String sql = "INSERT INTO Donations (donation_id, donation_name, display_order) VALUES (?, ?, ?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, donationId);
			pstmt.setString(2, donationName);
			pstmt.setInt(3, nextOrder);
			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				donationList.add(new Donations(donationId, donationName));
				return true;
			}
		} catch (SQLException e) {
			logger.error("Error adding Donation to DB (ID: {})", donationId, e);
		}
		return false;
	}

	public synchronized boolean deleteDonationFromDB(String donationId) {
		String sql = "DELETE FROM Donations WHERE donation_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, donationId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error deleting Donation from DB (ID: {})", donationId, e);
		}
		return false;
	}

	public synchronized boolean updateDisplayOrder(String donationId, int newOrder) {
		String sql = "UPDATE Donations SET display_order = ? WHERE donation_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, newOrder);
			pstmt.setString(2, donationId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error updating donation display order for {}", donationId, e);
			return false;
		}
	}
}