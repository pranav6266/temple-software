// File: Temple_Software/src/main/java/com/pranav/temple_software/controllers/menuControllers/SevaManager/SevaManagerController.java
package com.pranav.temple_software.controllers.menuControllers.SevaManager;

// ... other imports ...
import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.controllers.menuControllers.BaseManagerController;
import com.pranav.temple_software.models.Seva; // Use the specific Seva model
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.SevaRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent; // Import for close request


import java.util.*;

// Make SevaManagerController extend BaseManagerController<Seva>
public class SevaManagerController extends BaseManagerController<Seva> {
	public Button editButton;
	public Button openAddSevaButton;
	public Button deleteSeva;

	@Override
	protected String getItemId(SevaEntry item) {
		return "";
	}

	@Override
	protected String getItemName(SevaEntry item) {
		return "";
	}
// Remove fields already present in BaseManagerController (mainControllerInstance, tempItemList, etc.)
	// @FXML public GridPane sevaGridPane; // Inherited
	// public Button openAddSevaButton; // Inherited
	// public Button deleteSeva; // Inherited (rename FXML id to openDeleteButton)
	// public Button editButton; // Inherited (rename FXML id to openEditButton)
	// @FXML private Button saveButton; // Inherited
	// @FXML private Button cancelButton; // Inherited
	// @FXML private Button rearrangeButton; // This seems unused? Edit button handles rearrange

	@FXML private TextField sevaIdField; // Keep specific fields if needed

	private final SevaRepository sevaRepository = SevaRepository.getInstance();
	// Keep specific tracking lists if needed beyond the base class (though maybe integrate into originalState)
	// private List<Seva> addedSevas = new ArrayList<>(); // Can manage this via comparison with originalState
	// private List<Seva> deletedSevas = new ArrayList<>(); // Use itemsMarkedForDeletion from base
	private Map<String, Double> originalAmounts = new HashMap<>(); // Can be part of originalState
	private Map<String, Integer> originalOrder = new HashMap<>(); // Can be part of originalState


	// Override abstract methods

	@Override
	protected void loadData() {
		sevaRepository.loadSevasFromDB(); // Ensure repository is loaded
		tempItemList.setAll(sevaRepository.getAllSevas()); // Populate the temp list
	}

	@Override
	protected void storeOriginalState() {
		originalState.clear();
		originalAmounts.clear(); // Keep these for now, or refactor into originalState map
		originalOrder.clear();  // Keep these for now, or refactor into originalState map
		for (int i = 0; i < tempItemList.size(); i++) {
			Seva seva = tempItemList.get(i);
			originalState.put(seva.getId(), new Seva(seva.getId(), seva.getName(), seva.getAmount(), seva.getDisplayOrder())); // Store a copy
			originalAmounts.put(seva.getId(), seva.getAmount());
			originalOrder.put(seva.getId(), i + 1);
		}
	}

	@Override
	protected String getItemId(Seva item) {
		return item.getId();
	}
	@Override
	protected String getItemName(Seva item) {
		return item.getName();
	}


	@Override
	protected void refreshGridPane() {
		// Your existing refreshGridPane logic using 'itemGridPane' and 'tempItemList'
		itemGridPane.getChildren().clear(); //[cite: 227]
		Label indexHeader = new Label("No."); //[cite: 228]
		Label nameHeader = new Label("Seva Name"); //[cite: 228]
		Label amountHeader = new Label("Amount"); //[cite: 229]

		indexHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;"); //[cite: 229]
		nameHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;"); //[cite: 230]
		amountHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;"); //[cite: 230]

		itemGridPane.add(indexHeader, 0, 0); //[cite: 230]
		itemGridPane.add(nameHeader, 1, 0); //[cite: 231]
		itemGridPane.add(amountHeader, 2, 0); //[cite: 231]

		for (int i = 0; i < tempItemList.size(); i++) {
			Seva seva = tempItemList.get(i); //[cite: 231]
			int rowIndex = i + 1; //[cite: 232]

			Label orderLabel = new Label(String.valueOf(i + 1)); //[cite: 232]
			Label nameLabel = new Label(seva.getName()); //[cite: 233]
			Label amountLabel = new Label(String.format("₹%.2f", seva.getAmount())); // Format amount //[cite: 233]

			orderLabel.setAlignment(Pos.CENTER); //[cite: 233]
			nameLabel.setAlignment(Pos.CENTER_LEFT); //[cite: 233]
			amountLabel.setAlignment(Pos.CENTER_RIGHT); //[cite: 233]

			itemGridPane.add(orderLabel, 0, rowIndex); //[cite: 234]
			itemGridPane.add(nameLabel, 1, rowIndex); //[cite: 234]
			itemGridPane.add(amountLabel, 2, rowIndex); //[cite: 234]
		}
	}


