// FILE: src/main/java/com/pranav/temple_software/controllers/menuControllers/DonationManager/DonationManagerController.java
package com.pranav.temple_software.controllers.menuControllers.DonationManager;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.controllers.menuControllers.BaseManagerController;
import com.pranav.temple_software.models.Donations;
import com.pranav.temple_software.models.Donations; // Using Donations for Donations
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.DonationRepository;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


import java.util.*;

// Extend BaseManagerController using Donations for Donations
public class DonationManagerController extends BaseManagerController<Donations> {
	@Override
	protected String getItemId(SevaEntry item) {
		return "";
	}

	@Override
	protected String getItemName(SevaEntry item) {
		return "";
	}
// Inherited FXML fields: itemGridPane, saveButton, cancelButton, openAddButton, openEditButton, openDeleteButton
	// Remove duplicate FXML declarations if they exist here 

	private final DonationRepository donationRepository = DonationRepository.getInstance();
	public Button openAddDonationButton;
	public Button editButton;
	// Inherited: mainControllerInstance, tempItemList, itemsMarkedForDeletion, originalState

	// Store original order separately for easier comparison during save
	private final Map<String, Integer> originalOrderMap = new HashMap<>();

	@Override
	protected void loadData() {
		donationRepository.loadDonationsFromDB(); // Load from DB
		// CORRECTED LINE: Remove the invalid cast '(Donations)'
		tempItemList.setAll(donationRepository.getAllDonations()); // Populate temp list
	}

	@Override
	protected void storeOriginalState() {
		originalState.clear();
		originalOrderMap.clear();
		for (int i = 0; i < tempItemList.size(); i++) {
			Donations donation = tempItemList.get(i);
			// Store a copy or relevant info. Since Donations is mutable, store key info.
			// Here, we mainly care about the name and original order.
			originalState.put(donation.getName(), new Donations(donation.getName(), donation.getName(), 0)); // Store name, amount irrelevant
			originalOrderMap.put(donation.getName(), i + 1);
		}
	}


	@Override
	protected String getItemId(Donations item) {
		// Donations might not have a strict ID like Sevas, using name as identifier
		return item.getName();
	}
	@Override
	protected String getItemName(Donations item) {
		return item.getName();
	}


	@Override
	protected void refreshGridPane() {
		// Your existing refreshGridPane logic, adapted for BaseManagerController fields
		// Ensure it DOES NOT display amount
		itemGridPane.getChildren().clear(); // Use itemGridPane from base class
		Label indexHeader = new Label("Sl. Number");
		Label nameHeader = new Label("Donation Name");

		indexHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");
		nameHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");

		itemGridPane.add(indexHeader, 0, 0);
		itemGridPane.add(nameHeader, 1, 0);

		for (int i = 0; i < tempItemList.size(); i++) { // Use tempItemList from base class
			Donations donation = tempItemList.get(i);
			int rowIndex = i + 1;

			Label orderLabel = new Label(String.valueOf(rowIndex)); // Use rowIndex
			Label nameLabel = new Label(donation.getName());

			orderLabel.setAlignment(Pos.CENTER);
			nameLabel.setAlignment(Pos.CENTER_LEFT);

			itemGridPane.add(orderLabel, 0, rowIndex);
			itemGridPane.add(nameLabel, 1, rowIndex);
		}
	}


	@Override
	@FXML
	protected void openAddPopup(ActionEvent event) {
		// Your existing openAddDonationPopup logic
		// Modify to add to tempItemList only
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Add New Donation");

		// Note: Donations don't have separate ID and Amount fields in this manager
		Label nameLabel = new Label("Donation Name:");
		TextField nameField = new TextField();
		nameField.setPromptText("Enter Donation Name");

		Button submitButton = new Button("Submit");
		Button cancelPopupBtn = new Button("Cancel");

		GridPane grid = new GridPane();
		grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
		grid.add(nameLabel, 0, 0); grid.add(nameField, 1, 0);


		HBox buttonBox = new HBox(10, submitButton, cancelPopupBtn);
		buttonBox.setAlignment(Pos.CENTER);
		VBox layout = new VBox(15, grid, buttonBox);
		layout.setPadding(new Insets(20));
		Scene scene = new Scene(layout);
		popupStage.setScene(scene);

		submitButton.setOnAction(e -> {
			String donationName = nameField.getText();

			if (donationName == null || donationName.trim().isEmpty()) {
				showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a Donation Name.");
				return;
			}

			// Check if donation name already exists in temp list
			boolean exists = tempItemList.stream().anyMatch(item -> item.getName().equalsIgnoreCase(donationName.trim()));
			if (exists) {
				showAlert(Alert.AlertType.ERROR, "Input Error", "Donation '" + donationName + "' already exists.");
				return;
			}


			// Create a new Donations (amount is irrelevant here)
			Donations newDonation = new Donations(donationName,donationName,0);
			// Add to temporary list ONLY
			tempItemList.add(newDonation);
			refreshGridPane(); // Update the main grid view

			showAlert(Alert.AlertType.INFORMATION, "Success", "Donation '" + donationName + "' staged for addition. Press 'Save' to commit.");
			popupStage.close();
		});

		cancelPopupBtn.setOnAction(e -> popupStage.close());
		popupStage.showAndWait();
	}


