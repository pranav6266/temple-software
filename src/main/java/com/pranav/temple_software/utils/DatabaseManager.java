package com.pranav.temple_software.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class DatabaseManager {
	public static final String APP_DATA_FOLDER = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "TempleSoftware";
	public static final Path DB_FOLDER_PATH = Paths.get(APP_DATA_FOLDER, "db");

	// FIXED: Simplified connection string without problematic parameters
	public static final String DB_URL = "jdbc:h2:" + DB_FOLDER_PATH.toString() + File.separator + "temple_data";

	private static final String USER = "sa";
	private static final String PASS = "";

	public DatabaseManager() {
		try {
			// Create directories if they don't exist
			Files.createDirectories(DB_FOLDER_PATH);

			// Load H2 driver explicitly
			Class.forName("org.h2.Driver");

			// Create tables with PAN column included from the start
			createTablesWithPanColumn();

			// Initialize credentials
			initializeCredentials();

		} catch (Exception e) {
			System.err.println("❌ Error initializing database: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void createTablesWithPanColumn() {
		try (Connection conn = getConnection()) {
			createSevaTableIfNotExists(conn);
			createOthersTableIfNotExists(conn);
			createVisheshaPoojeTableIfNotExists(conn);
			createDonationsTableIfNotExists(conn);
			createReceiptTableIfNotExists(conn);
			createDonationReceiptTableIfNotExists(conn);
			createInKindDonationTableIfNotExists(conn);
			createShashwathaPoojaTableIfNotExists(conn);
			createCredentialsTableIfNotExists(conn);

		} catch (SQLException e) {
			System.err.println("❌ Error creating tables: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void createCredentialsTableIfNotExists(Connection conn) {
		String sql = "CREATE TABLE IF NOT EXISTS Credentials (" +
				"credential_key VARCHAR(50) PRIMARY KEY, " +
				"credential_value TEXT NOT NULL)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ Credentials table checked/created successfully.");
		} catch (SQLException e) {
			System.err.println("❌ Error creating Credentials table: " + e.getMessage());
		}
	}

	private void createInKindDonationTableIfNotExists(Connection conn) {
		String sql = "CREATE TABLE IF NOT EXISTS InKindDonations (" +
				"in_kind_receipt_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"devotee_name VARCHAR(255), " +
				"phone_number VARCHAR(20), " +
				"address TEXT, " +
				"pan_number VARCHAR(10), " +
				"rashi VARCHAR(20), " +
				"nakshatra VARCHAR(20), " +
				"donation_date DATE, " +
				"item_description TEXT, " +
				"timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ InKindDonations table checked/created successfully.");
		} catch (SQLException e) {
			System.err.println("❌ Error creating InKindDonations table: " + e.getMessage());
		}
	}

	private void createShashwathaPoojaTableIfNotExists(Connection conn) {
		String sql = "CREATE TABLE IF NOT EXISTS ShashwathaPoojaReceipts (" +
				"receipt_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"devotee_name VARCHAR(255), " +
				"phone_number VARCHAR(20), " +
				"address TEXT, " +
				"pan_number VARCHAR(10), " +
				"rashi VARCHAR(20), " +
				"nakshatra VARCHAR(20), " +
				"receipt_date DATE, " +
				"pooja_date TEXT, " +
				"timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ ShashwathaPoojaReceipts table checked/created successfully.");
		} catch (SQLException e) {
			System.err.println("❌ Error creating ShashwathaPoojaReceipts table: " + e.getMessage());
		}
	}

	private void initializeCredentials() {
		try (Connection conn = getConnection()) {
			// Check if credentials exist
			String checkSql = "SELECT COUNT(*) FROM Credentials";
			try (Statement stmt = conn.createStatement();
			     ResultSet rs = stmt.executeQuery(checkSql)) {

				if (rs.next() && rs.getInt(1) == 0) {
					// No credentials exist, create defaults
					String insertSql = "INSERT INTO Credentials (credential_key, credential_value) VALUES (?, ?)";
					try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
						// Default passwords (you should change these)
						pstmt.setString(1, "NORMAL_PASSWORD");
						pstmt.setString(2, "$2a$12$xKvn8qF5rYTK4YQHxkp9Q.RgWTzR8w7fZJ8nKQxQzPJd6wJcV/k6."); // "password"
						pstmt.executeUpdate();

						pstmt.setString(1, "SPECIAL_PASSWORD");
						pstmt.setString(2, "$2a$12$xKvn8qF5rYTK4YQHxkp9Q.RgWTzR8w7fZJ8nKQxQzPJd6wJcV/k6."); // "password"
						pstmt.executeUpdate();

						pstmt.setString(1, "ADMIN_USERNAME");
						pstmt.setString(2, "admin");
						pstmt.executeUpdate();

						pstmt.setString(1, "ADMIN_PASSWORD");
						pstmt.setString(2, "$2a$12$xKvn8qF5rYTK4YQHxkp9Q.RgWTzR8w7fZJ8nKQxQzPJd6wJcV/k6."); // "password"
						pstmt.executeUpdate();

						System.out.println("✅ Default credentials initialized.");
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("❌ Error initializing credentials: " + e.getMessage());
		}
	}

	private void createReceiptTableIfNotExists(Connection conn) {
		String sql = "CREATE TABLE IF NOT EXISTS Receipts(" +
				"receipt_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"devotee_name VARCHAR(255), " +
				"phone_number VARCHAR(20), " +
				"address TEXT, " +
				"pan_number VARCHAR(10), " +
				"rashi VARCHAR(20), " +
				"nakshatra VARCHAR(20), " +
				"seva_date DATE, " +
				"sevas_details TEXT, " +
				"total_amount DECIMAL(10, 2), " +
				"payment_mode VARCHAR(10), " +
				"timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ Receipts table checked/created successfully.");
		} catch (SQLException e) {
			System.err.println("❌ Error creating Receipts table: " + e.getMessage());
		}
	}

	private void createDonationReceiptTableIfNotExists(Connection conn) {
		String sql = "CREATE TABLE IF NOT EXISTS DonationReceipts(" +
				"donation_receipt_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"devotee_name VARCHAR(255), " +
				"phone_number VARCHAR(20), " +
				"address TEXT, " +
				"pan_number VARCHAR(10), " +
				"rashi VARCHAR(20), " +
				"nakshatra VARCHAR(20), " +
				"seva_date DATE, " +
				"donation_name VARCHAR(255), " +
				"donation_amount DECIMAL(10, 2), " +
				"payment_mode VARCHAR(10), " +
				"print_status VARCHAR(20) DEFAULT 'SUCCESS', " +
				"timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ DonationReceipts table checked/created successfully.");
		} catch (SQLException e) {
			System.err.println("❌ Error creating DonationReceipts table: " + e.getMessage());
		}
	}

	private void createSevaTableIfNotExists(Connection conn) {
		String sql = "CREATE TABLE IF NOT EXISTS Sevas (" +
				"seva_id VARCHAR(10) PRIMARY KEY, " +
				"seva_name VARCHAR(255) NOT NULL, " +
				"amount DECIMAL(10, 2) NOT NULL, " +
				"display_order INT)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ Sevas table checked/created successfully.");
			addInitialSevasIfEmpty(conn);
			ensureDisplayOrderExists(conn);
		} catch (SQLException e) {
			System.err.println("❌ Error creating Sevas table: " + e.getMessage());
		}
	}

	private void createOthersTableIfNotExists(Connection conn) {
		String sql = "CREATE TABLE IF NOT EXISTS Others (" +
				"others_id VARCHAR(10) PRIMARY KEY, " +
				"others_name VARCHAR(100) NOT NULL, " +
				"others_amount DECIMAL(10,2) DEFAULT 0, " +
				"display_order INT NOT NULL)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ Others table checked/created successfully.");
			addInitialOthersIfEmpty(conn);
		} catch (SQLException e) {
			System.err.println("❌ Error creating Others table: " + e.getMessage());
		}
	}

	private void createVisheshaPoojeTableIfNotExists(Connection conn) {
		String sql = "CREATE TABLE IF NOT EXISTS VisheshaPooje (" +
				"vishesha_pooje_id VARCHAR(10) PRIMARY KEY, " +
				"vishesha_pooje_name VARCHAR(100) NOT NULL, " +
				"vishesha_pooje_amount DECIMAL(10,2) DEFAULT 0, " +
				"display_order INT NOT NULL)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ VisheshaPooje table checked/created successfully.");
		} catch (SQLException e) {
			System.err.println("❌ Error creating VisheshaPooje table: " + e.getMessage());
		}
	}

	private void addInitialOthersIfEmpty(Connection conn) {
		// This method is intentionally empty as per requirements.
	}

	private void ensureDisplayOrderExists(Connection conn) {
		String sql = "UPDATE Sevas SET display_order = CAST(seva_id AS INT) WHERE display_order IS NULL";
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
		} catch (SQLException | NumberFormatException e) {
			// Ignore potential errors if seva_id is not a number
		}
	}

	private void createDonationsTableIfNotExists(Connection conn) {
		String sql = "CREATE TABLE IF NOT EXISTS Donations (" +
				"donation_id VARCHAR(10) PRIMARY KEY, " +
				"donation_name VARCHAR(255) NOT NULL, " +
				"display_order INT)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ Donations table checked/created successfully.");
			addInitialDonationsIfEmpty(conn);
			ensureDisplayOrderExistsForDonations(conn);
		} catch (SQLException e) {
			System.err.println("❌ Error creating Donations table: " + e.getMessage());
		}
	}

	private void addInitialDonationsIfEmpty(Connection conn) {
		// This method is intentionally empty as per requirements.
	}

	private void ensureDisplayOrderExistsForDonations(Connection conn) {
		String sql = "UPDATE Donations SET display_order = CAST(donation_id AS INT) WHERE display_order IS NULL";
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
		} catch (SQLException | NumberFormatException e) {
			// Ignore potential errors if donation_id is not a number
		}
	}

	private void addInitialSevasIfEmpty(Connection conn) {
		// This method is intentionally empty as per requirements.
	}

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER, PASS);
	}

	public static void closeConnection() {
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			stmt.execute("SHUTDOWN");
			System.out.println("✅ Database shutdown command issued.");
		} catch (SQLException e) {
			if (!"08006".equals(e.getSQLState())) {
				System.err.println("❌ Error during database shutdown: " + e.getMessage());
			} else {
				System.out.println("✅ Database connection closed successfully.");
			}
		}
	}
}
