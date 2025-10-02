package com.pranav.temple_software.controllers.menuControllers.DonationManager;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.controllers.menuControllers.BaseManagerController;
import com.pranav.temple_software.models.Donations;
import com.pranav.temple_software.repositories.DonationRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.*;

public class DonationManagerController extends BaseManagerController<Donations> {

	@FXML private TableView<Donations> itemTableView;
	@FXML private TableColumn<Donations, Integer> slNoColumn;
	@FXML private TableColumn<Donations, String> donationNameColumn;

	public Button openAddDonationButton;
	public Button editButton;
	public Button openDeleteButton;

	private final DonationRepository donationRepository = DonationRepository.getInstance();
	private final Map<String, Integer> originalOrderMap = new HashMap<>();

	@Override
	protected void loadData() {
		donationRepository.loadDonationsFromDB();
		tempItemList.setAll(donationRepository.getAllDonations());
	}

	@Override
	protected void storeOriginalState() {
		originalState.clear();
		originalOrderMap.clear();
		for (int i = 0; i < tempItemList.size(); i++) {
			Donations donation = tempItemList.get(i);
			originalState.put(donation.getName(), new Donations(donation.getId(), donation.getName()));
			originalOrderMap.put(donation.getName(), i + 1);
		}
	}

	@Override
	protected String getItemId(Donations item) { return item.getId(); }

	@Override
	protected void refreshGridPane() {
		itemTableView.setItems(FXCollections.observableArrayList(tempItemList));
		itemTableView.refresh();
	}

	@Override
	@FXML
	protected void openAddPopup(ActionEvent event) {
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Add New Donation");
		VBox mainContainer = new VBox();
		mainContainer.getStyleClass().add("popup-dialog");

		Label titleLabel = new Label("Add New Donation");
		titleLabel.getStyleClass().add("popup-title");

		VBox contentContainer = new VBox();
		contentContainer.getStyleClass().add("popup-content");
		Label nameLabel = new Label("Donation Name:");
		nameLabel.getStyleClass().add("popup-field-label");
		TextField nameField = new TextField();
		nameField.getStyleClass().add("popup-text-field");
		nameField.setPromptText("Enter Donation Name");

		contentContainer.getChildren().addAll(nameLabel, nameField);
		HBox buttonContainer = new HBox();
		buttonContainer.getStyleClass().add("popup-button-container");

		Button submitButton = new Button("Submit");
		submitButton.getStyleClass().addAll("manager-button", "manager-save-button");
		Button cancelPopupBtn = new Button("Cancel");
		cancelPopupBtn.getStyleClass().addAll("manager-button", "manager-cancel-button");
		buttonContainer.getChildren().addAll(submitButton, cancelPopupBtn);
		mainContainer.getChildren().addAll(titleLabel, contentContainer, buttonContainer);

		Scene scene = new Scene(mainContainer);
		scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/modern-manager-popups.css")).toExternalForm());
		popupStage.setScene(scene);
		submitButton.setOnAction(_ -> {
			String donationName = nameField.getText();

			if (donationName == null || donationName.trim().isEmpty()) {
				showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a Donation Name.");
				return;
			}

			boolean exists = tempItemList.stream().anyMatch(item -> item.getName().equalsIgnoreCase(donationName.trim()));
			if (exists) {
				showAlert(Alert.AlertType.ERROR, "Input Error", "Donation '" + donationName + "' already exists.");
				return;
			}

			Donations newDonation = new Donations("NEW_" + System.currentTimeMillis(), donationName);
			tempItemList.add(newDonation);
			refreshGridPane();

			showAlert(Alert.AlertType.INFORMATION, "Success", "Donation '" + donationName + "' staged for addition. Press 'Save' to commit.");
			popupStage.close();
		});
		cancelPopupBtn.setOnAction(_ -> popupStage.close());
		popupStage.showAndWait();
	}

