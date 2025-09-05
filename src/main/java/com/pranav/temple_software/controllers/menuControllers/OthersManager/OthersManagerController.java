package com.pranav.temple_software.controllers.menuControllers.OthersManager;
import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.controllers.menuControllers.BaseManagerController;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.OthersRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import java.util.*;

public class OthersManagerController extends BaseManagerController<SevaEntry> {

	private final OthersRepository repository = OthersRepository.getInstance();
	private final Map<String, Integer> originalOrderMap = new HashMap<>();

	@Override
	protected void loadData() {
		OthersRepository.loadOthersFromDB();
		tempItemList.setAll(OthersRepository.getAllOthers());
	}

	@Override
	protected void storeOriginalState() {
		originalState.clear();
		originalOrderMap.clear();
		for (int i = 0; i < tempItemList.size(); i++) {
			SevaEntry seva = tempItemList.get(i);
			originalState.put(seva.getName(), new SevaEntry(seva.getName(), 0.0));
			originalOrderMap.put(seva.getName(), i + 1);
		}
	}

	@Override
	protected String getItemId(SevaEntry item) {
		return item.getName();
	}
	@Override
	protected String getItemName(SevaEntry item) {
		return item.getName();
	}

	@Override
	protected void refreshGridPane() {
		itemGridPane.getChildren().clear();
		Label slNoHeader = new Label("Sl. No.");
		Label nameHeader = new Label("Other Seva Name");
		slNoHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");
		nameHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");

		itemGridPane.add(slNoHeader, 0, 0);
		itemGridPane.add(nameHeader, 1, 0);

		for (int i = 0; i < tempItemList.size(); i++) {
			SevaEntry seva = tempItemList.get(i);
			Label slNo = new Label(String.valueOf(i + 1));
			Label name = new Label(seva.getName());

			slNo.setAlignment(Pos.CENTER);
			name.setAlignment(Pos.CENTER_LEFT);

			itemGridPane.add(slNo, 0, i + 1);
			itemGridPane.add(name, 1, i + 1);
		}
	}

	@Override
	@FXML
	protected void openAddPopup(ActionEvent event) {
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Add Other Seva");

		VBox mainContainer = new VBox();
		mainContainer.getStyleClass().add("popup-dialog");
		Label titleLabel = new Label("Add New Other Seva");
		titleLabel.getStyleClass().add("popup-title");

		VBox contentContainer = new VBox();
		contentContainer.getStyleClass().add("popup-content");
		Label nameLabel = new Label("Seva Name:");
		nameLabel.getStyleClass().add("popup-field-label");
		TextField nameField = new TextField();
		nameField.getStyleClass().add("popup-text-field");
		nameField.setPromptText("Enter Seva name");

		contentContainer.getChildren().addAll(nameLabel, nameField);
		HBox buttonContainer = new HBox();
		buttonContainer.getStyleClass().add("popup-button-container");

		Button addButton = new Button("Add");
		addButton.getStyleClass().addAll("manager-button", "manager-save-button");
		Button cancelButton = new Button("Cancel");
		cancelButton.getStyleClass().addAll("manager-button", "manager-cancel-button");
		buttonContainer.getChildren().addAll(addButton, cancelButton);
		mainContainer.getChildren().addAll(titleLabel, contentContainer, buttonContainer);

		Scene scene = new Scene(mainContainer);
		scene.getStylesheets().add(getClass().getResource("/css/modern-manager-popups.css").toExternalForm());
		popupStage.setScene(scene);
		addButton.setOnAction(e -> {
			String name = nameField.getText();
			if (name == null || name.trim().isEmpty()) {
				showAlert(Alert.AlertType.ERROR,"Input Error", "Seva Name cannot be empty.");
				return;
			}
			if (tempItemList.stream().anyMatch(s -> s.getName().equalsIgnoreCase(name.trim()))) {
				showAlert(Alert.AlertType.ERROR,"Input Error", "Other Seva '" + name + "' already exists.");
				return;
			}

			tempItemList.add(new SevaEntry(name.trim(), 0.0));
			refreshGridPane();
			showAlert(Alert.AlertType.INFORMATION,"Success", "Other Seva '" + name + "' staged for addition. Press 'Save' to commit.");
			popupStage.close();
		});

		cancelButton.setOnAction(e -> popupStage.close());
		popupStage.showAndWait();
	}

