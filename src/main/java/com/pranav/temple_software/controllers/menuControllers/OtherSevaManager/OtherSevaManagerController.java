package com.pranav.temple_software.controllers.menuControllers.OtherSevaManager;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.Seva;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.List;

public class OtherSevaManagerController {

	@FXML public GridPane otherSevaGridPane;
	public Button saveButton;
	public Button rearrangeButton;
	public Button openAddOtherSevaButton;
	public Button deleteOtherSeva;
	public Button cancelButton;

	private final ObservableList<SevaEntry> tempSevaList = FXCollections.observableArrayList();
	private final OtherSevaRepository repository = OtherSevaRepository.getInstance();
	private MainController mainControllerInstance;

	public void setMainController(MainController controller) {
		this.mainControllerInstance = controller;
	}
	@FXML
	public void initialize() {
		tempSevaList.setAll(OtherSevaRepository.getAllOtherSevas());
		refreshGridPane();
	}

	private void refreshGridPane() {
		otherSevaGridPane.getChildren().clear();

		// Header
		otherSevaGridPane.add(new Label("Sl. No."), 0, 0);
		otherSevaGridPane.add(new Label("Other Seva Name"), 1, 0);
		otherSevaGridPane.add(new Label("Amount"), 2, 0);

		for (int i = 0; i < tempSevaList.size(); i++) {
			SevaEntry seva = tempSevaList.get(i);
			Label slNo = new Label(String.valueOf(i + 1));
			Label name = new Label(seva.getName());
			Label amount = new Label(String.format("₹%.2f", seva.getAmount()));

			slNo.setAlignment(Pos.CENTER);
			amount.setAlignment(Pos.CENTER_RIGHT);

			otherSevaGridPane.add(slNo, 0, i + 1);
			otherSevaGridPane.add(name, 1, i + 1);
			otherSevaGridPane.add(amount, 2, i + 1);
		}
	}

	@FXML
	private void handleSave() {
		System.out.println("Saving other sevas...");
		// First, refresh your grid view in the Manager if needed.
		refreshGridPane();

		// Now update the main view UI.
		if (mainControllerInstance != null) {
			mainControllerInstance.refreshOtherSevaComboBox();
		}

		// Close the popup.
		Stage stage = (Stage) saveButton.getScene().getWindow();
		stage.close();
	}



	@FXML
	private void handleAdd() {
		// Create a custom dialog
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("Add Other Seva");
		dialog.setHeaderText("Enter new seva name and amount:");

		// Set the button types
		ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

		// Create the name and amount fields
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField nameField = new TextField();
		nameField.setPromptText("Seva name");

		TextField amountField = new TextField();
		amountField.setPromptText("Amount (₹)");

		grid.add(new Label("Name:"), 0, 0);
		grid.add(nameField, 1, 0);
		grid.add(new Label("Amount:"), 0, 1);
		grid.add(amountField, 1, 1);

		dialog.getDialogPane().setContent(grid);

		// Convert the result to a pair of name and amount
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == addButtonType) {
				return new Pair<>(nameField.getText(), amountField.getText());
			}
			return null;
		});

		dialog.showAndWait().ifPresent(result -> {
			String name = result.getKey();
			String amountStr = result.getValue();

			if (name.isBlank()) return;

			int amount;
			try {
				amount = Integer.parseInt(amountStr);
			} catch (NumberFormatException e) {
				showError("Invalid amount. Please enter a number.");
				return;
			}

			String newId = String.valueOf(repository.getMaxOtherSevaId() + 1);

			repository.addOtherSevaToDB(newId, name, amount);
			tempSevaList.add(new SevaEntry(name, amount));

			refreshGridPane();
		});
	}

	@FXML
	private void handleDelete() {
		Stage popupStage = new Stage();
		VBox layout = new VBox(10);
		List<CheckBox> checkBoxes = FXCollections.observableArrayList();
		for (SevaEntry seva : tempSevaList) {
			CheckBox cb = new CheckBox(seva.getName());
			checkBoxes.add(cb);
			layout.getChildren().add(cb);
		}

		Button save = new Button("Delete Selected");
		save.setOnAction(e -> {
			for (CheckBox cb : checkBoxes) {
				if (cb.isSelected()) {
					String name = cb.getText();
					String id = repository.getOtherSevaIdByName(name);
					if (id != null) {
						repository.deleteOtherSevaFromDB(id);
					}
				}
			}
			tempSevaList.setAll(repository.getAllOtherSevas());
			refreshGridPane();
			popupStage.close();
		});

		layout.getChildren().add(save);
		Scene scene = new Scene(layout, 400, 400);
		popupStage.setScene(scene);
		popupStage.show();
	}

	@FXML
	private void handleClear() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}


	@FXML
	private void handleRearrange() {
		Stage popupStage = new Stage();
		popupStage.setTitle("Rearrange Other Sevas");

		// Load a temporary copy of the list from the repository
		ObservableList<SevaEntry> tempList = FXCollections.observableArrayList(repository.getAllOtherSevas());

		ListView<SevaEntry> listView = new ListView<>(tempList);
		listView.setPrefSize(400, 300);

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

			// Enable drag from this cell
			cell.setOnDragDetected(event -> {
				if (cell.getItem() == null) return;
				Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
				ClipboardContent content = new ClipboardContent();
				content.putString(cell.getItem().getName());
				db.setContent(content);
				event.consume();
			});

			// Accept drop over this cell
			cell.setOnDragOver(event -> {
				if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
					event.acceptTransferModes(TransferMode.MOVE);
				}
				event.consume();
			});

			cell.setOnDragEntered(event -> {
				if (event.getGestureSource() != cell) cell.setOpacity(0.3);
			});
			cell.setOnDragExited(event -> {
				if (event.getGestureSource() != cell) cell.setOpacity(1);
			});

			cell.setOnDragDropped(event -> {
				if (cell.getItem() == null) return;
				Dragboard db = event.getDragboard();
				boolean success = false;
				if (db.hasString()) {
					String draggedName = db.getString();
					SevaEntry draggedSeva = null;
					int fromIndex = -1;

					for (int i = 0; i < tempList.size(); i++) {
						if (tempList.get(i).getName().equals(draggedName)) {
							draggedSeva = tempList.get(i);
							fromIndex = i;
							break;
						}
					}

					int toIndex = cell.getIndex();
					if (draggedSeva != null && fromIndex != toIndex) {
						tempList.remove(draggedSeva);
						tempList.add(toIndex, draggedSeva);
						listView.setItems(null);
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

		// Save + Cancel buttons
		Button saveBtn = new Button("Save");
		Button cancelBtn = new Button("Cancel");
		saveBtn.setOnAction(e -> {
			for (int i = 0; i < tempList.size(); i++) {
				SevaEntry seva = tempList.get(i);
				String id = repository.getOtherSevaIdByName(seva.getName());
				if (id != null) {
					repository.updateDisplayOrder(id, i + 1);
				}
			}
			repository.loadOtherSevasFromDB();
			tempSevaList.setAll(repository.getAllOtherSevas());
			refreshGridPane();
			popupStage.close();
		});

		cancelBtn.setOnAction(e -> popupStage.close());

		// Layout
		HBox buttonBox = new HBox(10, saveBtn, cancelBtn);
		buttonBox.setAlignment(Pos.CENTER);
		VBox layout = new VBox(10, listView, buttonBox);
		layout.setPadding(new Insets(15));
		popupStage.setScene(new Scene(layout));
		popupStage.showAndWait();
	}

	private void showError(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Invalid Input");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
