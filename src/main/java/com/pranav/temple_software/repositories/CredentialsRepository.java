package com.pranav.temple_software.repositories;

import com.pranav.temple_software.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Repository class for accessing the Credentials table in the database.
 * This class handles fetching hashed passwords and other security credentials.
 */
public class CredentialsRepository {

	/**
	 * Fetches a credential value from the database based on its key.
	 *
	 * @param key The key of the credential to retrieve (e.g., "NORMAL_PASSWORD").
	 * @return An Optional containing the credential value if found, otherwise an empty Optional.
	 */
	public Optional<String> getCredential(String key) {
		String sql = "SELECT credential_value FROM Credentials WHERE credential_key = ?";

		try (Connection conn = DatabaseManager.getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, key);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				// Return the value found in the database
				return Optional.of(rs.getString("credential_value"));
			}

		} catch (SQLException e) {
			System.err.println("❌ Database error fetching credential for key '" + key + "': " + e.getMessage());
		}
		// Return an empty Optional if not found or if an error occurred
		return Optional.empty();
	}
}
