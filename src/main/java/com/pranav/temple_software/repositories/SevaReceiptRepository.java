package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.SevaReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.utils.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SevaReceiptRepository {
	private static final String DB_URL = DatabaseManager.DB_URL;
	private static final String USER = "sa";
	private static final String PASS = "";
	private static final String H2_PK_VIOLATION_STATE = "23505";

	private static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER, PASS);
	}

	public static int getNextReceiptId() {
		String sql = "SELECT MAX(receipt_id) FROM Receipts";
		int nextId = 1;

		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			if (rs.next()) {
				int maxId = rs.getInt(1);
				if (!rs.wasNull()) {
					nextId = maxId + 1;
				}
			}
		} catch (SQLException e) {
			System.err.println("Error fetching next receipt ID: " + e.getMessage());
			return -1;
		}
		return nextId;
	}

	public int saveSpecificReceipt(int id, String name, String phone, String address, String panNumber, String rashi, String nakshatra, LocalDate date,
	                               String sevasDetails, double total, String paymentMode) {

		String sql = "INSERT INTO Receipts (receipt_id, devotee_name, phone_number, " +
				"address, pan_number, rashi, nakshatra," +
				"seva_date, sevas_details, total_amount, payment_mode) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, id);
			pstmt.setString(2, name);
			pstmt.setString(3, phone);
			pstmt.setString(4, address);
			pstmt.setString(5, panNumber); // Add PAN
			pstmt.setString(6, rashi);
			pstmt.setString(7, nakshatra);
			pstmt.setDate(8, java.sql.Date.valueOf(date));
			pstmt.setString(9, sevasDetails);
			pstmt.setDouble(10, total);
			pstmt.setString(11, paymentMode);

			int affectedRows = pstmt.executeUpdate();

			if (affectedRows > 0) {
				return id;
			} else {
				System.err.println("Error inserting receipt with specific ID " + id + ": No rows affected, but no exception.");
				return saveReceipt(name, phone, address, panNumber, date, sevasDetails, total, paymentMode);
			}

		} catch (SQLException e) {
			if (H2_PK_VIOLATION_STATE.equals(e.getSQLState())) {
				System.out.println("Receipt ID " + id + " already exists. Falling back to auto-increment ID.");
				return saveReceipt(name, phone, address, panNumber, date, sevasDetails, total, paymentMode);
			} else {
				System.err.println("Error inserting receipt with specific ID " + id + ": " + e.getMessage() + " (SQLState: " + e.getSQLState() + ")");
				return -1;
			}
		}
	}

	public int saveReceipt(String name, String phone, String address, String panNumber, LocalDate date,
	                       String sevasDetails, double total, String paymentMode) {

		String sql = "INSERT INTO Receipts (devotee_name, " +
				"phone_number, address, pan_number, seva_date, sevas_details," +
				" total_amount, payment_mode) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		int generatedId = -1;

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, name);
			pstmt.setString(2, phone);
			pstmt.setString(3, address);
			pstmt.setString(4, panNumber); // Add PAN
			pstmt.setDate(5, java.sql.Date.valueOf(date));
			pstmt.setString(6, sevasDetails);
			pstmt.setDouble(7, total);
			pstmt.setString(8, paymentMode);

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
			return -1;
		}
		return generatedId;
	}

	public List<SevaReceiptData> getAllReceipts() {
		List<SevaReceiptData> receipts = new ArrayList<>();
		String query = "SELECT * FROM receipts ORDER BY receipt_id DESC";

		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(query);
		     ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				int receiptId = rs.getInt("receipt_id");
				String devoteeName = rs.getString("devotee_name");
				String phoneNumber = rs.getString("phone_number");
				String address = rs.getString("address");
				String panNumber = rs.getString("pan_number"); // Get PAN
				LocalDate sevaDate = rs.getDate("seva_date").toLocalDate();
				double totalAmount = rs.getDouble("total_amount");
				String rashi = rs.getString("rashi");
				String nakshatra = rs.getString("nakshatra");
				String sevaDetails = rs.getString("sevas_details");
				ObservableList<SevaEntry> sevas = parseSevas(sevaDetails);
				boolean hasDonation = sevas.stream().anyMatch(seva -> seva.getName().startsWith("ದೇಣಿಗೆ"));
				String donationStatus = hasDonation ? "ಹೌದು" : "ಇಲ್ಲ";
				String paymentMode = rs.getString("payment_mode");
				SevaReceiptData receipt = new SevaReceiptData(
						receiptId, devoteeName, phoneNumber, address, panNumber, rashi, nakshatra,
						sevaDate, sevas, totalAmount, paymentMode, donationStatus
				);
				receipts.add(receipt);
			}
			System.out.println("✅ Loaded " + receipts.size() + " receipts from database.");
		} catch (SQLException e) {
			System.err.println("❌ SQL error while fetching receipts: " + e.getMessage());
		}
		return receipts;
	}

	/**
	 * *** BUG FIX ***
	 * Corrected the parsing logic to handle names that might contain colons,
	 * such as donation names ("ದೇಣಿಗೆ : ಸ್ಥಳ ಕಾಣಿಕ"). It now correctly identifies
	 * the name, amount, and quantity from the formatted string.
	 */
	private ObservableList<SevaEntry> parseSevas(String sevasString) {
		ObservableList<SevaEntry> sevas = FXCollections.observableArrayList();
		if (sevasString == null || sevasString.isEmpty()) {
			return sevas;
		}
		String[] entries = sevasString.split(";");
		for (String entry : entries) {
			try {
				String[] parts = entry.split(":");
				if (parts.length >= 3) {
					// The name is everything up to the last two parts
					String name = String.join(":", Arrays.copyOfRange(parts, 0, parts.length - 2)).trim();
					double amount = Double.parseDouble(parts[parts.length - 2].trim());
					int quantity = Integer.parseInt(parts[parts.length - 1].trim());
					SevaEntry seva = new SevaEntry(name, amount);
					seva.setQuantity(quantity);
					sevas.add(seva);
				}
			} catch (NumberFormatException | NullPointerException e) {
				System.err.println("Error parsing seva entry: " + entry);
				if (entry.contains(":")) {
					sevas.add(new SevaEntry(entry.split(":")[0].trim(), 0.0));
				} else {
					sevas.add(new SevaEntry(entry, 0.0));
				}
			}
		}
		return sevas;
	}
}
