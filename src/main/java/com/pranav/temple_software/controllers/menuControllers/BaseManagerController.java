// FILE: src/main/java/com/pranav/temple_software/controllers/menuControllers/BaseManagerController.java
package com.pranav.temple_software.controllers.menuControllers;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.Donations;
import com.pranav.temple_software.models.SevaEntry; // Or a more generic type if needed
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.*;

public abstract class BaseManagerController<T> { // Use a generic type T for the items (Seva, DonationEntry, etc.)

	@FXML protected GridPane itemGridPane; // Common grid pane ID
	@FXML protected Button saveButton;
	@FXML protected Button cancelButton;
	@FXML protected Button openAddButton;
	@FXML protected Button openEditButton;
	@FXML protected Button openDeleteButton;
	// Add other common FXML elements if applicable

	protected MainController mainControllerInstance;
	protected ObservableList<T> tempItemList; // Temporary list for edits
	protected List<T> itemsMarkedForDeletion = new ArrayList<>();
	protected Map<String, T> originalState = new HashMap<>(); // To track original values/order for comparison on save

	public void setMainController(MainController controller) {
		this.mainControllerInstance = controller;
	}

	@FXML
	public void initialize() {
		tempItemList = FXCollections.observableArrayList();
		loadData(); // Abstract method to load initial data
		storeOriginalState(); // Store initial state for comparison
		refreshGridPane();
		setupButtonActions(); // Setup common button actions if possible
	}

	// --- Abstract Methods to be Implemented by Subclasses ---
	protected abstract void loadData(); // Load data from the specific repository into tempItemList

	protected abstract String getItemId(SevaEntry item);

	protected abstract String getItemName(SevaEntry item);

	protected abstract void refreshGridPane(); // Update the GridPane UI based on tempItemList
	protected abstract void openAddPopup(ActionEvent event);
	protected abstract void openEditPopup(ActionEvent event);
	protected abstract void openDeletePopup(ActionEvent event);
	protected abstract void handleSave(ActionEvent event); // Implement the final save logic
	protected abstract void storeOriginalState(); // Store the initial state (order, amounts etc.)
	protected abstract String getItemId(T item); // Helper to get a unique ID for comparison
	protected abstract String getItemName(T item); // Helper to get the name for display/comparison

	// --- Common Helper Methods ---
	@FXML
	protected void handleCancelButton(ActionEvent actionEvent) {
		// Check if there are unsaved changes
		boolean hasUnsavedChanges = checkForUnsavedChanges();

		if (hasUnsavedChanges) {
			// Show confirmation dialog
			Optional<ButtonType> result = showConfirmationDialog(
					"Confirm Cancel",
					"You have unsaved changes. Are you sure you want to cancel?"
			);

			// Handle user's choice
			if (result.isPresent() && result.get() == ButtonType.OK) {
				closeWindow(); // Proceed with closing the window
			}
		} else {
			closeWindow(); // No unsaved changes, close directly
		}

	}

	protected boolean checkForUnsavedChanges() {
		if (tempItemList == null || originalState == null) {
			System.err.println("Error: TempItemList or OriginalState is null!");
			return false;
		}

		// Check if any item is added, removed, or modified
		for (T tempItem : tempItemList) {
			String itemId = getItemId(tempItem); // Use abstract method to get unique item ID
			if (!originalState.containsKey(itemId)) {
				// Item is newly added
				return true;
			}

			T originalItem = originalState.get(itemId);
			if (!tempItem.equals(originalItem)) {
				// Item has been modified
				return true;
			}
		}

		// Check for deletions
		for (String originalItemId : originalState.keySet()) {
			boolean itemExists = tempItemList.stream()
					.anyMatch(item -> getItemId(item).equals(originalItemId));
			if (!itemExists) {
				// Item has been removed
				return true;
			}
		}

		// No changes detected
		return false;
	}


	protected void closeWindow() {
		if (cancelButton != null && cancelButton.getScene() != null && cancelButton.getScene().getWindow() != null) {
			((Stage) cancelButton.getScene().getWindow()).close();
		} else {
			// Fallback or log error if button/scene/window is null
			System.err.println("Could not close window: Cancel button or its scene/window is null.");
		}
	}


	protected void showAlert(Alert.AlertType alertType, String title, String message) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		// Consider setting owner: if (mainControllerInstance != null) alert.initOwner(mainControllerInstance.mainStage);
		alert.showAndWait();
	}

	protected Optional<ButtonType> showConfirmationDialog(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(null); // Optional, can be added for more detail
		alert.setContentText(message);
		return alert.showAndWait();
	}


	// Optional: Common setup for button actions if handlers are simple delegates
	protected void setupButtonActions() {
		if (openAddButton != null) openAddButton.setOnAction(this::openAddPopup);
		if (openEditButton != null) openEditButton.setOnAction(this::openEditPopup);
		if (openDeleteButton != null) openDeleteButton.setOnAction(this::openDeletePopup);
		if (saveButton != null) saveButton.setOnAction(this::handleSave);
		if (cancelButton != null) cancelButton.setOnAction(this::handleCancelButton);
	}
}