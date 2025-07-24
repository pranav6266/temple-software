package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.DonationReceiptData;
import com.pranav.temple_software.models.ReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.DonationReceiptRepository;
import com.pranav.temple_software.repositories.OtherSevaRepository;
import com.pranav.temple_software.repositories.ReceiptRepository;
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
import java.time.LocalDate;
import java.util.List;

public class HistoryController {
	@FXML public TableColumn<ReceiptData, String> paymentMode;
	@FXML public Button dashboardButton;
	@FXML private TableView<ReceiptData> historyTable;
	@FXML public TableColumn<ReceiptData, String> otherSevaColumn;
	@FXML public TableColumn<ReceiptData, String> sevaColumn;
	@FXML public TableColumn<ReceiptData, Double> totalAmountColumn;
	public TableColumn<ReceiptData, Void> detailsColumn;

	@FXML private TableColumn<ReceiptData, Integer> receiptIdColumn;
	@FXML private TableColumn<ReceiptData, String> devoteeNameColumn;
	@FXML private TableColumn<ReceiptData, String> sevaDateColumn;

	@FXML private Button toggleViewButton;
	@FXML private Label currentViewLabel;
	@FXML public TableView<DonationReceiptData> donationHistoryTable;
	@FXML public TableColumn<DonationReceiptData, Integer> donationReceiptIdColumn;
	@FXML public TableColumn<DonationReceiptData, String> donationDevoteeNameColumn;
	@FXML public TableColumn<DonationReceiptData, String> donationDateColumn;
	@FXML public TableColumn<DonationReceiptData, String> donationNameColumn;
	@FXML public TableColumn<DonationReceiptData, Double> donationAmountColumn;
	@FXML public TableColumn<DonationReceiptData, String> donationPaymentModeColumn;
	@FXML public TableColumn<DonationReceiptData, Void> donationDetailsColumn;
	private boolean isShowingDonations = false;

	private final ReceiptRepository receiptRepository = new ReceiptRepository();
	private final DonationReceiptRepository donationReceiptRepository = new DonationReceiptRepository();
	private LocalDate savedDate = null;
	private String savedMonth = "All";
	private String savedYear = "";
	private boolean savedOnline = false;
	private boolean savedOffline = false;

	@FXML
	public void initialize() {
		loadHistory();
		historyTable.setVisible(true);
		donationHistoryTable.setVisible(false);
		toggleViewButton.setText("ದೇಣಿಗೆ ರಶೀದಿಗಳನ್ನು ನೋಡಿ");
		currentViewLabel.setText("ಸೇವಾ ರಶೀದಿ ಇತಿಹಾಸ");

		setupDonationTableColumns();
		receiptIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getReceiptId()).asObject());
		devoteeNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDevoteeName()));
		sevaDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
		totalAmountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());

		setupDetailsColumn();

		setOtherSevaColumn();
		setSevaColumn();
		setPaymentModeColumn();
	}

	@FXML
	public void handleToggleView() {
		isShowingDonations = !isShowingDonations;

		if (isShowingDonations) {
			historyTable.setVisible(false);
			donationHistoryTable.setVisible(true);
			List<DonationReceiptData> allDonations = donationReceiptRepository.getAllDonationReceipts();
			donationHistoryTable.setItems(FXCollections.observableArrayList(allDonations));
			toggleViewButton.setText("ಸೇವಾ ರಶೀದಿಗಳನ್ನು ನೋಡಿ");
			currentViewLabel.setText("ದೇಣಿಗೆ ರಶೀದಿ ಇತಿಹಾಸ");
		} else {
			donationHistoryTable.setVisible(false);
			historyTable.setVisible(true);
			loadHistory();
			toggleViewButton.setText("ದೇಣಿಗೆ ರಶೀದಿಗಳನ್ನು ನೋಡಿ");
			currentViewLabel.setText("ಸೇವಾ ರಶೀದಿ ಇತಿಹಾಸ");
		}
	}

	private void setupDetailsColumn() {
		detailsColumn.setCellFactory(param -> new TableCell<>() {
			private final Button viewButton = new Button("ವಿವರ ನೋಡಿ");
			{
				viewButton.setOnAction(event -> {
					ReceiptData selectedReceipt = getTableView().getItems().get(getIndex());
					showReceiptDetails(selectedReceipt);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				setGraphic(empty ? null : viewButton);
				setAlignment(Pos.CENTER);
			}
		});
	}

	private void setupDonationDetailsColumn() {
		donationDetailsColumn.setCellFactory(param -> new TableCell<>() {
			private final Button viewButton = new Button("ವಿವರ ನೋಡಿ");
			{
				viewButton.setOnAction(event -> {
					DonationReceiptData selectedDonation = getTableView().getItems().get(getIndex());
					showDonationDetails(selectedDonation);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				setGraphic(empty ? null : viewButton);
				setAlignment(Pos.CENTER);
			}
		});
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

	private void setupDonationTableColumns() {
		if (donationHistoryTable != null) {
			donationReceiptIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getDonationReceiptId()).asObject());
			donationDevoteeNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDevoteeName()));
			donationDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
			donationAmountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getDonationAmount()).asObject());
			donationNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDonationName()));
			donationPaymentModeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentMode()));
			setupDonationDetailsColumn();
		}
	}

	private void showReceiptDetails(ReceiptData receiptData) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/History/SevaReceiptDetailsView.fxml"));
			Stage detailsStage = new Stage();
			detailsStage.setTitle("ರಶೀದಿ ವಿವರಗಳು");
			detailsStage.initModality(Modality.WINDOW_MODAL);
			detailsStage.initOwner(historyTable.getScene().getWindow());
			Scene scene = new Scene(loader.load());
			detailsStage.setScene(scene);
			ReceiptDetailsController detailsController = loader.getController();
			detailsController.initializeDetails(receiptData);
			detailsStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
			showAlert("Error", "Could not load receipt details view.");
		}
	}

	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}



	/**
	 * *** BUG FIX ***
	 * Corrected logic to sum the totalAmount of "other seva" entries.
	 */
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

	/**
	 * *** BUG FIX ***
	 * Corrected logic to sum the totalAmount of regular seva entries, excluding donations and other sevas.
	 */
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

	private void loadHistory() {
		historyTable.setItems(FXCollections.observableArrayList(receiptRepository.getAllReceipts()));
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
