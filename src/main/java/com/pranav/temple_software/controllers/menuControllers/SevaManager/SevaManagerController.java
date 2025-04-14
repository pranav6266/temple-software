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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SevaManagerController {

	@FXML public GridPane sevaGridPane; //
	public Button openAddSevaButton;
	public Button deleteSeva;
	public Button editButton;


	// *** ADDED FXML Fields for new Controls ***
	@FXML private TextField sevaIdField;
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
	private final List<Seva> addedSevas = new ArrayList<>();
	private final List<Seva> deletedSevas = new ArrayList<>();
	private final Map<String, Double> originalAmounts = new HashMap<>();
	private final Map<String, Integer> originalOrder = new HashMap<>();



	private MainController mainControllerInstance;

	// *** ADD Setter method for MainController instance ***
	public void setMainController(MainController controller) {
		this.mainControllerInstance = controller;
	}
	// *** ADDED initialize method ***
	@FXML
	public void initialize() {
		tempSevaList = FXCollections.observableArrayList(sevaRepository.getAllSevas());
		for (int i = 0; i < tempSevaList.size(); i++) {
			Seva seva = tempSevaList.get(i);
			originalAmounts.put(seva.getId(), seva.getAmount());
			originalOrder.put(seva.getId(), i + 1); // display order
		}
		updateDefaultSevaId();
		// Now refresh the grid pane to display the entries read-only.
		refreshGridPane();
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

			tempSevaList.add(newSeva);
			addedSevas.add(newSeva);
			showAlert("Success", "Seva added successfully!");
			// Refresh the grid in the main view – using your existing refreshGridPane() method.
			tempSevaList = FXCollections.observableArrayList(sevaRepository.getAllSevas());
			refreshGridPane();
			// Update default id for future additions:
			updateDefaultSevaId();
			popupStage.close();
		});

		// Handle cancel button:
		cancelButton.setOnAction(e -> popupStage.close());

		popupStage.showAndWait();
	}

	@FXML
	private void openEditPopup() {
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
			Seva seva = null;
			for (int i = 0; i < tempList.size(); i++) {
				seva = tempList.get(i);
				seva.setDisplayOrder(i + 1);
				sevaRepository.updateDisplayOrder(seva.getId(), i + 1);
				sevaRepository.updateAmount(seva.getId(), seva.getAmount());
			}

			sevaRepository.loadSevasFromDB();
			tempSevaList = FXCollections.observableArrayList(sevaRepository.getAllSevas());
			assert seva != null;
			System.out.println("Updated amount: " + seva.getName() + " - " + seva.getAmount());
			refreshGridPane();
			if (mainControllerInstance != null) {
				mainControllerInstance.refreshSevaCheckboxes();
			}
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


	@FXML
	public void handleSave(ActionEvent actionEvent) {
		StringBuilder summary = new StringBuilder();

		// 1. Save added sevas
		for (Seva seva : addedSevas) {
			boolean added = sevaRepository.addSevaToDB(seva);
			if (added) {
				summary.append("✅ Added: ").append(seva.getName()).append(" (₹").append(seva.getAmount()).append(")\n");
			}
		}

		// 2. Save deleted sevas
		for (Seva seva : deletedSevas) {
			boolean deleted = sevaRepository.deleteSevaFromDB(seva.getId());
			if (deleted) {
				summary.append("🗑️ Deleted: ").append(seva.getName()).append("\n");
			}
		}

		// 3. Check for amount and order changes
		for (int i = 0; i < tempSevaList.size(); i++) {
			Seva seva = tempSevaList.get(i);
			String sevaId = seva.getId();
			double currentAmount = seva.getAmount();
			int currentOrder = i + 1;

			if (originalAmounts.containsKey(sevaId) && currentAmount != originalAmounts.get(sevaId)) {
				sevaRepository.updateAmount(sevaId, currentAmount);
				summary.append("✏️ Amount changed: ").append(seva.getName())
						.append(" ₹").append(originalAmounts.get(sevaId))
						.append(" → ₹").append(currentAmount).append("\n");
			}

			if (originalOrder.containsKey(sevaId) && currentOrder != originalOrder.get(sevaId)) {
				sevaRepository.updateDisplayOrder(sevaId, currentOrder);
				summary.append("🔀 Order changed: ").append(seva.getName())
						.append(" #").append(originalOrder.get(sevaId))
						.append(" → #").append(currentOrder).append("\n");
			}
		}

		// 4. Reload and update views
		sevaRepository.loadSevasFromDB();
		tempSevaList = FXCollections.observableArrayList(sevaRepository.getAllSevas());
		refreshGridPane();
		if (mainControllerInstance != null) {
			mainControllerInstance.refreshSevaCheckboxes();
		}

		// 5. Show summary
		if (!summary.isEmpty()) {
			showAlert("Changes Saved", summary.toString());
		} else {
			showAlert("No Changes", "Nothing was changed.");
			((Stage) saveButton.getScene().getWindow()).close();
		}

		// 6. Clear trackers
		addedSevas.clear();
		deletedSevas.clear();
		originalAmounts.clear();
		originalOrder.clear();

		// Update new base state for next Save
		for (int i = 0; i < tempSevaList.size(); i++) {
			Seva seva = tempSevaList.get(i);
			originalAmounts.put(seva.getId(), seva.getAmount());
			originalOrder.put(seva.getId(), i + 1);
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
					deletedSevas.add(selected);
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