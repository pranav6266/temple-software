// File: Temple_Software/src/main/java/com/pranav/temple_software/controllers/menuControllers/SevaManager/SevaManagerController.java
package com.pranav.temple_software.controllers.menuControllers.SevaManager;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.Seva;
import com.pranav.temple_software.repositories.SevaRepository; // Import SevaRepository
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML; // Import FXML
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox; // For layout within grid cells
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;


public class SevaManagerController {

	@FXML public GridPane sevaGridPane; //
	public Button openAddSevaButton;
	public Button deleteSeva;
//	public ListView<Seva> sevaListView;

	// *** ADDED FXML Fields for new Controls ***
	@FXML private TextField sevaIdField;
	@FXML private TextField sevaNameField;
	@FXML private TextField sevaAmountField;
	@FXML private Button addSevaButton; // Although action is handled by onAction, useful to have reference
	@FXML
	private Button saveButton;     // Added fx:id="saveButton"
	@FXML
	private Button cancelButton;
	@FXML Button refreshButton;
	@FXML
	private Button rearrangeButton;
	// *** ADDED SevaRepository Instance ***
	private final SevaRepository sevaRepository = SevaRepository.getInstance();
	private int nextSevaId = 0;
	private ObservableList<Seva> tempSevaList;


	private MainController mainControllerInstance;

	// *** ADD Setter method for MainController instance ***
	public void setMainController(MainController controller) {
		this.mainControllerInstance = controller;
	}
	// *** ADDED initialize method ***
	@FXML
	public void initialize() {
		tempSevaList = FXCollections.observableArrayList(sevaRepository.getAllSevas());
		updateDefaultSevaId();
		// Now refresh the grid pane to display the entries read-only.
		refreshGridPane();

		// Set up the rearrange button to open a popup for reordering.
		rearrangeButton.setOnAction(e -> openRearrangePopup());


	}


