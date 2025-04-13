package com.pranav.temple_software.services;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.SevaEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

public class Tables {
	MainController controller;
	public Tables(MainController mainController) {
		this.controller = mainController;
	}


	public void setupTableView() {
		// Serial number column
		controller.slNoColumn.setCellFactory(col -> new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? null : String.valueOf(getIndex() + 1));
				setAlignment(Pos.CENTER);
			}
		});

		// Seva name column
		controller.sevaNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());


		// Amount column
		TableColumn<SevaEntry, Number> amountColumn = (TableColumn<SevaEntry, Number>) controller.sevaTableView.getColumns().get(2);
		amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty());

		// Format amount as currency
		amountColumn.setCellFactory(tc -> new TableCell<>() {
			@Override
			protected void updateItem(Number amount, boolean empty) {
				super.updateItem(amount, empty);
				if (empty || amount == null) {
					setText(null);
				} else {
					setText(String.format("₹%.2f", amount.doubleValue()));
					setAlignment(Pos.CENTER_RIGHT);
				}
			}
		});

		{
			// Add a new TableColumn for actions
			TableColumn<SevaEntry, Void> actionColumn = new TableColumn<>("Action");
			actionColumn.setCellFactory(col -> new TableCell<>() {
				private final Button removeButton = new Button("Remove");

				{
					removeButton.setOnAction(event -> {
						SevaEntry entry = getTableView().getItems().get(getIndex());
						controller.selectedSevas.remove(entry);
					});
				}

				@Override
				protected void updateItem(Void item, boolean empty) {
					super.updateItem(item, empty);
					setGraphic(empty ? null : removeButton);
				}
			});

			// Add the column to your TableView
			controller.sevaTableView.getColumns().add(actionColumn);

			ObservableList<String> rashis = FXCollections.observableArrayList(
					"ಆಯ್ಕೆ", "ಮೇಷ", "ವೃಷಭ", "ಮಿಥುನ", "ಕರ್ಕಾಟಕ", "ಸಿಂಹ", "ಕನ್ಯಾ",
					"ತುಲಾ", "ವೃಶ್ಚಿಕ", "ಧನು", "ಮಕರ", "ಕುಂಭ", "ಮೀನ"
			);

			ObservableList<String> nakshatras = FXCollections.observableArrayList(
					"ಆಯ್ಕೆ", "ಅಶ್ವಿನಿ", "ಭರಣಿ", "ಕೃತಿಕ", "ರೋಹಿಣಿ", "ಮೃಗಶಿರ", "ಆರ್ದ್ರ",
					"ಪುನರ್ವಸು", "ಪುಷ್ಯ", "ಆಶ್ಲೇಷ", "ಮಘ", "ಪೂರ್ವ ಫಲ್ಗುಣಿ", "ಉತ್ತರ ಫಲ್ಗುಣಿ",
					"ಹಸ್ತ", "ಚಿತ್ತ", "ಸ್ವಾತಿ", "ವಿಶಾಖ", "ಅನೂರಾಧ", "ಜ್ಯೇಷ್ಠ",
					"ಮೂಲ", "ಪೂರ್ವಾಷಾಢ", "ಉತ್ತರಾಷಾಢ", "ಶ್ರವಣ", "ಧನಿಷ್ಠ", "ಶತಭಿಷ",
					"ಪೂರ್ವ ಭಾದ್ರಪದ", "ಉತ್ತರ ಭಾದ್ರಪದ", "ರೇವತಿ"
			);


			ObservableList<String> donations = FXCollections.observableArrayList(
					"ಆಯ್ಕೆ",
					"ಸ್ಥಳ ಕಾಣಿಕ",
					"ಪಾತ್ರೆ ಬಾಡಿಗೆ",
					"ವಿದ್ಯುತ್",
					"ಜನರೇಟರ್", "ಕಟ್ಟಿಗೆ", "ತೆಂಗಿನಕಾಯಿ", "ಅರ್ಚಕರ ದಕ್ಷಿಣೆ", "ಅಡಿಗೆಯವರಿಗೆ", "ಕೂಲಿ", "ಊಟೋಪಚಾರದ ಬಗ್ಗೆ", "ಇತರ ಖರ್ಚಿನ ಬಾಬ್ತು"
			);



			ObservableList<String> otherSevaReciepts = FXCollections.observableArrayList(
					"ಆಯ್ಕೆ",
					"ಶತ ರುದ್ರಾಭಿಷೇಕ",
					"ಸಾಮೂಹಿಕ ಆಶ್ಲೇಷ ಬಲಿ",
					"ಶ್ರೀಕೃಷ್ಣ ಜನ್ಮಾಷ್ಟಮಿ",
					"ವರಮಹಾಲಕ್ಷ್ಮೀ  ಪೂಜೆ",
					"ಪ್ರತಿಷ್ಠಾ ದಿನ (ಕಳಭ)",
					"ಸಮಾಜ ಸೇವಾ ಕಾರ್ಯಗಳು",
					"ನಿತ್ಯ-ನೈಮಿತ್ತಿಕ ಕಾರ್ಯಗಳು",
					"ಜೀರ್ಣೋದ್ಧಾರ ಕಾರ್ಯಗಳು",
					"ಅಭಿವೃದ್ಧಿ ಕಾರ್ಯಗಳು",
					"ಅನ್ನದಾನ"
			);

			// *** Ensure SevaListener instance exists in controller before calling this ***
			// controller.sevaListener should be initialized in MainController constructor or early init
			if(controller.sevaListener != null) { //
				controller.sevaListener.setupSevaCheckboxes(); // Call setup AFTER listener is ready
			} else {
				System.err.println("Error in Tables.setupTableView: SevaListener is null!");
			}
			controller.raashiComboBox.setItems(rashis);
			controller.otherServicesComboBox.setItems(otherSevaReciepts);
			controller.donationComboBox.setItems(donations);


		}
	}
	public void donationListener(){
		//Donation checkbox listener to put it inside the table view
		controller.donationCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
			controller.donationField.setDisable(!newVal);
			controller.donationComboBox.setDisable(!newVal);
			controller.addDonationButton.setDisable(!newVal);
		});

		// Add Donation button handler
		controller.addDonationButton.setOnAction(e -> controller.donation.handleAddDonation());

	}
}
