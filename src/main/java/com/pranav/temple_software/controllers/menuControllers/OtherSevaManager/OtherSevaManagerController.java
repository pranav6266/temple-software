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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private final Map<String, Double> originalAmounts = new HashMap<>();
	private final Map<String, Integer> originalOrders = new HashMap<>();
	private final List<String> originalNames = new ArrayList<>();

	public void setMainController(MainController controller) {
		this.mainControllerInstance = controller;
	}
	@FXML
	public void initialize() {
		tempSevaList.setAll(OtherSevaRepository.getAllOtherSevas());
		// Store original values
		for (int i = 0; i < tempSevaList.size(); i++) {
			SevaEntry seva = tempSevaList.get(i);
			originalAmounts.put(seva.getName(), seva.getAmount());
			originalOrders.put(seva.getName(), i + 1);
			originalNames.add(seva.getName());
		}
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
		StringBuilder summary = new StringBuilder();

		for (int i = 0; i < tempSevaList.size(); i++) {
			SevaEntry seva = tempSevaList.get(i);
			String id = repository.getOtherSevaIdByName(seva.getName());

			// New seva added
			if (!originalNames.contains(seva.getName())) {
				String newId = String.valueOf(repository.getMaxOtherSevaId() + 1);
				repository.addOtherSevaToDB(newId, seva.getName(),(int) seva.getAmount());
				OtherSevaRepository.updateDisplayOrder(newId, i + 1);
				summary.append("✅ Added: ").append(seva.getName())
						.append(" - ₹").append(seva.getAmount()).append("\n");
			} else {
				// Modified amount
				double originalAmount = originalAmounts.get(seva.getName());
				if (seva.getAmount() != originalAmount) {
					OtherSevaRepository.updateAmount(id, seva.getAmount());
					summary.append("✏️ Amount changed: ").append(seva.getName())
							.append(" ₹").append(originalAmount)
							.append(" → ₹").append(seva.getAmount()).append("\n");
				}

				// Modified order
				int originalOrder = originalOrders.get(seva.getName());
				if ((i + 1) != originalOrder) {
					OtherSevaRepository.updateDisplayOrder(id, i + 1);
					summary.append("🔀 Order changed: ").append(seva.getName())
							.append(" #").append(originalOrder)
							.append(" → #").append(i + 1).append("\n");
				}
			}
		}

		// Detect deleted sevas
		for (String name : originalNames) {
			boolean exists = tempSevaList.stream().anyMatch(seva -> seva.getName().equals(name));
			if (!exists) {
				String id = repository.getOtherSevaIdByName(name);
				repository.deleteOtherSevaFromDB(id);
				summary.append("🗑️ Deleted: ").append(name).append("\n");
			}
		}

		// Final updates
		repository.loadOtherSevasFromDB();
		tempSevaList.setAll(OtherSevaRepository.getAllOtherSevas());
		refreshGridPane();

		if (mainControllerInstance != null) {
			mainControllerInstance.refreshOtherSevaComboBox();
		}

		if (!summary.isEmpty()) {
			showInfo("Changes Saved", summary.toString());
		} else {
			showInfo("No Changes", "Nothing was modified.");
		}

		((Stage) saveButton.getScene().getWindow()).close();
	}




	private void showInfo(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
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
	private void handleEdit() {
		Stage popupStage = new Stage();
		popupStage.setTitle("Edit Other Sevas");

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
						setGraphic(null);
					} else {
						int index = getIndex() + 1;

						Label slLabel = new Label(index + ". ");
						Label nameLabel = new Label(seva.getName());
						TextField amountField = new TextField(String.format("%.2f", seva.getAmount()));
						amountField.setPrefWidth(100);
						amountField.setAlignment(Pos.CENTER_RIGHT);
						Label rupeeLabel = new Label("₹");
						// Optional: strip ₹ and update seva
						amountField.textProperty().addListener((obs, oldVal, newVal) -> {
							String clean = newVal.replace("₹", "").trim();
							try {
								seva.setAmount(Double.parseDouble(clean));
							} catch (NumberFormatException ignored) {}
						});

						HBox spacer = new HBox(); // takes up remaining space
						HBox.setHgrow(spacer, Priority.ALWAYS);

						HBox hbox = new HBox(10, slLabel, nameLabel, spacer,rupeeLabel, amountField);
						hbox.setAlignment(Pos.CENTER_LEFT);
						setGraphic(hbox);
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
					OtherSevaRepository.updateDisplayOrder(id, i + 1);
					OtherSevaRepository.updateAmount(id, seva.getAmount());
				}
			}
			repository.loadOtherSevasFromDB();
			tempSevaList.setAll(OtherSevaRepository.getAllOtherSevas());
			refreshGridPane();
			mainControllerInstance.refreshOtherSevaComboBox();
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
