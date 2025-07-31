package com.pranav.temple_software;

import com.pranav.temple_software.utils.BackupService;
import com.pranav.temple_software.utils.DatabaseManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Launcher extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
		// Initialize the database first. This is crucial.
		BackupService.runStartupCheck();
		new DatabaseManager();

		// --- MODIFIED STARTUP FLOW ---
		// Instead of loading the main view, we now load the LoginView first.
		FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource(
				"/fxml/LoginView.fxml")); // <-- CHANGED
		Scene scene = new Scene(fxmlLoader.load());

		stage.setTitle("Temple Software - Login"); // <-- CHANGED
		stage.setScene(scene);
		stage.setResizable(false); // Login window should not be resizable
		stage.initStyle(StageStyle.DECORATED); // Use a standard window for the login
		stage.show();
	}
}
