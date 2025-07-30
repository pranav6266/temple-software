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
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

	@FXML
	private PasswordField passwordField;

	@FXML
	private Label errorLabel;

	private final CredentialsRepository credentialsRepository = new CredentialsRepository();

	@FXML
	public void initialize() {
		// Request focus on the password field upon showing the scene
		Platform.runLater(() -> passwordField.requestFocus());
	}

	@FXML
	void handleLogin(ActionEvent event) {
		String enteredPassword = passwordField.getText();
		if (enteredPassword == null || enteredPassword.isBlank()) {
			errorLabel.setText("Password cannot be empty.");
			return;
		}

		// Fetch the stored hashed password from the database
		Optional<String> storedHashOpt = credentialsRepository.getCredential("NORMAL_PASSWORD");

		if (storedHashOpt.isEmpty()) {
			errorLabel.setText("Error: Could not retrieve password from database.");
			return;
		}

		// Check if the entered password matches the stored hash
		if (PasswordUtils.checkPassword(enteredPassword, storedHashOpt.get())) {
			// On successful login, close the login stage and open the main application
			System.out.println("✅ Normal login successful.");
			closeCurrentStage();
			launchMainApplication();
		} else {
			// On failure, show an error message
			errorLabel.setText("Incorrect password. Please try again.");
			passwordField.clear();
		}
	}

	@FXML
	void handleAdminLoginLink(ActionEvent event) {
		// This will be implemented in Phase 4
		System.out.println("Admin login link clicked. To be implemented in Phase 4.");
		errorLabel.setText("Admin login is not yet implemented.");
	}

	private void launchMainApplication() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("/fxml/MainViewKannada.fxml"));
			Scene scene = new Scene(fxmlLoader.load());

			Stage mainStage = new Stage();
			MainController controller = fxmlLoader.getController();
			controller.setMainStage(mainStage);

			mainStage.setTitle("Temple Software");
			mainStage.setScene(scene);
			mainStage.setMaximized(true);
			mainStage.initStyle(StageStyle.UNDECORATED);
			mainStage.show();

		} catch (IOException e) {
			e.printStackTrace();
			errorLabel.setText("Fatal Error: Could not load the main application.");
		}
	}

	private void closeCurrentStage() {
		Stage stage = (Stage) passwordField.getScene().getWindow();
		stage.close();
	}
}
