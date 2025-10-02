package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.Karyakrama;
import com.pranav.temple_software.utils.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KaryakramaRepository {
	private static final Logger logger = LoggerFactory.getLogger(KaryakramaRepository.class);

	private Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	public List<Karyakrama> getAllKaryakramagalu() {
		List<Karyakrama> karyakramaList = new ArrayList<>();
		String sql = "SELECT * FROM Karyakramagalu WHERE is_active = TRUE ORDER BY karyakrama_name";
		try (Connection conn = getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				karyakramaList.add(new Karyakrama(
						rs.getInt("karyakrama_id"),
						rs.getString("karyakrama_name"),
						rs.getBoolean("is_active")
				));
			}
		} catch (SQLException e) {
			logger.error("Error fetching all Karyakrama entries", e);
		}
		return karyakramaList;
	}

	public void addKaryakrama(String name) {
		String sql = "INSERT INTO Karyakramagalu (karyakrama_name) VALUES (?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error adding Karyakrama with name: {}", name, e);
		}
	}

	public void deleteKaryakrama(int id) {
		String sql = "DELETE FROM Karyakramagalu WHERE karyakrama_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error deleting Karyakrama with ID: {}", id, e);
		}
	}
}