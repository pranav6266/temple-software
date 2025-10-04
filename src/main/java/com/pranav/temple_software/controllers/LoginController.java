package com.pranav.temple_software.controllers;

import com.pranav.temple_software.Launcher;
import com.pranav.temple_software.repositories.CredentialsRepository;
import com.pranav.temple_software.utils.BackupService;
import com.pranav.temple_software.utils.PasswordUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@FXML private PasswordField passwordField;
	@FXML private Label errorLabel;

	private final CredentialsRepository credentialsRepository = new CredentialsRepository();

	@FXML
	public void initialize() {
		Platform.runLater(() -> passwordField.requestFocus());
	}

	@FXML
	void handleLogin() {
		String enteredPassword = passwordField.getText();
		if (enteredPassword == null || enteredPassword.isBlank()) {
			errorLabel.setText("Password cannot be empty.");
			return;
		}

		Optional<String> storedHashOpt = credentialsRepository.getCredential("NORMAL_PASSWORD");

		if (storedHashOpt.isEmpty()) {
			errorLabel.setText("Error: Could not retrieve password from database.");
			logger.error("Could not retrieve NORMAL_PASSWORD from the database.");
			return;
		}

		if (PasswordUtils.checkPassword(enteredPassword, storedHashOpt.get())) {
			logger.info("Normal user login successful.");
			closeCurrentStage();
			launchMainApplication();
		} else {
			logger.warn("Failed normal user login attempt.");
			errorLabel.setText("Incorrect password. Please try again.");
			passwordField.clear();
		}
	}

	@FXML
	void handleAdminLoginLink() {
		logger.debug("Redirecting to Admin Login screen.");
		closeCurrentStage();
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("/fxml/AdminLoginView.fxml"));
			Scene scene = new Scene(fxmlLoader.load());
			Stage adminLoginStage = new Stage();
			adminLoginStage.setTitle("Temple Software - Admin Login");
			adminLoginStage.setScene(scene);
			adminLoginStage.setResizable(false);
			adminLoginStage.show();
		} catch (IOException e) {
			logger.error("Error loading the Admin Login screen FXML.", e);
			errorLabel.setText("Error: Could not load the admin login screen.");
		}
	}

	private void launchMainApplication() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("/fxml/MainViewKannada.fxml"));
			Scene scene = new Scene(fxmlLoader.load());

			Stage mainStage = new Stage();
			MainController controller = fxmlLoader.getController();
			controller.setMainStage(mainStage);

			mainStage.setTitle("Cherkabe Temple Receipt Printer");
			mainStage.setScene(scene);
			mainStage.setResizable(true);
			mainStage.setWidth(1000);
			mainStage.setHeight(720);
			mainStage.show();

			// ADD THIS ENTIRE BLOCK
			mainStage.setOnCloseRequest((WindowEvent event) -> {
				event.consume(); // Prevent the window from closing immediately

				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.initOwner(mainStage);
				alert.setTitle("Exit Confirmation");
				alert.setHeaderText(null);
				alert.setContentText("Are you sure you want to exit the application?");
				Optional<ButtonType> result = alert.showAndWait();

				if (result.isPresent() && result.get() == ButtonType.OK) {
					logger.info("Application is closing. Performing automatic backup...");
					BackupService.createAutomaticBackup();
					Platform.exit();
					System.exit(0);
				}
			});

		} catch (IOException e) {
			logger.error("Fatal Error: Could not load the main application FXML.", e);
			errorLabel.setText("Fatal Error: Could not load the main application.");
		}
	}

	private void closeCurrentStage() {
		Stage stage = (Stage) passwordField.getScene().getWindow();
		stage.close();
	}
}