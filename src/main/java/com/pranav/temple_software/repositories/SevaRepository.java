// File: Temple_Software/src/main/java/com/pranav/temple_software/repositories/SevaRepository.java
package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.Seva; //

import java.sql.*;
import java.util.*;
import java.util.stream.IntStream;
// Removed unused import: import java.util.List;


public class SevaRepository {

	// Constants for DB connection (Consider centralizing in DatabaseManager)
	private static final String DB_URL = "jdbc:h2:./temple_data"; //
	private static final String USER = "sa"; //
	private static final String PASS = ""; //

	private static final SevaRepository instance = new SevaRepository(); // Eager initialization
	private final List<Seva> sevaList = new ArrayList<>();


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
					.mapToInt(id -> {
						try {
							return Integer.parseInt(id);
						} catch (NumberFormatException nfe) {
							return 0;
						}
					})
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
		sevaList.clear(); // Clear existing data
		// *** MODIFIED SQL Query ***
		String sql = "SELECT seva_id, seva_name, amount, display_order FROM Sevas ORDER BY display_order";

		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				String id = rs.getString("seva_id");
				String name = rs.getString("seva_name");
				double amount = rs.getDouble("amount");
				int order = rs.getInt("display_order");
				// Use the constructor that accepts displayOrder
				sevaList.add(new Seva(id, name, amount, order));
			}
			System.out.println("Loaded " + sevaList.size() + " sevas from database, ordered by display_order.");

		} catch (SQLException e) {
			System.err.println("Error loading Sevas from database: " + e.getMessage());
		}
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
			// Fallback: get max order from the loaded list
			maxOrder = sevaList.stream().mapToInt(Seva::getDisplayOrder).max().orElse(0);
		}
		return maxOrder;
	}

	// *** ADDED Method to add a seva to DB ***
	public boolean addSevaToDB(Seva seva) {
		// Calculate the next display order
		int nextOrder = getMaxDisplayOrder() + 1;
		seva.setDisplayOrder(nextOrder); // Set the order for the new seva

		// *** MODIFIED SQL to include display_order ***
		String sql = "INSERT INTO Sevas (seva_id, seva_name, amount, display_order) VALUES (?, ?, ?, ?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, seva.getId());
			pstmt.setString(2, seva.getName());
			pstmt.setDouble(3, seva.getAmount());
			pstmt.setInt(4, seva.getDisplayOrder()); // Use the calculated order

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				// Add to in-memory list and sort it again (or add in correct position)
				sevaList.add(seva);
				sevaList.sort(Comparator.comparingInt(Seva::getDisplayOrder)); // Keep list sorted
				System.out.println("Seva added successfully: " + seva.getId() + " with order " + seva.getDisplayOrder());
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error adding Seva to database (ID: " + seva.getId() + "): " + e.getMessage());
		}
		return false;
	}


	// *** ADDED Method to delete a seva from DB ***
	public boolean deleteSevaFromDB(String sevaId) {
		// Find the order of the item being deleted to potentially re-order subsequent items
		// Optional: Re-ordering logic can be added here or handled separately
		// For now, just delete. Subsequent items retain their order number.
		String sql = "DELETE FROM Sevas WHERE seva_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, sevaId);
			int affectedRows = pstmt.executeUpdate();

			if (affectedRows > 0) {
				// Remove from in-memory list
				sevaList.removeIf(s -> s.getId().equals(sevaId));
				// No need to sort here as relative order doesn't change
				System.out.println("Seva deleted successfully: " + sevaId);
				// Optional: Implement logic to decrement display_order for subsequent items if desired
				return true;
			}
		} catch (SQLException e) {
			System.err.println("Error deleting Seva from database (ID: " + sevaId + "): " + e.getMessage());
		}
		return false;
	}


	private void updateSevaDisplayOrderInDB(Connection conn, String sevaId, int newOrder) throws SQLException {
		// Note: This method now throws SQLException to be handled by the calling transaction block
		String sql = "UPDATE Sevas SET display_order = ? WHERE seva_id = ?";
		// Use the passed-in connection, do not close it here
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, newOrder);
			pstmt.setString(2, sevaId);
			int affectedRows = pstmt.executeUpdate();
			if (affectedRows == 0) {
				// If the row wasn't found, we should probably fail the transaction
				throw new SQLException("Seva ID " + sevaId + " not found during display_order update within transaction.");
			}
			// No commit/close here - handled by caller
		}
		// Catch block removed - exception propagates to caller for transaction handling
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



	// *** ADDED Method to get all loaded sevas (for UI population) ***
	public List<Seva> getAllSevas() {
		// Return a new list containing copies or an unmodifiable view
		// Ensure internal list is sorted before returning copy
		sevaList.sort(Comparator.comparingInt(Seva::getDisplayOrder));
		return Collections.unmodifiableList(new ArrayList<>(sevaList)); // Return immutable copy
	}

	private OptionalInt findSevaIndexById(String sevaId) {
		return IntStream.range(0, sevaList.size())
				.filter(i -> sevaList.get(i).getId().equals(sevaId))
				.findFirst();
	}

	public boolean moveSevaUp(String sevaId) {
		OptionalInt optionalIndex = findSevaIndexById(sevaId);
		if (optionalIndex.isEmpty()) {
			System.err.println("Move Up Error: Seva ID not found in list: " + sevaId);
			return false;
		}
		int currentIndex = optionalIndex.getAsInt();

		if (currentIndex == 0) {
			// Already the first item, cannot move up
			// System.out.println("Cannot move up the first item.");
			return false;
		}

		// Get the Seva to move up and the one currently above it
		Seva currentSeva = sevaList.get(currentIndex);
		Seva aboveSeva = sevaList.get(currentIndex - 1);

		// Get their current display orders
		int currentOrder = currentSeva.getDisplayOrder();
		int aboveOrder = aboveSeva.getDisplayOrder();

		// Swap orders in the database within a transaction
		Connection conn = null; // Declare connection outside try for finally block
		boolean success = false;
		try {
			conn = getConnection();
			conn.setAutoCommit(false); // Start transaction

			// Update the one above to have the current one's order
			updateSevaDisplayOrderInDB(conn, aboveSeva.getId(), currentOrder);
			// Update the current one to have the one above's order
			updateSevaDisplayOrderInDB(conn, currentSeva.getId(), aboveOrder);

			conn.commit(); // Commit transaction
			success = true;
			System.out.println("Successfully moved up Seva ID: " + sevaId);

		} catch (SQLException e) {
			System.err.println("Error during move Seva Up transaction for ID " + sevaId + ": " + e.getMessage());
			if (conn != null) {
				try {
					System.err.println("Rolling back transaction...");
					conn.rollback();
				} catch (SQLException ex) {
					System.err.println("Error rolling back transaction: " + ex.getMessage());
				}
			}
			success = false;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true); // Reset auto-commit
					conn.close(); // Close connection
				} catch (SQLException ex) {
					System.err.println("Error closing connection/resetting auto-commit: " + ex.getMessage());
				}
			}
		}

		// If DB update was successful, update in-memory list order and re-sort
		if (success) {
			currentSeva.setDisplayOrder(aboveOrder);
			aboveSeva.setDisplayOrder(currentOrder);
			// Re-sort the list based on the updated displayOrder values
			sevaList.sort(Comparator.comparingInt(Seva::getDisplayOrder));
		}

		return success;
	}

	public boolean moveSevaDown(String sevaId) {
		OptionalInt optionalIndex = findSevaIndexById(sevaId);
		if (optionalIndex.isEmpty()) {
			System.err.println("Move Down Error: Seva ID not found in list: " + sevaId);
			return false;
		}
		int currentIndex = optionalIndex.getAsInt();

		if (currentIndex >= sevaList.size() - 1) {
			// Already the last item, cannot move down
			// System.out.println("Cannot move down the last item.");
			return false;
		}

		// Get the Seva to move down and the one currently below it
		Seva currentSeva = sevaList.get(currentIndex);
		Seva belowSeva = sevaList.get(currentIndex + 1);

		// Get their current display orders
		int currentOrder = currentSeva.getDisplayOrder();
		int belowOrder = belowSeva.getDisplayOrder();

		// Swap orders in the database within a transaction
		Connection conn = null;
		boolean success = false;
		try {
			conn = getConnection();
			conn.setAutoCommit(false); // Start transaction

			// Update the one below to have the current one's order
			updateSevaDisplayOrderInDB(conn, belowSeva.getId(), currentOrder);
			// Update the current one to have the one below's order
			updateSevaDisplayOrderInDB(conn, currentSeva.getId(), belowOrder);

			conn.commit(); // Commit transaction
			success = true;
			System.out.println("Successfully moved down Seva ID: " + sevaId);

		} catch (SQLException e) {
			System.err.println("Error during move Seva Down transaction for ID " + sevaId + ": " + e.getMessage());
			if (conn != null) {
				try {
					System.err.println("Rolling back transaction...");
					conn.rollback();
				} catch (SQLException ex) {
					System.err.println("Error rolling back transaction: " + ex.getMessage());
				}
			}
			success = false;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true); // Reset auto-commit
					conn.close(); // Close connection
				} catch (SQLException ex) {
					System.err.println("Error closing connection/resetting auto-commit: " + ex.getMessage());
				}
			}
		}

		// If DB update was successful, update in-memory list order and re-sort
		if (success) {
			currentSeva.setDisplayOrder(belowOrder);
			belowSeva.setDisplayOrder(currentOrder);
			// Re-sort the list based on the updated displayOrder values
			sevaList.sort(Comparator.comparingInt(Seva::getDisplayOrder));
		}

		return success;
	}
}