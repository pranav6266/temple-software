package com.pranav.temple_software.controllers.menuControllers.ShashwathaPoojaManager;

import com.pranav.temple_software.repositories.CredentialsRepository;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

public class ShashwathaPoojaManagerController {

	@FXML private Label statusLabel;
	@FXML private TextField amountField;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;

	private final CredentialsRepository credentialsRepository = new CredentialsRepository();
	private static final String PRICE_KEY = "SHASHWATHA_POOJA_PRICE";

	@FXML
	public void initialize() {
		// Load the current amount and display it
		Optional<String> currentAmountOpt = credentialsRepository.getCredential(PRICE_KEY);
		currentAmountOpt.ifPresent(amount -> amountField.setText(amount));

		// Add a listener to allow only numeric input
		amountField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*(\\.\\d*)?")) {
				amountField.setText(oldValue);
			}
		});
	}

	@FXML
	private void handleSave() {
		String newAmountStr = amountField.getText();
		if (newAmountStr == null || newAmountStr.trim().isEmpty()) {
			showStatus("Amount cannot be empty.", true);
			return;
		}

		try {
			double newAmount = Double.parseDouble(newAmountStr);
			if (newAmount < 0) {
				showStatus("Amount cannot be negative.", true);
				return;
			}

			boolean success = credentialsRepository.updateCredential(PRICE_KEY, String.format("%.2f", newAmount));
			if (success) {
				showStatus("Price updated successfully!", false);
				// Optionally close window after a short delay
				PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
				delay.setOnFinished(event -> closeWindow());
				delay.play();
			} else {
				showStatus("Failed to update price. Check logs.", true);
			}

		} catch (NumberFormatException e) {
			showStatus("Invalid amount format.", true);
		}
	}

	@FXML
	private void handleCancel() {
		closeWindow();
	}

	private void closeWindow() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}

	private void showStatus(String message, boolean isError) {
		statusLabel.setText(message);
		statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
		PauseTransition delay = new PauseTransition(Duration.seconds(4));
		delay.setOnFinished(event -> statusLabel.setText(""));
		delay.play();
	}
}