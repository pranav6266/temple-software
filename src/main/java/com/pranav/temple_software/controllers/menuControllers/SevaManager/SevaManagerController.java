// File: Temple_Software/src/main/java/com/pranav/temple_software/controllers/menuControllers/SevaManager/SevaManagerController.java
package com.pranav.temple_software.controllers.menuControllers.SevaManager;

import com.pranav.temple_software.models.Seva;
import com.pranav.temple_software.repositories.SevaRepository; // Import SevaRepository
import javafx.event.ActionEvent;
import javafx.fxml.FXML; // Import FXML
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox; // For layout within grid cells

import java.util.Collection; // Import Collection
import java.util.Optional;

public class SevaManagerController {

	@FXML public GridPane sevaGridPane; //

	// *** ADDED FXML Fields for new Controls ***
	@FXML private TextField sevaIdField;
	@FXML private TextField sevaNameField;
	@FXML private TextField sevaAmountField;
	@FXML private Button addSevaButton; // Although action is handled by onAction, useful to have reference

	// *** ADDED SevaRepository Instance ***
	// IMPORTANT: This controller needs the SAME instance as MainController.
	// This simple initialization creates a NEW instance, which is usually WRONG.
	// Implement proper sharing (Dependency Injection, Singleton, or passing instance).
	private final SevaRepository sevaRepository = SevaRepository.getInstance();
	private int nextSevaId = 0;

	// *** ADDED initialize method ***
	@FXML
	public void initialize() {
		// Add validation listeners (optional but recommended)
		setupInputValidation();
		updateDefaultSevaId();
		// Load initial data
		refreshGridPane();
		sevaIdField.setEditable(false);
	}

	private void updateDefaultSevaId() {
		if (this.sevaRepository == null) return; // Guard clause
		try {
			int maxId = sevaRepository.getMaxSevaId();
			this.nextSevaId = maxId + 1;
			sevaIdField.setText(String.valueOf(this.nextSevaId));
		} catch (Exception e) {
			System.err.println("Error updating default Seva ID: " + e.getMessage());
			showAlert(Alert.AlertType.ERROR, "Error", "Could not determine the next Seva ID.");
			// Optionally disable adding new sevas if ID cannot be determined
			// addSevaButton.setDisable(true);
			sevaIdField.setText(""); // Clear field on error
			this.nextSevaId = -1; // Indicate error state
		}
	}

