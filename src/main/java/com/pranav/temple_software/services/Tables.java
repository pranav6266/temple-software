package com.pranav.temple_software.services;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.SevaEntry;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class Tables {
	private final MainController controller;
	public Tables(MainController controller) {
		this.controller = controller;
	}

	public void setupTableView() {
		controller.slNoColumn.setCellFactory(_ -> new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? null : String.valueOf(getIndex() + 1));
				setAlignment(Pos.CENTER);
			}
		});
		controller.sevaNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		setupAmountColumn();
		setupQuantityColumn();
		setupTotalAmountColumn();
		setupPrintStatusColumn();
		setupActionColumn();

		// Add listener for status label updates
		controller.selectedSevas.addListener((ListChangeListener<SevaEntry>) change -> {
			while (change.next()) {
				if (change.wasAdded() || change.wasRemoved()) {
					controller.updatePrintStatusLabel();
				}
			}
		});
	}

	private void setupPrintStatusColumn() {
		controller.statusColumn.setCellValueFactory(cellData -> cellData.getValue().printStatusProperty());
		controller.statusColumn.setCellFactory(_ -> new TableCell<>() {
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
							setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
							break;
						case FAILED:
							setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
							break;
						case PRINTING:
							setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
							break;
						default:
							setStyle("-fx-text-fill: #7f8c8d;");
					}
				}
				setAlignment(Pos.CENTER);
			}
		});
		controller.statusColumn.setCellValueFactory(cellData -> {
			cellData.getValue().printStatusProperty().addListener((
					_, _, _) -> Platform.runLater(controller::updatePrintStatusLabel));
			return cellData.getValue().printStatusProperty();
		});
	}

	private void setupAmountColumn() {
		controller.amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
		controller.amountColumn.setCellFactory(_ -> new TableCell<>() {
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

	private void setupQuantityColumn() {
		controller.quantityColumn.setCellValueFactory(cellData ->
				cellData.getValue().quantityProperty().asObject()
		);

		controller.quantityColumn.setCellFactory(_ -> new TableCell<>() {
			private final Spinner<Integer> spinner = new Spinner<>(1, Integer.MAX_VALUE, 1);

			{
				spinner.setEditable(true);
				spinner.focusedProperty().addListener((_, _, newF) -> {
					if (!newF) commitEdit(spinner.getValue());
				});
				spinner.valueProperty().addListener((_, _, newVal) -> {
					if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
						SevaEntry entry = getTableView().getItems().get(getIndex());
						entry.setQuantity(newVal);
						entry.setPrintStatus(SevaEntry.PrintStatus.PENDING);
						controller.updatePrintStatusLabel();
					}
				});
			}

			@Override
			protected void updateItem(Integer qty, boolean empty) {
				super.updateItem(qty, empty);
				if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
					setGraphic(null);
				} else {
					spinner.getValueFactory().setValue(qty);
					setGraphic(spinner);
				}
				setAlignment(Pos.CENTER);
			}
		});
		controller.sevaTableView.setEditable(true);
	}

	private void setupTotalAmountColumn() {
		controller.totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
		controller.totalAmountColumn.setCellFactory(_ -> new TableCell<>() {
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

	private void setupActionColumn() {
		controller.actionColumn.setCellFactory(_ -> new TableCell<>() {
			private final Button removeButton = new Button("Remove");
			private final Button retryButton = new Button("Retry");
			private final HBox buttonBox = new HBox(5);

			{
				removeButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 8px;");
				retryButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 8px;");
				removeButton.setOnAction(_ -> {
					SevaEntry entry = getTableView().getItems().get(getIndex());
					controller.selectedSevas.remove(entry);
					controller.updatePrintStatusLabel();
				});
				retryButton.setOnAction(_ -> {
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
					boolean showRetry = entry.getPrintStatus() == SevaEntry.PrintStatus.FAILED;
					retryButton.setVisible(showRetry);
					retryButton.setManaged(showRetry);
					setGraphic(buttonBox);
				}
			}
		});
	}
}