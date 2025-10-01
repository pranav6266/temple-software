package com.pranav.temple_software.controllers.menuControllers.KaryakramaManager;

import com.pranav.temple_software.models.Others;
import com.pranav.temple_software.repositories.OthersRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class OthersManagerController {

	@FXML
	private TableView<Others> othersTable;
	@FXML
	private TableColumn<Others, String> nameColumn;

	private final OthersRepository repository = OthersRepository.getInstance();
	private final ObservableList<Others> othersList = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		othersTable.setItems(othersList);
		loadOthers();
	}

	private void loadOthers() {
		othersList.setAll(repository.getAllOthers());
	}

	@FXML
	private void handleAdd() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Add New 'Other' Item");
		dialog.setHeaderText("Enter the name for the new item.");
		dialog.setContentText("Name:");
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(name -> {
			if (name.trim().isEmpty()) {
				showAlert(Alert.AlertType.ERROR, "Input Error", "Name cannot be empty.");
				return;
			}
			if (repository.addOtherToDB(name)) {
				loadOthers();
			} else {
				showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add the new item.");
			}
		});
	}

	@FXML
	private void handleRename() {
		Others selected = othersTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item to rename.");
			return;
		}

		TextInputDialog dialog = new TextInputDialog(selected.getName());
		dialog.setTitle("Rename Item");
		dialog.setHeaderText("Enter the new name for '" + selected.getName() + "'.");
		dialog.setContentText("New Name:");
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(newName -> {
			if (newName.trim().isEmpty()) {
				showAlert(Alert.AlertType.ERROR, "Input Error", "Name cannot be empty.");
				return;
			}
			if (repository.updateOtherName(selected.getId(), newName)) {
				loadOthers();
			} else {
				showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to rename the item.");
			}
		});
	}

	@FXML
	private void handleDelete() {
		Others selected = othersTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item to delete.");
			return;
		}

		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
				"Are you sure you want to delete '" + selected.getName() + "'? This action cannot be undone.",
				ButtonType.YES, ButtonType.CANCEL);
		confirmation.setHeaderText("Confirm Deletion");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.YES) {
			if (repository.deleteOtherFromDB(selected.getId())) {
				loadOthers();
			} else {
				showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete the item.");
			}
		}
	}

	@FXML
	private void handleReorder() {
		Dialog<List<Others>> dialog = new Dialog<>();
		dialog.setTitle("Reorder Items");
		dialog.setHeaderText("Drag and drop items to change their order.");

		ListView<Others> listView = new ListView<>(FXCollections.observableArrayList(othersList));
		listView.setCellFactory(lv -> {
			ListCell<Others> cell = new ListCell<>() {
				@Override
				protected void updateItem(Others item, boolean empty) {
					super.updateItem(item, empty);
					setText(empty ? null : item.getName());
				}
			};

			cell.setOnDragDetected(event -> {
				if (cell.isEmpty()) return;
				Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
				ClipboardContent content = new ClipboardContent();
				content.putString(String.valueOf(cell.getIndex()));
				db.setContent(content);
				event.consume();
			});

			cell.setOnDragOver(event -> {
				if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
					event.acceptTransferModes(TransferMode.MOVE);
				}
				event.consume();
			});

			cell.setOnDragDropped(event -> {
				if (event.getDragboard().hasString()) {
					int draggedIndex = Integer.parseInt(event.getDragboard().getString());
					int dropIndex = cell.getIndex();

					ObservableList<Others> items = listView.getItems();
					Others draggedItem = items.remove(draggedIndex);
					items.add(dropIndex, draggedItem);
					event.setDropCompleted(true);
					event.consume();
				}
			});

			return cell;
		});

		dialog.getDialogPane().setContent(listView);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == ButtonType.OK) {
				return listView.getItems();
			}
			return null;
		});

		Optional<List<Others>> result = dialog.showAndWait();
		result.ifPresent(reorderedList -> {
			if(repository.updateDisplayOrder(reorderedList)) {
				loadOthers();
			} else {
				showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save the new order.");
			}
		});
	}

	@FXML
	private void closeWindow() {
		Stage stage = (Stage) othersTable.getScene().getWindow();
		stage.close();
	}

	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}