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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class Launcher extends Application {
	private static final Logger logger = LoggerFactory.getLogger(Launcher.class);
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

		stage.setTitle("Cherkabe Temple Receipt Printer- Login"); // <-- CHANGED
		stage.setScene(scene);
		stage.setResizable(false);
		// Login window should not be resizable
		stage.initStyle(StageStyle.DECORATED); // Use a standard window for the login
		stage.show();

		// Pass the primary stage to the update checker
		new Thread(() -> checkForUpdates(stage)).start();
	}

	private void checkForUpdates(Stage owner) {
		// IMPORTANT: This URL points to your GitHub repository's version.txt file
		String versionUrl = "https://raw.githubusercontent.com/pranav6266/temple-software/refs/heads/main/version.txt";
		String downloadUrl = "https://github.com/pranav6266/temple-software/releases/latest";

		try {
			// MODIFICATION START: Corrected version reading logic
			// 1. Get the current version from version.txt inside the application resources.
			String currentVersion;
			try (InputStream is = Launcher.class.getResourceAsStream("/version.txt")) {
				if (is == null) {
					logger.error("Could not find version.txt in resources. Skipping update check.");
					return;
				}
				// Read all bytes from the file and convert to a trimmed string
				currentVersion = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
			}
			// MODIFICATION END

			// 2. Fetch the latest version from your URL
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(versionUrl)).build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			String latestVersion = response.body().trim();

			// 3. Compare versions
			if (!currentVersion.isEmpty() && !currentVersion.equalsIgnoreCase(latestVersion)) {
				// If versions are different, show an alert on the UI thread
				Platform.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.initOwner(owner); // Set owner to prevent it from hiding
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
							logger.error("Error occurred while trying to get new version ",e);
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