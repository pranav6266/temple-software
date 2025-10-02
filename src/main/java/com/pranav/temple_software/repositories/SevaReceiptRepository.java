package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.SevaReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.utils.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SevaReceiptRepository {
	private static final Logger logger = LoggerFactory.getLogger(SevaReceiptRepository.class);

	private static Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	public int saveReceipt(String name, String phone, String address, String panNumber, String rashi, String nakshatra, LocalDate date,
	                       double total, String paymentMode) {

		String sql = "INSERT INTO Receipts (devotee_name, phone_number, address, pan_number, rashi, nakshatra, " +
				"seva_date, total_amount, payment_mode) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		int generatedId = -1;

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, name);
			pstmt.setString(2, phone);
			pstmt.setString(3, address);
			pstmt.setString(4, panNumber);
			pstmt.setString(5, rashi);
			pstmt.setString(6, nakshatra);
			pstmt.setDate(7, java.sql.Date.valueOf(date));
			pstmt.setDouble(8, total);
			pstmt.setString(9, paymentMode);

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						generatedId = generatedKeys.getInt(1);
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Error inserting auto-increment receipt", e);
			return -1;
		}
		return generatedId;
	}

	public boolean saveReceiptItems(int receiptId, List<SevaEntry> sevas) {
		String sql = "INSERT INTO Receipt_Items (receipt_id, seva_name, quantity, price_at_sale) VALUES (?, ?, ?, ?)";

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			for (SevaEntry seva : sevas) {
				pstmt.setInt(1, receiptId);
				pstmt.setString(2, seva.getName());
				pstmt.setInt(3, seva.getQuantity());
				pstmt.setDouble(4, seva.getAmount());
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			return true;
		} catch (SQLException e) {
			logger.error("Error batch inserting receipt items for receipt ID {}", receiptId, e);
			return false;
		}
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
				String panNumber = rs.getString("pan_number");
				LocalDate sevaDate = rs.getDate("seva_date").toLocalDate();
				double totalAmount = rs.getDouble("total_amount");
				String rashi = rs.getString("rashi");
				String nakshatra = rs.getString("nakshatra");

				ObservableList<SevaEntry> sevas = getSevasForReceipt(conn, receiptId);

				boolean hasDonation = sevas.stream().anyMatch(seva -> seva.getName().startsWith("ದೇಣಿಗೆ"));
				String donationStatus = hasDonation ? "ಹೌದು" : "ಇಲ್ಲ";
				String paymentMode = rs.getString("payment_mode");
				SevaReceiptData receipt = new SevaReceiptData(
						receiptId, devoteeName, phoneNumber, address, panNumber, rashi, nakshatra,
						sevaDate, sevas, totalAmount, paymentMode, donationStatus
				);
				receipts.add(receipt);
			}
			logger.info("✅ Loaded {} receipts from database.", receipts.size());
		} catch (SQLException e) {
			logger.error("❌ SQL error while fetching receipts", e);
		}
		return receipts;
	}

	private ObservableList<SevaEntry> getSevasForReceipt(Connection conn, int receiptId) throws SQLException {
		ObservableList<SevaEntry> sevas = FXCollections.observableArrayList();
		String sql = "SELECT seva_name, quantity, price_at_sale FROM Receipt_Items WHERE receipt_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, receiptId);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				String name = rs.getString("seva_name");
				int quantity = rs.getInt("quantity");
				double price = rs.getDouble("price_at_sale");
				SevaEntry entry = new SevaEntry(name, price);
				entry.setQuantity(quantity);
				sevas.add(entry);
			}
		}
		return sevas;
	}
}