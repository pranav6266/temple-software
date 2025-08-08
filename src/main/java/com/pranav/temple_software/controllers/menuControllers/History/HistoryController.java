// FILE: src/main/java/com/pranav/temple_software/controllers/menuControllers/History/HistoryController.java
package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.*;
import com.pranav.temple_software.repositories.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class HistoryController {
	public Label totalRecordsLabel;

	// Update the enum to include all 4 types
	private enum HistoryView {
		SEVA("ಸೇವಾ ರಶೀದಿ ಇತಿಹಾಸ"),
		DONATION("ದೇಣಿಗೆ ರಶೀದಿ ಇತಿಹಾಸ"),
		IN_KIND("ವಸ್ತು ದೇಣಿಗೆ ಇತಿಹಾಸ"),
		SHASHWATHA_POOJA("ಶಾಶ್ವತ ಪೂಜೆ ಇತಿಹಾಸ");

		private final String displayName;

		HistoryView(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	private HistoryView currentView = HistoryView.SEVA;

	// Repositories
	private final SevaReceiptRepository sevaReceiptRepository = new SevaReceiptRepository();
	private final DonationReceiptRepository donationReceiptRepository = new DonationReceiptRepository();
	private final InKindDonationRepository inKindDonationRepository = new InKindDonationRepository();
	private final ShashwathaPoojaRepository shashwathaPoojaRepository = new ShashwathaPoojaRepository();

	// FXML elements for Seva Table
	@FXML private TableView<SevaReceiptData> historyTable;
	@FXML private TableColumn<SevaReceiptData, Integer> receiptIdColumn;
	@FXML private TableColumn<SevaReceiptData, String> devoteeNameColumn;
	@FXML private TableColumn<SevaReceiptData, String> sevaDateColumn;
	@FXML private TableColumn<SevaReceiptData, String> sevaColumn;
	@FXML private TableColumn<SevaReceiptData, String> otherSevaColumn;
	@FXML private TableColumn<SevaReceiptData, String> visheshaPoojeColumn;
	@FXML private TableColumn<SevaReceiptData, Double> totalAmountColumn;
	@FXML private TableColumn<SevaReceiptData, String> paymentMode;
	@FXML private TableColumn<SevaReceiptData, Void> detailsColumn;

	// FXML elements for Donation Table
	@FXML private TableView<DonationReceiptData> donationHistoryTable;
	@FXML private TableColumn<DonationReceiptData, Integer> donationReceiptIdColumn;
	@FXML private TableColumn<DonationReceiptData, String> donationDevoteeNameColumn;
	@FXML private TableColumn<DonationReceiptData, String> donationDateColumn;
	@FXML private TableColumn<DonationReceiptData, String> donationNameColumn;
	@FXML private TableColumn<DonationReceiptData, Double> donationAmountColumn;
	@FXML private TableColumn<DonationReceiptData, String> donationPaymentModeColumn;
	@FXML private TableColumn<DonationReceiptData, Void> donationDetailsColumn;

	// FXML elements for In-Kind Donation Table
	@FXML private TableView<InKindDonation> inKindDonationHistoryTable;
	@FXML private TableColumn<InKindDonation, Integer> inKindReceiptIdColumn;
	@FXML private TableColumn<InKindDonation, String> inKindDevoteeNameColumn;
	@FXML private TableColumn<InKindDonation, String> inKindDonationDateColumn;
	@FXML private TableColumn<InKindDonation, String> itemDescriptionColumn;
	@FXML private TableColumn<InKindDonation, Void> inKindDetailsColumn;

	@FXML private TableView<ShashwathaPoojaReceipt> shashwathaPoojaHistoryTable;
	@FXML private TableColumn<ShashwathaPoojaReceipt, Integer> shashwathaReceiptIdColumn;
	@FXML private TableColumn<ShashwathaPoojaReceipt, String> shashwathaDevoteeNameColumn;
	@FXML private TableColumn<ShashwathaPoojaReceipt, String> shashwathaReceiptDateColumn;
	@FXML private TableColumn<ShashwathaPoojaReceipt, String> shashwathaPoojaDateColumn;
	@FXML private TableColumn<ShashwathaPoojaReceipt, Void> shashwathaDetailsColumn;

	// General FXML elements - REPLACE BUTTON WITH COMBOBOX
	@FXML private ComboBox<HistoryView> viewSelectionComboBox;
	@FXML private Label currentViewLabel;
	@FXML private Button dashboardButton;

	@FXML
	public void initialize() {
		// Setup all tables
		setupSevaTableColumns();
		setupDonationTableColumns();
		setupInKindDonationTableColumns();
		setupShashwathaPoojaTableColumns();

		// Setup the ComboBox with all view options
		setupViewSelectionComboBox();

		// Set the initial view to Seva receipts
		switchToView(HistoryView.SEVA);
	}

	private void setupViewSelectionComboBox() {
		// Populate ComboBox with all view options
		viewSelectionComboBox.setItems(FXCollections.observableArrayList(HistoryView.values()));

		// Set initial selection
		viewSelectionComboBox.setValue(HistoryView.SEVA);

		// Add listener for selection changes
		viewSelectionComboBox.setOnAction(event -> {
			HistoryView selectedView = viewSelectionComboBox.getValue();
			if (selectedView != null && selectedView != currentView) {
				switchToView(selectedView);
			}
		});
	}

	// REMOVE the old handleToggleView method and replace with this:
	private void switchToView(HistoryView view) {
		currentView = view;

		switch (view) {
			case SEVA -> switchToSevaView();
			case DONATION -> switchToDonationView();
			case IN_KIND -> switchToInKindDonationView();
			case SHASHWATHA_POOJA -> switchToShashwathaPoojaView();
		}

		// Update the current view label
		currentViewLabel.setText(view.displayName);

		// Ensure ComboBox reflects current selection
		if (viewSelectionComboBox.getValue() != view) {
			viewSelectionComboBox.setValue(view);
		}
	}

	private void switchToSevaView() {
		setTableVisibility(true, false, false, false);
		List<SevaReceiptData> sevaList = sevaReceiptRepository.getAllReceipts();
		historyTable.setItems(FXCollections.observableArrayList(sevaList));
		updateRecordCount(sevaList.size());
	}

	private void switchToDonationView() {
		setTableVisibility(false, true, false, false);
		List<DonationReceiptData> donationList = donationReceiptRepository.getAllDonationReceipts();
		donationHistoryTable.setItems(FXCollections.observableArrayList(donationList));
		updateRecordCount(donationList.size());
	}

	private void switchToInKindDonationView() {
		setTableVisibility(false, false, true, false);
		List<InKindDonation> inKindList = inKindDonationRepository.getAllInKindDonations();
		inKindDonationHistoryTable.setItems(FXCollections.observableArrayList(inKindList));
		updateRecordCount(inKindList.size());
	}

	private void switchToShashwathaPoojaView() {
		setTableVisibility(false, false, false, true);
		List<ShashwathaPoojaReceipt> shashwathaList = shashwathaPoojaRepository.getAllShashwathaPoojaReceipts();
		shashwathaPoojaHistoryTable.setItems(FXCollections.observableArrayList(shashwathaList));
		updateRecordCount(shashwathaList.size());
	}

	private void setTableVisibility(boolean seva, boolean donation, boolean inKind, boolean shashwatha) {
		historyTable.setVisible(seva);
		donationHistoryTable.setVisible(donation);
		inKindDonationHistoryTable.setVisible(inKind);
		shashwathaPoojaHistoryTable.setVisible(shashwatha);
	}

	private void updateRecordCount(int count) {
		if (totalRecordsLabel != null) {
			totalRecordsLabel.setText("ಒಟ್ಟು ದಾಖಲೆಗಳು: " + count);
		}
	}

	// Continue with the rest of your existing methods...
	// (Keep all the existing setup methods and detail display methods)

	private void setupShashwathaPoojaTableColumns() {
		shashwathaReceiptIdColumn.setCellValueFactory(cellData ->
				new SimpleIntegerProperty(cellData.getValue().getReceiptId()).asObject());
		shashwathaDevoteeNameColumn.setCellValueFactory(cellData ->
				new SimpleStringProperty(cellData.getValue().getDevoteeName()));
		shashwathaReceiptDateColumn.setCellValueFactory(cellData ->
				new SimpleStringProperty(cellData.getValue().getFormattedReceiptDate()));
		shashwathaPoojaDateColumn.setCellValueFactory(cellData ->
				new SimpleStringProperty(cellData.getValue().getPoojaDate()));
		setupShashwathaPoojaDetailsColumn();
	}

	private void setupShashwathaPoojaDetailsColumn() {
		shashwathaDetailsColumn.setCellFactory(param -> createDetailsButtonCell(this::showShashwathaPoojaDetails));
	}

	private void showShashwathaPoojaDetails(ShashwathaPoojaReceipt poojaData) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/History/ShashwathaPoojaDetailsView.fxml"));
			Stage detailsStage = new Stage();
			detailsStage.setTitle("ಶಾಶ್ವತ ಪೂಜೆ ವಿವರಗಳು");
			detailsStage.initModality(Modality.WINDOW_MODAL);
			detailsStage.initOwner(shashwathaPoojaHistoryTable.getScene().getWindow());
			Scene scene = new Scene(loader.load());
			detailsStage.setScene(scene);
			ShashwathaPoojaDetailsController detailsController = loader.getController();
			detailsController.initializeDetails(poojaData);
			detailsStage.setMaxHeight(650);
			detailsStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
			showAlert("Error", "Could not load Shashwatha Pooja details view.");
		}
	}

	// Keep all your existing methods for table setup and detail views...
	// (I'm keeping the existing methods as they are working correctly)

	private void setupSevaTableColumns() {
		receiptIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getReceiptId()).asObject());
		devoteeNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDevoteeName()));
		sevaDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
		totalAmountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());
		setupSevaDetailsColumn();
		setOtherSevaColumn();
		setVisheshaPoojeColumn();
		setSevaColumn();
		setPaymentModeColumn();
	}

	private void setupDonationTableColumns() {
		donationReceiptIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getDonationReceiptId()).asObject());
		donationDevoteeNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDevoteeName()));
		donationDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
		donationAmountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getDonationAmount()).asObject());
		donationNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDonationName()));
		donationPaymentModeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentMode()));
		setupDonationDetailsColumn();
	}

	private void setupInKindDonationTableColumns() {
		inKindReceiptIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getInKindReceiptId()).asObject());
		inKindDevoteeNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDevoteeName()));
		inKindDonationDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
		itemDescriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemDescription()));
		setupInKindDonationDetailsColumn();
	}

	private void setupSevaDetailsColumn() {
		detailsColumn.setCellFactory(param -> createDetailsButtonCell(this::showSevaReceiptDetails));
	}

	private void setupDonationDetailsColumn() {
		donationDetailsColumn.setCellFactory(param -> createDetailsButtonCell(this::showDonationDetails));
	}

	private void setupInKindDonationDetailsColumn() {
		inKindDetailsColumn.setCellFactory(param -> createDetailsButtonCell(this::showInKindDonationDetails));
	}

	private <T> TableCell<T, Void> createDetailsButtonCell(Consumer<T> action) {
		return new TableCell<>() {
			private final Button viewButton = new Button("ವಿವರ ನೋಡಿ");
			{
				viewButton.setOnAction(event -> {
					if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
						T item = getTableView().getItems().get(getIndex());
						action.accept(item);
					}
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				setGraphic(empty ? null : viewButton);
				setAlignment(Pos.CENTER);
			}
		};
	}

	private void showSevaReceiptDetails(SevaReceiptData sevaReceiptData) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/History/SevaReceiptDetailsView.fxml"));
			Stage detailsStage = new Stage();
			detailsStage.setTitle("ರಶೀದಿ ವಿವರಗಳು");
			detailsStage.initModality(Modality.WINDOW_MODAL);
			detailsStage.initOwner(historyTable.getScene().getWindow());
			Scene scene = new Scene(loader.load());
			detailsStage.setScene(scene);
			ReceiptDetailsController detailsController = loader.getController();
			detailsController.initializeDetails(sevaReceiptData);
			detailsStage.setMaxHeight(650);
			detailsStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
			showAlert("Error", "Could not load receipt details view.");
		}
	}

	private void showDonationDetails(DonationReceiptData donationData) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/History/DonationReceiptDetailsView.fxml"));
			Stage detailsStage = new Stage();
			detailsStage.setTitle("ದೇಣಿಗೆ ರಶೀದಿ ವಿವರಗಳು");
			detailsStage.initModality(Modality.WINDOW_MODAL);
			detailsStage.initOwner(donationHistoryTable.getScene().getWindow());
			Scene scene = new Scene(loader.load());
			detailsStage.setScene(scene);
			DonationDetailsController detailsController = loader.getController();
			detailsController.initializeDonationDetails(donationData);
			detailsStage.setMaxHeight(650);
			detailsStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
			showAlert("Error", "Could not load donation details view.");
		}
	}

	private void showInKindDonationDetails(InKindDonation donationData) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/History/InKindDonationDetailsView.fxml"));
			Stage detailsStage = new Stage();
			detailsStage.setTitle("ವಸ್ತು ದೇಣಿಗೆ ವಿವರಗಳು");
			detailsStage.initModality(Modality.WINDOW_MODAL);
			detailsStage.initOwner(inKindDonationHistoryTable.getScene().getWindow());
			Scene scene = new Scene(loader.load());
			detailsStage.setScene(scene);
			InKindDonationDetailsController detailsController = loader.getController();
			detailsController.initializeDetails(donationData);
			detailsStage.setMaxHeight(650);
			detailsStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
			showAlert("Error", "Could not load in-kind donation details view.");
		}
	}

	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void setOtherSevaColumn() {
		otherSevaColumn.setCellValueFactory(cellData -> {
			List<String> validOtherSevas = OthersRepository.getAllOthers().stream()
					.map(SevaEntry::getName)
					.toList();
			double otherSevaAmount = cellData.getValue().getSevas().stream()
					.filter(entry -> validOtherSevas.contains(entry.getName()))
					.mapToDouble(SevaEntry::getTotalAmount)
					.sum();
			return new SimpleStringProperty(otherSevaAmount > 0 ? String.format("₹%.2f", otherSevaAmount) : "N/A");
		});
	}

	private void setVisheshaPoojeColumn() {
		visheshaPoojeColumn.setCellValueFactory(cellData -> {
			List<String> validPoojas = VisheshaPoojeRepository.getAllVisheshaPooje().stream()
					.map(SevaEntry::getName)
					.toList();
			double poojaAmount = cellData.getValue().getSevas().stream()
					.filter(entry -> validPoojas.contains(entry.getName()))
					.mapToDouble(SevaEntry::getTotalAmount)
					.sum();
			return new SimpleStringProperty(poojaAmount > 0 ? String.format("₹%.2f", poojaAmount) : "---");
		});
	}

	private void setSevaColumn() {
		sevaColumn.setCellValueFactory(cellData -> {
			List<String> validOtherSevas = OthersRepository.getAllOthers().stream()
					.map(SevaEntry::getName)
					.toList();
			double sevaAmount = cellData.getValue().getSevas().stream()
					.filter(entry -> entry.getName() != null &&
							!entry.getName().startsWith("ದೇಣಿಗೆ ") &&
							!validOtherSevas.contains(entry.getName()))
					.mapToDouble(SevaEntry::getTotalAmount)
					.sum();
			return new SimpleStringProperty(sevaAmount > 0 ? String.format("₹%.2f", sevaAmount) : "N/A");
		});
	}

	private void setPaymentModeColumn() {
		paymentMode.setCellValueFactory(cellData -> {
			String mode = cellData.getValue().getPaymentMode();
			return new SimpleStringProperty(mode != null ? mode : "N/A");
		});
	}

	@FXML
	public void handleDashboardButton() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/DashboardView/Dashboard.fxml"));
			Stage dashboardStage = new Stage();
			dashboardStage.setTitle("ಸೇವಾ/ದೇಣಿಗೆ ಡ್ಯಾಶ್‌ಬೋರ್ಡ್");
			dashboardStage.setScene(new Scene(loader.load()));
			dashboardStage.initModality(Modality.WINDOW_MODAL);
			dashboardStage.initOwner(historyTable.getScene().getWindow());
			dashboardStage.setMaximized(true);
			dashboardStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			showAlert("Error", "Failed to load dashboard view: " + e.getMessage());
		}
	}
}
