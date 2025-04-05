package com.pranav.temple_software.utils;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class DatabaseManager {
	private static final String DB_URL = "jdbc:h2:./temple_data"; // Store DB file in project root
	private static final String USER = "sa";
	private static final String PASS = "";

	public DatabaseManager() {
		createTableIfNotExists();
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER, PASS);
	}

	private void createTableIfNotExists() {
		String sql = "CREATE TABLE IF NOT EXISTS Receipts (" +
				"receipt_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"devotee_name VARCHAR(255), " +
				"phone_number VARCHAR(20), " +
				"seva_date DATE, " +
				"sevas_details TEXT, " + // Store as JSON or delimited string
				"total_amount DECIMAL(10, 2), " +
				"payment_mode VARCHAR(10), " +
				"timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
		} catch (SQLException e) {
			System.err.println("Error creating table: " + e.getMessage());
			// Handle exception appropriately
		}
	}

	public int saveReceipt(String name, String phone, LocalDate date, String sevasDetails, double total, String paymentMode) {
		String sql = "INSERT INTO Receipts (devotee_name, " +
		"phone_number, seva_date, sevas_details," +
		" total_amount, payment_mode) VALUES (?, ?, ?, ?, ?, ?)";
		int generatedId = -1;

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
			System.err.println("Error inserting receipt: " + e.getMessage());
			return -1;
		}
		return generatedId;
	}
	// Add methods to format SevaEntry list to String (e.g., JSON) if needed
}