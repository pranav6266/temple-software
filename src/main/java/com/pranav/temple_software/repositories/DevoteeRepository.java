package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.DevoteeDetails;
import com.pranav.temple_software.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class DevoteeRepository {

	/**
	 * Finds the most recent details of a devotee from either the Receipts,
	 * [cite_start]DonationReceipts, InKindDonations, or ShashwathaPoojaReceipts table based on their phone number. [cite: 938, 939]
	 *
	 * [cite_start]@param phoneNumber The 10-digit phone number to search for. [cite: 939]
	 * [cite_start]@return An Optional containing DevoteeDetails if a record is found, otherwise an empty Optional. [cite: 940]
	 */
	public Optional<DevoteeDetails> findLatestDevoteeDetailsByPhone(String phoneNumber) {
		// This SQL query combines results from all tables, orders them by the most recent timestamp,

		String sql = "SELECT devotee_name, address, pan_number, rashi, nakshatra FROM (" +
				"  SELECT devotee_name, address, pan_number, rashi, nakshatra, timestamp FROM Receipts WHERE phone_number = ? " +
				"  UNION ALL " +
				"  SELECT devotee_name, address, pan_number, rashi, nakshatra, timestamp FROM DonationReceipts WHERE phone_number = ? " +
				"  UNION ALL " +
				"  SELECT devotee_name, address, pan_number, rashi, nakshatra, timestamp FROM InKindDonations WHERE phone_number = ? " +
				"  UNION ALL " +
				"  SELECT devotee_name, address, pan_number, rashi, nakshatra, timestamp FROM ShashwathaPoojaReceipts WHERE phone_number = ? " +
				") ORDER BY timestamp DESC LIMIT 1";
		try (Connection conn = DatabaseManager.getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, phoneNumber);
			pstmt.setString(2, phoneNumber);
			pstmt.setString(3, phoneNumber);
			pstmt.setString(4, phoneNumber);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				String name = rs.getString("devotee_name");
				String address = rs.getString("address");
				String panNumber = rs.getString("pan_number");
				String rashi = rs.getString("rashi");
				String nakshatra = rs.getString("nakshatra");

				DevoteeDetails details = new DevoteeDetails(name, address, panNumber, rashi, nakshatra);
				return Optional.of(details);
			}

		} catch (SQLException e) {
			System.err.println("Database error while fetching devotee details by phone: " + e.getMessage());
		}

		return Optional.empty(); // Return empty if no record is found or an error occurs [cite: 947]
	}

	/**
	 * Calculates the total cash amount transacted by a devotee on the current day.
	 * This query sums up totals from both the main Receipts and DonationReceipts tables.
	 *
	 * @param phoneNumber The 10-digit phone number of the devotee.
	 * @return The total cash amount for the current day. Returns 0.0 if no transactions are found.
	 */
	public double getTodaysCashTotalByPhone(String phoneNumber) {
		String sql = "SELECT SUM(total) AS daily_total FROM (" +
				"  SELECT total_amount AS total FROM Receipts " +
				"  WHERE phone_number = ? AND payment_mode = 'Cash' AND CAST(timestamp AS DATE) = CURRENT_DATE " +
				"  UNION ALL " +
				"  SELECT donation_amount AS total FROM DonationReceipts " +
				"  WHERE phone_number = ? AND payment_mode = 'Cash' AND CAST(timestamp AS DATE) = CURRENT_DATE" +
				")";
		double total = 0.0;

		try (Connection conn = DatabaseManager.getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, phoneNumber);
			pstmt.setString(2, phoneNumber);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				total = rs.getDouble("daily_total");
			}

		} catch (SQLException e) {
			System.err.println("Database error while fetching today's cash total: " + e.getMessage());
		}

		return total;
	}
}