package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.utils.DatabaseManager;

import java.sql.*;
import java.util.*;

public class OtherSevaRepository {
	private static final String DB_URL = "jdbc:h2:./temple_data";
	private static final String USER = "sa";
	private static final String PASS = "";

	private static final List<SevaEntry> otherSevaList = new ArrayList<>();

	private static final OtherSevaRepository instance = new OtherSevaRepository();


	private OtherSevaRepository() {
		loadOtherSevasFromDB();
	}

	public static OtherSevaRepository getInstance() {
		return instance;
	}

	private static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER, PASS);
	}

	public void loadOtherSevasFromDB() {
		otherSevaList.clear();
		String sql = "SELECT * FROM OtherSevas ORDER BY display_order";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				String name = rs.getString("other_seva_name");
				double amount = rs.getDouble("other_seva_amount");
				otherSevaList.add(new SevaEntry(name, amount));
			}
		} catch (SQLException e) {
			System.err.println("Failed to load Other Sevas: " + e.getMessage());
		}
	}


	public static List<SevaEntry> getAllOtherSevas() {
		return Collections.unmodifiableList(otherSevaList);
	}

	public int getMaxOtherSevaId() {
		String sql = "SELECT MAX(CAST(other_seva_id AS INT)) FROM OtherSevas";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()) return rs.getInt(1);
		} catch (SQLException e) {
			System.err.println("Error fetching max OtherSeva ID: " + e.getMessage());
		}
		return 0;
	}

	public void addOtherSevaToDB(String id, String name, int amount) {
		// Now includes amount column in the insertion.
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


	public void deleteOtherSevaFromDB(String id) {
		String sql = "DELETE FROM OtherSevas WHERE other_seva_id = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, id);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Failed to delete OtherSeva: " + e.getMessage());
		}
	}

	public static void updateDisplayOrder(String id, int order) {
		String sql = "UPDATE OtherSevas SET display_order = ? WHERE other_seva_id = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, order);
			pstmt.setString(2, id);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Failed to update display order: " + e.getMessage());
		}
	}

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

	public static boolean updateAmount(String otherSevaId, double newAmount) {
		String sql = "UPDATE OtherSevas SET other_seva_amount = ? WHERE other_seva_id = ?";
		try (Connection conn = DatabaseManager.getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setDouble(1, newAmount);
			stmt.setString(2, otherSevaId);
			int affectedRows = stmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			System.err.println("Error updating amount for seva ID " + otherSevaId + ": " + e.getMessage());
			return false;
		}
	}
}
