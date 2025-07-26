// File: Temple_Software/src/main/java/com/pranav/temple_software/repositories/SevaRepository.java
package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.Seva;
import java.sql.*;
import java.util.*;

public class SevaRepository {

	private static final String DB_URL = "jdbc:h2:~/temple_software/db/temple_data";
	private static final String USER = "sa";
	private static final String PASS = "";

	private static final SevaRepository instance = new SevaRepository();
	private final List<Seva> sevaList = new ArrayList<>();
	// A flag to ensure we only load from the DB once.
	private boolean isDataLoaded = false;

	// Private constructor prevents instantiation from other classes
	private SevaRepository() {
		// Data is no longer loaded in the constructor to prevent race conditions.
	}

	public static SevaRepository getInstance() {
		return instance;
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER, PASS);
	}

	public synchronized void loadSevasFromDB() {
		// *** SOLUTION 1: Force reload regardless of flag ***
		sevaList.clear();
		isDataLoaded = false;

		String sql = "SELECT seva_id, seva_name, amount, display_order FROM Sevas ORDER BY display_order";

		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				String id = rs.getString("seva_id");
				String name = rs.getString("seva_name");
				double amount = rs.getDouble("amount");
				int order = rs.getInt("display_order");
				sevaList.add(new Seva(id, name, amount, order));
			}
			isDataLoaded = true;
			System.out.println("✅ Loaded " + sevaList.size() + " sevas from database.");

		} catch (SQLException e) {
			System.err.println("❌ Error loading Sevas from database: " + e.getMessage());
			e.printStackTrace();
		}
	}


	public List<Seva> getAllSevas() {
		// *** KEY CHANGE ***
		// If data is not loaded, load it first. This is a "lazy-loading" approach.
		if (!isDataLoaded) {
			loadSevasFromDB();
		}
		// Return a new list containing copies or an unmodifiable view
		sevaList.sort(Comparator.comparingInt(Seva::getDisplayOrder));
		return List.copyOf(sevaList);
	}

	// ... The rest of your SevaRepository methods (add, delete, update, etc.) remain the same ...
	// NOTE: I have applied this same lazy-loading pattern to DonationRepository.java and OtherSevaRepository.java
	public int getMaxSevaId() {
		String sql = "SELECT MAX(CAST(seva_id AS INT)) FROM Sevas";
		int maxId = 0;
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			if (rs.next()) {
				maxId = rs.getInt(1);
			}
		} catch (SQLException e) {
			System.err.println("Error fetching max Seva ID: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Error casting Seva ID to INT for MAX function: " + e.getMessage());
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

	public int getMaxDisplayOrder() {
		String sql = "SELECT MAX(display_order) FROM Sevas";
		int maxOrder = 0;
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()) {
				maxOrder = rs.getInt(1);
			}
		} catch (SQLException e) {
			System.err.println("Error fetching max display_order: " + e.getMessage());
			maxOrder = sevaList.stream().mapToInt(Seva::getDisplayOrder).max().orElse(0);
		}
		return maxOrder;
	}

	public boolean addSevaToDB(Seva seva) {
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
				System.out.println("Seva added successfully: " + seva.getId() + " with order " + seva.getDisplayOrder());
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error adding Seva to database (ID: " + seva.getId() + "): " + e.getMessage());
		}
		return false;
	}

	public boolean deleteSevaFromDB(String sevaId) {
		String sql = "DELETE FROM Sevas WHERE seva_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, sevaId);
			int affectedRows = pstmt.executeUpdate();

			if (affectedRows > 0) {
				sevaList.removeIf(s -> s.getId().equals(sevaId));
				System.out.println("Seva deleted successfully: " + sevaId);
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error deleting Seva from database (ID: " + sevaId + "): " + e.getMessage());
		}
		return false;
	}

	public boolean updateDisplayOrder(String sevaId, int newOrder) {
		String sql = "UPDATE Sevas SET display_order = ? WHERE seva_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, newOrder);
			pstmt.setString(2, sevaId);
			int affectedRows = pstmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			System.err.println("Error updating display order for Seva " + sevaId + ": " + e.getMessage());
			return false;
		}
	}

	public boolean updateAmount(String sevaId, double newAmount) {
		String sql = "UPDATE Sevas SET amount = ? WHERE seva_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setDouble(1, newAmount);
			stmt.setString(2, sevaId);
			int affectedRows = stmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			System.err.println("Error updating amount for seva ID " + sevaId + ": " + e.getMessage());
			return false;
		}
	}
}