	@Override
	@FXML
	protected void openAddPopup(ActionEvent event) {
		// Your existing openAddSevaPopup logic //[cite: 193]
		// Ensure it adds the new Seva to tempItemList and refreshes the grid.
		// The actual DB save happens in handleSave.
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Add New Seva"); //[cite: 193]

		Label idLabel = new Label("Seva ID:"); //[cite: 194]
		TextField idField = new TextField(); //[cite: 194]
		idField.setEditable(false); //[cite: 194]
		int maxId = sevaRepository.getMaxSevaId(); //[cite: 195]
		int defaultId = maxId + 1; //[cite: 196]
		idField.setText(String.valueOf(defaultId)); //[cite: 196]

		Label nameLabel = new Label("Seva Name:"); //[cite: 196]
		TextField nameField = new TextField(); //[cite: 197]
		nameField.setPromptText("Enter Seva Name"); //[cite: 197]

		Label amountLabel = new Label("Amount (₹):"); //[cite: 197]
		TextField amountField = new TextField(); //[cite: 197]
		amountField.setPromptText("Enter Amount"); //[cite: 198]

		Button submitButton = new Button("Submit"); //[cite: 198]
		Button cancelPopupBtn = new Button("Cancel"); //[cite: 198]

		GridPane grid = new GridPane(); //[cite: 199]
		grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20)); //[cite: 199]
		grid.add(idLabel, 0, 0); grid.add(idField, 1, 0); //[cite: 199, 200]
		grid.add(nameLabel, 0, 1); grid.add(nameField, 1, 1); //[cite: 200]
		grid.add(amountLabel, 0, 2); grid.add(amountField, 1, 2); //[cite: 200]

		HBox buttonBox = new HBox(10, submitButton, cancelPopupBtn); //[cite: 201]
		buttonBox.setAlignment(Pos.CENTER); //[cite: 201]
		VBox layout = new VBox(15, grid, buttonBox); //[cite: 201]
		layout.setPadding(new Insets(20)); //[cite: 201]
		Scene scene = new Scene(layout); //[cite: 202]
		popupStage.setScene(scene); //[cite: 202]

		submitButton.setOnAction(e -> {
			String name = nameField.getText(); //[cite: 202]
			String amountStr = amountField.getText(); //[cite: 202]
			if (name == null || name.trim().isEmpty() || amountStr == null || amountStr.trim().isEmpty()) {
				showAlert(Alert.AlertType.ERROR, "Input Error", "Please fill in Seva Name and Amount."); //[cite: 202]
				return;
			}
			double amount;
			try {
				amount = Double.parseDouble(amountStr); //[cite: 202]
				if (amount < 0) {
					showAlert(Alert.AlertType.ERROR,"Input Error", "Amount cannot be negative."); //[cite: 202]
					return;
				}
			} catch (NumberFormatException ex) {
				showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid number for the Amount."); //[cite: 202]
				return;
			}
			String id = idField.getText(); //[cite: 202]
			Seva newSeva = new Seva(id, name, amount); //[cite: 202]
			// *** Add to temporary list ONLY ***
			tempItemList.add(newSeva); //[cite: 202]
			// No need to add to addedSevas explicitly if comparing tempItemList with originalState later
			refreshGridPane(); // Update the main grid view
			showAlert(Alert.AlertType.INFORMATION, "Success", "Seva '" + name + "' staged for addition. Press 'Save' to commit."); // Modified message
			popupStage.close(); //[cite: 204]
		});

		cancelPopupBtn.setOnAction(e -> popupStage.close()); //[cite: 204]
		popupStage.showAndWait(); //[cite: 204]
	}


	@Override
	@FXML
	protected void openEditPopup(ActionEvent event) {
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Edit/Rearrange Sevas"); //[cite: 204]

		// *** Crucial: Work on a DEEP COPY of the tempItemList for the popup ***
		// This prevents modifying the main tempItemList directly until "Save" in popup
		ObservableList<Seva> popupTempList = FXCollections.observableArrayList();
		for (Seva seva : tempItemList) {
			// Assuming Seva has a copy constructor or create new ones
			popupTempList.add(new Seva(seva.getId(), seva.getName(), seva.getAmount(), seva.getDisplayOrder()));
		}


		ListView<Seva> listView = new ListView<>(popupTempList); //[cite: 205]
		listView.setPrefSize(400, 300); //[cite: 205]

		// Use your existing cell factory logic
		// BUT make sure it updates the popupTempList, NOT the main tempItemList or DB
		listView.setCellFactory(lv -> {
			ListCell<Seva> cell = new ListCell<>() {
				// Use local variables inside updateItem if they don't need to be class members
				@Override
				protected void updateItem(Seva seva, boolean empty) {
					super.updateItem(seva, empty);

					if (empty || seva == null) {
						setGraphic(null);
					} else {
						int index = getIndex() + 1;

						Label slLabel = new Label(index + ". "); //[cite: 206]
						Label nameLabel = new Label(seva.getName()); //[cite: 206]
						TextField amountField = new TextField(String.format("%.2f", seva.getAmount())); //[cite: 206]
						amountField.setPrefWidth(100); //[cite: 206]
						amountField.setAlignment(Pos.CENTER_RIGHT); //[cite: 206]
						Label rupeeLabel = new Label("₹"); //[cite: 206]

						// *** Listener updates the Seva object IN THE POPUP LIST ***
						amountField.textProperty().addListener((obs, oldVal, newVal) -> {
							String clean = newVal.replace("₹", "").trim(); //[cite: 206]
							try {
								// Get the correct item from popupTempList to update
								Seva itemToUpdate = popupTempList.get(getIndex());
								if(itemToUpdate != null) {
									itemToUpdate.setAmount(Double.parseDouble(clean)); //[cite: 206]
									// Optionally refresh just this cell if needed, but ListView usually handles it
								}
							} catch (NumberFormatException | IndexOutOfBoundsException ignored) {} //[cite: 206]
						});


						HBox spacer = new HBox(); //[cite: 206]
						HBox.setHgrow(spacer, Priority.ALWAYS); //[cite: 206]
						HBox hbox = new HBox(10, slLabel, nameLabel, spacer,rupeeLabel, amountField); //[cite: 207]
						hbox.setAlignment(Pos.CENTER_LEFT); //[cite: 207]
						setGraphic(hbox); //[cite: 207]
					}
				}
			};

			// --- Drag and Drop Logic ---
			// *** Ensure drag/drop modifies popupTempList ONLY ***
			cell.setOnDragDetected(ev -> { // Renamed event variable
				if (cell.getItem() == null) return; //[cite: 208]
				Dragboard db = cell.startDragAndDrop(TransferMode.MOVE); //[cite: 208]
				ClipboardContent content = new ClipboardContent(); //[cite: 208]
				content.putString(cell.getItem().getId()); // Use ID //[cite: 208]
				db.setContent(content); //[cite: 208]
				ev.consume(); //[cite: 208]
			});

			cell.setOnDragOver(ev -> { // Renamed event variable
				if (ev.getGestureSource() != cell && ev.getDragboard().hasString()) {
					ev.acceptTransferModes(TransferMode.MOVE); //[cite: 209]
				}
				ev.consume(); //[cite: 209]
			});
			// Visual feedback... //[cite: 210]


			cell.setOnDragDropped(ev -> { // Renamed event variable
				if (cell.getItem() == null) return; //[cite: 211]
				Dragboard db = ev.getDragboard(); //[cite: 211]
				boolean success = false;
				if (db.hasString()) {
					String draggedId = db.getString(); //[cite: 211]
					Seva draggedSeva = null;
					int fromIndex = -1;

					// Find in popupTempList
					for (int i = 0; i < popupTempList.size(); i++) {
						if (popupTempList.get(i).getId().equals(draggedId)) { //[cite: 211]
							draggedSeva = popupTempList.get(i);
							fromIndex = i;
							break;
						}
					}

					int toIndex = cell.getIndex(); //[cite: 211]
					if (draggedSeva != null && fromIndex != toIndex) {
						popupTempList.remove(draggedSeva); // Modify popupTempList //[cite: 211]
						if (toIndex > popupTempList.size()) { // Handle drop at the end
							toIndex = popupTempList.size();
						}
						popupTempList.add(toIndex, draggedSeva); // Modify popupTempList //[cite: 211]
						listView.setItems(null); // Refresh list view //[cite: 211]
						listView.setItems(popupTempList); // Use popupTempList //[cite: 211]
						success = true; //[cite: 211]
					}
				}
				ev.setDropCompleted(success); //[cite: 211]
				ev.consume(); //[cite: 211]
			});
			cell.setOnDragDone(DragEvent::consume); //[cite: 212]

			return cell;
		});

		// Save and Cancel buttons for the POPUP
		Button savePopupBtn = new Button("Save Changes to Main View"); // Clarify button purpose
		Button cancelPopupBtn = new Button("Cancel Edit");

		// *** POPUP Save Button Logic ***
		savePopupBtn.setOnAction(ev -> {
			// *** Replace main tempItemList with the edited popup list ***
			tempItemList.setAll(popupTempList);
			refreshGridPane(); // Update the main manager grid
			popupStage.close();
			// The actual DB save will happen when the main "Save" is clicked
		});

		// *** POPUP Cancel Button Logic ***
		cancelPopupBtn.setOnAction(ev -> {
			// No confirmation needed here as per requirement, just close
			popupStage.close();
		});

		// *** Handle Popup Window Close Request (X button) ***
		popupStage.setOnCloseRequest((WindowEvent windowEvent) -> {
			// Prevent default close
			windowEvent.consume();
			// Show confirmation dialog
			Optional<ButtonType> result = showConfirmationDialog(
					"Exit Without Saving?",
					"You have unsaved changes in the editor. Are you sure you want to exit without applying them to the main view?"
			);
			if (result.isPresent() && result.get() == ButtonType.OK) {
				// User confirmed Yes - close without applying changes
				popupStage.close();
			}
			// If user clicks No or closes the dialog, do nothing, stay in the popup
		});


		HBox buttonBox = new HBox(10, savePopupBtn, cancelPopupBtn); //[cite: 213]
		buttonBox.setAlignment(Pos.CENTER); //[cite: 213]
		VBox layout = new VBox(10, listView, buttonBox); //[cite: 213]
		layout.setPadding(new Insets(15)); //[cite: 213]
		Scene scene = new Scene(layout); //[cite: 214]
		popupStage.setScene(scene); //[cite: 214]
		popupStage.showAndWait(); //[cite: 215] // Wait for the popup
	}

	@Override
	@FXML
	protected void openDeletePopup(ActionEvent event) {
		// Your existing openDeleteSevaPopup logic //[cite: 235]
		// Ensure it marks sevas for deletion (using itemsMarkedForDeletion)
		// and removes them from tempItemList, then refreshes the grid.
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Delete Sevas"); //[cite: 235]

		VBox checkboxContainer = new VBox(10); //[cite: 236]
		checkboxContainer.setPadding(new Insets(10)); //[cite: 236]
		List<CheckBox> sevaCheckBoxes = new ArrayList<>(); //[cite: 236]

		for (Seva seva : tempItemList) { // Use the current tempItemList
			CheckBox cb = new CheckBox(seva.getName() + " - ₹" + String.format("%.2f", seva.getAmount())); //[cite: 237]
			cb.setUserData(seva); // Store the Seva object itself
			sevaCheckBoxes.add(cb); //[cite: 237]
			checkboxContainer.getChildren().add(cb); //[cite: 237]
		}

		ScrollPane scrollPane = new ScrollPane(checkboxContainer); //[cite: 238]
		scrollPane.setFitToWidth(true); //[cite: 238]
		scrollPane.setPrefHeight(Math.min(tempItemList.size() * 30 + 20, 400)); // Adjust height

		Button deleteSelectedButton = new Button("Mark Selected for Deletion"); // Better label
		Button cancelPopupBtn = new Button("Cancel"); //[cite: 239]

		deleteSelectedButton.setOnAction(e -> {
			List<Seva> toRemoveFromTemp = new ArrayList<>();
			for (CheckBox cb : sevaCheckBoxes) {
				if (cb.isSelected()) {
					Seva selectedSeva = (Seva) cb.getUserData();
					if (selectedSeva != null && !itemsMarkedForDeletion.contains(selectedSeva)) {
						itemsMarkedForDeletion.add(selectedSeva); // Add to deletion list //[cite: 242]
						toRemoveFromTemp.add(selectedSeva); // Mark for removal from current view
					}
				}
			}
			if (!toRemoveFromTemp.isEmpty()) {
				tempItemList.removeAll(toRemoveFromTemp); // Remove from temp list //[cite: 242]
				refreshGridPane(); // Update main grid //[cite: 242]
				showAlert(Alert.AlertType.INFORMATION,"Marked for Deletion", toRemoveFromTemp.size() + " seva(s) marked. Press 'Save' to commit deletion.");
			}
			popupStage.close(); //[cite: 242]
		});

		cancelPopupBtn.setOnAction(e -> popupStage.close()); //[cite: 243]

		HBox buttonBox = new HBox(10, deleteSelectedButton, cancelPopupBtn); //[cite: 240]
		buttonBox.setAlignment(Pos.CENTER); //[cite: 240]
		VBox popupLayout = new VBox(15, scrollPane, buttonBox); //[cite: 240]
		popupLayout.setPadding(new Insets(15)); //[cite: 240]
		Scene scene = new Scene(popupLayout); //[cite: 241]
		popupStage.setScene(scene); //[cite: 241]
		popupStage.showAndWait(); //[cite: 243]
	}

	@Override
	@FXML
	protected void handleSave(ActionEvent actionEvent) {
		StringBuilder summary = new StringBuilder();
		boolean changesMade = false;

		// 1. Process Deletions
		if (!itemsMarkedForDeletion.isEmpty()) {
			for (Seva sevaToDelete : itemsMarkedForDeletion) {
				boolean deleted = sevaRepository.deleteSevaFromDB(sevaToDelete.getId()); //[cite: 219]
				if (deleted) {
					summary.append("🗑️ Deleted: ").append(sevaToDelete.getName()).append("\n"); //[cite: 220]
					changesMade = true;
				} else {
					summary.append("❌ Delete Failed: ").append(sevaToDelete.getName()).append("\n");
					// Consider adding back to tempItemList if deletion failed? Or just report error.
				}
			}
			// Clear the list after processing
		}


		// 2. Process Additions and Modifications (Compare tempItemList with originalState)
		Map<String, Seva> currentItemsMap = new HashMap<>();
		for(Seva currentSeva : tempItemList) {
			currentItemsMap.put(currentSeva.getId(), currentSeva);
		}

		for (int i = 0; i < tempItemList.size(); i++) {
			Seva currentSeva = tempItemList.get(i);
			String currentId = currentSeva.getId();
			int desiredOrder = i + 1; // Order based on final tempItemList position

			if (!originalState.containsKey(currentId)) {
				// It's a new Seva (added via Add popup)
				// Set the final display order before adding
				currentSeva.setDisplayOrder(desiredOrder);
				boolean added = sevaRepository.addSevaToDB(currentSeva); //[cite: 218]
				if (added) {
					summary.append("✅ Added: ").append(currentSeva.getName())
							.append(" (₹").append(currentSeva.getAmount()).append(") at #").append(desiredOrder).append("\n"); //[cite: 219]
					changesMade = true;
				} else {
					summary.append("❌ Add Failed: ").append(currentSeva.getName()).append("\n");
				}
			} else {
				// It's an existing Seva, check for modifications
				Seva originalSeva = originalState.get(currentId);
				boolean amountChanged = currentSeva.getAmount() != originalSeva.getAmount();
				// Check original position using originalOrder map
				boolean orderChanged = !originalOrder.containsKey(currentId) || desiredOrder != originalOrder.get(currentId);


				if (amountChanged) {
					boolean updated = sevaRepository.updateAmount(currentId, currentSeva.getAmount()); //[cite: 222]
					if(updated) {
						summary.append("✏️ Amount changed: ").append(currentSeva.getName())
								.append(" ₹").append(originalSeva.getAmount())
								.append(" → ₹").append(currentSeva.getAmount()).append("\n"); //[cite: 222]
						changesMade = true;
					} else {
						summary.append("❌ Amount Update Failed: ").append(currentSeva.getName()).append("\n");
					}
				}
				if (orderChanged) {
					// Update display order in DB
					boolean updated = sevaRepository.updateDisplayOrder(currentId, desiredOrder); //[cite: 223]
					if(updated) {
						summary.append("🔀 Order changed: ").append(currentSeva.getName())
								.append(" #").append(originalOrder.getOrDefault(currentId, -1)) // Show old order
								.append(" → #").append(desiredOrder).append("\n"); //[cite: 223]
						changesMade = true;
					} else {
						summary.append("❌ Order Update Failed: ").append(currentSeva.getName()).append("\n");
					}
				}
			}
		}

		// 3. Reload data from DB to ensure consistency and refresh UI components
		loadData(); // Reloads tempItemList from DB
		storeOriginalState(); // Update original state to the newly saved state
		itemsMarkedForDeletion.clear(); // MUST clear this after successful deletion processing
		refreshGridPane(); // Refresh the grid in the manager

		if (mainControllerInstance != null) {
			mainControllerInstance.refreshSevaCheckboxes(); // Refresh checkboxes in main view //[cite: 224]
		}

		// 4. Show Summary
		if (changesMade) {
			showAlert(Alert.AlertType.INFORMATION,"Changes Saved", summary.toString()); //[cite: 225]
		} else {
			showAlert(Alert.AlertType.INFORMATION,"No Changes", "Nothing was modified."); //[cite: 225]
		}
		// Optional: Close window after save? Only if no errors occurred.
		// closeWindow();
	}

	// --- Helper Methods Specific to SevaManager ---
	private void updateDefaultSevaId() {
		// Your existing logic
		if (sevaRepository != null && sevaIdField != null) {
			try {
				int maxId = sevaRepository.getMaxSevaId(); //[cite: 215]
				int nextSevaId = maxId + 1; //[cite: 216]
				sevaIdField.setText(String.valueOf(nextSevaId)); //[cite: 216]
			} catch (Exception e) {
				System.err.println("Error calculating default Seva ID: " + e.getMessage()); //[cite: 217]
				showAlert(Alert.AlertType.ERROR,"Error", "Could not determine next Seva ID.");
				sevaIdField.setText("ERR");
			}
		}
	}


	// Override initialize to call super and then specific setups
	@Override
	public void initialize() {
		super.initialize(); // Calls loadData, storeOriginalState, refreshGridPane etc.
		updateDefaultSevaId(); // Call specific setup after base initialization
		// Any other SevaManager specific initializations
	}

//	@Override
//	public void setMainController(MainController controller) {
//		super.setMainController(controller);
//	}
}