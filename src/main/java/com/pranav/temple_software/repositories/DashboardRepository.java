package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.DashboardStats;
import com.pranav.temple_software.models.Seva;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.models.Donations;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class DashboardRepository {
	private static final String DB_URL = "jdbc:h2:./temple_data";
	private static final String USER = "sa";
	private static final String PASS = "";

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER, PASS);
	}

	public List<DashboardStats> getSevaStatistics(LocalDate fromDate, LocalDate toDate,
	                                              String paymentMethod, String specificSevaId) {
		List<DashboardStats> stats = new ArrayList<>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT s.seva_id, s.seva_name, s.amount, ");
		sql.append("COUNT(*) as total_count, ");
		sql.append("SUM(CASE WHEN r.payment_mode = 'Cash' THEN 1 ELSE 0 END) as cash_count, ");
		sql.append("SUM(CASE WHEN r.payment_mode = 'Online' THEN 1 ELSE 0 END) as online_count, ");
		sql.append("SUM(s.amount) as total_amount ");
		sql.append("FROM Receipts r ");
		sql.append("JOIN Sevas s ON r.sevas_details LIKE CONCAT('%', s.seva_name, '%') ");
		sql.append("WHERE 1=1 ");

		List<Object> parameters = new ArrayList<>();

		if (fromDate != null) {
			sql.append("AND r.seva_date >= ? ");
			parameters.add(Date.valueOf(fromDate));
		}
		if (toDate != null) {
			sql.append("AND r.seva_date <= ? ");
			parameters.add(Date.valueOf(toDate));
		}
		if (paymentMethod != null && !paymentMethod.equals("All")) {
			sql.append("AND r.payment_mode = ? ");
			parameters.add(paymentMethod);
		}
		if (specificSevaId != null && !specificSevaId.isEmpty()) {
			sql.append("AND s.seva_id = ? ");
			parameters.add(specificSevaId);
		}

		sql.append("GROUP BY s.seva_id, s.seva_name, s.amount ");
		sql.append("ORDER BY total_count DESC");

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < parameters.size(); i++) {
				pstmt.setObject(i + 1, parameters.get(i));
			}

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				stats.add(new DashboardStats(
						rs.getString("seva_id"),
						rs.getString("seva_name"),
						"SEVA",
						rs.getInt("total_count"),
						rs.getInt("cash_count"),
						rs.getInt("online_count"),
						rs.getDouble("total_amount")
				));
			}
		} catch (SQLException e) {
			System.err.println("Error fetching seva statistics: " + e.getMessage());
		}

		return stats;
	}

	public List<DashboardStats> getOtherSevaStatistics(LocalDate fromDate, LocalDate toDate,
	                                                   String paymentMethod, String specificOtherSevaId) {
		List<DashboardStats> stats = new ArrayList<>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT os.other_seva_id, os.other_seva_name, os.other_seva_amount, ");
		sql.append("COUNT(*) as total_count, ");
		sql.append("SUM(CASE WHEN r.payment_mode = 'Cash' THEN 1 ELSE 0 END) as cash_count, ");
		sql.append("SUM(CASE WHEN r.payment_mode = 'Online' THEN 1 ELSE 0 END) as online_count, ");
		sql.append("SUM(os.other_seva_amount) as total_amount ");
		sql.append("FROM Receipts r ");
		sql.append("JOIN OtherSevas os ON r.sevas_details LIKE CONCAT('%', os.other_seva_name, '%') ");
		sql.append("WHERE 1=1 ");

		List<Object> parameters = new ArrayList<>();

		if (fromDate != null) {
			sql.append("AND r.seva_date >= ? ");
			parameters.add(Date.valueOf(fromDate));
		}
		if (toDate != null) {
			sql.append("AND r.seva_date <= ? ");
			parameters.add(Date.valueOf(toDate));
		}
		if (paymentMethod != null && !paymentMethod.equals("All")) {
			sql.append("AND r.payment_mode = ? ");
			parameters.add(paymentMethod);
		}
		if (specificOtherSevaId != null && !specificOtherSevaId.isEmpty()) {
			sql.append("AND os.other_seva_id = ? ");
			parameters.add(specificOtherSevaId);
		}

		sql.append("GROUP BY os.other_seva_id, os.other_seva_name, os.other_seva_amount ");
		sql.append("ORDER BY total_count DESC");

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < parameters.size(); i++) {
				pstmt.setObject(i + 1, parameters.get(i));
			}

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				stats.add(new DashboardStats(
						rs.getString("other_seva_id"),
						rs.getString("other_seva_name"),
						"OTHER_SEVA",
						rs.getInt("total_count"),
						rs.getInt("cash_count"),
						rs.getInt("online_count"),
						rs.getDouble("total_amount")
				));
			}
		} catch (SQLException e) {
			System.err.println("Error fetching other seva statistics: " + e.getMessage());
		}

		return stats;
	}

	public List<DashboardStats> getDonationStatistics(LocalDate fromDate, LocalDate toDate,
	                                                  String paymentMethod, String specificDonationId) {
		List<DashboardStats> stats = new ArrayList<>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT d.donation_id, d.donation_name, ");
		sql.append("COUNT(*) as total_count, ");
		sql.append("SUM(CASE WHEN dr.payment_mode = 'Cash' THEN 1 ELSE 0 END) as cash_count, ");
		sql.append("SUM(CASE WHEN dr.payment_mode = 'Online' THEN 1 ELSE 0 END) as online_count, ");
		sql.append("SUM(dr.donation_amount) as total_amount ");
		sql.append("FROM DonationReceipts dr ");
		sql.append("JOIN Donations d ON dr.donation_name = d.donation_name ");
		sql.append("WHERE 1=1 ");

		List<Object> parameters = new ArrayList<>();

		if (fromDate != null) {
			sql.append("AND dr.seva_date >= ? ");
			parameters.add(Date.valueOf(fromDate));
		}
		if (toDate != null) {
			sql.append("AND dr.seva_date <= ? ");
			parameters.add(Date.valueOf(toDate));
		}
		if (paymentMethod != null && !paymentMethod.equals("All")) {
			sql.append("AND dr.payment_mode = ? ");
			parameters.add(paymentMethod);
		}
		if (specificDonationId != null && !specificDonationId.isEmpty()) {
			sql.append("AND d.donation_id = ? ");
			parameters.add(specificDonationId);
		}

		sql.append("GROUP BY d.donation_id, d.donation_name ");
		sql.append("ORDER BY total_count DESC");

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < parameters.size(); i++) {
				pstmt.setObject(i + 1, parameters.get(i));
			}

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				stats.add(new DashboardStats(
						rs.getString("donation_id"),
						rs.getString("donation_name"),
						"DONATION",
						rs.getInt("total_count"),
						rs.getInt("cash_count"),
						rs.getInt("online_count"),
						rs.getDouble("total_amount")
				));
			}
		} catch (SQLException e) {
			System.err.println("Error fetching donation statistics: " + e.getMessage());
		}

		return stats;
	}

	public List<String> getAllSevaNames() {
		List<String> sevaNames = new ArrayList<>();
		String sql = "SELECT seva_id, seva_name FROM Sevas ORDER BY display_order";

		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				sevaNames.add(rs.getString("seva_id") + ":" + rs.getString("seva_name"));
			}
		} catch (SQLException e) {
			System.err.println("Error fetching seva names: " + e.getMessage());
		}

		return sevaNames;
	}

	public List<String> getAllOtherSevaNames() {
		List<String> otherSevaNames = new ArrayList<>();
		String sql = "SELECT other_seva_id, other_seva_name FROM OtherSevas ORDER BY display_order";

		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				otherSevaNames.add(rs.getString("other_seva_id") + ":" + rs.getString("other_seva_name"));
			}
		} catch (SQLException e) {
			System.err.println("Error fetching other seva names: " + e.getMessage());
		}

		return otherSevaNames;
	}

	public List<String> getAllDonationNames() {
		List<String> donationNames = new ArrayList<>();
		String sql = "SELECT donation_id, donation_name FROM Donations ORDER BY display_order";

		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				donationNames.add(rs.getString("donation_id") + ":" + rs.getString("donation_name"));
			}
		} catch (SQLException e) {
			System.err.println("Error fetching donation names: " + e.getMessage());
		}

		return donationNames;
	}
}
