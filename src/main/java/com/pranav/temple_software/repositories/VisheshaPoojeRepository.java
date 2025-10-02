// FILE: src/main/java/com/pranav/temple_software/repositories/VisheshaPoojeRepository.java
package com.pranav.temple_software.repositories;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.utils.DatabaseManager;

import java.sql.*;
import java.util.*;

public class VisheshaPoojeRepository {
	private static final String DB_URL = DatabaseManager.DB_URL;
	private static final String USER = "sa";
	private static final String PASS = "";
	private static final VisheshaPoojeRepository instance = new VisheshaPoojeRepository();
	private final List<SevaEntry> visheshaPoojeList = new ArrayList<>();
	private boolean isDataLoaded = false;
	private VisheshaPoojeRepository() {
	}

	public static VisheshaPoojeRepository getInstance() {
		return instance;
	}

	private static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER, PASS);
	}

	public static synchronized void loadVisheshaPoojeFromDB() {
		instance.visheshaPoojeList.clear();
		instance.isDataLoaded = false;
		String sql = "SELECT * FROM VisheshaPooje ORDER BY display_order";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				String name = rs.getString("vishesha_pooje_name");
				double amount = rs.getDouble("vishesha_pooje_amount");
				int order = rs.getInt("display_order");

				SevaEntry entry = new SevaEntry(name, amount);
				entry.setDisplayOrder(order);
				instance.visheshaPoojeList.add(entry);
			}
			instance.isDataLoaded = true;
			System.out.println("✅ Loaded " + instance.visheshaPoojeList.size() + " vishesha poojas from database.");
		} catch (SQLException e) {
			System.err.println("❌ Error loading Vishesha Pooja from database: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static synchronized List<SevaEntry> getAllVisheshaPooje() {
		if (!instance.isDataLoaded) {
			loadVisheshaPoojeFromDB();
		}
		instance.visheshaPoojeList.sort(Comparator.comparingInt(SevaEntry::getDisplayOrder));
		return Collections.unmodifiableList(instance.visheshaPoojeList);
	}

	public synchronized int getMaxVisheshaPoojeId() {
		String sql = "SELECT MAX(CAST(vishesha_pooje_id AS INT)) FROM VisheshaPooje";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()) return rs.getInt(1);
		} catch (SQLException e) {
			System.err.println("Error fetching max VisheshaPooje ID: " + e.getMessage());
		}
		return 0;
	}

	public synchronized void addVisheshaPoojeToDB(String id, String name, int amount) {
		String sql = "INSERT INTO VisheshaPooje (vishesha_pooje_id, vishesha_pooje_name, vishesha_pooje_amount, display_order) VALUES (?, ?, ?, ?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, id);
			pstmt.setString(2, name);
			pstmt.setInt(3, amount);
			pstmt.setInt(4, getMaxVisheshaPoojeId() + 1);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Failed to insert VisheshaPooje: " + e.getMessage());
		}
	}

	public synchronized boolean deleteVisheshaPoojeFromDB(String id) {
		String sql = "DELETE FROM VisheshaPooje WHERE vishesha_pooje_id = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, id);
			int affectedRows = pstmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			System.err.println("Failed to delete VisheshaPooje: " + e.getMessage());
		}
		return false;
	}

	public static synchronized boolean updateDisplayOrder(String id, int order) {
		String sql = "UPDATE VisheshaPooje SET display_order = ? WHERE vishesha_pooje_id = ?";
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

	public synchronized String getVisheshaPoojeIdByName(String name) {
		String sql = "SELECT vishesha_pooje_id FROM VisheshaPooje WHERE vishesha_pooje_name = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getString("vishesha_pooje_id");
			}
		} catch (SQLException e) {
			System.err.println("Error fetching vishesha_pooje_id: " + e.getMessage());
		}
		return null;
	}

	public static synchronized boolean updateAmount(String visheshaPoojeId, double newAmount) {
		String sql = "UPDATE VisheshaPooje SET vishesha_pooje_amount = ? WHERE vishesha_pooje_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setDouble(1, newAmount);
			stmt.setString(2, visheshaPoojeId);
			int affectedRows = stmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			System.err.println("Error updating amount for vishesha pooje ID " + visheshaPoojeId + ": " + e.getMessage());
			return false;
		}
	}
}