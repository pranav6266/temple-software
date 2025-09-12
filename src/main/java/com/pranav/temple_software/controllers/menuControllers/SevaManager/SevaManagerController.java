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

	@FXML private TextField sevaIdField; // Keep specific fields if needed

	private final SevaRepository sevaRepository = SevaRepository.getInstance();
	private final Map<String, Double> originalAmounts = new HashMap<>(); // Can be part of originalState
	private final Map<String, Integer> originalOrder = new HashMap<>(); // Can be part of originalState

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
		itemGridPane.getChildren().clear();
		Label indexHeader = new Label("No.");
		Label nameHeader = new Label("Seva Name");
		Label amountHeader = new Label("Amount");

		indexHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");
		nameHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");
		amountHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");

		itemGridPane.add(indexHeader, 0, 0);
		itemGridPane.add(nameHeader, 1, 0);
		itemGridPane.add(amountHeader, 2, 0);

		for (int i = 0; i < tempItemList.size(); i++) {
			Seva seva = tempItemList.get(i);
			int rowIndex = i + 1;

			Label orderLabel = new Label(String.valueOf(i + 1));
			Label nameLabel = new Label(seva.getName());
			Label amountLabel = new Label(String.format("₹%.2f", seva.getAmount())); // Format amount

			orderLabel.setAlignment(Pos.CENTER);
			nameLabel.setAlignment(Pos.CENTER_LEFT);
			amountLabel.setAlignment(Pos.CENTER_RIGHT);

			itemGridPane.add(orderLabel, 0, rowIndex);
			itemGridPane.add(nameLabel, 1, rowIndex);
			itemGridPane.add(amountLabel, 2, rowIndex);
		}
	}

	@Override
	@FXML
	protected void openAddPopup(ActionEvent event) {
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Add New Seva");
		// Create main container with modern styling
		VBox mainContainer = new VBox();
		mainContainer.getStyleClass().add("popup-dialog");

		// Title
		Label titleLabel = new Label("Add New Seva");
		titleLabel.getStyleClass().add("popup-title");

		// Content container
		VBox contentContainer = new VBox();
		contentContainer.getStyleClass().add("popup-content");

		// Form fields
		// --- REMOVED ID FIELD ---
		Label nameLabel = new Label("Seva Name:");
		nameLabel.getStyleClass().add("popup-field-label");
		TextField nameField = new TextField();
		nameField.getStyleClass().add("popup-text-field");
		nameField.setPromptText("Enter Seva Name");
		Label amountLabel = new Label("Amount (₹):");
		amountLabel.getStyleClass().add("popup-field-label");
		TextField amountField = new TextField();
		amountField.getStyleClass().add("popup-text-field");
		amountField.setPromptText("Enter Amount");
		contentContainer.getChildren().addAll(
				nameLabel, nameField, amountLabel, amountField
		);

		// Button container
		HBox buttonContainer = new HBox();
		buttonContainer.getStyleClass().add("popup-button-container");

		Button submitButton = new Button("Submit");
		submitButton.getStyleClass().addAll("manager-button", "manager-save-button");
		Button cancelPopupBtn = new Button("Cancel");
		cancelPopupBtn.getStyleClass().addAll("manager-button", "manager-cancel-button");

		buttonContainer.getChildren().addAll(submitButton, cancelPopupBtn);

		mainContainer.getChildren().addAll(titleLabel, contentContainer, buttonContainer);

		Scene scene = new Scene(mainContainer);
		scene.getStylesheets().add(getClass().getResource("/css/modern-manager-popups.css").toExternalForm());
		popupStage.setScene(scene);

		submitButton.setOnAction(e -> {
			String name = nameField.getText();
			String amountStr = amountField.getText();
			if (name == null || name.trim().isEmpty() || amountStr == null || amountStr.trim().isEmpty()) {
				showAlert(Alert.AlertType.ERROR, "Input Error", "Please fill in Seva Name and Amount.");
				return;
			}
			double amount;
			try {
				amount = Double.parseDouble(amountStr);
				if (amount < 0) {
					showAlert(Alert.AlertType.ERROR,"Input Error", "Amount cannot be negative.");
					return;
				}
			} catch (NumberFormatException ex) {
				showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid number for the Amount.");
				return;
			}

			// --- CHANGED: Use a temporary, unique placeholder ID ---
			String tempId = "NEW_" + System.currentTimeMillis();
			Seva newSeva = new Seva(tempId, name, amount);

			// Add to temporary list ONLY
			tempItemList.add(newSeva);
			refreshGridPane(); // Update the main grid view
			showAlert(Alert.AlertType.INFORMATION, "Success", "Seva '" + name + "' staged for addition. Press 'Save' to commit."); // Modified message
			popupStage.close();
		});

		cancelPopupBtn.setOnAction(e -> popupStage.close());
		popupStage.showAndWait();
	}

	@Override
	@FXML
	protected void openEditPopup(ActionEvent event) {
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Edit/Rearrange Sevas");

		// *** Crucial: Work on a DEEP COPY of the tempItemList for the popup ***
		ObservableList<Seva> popupTempList = FXCollections.observableArrayList();
		for (Seva seva : tempItemList) {
			popupTempList.add(new Seva(seva.getId(), seva.getName(), seva.getAmount(), seva.getDisplayOrder()));
		}

		ListView<Seva> listView = new ListView<>(popupTempList);
		listView.getStyleClass().add("manager-list-view");
		listView.setPrefSize(500, 400);

		// Cell factory for display and drag/drop
		listView.setCellFactory(lv -> {
			ListCell<Seva> cell = new ListCell<>() {
				@Override
				protected void updateItem(Seva seva, boolean empty) {
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
						amountField.getStyleClass().add("popup-text-field");
						Label rupeeLabel = new Label("₹");

						// *** Listener updates the Seva object IN THE POPUP LIST ***
						amountField.textProperty().addListener((obs, oldVal, newVal) -> {
							String clean = newVal.replace("₹", "").trim();
							try {
								Seva itemToUpdate = popupTempList.get(getIndex());
								if(itemToUpdate != null) {
									itemToUpdate.setAmount(Double.parseDouble(clean));
								}
							} catch (NumberFormatException | IndexOutOfBoundsException ignored) {}
						});

						HBox spacer = new HBox();
						HBox.setHgrow(spacer, Priority.ALWAYS);
						HBox hbox = new HBox(10, slLabel, nameLabel, spacer, rupeeLabel, amountField);
						hbox.setAlignment(Pos.CENTER_LEFT);
						setGraphic(hbox);
					}
				}
			};

			// --- Drag and Drop Logic ---
			cell.setOnDragDetected(ev -> {
				if (cell.getItem() == null) return;
				Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
				ClipboardContent content = new ClipboardContent();
				content.putString(cell.getItem().getId());
				db.setContent(content);
				ev.consume();
			});

			cell.setOnDragOver(ev -> {
				if (ev.getGestureSource() != cell && ev.getDragboard().hasString()) {
					ev.acceptTransferModes(TransferMode.MOVE);
				}
				ev.consume();
			});

			cell.setOnDragDropped(ev -> {
				if (cell.getItem() == null) return;
				Dragboard db = ev.getDragboard();
				boolean success = false;
				if (db.hasString()) {
					String draggedId = db.getString();
					Seva draggedSeva = null;
					int fromIndex = -1;

					for (int i = 0; i < popupTempList.size(); i++) {
						if (popupTempList.get(i).getId().equals(draggedId)) {
							draggedSeva = popupTempList.get(i);
							fromIndex = i;
							break;
						}
					}

					int toIndex = cell.getIndex();
					if (draggedSeva != null && fromIndex != toIndex) {
						popupTempList.remove(draggedSeva);
						if (toIndex > popupTempList.size()) {
							toIndex = popupTempList.size();
						}
						popupTempList.add(toIndex, draggedSeva);
						listView.setItems(null);
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

		Button savePopupBtn = new Button("Save Changes to Main View");
		savePopupBtn.getStyleClass().addAll("manager-button", "manager-save-button");
		Button cancelPopupBtn = new Button("Cancel Edit");
		cancelPopupBtn.getStyleClass().addAll("manager-button", "manager-cancel-button");

		savePopupBtn.setOnAction(ev -> {
			tempItemList.setAll(popupTempList);
			refreshGridPane();
			popupStage.close();
		});

		cancelPopupBtn.setOnAction(ev -> {
			popupStage.close();
		});

		popupStage.setOnCloseRequest((WindowEvent windowEvent) -> {
			windowEvent.consume();
			Optional<ButtonType> result = showConfirmationDialog(
					"Exit Without Saving?",
					"You have unsaved changes in the editor. Are you sure you want to exit without applying them to the main view?"
			);
			if (result.isPresent() && result.get() == ButtonType.OK) {
				popupStage.close();
			}
		});

		HBox buttonBox = new HBox(15);
		buttonBox.getStyleClass().add("popup-button-container");
		buttonBox.getChildren().addAll(savePopupBtn, cancelPopupBtn);

		VBox layout = new VBox(20);
		layout.getStyleClass().add("popup-dialog");

		Label titleLabel = new Label("Edit/Rearrange Sevas");
		titleLabel.getStyleClass().add("popup-title");

		VBox contentContainer = new VBox();
		contentContainer.getStyleClass().add("popup-content");
		contentContainer.getChildren().add(listView);

		layout.getChildren().addAll(titleLabel, contentContainer, buttonBox);

		Scene scene = new Scene(layout);
		scene.getStylesheets().add(getClass().getResource("/css/modern-manager-popups.css").toExternalForm());
		popupStage.setScene(scene);
		popupStage.showAndWait();
	}

	@Override
	@FXML
	protected void openDeletePopup(ActionEvent event) {
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Delete Sevas");

		VBox mainContainer = new VBox();
		mainContainer.getStyleClass().add("popup-dialog");

		Label titleLabel = new Label("Select Sevas to Delete");
		titleLabel.getStyleClass().add("popup-title");

		VBox checkboxContainer = new VBox(8);
		checkboxContainer.getStyleClass().add("popup-content");
		List<CheckBox> sevaCheckBoxes = new ArrayList<>();

		for (Seva seva : tempItemList) {
			CheckBox cb = new CheckBox(seva.getName() + " - ₹" + String.format("%.2f", seva.getAmount()));
			cb.getStyleClass().add("manager-checkbox");
			cb.setUserData(seva);
			sevaCheckBoxes.add(cb);
			checkboxContainer.getChildren().add(cb);
		}

		ScrollPane scrollPane = new ScrollPane(checkboxContainer);
		scrollPane.getStyleClass().add("manager-scroll-pane");
		scrollPane.setFitToWidth(true);
		scrollPane.setPrefHeight(Math.min(tempItemList.size() * 35 + 20, 300));

		Button deleteSelectedButton = new Button("Mark Selected for Deletion");
		deleteSelectedButton.getStyleClass().addAll("manager-button", "manager-delete-button");
		Button cancelPopupBtn = new Button("Cancel");
		cancelPopupBtn.getStyleClass().addAll("manager-button", "manager-cancel-button");

		deleteSelectedButton.setOnAction(e -> {
			List<Seva> toRemoveFromTemp = new ArrayList<>();
			for (CheckBox cb : sevaCheckBoxes) {
				if (cb.isSelected()) {
					Seva selectedSeva = (Seva) cb.getUserData();
					if (selectedSeva != null && !itemsMarkedForDeletion.contains(selectedSeva)) {
						itemsMarkedForDeletion.add(selectedSeva);
						toRemoveFromTemp.add(selectedSeva);
					}
				}
			}
			if (!toRemoveFromTemp.isEmpty()) {
				tempItemList.removeAll(toRemoveFromTemp);
				refreshGridPane();
				showAlert(Alert.AlertType.INFORMATION,"Marked for Deletion", toRemoveFromTemp.size() + " seva(s) marked. Press 'Save' to commit deletion.");
			}
			popupStage.close();
		});

		cancelPopupBtn.setOnAction(e -> popupStage.close());

		HBox buttonBox = new HBox(15);
		buttonBox.getStyleClass().add("popup-button-container");
		buttonBox.getChildren().addAll(deleteSelectedButton, cancelPopupBtn);

		mainContainer.getChildren().addAll(titleLabel, scrollPane, buttonBox);

		Scene scene = new Scene(mainContainer);
		scene.getStylesheets().add(getClass().getResource("/css/modern-manager-popups.css").toExternalForm());
		popupStage.setScene(scene);
		popupStage.showAndWait();
	}

	@Override
	@FXML
	protected void handleSave(ActionEvent actionEvent) {
		StringBuilder summary = new StringBuilder();
		boolean changesMade = false;
		// 1. Process Deletions
		if (!itemsMarkedForDeletion.isEmpty()) {
			for (Seva sevaToDelete : itemsMarkedForDeletion) {
				// Only try to delete items that actually exist in the DB
				if (!sevaToDelete.getId().startsWith("NEW_")) {
					boolean deleted = sevaRepository.deleteSevaFromDB(sevaToDelete.getId());
					if (deleted) {
						summary.append("🗑️ Deleted: ").append(sevaToDelete.getName()).append("\n");
						changesMade = true;
					} else {
						summary.append("❌ Delete Failed: ").append(sevaToDelete.getName()).append("\n");
					}
				}
			}
		}

		// 2. Process Additions and Modifications
		for (int i = 0; i < tempItemList.size(); i++) {
			Seva currentSeva = tempItemList.get(i);
			String currentId = currentSeva.getId();
			int desiredOrder = i + 1;

			// --- CHANGED: This block now handles adding new items ---
			if (currentId != null && currentId.startsWith("NEW_")) {
				// It's a new Seva, get a real ID now
				int newId = sevaRepository.getMaxSevaId() + 1;
				Seva sevaToSave = new Seva(String.valueOf(newId), currentSeva.getName(), currentSeva.getAmount());
				sevaToSave.setDisplayOrder(desiredOrder);

				boolean added = sevaRepository.addSevaToDB(sevaToSave);
				if (added) {
					summary.append("✅ Added: ").append(sevaToSave.getName())
							.append(" (₹").append(String.format("%.2f", sevaToSave.getAmount())).append(") at #").append(desiredOrder).append("\n");
					changesMade = true;
				} else {
					summary.append("❌ Add Failed: ").append(sevaToSave.getName()).append("\n");
				}
			} else {
				// It's an existing Seva, check for modifications
				Seva originalSeva = originalState.get(currentId);
				if (originalSeva == null) continue; // Should not happen, but a safeguard

				boolean amountChanged = currentSeva.getAmount() != originalSeva.getAmount();
				boolean orderChanged = desiredOrder != originalOrder.getOrDefault(currentId, -1);

				if (amountChanged) {
					boolean updated = sevaRepository.updateAmount(currentId, currentSeva.getAmount());
					if(updated) {
						summary.append("✏️ Amount changed: ").append(currentSeva.getName())
								.append(" ₹").append(String.format("%.2f", originalSeva.getAmount()))
								.append(" → ₹").append(String.format("%.2f", currentSeva.getAmount())).append("\n");
						changesMade = true;
					} else {
						summary.append("❌ Amount Update Failed: ").append(currentSeva.getName()).append("\n");
					}
				}
				if (orderChanged) {
					boolean updated = sevaRepository.updateDisplayOrder(currentId, desiredOrder);
					if(updated) {
						summary.append("🔀 Order changed: ").append(currentSeva.getName())
								.append(" #").append(originalOrder.getOrDefault(currentId, -1))
								.append(" → #").append(desiredOrder).append("\n");
						changesMade = true;
					} else {
						summary.append("❌ Order Update Failed: ").append(currentSeva.getName()).append("\n");
					}
				}
			}
		}

		// 3. Reload data from DB to ensure consistency and refresh UI components
		loadData();
		storeOriginalState();
		itemsMarkedForDeletion.clear();
		refreshGridPane();
		if (mainControllerInstance != null) {
			mainControllerInstance.refreshSevaCheckboxes();
		}

		// 4. Show Summary
		if (changesMade) {
			showAlert(Alert.AlertType.INFORMATION,"Changes Saved", summary.toString());
		} else {
			showAlert(Alert.AlertType.INFORMATION,"No Changes", "Nothing was modified.");
		}
		closeWindow();
	}

	private void updateDefaultSevaId() {
		if (sevaRepository != null && sevaIdField != null) {
			try {
				int maxId = sevaRepository.getMaxSevaId();
				int nextSevaId = maxId + 1;
				sevaIdField.setText(String.valueOf(nextSevaId));
			} catch (Exception e) {
				System.err.println("Error calculating default Seva ID: " + e.getMessage());
				showAlert(Alert.AlertType.ERROR,"Error", "Could not determine next Seva ID.");
				sevaIdField.setText("ERR");
			}
		}
	}

	@Override
	public void initialize() {
		super.initialize();
		updateDefaultSevaId();
	}
}
