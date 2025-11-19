package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.HistoryFilterCriteria;
import com.pranav.temple_software.models.KaryakramaReceiptData;
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

public class KaryakramaReceiptRepository {
	private static final Logger logger = LoggerFactory.getLogger(KaryakramaReceiptRepository.class);
	private Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	public int saveReceipt(Connection conn, KaryakramaReceiptData data) throws SQLException {
		String sql = "INSERT INTO KaryakramaReceipts (devotee_name, phone_number, address, pan_number, rashi, nakshatra, karyakrama_name, receipt_date, total_amount, payment_mode) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, data.getDevoteeName());
			pstmt.setString(2, data.getPhoneNumber());
			pstmt.setString(3, data.getAddress());
			pstmt.setString(4, data.getPanNumber());
			pstmt.setString(5, data.getRashi());
			pstmt.setString(6, data.getNakshatra());
			pstmt.setString(7, data.getKaryakramaName());
			pstmt.setDate(8, Date.valueOf(data.getReceiptDate()));
			pstmt.setDouble(9, data.getTotalAmount());
			pstmt.setString(10, data.getPaymentMode());

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						return generatedKeys.getInt(1);
					}
				}
			}
		}
		return -1;
	}

	public boolean saveReceiptItems(Connection conn, int receiptId, List<SevaEntry> items) throws SQLException {
		String sql = "INSERT INTO Karyakrama_Receipt_Items (receipt_id, item_name, quantity, price_at_sale) VALUES (?, ?, ?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			for (SevaEntry item : items) {
				pstmt.setInt(1, receiptId);
				pstmt.setString(2, item.getName());
				pstmt.setInt(3, item.getQuantity());
				pstmt.setDouble(4, item.getAmount());
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			return true;
		}
	}

	public List<KaryakramaReceiptData> getFilteredReceipts(HistoryFilterCriteria criteria) {
		List<KaryakramaReceiptData> receipts = new ArrayList<>();
		List<Object> parameters = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT * FROM KaryakramaReceipts WHERE 1=1 ");

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
				parameters.add(0);
			}
		}
		if (criteria.getFromDate() != null) {
			sql.append("AND receipt_date >= ? ");
			parameters.add(java.sql.Date.valueOf(criteria.getFromDate()));
		}
		if (criteria.getToDate() != null) {
			sql.append("AND receipt_date <= ? ");
			parameters.add(java.sql.Date.valueOf(criteria.getToDate()));
		}
		if (criteria.getPaymentMode() != null && !criteria.getPaymentMode().equals("All")) {
			sql.append("AND payment_mode = ? ");
			parameters.add(criteria.getPaymentMode());
		}

		sql.append("ORDER BY receipt_id DESC");

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < parameters.size(); i++) {
				pstmt.setObject(i + 1, parameters.get(i));
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					int receiptId = rs.getInt("receipt_id");
					ObservableList<SevaEntry> items = getItemsForReceipt(conn, receiptId);
					receipts.add(new KaryakramaReceiptData(
							receiptId,
							rs.getString("devotee_name"),
							rs.getString("phone_number"),
							rs.getString("address"),
							rs.getString("pan_number"),
							rs.getString("rashi"),
							rs.getString("nakshatra"),
							rs.getString("karyakrama_name"),
							rs.getDate("receipt_date").toLocalDate(),
							items,
							rs.getDouble("total_amount"),
							rs.getString("payment_mode")
					));
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching all Karyakrama receipts", e);
		}
		return receipts;
	}

	private ObservableList<SevaEntry> getItemsForReceipt(Connection conn, int receiptId) throws SQLException {
		ObservableList<SevaEntry> items = FXCollections.observableArrayList();
		String sql = "SELECT item_name, quantity, price_at_sale FROM Karyakrama_Receipt_Items WHERE receipt_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, receiptId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString("item_name");
				int quantity = rs.getInt("quantity");
				double price = rs.getDouble("price_at_sale");
				SevaEntry entry = new SevaEntry(name, price);
				entry.setQuantity(quantity);
				items.add(entry);
			}
		}
		return items;
	}
}