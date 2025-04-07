// Inside Temple_Software/src/main/java/com/pranav/temple_software/repositories/ReceiptRepository.java
package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.ReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class ReceiptRepository {
	private static final String DB_URL = "jdbc:h2:./temple_data"; // Store DB file in project root
	private static final String USER = "sa";
	private static final String PASS = "";
	private static final String H2_PK_VIOLATION_STATE = "23505"; // H2 specific SQLState for unique constraint violation


	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER, PASS);
	}

	/**
	 * Gets the next potential receipt ID based on the current maximum ID in the database.
	 * Does NOT reserve the ID.
	 * @return The next potential receipt ID (max_id + 1), or 1 if the table is empty, or -1 on error.
	 */
	public int getNextReceiptId() {
		String sql = "SELECT MAX(receipt_id) FROM Receipts";
		int nextId = 1; // Default if table is empty

		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			if (rs.next()) {
				int maxId = rs.getInt(1);
				if (!rs.wasNull()) { // Check if MAX returned a value (table not empty)
					nextId = maxId + 1;
				}
			}
		} catch (SQLException e) {
			System.err.println("Error fetching next receipt ID: " + e.getMessage());
			return -1; // Indicate error
		}
		return nextId;
	}


	/**
	 * Attempts to save a receipt with a SPECIFIC ID.
	 * If that ID is already taken (Primary Key violation), it falls back to saving
	 * with an auto-generated ID using the original saveReceipt method.
	 * @param id The desired receipt ID.
	 * @param name Devotee name.
	 * @param phone Phone number.
	 * @param date Seva date.
	 * @param sevasDetails Formatted string of sevas.
	 * @param total Total amount.
	 * @param paymentMode Payment mode.
	 * @return The actual ID the receipt was saved with, or -1 on failure.
	 */
	public int saveSpecificReceipt(int id, String name, String phone, LocalDate date,
	                               String sevasDetails, double total, String paymentMode) {

		String sql = "INSERT INTO Receipts (receipt_id, devotee_name, " +
				"phone_number, seva_date, sevas_details," +
				" total_amount, payment_mode) VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) { // No RETURN_GENERATED_KEYS needed here initially

			pstmt.setInt(1, id); // Try inserting with the specific ID
			pstmt.setString(2, name);
			pstmt.setString(3, phone);
			pstmt.setDate(4, java.sql.Date.valueOf(date));
			pstmt.setString(5, sevasDetails);
			pstmt.setDouble(6, total);
			pstmt.setString(7, paymentMode);

			int affectedRows = pstmt.executeUpdate();

			if (affectedRows > 0) {
				return id; // Successfully inserted with the specific ID
			} else {
				// This case shouldn't normally happen if executeUpdate doesn't throw, but handle defensively
				System.err.println("Error inserting receipt with specific ID " + id + ": No rows affected, but no exception.");
				// Fall through to fallback might be risky here, maybe just return -1?
				// For now, let's try the fallback.
				System.err.println("Attempting fallback to auto-increment save.");
				return saveReceipt(name, phone, date, sevasDetails, total, paymentMode); // Fallback
			}

		} catch (SQLException e) {
			// Check if the error is a Primary Key violation
			if (H2_PK_VIOLATION_STATE.equals(e.getSQLState())) {
				System.out.println("Receipt ID " + id + " already exists. Falling back to auto-increment ID.");
				// Fallback: Use the original method which gets an auto-generated key
				return saveReceipt(name, phone, date, sevasDetails, total, paymentMode);
			} else {
				// Different SQL error during specific insert attempt
				System.err.println("Error inserting receipt with specific ID " + id + ": " + e.getMessage() + " (SQLState: " + e.getSQLState() + ")");
				return -1; // Indicate failure
			}
		}
	}


	/**
	 * Original save method using auto-increment. (Keep this as the fallback)
	 * @return Generated ID or -1 on failure.
	 */
	public int saveReceipt(String name, String phone, LocalDate date,
	                       String sevasDetails, double total, String paymentMode) {

		String sql = "INSERT INTO Receipts (devotee_name, " +
				"phone_number, seva_date, sevas_details," +
				" total_amount, payment_mode) VALUES (?, ?, ?, ?, ?, ?)";
		int generatedId = -1;

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Use RETURN_GENERATED_KEYS

			// Set parameters starting from index 1
			pstmt.setString(1, name);
			pstmt.setString(2, phone);
			pstmt.setDate(3, java.sql.Date.valueOf(date));
			pstmt.setString(4, sevasDetails); // Pass the formatted string
			pstmt.setDouble(5, total);
			pstmt.setString(6, paymentMode);

			int affectedRows = pstmt.executeUpdate();

			if (affectedRows > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						generatedId = generatedKeys.getInt(1);
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("Error inserting auto-increment receipt: " + e.getMessage());
			return -1; // Return -1 specifically on error
		}
		return generatedId;
	}


	// Keep getAllReceipts and parseSevas methods as they are
	public List<ReceiptData> getAllReceipts() {
		String sql = "SELECT * FROM Receipts";
		List<ReceiptData> receipts = new ArrayList<>();

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				receipts.add(new ReceiptData(
						rs.getInt("receipt_id"),
						rs.getString("devotee_name"),
						rs.getString("phone_number"),
						"", "", // rashi and nakshatra not stored currently
						rs.getDate("seva_date").toLocalDate(),
						parseSevas(rs.getString("sevas_details")),
						rs.getDouble("total_amount")
				));
			}
		} catch (SQLException e) {
			System.err.println("Error fetching receipts: " + e.getMessage());
		}
		return receipts;
	}

	private ObservableList<SevaEntry> parseSevas(String sevasString) {
		ObservableList<SevaEntry> sevas = FXCollections.observableArrayList();

		if (sevasString == null || sevasString.isEmpty()) {
			return sevas;
		}

		String[] entries = sevasString.split(";");
		for (String entry : entries) {
			try {
				String[] parts = entry.split(":", 2);
				if (parts.length == 2) {
					String name = parts[0].trim();
					double amount = Double.parseDouble(parts[1].trim());
					sevas.add(new SevaEntry(name, amount));
				}
			} catch (NumberFormatException | NullPointerException e) {
				System.err.println("Error parsing seva entry: " + entry);
				sevas.add(new SevaEntry(entry.split(":")[0].trim(), 0.0));
			}
		}
		return sevas;
	}
}