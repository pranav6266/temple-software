// FILE: src/main/java/com/pranav/temple_software/controllers/menuControllers/OtherSevaManager/OtherSevaManagerController.java
package com.pranav.temple_software.controllers.menuControllers.OtherSevaManager;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.controllers.menuControllers.BaseManagerController;
// SevaEntry is suitable here as OtherSevas have name and amount [cite: 135, 318, 323]
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.OtherSevaRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair; // For the add dialog result

import java.util.*;

// Extend BaseManagerController using SevaEntry for Other Sevas
public class OtherSevaManagerController extends BaseManagerController<SevaEntry> {

	// Inherited FXML fields: itemGridPane, saveButton, cancelButton, openAddButton, openEditButton, openDeleteButton
	// Remove duplicate FXML declarations if they exist here

	private final OtherSevaRepository repository = OtherSevaRepository.getInstance();
	public Button editButton;
	public Button openAddOtherSevaButton;
	public Button deleteOtherSeva;
	// Inherited: mainControllerInstance, tempItemList, itemsMarkedForDeletion, originalState

	// Store original state including amount and order
	private Map<String, Pair<Double, Integer>> originalValuesMap = new HashMap<>();


	@Override
	protected void loadData() {
		repository.loadOtherSevasFromDB();
		tempItemList.setAll(OtherSevaRepository.getAllOtherSevas());
	}

	@Override
	protected void storeOriginalState() {
		originalState.clear();
		originalValuesMap.clear();
		for (int i = 0; i < tempItemList.size(); i++) {
			SevaEntry seva = tempItemList.get(i);
			String name = seva.getName();
			double amount = seva.getAmount();
			int order = i + 1;
			// Store a copy of the SevaEntry in originalState
			originalState.put(name, new SevaEntry(name, amount));
			// Store amount and order in the dedicated map for easier comparison
			originalValuesMap.put(name, new Pair<>(amount, order));
		}
	}

	@Override
	protected String getItemId(SevaEntry item) {
		// Using Name as the primary identifier for Other Sevas as ID isn't directly used in the logic often
		return item.getName();
	}
	@Override
	protected String getItemName(SevaEntry item) {
		return item.getName();
	}


	@Override
	protected void refreshGridPane() {
		// Your existing refreshGridPane logic, adapted for BaseManagerController fields
		itemGridPane.getChildren().clear(); // Use itemGridPane from base class

		// Header
		Label slNoHeader = new Label("Sl. No.");
		Label nameHeader = new Label("Other Seva Name");
		Label amountHeader = new Label("Amount");

		// Optional Styling
		slNoHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");
		nameHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");
		amountHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");
		amountHeader.setAlignment(Pos.CENTER_RIGHT); // Ensure header alignment matches data

		itemGridPane.add(slNoHeader, 0, 0);
		itemGridPane.add(nameHeader, 1, 0);
		itemGridPane.add(amountHeader, 2, 0);

		// Data Rows
		for (int i = 0; i < tempItemList.size(); i++) { // Use tempItemList from base class
			SevaEntry seva = tempItemList.get(i);
			Label slNo = new Label(String.valueOf(i + 1));
			Label name = new Label(seva.getName());
			Label amount = new Label(String.format("₹%.2f", seva.getAmount()));

			slNo.setAlignment(Pos.CENTER);
			name.setAlignment(Pos.CENTER_LEFT);
			amount.setAlignment(Pos.CENTER_RIGHT); // Align amount to the right

			itemGridPane.add(slNo, 0, i + 1);
			itemGridPane.add(name, 1, i + 1);
			itemGridPane.add(amount, 2, i + 1);
		}
	}

