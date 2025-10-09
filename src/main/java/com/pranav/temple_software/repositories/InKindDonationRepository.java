package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.InKindDonation;
import com.pranav.temple_software.utils.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

public class InKindDonationRepository {
	private static final Logger logger = LoggerFactory.getLogger(InKindDonationRepository.class);

	private Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	public boolean saveInKindDonation(InKindDonation donation) {
		String sql = "INSERT INTO InKindDonations (devotee_name, phone_number, address, pan_number, rashi, nakshatra, donation_date, item_description) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, donation.getDevoteeName());
			pstmt.setString(2, donation.getPhoneNumber());
			pstmt.setString(3, donation.getAddress());
			pstmt.setString(4, donation.getPanNumber());
			pstmt.setString(5, donation.getRashi());
			pstmt.setString(6, donation.getNakshatra());
			pstmt.setDate(7, Date.valueOf(donation.getDonationDate()));
			pstmt.setString(8, donation.getItemDescription());
			pstmt.setString(9, donation.getPaymentMode());

			int affectedRows = pstmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			logger.error("Error saving in-kind donation to database", e);
			return false;
		}
	}

	public ArrayList<InKindDonation> getAllInKindDonations() {
		ArrayList<InKindDonation> donations = new ArrayList<>();
		String sql = "SELECT * FROM InKindDonations ORDER BY in_kind_receipt_id DESC";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				donations.add(new InKindDonation(
						rs.getInt("in_kind_receipt_id"),
						rs.getString("devotee_name"),
						rs.getString("phone_number"),
						rs.getString("address"),
						rs.getString("pan_number"),
						rs.getString("rashi"),
						rs.getString("nakshatra"),
						rs.getDate("donation_date").toLocalDate(),
						rs.getString("item_description"),
						rs.getString("payment_mode")
				));
			}
		} catch (SQLException e) {
			logger.error("Error fetching in-kind donations", e);
		}
		return donations;
	}

	public int getNextReceiptId() {
		String sql = "SELECT MAX(in_kind_receipt_id) FROM InKindDonations";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()) {
				return rs.getInt(1) + 1;
			}
		} catch (SQLException e) {
			logger.error("Error fetching next in-kind donation ID", e);
		}
		return 1; // Default to 1 if table is empty
	}
}