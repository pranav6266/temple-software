package com.pranav.temple_software.controllers;

import com.pranav.temple_software.repositories.CredentialsRepository;
import com.pranav.temple_software.utils.DatabaseManager;
import com.pranav.temple_software.utils.PasswordUtils;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;

public class AdminPanelController {
	private static final Logger logger = LoggerFactory.getLogger(AdminPanelController.class);

	// Password Management Tab
	@FXML private PasswordField normalPassField;
	@FXML private PasswordField normalPassConfirmField;
	@FXML private PasswordField specialPassField;
	@FXML private PasswordField specialPassConfirmField;
	@FXML private TextField adminUserField;
	@FXML private PasswordField adminPassField;
	@FXML private PasswordField adminPassConfirmField;
	@FXML private Label statusLabel;
	// Log Viewer Tab
	@FXML private ComboBox<File> logFilesComboBox;
	@FXML private TextArea logContentArea;
	// Database Editor Tab
	@FXML private ComboBox<String> tablesComboBox;
	@FXML private TableView<ObservableList<String>> databaseTableView;

	private final CredentialsRepository credentialsRepository = new CredentialsRepository();
	private final String logDir = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "TempleSoftware" + File.separator + "logs";
	private final Set<String> allowedTableNames = new HashSet<>();

	@FXML
	public void initialize() {
		// Initialize Log Viewer
		handleRefreshLogs(null);
		logFilesComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				loadLogFileContent(newVal);
			}
		});

		// Initialize Database Editor
		loadTableNames();
		tablesComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				loadTableData(newVal);
			}
		});
	}

	// --- Password Management Logic ---
	@FXML
	void handleChangeNormalPassword(ActionEvent event) {
		String pass1 = normalPassField.getText();
		String pass2 = normalPassConfirmField.getText();
		updatePassword("NORMAL_PASSWORD", pass1, pass2);
	}

	@FXML
	void handleChangeSpecialPassword(ActionEvent event) {
		String pass1 = specialPassField.getText();
		String pass2 = specialPassConfirmField.getText();
		updatePassword("SPECIAL_PASSWORD", pass1, pass2);
	}

	@FXML
	void handleChangeAdminCredentials(ActionEvent event) {
		String username = adminUserField.getText();
		String pass1 = adminPassField.getText();
		String pass2 = adminPassConfirmField.getText();
		if (username == null || username.isBlank()) {
			showStatus("Admin username cannot be empty.", true);
			return;
		}

		boolean userUpdated = credentialsRepository.updateCredential("ADMIN_USERNAME", username);
		if (userUpdated) {
			showStatus("Admin username updated successfully.", false);
		} else {
			showStatus("Failed to update admin username.", true);
		}

		if (pass1 != null && !pass1.isEmpty()) {
			updatePassword("ADMIN_PASSWORD", pass1, pass2);
		}
	}

	private void updatePassword(String key, String pass1, String pass2) {
		if (pass1 == null || pass1.isBlank()) {
			showStatus("Password cannot be empty.", true);
			return;
		}
		if (!pass1.equals(pass2)) {
			showStatus("Passwords do not match.", true);
			return;
		}

		String hashedPassword = PasswordUtils.hashPassword(pass1);
		boolean success = credentialsRepository.updateCredential(key, hashedPassword);
		if (success) {
			showStatus(key.replace("_", " ") + " updated successfully!", false);
			clearPasswordFields(key);
		} else {
			showStatus("Error updating " + key + ". Check logs.", true);
		}
	}

	// --- Log Viewer Logic ---
	@FXML
	void handleRefreshLogs(ActionEvent event) {
		logContentArea.clear();
		logFilesComboBox.getItems().clear();
		File logDirectory = new File(logDir);
		if (logDirectory.exists() && logDirectory.isDirectory()) {
			File[] logFiles = logDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".log"));
			if (logFiles != null) {
				logFilesComboBox.getItems().addAll(logFiles);
			}
		}
	}

	private void loadLogFileContent(File logFile) {
		try {
			String content = Files.readString(logFile.toPath());
			logContentArea.setText(content);
		} catch (IOException e) {
			logger.error("Error reading log file: {}", logFile.getAbsolutePath(), e);
			logContentArea.setText("Error reading log file: " + e.getMessage());
		}
	}

	// --- Database Editor Logic ---
	private void loadTableNames() {
		try (Connection conn = DatabaseManager.getConnection()) {
			DatabaseMetaData metaData = conn.getMetaData();
			ResultSet rs = metaData.getTables(null, "PUBLIC", "%", new String[]{"TABLE"});
			ObservableList<String> tableNames = FXCollections.observableArrayList();
			allowedTableNames.clear();
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				tableNames.add(tableName);
				allowedTableNames.add(tableName);
			}
			tablesComboBox.setItems(tableNames);
		} catch (SQLException e) {
			logger.error("Failed to load table names from database metadata", e);
		}
	}

	private void loadTableData(String tableName) {
		if (!allowedTableNames.contains(tableName)) {
			showAlert("Security Warning", "Access to this table is not permitted.");
			return;
		}

		databaseTableView.getColumns().clear();
		databaseTableView.getItems().clear();

		try (Connection conn = DatabaseManager.getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			for (int i = 1; i <= columnCount; i++) {
				final int j = i - 1;
				TableColumn<ObservableList<String>, String> column = new TableColumn<>(metaData.getColumnName(i));
				column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j)));
				column.setCellFactory(TextFieldTableCell.forTableColumn());
				column.setOnEditCommit(event -> {
					ObservableList<String> row = event.getRowValue();
					row.set(event.getTablePosition().getColumn(), event.getNewValue());
				});
				databaseTableView.getColumns().add(column);
			}

			ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
			while (rs.next()) {
				ObservableList<String> row = FXCollections.observableArrayList();
				for (int i = 1; i <= columnCount; i++) {
					row.add(rs.getString(i));
				}
				data.add(row);
			}
			databaseTableView.setItems(data);

		} catch (SQLException e) {
			logger.error("Failed to load data for table: {}", tableName, e);
		}
	}

	@FXML
	void handleSaveChanges(ActionEvent event) {
		String selectedTable = tablesComboBox.getSelectionModel().getSelectedItem();
		if (selectedTable == null) return;
		if (!allowedTableNames.contains(selectedTable)) return;

		TableColumn<ObservableList<String>, ?> primaryKeyColumn = databaseTableView.getColumns().get(0);
		String pkColumnName = primaryKeyColumn.getText();

		try (Connection conn = DatabaseManager.getConnection()) {
			conn.setAutoCommit(false);
			StringBuilder updateSql = new StringBuilder("UPDATE " + selectedTable + " SET ");
			for (int i = 1; i < databaseTableView.getColumns().size(); i++) {
				updateSql.append(databaseTableView.getColumns().get(i).getText()).append(" = ?, ");
			}
			updateSql.setLength(updateSql.length() - 2);
			updateSql.append(" WHERE ").append(pkColumnName).append(" = ?");

			try (PreparedStatement pstmt = conn.prepareStatement(updateSql.toString())) {
				for (ObservableList<String> row : databaseTableView.getItems()) {
					int paramIndex = 1;
					for (int i = 1; i < row.size(); i++) {
						pstmt.setString(paramIndex++, row.get(i));
					}
					pstmt.setString(paramIndex, row.get(0));
					pstmt.addBatch();
				}
				pstmt.executeBatch();
				conn.commit();
				showStatus("Changes saved successfully to " + selectedTable, false);
			} catch (SQLException e) {
				conn.rollback();
				showStatus("Error saving changes: " + e.getMessage(), true);
				logger.error("Error saving changes to table {}", selectedTable, e);
			}
		} catch (SQLException e) {
			logger.error("Failed to get database connection for saving changes.", e);
		}
	}

	@FXML
	void handleDeleteRow(ActionEvent event) {
		String selectedTable = tablesComboBox.getSelectionModel().getSelectedItem();
		ObservableList<String> selectedRow = databaseTableView.getSelectionModel().getSelectedItem();
		if (selectedTable == null || selectedRow == null) {
			showStatus("Please select a row to delete.", true);
			return;
		}
		if (!allowedTableNames.contains(selectedTable)) return;

		String pkColumnName = databaseTableView.getColumns().get(0).getText();
		String pkValue = selectedRow.get(0);

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to permanently delete row with " + pkColumnName + " = " + pkValue + "? This action cannot be undone.", ButtonType.YES, ButtonType.CANCEL);
		Optional<ButtonType> result = alert.showAndWait();

		if (result.isPresent() && result.get() == ButtonType.YES) {
			String sql = "DELETE FROM " + selectedTable + " WHERE " + pkColumnName + " = ?";
			try (Connection conn = DatabaseManager.getConnection();
			     PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setString(1, pkValue);
				int affectedRows = pstmt.executeUpdate();
				if (affectedRows > 0) {
					showStatus("Row deleted successfully.", false);
					loadTableData(selectedTable);
				} else {
					showStatus("Could not delete row.", true);
				}
			} catch (SQLException e) {
				showStatus("Error deleting row: " + e.getMessage(), true);
				logger.error("Error deleting row from table {} with PK {}", selectedTable, pkValue, e);
			}
		}
	}

	// --- Utility Methods ---
	private void showStatus(String message, boolean isError) {
		statusLabel.setText(message);
		statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
		PauseTransition delay = new PauseTransition(Duration.seconds(5));
		delay.setOnFinished(event -> statusLabel.setText(""));
		delay.play();
	}

	private void showAlert(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}

	private void clearPasswordFields(String key) {
		switch (key) {
			case "NORMAL_PASSWORD" -> {
				normalPassField.clear();
				normalPassConfirmField.clear();
			}
			case "SPECIAL_PASSWORD" -> {
				specialPassField.clear();
				specialPassConfirmField.clear();
			}
			case "ADMIN_PASSWORD" -> {
				adminPassField.clear();
				adminPassConfirmField.clear();
			}
		}
	}
}