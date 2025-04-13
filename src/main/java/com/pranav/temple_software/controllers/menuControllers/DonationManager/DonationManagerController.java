package com.pranav.temple_software.controllers.menuControllers.DonationManager;

import com.pranav.temple_software.controllers.MainController;
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
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

public class DonationManagerController {

	@FXML public GridPane DonationGridPane;
	@FXML public Button openAddDonationButton;
	@FXML public Button saveButton;
	@FXML public Button rearrangeButton;
	@FXML public Button cancelButton;

	private final DonationRepository donationRepository = DonationRepository.getInstance();
	public Button openDeleteButton;
	private int nextDonationId = 0;
	private ObservableList<SevaEntry> tempDonationList;
	private final List<SevaEntry> donationsMarkedForDeletion = new ArrayList<>();
	private MainController mainControllerInstance;


	@FXML
	public void initialize() {
		tempDonationList = FXCollections.observableArrayList(donationRepository.getAllDonations());
		updateDefaultDonationId();
		refreshGridPane();
		rearrangeButton.setOnAction(e -> openRearrangePopup());
	}
	public void setMainController(MainController controller) {
		this.mainControllerInstance = controller;
		if (mainControllerInstance != null) {
			mainControllerInstance.refreshDonationComboBox();
		}
	}

	@FXML
	private void openAddDonationPopup() {
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Add New Donation");

		Label idLabel = new Label("Donation ID:");
		TextField idField = new TextField();
		idField.setEditable(false);
		idField.setText(String.valueOf(donationRepository.getMaxDonationId() + 1));

		Label nameLabel = new Label("Donation Name:");
		TextField nameField = new TextField();
		nameField.setPromptText("Enter Donation Name");

		Button submitButton = new Button("Submit");
		Button cancelButton = new Button("Cancel");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));
		grid.add(idLabel, 0, 0);
		grid.add(idField, 1, 0);
		grid.add(nameLabel, 0, 1);
		grid.add(nameField, 1, 1);

		HBox buttonBox = new HBox(10, submitButton, cancelButton);
		buttonBox.setAlignment(Pos.CENTER);

		VBox layout = new VBox(15, grid, buttonBox);
		layout.setPadding(new Insets(20));

		Scene scene = new Scene(layout);
		popupStage.setScene(scene);

		submitButton.setOnAction(e -> {
			String donationName = nameField.getText();

			if (donationName == null || donationName.trim().isEmpty()) {
				showAlert("Input Error", "Please enter a Donation Name.");
				return;
			}

			String donationId = idField.getText();
			boolean success = donationRepository.addDonationToDB(donationId, donationName, 0);
			if (success) {
				showAlert("Success", "Donation added successfully!");
				tempDonationList = FXCollections.observableArrayList(donationRepository.getAllDonations());
				refreshGridPane();
				popupStage.close();
			} else {
				showAlert("Database Error", "Failed to add donation. Check logs.");
			}
		});

		cancelButton.setOnAction(e -> popupStage.close());
		popupStage.showAndWait();
		mainControllerInstance.refreshSevaCheckboxes();
	}

	private void openRearrangePopup() {
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Rearrange Donations");

		ObservableList<SevaEntry> tempList = FXCollections.observableArrayList(donationRepository.getAllDonations());
		ListView<SevaEntry> listView = new ListView<>(tempList);
		listView.setPrefSize(400, 300);

		listView.setCellFactory(new Callback<ListView<SevaEntry>, ListCell<SevaEntry>>() {
			@Override
			public ListCell<SevaEntry> call(ListView<SevaEntry> lv) {
				ListCell<SevaEntry> cell = new ListCell<SevaEntry>() {
					@Override
					protected void updateItem(SevaEntry donation, boolean empty) {
						super.updateItem(donation, empty);
						if (empty || donation == null) {
							setText(null);
						} else {
							// Remove the amount display here
							setText(donation.getName()); // Only show name
						}
					}
				};
				cell.setOnDragDetected(event -> {
					if (cell.getItem() == null) return;
					Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
					ClipboardContent content = new ClipboardContent();
					content.putString(cell.getItem().getName());
					db.setContent(content);
					event.consume();
				});
				cell.setOnDragOver(event -> {
					if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
						event.acceptTransferModes(TransferMode.MOVE);
					}
					event.consume();
				});
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
				cell.setOnDragDropped(event -> {
					if (cell.getItem() == null) return;
					Dragboard db = event.getDragboard();
					boolean success = false;
					if (db.hasString()) {
						String draggedDonationName = db.getString();
						SevaEntry draggedDonation = null;
						int draggedIndex = -1;
						for (int i = 0; i < tempList.size(); i++) {
							if (tempList.get(i).getName().equals(draggedDonationName)) {
								draggedDonation = tempList.get(i);
								draggedIndex = i;
								break;
							}
						}
						int dropIndex = cell.getIndex();
						if (draggedDonation != null && draggedIndex != dropIndex) {
							tempList.remove(draggedDonation);
							tempList.add(dropIndex, draggedDonation);
							success = true;
						}
					}
					event.setDropCompleted(success);
					event.consume();
				});
				cell.setOnDragDone(DragEvent::consume);
				return cell;
			}
		});

		Button saveBtn = new Button("Save");
		Button cancelBtn = new Button("Cancel");
		HBox buttonBox = new HBox(10, saveBtn, cancelBtn);
		buttonBox.setAlignment(Pos.CENTER);

		VBox popupLayout = new VBox(10, listView, buttonBox);
		popupLayout.setPadding(new Insets(10));
		popupLayout.setAlignment(Pos.CENTER);

		Scene popupScene = new Scene(popupLayout);
		popupStage.setScene(popupScene);

		saveBtn.setOnAction(ev -> {
			for (int i = 0; i < tempList.size(); i++) {
				// Here you would update the new display order in the database.
				// For example, perform:
				// String donationId = ... ; int newOrder = i + 1;
				// donationRepository.updateDisplayOrder(donationId, newOrder);
				// (Note: Since we’re not storing donationId with SevaEntry here,
				// you may need to extend your Donation model.)
			}
			donationRepository.loadDonationsFromDB();
			// Optionally call mainController refresh if needed.
			popupStage.close();
		});

		cancelBtn.setOnAction(ev -> popupStage.close());
		popupStage.showAndWait();
	}

	private void updateDefaultDonationId() {
		int maxId = donationRepository.getMaxDonationId();
		nextDonationId = maxId + 1;
		// Optionally update any UI label that shows the next donation id.
	}

	private void refreshGridPane() {
		DonationGridPane.getChildren().clear();
		Label indexHeader = new Label("Sl. Number");
		Label nameHeader = new Label("Donation Name");


		indexHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");
		nameHeader.setStyle("-fx-font-weight: bold; -fx-background-color: lightgray;");


		DonationGridPane.add(indexHeader, 0, 0);
		DonationGridPane.add(nameHeader, 1, 0);


		for (int i = 0; i < tempDonationList.size(); i++) {
			SevaEntry donation = tempDonationList.get(i);
			int rowIndex = i + 1;
			Label orderLabel = new Label(String.valueOf(i + 1));
			Label nameLabel = new Label(donation.getName());


			orderLabel.setAlignment(Pos.CENTER);
			nameLabel.setAlignment(Pos.CENTER_LEFT);


			DonationGridPane.add(orderLabel, 0, rowIndex);
			DonationGridPane.add(nameLabel, 1, rowIndex);

		}
	}

	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	@FXML
	public void handleCancelButton(ActionEvent actionEvent) {
		((Stage) cancelButton.getScene().getWindow()).close(); // Close the current stage/window
	}



	public void openDeleteDonationPopup(ActionEvent actionEvent) {
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Delete Donations");

		VBox checkboxContainer = new VBox(10);
		checkboxContainer.setPadding(new Insets(10));
		List<CheckBox> donationCheckBoxes = new ArrayList<>();

		for (SevaEntry donation : tempDonationList) {
			CheckBox cb = new CheckBox(donation.getName() + " - ₹" + String.format("%.2f", donation.getAmount()));
			donationCheckBoxes.add(cb);
			checkboxContainer.getChildren().add(cb);
		}

		ScrollPane scrollPane = new ScrollPane(checkboxContainer);
		scrollPane.setPrefWidth(200);
		scrollPane.setPrefHeight(700);
		scrollPane.setFitToWidth(true);

		Button saveBtn = new Button("Save");
		Button cancelBtn = new Button("Cancel");
		HBox buttonBox = new HBox(10, saveBtn, cancelBtn);
		buttonBox.setAlignment(Pos.CENTER);

		VBox popupLayout = new VBox(15, scrollPane, buttonBox);
		popupLayout.setPadding(new Insets(15));

		Scene scene = new Scene(popupLayout);
		popupStage.setScene(scene);

		saveBtn.setOnAction(e -> {
			List<SevaEntry> toRemove = new ArrayList<>();
			for (int i = 0; i < donationCheckBoxes.size(); i++) {
				if (donationCheckBoxes.get(i).isSelected()) {
					SevaEntry selected = tempDonationList.get(i);
					toRemove.add(selected);
					donationsMarkedForDeletion.add(selected);
				}
			}
			tempDonationList.removeAll(toRemove);
			refreshGridPane();
			popupStage.close();
		});

		cancelBtn.setOnAction(e -> popupStage.close());

		popupStage.showAndWait();
	}


	public void handleFinalDonationDeleteSave(ActionEvent actionEvent) {
		for (SevaEntry donation : donationsMarkedForDeletion) {
			String name = donation.getName();
			// You might need a way to map name to ID; assuming method exists:
			String donationId = donationRepository.getDonationIdByName(name);
			if (donationId != null) {
				boolean deleted = donationRepository.deleteDonationFromDB(donationId);
				if (!deleted) {
					showAlert("Delete Failed", "Could not delete Donation '" + name + "'");
				}
			}
		}
		donationsMarkedForDeletion.clear();
		donationRepository.loadDonationsFromDB();
		tempDonationList = FXCollections.observableArrayList(donationRepository.getAllDonations());
		refreshGridPane();
		if (mainControllerInstance != null) {
			mainControllerInstance.refreshDonationComboBox();
		}
		showAlert("Success", "Selected donations permanently deleted.");
	}
}