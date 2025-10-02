package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.DashboardStats;
import com.pranav.temple_software.utils.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardRepository {
	private static final Logger logger = LoggerFactory.getLogger(DashboardRepository.class);

	private Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	public List<DashboardStats> getSevaStatistics(LocalDate fromDate, LocalDate toDate, String paymentMethod, String specificSevaId) {
		List<DashboardStats> stats = new ArrayList<>();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ri.seva_name, COUNT(DISTINCT r.receipt_id) as total_count, ");
		sql.append("SUM(CASE WHEN r.payment_mode = 'Cash' THEN 1 ELSE 0 END) as cash_count, ");
		sql.append("SUM(CASE WHEN r.payment_mode = 'Online' THEN 1 ELSE 0 END) as online_count, ");
		sql.append("SUM(ri.quantity * ri.price_at_sale) as total_amount ");
		sql.append("FROM Receipts r JOIN Receipt_Items ri ON r.receipt_id = ri.receipt_id ");
		sql.append("LEFT JOIN Sevas s ON ri.seva_name = s.seva_name ");
		sql.append("WHERE 1=1 ");

		// Exclude items that are listed as Vishesha Poojas
		sql.append("AND ri.seva_name NOT IN (SELECT vishesha_pooje_name FROM VisheshaPooje) ");

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

		sql.append("GROUP BY ri.seva_name ");
		sql.append("ORDER BY total_count DESC");

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < parameters.size(); i++) {
				pstmt.setObject(i + 1, parameters.get(i));
			}

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				stats.add(new DashboardStats(
						null,
						rs.getString("seva_name"),
						"SEVA",
						rs.getInt("total_count"),
						rs.getInt("cash_count"),
						rs.getInt("online_count"),
						rs.getDouble("total_amount")
				));
			}
		} catch (SQLException e) {
			logger.error("Error fetching seva statistics", e);
		}
		return stats;
	}


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
			logger.error("Error fetching donation statistics", e);
		}
		return stats;
	}

	public List<DashboardStats> getKaryakramaStatistics(LocalDate fromDate, LocalDate toDate, String paymentMethod, String specificKaryakramaId) {
		List<DashboardStats> stats = new ArrayList<>();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT k.karyakrama_id, kr.karyakrama_name, ");
		sql.append("COUNT(DISTINCT kr.receipt_id) as total_count, ");
		sql.append("SUM(CASE WHEN kr.payment_mode = 'Cash' THEN 1 ELSE 0 END) as cash_count, ");
		sql.append("SUM(CASE WHEN kr.payment_mode = 'Online' THEN 1 ELSE 0 END) as online_count, ");
		sql.append("SUM(kri.quantity * kri.price_at_sale) as total_amount ");
		sql.append("FROM KaryakramaReceipts kr ");
		sql.append("JOIN Karyakrama_Receipt_Items kri ON kr.receipt_id = kri.receipt_id ");
		sql.append("LEFT JOIN Karyakramagalu k ON kr.karyakrama_name = k.karyakrama_name ");
		sql.append("WHERE kr.karyakrama_name IS NOT NULL ");

		List<Object> parameters = new ArrayList<>();
		if (fromDate != null) {
			sql.append("AND kr.receipt_date >= ? ");
			parameters.add(Date.valueOf(fromDate));
		}
		if (toDate != null) {
			sql.append("AND kr.receipt_date <= ? ");
			parameters.add(Date.valueOf(toDate));
		}
		if (paymentMethod != null && !paymentMethod.equals("All")) {
			sql.append("AND kr.payment_mode = ? ");
			parameters.add(paymentMethod);
		}
		if (specificKaryakramaId != null && !specificKaryakramaId.isEmpty()) {
			sql.append("AND k.karyakrama_id = ? ");
			parameters.add(specificKaryakramaId);
		}

		sql.append("GROUP BY k.karyakrama_id, kr.karyakrama_name ORDER BY total_count DESC");

		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
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
			logger.error("Error fetching Karyakrama statistics", e);
		}
		return stats;
	}

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
			logger.error("Error fetching Shashwatha Pooja statistics", e);
		}
		return stats;
	}

	public List<DashboardStats> getVisheshaPoojaStatistics(LocalDate fromDate, LocalDate toDate, String paymentMethod, String specificVisheshaPoojaId) {
		List<DashboardStats> stats = new ArrayList<>();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT vp.vishesha_pooje_id, vp.vishesha_pooje_name, ");
		sql.append("COUNT(DISTINCT r.receipt_id) as total_count, ");
		sql.append("SUM(CASE WHEN r.payment_mode = 'Cash' THEN 1 ELSE 0 END) as cash_count, ");
		sql.append("SUM(CASE WHEN r.payment_mode = 'Online' THEN 1 ELSE 0 END) as online_count, ");
		sql.append("SUM(ri.quantity * ri.price_at_sale) as total_amount ");
		sql.append("FROM Receipts r JOIN Receipt_Items ri ON r.receipt_id = ri.receipt_id ");
		sql.append("JOIN VisheshaPooje vp ON ri.seva_name = vp.vishesha_pooje_name ");
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

		sql.append("GROUP BY vp.vishesha_pooje_id, vp.vishesha_pooje_name ");
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
			logger.error("Error fetching Vishesha Pooja statistics", e);
		}
		return stats;
	}

	public List<String> getAllSevaNames() {
		List<String> sevaNames = new ArrayList<>();
		String sql = "SELECT seva_id, seva_name FROM Sevas ORDER BY display_order";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				sevaNames.add(rs.getString("seva_id") + ":" + rs.getString("seva_name"));
			}
		} catch (SQLException e) {
			logger.error("Error fetching seva names", e);
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
			logger.error("Error fetching donation names", e);
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
			logger.error("Error fetching Vishesha Pooja names", e);
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
			logger.error("Error fetching Karyakrama names", e);
		}
		return names;
	}
}