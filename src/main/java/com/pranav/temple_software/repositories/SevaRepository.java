// File: Temple_Software/src/main/java/com/pranav/temple_software/repositories/SevaRepository.java
package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.Seva;
import com.pranav.temple_software.utils.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class SevaRepository {
	private static final Logger logger = LoggerFactory.getLogger(SevaRepository.class);

	private static final SevaRepository instance = new SevaRepository();
	private final List<Seva> sevaList = new ArrayList<>();
	private boolean isDataLoaded = false;

	private SevaRepository() {
	}

	public static SevaRepository getInstance() {
		return instance;
	}

	private Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	public synchronized void loadSevasFromDB() {
		logger.debug("SevaRepository.loadSevasFromDB() called");
		sevaList.clear();

		String sql = "SELECT * FROM SEVAS ORDER BY display_order";
		logger.debug("Executing SQL: {}", sql);
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql);
		     ResultSet rs = pstmt.executeQuery()) {

			int count = 0;
			while (rs.next()) {
				count++;
				Seva seva = new Seva(
						rs.getString("seva_id"),
						rs.getString("seva_name"),
						rs.getDouble("amount"),
						rs.getInt("display_order")
				);
				sevaList.add(seva);
			}
			logger.info("Loaded {} sevas from database.", count);
			isDataLoaded = true;
		} catch (SQLException e) {
			logger.error("❌ Error loading sevas from database", e);
		}
	}



	public synchronized List<Seva> getAllSevas() {
		if (!isDataLoaded) {
			loadSevasFromDB();
		}
		return List.copyOf(sevaList);
	}

	public synchronized int getMaxSevaId() {
		String sql = "SELECT MAX(CAST(seva_id AS INT)) FROM Sevas";
		int maxId = 0;
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			if (rs.next()) {
				maxId = rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("Error fetching max Seva ID", e);
		} catch (Exception e) {
			logger.error("Error casting Seva ID to INT for MAX function", e);
			maxId = sevaList.stream()
					.mapToInt(s -> {
						try {
							return Integer.parseInt(s.getId());
						} catch (NumberFormatException nfe) {
							return 0;
						}
					})
					.max().orElse(0);
		}
		return maxId;
	}

	public synchronized int getMaxDisplayOrder() {
		String sql = "SELECT MAX(display_order) FROM Sevas";
		int maxOrder = 0;
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()) {
				maxOrder = rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("Error fetching max display_order", e);
			maxOrder = sevaList.stream().mapToInt(Seva::getDisplayOrder).max().orElse(0);
		}
		return maxOrder;
	}

	public synchronized boolean addSevaToDB(Seva seva) {
		int nextOrder = getMaxDisplayOrder() + 1;
		seva.setDisplayOrder(nextOrder);
		String sql = "INSERT INTO Sevas (seva_id, seva_name, amount, display_order) VALUES (?, ?, ?, ?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, seva.getId());
			pstmt.setString(2, seva.getName());
			pstmt.setDouble(3, seva.getAmount());
			pstmt.setInt(4, seva.getDisplayOrder());

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				sevaList.add(seva);
				sevaList.sort(Comparator.comparingInt(Seva::getDisplayOrder));
				logger.info("Seva added successfully: {} with order {}", seva.getId(), seva.getDisplayOrder());
				return true;
			}
		} catch (SQLException e) {
			logger.error("Error adding Seva to database (ID: {})", seva.getId(), e);
		}
		return false;
	}

	public synchronized boolean deleteSevaFromDB(String sevaId) {
		String sql = "DELETE FROM Sevas WHERE seva_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, sevaId);
			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				sevaList.removeIf(s -> s.getId().equals(sevaId));
				logger.info("Seva deleted successfully: {}", sevaId);
				return true;
			}
		} catch (SQLException e) {
			logger.error("Error deleting Seva from database (ID: {})", sevaId, e);
		}
		return false;
	}

	public synchronized boolean updateDisplayOrder(String sevaId, int newOrder) {
		String sql = "UPDATE Sevas SET display_order = ? WHERE seva_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, newOrder);
			pstmt.setString(2, sevaId);
			int affectedRows = pstmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			logger.error("Error updating display order for Seva {}", sevaId, e);
			return false;
		}
	}

	public synchronized boolean updateAmount(String sevaId, double newAmount) {
		String sql = "UPDATE Sevas SET amount = ? WHERE seva_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setDouble(1, newAmount);
			stmt.setString(2, sevaId);
			int affectedRows = stmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			logger.error("Error updating amount for seva ID {}", sevaId, e);
			return false;
		}
	}
}