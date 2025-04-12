// File: Temple_Software/src/main/java/com/pranav/temple_software/repositories/SevaRepository.java
package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.Seva; //

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection; // Import Collection
import java.util.Collections; // Import Collections
import java.util.LinkedHashMap;
// Removed unused import: import java.util.List;
import java.util.Map;

public class SevaRepository {

	// Constants for DB connection (Consider centralizing in DatabaseManager)
	private static final String DB_URL = "jdbc:h2:./temple_data"; //
	private static final String USER = "sa"; //
	private static final String PASS = ""; //

	private static final SevaRepository instance = new SevaRepository(); // Eager initialization

	public int getMaxSevaId() {
		String sql = "SELECT MAX(CAST(seva_id AS INT)) FROM Sevas";
		int maxId = 0; // Default to 0 if table is empty or error occurs

		// Ensure we have data loaded, although query runs on DB directly
		if (sevaMap.isEmpty() && !loadSevasFromDBIfEmpty()) { // Optional: Try loading if map is empty
			System.err.println("Warning: Seva map empty, attempting DB query for max ID anyway.");
		}


		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			if (rs.next()) {
				maxId = rs.getInt(1); // Get the max ID
			}
		} catch (SQLException e) {
			System.err.println("Error fetching max Seva ID: " + e.getMessage());
		} catch (Exception e) { // Catch potential CAST errors if ID isn't numeric
			System.err.println("Error casting Seva ID to INT for MAX function: " + e.getMessage());
			// Fallback: manually iterate through map keys if needed (less efficient)
			maxId = sevaMap.keySet().stream()
					.mapToInt(id -> { try { return Integer.parseInt(id); } catch (NumberFormatException nfe) { return 0; } })
					.max().orElse(0);
		}
		return maxId;
	}

	private boolean loadSevasFromDBIfEmpty() {
		if (sevaMap.isEmpty()) {
			System.out.println("Seva map is empty, attempting load from DB...");
			loadSevasFromDB(); // Assuming this method exists and loads into sevaMap
			return !sevaMap.isEmpty();
		}
		return true; // Map already had data
	}
	// Private constructor prevents instantiation from other classes
	private SevaRepository() {
		loadSevasFromDB(); // Load data when the single instance is created
	}

	// Public static method to get the single instance
	public static SevaRepository getInstance() {
		return instance;
	}

	// Keep the map to hold loaded sevas
	private final Map<String, Seva> sevaMap = new LinkedHashMap<>(); //


	// *** Method to get DB connection (Consider centralizing) ***
	private Connection getConnection() throws SQLException { //
		return DriverManager.getConnection(DB_URL, USER, PASS); //
	}

	// *** REMOVED initializeSevaData() method ***

	// *** ADDED Method to load sevas from DB ***
	public void loadSevasFromDB() {
		sevaMap.clear(); // Clear existing data before loading
		String sql = "SELECT seva_id, seva_name, amount FROM Sevas ORDER BY CAST(seva_id AS INT)"; // Order numerically

		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				String id = rs.getString("seva_id");
				String name = rs.getString("seva_name");
				double amount = rs.getDouble("amount");
				sevaMap.put(id, new Seva(id, name, amount)); //
			}
			System.out.println("Loaded " + sevaMap.size() + " sevas from database.");

		} catch (SQLException e) {
			System.err.println("Error loading Sevas from database: " + e.getMessage());
			// Consider throwing a custom exception or returning a boolean status
		} catch (Exception e) { // Catch potential NumberFormatException if CAST fails
			System.err.println("Error processing Seva data (potentially non-numeric ID?): " + e.getMessage());
		}
	}

	// *** ADDED Method to add a seva to DB ***
	public boolean addSevaToDB(Seva seva) {
		if (seva == null || sevaMap.containsKey(seva.getId())) { //
			System.err.println("Cannot add null seva or seva with duplicate ID: " + (seva != null ? seva.getId() : "null")); //
			return false;
		}
		String sql = "INSERT INTO Sevas (seva_id, seva_name, amount) VALUES (?, ?, ?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, seva.getId()); //
			pstmt.setString(2, seva.getName()); //
			pstmt.setDouble(3, seva.getAmount()); //

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				sevaMap.put(seva.getId(), seva); // Add to in-memory map as well //
				System.out.println("Seva added successfully: " + seva.getId()); //
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error adding Seva to database (ID: " + seva.getId() + "): " + e.getMessage()); //
		}
		return false;
	}

	// *** ADDED Method to delete a seva from DB ***
	public boolean deleteSevaFromDB(String sevaId) {
		if (sevaId == null || !sevaMap.containsKey(sevaId)) {
			System.err.println("Cannot delete null or non-existent Seva ID: " + sevaId);
			return false;
		}
		String sql = "DELETE FROM Sevas WHERE seva_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, sevaId);
			int affectedRows = pstmt.executeUpdate();

			if (affectedRows > 0) {
				sevaMap.remove(sevaId); // Remove from in-memory map as well
				System.out.println("Seva deleted successfully: " + sevaId);
				return true;
			} else {
				System.err.println("Seva ID not found in DB for deletion: " + sevaId);
			}
		} catch (SQLException e) {
			System.err.println("Error deleting Seva from database (ID: " + sevaId + "): " + e.getMessage());
		}
		return false;
	}

	// *** ADDED Method to get all loaded sevas (for UI population) ***
	public Collection<Seva> getAllSevas() {
		// Return an unmodifiable view to prevent external modification of the map's values directly
		return Collections.unmodifiableCollection(new ArrayList<>(sevaMap.values()));
	}

	// *** (Optional) ADDED Method to update a seva in DB ***
//    public boolean updateSevaInDB(Seva seva) {
//        if (seva == null || !sevaMap.containsKey(seva.getId())) {
//             System.err.println("Cannot update null or non-existent Seva ID: " + (seva != null ? seva.getId() : "null"));
//            return false;
//        }
//        String sql = "UPDATE Sevas SET seva_name = ?, amount = ? WHERE seva_id = ?";
//        try (Connection conn = getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setString(1, seva.getName());
//            pstmt.setDouble(2, seva.getAmount());
//            pstmt.setString(3, seva.getId());
//
//            int affectedRows = pstmt.executeUpdate();
//            if (affectedRows > 0) {
//                sevaMap.put(seva.getId(), seva); // Update in-memory map
//                 System.out.println("Seva updated successfully: " + seva.getId());
//                return true;
//            } else {
//                 System.err.println("Seva ID not found in DB for update: " + seva.getId());
//            }
//        } catch (SQLException e) {
//            System.err.println("Error updating Seva in database (ID: " + seva.getId() + "): " + e.getMessage());
//        }
//        return false;
//    }
}