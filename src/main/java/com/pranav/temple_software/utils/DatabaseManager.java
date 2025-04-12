// File: Temple_Software/src/main/java/com/pranav/temple_software/utils/DatabaseManager.java
package com.pranav.temple_software.utils;

import java.sql.*;
// Remove unused imports if any: import java.time.LocalDate; import java.util.List;

public class DatabaseManager {
	private static final String DB_URL = "jdbc:h2:./temple_data"; // Store DB file in project root
	private static final String USER = "sa"; //
	private static final String PASS = ""; //

	public DatabaseManager() {
		createReceiptTableIfNotExists(); // Renamed for clarity
		createSevaTableIfNotExists(); // *** ADDED CALL ***
	}

	private void createReceiptTableIfNotExists() {
		// ... (Receipts table creation code remains the same) ...
		String sql = "CREATE TABLE IF NOT EXISTS Receipts(" +
				"receipt_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"devotee_name VARCHAR(255), " +
				"phone_number VARCHAR(20), " +
				"seva_date DATE, " +
				"sevas_details TEXT, " +
				"total_amount DECIMAL(10, 2), " +
				"payment_mode VARCHAR(10), " +
				"timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
		} catch (SQLException e) {
			System.err.println("Error creating Receipts table: " + e.getMessage());
		}
	}

	private void createSevaTableIfNotExists() {
		String sql = "CREATE TABLE IF NOT EXISTS Sevas (" +
				"seva_id VARCHAR(10) PRIMARY KEY, " +
				"seva_name VARCHAR(255) NOT NULL, " +
				"amount DECIMAL(10, 2) NOT NULL)";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("Sevas table checked/created successfully.");
			// *** ADD CALL to populate default data ***
			addInitialSevasIfEmpty(conn);
		} catch (SQLException e) {
			System.err.println("Error creating Sevas table: " + e.getMessage());
		}
	}

	// *** Method reused from ReceiptRepository (Consider centralizing) ***
	private Connection getConnection() throws SQLException { //
		// Ensure H2 driver is loaded (optional, JDBC 4+ auto-loads)
		// try { Class.forName("org.h2.Driver"); } catch (ClassNotFoundException e) { System.err.println("H2 Driver not found!"); }
		return DriverManager.getConnection(DB_URL, USER, PASS); //
	}

	// Optional: Helper to add default sevas only if the table is empty after creation
	private void addInitialSevasIfEmpty(Connection conn) {
		String checkSql = "SELECT COUNT(*) FROM Sevas";
		// Use try-with-resources for Statement as well
		try (Statement checkStmt = conn.createStatement();
		     ResultSet rs = checkStmt.executeQuery(checkSql)) {

			if (rs.next() && rs.getInt(1) == 0) {
				System.out.println("Sevas table is empty. Adding initial 38 sevas...");
				// Use PreparedStatement for efficient batch insertion
				String insertSql = "INSERT INTO Sevas (seva_id, seva_name, amount) VALUES (?, ?, ?)";
				try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

					// Add the 38 Sevas based on your original hardcoded data
					insertStmt.setString(1, "1");
					insertStmt.setString(2, "ಬಲಿವಾಡು");
					insertStmt.setDouble(3, 50.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "2");
					insertStmt.setString(2, "ಪಂಚಾಮೃತಾಭಿಷೇಕ");
					insertStmt.setDouble(3, 30.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "3");
					insertStmt.setString(2, "ರುದ್ರಾಭಿಷೇಕ");
					insertStmt.setDouble(3, 50.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "4");
					insertStmt.setString(2, "ಏಕಾದಶ ರುದ್ರಾಭಿಷೇಕ");
					insertStmt.setDouble(3, 1000.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "5");
					insertStmt.setString(2, "ಕ್ಷೀರಾಭಿಷೇಕ");
					insertStmt.setDouble(3, 20.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "6");
					insertStmt.setString(2, "ಅಷ್ಟೋತ್ತರ ಕುಂಕುಮಾರ್ಚನೆ");
					insertStmt.setDouble(3, 10.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "7");
					insertStmt.setString(2, "ಸಹಸ್ರನಾಮ ಕುಂಕುಮಾರ್ಚನೆ");
					insertStmt.setDouble(3, 200.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "8");
					insertStmt.setString(2, "ಕಾರ್ತಿಕ ಪೂಜೆ");
					insertStmt.setDouble(3, 50.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "9");
					insertStmt.setString(2, "ತ್ರಿಮಧುರ");
					insertStmt.setDouble(3, 30.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "10");
					insertStmt.setString(2, "ಪುಷ್ಪಾಂಜಲಿ");
					insertStmt.setDouble(3, 30.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "11");
					insertStmt.setString(2, "ಹಣ್ಣುಕಾಯಿ");
					insertStmt.setDouble(3, 25.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "12");
					insertStmt.setString(2, "ಶಾಸ್ತಾರ ದೇವರಿಗೆ ಕಾಯಿ");
					insertStmt.setDouble(3, 20.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "13");
					insertStmt.setString(2, "ಪಂಚಕಜ್ಜಾಯ");
					insertStmt.setDouble(3, 100.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "14");
					insertStmt.setString(2, "ಅಪ್ಪಕಜ್ಜಾಯ (1 ಕುಡ್ತೆ )");
					insertStmt.setDouble(3, 20.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "15");
					insertStmt.setString(2, "ಮಂಗಳಾರತಿ");
					insertStmt.setDouble(3, 20.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "16");
					insertStmt.setString(2, "ಕರ್ಪೂರಾರತಿ");
					insertStmt.setDouble(3, 100.00);
					insertStmt.addBatch(); // Note: Amount was 100.00 in original repo for seva 16
					insertStmt.setString(1, "17");
					insertStmt.setString(2, "ತುಪ್ಪದ ನಂದಾದೀಪ");
					insertStmt.setDouble(3, 50.00);
					insertStmt.addBatch(); // Note: Amount was 50.00 in original repo for seva 17
					insertStmt.setString(1, "18");
					insertStmt.setString(2, "ಎಳ್ಳೆಣ್ಣೆ ನಂದಾದೀಪ");
					insertStmt.setDouble(3, 300.00);
					insertStmt.addBatch(); // Note: Amount was 300.00 in original repo for seva 18

					// Seva 19 (Amount was 300 in original repo)
					insertStmt.setString(1, "19");
					insertStmt.setString(2, "ಒಂದು ದಿನದ ಪೂಜೆ");
					insertStmt.setDouble(3, 300.00);
					insertStmt.addBatch();

					// Sevas 20-38
					insertStmt.setString(1, "20");
					insertStmt.setString(2, "ಸರ್ವಸೇವೆ");
					insertStmt.setDouble(3, 200.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "21");
					insertStmt.setString(2, "ಗಣಪತಿ ಹವನ");
					insertStmt.setDouble(3, 250.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "22");
					insertStmt.setString(2, "ದೂರ್ವಾಹೋಮ");
					insertStmt.setDouble(3, 350.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "23");
					insertStmt.setString(2, "ಶನಿ ಪೂಜೆ");
					insertStmt.setDouble(3, 300.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "24");
					insertStmt.setString(2, "ಶನಿ ಜಪ");
					insertStmt.setDouble(3, 250.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "25");
					insertStmt.setString(2, "ರಾಹು ಜಪ");
					insertStmt.setDouble(3, 250.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "26");
					insertStmt.setString(2, "ತುಲಾಭಾರ");
					insertStmt.setDouble(3, 100.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "27");
					insertStmt.setString(2, "ದೀಪಾರಾಧನೆ");
					insertStmt.setDouble(3, 1000.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "28");
					insertStmt.setString(2, "ನೈವೇದ್ಯ ಸಮರ್ಪಣೆ");
					insertStmt.setDouble(3, 100.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "29");
					insertStmt.setString(2, "ಹಾಲು ಪಾಯಸ");
					insertStmt.setDouble(3, 50.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "30");
					insertStmt.setString(2, "ಪಿಂಡಿ ಪಾಯಸ");
					insertStmt.setDouble(3, 100.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "31");
					insertStmt.setString(2, "ಕಠಿಣ ಪಾಯಸ");
					insertStmt.setDouble(3, 125.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "32");
					insertStmt.setString(2, "2 ಕಾಯಿ ಪಾಯಸ");
					insertStmt.setDouble(3, 250.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "33");
					insertStmt.setString(2, "5 ಕಾಯಿ ಪಾಯಸ");
					insertStmt.setDouble(3, 400.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "34");
					insertStmt.setString(2, "ಹೆಸರುಬೇಳೆ ಪಾಯಸ");
					insertStmt.setDouble(3, 400.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "35");
					insertStmt.setString(2, "ನಾಗನಿಗೆ ಹಾಲು ಸಮರ್ಪಣೆ");
					insertStmt.setDouble(3, 30.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "36");
					insertStmt.setString(2, "ನಾಗ ಪೂಜೆ");
					insertStmt.setDouble(3, 200.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "37");
					insertStmt.setString(2, "ನಾಗ ತಂಬಿಲ");
					insertStmt.setDouble(3, 300.00);
					insertStmt.addBatch();
					insertStmt.setString(1, "38");
					insertStmt.setString(2, "ಪವಮಾನ ಅಭಿಷೇಕ");
					insertStmt.setDouble(3, 500.00);
					insertStmt.addBatch();

					// Execute the batch
					insertStmt.executeBatch();
					System.out.println("Initial 38 Sevas added successfully.");

				} // PreparedStatement closed here
			} else {
				System.out.println("Sevas table already contains data. Skipping initial data insertion.");
			}
		} catch (SQLException e) {
			System.err.println("Error during initial Seva data check/insertion: " + e.getMessage());
		}
	}
}