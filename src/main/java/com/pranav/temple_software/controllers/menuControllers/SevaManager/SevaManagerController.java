// File: Temple_Software/src/main/java/com/pranav/temple_software/controllers/menuControllers/SevaManager/SevaManagerController.java
package com.pranav.temple_software.controllers.menuControllers.SevaManager;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.Seva;
import com.pranav.temple_software.repositories.SevaRepository; // Import SevaRepository
import javafx.event.ActionEvent;
import javafx.fxml.FXML; // Import FXML
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox; // For layout within grid cells

import java.util.Collection; // Import Collection
import java.util.List;


public class SevaManagerController {

	@FXML public GridPane sevaGridPane; //

	// *** ADDED FXML Fields for new Controls ***
	@FXML private TextField sevaIdField;
	@FXML private TextField sevaNameField;
	@FXML private TextField sevaAmountField;
	@FXML private Button addSevaButton; // Although action is handled by onAction, useful to have reference

	// *** ADDED SevaRepository Instance ***
	private final SevaRepository sevaRepository = SevaRepository.getInstance();
	private int nextSevaId = 0;


	private MainController mainControllerInstance;

	// *** ADD Setter method for MainController instance ***
	public void setMainController(MainController controller) {
		this.mainControllerInstance = controller;
	}
	// *** ADDED initialize method ***
	@FXML
	public void initialize() {
		updateDefaultSevaId(); // Sets the next *seva_id*
		refreshGridPane();
		sevaIdField.setEditable(false);
	}

