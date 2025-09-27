package com.pranav.temple_software.repositories;

import com.pranav.temple_software.models.KaryakramaSeva;
import com.pranav.temple_software.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KaryakramaSevaRepository {
	private Connection getConnection() throws SQLException {
		return DatabaseManager.getConnection();
	}

	public List<KaryakramaSeva> getSevasForKaryakrama(int karyakramaId) {
		List<KaryakramaSeva> sevaList = new ArrayList<>();
		String sql = "SELECT * FROM KaryakramaSevas WHERE karyakrama_id = ? ORDER BY seva_name";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, karyakramaId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				sevaList.add(new KaryakramaSeva(
						rs.getInt("seva_id"),
						rs.getInt("karyakrama_id"),
						rs.getString("seva_name"),
						rs.getDouble("amount")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sevaList;
	}

	public boolean addSevaToKaryakrama(int karyakramaId, String sevaName, double amount) {
		String sql = "INSERT INTO KaryakramaSevas (karyakrama_id, seva_name, amount) VALUES (?, ?, ?)";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, karyakramaId);
			pstmt.setString(2, sevaName);
			pstmt.setDouble(3, amount);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateSeva(int sevaId, String newName, double newAmount) {
		String sql = "UPDATE KaryakramaSevas SET seva_name = ?, amount = ? WHERE seva_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newName);
			pstmt.setDouble(2, newAmount);
			pstmt.setInt(3, sevaId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteSeva(int sevaId) {
		String sql = "DELETE FROM KaryakramaSevas WHERE seva_id = ?";
		try (Connection conn = getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, sevaId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}