	@Override
	@FXML
	protected void openEditPopup(ActionEvent event) {
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Rearrange Other Sevas");

		ObservableList<SevaEntry> popupTempList = FXCollections.observableArrayList();
		for(SevaEntry seva : tempItemList) {
			popupTempList.add(new SevaEntry(seva.getName(), 0.0));
		}

		ListView<SevaEntry> listView = new ListView<>(popupTempList);
		listView.getStyleClass().add("manager-list-view");
		listView.setPrefSize(500, 400);

		listView.setCellFactory(lv -> {
			ListCell<SevaEntry> cell = new ListCell<>() {
				@Override
				protected void updateItem(SevaEntry seva, boolean empty) {
					super.updateItem(seva, empty);
					if (empty || seva == null) {
						setText(null);
					} else {
						setText((getIndex() + 1) + ". " + seva.getName());
					}
				}
			};

			cell.setOnDragDetected(ev -> {
				if (cell.getItem() == null) return;
				Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
				ClipboardContent content = new ClipboardContent();
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
			cell.setOnDragDropped(ev -> {
				if (cell.getItem() == null) return;
				Dragboard db = ev.getDragboard();
				boolean success = false;
				if (db.hasString()) {
					String draggedName = db.getString();
					SevaEntry draggedSeva = popupTempList.stream()
							.filter(s -> s.getName().equals(draggedName))
							.findFirst().orElse(null);

					if (draggedSeva != null) {
						popupTempList.remove(draggedSeva);
						int toIndex = cell.getIndex(); // <-- THIS IS THE FIX
						if (toIndex >= popupTempList.size()) {
							popupTempList.add(draggedSeva);
						} else {
							popupTempList.add(toIndex, draggedSeva);
						}
						success = true;
					}
				}
				ev.setDropCompleted(success);
				ev.consume();
			});
			cell.setOnDragDone(DragEvent::consume);
			return cell;
		});

		Button savePopupBtn = new Button("Apply Changes");
		savePopupBtn.getStyleClass().addAll("manager-button", "manager-save-button");
		Button cancelPopupBtn = new Button("Cancel");
		cancelPopupBtn.getStyleClass().addAll("manager-button", "manager-cancel-button");
		savePopupBtn.setOnAction(ev -> {
			tempItemList.setAll(popupTempList);
			refreshGridPane();
			popupStage.close();
		});
		cancelPopupBtn.setOnAction(ev -> popupStage.close());

		popupStage.setOnCloseRequest((WindowEvent windowEvent) -> {
			windowEvent.consume();
			Optional<ButtonType> result = showConfirmationDialog("Exit Without Saving?","You have unsaved changes. Are you sure you want to exit?");
			if (result.isPresent() && result.get() == ButtonType.OK) {
				popupStage.close();
			}
		});

		HBox buttonBox = new HBox(15);
		buttonBox.getStyleClass().add("popup-button-container");
		buttonBox.getChildren().addAll(savePopupBtn, cancelPopupBtn);
		VBox layout = new VBox(20);
		layout.getStyleClass().add("popup-dialog");
		Label titleLabel = new Label("Rearrange Other Sevas");
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
		popupStage.setTitle("Delete Other Sevas");

		VBox mainContainer = new VBox();
		mainContainer.getStyleClass().add("popup-dialog");
		Label titleLabel = new Label("Select Other Sevas to Delete");
		titleLabel.getStyleClass().add("popup-title");
		VBox checkboxContainer = new VBox(8);
		checkboxContainer.getStyleClass().add("popup-content");
		List<CheckBox> checkBoxes = new ArrayList<>();
		for (SevaEntry seva : tempItemList) {
			CheckBox cb = new CheckBox(seva.getName());
			cb.getStyleClass().add("manager-checkbox");
			cb.setUserData(seva);
			checkBoxes.add(cb);
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
			List<SevaEntry> toRemoveFromTemp = new ArrayList<>();
			for (CheckBox cb : checkBoxes) {
				if (cb.isSelected()) {
					SevaEntry selectedSeva = (SevaEntry) cb.getUserData();
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
	protected void handleSave(ActionEvent event) {
		StringBuilder summary = new StringBuilder();
		boolean changesMade = false;

		if (!itemsMarkedForDeletion.isEmpty()) {
			for (SevaEntry sevaToDelete : itemsMarkedForDeletion) {
				String sevaId = repository.getOtherSevaIdByName(sevaToDelete.getName());
				if(sevaId != null) {
					boolean deleted = repository.deleteOtherSevaFromDB(sevaId);
					if (deleted) {
						summary.append("🗑️ Deleted: ").append(sevaToDelete.getName()).append("\n");
						changesMade = true;
					} else {
						summary.append("❌ Delete Failed: ").append(sevaToDelete.getName()).append("\n");
					}
				}
			}
			itemsMarkedForDeletion.clear();
		}

		for (int i = 0; i < tempItemList.size(); i++) {
			SevaEntry currentSeva = tempItemList.get(i);
			String currentName = currentSeva.getName();
			int desiredOrder = i + 1;

			if (!originalOrderMap.containsKey(currentName)) {
				String newId = String.valueOf(repository.getMaxOtherSevaId() + 1);
				repository.addOtherSevaToDB(newId, currentName);
				OthersRepository.updateDisplayOrder(newId, desiredOrder);
				summary.append("✅ Added: ").append(currentName).append(" at #").append(desiredOrder).append("\n");
				changesMade = true;
			} else {
				int originalOrder = originalOrderMap.get(currentName);
				if (desiredOrder != originalOrder) {
					String sevaId = repository.getOtherSevaIdByName(currentName);
					if (sevaId != null) {
						boolean updated = OthersRepository.updateDisplayOrder(sevaId, desiredOrder);
						if(updated) {
							summary.append("🔀 Order changed: ").append(currentName).append(" #").append(originalOrder).append(" → #").append(desiredOrder).append("\n");
							changesMade = true;
						} else {
							summary.append("❌ Order Update Failed: ").append(currentName).append("\n");
						}
					}
				}
			}
		}

		loadData();
		storeOriginalState();
		itemsMarkedForDeletion.clear();
		refreshGridPane();
		mainControllerInstance.refreshOthersComboBox();

		if (changesMade) {
			showAlert(Alert.AlertType.INFORMATION,"Changes Saved", summary.toString());
		} else {
			showAlert(Alert.AlertType.INFORMATION,"No Changes", "Nothing was modified.");
		}
		closeWindow();
	}
}