// PASTE THIS CODE INTO THE NEW FILE

package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.InKindDonation;
import com.pranav.temple_software.utils.DatabaseManager;
import javafx.beans.Observable;
import javafx.util.Callback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InKindDonationRepository {

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DatabaseManager.DB_URL, "sa", "");
	}

	public boolean saveInKindDonation(InKindDonation donation) {
		String sql = "INSERT INTO InKindDonations (devotee_name, phone_number, address, rashi, nakshatra, donation_date, item_description) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, donation.getDevoteeName());
			pstmt.setString(2, donation.getPhoneNumber());
			pstmt.setString(3, donation.getAddress());
			pstmt.setString(4, donation.getRashi());
			pstmt.setString(5, donation.getNakshatra());
			pstmt.setDate(6, Date.valueOf(donation.getDonationDate()));
			pstmt.setString(7, donation.getItemDescription());

			int affectedRows = pstmt.executeUpdate();
			return affectedRows > 0;

		} catch (SQLException e) {
			System.err.println("Error saving in-kind donation to database: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public ArrayList<InKindDonation> getAllInKindDonations() {
		ArrayList<InKindDonation> donations = new ArrayList<>();
		String sql = "SELECT * FROM InKindDonations ORDER BY in_kind_receipt_id DESC";

		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				donations.add(new InKindDonation(
						rs.getInt("in_kind_receipt_id"),
						rs.getString("devotee_name"),
						rs.getString("phone_number"),
						rs.getString("address"),
						rs.getString("rashi"),
						rs.getString("nakshatra"),
						rs.getDate("donation_date").toLocalDate(),
						rs.getString("item_description")
				));
			}
		} catch (SQLException e) {
			System.err.println("Error fetching in-kind donations: " + e.getMessage());
			e.printStackTrace();
		}
		return donations;
	}
}