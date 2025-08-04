// FILE: src/main/java/com/pranav/temple_software/utils/DatabaseManager.java
package com.pranav.temple_software.utils;

import com.pranav.temple_software.repositories.DonationRepository;
import com.pranav.temple_software.repositories.OthersRepository;
import com.pranav.temple_software.repositories.SevaRepository;
import com.pranav.temple_software.repositories.VisheshaPoojeRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseManager {
	private static final String DB_FOLDER_PATH = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "TempleSoftware" + File.separator + "db";
	public static final String DB_URL = "jdbc:h2:" + DB_FOLDER_PATH + File.separator + "temple_data";
	private static final String USER = "sa";
	private static final String PASS = "";

	public DatabaseManager() {
		try {
			String logDir = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "TempleSoftware" + File.separator + "logs";
			File logDirFile = new File(logDir);
			if (!logDirFile.exists()) {
				logDirFile.mkdirs();
			}

			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
			File outputLog = new File(logDir, "temple_app_" + timestamp + ".log");
			File errorLog = new File(logDir, "temple_error_" + timestamp + ".log");
			PrintStream fileOut = new PrintStream(new FileOutputStream(outputLog));
			PrintStream fileErr = new PrintStream(new FileOutputStream(errorLog));
			System.setOut(new PrintStream(new TeeOutputStream(System.out, fileOut)));
			System.setErr(new PrintStream(new TeeOutputStream(System.err, fileErr)));
			System.out.println("🚀 Application started at: " + LocalDateTime.now());
			System.out.println("📁 Database path: " + DB_FOLDER_PATH);
			System.out.println("🔗 Database URL: " + DB_URL);
		} catch (FileNotFoundException e) {
			System.err.println("Could not set up logging: " + e.getMessage());
		}
		try {
			File dbDir = new File(DB_FOLDER_PATH);
			if (!dbDir.exists()) {
				boolean created = dbDir.mkdirs();
				System.out.println("📁 Database directory created: " + created + " at " + DB_FOLDER_PATH);
			}

			System.out.println("🔗 Attempting database connection to: " + DB_URL);

			// --- Create all application tables ---
			createSevaTableIfNotExists();
			createDonationsTableIfNotExists();
			createOthersTableIfNotExists();
			createVisheshaPoojeTableIfNotExists(); // <-- ADDED
			createReceiptTableIfNotExists();
			createDonationReceiptTableIfNotExists();
			createInKindDonationTableIfNotExists();
			createShashwathaPoojaReceiptTableIfNotExists();
			createCredentialsTableIfNotExists();

			// --- Verify connection and seed initial data ---
			testConnection();
			if (!verifyTablesExist()) {
				throw new RuntimeException("Required database tables could not be created or verified.");
			}
			Thread.sleep(100);

			seedInitialCredentials();

			// --- Load Repositories ---
			testDatabaseData();
			System.out.println("📊 Loading initial data...");
			SevaRepository.getInstance().loadSevasFromDB();
			DonationRepository.getInstance().loadDonationsFromDB();
			OthersRepository.loadOthersFromDB();
			VisheshaPoojeRepository.loadVisheshaPoojeFromDB(); // <-- ADDED

			System.out.println("✅ Database initialization complete!");
		} catch (Exception e) {
			System.err.println("❌ Database initialization failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void createShashwathaPoojaReceiptTableIfNotExists() {
		String sql = "CREATE TABLE IF NOT EXISTS ShashwathaPoojaReceipts(" +
				"receipt_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"devotee_name VARCHAR(255), " +
				"phone_number VARCHAR(20), " +
				"address VARCHAR(255), " +
				"rashi VARCHAR(50), " +
				"nakshatra VARCHAR(50), " +
				"receipt_date DATE, " +
				"pooja_date VARCHAR(255), " + // Storing as String
				"timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ ShashwathaPoojaReceipts table checked/created successfully.");
		} catch (SQLException e) {
			System.err.println("❌ Error creating ShashwathaPoojaReceipts table: " + e.getMessage());
		}
	}

	private void createCredentialsTableIfNotExists() {
		String sql = "CREATE TABLE IF NOT EXISTS Credentials (" +
				"credential_key VARCHAR(255) PRIMARY KEY, " +
				"credential_value VARCHAR(255) NOT NULL)";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("✅ Credentials table checked/created successfully.");
		} catch (SQLException e) {
			System.err.println("❌ Error creating Credentials table: " + e.getMessage());
		}
	}

	private void seedInitialCredentials() {
		String checkSql = "SELECT COUNT(*) FROM Credentials";
		try (Connection conn = getConnection();
		     Statement checkStmt = conn.createStatement();
		     ResultSet rs = checkStmt.executeQuery(checkSql)) {

			if (rs.next() && rs.getInt(1) == 0) {
				System.out.println("🔑 Credentials table is empty. Seeding initial default passwords...");
				String insertSql = "INSERT INTO Credentials (credential_key, credential_value) VALUES (?, ?)";
				try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
					String defaultNormalPass = PasswordUtils.hashPassword("user123");
					String defaultSpecialPass = PasswordUtils.hashPassword("special123");
					String defaultAdminUser = "admin";
					String defaultAdminPass = PasswordUtils.hashPassword("admin123");

					pstmt.setString(1, "NORMAL_PASSWORD");
					pstmt.setString(2, defaultNormalPass);
					pstmt.addBatch();

					pstmt.setString(1, "SPECIAL_PASSWORD");
					pstmt.setString(2, defaultSpecialPass);
					pstmt.addBatch();

					pstmt.setString(1, "ADMIN_USERNAME");
					pstmt.setString(2, defaultAdminUser);
					pstmt.addBatch();

					pstmt.setString(1, "ADMIN_PASSWORD");
					pstmt.setString(2, defaultAdminPass);
					pstmt.addBatch();

					pstmt.executeBatch();
					System.out.println("✅ Default credentials have been seeded successfully.");
				}
			}
		} catch (SQLException e) {
			System.err.println("❌ Error during credential seeding: " + e.getMessage());
		}
	}

	private void createInKindDonationTableIfNotExists() {
		String sql = "CREATE TABLE IF NOT EXISTS InKindDonations(" +
				"in_kind_receipt_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"devotee_name VARCHAR(255), " +
				"phone_number VARCHAR(20), " +
				"address VARCHAR(255), " +
				"rashi VARCHAR(50), " +
				"nakshatra VARCHAR(50), " +
				"donation_date DATE, " +
				"item_description TEXT, " +
				"timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("InKindDonations table checked/created successfully.");
		} catch (SQLException e) {
			System.err.println("Error creating InKindDonations table: " + e.getMessage());
		}
	}


	private boolean verifyTablesExist() {
		String[] tableNames = {"SEVAS", "DONATIONS", "OTHERS", "VISHESHAPOOJE", "RECEIPTS", "DONATIONRECEIPTS", "CREDENTIALS"};

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
			DatabaseMetaData meta = conn.getMetaData();
			for (String tableName : tableNames) {
				try (ResultSet rs = meta.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
					if (!rs.next()) {
						System.err.println("❌ Table " + tableName + " does not exist!");
						return false;
					}
				}
			}
			System.out.println("✅ All required tables verified to exist.");
			return true;
		} catch (SQLException e) {
			System.err.println("❌ Error verifying tables: " + e.getMessage());
			return false;
		}
	}

	public void testDatabaseData() {
		System.out.println("DEBUG: Testing direct database queries...");

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
			// Test Sevas
			try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM SEVAS");
			     ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					System.out.println("DEBUG: Direct SEVAS count: " + rs.getInt(1));
				}
			}

			// Test sample seva data
			try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM SEVAS LIMIT 5");
			     ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					System.out.println("DEBUG: Sample seva: " + rs.getString("seva_name") + " - " + rs.getDouble("amount"));
				}
			}

		} catch (SQLException e) {
			System.err.println("❌ Error testing database data: " + e.getMessage());
		}
	}


	public static void testConnection() {
		try (Connection conn = getConnection()) {
			System.out.println("✅ Database connection successful!");
			String[] tables = {"Sevas", "Donations", "Others", "VisheshaPooje"};
			for (String table : tables) {
				try (Statement stmt = conn.createStatement();
				     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
					if (rs.next()) {
						int count = rs.getInt(1);
						System.out.println("📊 " + table + " table has " + count + " records");
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("❌ Database connection failed: " + e.getMessage());
		}
	}


	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER, PASS);
	}

	private void createReceiptTableIfNotExists() {
		String sql = "CREATE TABLE IF NOT EXISTS Receipts(" +
				"receipt_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"devotee_name VARCHAR(255), " +
				"phone_number VARCHAR(20), " +
				"address VARCHAR(150), " +
				"rashi VARCHAR(20), " +
				"nakshatra VARCHAR(20), " +
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

	private void createDonationReceiptTableIfNotExists() {
		String sql = "CREATE TABLE IF NOT EXISTS DonationReceipts(" +
				"donation_receipt_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"devotee_name VARCHAR(255), " +
				"phone_number VARCHAR(20), " +
				"address VARCHAR(150), " +
				"rashi VARCHAR(20), " +
				"nakshatra VARCHAR(20), " +
				"seva_date DATE, " +
				"donation_name VARCHAR(255), " +
				"donation_amount DECIMAL(10, 2), " +
				"payment_mode VARCHAR(10), " +
				"print_status VARCHAR(20) DEFAULT 'SUCCESS', " +
				"timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("DonationReceipts table checked/created successfully.");
		} catch (SQLException e) {
			System.err.println("Error creating DonationReceipts table: " + e.getMessage());
		}
	}

	private void createSevaTableIfNotExists() {
		String sql = "CREATE TABLE IF NOT EXISTS Sevas (" +
				"seva_id VARCHAR(10) PRIMARY KEY, " +
				"seva_name VARCHAR(255) NOT NULL, " +
				"amount DECIMAL(10, 2) NOT NULL, " +
				"display_order INT)";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("Sevas table checked/created successfully.");
			addInitialSevasIfEmpty(conn);
			ensureDisplayOrderExists(conn);
		} catch (SQLException e) {
			System.err.println("Error creating Sevas table: " + e.getMessage());
		}
	}

	private void createOthersTableIfNotExists() {
		String sql = "CREATE TABLE IF NOT EXISTS Others (" +
				"others_id VARCHAR(10) PRIMARY KEY, " +
				"others_name VARCHAR(100) NOT NULL, " +
				"others_amount DECIMAL(10,2) DEFAULT 0, " +
				"display_order INT NOT NULL)";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("Others table checked/created successfully.");
			addInitialOthersIfEmpty(conn);
		} catch (SQLException e) {
			System.err.println("Error creating Others table: " + e.getMessage());
		}
	}

	private void createVisheshaPoojeTableIfNotExists() {
		String sql = "CREATE TABLE IF NOT EXISTS VisheshaPooje (" +
				"vishesha_pooje_id VARCHAR(10) PRIMARY KEY, " +
				"vishesha_pooje_name VARCHAR(100) NOT NULL, " +
				"vishesha_pooje_amount DECIMAL(10,2) DEFAULT 0, " +
				"display_order INT NOT NULL)";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("VisheshaPooje table checked/created successfully.");
		} catch (SQLException e) {
			System.err.println("Error creating VisheshaPooje table: " + e.getMessage());
		}
	}

	private void addInitialOthersIfEmpty(Connection conn) {
		// This method is now intentionally empty as per requirements.
	}

	private void ensureDisplayOrderExists(Connection conn) {
		String sql = "UPDATE Sevas SET display_order = CAST(seva_id AS INT) WHERE display_order IS NULL";
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
		} catch (SQLException | NumberFormatException e) {
			// Ignore potential errors if seva_id is not a number
		}
	}

	private void createDonationsTableIfNotExists() {
		String sql = "CREATE TABLE IF NOT EXISTS Donations (" +
				"donation_id VARCHAR(10) PRIMARY KEY, " +
				"donation_name VARCHAR(255) NOT NULL, " +
				"display_order INT)";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("Donations table checked/created successfully.");
			addInitialDonationsIfEmpty(conn);
			ensureDisplayOrderExistsForDonations(conn);
		} catch (SQLException e) {
			System.err.println("Error creating Donations table: " + e.getMessage());
		}
	}

	private void addInitialDonationsIfEmpty(Connection conn) {
		// This method is now intentionally empty as per requirements.
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
		// This method is now intentionally empty as per requirements.
	}

	public static void closeConnection() {
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			stmt.execute("SHUTDOWN");
			System.out.println("Database shutdown command issued.");
		} catch (SQLException e) {
			if (!"08006".equals(e.getSQLState())) {
				System.err.println("Error during database shutdown: " + e.getMessage());
			} else {
				System.out.println("Database connection closed successfully.");
			}
		}
	}
}