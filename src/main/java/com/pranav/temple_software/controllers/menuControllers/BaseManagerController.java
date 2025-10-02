// FILE: src/main/java/com/pranav/temple_software/controllers/menuControllers/BaseManagerController.java
package com.pranav.temple_software.controllers.menuControllers;

import com.pranav.temple_software.controllers.MainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.*;

public abstract class BaseManagerController<T> {

	@FXML protected Button saveButton;
	@FXML protected Button cancelButton;
	@FXML protected Button openAddButton;
	@FXML protected Button openDeleteButton;

	protected MainController mainControllerInstance;
	protected ObservableList<T> tempItemList;
	protected List<T> itemsMarkedForDeletion = new ArrayList<>();
	protected Map<String, T> originalState = new HashMap<>();

	public void setMainController(MainController controller) {
		this.mainControllerInstance = controller;
	}

	@FXML
	public void initialize() {
		tempItemList = FXCollections.observableArrayList();
		loadData();
		storeOriginalState();
		refreshGridPane();
	}

	// --- Abstract Methods ---
	protected abstract void loadData();
	protected abstract void refreshGridPane();
	@FXML public abstract void handleSave(ActionEvent event);
	protected abstract void storeOriginalState();
	protected abstract String getItemId(T item);

	@FXML protected abstract void openAddPopup(ActionEvent event);
	@FXML protected abstract void openEditPopup(ActionEvent event);
	@FXML protected abstract void openDeletePopup(ActionEvent event);

	// --- Common Helper Methods ---
	@FXML
	public void handleCancelButton() {
		if (checkForUnsavedChanges()) {
			Optional<ButtonType> result = showConfirmationDialog(
					"Confirm Cancel",
					"You have unsaved changes. Are you sure you want to cancel?"
			);
			if (result.isPresent() && result.get() == ButtonType.OK) {
				closeWindow();
			}
		} else {
			closeWindow();
		}
	}

	protected boolean checkForUnsavedChanges() {
		if (itemsMarkedForDeletion != null && !itemsMarkedForDeletion.isEmpty()) {
			return true;
		}
		if (tempItemList == null || originalState == null) {
			return false;
		}
		if (tempItemList.size() != originalState.size()) {
			return true;
		}

		for (T tempItem : tempItemList) {
			String itemId = getItemId(tempItem);
			// Check for new, unsaved items
			if (itemId == null || itemId.startsWith("NEW_") || itemId.equals("-1")) {
				return true;
			}
			if (!originalState.containsKey(itemId)) {
				return true;
			}
			T originalItem = originalState.get(itemId);
			if (!tempItem.equals(originalItem)) {
				return true; // Item was modified
			}
		}
		return false;
	}

	protected void closeWindow() {
		// Use the cancelButton as an anchor to find the window and close it
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		if (stage != null) {
			stage.close();
		}
	}

	protected void showAlert(Alert.AlertType alertType, String title, String message) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	protected Optional<ButtonType> showConfirmationDialog(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		return alert.showAndWait();
	}
}