	@Override
	@FXML
	protected void openAddPopup(ActionEvent event) {
		// Your existing handleAdd logic, adapted
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("Add Other Seva");
		dialog.setHeaderText("Enter new seva name and amount:");

		ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));

		TextField nameField = new TextField(); nameField.setPromptText("Seva name");
		TextField amountField = new TextField(); amountField.setPromptText("Amount (₹)");
		// Add validation listener to amountField
		amountField.textProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null && !newVal.matches("\\d*(\\.\\d*)?")) {
				amountField.setText(oldVal); // Revert if invalid input
			}
		});


		grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
		grid.add(new Label("Amount:"), 0, 1); grid.add(amountField, 1, 1);

		dialog.getDialogPane().setContent(grid);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == addButtonType) {
				return new Pair<>(nameField.getText(), amountField.getText());
			}
			return null;
		});

		dialog.showAndWait().ifPresent(result -> {
			String name = result.getKey();
			String amountStr = result.getValue();

			if (name == null || name.trim().isEmpty()) {
				showAlert(Alert.AlertType.ERROR,"Input Error", "Seva Name cannot be empty.");
				return;
			}
			// Check if name already exists
			if (tempItemList.stream().anyMatch(s -> s.getName().equalsIgnoreCase(name.trim()))) {
				showAlert(Alert.AlertType.ERROR,"Input Error", "Other Seva '" + name + "' already exists.");
				return;
			}


			double amount;
			try {
				amount = Double.parseDouble(amountStr);
				if (amount < 0) {
					showAlert(Alert.AlertType.ERROR,"Input Error", "Amount cannot be negative.");
					return;
				}
			} catch (NumberFormatException | NullPointerException e) {
				showAlert(Alert.AlertType.ERROR,"Invalid Input", "Invalid amount. Please enter a number.");
				return;
			}

			// Add to temporary list ONLY
			tempItemList.add(new SevaEntry(name.trim(), amount));
			refreshGridPane(); // Update the main grid view
			showAlert(Alert.AlertType.INFORMATION,"Success", "Other Seva '" + name + "' staged for addition. Press 'Save' to commit.");
		});
	}


	@Override
	@FXML
	protected void openEditPopup(ActionEvent event) {
		// Your existing handleEdit logic, adapted
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Edit/Rearrange Other Sevas");

		// *** Work on a DEEP COPY of the tempItemList for the popup ***
		ObservableList<SevaEntry> popupTempList = FXCollections.observableArrayList();
		for(SevaEntry seva : tempItemList) {
			popupTempList.add(new SevaEntry(seva.getName(), seva.getAmount()));
		}


		ListView<SevaEntry> listView = new ListView<>(popupTempList);
		listView.setPrefSize(400, 300);

		// Cell factory for display (including amount editing) and drag/drop
		listView.setCellFactory(lv -> {
			ListCell<SevaEntry> cell = new ListCell<>() {
				@Override
				protected void updateItem(SevaEntry seva, boolean empty) {
					super.updateItem(seva, empty);
					if (empty || seva == null) {
						setGraphic(null);
					} else {
						int index = getIndex() + 1;
						Label slLabel = new Label(index + ". ");
						Label nameLabel = new Label(seva.getName());
						TextField amountField = new TextField(String.format("%.2f", seva.getAmount()));
						amountField.setPrefWidth(100);
						amountField.setAlignment(Pos.CENTER_RIGHT);
						Label rupeeLabel = new Label("₹");


						// Update amount in popupTempList when TextField changes
						amountField.textProperty().addListener((obs, oldVal, newVal) -> {
							String clean = newVal.replace("₹", "").trim();
							try {
								SevaEntry itemToUpdate = popupTempList.get(getIndex());
								if(itemToUpdate != null) {
									itemToUpdate.setAmount(Double.parseDouble(clean));
								}
							} catch (NumberFormatException | IndexOutOfBoundsException ignored) {
								// Optionally revert field or show temporary error style
							}
						});
						// Add validation on focus lost
						amountField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
							if (!isFocused) { // Focus Lost
								String clean = amountField.getText().replace("₹", "").trim();
								try {
									double parsedAmount = Double.parseDouble(clean);
									if(parsedAmount < 0) throw new NumberFormatException();
									// Format back to currency standard on focus lost
									amountField.setText(String.format("%.2f", parsedAmount));
								} catch (NumberFormatException ex) {
									// Revert to original amount if invalid on focus lost
									SevaEntry item = popupTempList.get(getIndex());
									if(item != null) {
										amountField.setText(String.format("%.2f", item.getAmount()));
										// Optionally show brief error indication
									}
								}
							}
						});


						HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
						HBox hbox = new HBox(10, slLabel, nameLabel, spacer, rupeeLabel, amountField);
						hbox.setAlignment(Pos.CENTER_LEFT);
						setGraphic(hbox);
					}
				}
			};

			// Drag and Drop Logic (Modifies popupTempList) - Similar to DonationManager
			cell.setOnDragDetected(ev -> {
				if (cell.getItem() == null) return;
				Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
				ClipboardContent content = new ClipboardContent();
				content.putString(cell.getItem().getName()); // Use Name as ID
				db.setContent(content);
				ev.consume();
			});

			cell.setOnDragOver(ev -> {
				if (ev.getGestureSource() != cell && ev.getDragboard().hasString()) {
					ev.acceptTransferModes(TransferMode.MOVE);
				}
				ev.consume();
			});
			// Visual feedback...

			cell.setOnDragDropped(ev -> {
				if (cell.getItem() == null) return;
				Dragboard db = ev.getDragboard();
				boolean success = false;
				if (db.hasString()) {
					String draggedName = db.getString();
					SevaEntry draggedSeva = null;
					int fromIndex = -1;

					for (int i = 0; i < popupTempList.size(); i++) {
						if (popupTempList.get(i).getName().equals(draggedName)) {
							draggedSeva = popupTempList.get(i);
							fromIndex = i;
							break;
						}
					}

					int toIndex = cell.getIndex();
					if (draggedSeva != null && fromIndex != -1 && fromIndex != toIndex) {
						popupTempList.remove(draggedSeva);
						if (toIndex > popupTempList.size()) toIndex = popupTempList.size();
						popupTempList.add(toIndex, draggedSeva);
						listView.setItems(null); // Refresh list view
						listView.setItems(popupTempList);
						success = true;
					}
				}
				ev.setDropCompleted(success);
				ev.consume();
			});

			cell.setOnDragDone(DragEvent::consume);


			return cell;
		});


		// Save + Cancel buttons for the POPUP
		Button savePopupBtn = new Button("Apply Changes");
		Button cancelPopupBtn = new Button("Cancel");

		// *** POPUP Save Button Logic ***
		savePopupBtn.setOnAction(ev -> {
			// --- Validate amounts before applying ---
			boolean amountsValid = true;
			for (SevaEntry seva : popupTempList) {
				if (seva.getAmount() < 0) { // Check for negative amounts introduced during edit
					amountsValid = false;
					showAlert(Alert.AlertType.ERROR,"Invalid Amount", "Amount for '" + seva.getName() + "' cannot be negative.");
					break;
				}
			}
			if (!amountsValid) return; // Don't close if validation fails

			// Replace main tempItemList with the edited popup list
			tempItemList.setAll(popupTempList);
			refreshGridPane(); // Update the main manager grid
			popupStage.close();
		});

		// *** POPUP Cancel Button Logic ***
		cancelPopupBtn.setOnAction(ev -> {
			popupStage.close();
		});

		// *** Handle Popup Window Close Request (X button) ***
		popupStage.setOnCloseRequest((WindowEvent windowEvent) -> {
			windowEvent.consume(); // Prevent default close
			Optional<ButtonType> result = showConfirmationDialog(
					"Exit Without Saving?",
					"You have unsaved changes in the editor. Are you sure you want to exit without applying them to the main view?"
			);
			if (result.isPresent() && result.get() == ButtonType.OK) {
				popupStage.close(); // Close without applying changes
			}
		});


		HBox buttonBox = new HBox(10, savePopupBtn, cancelPopupBtn);
		buttonBox.setAlignment(Pos.CENTER);
		VBox layout = new VBox(10, listView, buttonBox);
		layout.setPadding(new Insets(15));
		Scene scene = new Scene(layout);
		popupStage.setScene(scene);
		popupStage.showAndWait();
	}

	@Override
	@FXML
	protected void openDeletePopup(ActionEvent event) {
		// Your existing handleDelete logic, adapted
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Delete Other Sevas");

		VBox checkboxContainer = new VBox(10);
		checkboxContainer.setPadding(new Insets(10));
		List<CheckBox> checkBoxes = new ArrayList<>();

		for (SevaEntry seva : tempItemList) {
			CheckBox cb = new CheckBox(seva.getName() + " - ₹" + String.format("%.2f", seva.getAmount()));
			cb.setUserData(seva); // Store object
			checkBoxes.add(cb);
			checkboxContainer.getChildren().add(cb);
		}

		ScrollPane scrollPane = new ScrollPane(checkboxContainer);
		scrollPane.setFitToWidth(true);
		scrollPane.setPrefHeight(Math.min(tempItemList.size() * 30 + 20, 400)); // Adjust height

		Button deleteSelectedButton = new Button("Mark Selected for Deletion");
		Button cancelPopupBtn = new Button("Cancel");

		deleteSelectedButton.setOnAction(e -> {
			List<SevaEntry> toRemoveFromTemp = new ArrayList<>();
			for (CheckBox cb : checkBoxes) {
				if (cb.isSelected()) {
					SevaEntry selectedSeva = (SevaEntry) cb.getUserData();
					if (selectedSeva != null && !itemsMarkedForDeletion.contains(selectedSeva)) {
						// Check if it exists in the original state map before marking for deletion
						if (originalState.containsKey(selectedSeva.getName())) {
							itemsMarkedForDeletion.add(selectedSeva);
							toRemoveFromTemp.add(selectedSeva);
						} else {
							// Just remove unsaved item from temp list
							toRemoveFromTemp.add(selectedSeva);
						}
					}
				}
			}

			boolean itemsRemoved = !toRemoveFromTemp.isEmpty();
			boolean itemsMarked = itemsMarkedForDeletion.stream().anyMatch(toRemoveFromTemp::contains);

			if (itemsRemoved) {
				tempItemList.removeAll(toRemoveFromTemp);
				refreshGridPane();
				if(itemsMarked) {
					showAlert(Alert.AlertType.INFORMATION, "Marked for Deletion", toRemoveFromTemp.size() + " seva(s) marked. Press 'Save' to commit deletion.");
				} else {
					showAlert(Alert.AlertType.INFORMATION, "Items Removed", toRemoveFromTemp.size() + " unsaved seva(s) removed.");
				}
			}
			popupStage.close();
		});

		cancelPopupBtn.setOnAction(e -> popupStage.close());

		HBox buttonBox = new HBox(10, deleteSelectedButton, cancelPopupBtn);
		buttonBox.setAlignment(Pos.CENTER);
		VBox popupLayout = new VBox(15, scrollPane, buttonBox);
		popupLayout.setPadding(new Insets(15));
		Scene scene = new Scene(popupLayout);
		popupStage.setScene(scene);
		popupStage.showAndWait();
	}

	@Override
	@FXML
	protected void handleSave(ActionEvent event) {
		// Implement save logic comparing tempItemList with originalValuesMap
		StringBuilder summary = new StringBuilder();
		boolean changesMade = false;

		// 1. Process Deletions
		if (!itemsMarkedForDeletion.isEmpty()) {
			List<SevaEntry> successfullyDeleted = new ArrayList<>();
			for (SevaEntry sevaToDelete : itemsMarkedForDeletion) {
				String sevaId = repository.getOtherSevaIdByName(sevaToDelete.getName());
				if(sevaId != null) {
					boolean deleted = repository.deleteOtherSevaFromDB(sevaId);
					if (deleted) {
						summary.append("🗑️ Deleted: ").append(sevaToDelete.getName()).append("\n");
						successfullyDeleted.add(sevaToDelete);
						changesMade = true;
					} else {
						summary.append("❌ Delete Failed: ").append(sevaToDelete.getName()).append("\n");
					}
				} else {
					summary.append("❓ Delete Skipped (Not Found): ").append(sevaToDelete.getName()).append("\n");
				}
			}
			itemsMarkedForDeletion.clear(); // Clear list after processing
		}


		// 2. Process Additions and Modifications
		Map<String, SevaEntry> currentItemsMap = new HashMap<>();
		for(SevaEntry currentSeva : tempItemList) {
			currentItemsMap.put(currentSeva.getName(), currentSeva);
		}


		for (int i = 0; i < tempItemList.size(); i++) {
			SevaEntry currentSeva = tempItemList.get(i);
			String currentName = currentSeva.getName();
			double currentAmount = currentSeva.getAmount();
			int desiredOrder = i + 1;

			if (!originalValuesMap.containsKey(currentName)) {
				// New Seva
				String newId = String.valueOf(repository.getMaxOtherSevaId() + 1);
				// Add to DB first (which also calculates initial display order)
				repository.addOtherSevaToDB(newId, currentName, (int) currentAmount); // Pass amount
				// Then explicitly update the order to the desired position
				boolean orderUpdated = OtherSevaRepository.updateDisplayOrder(newId, desiredOrder);
				summary.append("✅ Added: ").append(currentName)
						.append(" (₹").append(currentAmount).append(")")
						.append(orderUpdated ? (" at #" + desiredOrder) : " (Order update failed)")
						.append("\n");
				changesMade = true;
			} else {
				// Existing Seva - check amount and order
				Pair<Double, Integer> originalPair = originalValuesMap.get(currentName);
				double originalAmount = originalPair.getKey();
				int originalOrder = originalPair.getValue();
				String sevaId = repository.getOtherSevaIdByName(currentName); // Get ID for updates

				if (sevaId == null) {
					summary.append("❓ Update Skipped (Not Found): ").append(currentName).append("\n");
					continue; // Skip if ID not found
				}

				boolean amountChanged = currentAmount != originalAmount;
				boolean orderChanged = desiredOrder != originalOrder;

				if (amountChanged) {
					boolean updated = OtherSevaRepository.updateAmount(sevaId, currentAmount);
					if(updated) {
						summary.append("✏️ Amount changed: ").append(currentName)
								.append(" ₹").append(originalAmount)
								.append(" → ₹").append(currentAmount).append("\n");
						changesMade = true;
					} else {
						summary.append("❌ Amount Update Failed: ").append(currentName).append("\n");
					}
				}

				if (orderChanged) {
					boolean updated = OtherSevaRepository.updateDisplayOrder(sevaId, desiredOrder);
					if(updated) {
						summary.append("🔀 Order changed: ").append(currentName)
								.append(" #").append(originalOrder)
								.append(" → #").append(desiredOrder).append("\n");
						changesMade = true;
					} else {
						summary.append("❌ Order Update Failed: ").append(currentName).append("\n");
					}
				}
			}
		}

		// 3. Reload data, update state, refresh UI
		loadData(); // Reloads tempItemList from DB
		storeOriginalState(); // Update original state maps
		itemsMarkedForDeletion.clear(); // Ensure cleared
		refreshGridPane(); // Refresh the grid in the manager


		mainControllerInstance.refreshOtherSevaComboBox(); // Refresh combo box in main view


		// 4. Show Summary
		if (changesMade) {
			showAlert(Alert.AlertType.INFORMATION,"Changes Saved", summary.toString());
		} else {
			showAlert(Alert.AlertType.INFORMATION,"No Changes", "Nothing was modified.");
		}
		// Optional: Close window on successful save
		 closeWindow();
	}

	// Override initialize if needed
	@Override
	public void initialize() {
		super.initialize();
		// Any OtherSevaManager specific initializations
	}

	@Override
	public void setMainController(MainController controller) {
		super.setMainController(controller);
	}
}