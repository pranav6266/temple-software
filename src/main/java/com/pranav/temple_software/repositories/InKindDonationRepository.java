package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.HistoryFilterCriteria;
import com.pranav.temple_software.models.InKindDonation;
import com.pranav.temple_software.utils.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InKindDonationRepository {
	private static final Logger logger = LoggerFactory.getLogger(InKindDonationRepository.class);

	private Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	public int saveInKindDonation(InKindDonation donation) {
		String sql = "INSERT INTO InKindDonations (devotee_name, phone_number, address, pan_number, rashi, nakshatra, donation_date, item_description) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, donation.getDevoteeName());
			pstmt.setString(2, donation.getPhoneNumber());
			pstmt.setString(3, donation.getAddress());
			pstmt.setString(4, donation.getPanNumber());
			pstmt.setString(5, donation.getRashi());
			pstmt.setString(6, donation.getNakshatra());
			pstmt.setDate(7, Date.valueOf(donation.getDonationDate()));
			pstmt.setString(8, donation.getItemDescription());

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						return generatedKeys.getInt(1); // Return the generated ID
					}
				}
			}
			return -1; // Return -1 if save failed to get ID
		} catch (SQLException e) {
			logger.error("Error saving in-kind donation to database", e);
			return -1; // Return -1 on error
		}
	}

	public List<InKindDonation> getFilteredInKindDonations(HistoryFilterCriteria criteria) {
		List<InKindDonation> donations = new ArrayList<>();
		List<Object> parameters = new ArrayList<>();

		StringBuilder sql = new StringBuilder("SELECT * FROM InKindDonations WHERE 1=1 ");

		if (criteria.getDevoteeName() != null) {
			sql.append("AND devotee_name LIKE ? ");
			parameters.add("%" + criteria.getDevoteeName() + "%");
		}
		if (criteria.getPhoneNumber() != null) {
			sql.append("AND phone_number LIKE ? ");
			parameters.add("%" + criteria.getPhoneNumber() + "%");
		}
		if (criteria.getReceiptId() != null) {
			sql.append("AND in_kind_receipt_id = ? ");
			try {
				parameters.add(Integer.parseInt(criteria.getReceiptId()));
			} catch (NumberFormatException e) {
				parameters.add(0);
			}
		}
		if (criteria.getFromDate() != null) {
			sql.append("AND donation_date >= ? ");
			parameters.add(java.sql.Date.valueOf(criteria.getFromDate()));
		}
		if (criteria.getToDate() != null) {
			sql.append("AND donation_date <= ? ");
			parameters.add(java.sql.Date.valueOf(criteria.getToDate()));
		}

		sql.append("ORDER BY in_kind_receipt_id DESC");

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < parameters.size(); i++) {
				pstmt.setObject(i + 1, parameters.get(i));
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					donations.add(new InKindDonation(
							rs.getInt("in_kind_receipt_id"),
							rs.getString("devotee_name"),
							rs.getString("phone_number"),
							rs.getString("address"),
							rs.getString("pan_number"),
							rs.getString("rashi"),
							rs.getString("nakshatra"),
							rs.getDate("donation_date").toLocalDate(),
							rs.getString("item_description")
					));
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching filtered in-kind donations", e);
		}
		return donations;
	}
}