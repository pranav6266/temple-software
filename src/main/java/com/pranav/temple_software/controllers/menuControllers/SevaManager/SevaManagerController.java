package com.pranav.temple_software.controllers.menuControllers.SevaManager;

import com.pranav.temple_software.controllers.menuControllers.BaseManagerController;
import com.pranav.temple_software.models.Seva;
import com.pranav.temple_software.repositories.SevaRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.*;

public class SevaManagerController extends BaseManagerController<Seva> {
	public Button editButton;
	public Button openAddSevaButton;
	public Button deleteSeva;

	// --- NEW TABLEVIEW AND COLUMNS ---
	@FXML private TableView<Seva> itemTableView;
	@FXML private TableColumn<Seva, Integer> slNoColumn;
	@FXML private TableColumn<Seva, String> sevaNameColumn;
	@FXML private TableColumn<Seva, Double> amountColumn;


	private final SevaRepository sevaRepository = SevaRepository.getInstance();
	private final Map<String, Double> originalAmounts = new HashMap<>();
	private final Map<String, Integer> originalOrder = new HashMap<>();

	@Override
	protected void loadData() {
		sevaRepository.loadSevasFromDB();
		tempItemList.setAll(sevaRepository.getAllSevas());
	}

	@Override
	protected void storeOriginalState() {
		originalState.clear();
		originalAmounts.clear();
		originalOrder.clear();
		for (int i = 0; i < tempItemList.size(); i++) {
			Seva seva = tempItemList.get(i);
			originalState.put(seva.getId(), new Seva(seva.getId(), seva.getName(), seva.getAmount(), seva.getDisplayOrder()));
			originalAmounts.put(seva.getId(), seva.getAmount());
			originalOrder.put(seva.getId(), i + 1);
		}
	}

	@Override
	protected String getItemId(Seva item) {
		return item.getId();
	}

	@Override
	protected void refreshGridPane() {
		// This method is now repurposed to refresh the TableView
		itemTableView.setItems(FXCollections.observableArrayList(tempItemList));
		itemTableView.refresh();
	}


	@Override
	@FXML
	protected void openAddPopup(ActionEvent event) {
		Stage popupStage = new Stage();
		popupStage.initOwner(itemTableView.getScene().getWindow());
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Add New Seva");
		VBox mainContainer = new VBox();
		mainContainer.getStyleClass().add("popup-dialog");

		Label titleLabel = new Label("Add New Seva");
		titleLabel.getStyleClass().add("popup-title");

		VBox contentContainer = new VBox();
		contentContainer.getStyleClass().add("popup-content");

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

			String tempId = "NEW_" + System.currentTimeMillis();
			Seva newSeva = new Seva(tempId, name, amount);

			tempItemList.add(newSeva);
			refreshGridPane();
			showAlert(Alert.AlertType.INFORMATION, "Success", "Seva '" + name
					+ "' staged for addition. Press 'Save' to commit.");
			popupStage.close();
		});

		cancelPopupBtn.setOnAction(e -> popupStage.close());
		popupStage.showAndWait();
	}

	@Override
	@FXML
	protected void openEditPopup(ActionEvent event) {
		Stage popupStage = new Stage();
		popupStage.initOwner(itemTableView.getScene().getWindow());
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Edit/Rearrange Sevas");

		ObservableList<Seva> popupTempList = FXCollections.observableArrayList();
		for (Seva seva : tempItemList) {
			popupTempList.add(new Seva(seva.getId(), seva.getName(), seva.getAmount(), seva.getDisplayOrder()));
		}

		ListView<Seva> listView = new ListView<>(popupTempList);
		listView.getStyleClass().add("manager-list-view");
		listView.setPrefSize(500, 400);

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
		popupStage.initOwner(itemTableView.getScene().getWindow());
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
	public void handleSave(ActionEvent actionEvent) {
		StringBuilder summary = new StringBuilder();
		boolean changesMade = false;
		if (!itemsMarkedForDeletion.isEmpty()) {
			for (Seva sevaToDelete : itemsMarkedForDeletion) {
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

		for (int i = 0; i < tempItemList.size(); i++) {
			Seva currentSeva = tempItemList.get(i);
			String currentId = currentSeva.getId();
			int desiredOrder = i + 1;
			if (currentId != null && currentId.startsWith("NEW_")) {
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
				Seva originalSeva = originalState.get(currentId);
				if (originalSeva == null) continue;

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

		loadData();
		storeOriginalState();
		itemsMarkedForDeletion.clear();
		refreshGridPane();
		if (mainControllerInstance != null) {
			mainControllerInstance.refreshSevaCheckboxes();
		}

		if (changesMade) {
			showAlert(Alert.AlertType.INFORMATION,"Changes Saved", summary.toString());
		} else {
			showAlert(Alert.AlertType.INFORMATION,"No Changes", "Nothing was modified.");
		}
		closeWindow();
	}

	@Override
	public void initialize() {
		super.initialize();
		// --- NEW: Initialize Table Columns ---
		slNoColumn.setCellFactory(col -> new TableCell<>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? null : String.valueOf(getIndex() + 1));
			}
		});
		sevaNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
		amountColumn.setCellFactory(column -> new TableCell<>() {
			@Override
			protected void updateItem(Double amount, boolean empty) {
				super.updateItem(amount, empty);
				if (empty || amount == null) {
					setText(null);
				} else {
					setText(String.format("₹%.2f", amount));
				}
			}
		});
		refreshGridPane(); // Initial data load
	}
}