	// *** ADDED input validation (basic example) ***
	private void setupInputValidation() {
		// Allow only numbers (and optionally a decimal point) in Amount field
		sevaAmountField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null && !newValue.matches("\\d*(\\.\\d*)?")) {
				sevaAmountField.setText(oldValue != null ? oldValue : "");
			}
		});
	}


	@FXML
	public void handleAddSeva(ActionEvent actionEvent) {
		if (this.sevaRepository == null || this.nextSevaId <= 0) { // Check if repo/nextId is valid
			showAlert(Alert.AlertType.ERROR, "Error", "Cannot add Seva. Repository or next ID not initialized correctly.");
			return;
		}

		// Get ID directly from our calculated next ID, ignore text field value if it was editable
		String id = String.valueOf(this.nextSevaId); // Use the calculated next ID
		// String idFromField = sevaIdField.getText(); // No longer needed if field is read-only

		String name = sevaNameField.getText();
		String amountStr = sevaAmountField.getText();

		// Basic Validation
		if (name.isEmpty() || amountStr.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Input Error", "Please fill in Seva Name and Amount.");
			return;
		}

		if (this.sevaRepository.getAllSevas().stream().anyMatch(s -> s.getId().equals(id))) {
			showAlert(Alert.AlertType.ERROR, "Concurrency Error", "Seva ID '" + id + "' was unexpectedly found. Please refresh and try again.");
			updateDefaultSevaId(); // Recalculate ID
			return;
		}


		try {
			double amount = Double.parseDouble(amountStr);
			if (amount < 0) {
				showAlert(Alert.AlertType.WARNING, "Input Error", "Amount cannot be negative.");
				return;
			}

			Seva newSeva = new Seva(id, name, amount);
			boolean success = this.sevaRepository.addSevaToDB(newSeva);

			if (success) {
				showAlert(Alert.AlertType.INFORMATION, "Success", "Seva added successfully!");
				clearInputFields(); // Keep name/amount clear
				refreshGridPane(); // Update the display
				updateDefaultSevaId(); // IMPORTANT: Update the ID for the *next* Seva
			} else {
				showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add Seva to the database. Check logs.");
			}

		} catch (NumberFormatException e) {
			showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter a valid number for the Amount.");
		}
	}

	// *** NO CHANGE needed for handleDeleteSeva method signature ***
	// The actual deletion logic will be handled by the button created in refreshGridPane
	@FXML public void handleDeleteSeva(ActionEvent actionEvent) { /* Keep signature, logic moves */ }



	@FXML public void handleSave(ActionEvent actionEvent) { //
		System.out.println("Refresh button clicked.");
		updateDefaultSevaId(); // Ensure default ID is up-to-date on refresh
		refreshGridPane();
	}

	private void refreshGridPane() {
		if (this.sevaRepository == null) {
			System.err.println("refreshGridPane called but sevaRepository is null.");
			return;
		}
		sevaGridPane.getChildren().clear(); // Clear previous content

		// Add header row (displaying "Sl No." instead of "ID")
		sevaGridPane.add(new Label("Sl No."), 0, 0); // Changed header
		sevaGridPane.add(new Label("Name"), 1, 0);
		sevaGridPane.add(new Label("Amount (₹)"), 2, 0);
		sevaGridPane.add(new Label("Action"), 3, 0);

		// Get sevas (assuming they are loaded sorted by ID from repository)
		// If you later add display_order, the repository method should return them sorted by that column.
		Collection<Seva> sevas = this.sevaRepository.getAllSevas(); // Make sure this list is consistently sorted

		// Use loop index for display number
		int displayIndex = 1; // Start sequential numbering from 1

		for (Seva seva : sevas) {
			// *** CHANGE: Use displayIndex for the label text ***
			Label slNoLabel = new Label(String.valueOf(displayIndex));

			// Other labels remain the same
			Label nameLabel = new Label(seva.getName());
			Label amountLabel = new Label(String.format("%.2f", seva.getAmount()));

			Button deleteButton = new Button("Delete");
			// *** IMPORTANT: Use the REAL seva.getId() for the action ***
			final String currentSevaId = seva.getId(); // Store real ID for the lambda
			deleteButton.setOnAction(event -> {
				boolean deleted = this.sevaRepository.deleteSevaFromDB(currentSevaId); // Use real ID
				if (deleted) {
					refreshGridPane(); // Refresh grid (will re-calculate display numbers)
					updateDefaultSevaId(); // Update default ID for adding next seva
				} else {
					showAlert(Alert.AlertType.ERROR,"Delete Failed", "Could not delete Seva '" + seva.getName() + "'.");
				}
			});

			sevaGridPane.add(slNoLabel, 0, displayIndex); // Row index is now displayIndex
			sevaGridPane.add(nameLabel, 1, displayIndex);
			sevaGridPane.add(amountLabel, 2, displayIndex);
			HBox actionBox = new HBox(deleteButton);
			actionBox.setPadding(new Insets(0, 5, 0, 5));
			sevaGridPane.add(actionBox, 3, displayIndex);

			displayIndex++; // Increment for the next row
		}
		// ... optional styling ...
	}


	// *** ADDED Helper method to clear input fields ***
	private void clearInputFields() {
		sevaNameField.clear();
		sevaAmountField.clear();
	}


	// *** ADDED Helper method for alerts ***
	private void showAlert(Alert.AlertType alertType, String title, String message) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}