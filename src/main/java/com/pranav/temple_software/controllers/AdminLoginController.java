package com.pranav.temple_software.controllers;

import com.pranav.temple_software.Launcher;
import com.pranav.temple_software.repositories.CredentialsRepository;
import com.pranav.temple_software.utils.PasswordUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class AdminLoginController {
	private static final Logger logger = LoggerFactory.getLogger(AdminLoginController.class);

	@FXML private TextField usernameField;
	@FXML private PasswordField passwordField;
	@FXML private Label errorLabel;

	private final CredentialsRepository credentialsRepository = new CredentialsRepository();

	@FXML
	public void initialize() {
		Platform.runLater(() -> usernameField.requestFocus());
	}

	@FXML
	void handleAdminLogin(ActionEvent event) {
		String username = usernameField.getText();
		String password = passwordField.getText();

		if (username.isBlank() || password.isBlank()) {
			errorLabel.setText("Username and password are required.");
			return;
		}

		Optional<String> storedUserOpt = credentialsRepository.getCredential("ADMIN_USERNAME");
		Optional<String> storedHashOpt = credentialsRepository.getCredential("ADMIN_PASSWORD");

		if (storedUserOpt.isEmpty() || storedHashOpt.isEmpty()) {
			errorLabel.setText("Error: Could not retrieve admin credentials.");
			logger.error("Could not retrieve ADMIN_USERNAME or ADMIN_PASSWORD from the database.");
			return;
		}

		boolean userMatches = username.equals(storedUserOpt.get());
		boolean passMatches = PasswordUtils.checkPassword(password, storedHashOpt.get());

		if (userMatches && passMatches) {
			logger.info("Admin login successful for user '{}'.", username);
			closeCurrentStage();
			launchAdminPanel();
		} else {
			logger.warn("Failed admin login attempt for user '{}'.", username);
			errorLabel.setText("Invalid admin username or password.");
		}
	}

	@FXML
	void handleBackToUserLogin(ActionEvent event) {
		closeCurrentStage();
		try {
			new Launcher().start(new Stage());
		} catch (IOException e) {
			logger.error("Failed to relaunch the main application to go back to user login.", e);
		}
	}

	private void launchAdminPanel() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("/fxml/AdminPanelView.fxml"));
			Scene scene = new Scene(fxmlLoader.load());
			Stage adminStage = new Stage();
			adminStage.setTitle("Administrator Panel");
			adminStage.setScene(scene);
			adminStage.setMaximized(true);
			adminStage.show();
		} catch (IOException e) {
			logger.error("Fatal Error: Could not load the Admin Panel FXML.", e);
			errorLabel.setText("Fatal Error: Could not load the admin panel.");
		}
	}

	private void closeCurrentStage() {
		Stage stage = (Stage) usernameField.getScene().getWindow();
		stage.close();
	}
}