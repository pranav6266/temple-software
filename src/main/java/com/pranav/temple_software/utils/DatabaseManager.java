package com.pranav.temple_software.utils;

import com.pranav.temple_software.repositories.DonationRepository;
import com.pranav.temple_software.repositories.OtherSevaRepository;
import com.pranav.temple_software.repositories.SevaRepository;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseManager {
	// *** KEY CHANGE: Using a stable path in the user's AppData/Roaming directory ***
	// This is the standard and most reliable location for application data on Windows.
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

			// Create log files with timestamp
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
			File outputLog = new File(logDir, "temple_app_" + timestamp + ".log");
			File errorLog = new File(logDir, "temple_error_" + timestamp + ".log");

			PrintStream fileOut = new PrintStream(new FileOutputStream(outputLog));
			PrintStream fileErr = new PrintStream(new FileOutputStream(errorLog));

			// Tee output - print to both console and file
			System.setOut(new PrintStream(new TeeOutputStream(System.out, fileOut)));
			System.setErr(new PrintStream(new TeeOutputStream(System.err, fileErr)));

			System.out.println("🚀 Application started at: " + LocalDateTime.now());
			System.out.println("📁 Database path: " + DB_FOLDER_PATH);
			System.out.println("🔗 Database URL: " + DB_URL);

		} catch (FileNotFoundException e) {
			System.err.println("Could not set up logging: " + e.getMessage());
		}
		try {
			// Ensure the directory exists before trying to connect
			File dbDir = new File(DB_FOLDER_PATH);
			if (!dbDir.exists()) {
				boolean created = dbDir.mkdirs();
				System.out.println("📁 Database directory created: " + created + " at " + DB_FOLDER_PATH);
			}

			System.out.println("🔗 Attempting database connection to: " + DB_URL);

			// --- Create all application tables ---
			createSevaTableIfNotExists();
			createDonationsTableIfNotExists();
			createOtherSevaTableIfNotExists();
			createReceiptTableIfNotExists();
			createDonationReceiptTableIfNotExists();
			createInKindDonationTableIfNotExists();
			createCredentialsTableIfNotExists(); // <-- ADDED

			// --- Verify connection and seed initial data ---
			testConnection();
			if (!verifyTablesExist()) {
				throw new RuntimeException("Required database tables could not be created or verified.");
			}
			Thread.sleep(100);

			seedInitialCredentials(); // <-- ADDED

			// --- Load Repositories ---
			testDatabaseData();
			System.out.println("📊 Loading initial data...");
			SevaRepository.getInstance().loadSevasFromDB();
			DonationRepository.getInstance().loadDonationsFromDB();
			OtherSevaRepository.loadOtherSevasFromDB();

			System.out.println("✅ Database initialization complete!");



		} catch (Exception e) {
			System.err.println("❌ Database initialization failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Creates the Credentials table if it doesn't already exist.
	 * This table will store hashed passwords and other security-related key-value pairs.
	 */
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

	/**
	 * Seeds the Credentials table with default values if it is empty.
	 * This ensures the application has default passwords on its first-ever run.
	 * Passwords are HASHED before being stored.
	 */
	private void seedInitialCredentials() {
		String checkSql = "SELECT COUNT(*) FROM Credentials";
		try (Connection conn = getConnection();
		     Statement checkStmt = conn.createStatement();
		     ResultSet rs = checkStmt.executeQuery(checkSql)) {

			// Only seed if the table is empty
			if (rs.next() && rs.getInt(1) == 0) {
				System.out.println("🔑 Credentials table is empty. Seeding initial default passwords...");
				String insertSql = "INSERT INTO Credentials (credential_key, credential_value) VALUES (?, ?)";
				try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
					// Hash default passwords before inserting
					String defaultNormalPass = PasswordUtils.hashPassword("user123");
					String defaultSpecialPass = PasswordUtils.hashPassword("special123");
					String defaultAdminUser = "admin"; // Username is not hashed
					String defaultAdminPass = PasswordUtils.hashPassword("admin123");

					// Batch insert for efficiency
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
		String[] tableNames = {"SEVAS", "DONATIONS", "OTHERSEVAS", "RECEIPTS", "DONATIONRECEIPTS", "CREDENTIALS"}; // Added CREDENTIALS

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

			// Test if tables exist and have data
			String[] tables = {"Sevas", "Donations", "OtherSevas"};
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

	private void createOtherSevaTableIfNotExists(){
		String sql = "CREATE TABLE IF NOT EXISTS OtherSevas (" +
				"other_seva_id VARCHAR(10) PRIMARY KEY, " +
				"other_seva_name VARCHAR(100) NOT NULL, " +
				"other_seva_amount DECIMAL(10,2) DEFAULT 0, " +
				"display_order INT NOT NULL)";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			System.out.println("Other Sevas table checked/created successfully.");
			addInitialOtherSevasIfEmpty(conn);
		} catch (SQLException e) {
			System.err.println("Error creating Other Sevas table: " + e.getMessage());
		}
	}

	private void addInitialOtherSevasIfEmpty(Connection conn) {
		String checkSql = "SELECT COUNT(*) FROM OtherSevas";
		try (Statement checkStmt = conn.createStatement();
		     ResultSet rs = checkStmt.executeQuery(checkSql)) {
			if (rs.next() && rs.getInt(1) == 0) {
				System.out.println("OtherSevas table is empty. Adding initial other sevas...");
				String insertSql = "INSERT INTO OtherSevas (other_seva_id, other_seva_name, other_seva_amount, display_order) VALUES (?, ?, ?, ?)";
				try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
					int order = 1;
					pstmt.setString(1, String.valueOf(order)); pstmt.setString(2, "ಶತ ರುದ್ರಾಭಿಷೇಕ"); pstmt.setDouble(3, 1000.00); pstmt.setInt(4, order++); pstmt.addBatch();
					pstmt.setString(1, String.valueOf(order)); pstmt.setString(2, "ಸಾಮೂಹಿಕ ಆಶ್ಲೇಷ ಬಲಿ"); pstmt.setDouble(3, 500.00); pstmt.setInt(4, order++); pstmt.addBatch();
					pstmt.setString(1, String.valueOf(order)); pstmt.setString(2, "ಶ್ರೀಕೃಷ್ಣ ಜನ್ಮಾಷ್ಟಮಿ"); pstmt.setDouble(3, 1500.00); pstmt.setInt(4, order++); pstmt.addBatch();
					pstmt.setString(1, String.valueOf(order)); pstmt.setString(2, "ವರಮಹಾಲಕ್ಷ್ಮೀ ಪೂಜೆ"); pstmt.setDouble(3, 800.00); pstmt.setInt(4, order++); pstmt.addBatch();
					pstmt.setString(1, String.valueOf(order)); pstmt.setString(2, "ಪ್ರತಿಷ್ಠಾ ದಿನ (ಕಳಭ)"); pstmt.setDouble(3, 1200.00); pstmt.setInt(4, order++); pstmt.addBatch();
					pstmt.setString(1, String.valueOf(order)); pstmt.setString(2, "ಸಮಾಜ ಸೇವಾ ಕಾರ್ಯಗಳು"); pstmt.setDouble(3, 600.00); pstmt.setInt(4, order++); pstmt.addBatch();
					pstmt.setString(1, String.valueOf(order)); pstmt.setString(2, "ನಿತ್ಯ-ನೈಮಿತ್ತಿಕ ಕಾರ್ಯಗಳು"); pstmt.setDouble(3, 500.00); pstmt.setInt(4, order++); pstmt.addBatch();
					pstmt.setString(1, String.valueOf(order)); pstmt.setString(2, "ಜೀರ್ಣೋದ್ಧಾರ ಕಾರ್ಯಗಳು"); pstmt.setDouble(3, 700.00); pstmt.setInt(4, order++); pstmt.addBatch();
					pstmt.setString(1, String.valueOf(order)); pstmt.setString(2, "ಅಭಿವೃದ್ಧಿ ಕಾರ್ಯಗಳು"); pstmt.setDouble(3, 900.00); pstmt.setInt(4, order++); pstmt.addBatch();
					pstmt.setString(1, String.valueOf(order)); pstmt.setString(2, "ಅನ್ನದಾನ"); pstmt.setDouble(3, 300.00); pstmt.setInt(4, order++); pstmt.addBatch();
					pstmt.setString(1, String.valueOf(order)); pstmt.setString(2, "ವಿಶೇಷ ಪೂಜೆ"); pstmt.setDouble(3, 250.00); pstmt.setInt(4, order++); pstmt.addBatch();
					pstmt.executeBatch();
					System.out.println("Initial Other Sevas added successfully.");
				}
			}
		} catch (SQLException e) {
			System.err.println("Error during initial Other Seva data insertion: " + e.getMessage());
		}
	}

	private void ensureDisplayOrderExists(Connection conn) {
		String sql = "UPDATE Sevas SET display_order = CAST(seva_id AS INT) WHERE display_order IS NULL";
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
		} catch (SQLException | NumberFormatException e) {
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
		String checkSql = "SELECT COUNT(*) FROM Donations";
		try (Statement checkStmt = conn.createStatement();
		     ResultSet rs = checkStmt.executeQuery(checkSql)) {
			if (rs.next() && rs.getInt(1) == 0) {
				System.out.println("Donations table is empty. Adding initial donations...");
				String insertSql = "INSERT INTO Donations (donation_id, donation_name, display_order) VALUES (?, ?, ?)";
				try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
					int order = 1;
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಸ್ಥಳ ಕಾಣಿಕ"); insertStmt.setInt(3, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಪಾತ್ರೆ ಬಾಡಿಗೆ"); insertStmt.setInt(3, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ವಿದ್ಯುತ್"); insertStmt.setInt(3, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಜನರೇಟರ್"); insertStmt.setInt(3, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಕಟ್ಟಿಗೆ"); insertStmt.setInt(3, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ತೆಂಗಿನಕಾಯಿ"); insertStmt.setInt(3, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಅರ್ಚಕರ ದಕ್ಷಿಣೆ"); insertStmt.setInt(3, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಅಡಿಗೆಯವರಿಗೆ"); insertStmt.setInt(3, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಕೂಲಿ"); insertStmt.setInt(3, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಊಟೋಪಚಾರದ ಬಗ್ಗೆ"); insertStmt.setInt(3, order++); insertStmt.addBatch();
					insertStmt.executeBatch();
					System.out.println("Initial donations added successfully.");
				}
			}
		} catch (SQLException e) {
			System.err.println("Error during initial donation data insertion: " + e.getMessage());
		}
	}

	private void ensureDisplayOrderExistsForDonations(Connection conn) {
		String sql = "UPDATE Donations SET display_order = CAST(donation_id AS INT) WHERE display_order IS NULL";
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(sql);
		} catch (SQLException | NumberFormatException e) {
		}
	}

	private void addInitialSevasIfEmpty(Connection conn) {
		String checkSql = "SELECT COUNT(*) FROM Sevas";
		try (Statement checkStmt = conn.createStatement();
		     ResultSet rs = checkStmt.executeQuery(checkSql)) {
			if (rs.next() && rs.getInt(1) == 0) {
				System.out.println("Sevas table is empty. Adding initial 38 sevas...");
				String insertSql = "INSERT INTO Sevas (seva_id, seva_name, amount, display_order) VALUES (?, ?, ?, ?)";
				try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
					int order = 1;
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಬಲಿವಾಡು"); insertStmt.setDouble(3, 50.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಪಂಚಾಮೃತಾಭಿಷೇಕ"); insertStmt.setDouble(3, 30.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ರುದ್ರಾಭಿಷೇಕ"); insertStmt.setDouble(3, 50.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಏಕಾದಶ ರುದ್ರಾಭಿಷೇಕ"); insertStmt.setDouble(3, 1000.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಕ್ಷೀರಾಭಿಷೇಕ"); insertStmt.setDouble(3, 20.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಅಷ್ಟೋತ್ತರ ಕುಂಕುಮಾರ್ಚನೆ"); insertStmt.setDouble(3, 10.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಸಹಸ್ರನಾಮ ಕುಂಕುಮಾರ್ಚನೆ"); insertStmt.setDouble(3, 200.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಕಾರ್ತಿಕ ಪೂಜೆ"); insertStmt.setDouble(3, 50.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ತ್ರಿಮಧುರ"); insertStmt.setDouble(3, 30.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಪುಷ್ಪಾಂಜಲಿ"); insertStmt.setDouble(3, 30.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಹಣ್ಣುಕಾಯಿ"); insertStmt.setDouble(3, 25.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಶಾಸ್ತಾರ ದೇವರಿಗೆ ಕಾಯಿ"); insertStmt.setDouble(3, 20.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಪಂಚಕಜ್ಜಾಯ"); insertStmt.setDouble(3, 100.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಅಪ್ಪಕಜ್ಜಾಯ (1 ಕುಡ್ತೆ )"); insertStmt.setDouble(3, 20.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಮಂಗಳಾರತಿ"); insertStmt.setDouble(3, 20.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಕರ್ಪೂರಾರತಿ"); insertStmt.setDouble(3, 100.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ತುಪ್ಪದ ನಂದಾದೀಪ"); insertStmt.setDouble(3, 50.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಎಳ್ಳೆಣ್ಣೆ ನಂದಾದೀಪ"); insertStmt.setDouble(3, 300.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಒಂದು ದಿನದ ಪೂಜೆ"); insertStmt.setDouble(3, 300.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಸರ್ವಸೇವೆ"); insertStmt.setDouble(3, 200.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಗಣಪತಿ ಹವನ"); insertStmt.setDouble(3, 250.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ದೂರ್ವಾಹೋಮ"); insertStmt.setDouble(3, 350.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಶನಿ ಪೂಜೆ"); insertStmt.setDouble(3, 300.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಶನಿ ಜಪ"); insertStmt.setDouble(3, 250.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ರಾಹು ಜಪ"); insertStmt.setDouble(3, 250.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ತುಲಾಭಾರ"); insertStmt.setDouble(3, 100.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ದೀಪಾರಾಧನೆ"); insertStmt.setDouble(3, 1000.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ನೈವೇದ್ಯ ಸಮರ್ಪಣೆ"); insertStmt.setDouble(3, 100.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಹಾಲು ಪಾಯಸ"); insertStmt.setDouble(3, 50.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಪಿಂಡಿ ಪಾಯಸ"); insertStmt.setDouble(3, 100.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಕಠಿಣ ಪಾಯಸ"); insertStmt.setDouble(3, 125.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "2 ಕಾಯಿ ಪಾಯಸ"); insertStmt.setDouble(3, 250.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "5 ಕಾಯಿ ಪಾಯಸ"); insertStmt.setDouble(3, 400.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಹೆಸರುಬೇಳೆ ಪಾಯಸ"); insertStmt.setDouble(3, 400.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ನಾಗನಿಗೆ ಹಾಲು ಸಮರ್ಪಣೆ"); insertStmt.setDouble(3, 30.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ನಾಗ ಪೂಜೆ"); insertStmt.setDouble(3, 200.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ನಾಗ ತಂಬಿಲ"); insertStmt.setDouble(3, 300.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.setString(1, String.valueOf(order)); insertStmt.setString(2, "ಪವಮಾನ ಅಭಿಷೇಕ"); insertStmt.setDouble(3, 500.00); insertStmt.setInt(4, order++); insertStmt.addBatch();
					insertStmt.executeBatch();
					System.out.println("Initial 38 Sevas added successfully.");
				}
			}
		} catch (SQLException e) {
			System.err.println("Error during initial Seva data check/insertion: " + e.getMessage());
		}
	}
}