	@FXML
	private void openAddSevaPopup() {
		// Create a new stage for the popup
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Add New Seva");

		// Create UI controls for the popup:
		Label idLabel = new Label("Seva ID:");
		TextField idField = new TextField();
		idField.setEditable(false);  // non-editable
		// Populate this field using your updateDefaultSevaId() method logic.
		int maxId = sevaRepository.getMaxSevaId(); // Compute maximum from DB
		int defaultId = maxId + 1;
		idField.setText(String.valueOf(defaultId));

		Label nameLabel = new Label("Seva Name:");
		TextField nameField = new TextField();
		nameField.setPromptText("Enter Seva Name");

		Label amountLabel = new Label("Amount (₹):");
		TextField amountField = new TextField();
		amountField.setPromptText("Enter Amount");

		// Create buttons for submit and cancel
		Button submitButton = new Button("Submit");
		Button cancelButton = new Button("Cancel");

		// Arrange them in a grid (or VBox):
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));
		grid.add(idLabel, 0, 0);
		grid.add(idField, 1, 0);
		grid.add(nameLabel, 0, 1);
		grid.add(nameField, 1, 1);
		grid.add(amountLabel, 0, 2);
		grid.add(amountField, 1, 2);

		HBox buttonBox = new HBox(10, submitButton, cancelButton);
		buttonBox.setAlignment(Pos.CENTER);

		VBox layout = new VBox(15, grid, buttonBox);
		layout.setPadding(new Insets(20));

		Scene scene = new Scene(layout);
		popupStage.setScene(scene);

		// Add event handler for the Submit button:
		submitButton.setOnAction(e -> {
			String name = nameField.getText();
			String amountStr = amountField.getText();

			// Basic validation:
			if (name == null || name.trim().isEmpty() || amountStr == null || amountStr.trim().isEmpty()) {
				showAlert("Input Error", "Please fill in Seva Name and Amount.");
				return;
			}

			double amount;
			try {
				amount = Double.parseDouble(amountStr);
				if (amount < 0) {
					showAlert("Input Error", "Amount cannot be negative.");
					return;
				}
			} catch (NumberFormatException ex) {
				showAlert("Input Error", "Please enter a valid number for the Amount.");
				return;
			}

			// Use the default ID from idField (which is non-editable).
			String id = idField.getText();

			// Create a new Seva object
			Seva newSeva = new Seva(id, name, amount);

			boolean success = sevaRepository.addSevaToDB(newSeva);
			if (success) {
				showAlert("Success", "Seva added successfully!");
				// Refresh the grid in the main view – using your existing refreshGridPane() method.
				tempSevaList = FXCollections.observableArrayList(sevaRepository.getAllSevas());
				refreshGridPane();
				// Update default id for future additions:
				updateDefaultSevaId();
				popupStage.close();
			} else {
				showAlert("Database Error", "Failed to add Seva to the database. Check logs.");
			}
		});

		// Handle cancel button:
		cancelButton.setOnAction(e -> popupStage.close());

		popupStage.showAndWait();
	}

	private void openRearrangePopup() {
		// Create a new stage configured as modal
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Rearrange Sevas");

		// Create a temporary list from the repository data
		ObservableList<Seva> tempSevaList = FXCollections.observableArrayList(sevaRepository.getAllSevas());

		// Create a ListView with the temporary list
		ListView<Seva> listView = new ListView<>(tempSevaList);
		listView.setPrefSize(400, 300);

		// Set up the cell factory with drag and drop support
		listView.setCellFactory(new Callback<ListView<Seva>, ListCell<Seva>>() {
			@Override
			public ListCell<Seva> call(ListView<Seva> lv) {
				ListCell<Seva> cell = new ListCell<Seva>() {
					@Override
					protected void updateItem(Seva seva, boolean empty) {
						super.updateItem(seva, empty);
						if (empty || seva == null) {
							setText(null);
						} else {
							setText(seva.getName() + " - ₹" + String.format("%.2f", seva.getAmount()));
						}
					}
				};

				// Start the drag-and-drop gesture when a drag is detected.
				cell.setOnDragDetected((event) -> {
					if (cell.getItem() == null)
						return;
					Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
					ClipboardContent content = new ClipboardContent();
					// You can use the Seva's ID to identify it
					content.putString(cell.getItem().getId());
					db.setContent(content);
					event.consume();
				});

				// When dragged over another cell, accept the move.
				cell.setOnDragOver(event -> {
					if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
						event.acceptTransferModes(TransferMode.MOVE);
					}
					event.consume();
				});

				// Optional visual feedback
				cell.setOnDragEntered(event -> {
					if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
						cell.setOpacity(0.3);
					}
				});
				cell.setOnDragExited(event -> {
					if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
						cell.setOpacity(1);
					}
				});

				// Handle drop: update the temporary list order.
				cell.setOnDragDropped((DragEvent event) -> {
					if (cell.getItem() == null)
						return;
					Dragboard db = event.getDragboard();
					boolean success = false;
					if (db.hasString()) {
						String draggedSevaId = db.getString();
						Seva draggedSeva = null;
						int draggedIndex = -1;
						// Look for the dragged seva in the temporary list.
						for (int i = 0; i < tempSevaList.size(); i++) {
							if (tempSevaList.get(i).getId().equals(draggedSevaId)) {
								draggedSeva = tempSevaList.get(i);
								draggedIndex = i;
								break;
							}
						}
						int dropIndex = cell.getIndex();
						if (draggedSeva != null && draggedIndex != dropIndex) {
							tempSevaList.remove(draggedSeva);
							tempSevaList.add(dropIndex, draggedSeva);
							success = true;
						}
					}
					event.setDropCompleted(success);
					event.consume();
				});
				cell.setOnDragDone(DragEvent::consume);
				return cell;
			}
		});

		// Create Save and Cancel buttons for the popup
		Button saveBtn = new Button("Save");
		Button cancelBtn = new Button("Cancel");
		HBox buttonBox = new HBox(10, saveBtn, cancelBtn);
		buttonBox.setAlignment(Pos.CENTER);

		// Organize the popup layout in a VBox
		VBox popupLayout = new VBox(10, listView, buttonBox);
		popupLayout.setPadding(new Insets(10));
		popupLayout.setAlignment(Pos.CENTER);

		// Create the scene and set it in the popup stage
		Scene popupScene = new Scene(popupLayout);
		popupStage.setScene(popupScene);

		// Save: commit the new ordering (update display orders, update the main UI)
		saveBtn.setOnAction(ev -> {
			for (int i = 0; i < tempSevaList.size(); i++) {
				Seva seva = tempSevaList.get(i);
				int newDisplayOrder = i + 1;  // Ordering starts at 1
				seva.setDisplayOrder(newDisplayOrder);
				// Update the repository (assumes you have an updateDisplayOrder method)
				sevaRepository.updateDisplayOrder(seva.getId(), newDisplayOrder);
			}
			// Optionally reload the repository to keep in‑memory state updated.
			sevaRepository.loadSevasFromDB();
			// Update the main UI checkboxes (if your main UI uses the ordering).
			if (mainControllerInstance != null) {
				mainControllerInstance.refreshSevaCheckboxes();
			}
			popupStage.close();
		});

		// Cancel: discard changes and close the popup
		cancelBtn.setOnAction(ev -> popupStage.close());

		// Show the popup as a modal window
		popupStage.showAndWait();
	}

	private void updateDefaultSevaId() {
		if (sevaRepository != null) {
			try {
				int maxId = sevaRepository.getMaxSevaId(); // This already queries the DB
				nextSevaId = maxId + 1;
				sevaIdField.setText(String.valueOf(nextSevaId)); // Display non-editable default ID
			} catch (Exception e) {
				System.err.println("Error calculating default Seva ID: " + e.getMessage());
			}
		}
	}


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
		// Clear the grid pane.
		sevaGridPane.getChildren().clear();

		// Create header labels for row 0.
		Label indexHeader = new Label("No.");
		Label nameHeader = new Label("Seva Name");
		Label amountHeader = new Label("Amount");

		// Optional: Apply some styling to the headers.
		indexHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");
		nameHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");
		amountHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");

		// Add header labels to the grid pane at row 0.
		sevaGridPane.add(indexHeader, 0, 0);
		sevaGridPane.add(nameHeader, 1, 0);
		sevaGridPane.add(amountHeader, 2, 0);

		// Now, loop through the temporary list and add each Seva's data starting at row 1.
		for (int i = 0; i < tempSevaList.size(); i++) {
			Seva seva = tempSevaList.get(i);
			int rowIndex = i + 1;  // Data rows start at row index 1.

			Label orderLabel = new Label(String.valueOf(i + 1));
			Label nameLabel = new Label(seva.getName());
			Label amountLabel = new Label(String.format("%.2f", seva.getAmount()));

			orderLabel.setAlignment(Pos.CENTER);
			nameLabel.setAlignment(Pos.CENTER_LEFT);
			amountLabel.setAlignment(Pos.CENTER_RIGHT);

			sevaGridPane.add(orderLabel, 0, rowIndex);
			sevaGridPane.add(nameLabel, 1, rowIndex);
			sevaGridPane.add(amountLabel, 2, rowIndex);
		}
	}

	@FXML
	public void handleCancelButton(ActionEvent actionEvent) {
		((Stage) cancelButton.getScene().getWindow()).close(); // Close the current stage/window
	}

	private void handleSaveTempChanges() {
		// Loop through tempSevaList and update the displayOrder field
		for (int i = 0; i < tempSevaList.size(); i++) {
			Seva seva = tempSevaList.get(i);
			int newDisplayOrder = i + 1; // New order based on its position
			seva.setDisplayOrder(newDisplayOrder);
			// Update the DB for this seva or use a repository method to commit the order change
			sevaRepository.updateDisplayOrder(seva.getId(), newDisplayOrder);
		}

		// Optionally reload the in-memory repository list from the DB if needed
		sevaRepository.loadSevasFromDB();

		// Now update the main UI checkboxes – only when Save is clicked
		if (mainControllerInstance != null) {
			mainControllerInstance.refreshSevaCheckboxes();
		}

		// Optionally, show a confirmation message
		showAlert("Success", "Seva ordering has been updated successfully.");

		// Recreate the temporary list to reflect the committed state
		tempSevaList = FXCollections.observableArrayList(sevaRepository.getAllSevas());
		refreshGridPane();
	}

	private void handleCancelTempChanges() {
		tempSevaList = FXCollections.observableArrayList(sevaRepository.getAllSevas());
//		sevaListView.setItems(tempSevaList);
		showAlert("Cancelled", "Any changes have been discarded.");
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
			showAlert("Delete Failed", "Could not delete Seva '" + sevaName + "'.");
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
	private void showAlert(String title, String message) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}


	public void openDeleteSevaPopup(ActionEvent actionEvent) {
	}
}