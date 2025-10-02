package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.Others;
import com.pranav.temple_software.utils.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OthersRepository {
	private static final Logger logger = LoggerFactory.getLogger(OthersRepository.class);

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
			logger.error("❌ Error loading Others from database", e);
		}
	}

	public synchronized List<Others> getAllOthers() {
		if (!isDataLoaded) {
			loadOthersFromDB();
		}
		othersList.sort(Comparator.comparingInt(Others::getDisplayOrder));
		return List.copyOf(this.othersList);
	}

	public synchronized void addOtherToDB(String name) {
		String sql = "INSERT INTO Others (others_name) VALUES (?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error adding Other to DB", e);
		}
	}

	public synchronized void deleteOtherFromDB(int otherId) {
		String sql = "DELETE FROM Others WHERE others_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, otherId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error deleting Other from DB (ID: {})", otherId, e);
		}
	}

	public synchronized void updateDisplayOrder(List<Others> orderedList) {
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
		} catch (SQLException e) {
			logger.error("Error updating display order for Others", e);
		}
	}
}