// COPY AND PASTE THE ENTIRE MODIFIED FILE

package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.DonationReceiptData;
import com.pranav.temple_software.models.InKindDonation;
import com.pranav.temple_software.models.SevaReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.DonationReceiptRepository;
import com.pranav.temple_software.repositories.InKindDonationRepository;
import com.pranav.temple_software.repositories.OtherSevaRepository;
import com.pranav.temple_software.repositories.SevaReceiptRepository;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
	// Enum to manage which view is currently active
	private enum HistoryView {
		SEVA,
		DONATION,
		IN_KIND
	}

	private HistoryView currentView = HistoryView.SEVA;

	// Repositories
	private final SevaReceiptRepository sevaReceiptRepository = new SevaReceiptRepository();
	private final DonationReceiptRepository donationReceiptRepository = new DonationReceiptRepository();
	private final InKindDonationRepository inKindDonationRepository = new InKindDonationRepository();

	// FXML elements for Seva Table
	@FXML private TableView<SevaReceiptData> historyTable;
	@FXML private TableColumn<SevaReceiptData, Integer> receiptIdColumn;
	@FXML private TableColumn<SevaReceiptData, String> devoteeNameColumn;
	@FXML private TableColumn<SevaReceiptData, String> sevaDateColumn;
	@FXML private TableColumn<SevaReceiptData, String> sevaColumn;
	@FXML private TableColumn<SevaReceiptData, String> otherSevaColumn;
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

	// General FXML elements
	@FXML private Button toggleViewButton;
	@FXML private Label currentViewLabel;
	@FXML private Button dashboardButton;

	@FXML
	public void initialize() {
		// Setup all three tables
		setupSevaTableColumns();
		setupDonationTableColumns();
		setupInKindDonationTableColumns();

		// Set the initial view to Seva receipts
		switchToSevaView();
	}

	@FXML
	public void handleToggleView() {
		switch (currentView) {
			case SEVA -> switchToDonationView();
			case DONATION -> switchToInKindDonationView();
			case IN_KIND -> switchToSevaView();
		}
	}

	private void switchToSevaView() {
		currentView = HistoryView.SEVA;
		historyTable.setVisible(true);
		donationHistoryTable.setVisible(false);
		inKindDonationHistoryTable.setVisible(false);

		List<SevaReceiptData> sevaList = sevaReceiptRepository.getAllReceipts();
		historyTable.setItems(FXCollections.observableArrayList(sevaList));
		currentViewLabel.setText("ಸೇವಾ ರಶೀದಿ ಇತಿಹಾಸ");
		toggleViewButton.setText("ದೇಣಿಗೆ ರಶೀದಿಗಳನ್ನು ನೋಡಿ");
	}

	private void switchToDonationView() {
		currentView = HistoryView.DONATION;
		historyTable.setVisible(false);
		donationHistoryTable.setVisible(true);
		inKindDonationHistoryTable.setVisible(false);

		List<DonationReceiptData> donationList = donationReceiptRepository.getAllDonationReceipts();
		donationHistoryTable.setItems(FXCollections.observableArrayList(donationList));
		currentViewLabel.setText("ದೇಣಿಗೆ ರಶೀದಿ ಇತಿಹಾಸ");
		toggleViewButton.setText("ವಸ್ತು ದೇಣಿಗೆ ನೋಡಿ");
	}

	private void switchToInKindDonationView() {
		currentView = HistoryView.IN_KIND;
		historyTable.setVisible(false);
		donationHistoryTable.setVisible(false);
		inKindDonationHistoryTable.setVisible(true);

		// ============================================
		// START: CORRECTED CODE BLOCK
		// ============================================
		// Explicitly create the List first, then wrap it in an ObservableList.
		// This resolves the ClassCastException.
		List<InKindDonation> inKindList = inKindDonationRepository.getAllInKindDonations();
		inKindDonationHistoryTable.setItems(FXCollections.observableArrayList(inKindList));
		// ============================================
		// END: CORRECTED CODE BLOCK
		// ============================================

		currentViewLabel.setText("ವಸ್ತು ದೇಣಿಗೆ ಇತಿಹಾಸ");
		toggleViewButton.setText("ಸೇವಾ ರಶೀದಿಗಳನ್ನು ನೋಡಿ");
	}

	// --- Seva Table Setup ---
	private void setupSevaTableColumns() {
		receiptIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getReceiptId()).asObject());
		devoteeNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDevoteeName()));
		sevaDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
		totalAmountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());
		setupSevaDetailsColumn();
		setOtherSevaColumn();
		setSevaColumn();
		setPaymentModeColumn();
	}

	// --- Donation Table Setup ---
	private void setupDonationTableColumns() {
		donationReceiptIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getDonationReceiptId()).asObject());
		donationDevoteeNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDevoteeName()));
		donationDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
		donationAmountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getDonationAmount()).asObject());
		donationNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDonationName()));
		donationPaymentModeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentMode()));
		setupDonationDetailsColumn();
	}

	// --- In-Kind Donation Table Setup ---
	private void setupInKindDonationTableColumns() {
		inKindReceiptIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getInKindReceiptId()).asObject());
		inKindDevoteeNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDevoteeName()));
		inKindDonationDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
		itemDescriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemDescription()));
		setupInKindDonationDetailsColumn();
	}

	// --- Details Column Button Setup (Refactored for stability) ---
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

	// --- Show Details Window Logic ---
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
			detailsStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
			showAlert("Error", "Could not load in-kind donation details view.");
		}
	}

	// --- Helper Methods ---
	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void setOtherSevaColumn() {
		otherSevaColumn.setCellValueFactory(cellData -> {
			List<String> validOtherSevas = OtherSevaRepository.getAllOtherSevas().stream()
					.map(SevaEntry::getName)
					.toList();
			double otherSevaAmount = cellData.getValue().getSevas().stream()
					.filter(entry -> validOtherSevas.contains(entry.getName()))
					.mapToDouble(SevaEntry::getTotalAmount)
					.sum();
			return new SimpleStringProperty(otherSevaAmount > 0 ? String.format("₹%.2f", otherSevaAmount) : "N/A");
		});
	}

	private void setSevaColumn() {
		sevaColumn.setCellValueFactory(cellData -> {
			List<String> validOtherSevas = OtherSevaRepository.getAllOtherSevas().stream()
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