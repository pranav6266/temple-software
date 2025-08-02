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
import javafx.util.Pair;

import java.util.*;

public class OthersManagerController extends BaseManagerController<SevaEntry> {

	private final OthersRepository repository = OthersRepository.getInstance();
	public Button editButton;
	public Button openAddOtherSevaButton;
	public Button deleteOtherSeva;

	private final Map<String, Pair<Double, Integer>> originalValuesMap = new HashMap<>();

	@Override
	protected void loadData() {
		OthersRepository.loadOthersFromDB();
		tempItemList.setAll(OthersRepository.getAllOthers());
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
			originalState.put(name, new SevaEntry(name, amount));
			originalValuesMap.put(name, new Pair<>(amount, order));
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

		// Header
		Label slNoHeader = new Label("Sl. No.");
		Label nameHeader = new Label("Other Seva Name");
		Label amountHeader = new Label("Amount");

		slNoHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");
		nameHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");
		amountHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");
		amountHeader.setAlignment(Pos.CENTER_RIGHT);

		itemGridPane.add(slNoHeader, 0, 0);
		itemGridPane.add(nameHeader, 1, 0);
		itemGridPane.add(amountHeader, 2, 0);

		for (int i = 0; i < tempItemList.size(); i++) {
			SevaEntry seva = tempItemList.get(i);
			Label slNo = new Label(String.valueOf(i + 1));
			Label name = new Label(seva.getName());
			Label amount = new Label(String.format("₹%.2f", seva.getAmount()));

			slNo.setAlignment(Pos.CENTER);
			name.setAlignment(Pos.CENTER_LEFT);
			amount.setAlignment(Pos.CENTER_RIGHT);

			itemGridPane.add(slNo, 0, i + 1);
			itemGridPane.add(name, 1, i + 1);
			itemGridPane.add(amount, 2, i + 1);
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

		Label amountLabel = new Label("Amount (₹):");
		amountLabel.getStyleClass().add("popup-field-label");
		TextField amountField = new TextField();
		amountField.getStyleClass().add("popup-text-field");
		amountField.setPromptText("Enter Amount");

		// Add validation listener to amountField
		amountField.textProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null && !newVal.matches("\\d*(\\.\\d*)?")) {
				amountField.setText(oldVal);
			}
		});

		contentContainer.getChildren().addAll(nameLabel, nameField, amountLabel, amountField);

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
			String amountStr = amountField.getText();

			if (name == null || name.trim().isEmpty()) {
				showAlert(Alert.AlertType.ERROR,"Input Error", "Seva Name cannot be empty.");
				return;
			}
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
			} catch (NumberFormatException | NullPointerException ex) {
				showAlert(Alert.AlertType.ERROR,"Invalid Input", "Invalid amount. Please enter a number.");
				return;
			}

			tempItemList.add(new SevaEntry(name.trim(), amount));
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
		popupStage.setTitle("Edit/Rearrange Other Sevas");

		ObservableList<SevaEntry> popupTempList = FXCollections.observableArrayList();
		for(SevaEntry seva : tempItemList) {
			popupTempList.add(new SevaEntry(seva.getName(), seva.getAmount()));
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
								SevaEntry itemToUpdate = popupTempList.get(getIndex());
								if(itemToUpdate != null) {
									itemToUpdate.setAmount(Double.parseDouble(clean));
								}
							} catch (NumberFormatException | IndexOutOfBoundsException ignored) {
							}
						});

						amountField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
							if (!isFocused) {
								String clean = amountField.getText().replace("₹", "").trim();
								try {
									double parsedAmount = Double.parseDouble(clean);
									if(parsedAmount < 0) throw new NumberFormatException();
									amountField.setText(String.format("%.2f", parsedAmount));
								} catch (NumberFormatException ex) {
									SevaEntry item = popupTempList.get(getIndex());
									if(item != null) {
										amountField.setText(String.format("%.2f", item.getAmount()));
									}
								}
							}
						});

						HBox spacer = new HBox();
						HBox.setHgrow(spacer, Priority.ALWAYS);
						HBox hbox = new HBox(10, slLabel, nameLabel, spacer, rupeeLabel, amountField);
						hbox.setAlignment(Pos.CENTER_LEFT);
						setGraphic(hbox);
					}
				}
			};

			// Drag and Drop Logic
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

		Button savePopupBtn = new Button("Apply Changes");
		savePopupBtn.getStyleClass().addAll("manager-button", "manager-save-button");
		Button cancelPopupBtn = new Button("Cancel");
		cancelPopupBtn.getStyleClass().addAll("manager-button", "manager-cancel-button");

		savePopupBtn.setOnAction(ev -> {
			boolean amountsValid = true;
			for (SevaEntry seva : popupTempList) {
				if (seva.getAmount() < 0) {
					amountsValid = false;
					showAlert(Alert.AlertType.ERROR,"Invalid Amount", "Amount for '" + seva.getName() + "' cannot be negative.");
					break;
				}
			}
			if (!amountsValid) return;

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

		Label titleLabel = new Label("Edit/Rearrange Other Sevas");
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
			CheckBox cb = new CheckBox(seva.getName() + " - ₹" + String.format("%.2f", seva.getAmount()));
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
						if (originalState.containsKey(selectedSeva.getName())) {
							itemsMarkedForDeletion.add(selectedSeva);
							toRemoveFromTemp.add(selectedSeva);
						} else {
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
			itemsMarkedForDeletion.clear();
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
				repository.addOtherSevaToDB(newId, currentName, (int) currentAmount);
				boolean orderUpdated = OthersRepository.updateDisplayOrder(newId, desiredOrder);
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
				String sevaId = repository.getOtherSevaIdByName(currentName);

				if (sevaId == null) {
					summary.append("❓ Update Skipped (Not Found): ").append(currentName).append("\n");
					continue;
				}

				boolean amountChanged = currentAmount != originalAmount;
				boolean orderChanged = desiredOrder != originalOrder;

				if (amountChanged) {
					boolean updated = OthersRepository.updateAmount(sevaId, currentAmount);
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
					boolean updated = OthersRepository.updateDisplayOrder(sevaId, desiredOrder);
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
		loadData();
		storeOriginalState();
		itemsMarkedForDeletion.clear();
		refreshGridPane();

		mainControllerInstance.refreshOthersComboBox();

		// 4. Show Summary
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
	}

	@Override
	public void setMainController(MainController controller) {
		super.setMainController(controller);
	}
}
