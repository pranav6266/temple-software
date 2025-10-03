package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.DonationReceiptData;
import com.pranav.temple_software.utils.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DonationReceiptRepository {
	private static final Logger logger = LoggerFactory.getLogger(DonationReceiptRepository.class);

	private static Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	public List<DonationReceiptData> getAllDonationReceipts() {
		List<DonationReceiptData> donationReceipts = new ArrayList<>();
		String sql = "SELECT * FROM DonationReceipts ORDER BY donation_receipt_id DESC";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				int donationReceiptId = rs.getInt("donation_receipt_id");
				String devoteeName = rs.getString("devotee_name");
				String phoneNumber = rs.getString("phone_number");
				String address = rs.getString("address");
				String panNumber = rs.getString("pan_number");
				String rashi = rs.getString("rashi");
				String nakshatra = rs.getString("nakshatra");
				LocalDate sevaDate = rs.getDate("seva_date").toLocalDate();
				String donationName = rs.getString("donation_name");
				double donationAmount = rs.getDouble("donation_amount");
				String paymentMode = rs.getString("payment_mode");

				DonationReceiptData donationReceipt = new DonationReceiptData(
						donationReceiptId, devoteeName, phoneNumber, address, panNumber, rashi, nakshatra,
						sevaDate, donationName, donationAmount, paymentMode
				);
				donationReceipts.add(donationReceipt);
			}
			logger.info("✅ Loaded {} donation receipts from database.", donationReceipts.size());
		} catch (SQLException e) {
			logger.error("❌ SQL error while fetching donation receipts", e);
		} catch (Exception e) {
			logger.error("❌ Unexpected error while fetching donation receipts", e);
		}
		return donationReceipts;
	}

	public int saveDonationReceipt(String name, String phone, String address, String panNumber,
	                               String rashi, String nakshatra, LocalDate date,
	                               String donationName, double amount, String paymentMode) {
		String sql = "INSERT INTO DonationReceipts (devotee_name, phone_number, " +
				"address, pan_number, rashi, nakshatra, seva_date, donation_name, donation_amount, payment_mode) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		int generatedId = -1;
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, name);
			pstmt.setString(2, phone);
			pstmt.setString(3, address);
			pstmt.setString(4, panNumber);
			pstmt.setString(5, rashi);
			pstmt.setString(6, nakshatra);
			pstmt.setDate(7, java.sql.Date.valueOf(date));
			pstmt.setString(8, donationName);
			pstmt.setDouble(9, amount);
			pstmt.setString(10, paymentMode);

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						generatedId = generatedKeys.getInt(1);
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Error inserting donation receipt", e);
			return -1;
		}
		return generatedId;
	}

	public int getNextReceiptId() {
		String sql = "SELECT MAX(donation_receipt_id) FROM DonationReceipts";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()) {
				return rs.getInt(1) + 1;
			}
		} catch (SQLException e) {
			logger.error("Error fetching next donation receipt ID", e);
		}
		return 1; // Default to 1 if table is empty
	}
}