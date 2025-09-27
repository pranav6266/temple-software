package com.pranav.temple_software.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class DatabaseManager {
	public static final String APP_DATA_FOLDER = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "TempleSoftware";
	public static final Path DB_FOLDER_PATH = Paths.get(APP_DATA_FOLDER, "db");
	public static final String DB_URL = "jdbc:h2:" + DB_FOLDER_PATH.toString() + File.separator + "temple_data";
	private static final String USER = "sa";
	private static final String PASS = "";

	public DatabaseManager() {
		try {
			Files.createDirectories(DB_FOLDER_PATH);
			Class.forName("org.h2.Driver");
			createTablesAndRunMigrations();
		} catch (Exception e) {
			System.err.println("❌ Error initializing database: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void createTablesAndRunMigrations() {
		try (Connection conn = getConnection()) {
			createSevaTableIfNotExists(conn);
			createVisheshaPoojeTableIfNotExists(conn);
			createDonationsTableIfNotExists(conn);
			createReceiptTableIfNotExists(conn);
			createDonationReceiptTableIfNotExists(conn);
			createInKindDonationTableIfNotExists(conn);
			createShashwathaPoojaTableIfNotExists(conn);
			createCredentialsTableIfNotExists(conn);

			createKaryakramagaluTableIfNotExists(conn);
			createKaryakramaSevasTableIfNotExists(conn); // THIS IS THE RE-ADDED TABLE
			createKaryakramaReceiptsTableIfNotExists(conn);

			runMigrations(conn);
		} catch (SQLException e) {
			System.err.println("❌ Error creating tables or running migrations: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// runMigrations() and other table creation methods remain the same...

	// --- RE-ADD THIS METHOD ---
	private void createKaryakramaSevasTableIfNotExists(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS KaryakramaSevas (" +
				"seva_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"karyakrama_id INT NOT NULL, " +
				"seva_name VARCHAR(255) NOT NULL, " +
				"amount DECIMAL(10, 2) NOT NULL, " +
				"FOREIGN KEY (karyakrama_id) REFERENCES Karyakramagalu(karyakrama_id) ON DELETE CASCADE)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ KaryakramaSevas table checked/created successfully.");
		}
	}

	// --- PASTE ALL OTHER EXISTING METHODS FROM YOUR FILE BELOW THIS LINE ---
	// (e.g., runMigrations, createKaryakramagaluTableIfNotExists, createKaryakramaReceiptsTableIfNotExists, createCredentialsTableIfNotExists, etc.)
	// For completeness, the full file is below.

	private void runMigrations(Connection conn) throws SQLException {
		DatabaseMetaData meta = conn.getMetaData();
		try (ResultSet rs = meta.getColumns(null, null, "SHASHWATHAPOOJARECEIPTS", "AMOUNT")) {
			if (!rs.next()) {
				System.out.println("⏳ Running migration: Adding AMOUNT column to ShashwathaPoojaReceipts...");
				try (Statement stmt = conn.createStatement()) {
					stmt.executeUpdate("ALTER TABLE ShashwathaPoojaReceipts ADD COLUMN amount DECIMAL(10, 2) NOT NULL DEFAULT 1000.00");
					System.out.println("✅ Migration successful for AMOUNT column.");
				}
			}
		}
		try (ResultSet rs = meta.getColumns(null, null, "SHASHWATHAPOOJARECEIPTS", "PAYMENT_MODE")) {
			if (!rs.next()) {
				System.out.println("⏳ Running migration: Adding PAYMENT_MODE column to ShashwathaPoojaReceipts...");
				try (Statement stmt = conn.createStatement()) {
					stmt.executeUpdate("ALTER TABLE ShashwathaPoojaReceipts ADD COLUMN payment_mode VARCHAR(10) DEFAULT 'Cash'");
					System.out.println("✅ Migration successful for PAYMENT_MODE column.");
				}
			}
		}
		try (ResultSet rs = meta.getColumns(null, null, "OTHERS", "OTHERS_AMOUNT")) {
			if (rs.next()) {
				System.out.println("⏳ Running migration: Dropping OTHERS_AMOUNT column from Others...");
				try (Statement stmt = conn.createStatement()) {
					stmt.executeUpdate("ALTER TABLE Others DROP COLUMN OTHERS_AMOUNT");
					System.out.println("✅ Migration successful for OTHERS_AMOUNT column removal.");
				}
			}
		}
	}

	private void createKaryakramagaluTableIfNotExists(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS Karyakramagalu (" +
				"karyakrama_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"karyakrama_name VARCHAR(255) NOT NULL, " +
				"is_active BOOLEAN DEFAULT TRUE)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ Karyakramagalu table checked/created successfully.");
		}
	}

	private void createKaryakramaReceiptsTableIfNotExists(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS KaryakramaReceipts (" +
				"receipt_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"devotee_name VARCHAR(255), " +
				"phone_number VARCHAR(20), " +
				"address TEXT, " +
				"pan_number VARCHAR(10), " +
				"rashi VARCHAR(20), " +
				"nakshatra VARCHAR(20), " +
				"receipt_date DATE, " +
				"sevas_details TEXT, " +
				"total_amount DECIMAL(10, 2), " +
				"payment_mode VARCHAR(10), " +
				"timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ KaryakramaReceipts table checked/created successfully.");
		}
	}

	private void createCredentialsTableIfNotExists(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS Credentials (" +
				"credential_key VARCHAR(50) PRIMARY KEY, " +
				"credential_value TEXT NOT NULL)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			initializeCredentials(conn);
			System.out.println("✅ Credentials table checked/created successfully.");
		}
	}

	private void createInKindDonationTableIfNotExists(Connection conn) throws SQLException {
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
		}
	}

	private void createShashwathaPoojaTableIfNotExists(Connection conn) throws SQLException {
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
				"amount DECIMAL(10, 2), " +
				"payment_mode VARCHAR(10), " +
				"timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ ShashwathaPoojaReceipts table checked/created successfully.");
		}
	}

	private void initializeCredentials(Connection conn) throws SQLException {
		insertCredentialIfMissing(conn, "NORMAL_PASSWORD", "$2a$12$mKDI7SvSR3xVxoOgE6BRu.kNVPpc9jAThhtmgVP66tzcUVZO811k.");
		insertCredentialIfMissing(conn, "SPECIAL_PASSWORD", "$2a$12$Z2qx6uSzEIvkmI21GuY02uIFZHnUeDf/d.xAPHvm0H3IA2EEqfK/O");
		insertCredentialIfMissing(conn, "ADMIN_USERNAME", "Pranav");
		insertCredentialIfMissing(conn, "ADMIN_PASSWORD", "$2a$12$KJgaWpl8PHvOWBd1Nw7yI.je6qqWBZ1ZKRtPF.vCASqbVdEmPfPlO");
		insertCredentialIfMissing(conn, "SHASHWATHA_POOJA_PRICE", "1000.00");
	}

	private void insertCredentialIfMissing(Connection conn, String key, String value) throws SQLException {
		String checkSql = "SELECT COUNT(*) FROM Credentials WHERE credential_key = ?";
		String insertSql = "INSERT INTO Credentials (credential_key, credential_value) VALUES (?, ?)";

		try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
			checkStmt.setString(1, key);
			ResultSet rs = checkStmt.executeQuery();
			if (rs.next() && rs.getInt(1) == 0) {
				try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
					insertStmt.setString(1, key);
					insertStmt.setString(2, value);
					insertStmt.executeUpdate();
					System.out.println("✅ Initialized default credential for: " + key);
				}
			}
		}
	}

	private void createReceiptTableIfNotExists(Connection conn) throws SQLException {
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
		}
	}

	private void createDonationReceiptTableIfNotExists(Connection conn) throws SQLException {
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
		}
	}

	private void createSevaTableIfNotExists(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS Sevas (" +
				"seva_id VARCHAR(10) PRIMARY KEY, " +
				"seva_name VARCHAR(255) NOT NULL, " +
				"amount DECIMAL(10, 2) NOT NULL, " +
				"display_order INT)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ Sevas table checked/created successfully.");
			ensureDisplayOrderExists(conn);
		}
	}

	private void createVisheshaPoojeTableIfNotExists(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS VisheshaPooje (" +
				"vishesha_pooje_id VARCHAR(10) PRIMARY KEY, " +
				"vishesha_pooje_name VARCHAR(100) NOT NULL, " +
				"vishesha_pooje_amount DECIMAL(10,2) DEFAULT 0, " +
				"display_order INT NOT NULL)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ VisheshaPooje table checked/created successfully.");
		}
	}

	private void ensureDisplayOrderExists(Connection conn) {
		String sql = "UPDATE Sevas SET display_order = CAST(seva_id AS INT) WHERE display_order IS NULL";
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
		} catch (SQLException | NumberFormatException e) {
			// Ignore
		}
	}

	private void createDonationsTableIfNotExists(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS Donations (" +
				"donation_id VARCHAR(10) PRIMARY KEY, " +
				"donation_name VARCHAR(255) NOT NULL, " +
				"display_order INT)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ Donations table checked/created successfully.");
			ensureDisplayOrderExistsForDonations(conn);
		}
	}

	private void ensureDisplayOrderExistsForDonations(Connection conn) {
		String sql = "UPDATE Donations SET display_order = CAST(donation_id AS INT) WHERE display_order IS NULL";
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
		} catch (SQLException | NumberFormatException e) {
			// Ignore
		}
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