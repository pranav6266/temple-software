package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.ShashwathaPoojaReceipt;
import com.pranav.temple_software.utils.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShashwathaPoojaRepository {
	private static final Logger logger = LoggerFactory.getLogger(ShashwathaPoojaRepository.class);

	private Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	public boolean saveShashwathaPooja(ShashwathaPoojaReceipt receipt) {
		String sql = "INSERT INTO ShashwathaPoojaReceipts (devotee_name, phone_number, address, pan_number, rashi, nakshatra, receipt_date, pooja_date, amount, payment_mode) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, receipt.getDevoteeName());
			pstmt.setString(2, receipt.getPhoneNumber());
			pstmt.setString(3, receipt.getAddress());
			pstmt.setString(4, receipt.getPanNumber());
			pstmt.setString(5, receipt.getRashi());
			pstmt.setString(6, receipt.getNakshatra());
			pstmt.setDate(7, Date.valueOf(receipt.getReceiptDate()));
			pstmt.setString(8, receipt.getPoojaDate());
			pstmt.setDouble(9, receipt.getAmount());
			pstmt.setString(10, receipt.getPaymentMode());

			int affectedRows = pstmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			logger.error("Error saving Shashwatha Pooja to database", e);
			return false;
		}
	}

	public List<ShashwathaPoojaReceipt> getAllShashwathaPoojaReceipts() {
		List<ShashwathaPoojaReceipt> receipts = new ArrayList<>();
		String sql = "SELECT * FROM ShashwathaPoojaReceipts ORDER BY receipt_id DESC";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				receipts.add(new ShashwathaPoojaReceipt(
						rs.getInt("receipt_id"),
						rs.getString("devotee_name"),
						rs.getString("phone_number"),
						rs.getString("address"),
						rs.getString("pan_number"),
						rs.getString("rashi"),
						rs.getString("nakshatra"),
						rs.getDate("receipt_date").toLocalDate(),
						rs.getString("pooja_date"),
						rs.getDouble("amount"),
						rs.getString("payment_mode")
				));
			}
		} catch (SQLException e) {
			logger.error("Error fetching Shashwatha Pooja receipts", e);
		}
		return receipts;
	}
}