	@Override
	@FXML
	protected void openEditPopup(ActionEvent event) {
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Rearrange Donations");

		ObservableList<Donations> popupTempList = FXCollections.observableArrayList();
		for(Donations donation : tempItemList) {
			popupTempList.add(new Donations(donation.getId(), donation.getName()));
		}

		ListView<Donations> listView = new ListView<>(popupTempList);
		listView.getStyleClass().add("manager-list-view");
		listView.setPrefSize(500, 400);
		listView.setCellFactory(_ -> {
			ListCell<Donations> cell = new ListCell<>() {
				@Override
				protected void updateItem(Donations donation, boolean empty) {
					super.updateItem(donation, empty);
					if (empty || donation == null) {
						setText(null);
					} else {
						int index = getIndex() + 1;
						setText(index + ". " + donation.getName());
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
					Donations draggedDonation = null;
					int fromIndex = -1;
					for (int i = 0; i < popupTempList.size(); i++) {
						if (popupTempList.get(i).getId().equals(draggedId)) {
							draggedDonation = popupTempList.get(i);
							fromIndex = i;
							break;
						}
					}

					int toIndex = cell.getIndex();
					if (draggedDonation != null && fromIndex != toIndex) {
						popupTempList.remove(draggedDonation);

						if (toIndex > popupTempList.size()) {
							toIndex = popupTempList.size();
						} else if (toIndex < 0) {
							toIndex = 0;
						}
						popupTempList.add(toIndex, draggedDonation);

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

		Button savePopupBtn = new Button("Apply Reordering");
		savePopupBtn.getStyleClass().addAll("manager-button", "manager-save-button");
		Button cancelPopupBtn = new Button("Cancel");
		cancelPopupBtn.getStyleClass().addAll("manager-button", "manager-cancel-button");
		savePopupBtn.setOnAction(_ -> {
			tempItemList.setAll(popupTempList);
			refreshGridPane();
			popupStage.close();
		});

		cancelPopupBtn.setOnAction(_ -> popupStage.close());

		popupStage.setOnCloseRequest((WindowEvent windowEvent) -> {
			windowEvent.consume();
			Optional<ButtonType> result = showConfirmationDialog(
					"Exit Without Saving?",
					"You have unsaved changes. Are you sure you want to exit without applying them?"
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

		Label titleLabel = new Label("Rearrange Donations");
		titleLabel.getStyleClass().add("popup-title");

		VBox contentContainer = new VBox();
		contentContainer.getStyleClass().add("popup-content");
		contentContainer.getChildren().add(listView);

		layout.getChildren().addAll(titleLabel, contentContainer, buttonBox);

		Scene scene = new Scene(layout);
		scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/modern-manager-popups.css")).toExternalForm());
		popupStage.setScene(scene);
		popupStage.showAndWait();
	}

	@Override
	@FXML
	protected void openDeletePopup(ActionEvent event) {
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Delete Donations");

		VBox mainContainer = new VBox();
		mainContainer.getStyleClass().add("popup-dialog");
		Label titleLabel = new Label("Select Donations to Delete");
		titleLabel.getStyleClass().add("popup-title");

		VBox checkboxContainer = new VBox(8);
		checkboxContainer.getStyleClass().add("popup-content");
		List<CheckBox> donationCheckBoxes = new ArrayList<>();
		for (Donations donation : tempItemList) {
			CheckBox cb = new CheckBox(donation.getName());
			cb.getStyleClass().add("manager-checkbox");
			cb.setUserData(donation);
			donationCheckBoxes.add(cb);
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

		deleteSelectedButton.setOnAction(_ -> {
			List<Donations> toRemoveFromTemp = new ArrayList<>();
			for (CheckBox cb : donationCheckBoxes) {
				if (cb.isSelected()) {
					Donations selectedDonation = (Donations) cb.getUserData();
					if (selectedDonation != null && !itemsMarkedForDeletion.contains(selectedDonation)) {
						if (originalState.containsKey(selectedDonation.getName())) {
							itemsMarkedForDeletion.add(selectedDonation);
							toRemoveFromTemp.add(selectedDonation);
						} else {
							toRemoveFromTemp.add(selectedDonation);
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
					showAlert(Alert.AlertType.INFORMATION, "Marked for Deletion", toRemoveFromTemp.size() + " donation(s) marked. Press 'Save' to commit deletion.");
				} else {
					showAlert(Alert.AlertType.INFORMATION, "Items Removed", toRemoveFromTemp.size() + " unsaved donation(s) removed.");
				}
			}
			popupStage.close();
		});

		cancelPopupBtn.setOnAction(_ -> popupStage.close());

		HBox buttonBox = new HBox(15);
		buttonBox.getStyleClass().add("popup-button-container");
		buttonBox.getChildren().addAll(deleteSelectedButton, cancelPopupBtn);

		mainContainer.getChildren().addAll(titleLabel, scrollPane, buttonBox);
		Scene scene = new Scene(mainContainer);
		scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/modern-manager-popups.css")).toExternalForm());
		popupStage.setScene(scene);
		popupStage.showAndWait();
	}

	@Override
	@FXML
	public void handleSave(ActionEvent event) {
		StringBuilder summary = new StringBuilder();
		boolean changesMade = false;

		if (!itemsMarkedForDeletion.isEmpty()) {
			List<Donations> successfullyDeleted = new ArrayList<>();
			for (Donations donationToDelete : itemsMarkedForDeletion) {
				String donationId = donationRepository.getDonationIdByName(donationToDelete.getName());
				if(donationId != null) {
					boolean deleted = donationRepository.deleteDonationFromDB(donationId);
					if (deleted) {
						summary.append("🗑️ Deleted: ").append(donationToDelete.getName()).append("\n");
						successfullyDeleted.add(donationToDelete);
						changesMade = true;
					} else {
						summary.append("❌ Delete Failed: ").append(donationToDelete.getName()).append("\n");
					}
				} else {
					summary.append("❓ Delete Skipped (Not Found): ").append(donationToDelete.getName()).append("\n");
				}
			}
			itemsMarkedForDeletion.removeAll(successfullyDeleted);
			itemsMarkedForDeletion.clear();
		}

		Map<String, Donations> currentItemsMap = new HashMap<>();
		for(Donations currentDonation : tempItemList) {
			currentItemsMap.put(currentDonation.getName(), currentDonation);
		}

		for (int i = 0; i < tempItemList.size(); i++) {
			Donations currentDonation = tempItemList.get(i);
			String currentName = currentDonation.getName();
			int desiredOrder = i + 1;

			if (!originalState.containsKey(currentName)) {
				String newId = String.valueOf(donationRepository.getMaxDonationId() + 1);
				boolean added = donationRepository.addDonationToDB(newId, currentName); // Amount is not stored in Donations table
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

		loadData();
		storeOriginalState();
		itemsMarkedForDeletion.clear();
		refreshGridPane();

		if (changesMade) {
			showAlert(Alert.AlertType.INFORMATION, "Changes Saved", summary.toString());
		} else {
			showAlert(Alert.AlertType.INFORMATION, "No Changes", "Nothing was modified.");
		}
		closeWindow();
	}

	@Override
	public void initialize() {
		super.initialize();
		slNoColumn.setCellFactory(_ -> new TableCell<>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? null : String.valueOf(getIndex() + 1));
			}
		});
		donationNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		refreshGridPane();
	}

	@Override
	public void setMainController(MainController controller) {
		super.setMainController(controller);
	}
}