	private void updateDefaultSevaId() { // This calculates the next *seva_id*
		if (this.sevaRepository == null) return;
		try {
			int maxId = sevaRepository.getMaxSevaId(); // Get max *ID*
			this.nextSevaId = maxId + 1;
			sevaIdField.setText(String.valueOf(this.nextSevaId));
		} catch (Exception e) { /* ... error handling ... */ }
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


		double amount = 0;
		try {
			amount = Double.parseDouble(amountStr);
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

		Seva newSeva = new Seva(id, name, amount); // Create Seva (display_order set by repo)
		boolean success = this.sevaRepository.addSevaToDB(newSeva);

		if (success) {
			// ... show success, clear fields ...
			refreshGridPane();
			updateDefaultSevaId(); // Update ID for the *next* add
		}
	}

	// *** NO CHANGE needed for handleDeleteSeva method signature ***
	// The actual deletion logic will be handled by the button created in refreshGridPane
	@FXML public void handleDeleteSeva(ActionEvent actionEvent) { /* Keep signature, logic moves */ }



	@FXML public void handleSave(ActionEvent actionEvent) { //
		System.out.println("Refresh button clicked.");
		updateDefaultSevaId(); // Ensure default ID is up-to-date on refresh
		refreshGridPane();
		mainControllerInstance.refreshSevaCheckboxes();
	}

	private void triggerMainViewRefresh() {
		if (mainControllerInstance != null) {
			System.out.println("DEBUG: Triggering MainView refresh from SevaManager...");
			// Use Platform.runLater if repository operations were complex/off-thread,
			// but likely okay directly if called from button handlers.
			// Platform.runLater(() -> mainControllerInstance.refreshSevaCheckboxes());
			mainControllerInstance.refreshSevaCheckboxes(); // Call the new public method
		} else {
			System.err.println("Error: mainControllerInstance is null in SevaManagerController. Cannot refresh MainView.");
		}
	}
	private void refreshGridPane() {
		if (this.sevaRepository == null) {
			System.err.println("refreshGridPane called but sevaRepository is null.");
			return;
		}

		// --- Clearing Step ---
		// This should be sufficient, but we ensure it happens first.
		sevaGridPane.getChildren().clear();

		// Optional: You could try clearing constraints too if you suspect they interfere,
		// but it adds complexity as you'd need to re-add them. Usually not needed.
		// sevaGridPane.getRowConstraints().clear();
		// sevaGridPane.getColumnConstraints().clear();
		// --- End Clearing ---


		// Re-add headers (make sure these are fresh Labels too)
		// Headers are in Row 0
		sevaGridPane.add(new Label("Order"), 0, 0);
		sevaGridPane.add(new Label("Name"), 1, 0);
		sevaGridPane.add(new Label("Amount (₹)"), 2, 0);
		sevaGridPane.add(new Label("Actions"), 3, 0);

		// Define Column Constraints here if you cleared them, or preferably define in FXML
		// Ensure constraints allow columns to size correctly.

		// Get the correctly sorted list from the repository
		List<Seva> sevas = this.sevaRepository.getAllSevas();

		// Populate rows starting from index 1
		for (int i = 0; i < sevas.size(); i++) {
			Seva seva = sevas.get(i);
			int rowIndex = i + 1; // Data rows start from 1

			// *** IMPORTANT: Create NEW Nodes every time ***
			// This ensures you aren't accidentally reusing old node references.
			Label orderLabel = new Label(String.valueOf(seva.getDisplayOrder()));
			Label nameLabel = new Label(seva.getName());
			// Limit name label width if necessary to prevent overlap
			// nameLabel.setMaxWidth(200); // Example width limit
			// nameLabel.setWrapText(true);
			Label amountLabel = new Label(String.format("%.2f", seva.getAmount()));
			Button deleteButton = new Button("Del");
			Button upButton = new Button("▲");
			Button downButton = new Button("▼");
			// Create a new HBox for buttons in each row
			HBox actionBox = new HBox(5, upButton, downButton, deleteButton);
			actionBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

			// --- Assign actions ---
			final String currentSevaId = seva.getId(); // Final variable for lambdas
			// Ensure action handlers are set on the NEW buttons
			deleteButton.setOnAction(event -> handleDeleteAction(currentSevaId, seva.getName()));
			upButton.setOnAction(event -> handleMoveUp(currentSevaId));
			downButton.setOnAction(event -> handleMoveDown(currentSevaId));

			// --- Disable buttons at edges ---
			upButton.setDisable(i == 0);
			downButton.setDisable(i == sevas.size() - 1);

			// --- Add the NEW nodes to the grid ---
			// Add nodes to the correct column/row index
			sevaGridPane.add(orderLabel, 0, rowIndex);
			sevaGridPane.add(nameLabel, 1, rowIndex);
			sevaGridPane.add(amountLabel, 2, rowIndex);
			sevaGridPane.add(actionBox, 3, rowIndex);

			// Optional: Define Row Constraints if needed (usually not necessary for simple lists)
			// RowConstraints rowConst = new RowConstraints(); ... sevaGridPane.getRowConstraints().add(rowConst);
		}

		// Optional: Force layout pass after updates - might help sometimes but shouldn't be required
		// Platform.runLater(() -> sevaGridPane.requestLayout());
	}


	// *** ADDED Helper method to clear input fields ***
	private void clearInputFields() {
		sevaNameField.clear();
		sevaAmountField.clear();
	}


	private void handleDeleteAction(String sevaId, String sevaName) {
		// Optional: Confirmation Dialog
		boolean deleted = this.sevaRepository.deleteSevaFromDB(sevaId);
		if (deleted) {
			refreshGridPane();
			updateDefaultSevaId(); // Update ID for next add
		} else {
			showAlert(Alert.AlertType.ERROR,"Delete Failed", "Could not delete Seva '" + sevaName + "'.");
		}
	}

	private void handleMoveUp(String sevaId) {
		boolean moved = sevaRepository.moveSevaUp(sevaId); // Call repo method
		if (moved) {
			refreshGridPane(); // Refresh UI if DB update was successful
		} else {
			// Optional: Show feedback if move failed (e.g., already at top, or DB error)
			System.err.println("Move up failed for Seva ID: " + sevaId);
			// showAlert(Alert.AlertType.WARNING, "Move Failed", "Could not move Seva up.");
		}
	}


	private void handleMoveDown(String sevaId) {
		boolean moved = sevaRepository.moveSevaDown(sevaId); // Call repo method
		if (moved) {
			refreshGridPane(); // Refresh UI if DB update was successful
		} else {
			System.err.println("Move down failed for Seva ID: " + sevaId);
			// showAlert(Alert.AlertType.WARNING, "Move Failed", "Could not move Seva down.");
		}
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