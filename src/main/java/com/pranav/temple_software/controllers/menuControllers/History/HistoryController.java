package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.*;
import com.pranav.temple_software.repositories.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HistoryController {
	private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);

	public Label totalRecordsLabel;
	private enum HistoryView {
		SEVA("ಸೇವಾ ರಶೀದಿ ಇತಿಹಾಸ"),
		DONATION("ದೇಣಿಗೆ ರಶೀದಿ ಇತಿಹಾಸ"),
		IN_KIND("ವಸ್ತು ದೇಣಿಗೆ ಇತಿಹಾಸ"),
		SHASHWATHA_POOJA("ಶಾಶ್ವತ ಪೂಜೆ ಇತಿಹಾಸ"),
		KARYAKRAMA("ಕಾರ್ಯಕ್ರಮ ರಶೀದಿ ಇತಿಹಾಸ");
		private final String displayName;
		HistoryView(String displayName) { this.displayName = displayName; }
		@Override
		public String toString() { return displayName;
		}
	}

	private HistoryView currentView = HistoryView.SEVA;
	// Repositories
	private final SevaReceiptRepository sevaReceiptRepository = new SevaReceiptRepository();
	private final DonationReceiptRepository donationReceiptRepository = new DonationReceiptRepository();
	private final InKindDonationRepository inKindDonationRepository = new InKindDonationRepository();
	private final ShashwathaPoojaRepository shashwathaPoojaRepository = new ShashwathaPoojaRepository();
	private final KaryakramaReceiptRepository karyakramaReceiptRepository = new KaryakramaReceiptRepository();

	// FXML elements
	@FXML private ComboBox<HistoryView> viewSelectionComboBox;
	@FXML private Label currentViewLabel;
	@FXML private ProgressIndicator progressIndicator;

	// Seva Table
	@FXML private TableView<SevaReceiptData> historyTable;
	@FXML private TableColumn<SevaReceiptData, Integer> receiptIdColumn;
	@FXML private TableColumn<SevaReceiptData, String> devoteeNameColumn;
	@FXML private TableColumn<SevaReceiptData, String> sevaDateColumn;
	@FXML private TableColumn<SevaReceiptData, String> sevaColumn;
	@FXML private TableColumn<SevaReceiptData, String> visheshaPoojeColumn;
	@FXML private TableColumn<SevaReceiptData, Double> totalAmountColumn;
	@FXML private TableColumn<SevaReceiptData, Void> detailsColumn;

	// Donation Table
	@FXML private TableView<DonationReceiptData> donationHistoryTable;
	@FXML private TableColumn<DonationReceiptData, Integer> donationReceiptIdColumn;
	@FXML private TableColumn<DonationReceiptData, String> donationDevoteeNameColumn;
	@FXML private TableColumn<DonationReceiptData, String> donationDateColumn;
	@FXML private TableColumn<DonationReceiptData, String> donationNameColumn;
	@FXML private TableColumn<DonationReceiptData, Double> donationAmountColumn;
	@FXML private TableColumn<DonationReceiptData, Void> donationDetailsColumn;

	// In-Kind Donation Table
	@FXML private TableView<InKindDonation> inKindDonationHistoryTable;
	@FXML private TableColumn<InKindDonation, Integer> inKindReceiptIdColumn;
	@FXML private TableColumn<InKindDonation, String> inKindDevoteeNameColumn;
	@FXML private TableColumn<InKindDonation, String> inKindDonationDateColumn;
	@FXML private TableColumn<InKindDonation, String> itemDescriptionColumn;
	@FXML private TableColumn<InKindDonation, Void> inKindDetailsColumn;
	// Shashwatha Pooja Table
	@FXML private TableView<ShashwathaPoojaReceipt> shashwathaPoojaHistoryTable;
	@FXML private TableColumn<ShashwathaPoojaReceipt, Integer> shashwathaReceiptIdColumn;
	@FXML private TableColumn<ShashwathaPoojaReceipt, String> shashwathaDevoteeNameColumn;
	@FXML private TableColumn<ShashwathaPoojaReceipt, String> shashwathaReceiptDateColumn;
	@FXML private TableColumn<ShashwathaPoojaReceipt, String> shashwathaPoojaDateColumn;
	@FXML private TableColumn<ShashwathaPoojaReceipt, Void> shashwathaDetailsColumn;
	// Karyakrama Table
	@FXML private TableView<KaryakramaReceiptData> karyakramaHistoryTable;
	@FXML private TableColumn<KaryakramaReceiptData, Integer> karyakramaReceiptIdColumn;
	@FXML private TableColumn<KaryakramaReceiptData, String> karyakramaDevoteeNameColumn;
	@FXML private TableColumn<KaryakramaReceiptData, String> karyakramaNameColumn;
	@FXML private TableColumn<KaryakramaReceiptData, String> karyakramaReceiptDateColumn;
	@FXML private TableColumn<KaryakramaReceiptData, Double> karyakramaTotalAmountColumn;
	@FXML private TableColumn<KaryakramaReceiptData, Void> karyakramaDetailsColumn;
	@FXML
	public void initialize() {
		setupSevaTableColumns();
		setupDonationTableColumns();
		setupInKindDonationTableColumns();
		setupShashwathaPoojaTableColumns();
		setupKaryakramaTableColumns();
		setupViewSelectionComboBox();
		switchToView(HistoryView.SEVA);
	}

	private <T> void loadDataInBackground(Supplier<List<T>> dataSupplier, Consumer<List<T>> successConsumer) {
		progressIndicator.setVisible(true);
		setTableVisibility(false, false, false, false, false);
		updateRecordCount(0);

		Task<List<T>> loadTask = new Task<>() {
			@Override
			protected List<T> call() {
				return dataSupplier.get();
			}
		};

		loadTask.setOnSucceeded(_ -> {
			successConsumer.accept(loadTask.getValue());
			progressIndicator.setVisible(false);
		});
		loadTask.setOnFailed(_ -> {
			progressIndicator.setVisible(false);
			showAlert("Failed to load history data from the database.");
			logger.error("Failed to load history data from the database", loadTask.getException());
		});

		new Thread(loadTask).start();
	}

	private void setupViewSelectionComboBox() {
		viewSelectionComboBox.setItems(FXCollections.observableArrayList(HistoryView.values()));
		viewSelectionComboBox.setValue(HistoryView.SEVA);
		viewSelectionComboBox.setOnAction(_ -> {
			HistoryView selectedView = viewSelectionComboBox.getValue();
			if (selectedView != null && selectedView != currentView) {
				switchToView(selectedView);
			}
		});
	}

	private void switchToView(HistoryView view) {
		currentView = view;
		switch (view) {
			case SEVA -> switchToSevaView();
			case DONATION -> switchToDonationView();
			case IN_KIND -> switchToInKindDonationView();
			case SHASHWATHA_POOJA -> switchToShashwathaPoojaView();
			case KARYAKRAMA -> switchToKaryakramaView();
		}
		currentViewLabel.setText(view.displayName);
		if (viewSelectionComboBox.getValue() != view) {
			viewSelectionComboBox.setValue(view);
		}
	}

	private void switchToSevaView() {
		loadDataInBackground(
				sevaReceiptRepository::getAllReceipts,
				receipts -> {
					setTableVisibility(true, false, false, false, false);
					historyTable.setItems(FXCollections.observableArrayList(receipts));
					updateRecordCount(receipts.size());
				}
		);
	}

	private void switchToDonationView() {
		loadDataInBackground(
				donationReceiptRepository::getAllDonationReceipts,
				receipts -> {
					setTableVisibility(false, true, false, false, false);
					donationHistoryTable.setItems(FXCollections.observableArrayList(receipts));
					updateRecordCount(receipts.size());
				}
		);
	}

	private void switchToInKindDonationView() {
		loadDataInBackground(
				inKindDonationRepository::getAllInKindDonations,
				receipts -> {
					setTableVisibility(false, false, true, false, false);
					inKindDonationHistoryTable.setItems(FXCollections.observableArrayList(receipts));
					updateRecordCount(receipts.size());
				}
		);
	}

	private void switchToShashwathaPoojaView() {
		loadDataInBackground(
				shashwathaPoojaRepository::getAllShashwathaPoojaReceipts,
				receipts -> {
					setTableVisibility(false, false, false, true, false);
					shashwathaPoojaHistoryTable.setItems(FXCollections.observableArrayList(receipts));
					updateRecordCount(receipts.size());
				}
		);
	}

	private void switchToKaryakramaView() {
		loadDataInBackground(
				karyakramaReceiptRepository::getAllReceipts,
				receipts -> {
					setTableVisibility(false, false, false, false, true);
					karyakramaHistoryTable.setItems(FXCollections.observableArrayList(receipts));
					updateRecordCount(receipts.size());
				}
		);
	}

	private void setTableVisibility(boolean seva, boolean donation, boolean inKind, boolean shashwatha, boolean karyakrama) {
		historyTable.setVisible(seva);
		donationHistoryTable.setVisible(donation);
		inKindDonationHistoryTable.setVisible(inKind);
		shashwathaPoojaHistoryTable.setVisible(shashwatha);
		karyakramaHistoryTable.setVisible(karyakrama);
	}

	private void setupKaryakramaTableColumns() {
		karyakramaReceiptIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getReceiptId()).asObject());
		karyakramaDevoteeNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDevoteeName()));
		karyakramaNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKaryakramaName()));
		karyakramaReceiptDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedReceiptDate()));
		karyakramaTotalAmountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());
		karyakramaDetailsColumn.setCellFactory(_ -> createDetailsButtonCell(this::showKaryakramaDetails));
	}

	private void showKaryakramaDetails(KaryakramaReceiptData receiptData) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/History/KaryakramaDetailsView.fxml"));
			Stage detailsStage = new Stage();
			detailsStage.setTitle("ಕಾರ್ಯಕ್ರಮ ವಿವರಗಳು");
			detailsStage.initModality(Modality.WINDOW_MODAL);
			detailsStage.initOwner(karyakramaHistoryTable.getScene().getWindow());
			Scene scene = new Scene(loader.load());
			detailsStage.setScene(scene);
			KaryakramaDetailsController detailsController = loader.getController();
			detailsController.initializeDetails(receiptData);
			detailsStage.setMaxHeight(650);
			detailsStage.showAndWait();
		} catch (IOException e) {
			logger.error("Could not load Karyakrama details view", e);
			showAlert("Could not load Karyakrama details view.");
		}
	}

	private void updateRecordCount(int count) {
		if (totalRecordsLabel != null) {
			totalRecordsLabel.setText("ಒಟ್ಟು ದಾಖಲೆಗಳು: " + count);
		}
	}

	private void setupShashwathaPoojaTableColumns() {
		shashwathaReceiptIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getReceiptId()).asObject());
		shashwathaDevoteeNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDevoteeName()));
		shashwathaReceiptDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedReceiptDate()));
		shashwathaPoojaDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPoojaDate()));
		shashwathaDetailsColumn.setCellFactory(_ -> createDetailsButtonCell(this::showShashwathaPoojaDetails));
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
			logger.error("Could not load ShashwathaPooja details view",e);
			showAlert("Could not load Shashwatha Pooja details view.");
		}
	}

	private void setupSevaTableColumns() {
		receiptIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getReceiptId()).asObject());
		devoteeNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDevoteeName()));
		sevaDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
		totalAmountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());
		setupSevaDetailsColumn();
		setVisheshaPoojeColumn();
		setSevaColumn();
	}

	private void setupDonationTableColumns() {
		donationReceiptIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getDonationReceiptId()).asObject());
		donationDevoteeNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDevoteeName()));
		donationDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
		donationAmountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getDonationAmount()).asObject());
		donationNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDonationName()));
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
		detailsColumn.setCellFactory(_ -> createDetailsButtonCell(this::showSevaReceiptDetails));
	}

	private void setupDonationDetailsColumn() {
		donationDetailsColumn.setCellFactory(_ -> createDetailsButtonCell(this::showDonationDetails));
	}

	private void setupInKindDonationDetailsColumn() {
		inKindDetailsColumn.setCellFactory(_ -> createDetailsButtonCell(this::showInKindDonationDetails));
	}

	private <T> TableCell<T, Void> createDetailsButtonCell(Consumer<T> action) {
		return new TableCell<>() {
			private final Button viewButton = new Button("ವಿವರ ನೋಡಿ");
			{
				viewButton.setOnAction(_ -> {
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
			SevaReceiptDetailsController detailsController = loader.getController();
			detailsController.initializeDetails(sevaReceiptData);
			detailsStage.setMaxHeight(650);
			detailsStage.showAndWait();
		} catch (IOException e) {
			logger.error("Could not load receipt details view", e);
			showAlert("Could not load receipt details view.");
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
			logger.error("Could not load donation details view", e);
			showAlert("Could not load donation details view.");
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
			logger.error("Couldn't load In-Kind Donation view",e);
			showAlert("Could not load in-kind donation details view.");
		}
	}

	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.initOwner(historyTable.getScene().getWindow());
		alert.setTitle("Error");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void setVisheshaPoojeColumn() {
		visheshaPoojeColumn.setCellValueFactory(cellData -> {
			List<String> validPoojas = VisheshaPoojeRepository.getAllVisheshaPooje().stream().map(SevaEntry::getName).toList();
			double poojaAmount = cellData.getValue().getSevas().stream().filter(entry -> validPoojas.contains(entry.getName())).mapToDouble(SevaEntry::getTotalAmount).sum();
			return new SimpleStringProperty(poojaAmount > 0 ? String.format("₹%.2f", poojaAmount) : "---");
		});
	}

	private void setSevaColumn() {
		sevaColumn.setCellValueFactory(cellData -> {
			List<String> nonSevaTypes = VisheshaPoojeRepository.getAllVisheshaPooje().stream().map(SevaEntry::getName).toList();
			double sevaAmount = cellData.getValue().getSevas().stream()
					.filter(entry -> entry.getName() != null && !nonSevaTypes.contains(entry.getName()))
					.mapToDouble(SevaEntry::getTotalAmount).sum();
			return new SimpleStringProperty(sevaAmount > 0 ? String.format("₹%.2f", sevaAmount) : "N/A");
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
			logger.error("Failed to load dashboard view", e);
			showAlert("Failed to load dashboard view: " + e.getMessage());
		}
	}

	// --- NEW AND MODIFIED METHODS FOR EXCEL EXPORT ---

	@FXML
	private void handleExportToExcel() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save History to Excel");
		fileChooser.setInitialFileName("Temple_History_Export.xlsx");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
		File file = fileChooser.showSaveDialog(historyTable.getScene().getWindow());

		if (file != null) {
			try (Workbook workbook = new XSSFWorkbook()) {
				// **Create a cell style for text wrapping**
				CellStyle wrapStyle = workbook.createCellStyle();
				wrapStyle.setWrapText(true);

				// Pass the style to the sheet creation methods
				createSevaSheet(workbook, sevaReceiptRepository.getAllReceipts(), wrapStyle);
				createDonationSheet(workbook, donationReceiptRepository.getAllDonationReceipts());
				createInKindSheet(workbook, inKindDonationRepository.getAllInKindDonations());
				createShashwathaSheet(workbook, shashwathaPoojaRepository.getAllShashwathaPoojaReceipts());
				createKaryakramaSheet(workbook, karyakramaReceiptRepository.getAllReceipts(), wrapStyle);

				try (FileOutputStream fileOut = new FileOutputStream(file)) {
					workbook.write(fileOut);
				}

				Alert alert = new Alert(Alert.AlertType.INFORMATION, "History exported successfully to " + file.getName());
				alert.initOwner(historyTable.getScene().getWindow());
				alert.showAndWait();

			} catch (IOException e) {
				logger.error("Failed to write the Excel file", e);
				showAlert("Failed to write the Excel file: " + e.getMessage());
			}
		}
	}

	private void createHeaderRow(Sheet sheet, String[] headers) {
		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < headers.length; i++) {
			headerRow.createCell(i).setCellValue(headers[i]);
		}
	}

	private void createSevaSheet(Workbook workbook, List<SevaReceiptData> data, CellStyle wrapStyle) {
		Sheet sheet = workbook.createSheet("Seva Receipts");
		String[] headers = {"ರಶೀದಿ ಸಂಖ್ಯೆ", "ಭಕ್ತರ ಹೆಸರು", "ಸಂಪರ್ಕ ಸಂಖ್ಯೆ", "ವಿಳಾಸ", "PAN", "ಜನ್ಮ ರಾಶಿ", "ಜನ್ಮ ನಕ್ಷತ್ರ", "ಸೇವಾ ದಿನಾಂಕ", "ಪಾವತಿ ವಿಧಾನ", "ಸೇವಾ ವಿವರಗಳು", "ರಶೀದಿ ಒಟ್ಟು ಮೊತ್ತ"};
		createHeaderRow(sheet, headers);

		int rowNum = 1;
		for (SevaReceiptData receipt : data) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(receipt.getReceiptId());
			row.createCell(1).setCellValue(receipt.getDevoteeName());
			row.createCell(2).setCellValue(receipt.getPhoneNumber());
			row.createCell(3).setCellValue(receipt.getAddress());
			row.createCell(4).setCellValue(receipt.getPanNumber());
			row.createCell(5).setCellValue(receipt.getRashi());
			row.createCell(6).setCellValue(receipt.getNakshatra());
			row.createCell(7).setCellValue(receipt.getFormattedDate());
			row.createCell(8).setCellValue(receipt.getPaymentMode());

			StringBuilder sevasText = new StringBuilder();
			int sevaIndex = 1;
			for (SevaEntry seva : receipt.getSevas()) {
				if (sevaIndex > 1) {
					sevasText.append("\n"); // Use newline character
				}
				sevasText.append(String.format("%d. %s (%d x %.2f)",
						sevaIndex++,
						seva.getName(),
						seva.getQuantity(),
						seva.getAmount()));
			}
			// Create the cell, set its value, and apply the wrap style
			org.apache.poi.ss.usermodel.Cell sevaCell = row.createCell(9);
			sevaCell.setCellValue(sevasText.toString());
			sevaCell.setCellStyle(wrapStyle);

			row.createCell(10).setCellValue(receipt.getTotalAmount());
		}
	}

	private void createDonationSheet(Workbook workbook, List<DonationReceiptData> data) {
		Sheet sheet = workbook.createSheet("Donation Receipts");
		String[] headers = {"ರಶೀದಿ ಸಂಖ್ಯೆ", "ಭಕ್ತರ ಹೆಸರು", "ಸಂಪರ್ಕ ಸಂಖ್ಯೆ", "ವಿಳಾಸ", "PAN", "ಜನ್ಮ ರಾಶಿ", "ಜನ್ಮ ನಕ್ಷತ್ರ", "ದೇಣಿಗೆ ದಿನಾಂಕ", "ಪಾವತಿ ವಿಧಾನ", "ದೇಣಿಗೆ ವಿಧ", "ದೇಣಿಗೆ ಮೊತ್ತ"};
		createHeaderRow(sheet, headers);

		int rowNum = 1;
		for (DonationReceiptData receipt : data) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(receipt.getDonationReceiptId());
			row.createCell(1).setCellValue(receipt.getDevoteeName());
			row.createCell(2).setCellValue(receipt.getPhoneNumber());
			row.createCell(3).setCellValue(receipt.getAddress());
			row.createCell(4).setCellValue(receipt.getPanNumber());
			row.createCell(5).setCellValue(receipt.getRashi());
			row.createCell(6).setCellValue(receipt.getNakshatra());
			row.createCell(7).setCellValue(receipt.getFormattedDate());
			row.createCell(8).setCellValue(receipt.getPaymentMode());
			row.createCell(9).setCellValue(receipt.getDonationName());
			row.createCell(10).setCellValue(receipt.getDonationAmount());
		}
	}

	private void createInKindSheet(Workbook workbook, List<InKindDonation> data) {
		Sheet sheet = workbook.createSheet("In-Kind Donations");
		String[] headers = {"ರಶೀದಿ ಸಂಖ್ಯೆ", "ಭಕ್ತರ ಹೆಸರು", "ಸಂಪರ್ಕ ಸಂಖ್ಯೆ", "ವಿಳಾಸ", "PAN", "ಜನ್ಮ ರಾಶಿ", "ಜನ್ಮ ನಕ್ಷತ್ರ", "ದೇಣಿಗೆ ದಿನಾಂಕ", "ವಸ್ತುವಿನ ವಿವರ"};
		createHeaderRow(sheet, headers);

		int rowNum = 1;
		for (InKindDonation receipt : data) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(receipt.getInKindReceiptId());
			row.createCell(1).setCellValue(receipt.getDevoteeName());
			row.createCell(2).setCellValue(receipt.getPhoneNumber());
			row.createCell(3).setCellValue(receipt.getAddress());
			row.createCell(4).setCellValue(receipt.getPanNumber());
			row.createCell(5).setCellValue(receipt.getRashi());
			row.createCell(6).setCellValue(receipt.getNakshatra());
			row.createCell(7).setCellValue(receipt.getFormattedDate());
			row.createCell(8).setCellValue(receipt.getItemDescription());
		}
	}

	private void createShashwathaSheet(Workbook workbook, List<ShashwathaPoojaReceipt> data) {
		Sheet sheet = workbook.createSheet("Shashwatha Pooja");
		String[] headers = {"ರಶೀದಿ ಸಂಖ್ಯೆ", "ಭಕ್ತರ ಹೆಸರು", "ಸಂಪರ್ಕ ಸಂಖ್ಯೆ", "ವಿಳಾಸ", "PAN", "ಜನ್ಮ ರಾಶಿ", "ಜನ್ಮ ನಕ್ಷತ್ರ", "ರಶೀದಿ ದಿನಾಂಕ", "ಪಾವತಿ ವಿಧಾನ", "ಪೂಜಾ ದಿನಾಂಕ/ವಿವರ", "ಪೂಜಾ ಮೊತ್ತ"};
		createHeaderRow(sheet, headers);

		int rowNum = 1;
		for (ShashwathaPoojaReceipt receipt : data) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(receipt.getReceiptId());
			row.createCell(1).setCellValue(receipt.getDevoteeName());
			row.createCell(2).setCellValue(receipt.getPhoneNumber());
			row.createCell(3).setCellValue(receipt.getAddress());
			row.createCell(4).setCellValue(receipt.getPanNumber());
			row.createCell(5).setCellValue(receipt.getRashi());
			row.createCell(6).setCellValue(receipt.getNakshatra());
			row.createCell(7).setCellValue(receipt.getFormattedReceiptDate());
			row.createCell(8).setCellValue(receipt.getPaymentMode());
			row.createCell(9).setCellValue(receipt.getPoojaDate());
			row.createCell(10).setCellValue(receipt.getAmount());
		}
	}

	private void createKaryakramaSheet(Workbook workbook, List<KaryakramaReceiptData> data, CellStyle wrapStyle) {
		Sheet sheet = workbook.createSheet("Karyakrama Receipts");
		String[] headers = {"ರಶೀದಿ ಸಂಖ್ಯೆ", "ಭಕ್ತರ ಹೆಸರು", "ಸಂಪರ್ಕ ಸಂಖ್ಯೆ", "ವಿಳಾಸ", "PAN", "ಕಾರ್ಯಕ್ರಮ", "ರಶೀದಿ ದಿನಾಂಕ", "ಪಾವತಿ ವಿಧಾನ", "ಇತರೆ ವಿವರಗಳು", "ರಶೀದಿ ಒಟ್ಟು ಮೊತ್ತ"};
		createHeaderRow(sheet, headers);

		int rowNum = 1;
		for (KaryakramaReceiptData receipt : data) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(receipt.getReceiptId());
			row.createCell(1).setCellValue(receipt.getDevoteeName());
			row.createCell(2).setCellValue(receipt.getPhoneNumber());
			row.createCell(3).setCellValue(receipt.getAddress());
			row.createCell(4).setCellValue(receipt.getPanNumber());
			row.createCell(5).setCellValue(receipt.getKaryakramaName());
			row.createCell(6).setCellValue(receipt.getFormattedReceiptDate());
			row.createCell(7).setCellValue(receipt.getPaymentMode());

			StringBuilder itemsText = new StringBuilder();
			int itemIndex = 1;
			for (SevaEntry item : receipt.getSevas()) {
				if (itemIndex > 1) {
					itemsText.append("\n");
				}
				itemsText.append(String.format("%d. %s (%.2f)",
						itemIndex++,
						item.getName(),
						item.getAmount()));
			}

			org.apache.poi.ss.usermodel.Cell itemsCell = row.createCell(8);
			itemsCell.setCellValue(itemsText.toString());
			itemsCell.setCellStyle(wrapStyle);

			row.createCell(9).setCellValue(receipt.getTotalAmount());
		}
	}
}