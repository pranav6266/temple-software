package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.DashboardStats;
import com.pranav.temple_software.utils.DatabaseManager;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardRepository {

	private Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	// --- Seva Statistics ---
	public List<DashboardStats> getSevaStatistics(LocalDate fromDate, LocalDate toDate, String paymentMethod, String specificSevaId) {
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

	// --- Donation Statistics ---
	public List<DashboardStats> getDonationStatistics(LocalDate fromDate, LocalDate toDate, String paymentMethod, String specificDonationId) {
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

	// --- Karyakrama Statistics ---
	public List<DashboardStats> getKaryakramaStatistics(LocalDate fromDate, LocalDate toDate, String paymentMethod, String specificKaryakramaId) {
		List<DashboardStats> stats = new ArrayList<>();
		String sql = "SELECT " +
				"k.karyakrama_id, k.karyakrama_name, " +
				"COUNT(kr.receipt_id) as total_count, " +
				"SUM(CASE WHEN kr.payment_mode = 'Cash' THEN 1 ELSE 0 END) as cash_count, " +
				"SUM(CASE WHEN kr.payment_mode = 'Online' THEN 1 ELSE 0 END) as online_count, " +
				"SUM(kr.total_amount) as total_amount " +
				"FROM KaryakramaReceipts kr " +
				"JOIN KaryakramaSevas ks ON kr.sevas_details LIKE CONCAT('%', ks.seva_name, '%') " +
				"JOIN Karyakramagalu k ON ks.karyakrama_id = k.karyakrama_id " +
				"WHERE 1=1 ";

		List<Object> parameters = new ArrayList<>();
		if (fromDate != null) {
			sql += "AND kr.receipt_date >= ? ";
			parameters.add(Date.valueOf(fromDate));
		}
		if (toDate != null) {
			sql += "AND kr.receipt_date <= ? ";
			parameters.add(Date.valueOf(toDate));
		}
		if (paymentMethod != null && !paymentMethod.equals("All")) {
			sql += "AND kr.payment_mode = ? ";
			parameters.add(paymentMethod);
		}
		if (specificKaryakramaId != null && !specificKaryakramaId.isEmpty()) {
			sql += "AND k.karyakrama_id = ? ";
			parameters.add(specificKaryakramaId);
		}

		sql += "GROUP BY k.karyakrama_id, k.karyakrama_name ORDER BY total_count DESC";

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			for (int i = 0; i < parameters.size(); i++) {
				pstmt.setObject(i + 1, parameters.get(i));
			}
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				stats.add(new DashboardStats(
						rs.getString("karyakrama_id"),
						rs.getString("karyakrama_name"),
						"KARYAKRAMA",
						rs.getInt("total_count"),
						rs.getInt("cash_count"),
						rs.getInt("online_count"),
						rs.getDouble("total_amount")
				));
			}
		} catch (SQLException e) {
			System.err.println("Error fetching Karyakrama statistics: " + e.getMessage());
		}
		return stats;
	}

	// --- Shashwatha Pooja, Vishesha Pooja Statistics ---
	public List<DashboardStats> getShashwathaPoojaStatistics(LocalDate fromDate, LocalDate toDate) {
		List<DashboardStats> stats = new ArrayList<>();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT 'SHASHWATHA_POOJA' as item_id, 'ಶಾಶ್ವತ ಪೂಜೆ' as item_name, ");
		sql.append("COUNT(spr.receipt_id) as total_count, ");
		sql.append("SUM(CASE WHEN spr.payment_mode = 'Cash' THEN 1 ELSE 0 END) as cash_count, ");
		sql.append("SUM(CASE WHEN spr.payment_mode = 'Online' THEN 1 ELSE 0 END) as online_count, ");
		sql.append("SUM(spr.amount) as total_amount ");
		sql.append("FROM ShashwathaPoojaReceipts spr ");
		sql.append("WHERE 1=1 ");

		List<Object> parameters = new ArrayList<>();
		if (fromDate != null) {
			sql.append("AND spr.receipt_date >= ? ");
			parameters.add(Date.valueOf(fromDate));
		}
		if (toDate != null) {
			sql.append("AND spr.receipt_date <= ? ");
			parameters.add(Date.valueOf(toDate));
		}
		sql.append("GROUP BY item_name");

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < parameters.size(); i++) {
				pstmt.setObject(i + 1, parameters.get(i));
			}
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				int totalCount = rs.getInt("total_count");
				if (totalCount > 0) {
					stats.add(new DashboardStats(
							rs.getString("item_id"),
							rs.getString("item_name"),
							"SHASHWATHA_POOJA",
							totalCount,
							rs.getInt("cash_count"),
							rs.getInt("online_count"),
							rs.getDouble("total_amount")
					));
				}
			}
		} catch (SQLException e) {
			System.err.println("Error fetching Shashwatha Pooja statistics: " + e.getMessage());
		}
		return stats;
	}

	public List<DashboardStats> getVisheshaPoojaStatistics(LocalDate fromDate, LocalDate toDate, String paymentMethod, String specificVisheshaPoojaId) {
		List<DashboardStats> stats = new ArrayList<>();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT vp.vishesha_pooje_id, vp.vishesha_pooje_name, vp.vishesha_pooje_amount, ");
		sql.append("COUNT(*) as total_count, ");
		sql.append("SUM(CASE WHEN r.payment_mode = 'Cash' THEN 1 ELSE 0 END) as cash_count, ");
		sql.append("SUM(CASE WHEN r.payment_mode = 'Online' THEN 1 ELSE 0 END) as online_count, ");
		sql.append("SUM(vp.vishesha_pooje_amount) as total_amount ");
		sql.append("FROM Receipts r ");
		sql.append("JOIN VisheshaPooje vp ON r.sevas_details LIKE CONCAT('%', vp.vishesha_pooje_name, '%') ");
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
		if (specificVisheshaPoojaId != null && !specificVisheshaPoojaId.isEmpty()) {
			sql.append("AND vp.vishesha_pooje_id = ? ");
			parameters.add(specificVisheshaPoojaId);
		}

		sql.append("GROUP BY vp.vishesha_pooje_id, vp.vishesha_pooje_name, vp.vishesha_pooje_amount ");
		sql.append("ORDER BY total_count DESC");

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < parameters.size(); i++) {
				pstmt.setObject(i + 1, parameters.get(i));
			}
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				stats.add(new DashboardStats(
						rs.getString("vishesha_pooje_id"),
						rs.getString("vishesha_pooje_name"),
						"VISHESHA_POOJA",
						rs.getInt("total_count"),
						rs.getInt("cash_count"),
						rs.getInt("online_count"),
						rs.getDouble("total_amount")
				));
			}
		} catch (SQLException e) {
			System.err.println("Error fetching Vishesha Pooja statistics: " + e.getMessage());
		}
		return stats;
	}

	// --- Name retrieval methods ---
	public List<String> getAllSevaNames() {
		List<String> sevaNames = new ArrayList<>();
		String sql = "SELECT seva_id, seva_name FROM Sevas ORDER BY display_order";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				sevaNames.add(rs.getString("seva_id") + ":" + rs.getString("seva_name"));
			}
		} catch (SQLException e) {
			System.err.println("Error fetching seva names: " + e.getMessage());
		}
		return sevaNames;
	}

	public List<String> getAllDonationNames() {
		List<String> donationNames = new ArrayList<>();
		String sql = "SELECT donation_id, donation_name FROM Donations ORDER BY display_order";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				donationNames.add(rs.getString("donation_id") + ":" + rs.getString("donation_name"));
			}
		} catch (SQLException e) {
			System.err.println("Error fetching donation names: " + e.getMessage());
		}
		return donationNames;
	}

	public List<String> getAllVisheshaPoojaNames() {
		List<String> names = new ArrayList<>();
		String sql = "SELECT vishesha_pooje_id, vishesha_pooje_name FROM VisheshaPooje ORDER BY display_order";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				names.add(rs.getString("vishesha_pooje_id") + ":" + rs.getString("vishesha_pooje_name"));
			}
		} catch (SQLException e) {
			System.err.println("Error fetching Vishesha Pooja names: " + e.getMessage());
		}
		return names;
	}

	// --- NEWLY ADDED METHOD TO FIX THE ERROR ---
	public List<String> getAllShashwathaPoojaNames() {
		List<String> names = new ArrayList<>();
		// Since Shashwatha Pooja is a single type, we can return a hardcoded value if any receipts exist.
		String sql = "SELECT DISTINCT 'SHASHWATHA_POOJA:ಶಾಶ್ವತ ಪೂಜೆ' as name_entry FROM ShashwathaPoojaReceipts LIMIT 1";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()) {
				names.add(rs.getString("name_entry"));
			}
		} catch (SQLException e) {
			System.err.println("Error fetching Shashwatha Pooja names: " + e.getMessage());
		}
		return names;
	}

	public List<String> getAllKaryakramaNames() {
		List<String> names = new ArrayList<>();
		String sql = "SELECT karyakrama_id, karyakrama_name FROM Karyakramagalu ORDER BY karyakrama_name";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				names.add(rs.getString("karyakrama_id") + ":" + rs.getString("karyakrama_name"));
			}
		} catch (SQLException e) {
			System.err.println("Error fetching Karyakrama names: " + e.getMessage());
		}
		return names;
	}
}