	@Override
	@FXML
	protected void openEditPopup(ActionEvent event) {
		// This corresponds to the old openRearrangePopup
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Rearrange Donations");

		// *** Work on a DEEP COPY of the tempItemList for the popup ***
		ObservableList<Donations> popupTempList = FXCollections.observableArrayList();
		for(Donations donation : tempItemList) {
			popupTempList.add(new Donations(donation.getName(), donation.getName(), 0)); // Copy relevant info
		}


		ListView<Donations> listView = new ListView<>(popupTempList);
		listView.setPrefSize(400, 300);

		// Cell factory for display and drag/drop
		listView.setCellFactory(lv -> {
			ListCell<Donations> cell = new ListCell<Donations>() {
				@Override
				protected void updateItem(Donations donation, boolean empty) {
					super.updateItem(donation, empty);
					if (empty || donation == null) {
						setText(null);
					} else {
						int index = getIndex() + 1; // Serial number
						setText(index + ". " + donation.getName()); // Display name only
					}
				}
			};

			// Drag and Drop Logic (Modifies popupTempList)
			// Ensure you use popupTempList inside the D&D handlers

			cell.setOnDragDetected(ev -> {
				if (cell.getItem() == null) return;
				Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
				ClipboardContent content = new ClipboardContent();
				// Use name as identifier since ID might not be formal
				content.putString(cell.getItem().getName());
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
					Donations draggedDonation = null;
					int fromIndex = -1;

					// Find in popupTempList
					for (int i = 0; i < popupTempList.size(); i++) {
						if (popupTempList.get(i).getName().equals(draggedName)) {
							draggedDonation = popupTempList.get(i);
							fromIndex = i;
							break;
						}
					}

					int toIndex = cell.getIndex();
					if (draggedDonation != null && fromIndex != toIndex) {
						// Prevent dropping onto itself if indices are same after removal
						popupTempList.remove(draggedDonation); // Modify popupTempList

						// Adjust drop index if item was moved downwards
						if (toIndex > fromIndex) {
							// No adjustment needed as remove shifts subsequent items up
						}

						// Clamp toIndex to valid range after removal
						if (toIndex > popupTempList.size()) {
							toIndex = popupTempList.size();
						} else if (toIndex < 0) {
							toIndex = 0;
						}

						popupTempList.add(toIndex, draggedDonation); // Modify popupTempList

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
		Button savePopupBtn = new Button("Apply Reordering");
		Button cancelPopupBtn = new Button("Cancel");

		// *** POPUP Save Button Logic ***
		savePopupBtn.setOnAction(ev -> {
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
		// Your existing openDeleteDonationPopup logic
		// Modify to use itemsMarkedForDeletion and tempItemList
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Delete Donations");

		VBox checkboxContainer = new VBox(10);
		checkboxContainer.setPadding(new Insets(10));
		List<CheckBox> donationCheckBoxes = new ArrayList<>();

		for (Donations donation : tempItemList) { // Use current tempItemList
			CheckBox cb = new CheckBox(donation.getName());
			cb.setUserData(donation); // Store the object
			donationCheckBoxes.add(cb);
			checkboxContainer.getChildren().add(cb);
		}

		ScrollPane scrollPane = new ScrollPane(checkboxContainer);
		scrollPane.setFitToWidth(true);
		scrollPane.setPrefHeight(Math.min(tempItemList.size() * 30 + 20, 400)); // Adjust height


		Button deleteSelectedButton = new Button("Mark Selected for Deletion");
		Button cancelPopupBtn = new Button("Cancel");

		deleteSelectedButton.setOnAction(e -> {
			List<Donations> toRemoveFromTemp = new ArrayList<>();
			for (CheckBox cb : donationCheckBoxes) {
				if (cb.isSelected()) {
					Donations selectedDonation = (Donations) cb.getUserData();
					if (selectedDonation != null && !itemsMarkedForDeletion.contains(selectedDonation)) {
						// Check if it exists in the original state map before marking for deletion
						if (originalState.containsKey(selectedDonation.getName())) {
							itemsMarkedForDeletion.add(selectedDonation); // Mark for deletion
							toRemoveFromTemp.add(selectedDonation);      // Mark for removal from current view
						} else {
							// If it's not in original state, it's a newly added item not yet saved.
							// Just remove it from the temp list directly.
							toRemoveFromTemp.add(selectedDonation);
						}
					}
				}
			}

			boolean itemsRemoved = !toRemoveFromTemp.isEmpty();
			boolean itemsMarked = itemsMarkedForDeletion.stream().anyMatch(toRemoveFromTemp::contains);


			if (itemsRemoved) {
				tempItemList.removeAll(toRemoveFromTemp); // Remove from temp list
				refreshGridPane(); // Update main grid
				if(itemsMarked) {
					showAlert(Alert.AlertType.INFORMATION, "Marked for Deletion", toRemoveFromTemp.size() + " donation(s) marked. Press 'Save' to commit deletion.");
				} else {
					showAlert(Alert.AlertType.INFORMATION, "Items Removed", toRemoveFromTemp.size() + " unsaved donation(s) removed.");
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
		// Implement save logic comparing tempItemList with originalState
		StringBuilder summary = new StringBuilder();
		boolean changesMade = false;

		// 1. Process Deletions
		if (!itemsMarkedForDeletion.isEmpty()) {
			List<Donations> successfullyDeleted = new ArrayList<>();
			for (Donations donationToDelete : itemsMarkedForDeletion) {
				String donationId = donationRepository.getDonationIdByName(donationToDelete.getName());
				if(donationId != null) {
					boolean deleted = donationRepository.deleteDonationFromDB(donationId);
					if (deleted) {
						summary.append("🗑️ Deleted: ").append(donationToDelete.getName()).append("\n");
						successfullyDeleted.add(donationToDelete); // Track successful deletions
						changesMade = true;
					} else {
						summary.append("❌ Delete Failed: ").append(donationToDelete.getName()).append("\n");
					}
				} else {
					summary.append("❓ Delete Skipped (Not Found): ").append(donationToDelete.getName()).append("\n");
				}
			}
			itemsMarkedForDeletion.removeAll(successfullyDeleted); // Remove successfully deleted items
			// Keep failed deletions in the list? Or clear it fully? Clearing it assumes we won't retry.
			// Let's clear it fully for now. If deletion fails, the item will reappear on next load.
			itemsMarkedForDeletion.clear();
		}


		// 2. Process Additions and Order Changes
		Map<String, Donations> currentItemsMap = new HashMap<>();
		for(Donations currentDonation : tempItemList) {
			currentItemsMap.put(currentDonation.getName(), currentDonation);
		}


		for (int i = 0; i < tempItemList.size(); i++) {
			Donations currentDonation = tempItemList.get(i);
			String currentName = currentDonation.getName();
			int desiredOrder = i + 1;

			if (!originalState.containsKey(currentName)) {
				// It's a new Donation
				String newId = String.valueOf(donationRepository.getMaxDonationId() + 1); // Calculate potential ID
				boolean added = donationRepository.addDonationToDB(newId, currentName, 0); // Amount 0
				// Explicitly set the display order after adding
				if(added) {
					boolean orderUpdated = donationRepository.updateDisplayOrder(newId, desiredOrder);
					summary.append("✅ Added: ").append(currentName)
							.append(orderUpdated ? (" at #" + desiredOrder) : " (Order update failed)")
							.append("\n");
					changesMade = true;
				} else {
					summary.append("❌ Add Failed: ").append(currentName).append("\n");
				}

			} else {
				// Existing donation, check for order change
				int originalOrder = originalOrderMap.getOrDefault(currentName, -1);
				if (originalOrder != desiredOrder) {
					String donationId = donationRepository.getDonationIdByName(currentName);
					if(donationId != null) {
						boolean updated = donationRepository.updateDisplayOrder(donationId, desiredOrder);
						if (updated) {
							summary.append("🔀 Order changed: ").append(currentName)
									.append(" #").append(originalOrder)
									.append(" → #").append(desiredOrder).append("\n");
							changesMade = true;
						} else {
							summary.append("❌ Order Update Failed: ").append(currentName).append("\n");
						}
					} else {
						summary.append("❓ Order Update Skipped (Not Found): ").append(currentName).append("\n");
					}
				}
			}
		}

		// Check for items that were in originalState but are now missing from tempItemList AND not marked for deletion
		// This shouldn't happen with the current delete logic, but good defensive check.

         for (String originalName : originalState.keySet()) {
             if (!currentItemsMap.containsKey(originalName) && itemsMarkedForDeletion.stream().noneMatch(d -> d.getName().equals(originalName))) {
                 // This item was removed without using the delete popup - handle as needed (e.g., log warning)
                 summary.append("⚠️ Implicitly Removed (Not Saved): ").append(originalName).append("\n");
             }
         }



		// 3. Reload data, update state, refresh UI
		loadData(); // Reloads tempItemList from DB
		storeOriginalState(); // Update original state map
		itemsMarkedForDeletion.clear(); // Ensure cleared after save attempt
		refreshGridPane(); // Refresh the grid in the manager

		if (mainControllerInstance != null) {
			mainControllerInstance.refreshDonationComboBox(); // Refresh combo box in main view
		}

		// 4. Show Summary
		if (changesMade) {
			showAlert(Alert.AlertType.INFORMATION, "Changes Saved", summary.toString());
		} else {
			showAlert(Alert.AlertType.INFORMATION, "No Changes", "Nothing was modified.");
		}
		// Optional: Close window on successful save
		 closeWindow();
	}

	// Override initialize if needed
	@Override
	public void initialize() {
		super.initialize();
		// Any DonationManager specific initializations
	}

	// Override setMainController if needed (likely not, base class handles it)
	 @Override
	 public void setMainController(MainController controller) {
	    super.setMainController(controller);
	 }
}