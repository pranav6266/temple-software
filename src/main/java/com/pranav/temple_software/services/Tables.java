package com.pranav.temple_software.services;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.SevaEntry;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.util.converter.IntegerStringConverter;

public class Tables {
	private final MainController controller;

	public Tables(MainController controller) {
		this.controller = controller;
	}

	public void setupTableView() {
		// Serial number column
		controller.slNoColumn.setCellFactory(col -> new TableCell<SevaEntry, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? null : String.valueOf(getIndex() + 1));
				setAlignment(Pos.CENTER);
			}
		});

		// Seva name column
		controller.sevaNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

		// Setup amount column
		setupAmountColumn();

		// Setup quantity column
		setupQuantityColumn();

		// Setup total amount column
		setupTotalAmountColumn();

		// Add Print Status Column - this is the key addition
		setupPrintStatusColumn();

		// Setup action column with print controls
		setupActionColumn();

		// Setup donation-related functionality
		donationListener();
	}

	private void setupPrintStatusColumn() {
		// Create the print status column
		TableColumn<SevaEntry, SevaEntry.PrintStatus> statusColumn = new TableColumn<>("Print Status");
		statusColumn.setPrefWidth(120);
		statusColumn.setCellValueFactory(cellData -> cellData.getValue().printStatusProperty());

		statusColumn.setCellFactory(column -> new TableCell<SevaEntry, SevaEntry.PrintStatus>() {
			@Override
			protected void updateItem(SevaEntry.PrintStatus status, boolean empty) {
				super.updateItem(status, empty);
				if (empty || status == null) {
					setText(null);
					setStyle("");
				} else {
					setText(status.getDisplayText());
					switch (status) {
						case SUCCESS:
							setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
							break;
						case FAILED:
							setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
							break;
						case PRINTING:
							setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
							break;
						default:
							setStyle("-fx-text-fill: gray;");
					}
				}
				setAlignment(Pos.CENTER);
			}
		});

		// Insert status column at the appropriate position
		// Find the position after total amount column
		int insertPosition = -1;
		for (int i = 0; i < controller.sevaTableView.getColumns().size(); i++) {
			TableColumn<?, ?> column = controller.sevaTableView.getColumns().get(i);
			if ("ಒಟ್ಟು ಮೊತ್ತ ".equals(column.getText()) || "Total Amount".equals(column.getText())) {
				insertPosition = i + 1;
				break;
			}
		}

		if (insertPosition > 0 && insertPosition <= controller.sevaTableView.getColumns().size()) {
			controller.sevaTableView.getColumns().add(insertPosition, statusColumn);
		} else {
			// Fallback: add at the end before action column
			controller.sevaTableView.getColumns().add(controller.sevaTableView.getColumns().size() - 1, statusColumn);
		}
	}

	private void setupAmountColumn() {
		// Find the amount column
		TableColumn<SevaEntry, Number> amountColumn = null;
		for (TableColumn<SevaEntry, ?> column : controller.sevaTableView.getColumns()) {
			if ("ಮೊತ್ತ ".equals(column.getText()) || "Amount".equals(column.getText())) {
				amountColumn = (TableColumn<SevaEntry, Number>) column;
				break;
			}
		}

		if (amountColumn != null) {
			amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
			amountColumn.setCellFactory(column -> new TableCell<SevaEntry, Number>() {
				@Override
				protected void updateItem(Number amount, boolean empty) {
					super.updateItem(amount, empty);
					if (empty || amount == null) {
						setText(null);
					} else {
						setText(String.format("₹%.2f", amount.doubleValue()));
					}
					setAlignment(Pos.CENTER_RIGHT);
				}
			});
		}
	}

	private void setupQuantityColumn() {
		// Find the quantity column
		TableColumn<SevaEntry, Integer> quantityColumn = null;
		for (TableColumn<SevaEntry, ?> column : controller.sevaTableView.getColumns()) {
			if ("ಪ್ರಮಾಣ ".equals(column.getText()) || "Quantity".equals(column.getText())) {
				quantityColumn = (TableColumn<SevaEntry, Integer>) column;
				break;
			}
		}

		if (quantityColumn != null) {
			quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
			quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
			quantityColumn.setOnEditCommit(event -> {
				SevaEntry seva = event.getRowValue();
				int newQuantity = event.getNewValue();
				if (newQuantity > 0) {
					seva.setQuantity(newQuantity);
					// Reset print status when quantity changes
					seva.setPrintStatus(SevaEntry.PrintStatus.PENDING);
					controller.updatePrintStatusLabel();
				} else {
					controller.showAlert("Invalid Quantity", "Quantity must be greater than 0");
					event.consume();
				}
			});
		}
	}

	private void setupTotalAmountColumn() {
		// Find the total amount column
		TableColumn<SevaEntry, Number> totalColumn = null;
		for (TableColumn<SevaEntry, ?> column : controller.sevaTableView.getColumns()) {
			if ("ಒಟ್ಟು ಮೊತ್ತ ".equals(column.getText()) || "Total Amount".equals(column.getText())) {
				totalColumn = (TableColumn<SevaEntry, Number>) column;
				break;
			}
		}

		if (totalColumn != null) {
			totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
			totalColumn.setCellFactory(column -> new TableCell<SevaEntry, Number>() {
				@Override
				protected void updateItem(Number totalAmount, boolean empty) {
					super.updateItem(totalAmount, empty);
					if (empty || totalAmount == null) {
						setText(null);
					} else {
						setText(String.format("₹%.2f", totalAmount.doubleValue()));
					}
					setAlignment(Pos.CENTER_RIGHT);
				}
			});
		}
	}

	private void setupActionColumn() {
		// Find or create the action column
		TableColumn<SevaEntry, Void> actionColumn = null;
		for (TableColumn<SevaEntry, ?> column : controller.sevaTableView.getColumns()) {
			if ("Action".equals(column.getText()) || "Actions".equals(column.getText()) ||
					"ಕ್ರಿಯೆಗಳು".equals(column.getText())) {
				actionColumn = (TableColumn<SevaEntry, Void>) column;
				break;
			}
		}

		if (actionColumn == null) {
			actionColumn = new TableColumn<>("Actions");
			controller.sevaTableView.getColumns().add(actionColumn);
		}

		actionColumn.setPrefWidth(120);
		actionColumn.setCellFactory(col -> new TableCell<SevaEntry, Void>() {
			private final Button removeButton = new Button("Remove");
			private final Button retryButton = new Button("Retry");
			private final HBox buttonBox = new HBox(5);

			{
				removeButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 8px;");
				retryButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 8px;");

				removeButton.setOnAction(event -> {
					SevaEntry entry = getTableView().getItems().get(getIndex());
					controller.selectedSevas.remove(entry);
					controller.updatePrintStatusLabel();
				});

				retryButton.setOnAction(event -> {
					SevaEntry entry = getTableView().getItems().get(getIndex());
					if (entry.getPrintStatus() == SevaEntry.PrintStatus.FAILED) {
						controller.receiptServices.retryIndividualItem(entry);
					}
				});

				buttonBox.getChildren().addAll(removeButton, retryButton);
				buttonBox.setAlignment(Pos.CENTER);
				buttonBox.setSpacing(3);
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					SevaEntry entry = getTableView().getItems().get(getIndex());
					// Show retry button only for failed items
					boolean showRetry = entry.getPrintStatus() == SevaEntry.PrintStatus.FAILED;
					retryButton.setVisible(showRetry);
					retryButton.setManaged(showRetry);
					setGraphic(buttonBox);
				}
			}
		});
	}

	public void donationListener() {
		controller.donationCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				controller.donationField.setDisable(false);
				controller.donationComboBox.setDisable(false);
				controller.addDonationButton.setDisable(false);
			} else {
				controller.donationField.setDisable(true);
				controller.donationComboBox.setDisable(true);
				controller.addDonationButton.setDisable(true);
				controller.donationField.clear();
				controller.donationComboBox.getSelectionModel().selectFirst();
			}
		});

		controller.addDonationButton.setOnAction(e -> {
			String selectedDonation = controller.donationComboBox.getValue();
			String donationAmountText = controller.donationField.getText();

			if (selectedDonation == null || selectedDonation.equals("ಆಯ್ಕೆ") ||
					donationAmountText == null || donationAmountText.trim().isEmpty()) {
				controller.showAlert("Incomplete Input", "Please select a donation type and enter an amount.");
				return;
			}

			try {
				double donationAmount = Double.parseDouble(donationAmountText);
				if (donationAmount <= 0) {
					controller.showAlert("Invalid Amount", "Donation amount must be greater than 0.");
					return;
				}

				String donationName = "ದೇಣಿಗೆ : " + selectedDonation;

				// Check if this donation already exists
				boolean exists = controller.selectedSevas.stream()
						.anyMatch(seva -> seva.getName().equals(donationName));

				if (exists) {
					controller.showAlert("Duplicate Entry", "This donation is already added.");
					return;
				}

				SevaEntry donationEntry = new SevaEntry(donationName, donationAmount);
				donationEntry.setPrintStatus(SevaEntry.PrintStatus.PENDING);
				controller.selectedSevas.add(donationEntry);
				controller.updatePrintStatusLabel();

				// Clear the fields
				controller.donationField.clear();
				controller.donationComboBox.getSelectionModel().selectFirst();

			} catch (NumberFormatException ex) {
				controller.showAlert("Invalid Input", "Please enter a valid donation amount.");
			}
		});

		// Listen for changes to update print status label
		controller.selectedSevas.addListener((ListChangeListener<SevaEntry>) change -> {
			while (change.next()) {
				if (change.wasAdded() || change.wasRemoved()) {
					controller.updatePrintStatusLabel();
				}
			}
		});
	}
}
