package com.pranav.temple_software;

import com.pranav.temple_software.utils.BackupService;
import com.pranav.temple_software.utils.DatabaseManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.Properties;

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

		new Thread(this::checkForUpdates).start();
	}

	private void checkForUpdates() {
		// IMPORTANT: Replace these URLs with your actual GitHub URLs from Steps 1 and 2
		String versionUrl = "https://raw.githubusercontent.com/pranav6266/temple-software/refs/heads/main/version.txt";
		String downloadUrl = "https://github.com/your-username/pranav6266-temple-software/releases/latest"; // Link to the latest release page

		try {
			// 1. Get the current version from application.properties
			Properties props = new Properties();
			try (InputStream is = Launcher.class.getResourceAsStream("/application.properties")) {
				props.load(is);
			}
			String currentVersion = props.getProperty("app.version");

			// 2. Fetch the latest version from your URL
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(versionUrl)).build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			String latestVersion = response.body().trim();

			// 3. Compare versions
			if (currentVersion != null && !currentVersion.equalsIgnoreCase(latestVersion)) {
				// If versions are different, show an alert on the UI thread
				Platform.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Update Available");
					alert.setHeaderText("A new version (" + latestVersion + ") is available!");
					alert.setContentText("Would you like to go to the download page now?");

					ButtonType downloadButton = new ButtonType("Download");
					alert.getButtonTypes().setAll(downloadButton, ButtonType.CANCEL);

					Optional<ButtonType> result = alert.showAndWait();
					if (result.isPresent() && result.get() == downloadButton) {
						try {
							// Open the download link in the user's default browser
							Desktop.getDesktop().browse(new URI(downloadUrl));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			} else {
				System.out.println("Application is up to date.");
			}
		} catch (Exception e) {
			System.err.println("Could not check for updates: " + e.getMessage());
			// Fail silently, as this is not a critical feature
		}
	}
}
