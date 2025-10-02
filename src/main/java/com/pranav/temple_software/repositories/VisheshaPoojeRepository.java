package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.utils.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class VisheshaPoojeRepository {
	private static final Logger logger = LoggerFactory.getLogger(VisheshaPoojeRepository.class);

	private static final VisheshaPoojeRepository instance = new VisheshaPoojeRepository();
	private final List<SevaEntry> visheshaPoojeList = new ArrayList<>();
	private boolean isDataLoaded = false;
	private VisheshaPoojeRepository() {
	}

	public static VisheshaPoojeRepository getInstance() {
		return instance;
	}

	private static Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
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
			logger.info("✅ Loaded {} vishesha poojas from database.", instance.visheshaPoojeList.size());
		} catch (SQLException e) {
			logger.error("❌ Error loading Vishesha Pooja from database", e);
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
			logger.error("Error fetching max VisheshaPooje ID", e);
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
			logger.error("Failed to insert VisheshaPooje", e);
		}
	}

	public synchronized boolean deleteVisheshaPoojeFromDB(String id) {
		String sql = "DELETE FROM VisheshaPooje WHERE vishesha_pooje_id = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, id);
			int affectedRows = pstmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			logger.error("Failed to delete VisheshaPooje with ID {}", id, e);
		}
		return false;
	}

	public static synchronized boolean updateDisplayOrder(String id, int order) {
		String sql = "UPDATE VisheshaPooje SET display_order = ? WHERE vishesha_pooje_id = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, order);
			pstmt.setString(2, id);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Failed to update display order for VisheshaPooje ID {}", id, e);
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
			logger.error("Error fetching vishesha_pooje_id for name {}", name, e);
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
			logger.error("Error updating amount for vishesha pooje ID {}", visheshaPoojeId, e);
			return false;
		}
	}
}