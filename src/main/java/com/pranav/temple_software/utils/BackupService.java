package com.pranav.temple_software.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class BackupService {

	private static final String APP_DATA_FOLDER = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "TempleSoftware";
	private static final Path DB_FOLDER_PATH = Paths.get(APP_DATA_FOLDER, "db");
	private static final Path BACKUP_FOLDER_PATH = Paths.get(APP_DATA_FOLDER, "backups");
	private static final String DB_FILE_NAME = "temple_data.mv.db";
	private static final Path DB_FILE_PATH = DB_FOLDER_PATH.resolve(DB_FILE_NAME);

	/**
	 * Performs a check on the database when the application starts.
	 * If the main database is missing or corrupt, it prompts the user to restore from the latest auto-backup.
	 */
	public static void runStartupCheck() {
		if (!Files.exists(DB_FILE_PATH)) {
			System.out.println("Database file not found. Checking for backups...");
			findAndRestoreLatestBackup();
		}
	}

	/**
	 * Creates a timestamped backup of the database file in the automatic backup directory.
	 */
	public static void createAutomaticBackup() {
		try {
			DatabaseManager.closeConnection(); // Ensure DB connection is closed
			if (!Files.exists(BACKUP_FOLDER_PATH)) {
				Files.createDirectories(BACKUP_FOLDER_PATH);
			}

			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
			Path backupFilePath = BACKUP_FOLDER_PATH.resolve("backup_" + timestamp + ".mv.db");

			if (Files.exists(DB_FILE_PATH)) {
				Files.copy(DB_FILE_PATH, backupFilePath, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Automatic backup created successfully at: " + backupFilePath);
				cleanupOldBackups();
			}
		} catch (IOException e) {
			System.err.println("Failed to create automatic backup: " + e.getMessage());
		}
	}

	/**
	 * Opens a dialog for the user to choose between creating a manual backup or restoring from one.
	 * @param owner The parent stage for the dialogs.
	 */
	public static void showBackupRestoreDialog(Stage owner) {
		List<String> choices = Arrays.asList("Create a Manual Backup", "Restore from a Backup File");
		ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.getFirst(), choices);
		dialog.setTitle("Backup and Restore");
		dialog.setHeaderText("Choose an operation");
		dialog.setContentText("What would you like to do?");
		dialog.initOwner(owner);

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(choice -> {
			if (choice.equals("Create a Manual Backup")) {
				createManualBackup(owner);
			} else if (choice.equals("Restore from a Backup File")) {
				restoreFromManualBackup(owner);
			}
		});
	}

	/**
	 * Opens a file chooser for the user to save a manual backup of the database.
	 * @param owner The parent stage for the file chooser.
	 */
	private static void createManualBackup(Stage owner) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Database Backup");
		fileChooser.setInitialFileName("manual_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".mv.db");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Database Files", "*.mv.db"));

		File backupFile = fileChooser.showSaveDialog(owner);
		if (backupFile != null) {
			try {
				DatabaseManager.closeConnection();
				Files.copy(DB_FILE_PATH, backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				showAlert(Alert.AlertType.INFORMATION, "Success", "Manual backup created successfully at:\n" + backupFile.getAbsolutePath());
			} catch (IOException e) {
				showAlert(Alert.AlertType.ERROR, "Error", "Failed to create manual backup: " + e.getMessage());
			}
		}
	}

	/**
	 * Opens a file chooser for the user to select a database file to restore.
	 * @param owner The parent stage for the file chooser and alerts.
	 */
	private static void restoreFromManualBackup(Stage owner) {
		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
		confirmation.setTitle("Confirm Restore");
		confirmation.setHeaderText("This will overwrite all current data.");
		confirmation.setContentText("This action cannot be undone. Are you sure you want to proceed?");
		confirmation.initOwner(owner);

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select Database Backup to Restore");
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Database Files", "*.mv.db"));

			File backupFile = fileChooser.showOpenDialog(owner);
			if (backupFile != null && Files.exists(backupFile.toPath())) {
				try {
					DatabaseManager.closeConnection();
					Files.copy(backupFile.toPath(), DB_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
					showAlert(Alert.AlertType.INFORMATION, "Restore Successful", "Database has been restored. Please restart the application for the changes to take effect.");
					Platform.exit();
				} catch (IOException e) {
					showAlert(Alert.AlertType.ERROR, "Restore Failed", "Could not restore the database: " + e.getMessage());
				}
			}
		}
	}

	private static void findAndRestoreLatestBackup() {
		try {
			if (!Files.exists(BACKUP_FOLDER_PATH)) return;

			Optional<Path> latestBackup = Files.list(BACKUP_FOLDER_PATH)
					.filter(p -> p.toString().endsWith(".mv.db"))
					.max(Comparator.comparingLong((Path p) -> p.toFile().lastModified()));

			latestBackup.ifPresent(path -> Platform.runLater(() -> {
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle("Database Missing");
				alert.setHeaderText("The main database file is missing or corrupted.");
				alert.setContentText("A backup from " + path.toFile().getName() + " is available. Would you like to restore it?");

				Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.OK) {
					try {
						Files.createDirectories(DB_FOLDER_PATH);
						Files.copy(path, DB_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
						showAlert(Alert.AlertType.INFORMATION, "Restore Successful", "Backup restored. The application will now start.");
					} catch (IOException e) {
						showAlert(Alert.AlertType.ERROR, "Restore Failed", "Could not restore the backup: " + e.getMessage());
						Platform.exit();
					}
				} else {
					showAlert(Alert.AlertType.WARNING, "Startup Halted", "Cannot start without a database. Please restore a backup manually.");
					Platform.exit();
				}
			}));
		} catch (IOException e) {
			System.err.println("Error while searching for backups: " + e.getMessage());
		}
	}

	private static void cleanupOldBackups() {
		try {
			if (!Files.exists(BACKUP_FOLDER_PATH)) return;

			Files.list(BACKUP_FOLDER_PATH)
					.filter(p -> p.toString().endsWith(".mv.db"))
					.sorted(Comparator.comparingLong((Path p) -> p.toFile().lastModified()).reversed())
					.skip(7) // Keep the 7 most recent backups
					.forEach(p -> {
						try {
							Files.delete(p);
							System.out.println("Deleted old backup: " + p.getFileName());
						} catch (IOException e) {
							System.err.println("Failed to delete old backup " + p.getFileName() + ": " + e.getMessage());
						}
					});
		} catch (IOException e) {
			System.err.println("Error during backup cleanup: " + e.getMessage());
		}
	}

	private static void showAlert(Alert.AlertType type, String title, String content) {
		Platform.runLater(() -> {
			Alert alert = new Alert(type);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(content);
			alert.showAndWait();
		});
	}
}