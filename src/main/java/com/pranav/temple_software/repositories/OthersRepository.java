package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.Others;
import com.pranav.temple_software.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OthersRepository {
	private static final OthersRepository instance = new OthersRepository();
	private final List<Others> othersList = new ArrayList<>();
	private boolean isDataLoaded = false;

	private OthersRepository() {
		// Private constructor for singleton pattern
	}

	public static OthersRepository getInstance() {
		return instance;
	}

	private Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	public synchronized void loadOthersFromDB() {
		othersList.clear();
		String sql = "SELECT others_id, others_name, display_order FROM Others ORDER BY display_order";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				int id = rs.getInt("others_id");
				String name = rs.getString("others_name");
				int order = rs.getInt("display_order");
				othersList.add(new Others(id, name, order));
			}
			isDataLoaded = true;
			System.out.println("✅ Loaded " + othersList.size() + " others from database.");
		} catch (SQLException e) {
			System.err.println("❌ Error loading Others from database: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public List<Others> getAllOthers() {
		if (!isDataLoaded) {
			loadOthersFromDB();
		}
		// Return a copy sorted by display order
		othersList.sort(Comparator.comparingInt(Others::getDisplayOrder));
		return Collections.unmodifiableList(new ArrayList<>(this.othersList));
	}

	public boolean addOtherToDB(String name) {
		String sql = "INSERT INTO Others (others_name, display_order) VALUES (?, ?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			int nextOrder = othersList.size() + 1;
			pstmt.setString(1, name);
			pstmt.setInt(2, nextOrder);

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				// Reload data to get the new item with its auto-generated ID
				loadOthersFromDB();
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error adding Other to DB: " + e.getMessage());
		}
		return false;
	}

	public boolean deleteOtherFromDB(int otherId) {
		String sql = "DELETE FROM Others WHERE others_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, otherId);
			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				loadOthersFromDB(); // Reload list after deletion
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error deleting Other from DB (ID: " + otherId + "): " + e.getMessage());
		}
		return false;
	}

	public boolean updateOtherName(int otherId, String newName) {
		String sql = "UPDATE Others SET others_name = ? WHERE others_id = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newName);
			pstmt.setInt(2, otherId);
			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				loadOthersFromDB(); // Reload list after update
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error updating other name for ID " + otherId + ": " + e.getMessage());
		}
		return false;
	}

	public boolean updateDisplayOrder(List<Others> orderedList) {
		String sql = "UPDATE Others SET display_order = ? WHERE others_id = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			for (int i = 0; i < orderedList.size(); i++) {
				Others other = orderedList.get(i);
				pstmt.setInt(1, i + 1); // New display order (1-based index)
				pstmt.setInt(2, other.getId());
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			loadOthersFromDB(); // Reload list after reordering
			return true;
		} catch (SQLException e) {
			System.err.println("Error updating display order for Others: " + e.getMessage());
			return false;
		}
	}
}