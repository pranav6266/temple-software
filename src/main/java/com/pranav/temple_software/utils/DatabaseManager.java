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

}