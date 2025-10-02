// FILE: src/main/java/com/pranav/temple_software/repositories/OthersRepository.java
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
	private OthersRepository() {}

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
		} catch (SQLException e) {
			System.err.println("❌ Error loading Others from database: " + e.getMessage());
		}
	}

	public List<Others> getAllOthers() {
		if (!isDataLoaded) {
			loadOthersFromDB();
		}
		othersList.sort(Comparator.comparingInt(Others::getDisplayOrder));
		return Collections.unmodifiableList(new ArrayList<>(this.othersList));
	}

	public boolean addOtherToDB(String name) {
		String sql = "INSERT INTO Others (others_name) VALUES (?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("Error adding Other to DB: " + e.getMessage());
			return false;
		}
	}

	/**
	 * MODIFIED: This method now behaves like other repositories.
	 * It will delete the item regardless of whether it's used in past receipts.
	 * The special check for "in-use" constraint violations has been removed.
	 */
	public boolean deleteOtherFromDB(int otherId) {
		String sql = "DELETE FROM Others WHERE others_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, otherId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("Error deleting Other from DB (ID: " + otherId + "): " + e.getMessage());
			return false;
		}
	}

	public boolean updateDisplayOrder(List<Others> orderedList) {
		String sql = "UPDATE Others SET display_order = ? WHERE others_id = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			for (int i = 0; i < orderedList.size(); i++) {
				Others other = orderedList.get(i);
				pstmt.setInt(1, i + 1);
				pstmt.setInt(2, other.getId());
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			loadOthersFromDB();
			return true;
		} catch (SQLException e) {
			System.err.println("Error updating display order for Others: " + e.getMessage());
			return false;
		}
	}
}