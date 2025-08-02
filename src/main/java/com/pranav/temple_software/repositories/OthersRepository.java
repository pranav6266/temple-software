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
		// Constructor remains empty for lazy loading
	}

	public static OthersRepository getInstance() {
		return instance;
	}

	private static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER, PASS);
	}

	// *** KEY CHANGE: Made this method static again ***
	public static synchronized void loadOthersFromDB() {
		// *** SOLUTION 1: Force reload regardless of flag ***
		instance.otherSevaList.clear();
		instance.isDataLoaded = false;

		String sql = "SELECT * FROM Others ORDER BY display_order";

		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				String name = rs.getString("other_seva_name");
				double amount = rs.getDouble("other_seva_amount");
				int order = rs.getInt("display_order");

				SevaEntry entry = new SevaEntry(name, amount);
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


	// *** KEY CHANGE: Made this method static again ***
	public static List<SevaEntry> getAllOthers() {
		if (!instance.isDataLoaded) {
			loadOthersFromDB();
		}
		instance.otherSevaList.sort(Comparator.comparingInt(SevaEntry::getDisplayOrder));
		return Collections.unmodifiableList(instance.otherSevaList);
	}

	// This method was already non-static and correct, as it's called on the instance.
	public int getMaxOtherSevaId() {
		String sql = "SELECT MAX(CAST(other_seva_id AS INT)) FROM Others";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()) return rs.getInt(1);
		} catch (SQLException e) {
			System.err.println("Error fetching max OtherSeva ID: " + e.getMessage());
		}
		return 0;
	}

	// This method was already non-static and correct.
	public void addOtherSevaToDB(String id, String name, int amount) {
		String sql = "INSERT INTO Others (other_seva_id, other_seva_name, other_seva_amount, display_order) VALUES (?, ?, ?, ?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, id);
			pstmt.setString(2, name);
			pstmt.setInt(3, amount);
			pstmt.setInt(4, getMaxOtherSevaId() + 1);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Failed to insert OtherSeva: " + e.getMessage());
		}
	}

	// This method was already non-static and correct.
	public boolean deleteOtherSevaFromDB(String id) {
		String sql = "DELETE FROM Others WHERE other_seva_id = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, id);
			int affectedRows = pstmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			System.err.println("Failed to delete OtherSeva: " + e.getMessage());
		}
		return false;
	}

	// *** KEY CHANGE: Made this method static again ***
	public static boolean updateDisplayOrder(String id, int order) {
		String sql = "UPDATE Others SET display_order = ? WHERE other_seva_id = ?";
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

	// This method was already non-static and correct.
	public String getOtherSevaIdByName(String name) {
		String sql = "SELECT other_seva_id FROM Others WHERE other_seva_name = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getString("other_seva_id");
			}
		} catch (SQLException e) {
			System.err.println("Error fetching other_seva_id: " + e.getMessage());
		}
		return null;
	}

	// *** KEY CHANGE: Made this method static again ***
	public static boolean updateAmount(String otherSevaId, double newAmount) {
		String sql = "UPDATE Others SET other_seva_amount = ? WHERE other_seva_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setDouble(1, newAmount);
			stmt.setString(2, otherSevaId);
			int affectedRows = stmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			System.err.println("Error updating amount for other seva ID " + otherSevaId + ": " + e.getMessage());
			return false;
		}
	}
}
