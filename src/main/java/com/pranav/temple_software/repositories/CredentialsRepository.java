package com.pranav.temple_software.repositories;

import com.pranav.temple_software.utils.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class CredentialsRepository {
	private static final Logger logger = LoggerFactory.getLogger(CredentialsRepository.class);

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
			logger.error("❌ Database error fetching credential for key '{}'", key, e);
		}
		return Optional.empty();
	}

	public boolean updateCredential(String key, String value) {
		String sql = "UPDATE Credentials SET credential_value = ? WHERE credential_key = ?";
		try (Connection conn = DatabaseManager.getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, value);
			pstmt.setString(2, key);
			int affectedRows = pstmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			logger.error("❌ Database error updating credential for key '{}'", key, e);
			return false;
		}
	}
}