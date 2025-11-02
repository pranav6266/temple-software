package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.HistoryFilterCriteria;
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

	public int saveReceipt(Connection conn, String name, String phone, String address, String panNumber, String rashi, String nakshatra, LocalDate date,
	                       double total, String paymentMode) throws SQLException {
		String sql = "INSERT INTO Receipts (devotee_name, phone_number, address, pan_number, rashi, nakshatra, " +
				"seva_date, total_amount, payment_mode) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		int generatedId = -1;

		try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, name);
			pstmt.setString(2, phone);
			pstmt.setString(3, address);
			pstmt.setString(4, panNumber);
			pstmt.setString(5, rashi);
			pstmt.setString(6, nakshatra);
			pstmt.setDate(7,Date.valueOf(date));
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
		}
		return generatedId;
	}

	public boolean saveReceiptItems(Connection conn, int receiptId, List<SevaEntry> sevas) throws SQLException {
		String sql = "INSERT INTO Receipt_Items (receipt_id, seva_name, quantity, price_at_sale) VALUES (?, ?, ?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			for (SevaEntry seva : sevas) {
				pstmt.setInt(1, receiptId);
				pstmt.setString(2, seva.getName());
				pstmt.setInt(3, seva.getQuantity());
				pstmt.setDouble(4, seva.getAmount());
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			return true;
		}
	}

	public List<SevaReceiptData> getFilteredReceipts(HistoryFilterCriteria criteria) {
		List<SevaReceiptData> receipts = new ArrayList<>();
		List<Object> parameters = new ArrayList<>();

		StringBuilder sql = new StringBuilder("SELECT * FROM receipts WHERE 1=1 ");

		if (criteria.getDevoteeName() != null) {
			sql.append("AND devotee_name LIKE ? ");
			parameters.add("%" + criteria.getDevoteeName() + "%");
		}
		if (criteria.getPhoneNumber() != null) {
			sql.append("AND phone_number LIKE ? ");
			parameters.add("%" + criteria.getPhoneNumber() + "%");
		}
		if (criteria.getReceiptId() != null) {
			sql.append("AND receipt_id = ? ");
			try {
				parameters.add(Integer.parseInt(criteria.getReceiptId()));
			} catch (NumberFormatException e) {
				parameters.add(0); // Will find no matches
			}
		}
		if (criteria.getFromDate() != null) {
			sql.append("AND seva_date >= ? ");
			parameters.add(java.sql.Date.valueOf(criteria.getFromDate()));
		}
		if (criteria.getToDate() != null) {
			sql.append("AND seva_date <= ? ");
			parameters.add(java.sql.Date.valueOf(criteria.getToDate()));
		}

		sql.append("ORDER BY receipt_id DESC");

		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < parameters.size(); i++) {
				stmt.setObject(i + 1, parameters.get(i));
			}

			try (ResultSet rs = stmt.executeQuery()) {
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
					String paymentMode = rs.getString("payment_mode");

					SevaReceiptData receipt = new SevaReceiptData(
							receiptId, devoteeName, phoneNumber, address, panNumber, rashi, nakshatra,
							sevaDate, sevas, totalAmount, paymentMode
					);
					receipts.add(receipt);
				}
			}
			logger.info("✅ Loaded {} filtered receipts from database.", receipts.size());
		} catch (SQLException e) {
			logger.error("❌ SQL error while fetching filtered receipts", e);
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