package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.DonationReceiptData;
import com.pranav.temple_software.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DonationReceiptRepository {
	private static final String DB_URL = DatabaseManager.DB_URL;
	private static final String USER = "sa";
	private static final String PASS = "";

	private static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER, PASS);
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

			System.out.println("✅ Loaded " + donationReceipts.size() + " donation receipts from database.");
		} catch (SQLException e) {
			System.err.println("❌ SQL error while fetching donation receipts: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("❌ Unexpected error while fetching donation receipts: " + e.getMessage());
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
			System.err.println("Error inserting donation receipt: " + e.getMessage());
			return -1;
		}
		return generatedId;
	}
}