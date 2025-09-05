package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.utils.DatabaseManager;

import java.sql.*;
import java.util.*;

public class OthersRepository {
	private static final String DB_URL = DatabaseManager.DB_URL;
	private static final String USER = "sa";
	private static final String PASS = "";
	private static final OthersRepository instance = new OthersRepository();
	private final List<SevaEntry> otherSevaList = new ArrayList<>();
	private boolean isDataLoaded = false;
	private OthersRepository() {
	}

	public static OthersRepository getInstance() {
		return instance;
	}

	private static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER, PASS);
	}

	public static synchronized void loadOthersFromDB() {
		instance.otherSevaList.clear();
		instance.isDataLoaded = false;

		String sql = "SELECT * FROM Others ORDER BY display_order";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				String name = rs.getString("others_name");
				int order = rs.getInt("display_order");

				// Amount is no longer stored, so we use a placeholder 0.0
				SevaEntry entry = new SevaEntry(name, 0.0);
				entry.setDisplayOrder(order);
				instance.otherSevaList.add(entry);
			}
			instance.isDataLoaded = true;
			System.out.println("✅ Loaded " + instance.otherSevaList.size() + " other sevas from database.");
		} catch (SQLException e) {
			System.err.println("❌ Error loading Other Sevas from database: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static List<SevaEntry> getAllOthers() {
		if (!instance.isDataLoaded) {
			loadOthersFromDB();
		}
		instance.otherSevaList.sort(Comparator.comparingInt(SevaEntry::getDisplayOrder));
		return Collections.unmodifiableList(instance.otherSevaList);
	}

	public int getMaxOtherSevaId() {
		String sql = "SELECT MAX(CAST(others_id AS INT)) FROM Others";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()) return rs.getInt(1);
		} catch (SQLException e) {
			System.err.println("Error fetching max OtherSeva ID: " + e.getMessage());
		}
		return 0;
	}

	public void addOtherSevaToDB(String id, String name) {
		String sql = "INSERT INTO Others (others_id, others_name, display_order) VALUES (?, ?, ?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, id);
			pstmt.setString(2, name);
			pstmt.setInt(3, getMaxOtherSevaId() + 1);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Failed to insert OtherSeva: " + e.getMessage());
		}
	}

	public boolean deleteOtherSevaFromDB(String id) {
		String sql = "DELETE FROM Others WHERE others_id = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, id);
			int affectedRows = pstmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			System.err.println("Failed to delete OtherSeva: " + e.getMessage());
		}
		return false;
	}

	public static boolean updateDisplayOrder(String id, int order) {
		String sql = "UPDATE Others SET display_order = ? WHERE others_id = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, order);
			pstmt.setString(2, id);
			int affected = pstmt.executeUpdate();
			return affected > 0;
		} catch (SQLException e) {
			System.err.println("Failed to update display order: " + e.getMessage());
			return false;
		}
	}

	public String getOtherSevaIdByName(String name) {
		String sql = "SELECT others_id FROM Others WHERE others_name = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getString("others_id");
			}
		} catch (SQLException e) {
			System.err.println("Error fetching others_id: " + e.getMessage());
		}
		return null;
	}

	// updateAmount method is no longer needed and can be removed.
}