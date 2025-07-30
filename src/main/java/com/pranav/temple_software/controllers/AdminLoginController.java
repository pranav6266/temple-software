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
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Optional;

public class AdminLoginController {

	@FXML
	private TextField usernameField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private Label errorLabel;

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
			return;
		}

		boolean userMatches = username.equals(storedUserOpt.get());
		boolean passMatches = PasswordUtils.checkPassword(password, storedHashOpt.get());

		if (userMatches && passMatches) {
			System.out.println("✅ Admin login successful.");
			closeCurrentStage();
			launchAdminPanel();
		} else {
			errorLabel.setText("Invalid admin username or password.");
		}
	}

	@FXML
	void handleBackToUserLogin(ActionEvent event) {
		closeCurrentStage();
		try {
			// Relaunch the application which starts at the user login
			new Launcher().start(new Stage());
		} catch (IOException e) {
			e.printStackTrace();
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
			e.printStackTrace();
			errorLabel.setText("Fatal Error: Could not load the admin panel.");
		}
	}

	private void closeCurrentStage() {
		Stage stage = (Stage) usernameField.getScene().getWindow();
		stage.close();
	}
}
