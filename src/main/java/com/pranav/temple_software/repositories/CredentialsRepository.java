package com.pranav.temple_software.repositories;

import com.pranav.temple_software.utils.DatabaseManager;
import com.pranav.temple_software.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class CredentialsRepository {

	public Optional<String> getCredential(String key) {
		String sql = "SELECT credential_value FROM Credentials WHERE credential_key = ?";
		try (Connection conn = DatabaseManager.getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, key);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return Optional.of(rs.getString("credential_value"));
			}
		} catch (SQLException e) {
			System.err.println("❌ Database error fetching credential for key '" + key + "': " + e.getMessage());
		}
		return Optional.empty();
	}

	/**
	 * Updates an existing credential in the database.
	 *
	 * @param key   The key of the credential to update (e.g., "NORMAL_PASSWORD").
	 * @param value The new value for the credential.
	 * @return true if the update was successful, false otherwise.
	 */
	public boolean updateCredential(String key, String value) {
		String sql = "UPDATE Credentials SET credential_value = ? WHERE credential_key = ?";
		try (Connection conn = DatabaseManager.getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, value);
			pstmt.setString(2, key);
			int affectedRows = pstmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			System.err.println("❌ Database error updating credential for key '" + key + "': " + e.getMessage());
			return false;
		}
	}
}
