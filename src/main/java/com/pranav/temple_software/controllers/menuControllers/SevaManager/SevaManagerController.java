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

import java.util.ArrayList;
import java.util.List;


public class SevaManagerController {

	@FXML public GridPane sevaGridPane; //
	public Button openAddSevaButton;
	public Button deleteSeva;
//	public ListView<Seva> sevaListView;

	// *** ADDED FXML Fields for new Controls ***
	@FXML private TextField sevaIdField;
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
	private List<Seva> sevasMarkedForDeletion = new ArrayList<>();

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
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Rearrange Sevas");

		// Local temporary list used for drag and display
		ObservableList<Seva> tempList = FXCollections.observableArrayList(sevaRepository.getAllSevas());

		ListView<Seva> listView = new ListView<>(tempList);
		listView.setPrefSize(400, 300);

		listView.setCellFactory(lv -> {
			ListCell<Seva> cell = new ListCell<>() {
				@Override
				protected void updateItem(Seva seva, boolean empty) {
					super.updateItem(seva, empty);
					if (empty || seva == null) {
						setText(null);
					} else {
						int index = getIndex() + 1; // Dynamic Sl. No.
						setText(index + ". " + seva.getName() + " - ₹" + String.format("%.2f", seva.getAmount()));
					}
				}
			};

			// Drag detected
			cell.setOnDragDetected(event -> {
				if (cell.getItem() == null) return;
				Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
				ClipboardContent content = new ClipboardContent();
				content.putString(cell.getItem().getId());
				db.setContent(content);
				event.consume();
			});

			// Drag over target cell
			cell.setOnDragOver(event -> {
				if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
					event.acceptTransferModes(TransferMode.MOVE);
				}
				event.consume();
			});

			// Visual feedback
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

			// Drop logic
			cell.setOnDragDropped(event -> {
				if (cell.getItem() == null) return;

				Dragboard db = event.getDragboard();
				boolean success = false;

				if (db.hasString()) {
					String draggedId = db.getString();
					Seva draggedSeva = null;
					int fromIndex = -1;

					for (int i = 0; i < tempList.size(); i++) {
						if (tempList.get(i).getId().equals(draggedId)) {
							draggedSeva = tempList.get(i);
							fromIndex = i;
							break;
						}
					}

					int toIndex = cell.getIndex();
					if (draggedSeva != null && fromIndex != toIndex) {
						tempList.remove(draggedSeva);
						tempList.add(toIndex, draggedSeva);
						listView.setItems(null); // refresh listView to update Sl. No.
						listView.setItems(tempList);
						success = true;
					}
				}

				event.setDropCompleted(success);
				event.consume();
			});

			cell.setOnDragDone(DragEvent::consume);
			return cell;
		});

		// Save and cancel
		Button saveBtn = new Button("Save");
		Button cancelBtn = new Button("Cancel");
		HBox buttonBox = new HBox(10, saveBtn, cancelBtn);
		buttonBox.setAlignment(Pos.CENTER);

		VBox layout = new VBox(10, listView, buttonBox);
		layout.setPadding(new Insets(15));

		Scene scene = new Scene(layout);
		popupStage.setScene(scene);

		saveBtn.setOnAction(ev -> {
			for (int i = 0; i < tempList.size(); i++) {
				Seva seva = tempList.get(i);
				seva.setDisplayOrder(i + 1);
				sevaRepository.updateDisplayOrder(seva.getId(), i + 1);
			}

			sevaRepository.loadSevasFromDB();
			tempSevaList = FXCollections.observableArrayList(sevaRepository.getAllSevas());
			refreshGridPane(); // refresh SevaManager grid only
			popupStage.close();
		});

		cancelBtn.setOnAction(ev -> popupStage.close());
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

	@FXML
	private void handleDeleteSevaMainSave(ActionEvent event) {
		// Loop over sevas marked for deletion and remove them permanently
		for (Seva seva : sevasMarkedForDeletion) {
			boolean deleted = sevaRepository.deleteSevaFromDB(seva.getId());
			if (!deleted) {
				showAlert("Delete Failed", "Could not delete Seva '" + seva.getName() + "'.");
			}
		}
		// Once deletion is done, clear the temporary deletion list
		sevasMarkedForDeletion.clear();
		// Refresh the gridpane (reflecting the repository state, if needed) and also the checkboxes in the main view
		refreshGridPane();
		mainControllerInstance.refreshSevaCheckboxes();
		showAlert("Success", "Selected Sevas have been permanently deleted.");
	}
	// *** ADDED Helper method for alerts ***
	private void showAlert(String title, String message) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}


	@FXML
	public void openDeleteSevaPopup(ActionEvent event) {
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Delete Sevas");

		// VBox that holds all checkboxes
		VBox checkboxContainer = new VBox(10);
		checkboxContainer.setPadding(new Insets(10));
		List<CheckBox> sevaCheckBoxes = new ArrayList<>();

		for (Seva seva : tempSevaList) {
			CheckBox cb = new CheckBox(seva.getName() + " - ₹" + String.format("%.2f", seva.getAmount()));
			sevaCheckBoxes.add(cb);
			checkboxContainer.getChildren().add(cb);
		}

		// Wrap VBox inside a ScrollPane
		ScrollPane scrollPane = new ScrollPane(checkboxContainer);
		scrollPane.setPrefWidth(200);
		scrollPane.setPrefHeight(700);
		scrollPane.setFitToWidth(true); // Ensures checkboxes expand to fit width

		// Save and Cancel buttons
		Button saveBtn = new Button("Save");
		Button cancelBtn = new Button("Cancel");

		HBox buttonBox = new HBox(10, saveBtn, cancelBtn);
		buttonBox.setAlignment(Pos.CENTER);

		VBox popupLayout = new VBox(15, scrollPane, buttonBox);
		popupLayout.setPadding(new Insets(15));

		// Popup scene and stage
		Scene scene = new Scene(popupLayout);
		popupStage.setScene(scene);

		// Save logic: remove selected sevas from tempSevaList only
		saveBtn.setOnAction(e -> {
			List<Seva> toRemove = new ArrayList<>();
			for (int i = 0; i < sevaCheckBoxes.size(); i++) {
				if (sevaCheckBoxes.get(i).isSelected()) {
					Seva selected = tempSevaList.get(i);
					toRemove.add(selected);
					sevasMarkedForDeletion.add(selected);
				}
			}
			tempSevaList.removeAll(toRemove);
			refreshGridPane(); // visually reflect deletion in manager only
			popupStage.close();
		});

		cancelBtn.setOnAction(e -> popupStage.close());

		popupStage.showAndWait();
	}
}