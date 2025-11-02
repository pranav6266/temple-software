// FILE: src/main/java/com/pranav/temple_software/utils/DatabaseManager.java
package com.pranav.temple_software.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class DatabaseManager {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
	public static final String APP_DATA_FOLDER = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "TempleSoftware";
	public static final Path DB_FOLDER_PATH = Paths.get(APP_DATA_FOLDER, "db");
	public static final String DB_URL = "jdbc:h2:" + DB_FOLDER_PATH + File.separator + "temple_data";
	private static final String USER = "sa";
	private static final String PASS = "";
	private static HikariDataSource dataSource;


	public DatabaseManager() {
		try {
			Files.createDirectories(DB_FOLDER_PATH);
			initializeDataSource();
			createTablesAndRunMigrations();
		} catch (Exception e) {
			logger.error("❌ Error initializing database", e);
		}
	}

	private void initializeDataSource() {
		if (dataSource == null) {
			try {
				Class.forName("org.h2.Driver");
				HikariConfig config = new HikariConfig();
				config.setJdbcUrl(DB_URL);
				config.setUsername(USER);
				config.setPassword(PASS);
				config.addDataSourceProperty("cachePrepStmts", "true");
				config.addDataSourceProperty("prepStmtCacheSize", "250");
				config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
				dataSource = new HikariDataSource(config);
				logger.info("✅ HikariCP Connection Pool initialized.");
			} catch (ClassNotFoundException e) {
				logger.error("H2 JDBC Driver not found!", e);
			}
		}
	}

	private void createTablesAndRunMigrations() {
		try (Connection conn = getConnection()) {
			createSevaTableIfNotExists(conn);
			createVisheshaPoojeTableIfNotExists(conn);
			createDonationsTableIfNotExists(conn);
			createReceiptTableIfNotExists(conn);
			createReceiptItemsTableIfNotExists(conn);
			createDonationReceiptTableIfNotExists(conn);
			createInKindDonationTableIfNotExists(conn);
			createShashwathaPoojaTableIfNotExists(conn);
			createCredentialsTableIfNotExists(conn);
			createKaryakramagaluTableIfNotExists(conn);
			createOthersTableIfNotExists(conn);
			createKaryakramaReceiptsTableIfNotExists(conn);
			createKaryakramaReceiptItemsTableIfNotExists(conn);

			runMigrations(conn);
			createIndexesIfNotExists(conn);
		} catch (SQLException e) {
			logger.error("❌ Error creating tables or running migrations", e);
		}
	}

	private void runMigrations(Connection conn) throws SQLException {
		DatabaseMetaData meta = conn.getMetaData();
		try (ResultSet rs = meta.getColumns(null, null, "OTHERS", "OTHERS_ID")) {
			if (rs.next()) {
				String isAutoIncrement = rs.getString("IS_AUTOINCREMENT");
				if (!"YES".equalsIgnoreCase(isAutoIncrement)) {
					logger.info("⏳ Running migration: Fixing AUTO_INCREMENT property for OTHERS_ID...");
					try (Statement stmt = conn.createStatement()) {
						stmt.executeUpdate("ALTER TABLE OTHERS ALTER COLUMN OTHERS_ID INT AUTO_INCREMENT");
						logger.info("✅ Migration successful for OTHERS_ID.");
					} catch (SQLException e) {
						logger.error("❌ Failed to apply OTHERS_ID migration", e);
					}
				}
			}
		}

		try (ResultSet rs = meta.getColumns(null, null, "SHASHWATHAPOOJARECEIPTS", "AMOUNT")) {
			if (!rs.next()) {
				logger.info("⏳ Running migration: Adding AMOUNT column to ShashwathaPoojaReceipts...");
				try (Statement stmt = conn.createStatement()) {
					stmt.executeUpdate("ALTER TABLE ShashwathaPoojaReceipts ADD COLUMN amount DECIMAL(10, 2) NOT NULL DEFAULT 1000.00");
					logger.info("✅ Migration successful for AMOUNT column.");
				}
			}
		}

		try (ResultSet rs = meta.getColumns(null, null, "SHASHWATHAPOOJARECEIPTS", "PAYMENT_MODE")) {
			if (!rs.next()) {
				logger.info("⏳ Running migration: Adding PAYMENT_MODE column to ShashwathaPoojaReceipts...");
				try (Statement stmt = conn.createStatement()) {
					stmt.executeUpdate("ALTER TABLE ShashwathaPoojaReceipts ADD COLUMN payment_mode VARCHAR(10) DEFAULT 'Cash'");
					logger.info("✅ Migration successful for PAYMENT_MODE column.");
				}
			}
		}

		try (ResultSet rs = meta.getColumns(null, null, "INKINDDONATIONS", "PAYMENT_MODE")) {
			if (rs.next()) { // Check if the column EXISTS
				logger.info("⏳ Running migration: Removing (dropping) PAYMENT_MODE column from InKindDonations...");
				try (Statement stmt = conn.createStatement()) {
					stmt.executeUpdate("ALTER TABLE InKindDonations DROP COLUMN payment_mode");
					logger.info("✅ Migration successful: PAYMENT_MODE column dropped from InKindDonations.");
				} catch (SQLException e) {
					logger.error("❌ Failed to apply InKindDonations.PAYMENT_MODE drop column migration", e);
				}
			}
		}

		try (ResultSet rs = meta.getColumns(null, null, "KARYAKRAMARECEIPTS", "KARYAKRAMA_NAME")) {
			if (!rs.next()) {
				logger.info("⏳ Running migration: Adding KARYAKRAMA_NAME column to KaryakramaReceipts...");
				try (Statement stmt = conn.createStatement()) {
					stmt.executeUpdate("ALTER TABLE KaryakramaReceipts ADD COLUMN karyakrama_name VARCHAR(255)");
					logger.info("✅ Migration successful for KARYAKRAMA_NAME column.");
				}
			}
		}
	}

	private void createOthersTableIfNotExists(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS Others (" +
				"others_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"others_name VARCHAR(255) NOT NULL, " +
				"display_order INT)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			logger.debug("✅ Others table checked/created successfully.");
		}
	}

	private void createKaryakramagaluTableIfNotExists(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS Karyakramagalu (" +
				"karyakrama_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"karyakrama_name VARCHAR(255) NOT NULL, " +
				"is_active BOOLEAN DEFAULT TRUE)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			logger.debug("✅ Karyakramagalu table checked/created successfully.");
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
				"karyakrama_name VARCHAR(255), " +
				"receipt_date DATE, " +
				"total_amount DECIMAL(10, 2), " +
				"payment_mode VARCHAR(10), " +
				"timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			logger.debug("✅ KaryakramaReceipts table checked/created successfully.");
		}
	}

	private void createKaryakramaReceiptItemsTableIfNotExists(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS Karyakrama_Receipt_Items (" +
				"item_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"receipt_id INT, " +
				"item_name VARCHAR(255), " +
				"quantity INT, " +
				"price_at_sale DECIMAL(10, 2), " +
				"FOREIGN KEY(receipt_id) REFERENCES KaryakramaReceipts(receipt_id) ON DELETE CASCADE)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			logger.debug("✅ Karyakrama_Receipt_Items table checked/created successfully.");
		}
	}

	private void createCredentialsTableIfNotExists(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS Credentials (" +
				"credential_key VARCHAR(50) PRIMARY KEY, " +
				"credential_value TEXT NOT NULL)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			initializeCredentials(conn);
			logger.debug("✅ Credentials table checked/created successfully.");
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
			logger.debug("✅ InKindDonations table checked/created successfully.");
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
			logger.debug("✅ ShashwathaPoojaReceipts table checked/created successfully.");
		}
	}

	private void initializeCredentials(Connection conn) throws SQLException {
		insertCredentialIfMissing(conn, "NORMAL_PASSWORD", "$2a$12$mKDI7SvSR3xVxoOgE6BRu.kNVPpc9jAThhtmgVP66tzcUVZO811k.");
		insertCredentialIfMissing(conn, "SPECIAL_PASSWORD", "$2a$12$Z2qx6uSzEIvkmI21GuY02uIFZHnUeDf/d.xAPHvm0H3IA2EEqfK/O");
		insertCredentialIfMissing(conn, "ADMIN_USERNAME", "Pranav");
		insertCredentialIfMissing(conn, "ADMIN_PASSWORD", "$2a$12$KJgaWpl8PHvOWBd1Nw7yI.je6qqWBZ1ZKRtPF.vCASqbVdEmPfPlO");
		insertCredentialIfMissing(conn, "SHASHWATHA_POOJA_PRICE", "500.00");
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
					logger.info("✅ Initialized default credential for: {}", key);
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
				"total_amount DECIMAL(10, 2), " +
				"payment_mode VARCHAR(10), " +
				"timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			logger.debug("✅ Receipts table checked/created successfully.");
		}
	}

	private void createReceiptItemsTableIfNotExists(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS Receipt_Items (" +
				"receipt_item_id INT AUTO_INCREMENT PRIMARY KEY, " +
				"receipt_id INT, " +
				"seva_name VARCHAR(255), " +
				"quantity INT, " +
				"price_at_sale DECIMAL(10, 2), " +
				"FOREIGN KEY(receipt_id) REFERENCES Receipts(receipt_id) ON DELETE CASCADE)";
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
			logger.debug("✅ Receipt_Items table checked/created successfully.");
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
			logger.debug("✅ DonationReceipts table checked/created successfully.");
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
			logger.debug("✅ Sevas table checked/created successfully.");
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
			logger.debug("✅ VisheshaPooje table checked/created successfully.");
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
			logger.debug("✅ Donations table checked/created successfully.");
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
		if (dataSource == null) {
			throw new SQLException("DataSource is not initialized.");
		}
		return dataSource.getConnection();
	}

	public static void closeConnection() {
		if (dataSource != null && !dataSource.isClosed()) {
			dataSource.close();
			logger.info("✅ HikariCP Connection Pool closed.");
		}
		try (Connection conn = DriverManager.getConnection(DB_URL + ";ifexists=true", USER, PASS);
		     Statement stmt = conn.createStatement()) {
			stmt.execute("SHUTDOWN");
			logger.info("✅ Database shutdown command issued.");
		} catch (SQLException e) {
			if (e.getSQLState().equals("08006")) {
				logger.info("✅ Database already shut down.");
			} else {
				logger.error("❌ Error during database shutdown", e);
			}
		}
	}

	private void createIndexesIfNotExists(Connection conn) {
		try (Statement stmt = conn.createStatement()) {
			// Indexes for Receipts table
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_receipts_phone ON Receipts(phone_number)");
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_receipts_timestamp ON Receipts(timestamp)");

			// Indexes for DonationReceipts table
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_donations_phone ON DonationReceipts(phone_number)");
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_donations_timestamp ON DonationReceipts(timestamp)");

			// Indexes for InKindDonations table
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_inkind_phone ON InKindDonations(phone_number)");
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_inkind_timestamp ON InKindDonations(timestamp)");

			// Indexes for ShashwathaPoojaReceipts table
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_shashwatha_phone ON ShashwathaPoojaReceipts(phone_number)");
			stmt.execute("CREATE INDEX IF NOT EXISTS idx_shashwatha_timestamp ON ShashwathaPoojaReceipts(timestamp)");

			logger.info("✅ Database indexes checked/created successfully.");
		} catch (SQLException e) {
			logger.error("❌ Error creating database indexes", e);
		}
	}
}