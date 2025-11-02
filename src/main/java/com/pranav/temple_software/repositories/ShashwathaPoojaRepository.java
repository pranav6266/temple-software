package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.HistoryFilterCriteria;
import com.pranav.temple_software.models.ShashwathaPoojaReceipt;
import com.pranav.temple_software.utils.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ShashwathaPoojaRepository {
	private static final Logger logger = LoggerFactory.getLogger(ShashwathaPoojaRepository.class);

	private Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	public int saveShashwathaPooja(ShashwathaPoojaReceipt receipt) {
		String sql = "INSERT INTO ShashwathaPoojaReceipts (devotee_name, phone_number, address, pan_number, rashi, nakshatra, receipt_date, pooja_date, amount, payment_mode) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		int generatedId = -1;
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
			if (affectedRows > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						generatedId = generatedKeys.getInt(1); // Get the generated ID
					}
				}
			}
			return generatedId; // Return the ID
		} catch (SQLException e) {
			logger.error("Error saving Shashwatha Pooja to database", e);
			return -1; // Return -1 on error
		}
	}

	public List<ShashwathaPoojaReceipt> getFilteredShashwathaPoojaReceipts(HistoryFilterCriteria criteria) {
		List<ShashwathaPoojaReceipt> receipts = new ArrayList<>();
		List<Object> parameters = new ArrayList<>();

		StringBuilder sql = new StringBuilder("SELECT * FROM ShashwathaPoojaReceipts WHERE 1=1 ");

		if (criteria.getDevoteeName() != null) {
			sql.append("AND devotee_name LIKE ? ");
			parameters.add("%" + criteria.getDevoteeName() + "%");
		}
		if (criteria.getPhoneNumber() != null) {
			sql.append("AND phone_number LIKE ? ");
			parameters.add("%" + criteria.getPhoneNumber() + "%");
		}
		if (criteria.getReceiptId() != null) {
			sql.append("AND receipt_id = ? ");
			try {
				parameters.add(Integer.parseInt(criteria.getReceiptId()));
			} catch (NumberFormatException e) {
				parameters.add(0);
			}
		}
		if (criteria.getFromDate() != null) {
			sql.append("AND receipt_date >= ? ");
			parameters.add(java.sql.Date.valueOf(criteria.getFromDate()));
		}
		if (criteria.getToDate() != null) {
			sql.append("AND receipt_date <= ? ");
			parameters.add(java.sql.Date.valueOf(criteria.getToDate()));
		}

		sql.append("ORDER BY receipt_id DESC");

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < parameters.size(); i++) {
				pstmt.setObject(i + 1, parameters.get(i));
			}

			try (ResultSet rs = pstmt.executeQuery()) {
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
			}
		} catch (SQLException e) {
			logger.error("Error fetching filtered Shashwatha Pooja receipts", e);
		}
		return receipts;
	}
}