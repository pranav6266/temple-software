package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.Karyakrama;
import com.pranav.temple_software.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KaryakramaRepository {
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
			e.printStackTrace();
		}
		return karyakramaList;
	}

	public boolean addKaryakrama(String name) {
		String sql = "INSERT INTO Karyakramagalu (karyakrama_name) VALUES (?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateKaryakrama(int id, String newName) {
		String sql = "UPDATE Karyakramagalu SET karyakrama_name = ? WHERE karyakrama_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newName);
			pstmt.setInt(2, id);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteKaryakrama(int id) {
		String sql = "DELETE FROM Karyakramagalu WHERE karyakrama_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}