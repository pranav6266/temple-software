package com.pranav.temple_software.controllers.menuControllers.ShashwathaPoojaManager;

import ch.qos.logback.classic.Logger;
import com.pranav.temple_software.repositories.CredentialsRepository;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ShashwathaPoojaManagerController {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(ShashwathaPoojaManagerController.class);

	@FXML private Label statusLabel;
	@FXML private TextField amountField;
	@FXML public Button saveButton;
	@FXML private Button cancelButton;

	private final CredentialsRepository credentialsRepository = new CredentialsRepository();
	private static final String PRICE_KEY = "SHASHWATHA_POOJA_PRICE";

	@FXML
	public void initialize() {
		Optional<String> currentAmountOpt = credentialsRepository.getCredential(PRICE_KEY);
		currentAmountOpt.ifPresent(amount -> amountField.setText(amount));
		amountField.textProperty().addListener((_, oldValue, newValue) -> {
			if (!newValue.matches("\\d*(\\.\\d*)?")) {
				amountField.setText(oldValue);
			}
		});
	}

	@FXML
	public void handleSave() {
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
				PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
				delay.setOnFinished(_ -> closeWindow());
				delay.play();
			} else {
				showStatus("Failed to update price. Check logs.", true);
			}
		} catch (NumberFormatException e) {
			logger.warn("Invalid amount format entered: {}", newAmountStr);
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
		delay.setOnFinished(_ -> statusLabel.setText(""));
		delay.play();
	}
}