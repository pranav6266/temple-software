package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.KaryakramaReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KaryakramaReceiptRepository {
	private Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	public int saveReceipt(KaryakramaReceiptData data) {
		String sql = "INSERT INTO KaryakramaReceipts (devotee_name, phone_number, address, pan_number, rashi, nakshatra, receipt_date, sevas_details, total_amount, payment_mode) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, data.getDevoteeName());
			pstmt.setString(2, data.getPhoneNumber());
			pstmt.setString(3, data.getAddress());
			pstmt.setString(4, data.getPanNumber());
			pstmt.setString(5, data.getRashi());
			pstmt.setString(6, data.getNakshatra());
			pstmt.setDate(7, Date.valueOf(data.getReceiptDate()));
			pstmt.setString(8, formatSevasForDatabase(data.getSevas()));
			pstmt.setDouble(9, data.getTotalAmount());
			pstmt.setString(10, data.getPaymentMode());

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						return generatedKeys.getInt(1);
					}
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public List<KaryakramaReceiptData> getAllReceipts() {
		List<KaryakramaReceiptData> receipts = new ArrayList<>();
		String sql = "SELECT * FROM KaryakramaReceipts ORDER BY receipt_id DESC";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				receipts.add(new KaryakramaReceiptData(
						rs.getInt("receipt_id"),
						rs.getString("devotee_name"),
						rs.getString("phone_number"),
						rs.getString("address"),
						rs.getString("pan_number"),
						rs.getString("rashi"),
						rs.getString("nakshatra"),
						rs.getDate("receipt_date").toLocalDate(),
						parseSevas(rs.getString("sevas_details")),
						rs.getDouble("total_amount"),
						rs.getString("payment_mode")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return receipts;
	}

	private String formatSevasForDatabase(List<SevaEntry> sevas) {
		StringBuilder sb = new StringBuilder();
		for (SevaEntry seva : sevas) {
			sb.append(seva.getName()).append(":")
					.append(seva.getAmount()).append(":")
					.append(seva.getQuantity()).append(";");
		}
		if(sb.length() > 0) sb.setLength(sb.length() - 1); // Remove trailing semicolon
		return sb.toString();
	}

	private List<SevaEntry> parseSevas(String sevasString) {
		List<SevaEntry> sevas = new ArrayList<>();
		if (sevasString == null || sevasString.isEmpty()) {
			return sevas;
		}
		String[] entries = sevasString.split(";");
		for (String entry : entries) {
			try {
				String[] parts = entry.split(":");
				if (parts.length >= 3) {
					String name = String.join(":", Arrays.copyOfRange(parts, 0, parts.length - 2)).trim();
					double amount = Double.parseDouble(parts[parts.length - 2].trim());
					int quantity = Integer.parseInt(parts[parts.length - 1].trim());
					SevaEntry seva = new SevaEntry(name, amount);
					seva.setQuantity(quantity);
					sevas.add(seva);
				}
			} catch (Exception e) {
				System.err.println("Could not parse Karyakrama seva entry: " + entry);
			}
		}
		return sevas;
	}
}