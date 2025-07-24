package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.SevaEntry;
import java.sql.*;
import java.util.*;

public class OtherSevaRepository {
	private static final String DB_URL = "jdbc:h2:~/temple_software/db/temple_data";
	private static final String USER = "sa";
	private static final String PASS = "";

	private static final OtherSevaRepository instance = new OtherSevaRepository();
	private final List<SevaEntry> otherSevaList = new ArrayList<>();
	private boolean isDataLoaded = false;

	private OtherSevaRepository() {
		// Constructor remains empty for lazy loading
	}

	public static OtherSevaRepository getInstance() {
		return instance;
	}

	private static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER, PASS);
	}

	// *** KEY CHANGE: Made this method static again ***
	public static synchronized void loadOtherSevasFromDB() {
		// Use the instance to manage the loaded state and list
		if (instance.isDataLoaded) {
			return;
		}
		instance.otherSevaList.clear();
		String sql = "SELECT * FROM OtherSevas ORDER BY display_order";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				String name = rs.getString("other_seva_name");
				double amount = rs.getDouble("other_seva_amount");
				int order = rs.getInt("display_order");

				SevaEntry entry = new SevaEntry(name, amount);
				entry.setDisplayOrder(order);
				instance.otherSevaList.add(entry);
			}
			instance.isDataLoaded = true;
			System.out.println("Loaded " + instance.otherSevaList.size() + " other sevas from DB.");
		} catch (SQLException e) {
			System.err.println("Failed to load Other Sevas: " + e.getMessage());
		}
	}

	// *** KEY CHANGE: Made this method static again ***
	public static List<SevaEntry> getAllOtherSevas() {
		if (!instance.isDataLoaded) {
			loadOtherSevasFromDB();
		}
		instance.otherSevaList.sort(Comparator.comparingInt(SevaEntry::getDisplayOrder));
		return Collections.unmodifiableList(instance.otherSevaList);
	}

	// This method was already non-static and correct, as it's called on the instance.
	public int getMaxOtherSevaId() {
		String sql = "SELECT MAX(CAST(other_seva_id AS INT)) FROM OtherSevas";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()) return rs.getInt(1);
		} catch (SQLException e) {
			System.err.println("Error fetching max OtherSeva ID: " + e.getMessage());
		}
		return 0;
	}

	// This method was already non-static and correct.
	public void addOtherSevaToDB(String id, String name, int amount) {
		String sql = "INSERT INTO OtherSevas (other_seva_id, other_seva_name, other_seva_amount, display_order) VALUES (?, ?, ?, ?)";
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
		String sql = "DELETE FROM OtherSevas WHERE other_seva_id = ?";
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
		String sql = "UPDATE OtherSevas SET display_order = ? WHERE other_seva_id = ?";
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
		String sql = "SELECT other_seva_id FROM OtherSevas WHERE other_seva_name = ?";
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
		String sql = "UPDATE OtherSevas SET other_seva_amount = ? WHERE other_seva_id = ?";
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
