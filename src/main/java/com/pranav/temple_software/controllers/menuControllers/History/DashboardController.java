// FILE: src/main/java/com/pranav/temple_software/controllers/menuControllers/History/DashboardController.java
		package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.DashboardStats;
import com.pranav.temple_software.repositories.DashboardRepository;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DashboardController {
	private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

	@FXML private TableView<DashboardStats> dashboardTable;
	@FXML private TableColumn<DashboardStats, String> itemNameColumn;
	@FXML private TableColumn<DashboardStats, String> itemTypeColumn;
	@FXML private TableColumn<DashboardStats, Integer> totalCountColumn;
	@FXML private TableColumn<DashboardStats, Integer> cashCountColumn;
	@FXML private TableColumn<DashboardStats, Integer> onlineCountColumn;
	@FXML private TableColumn<DashboardStats, Double> totalAmountColumn;
	@FXML private Label totalRecordsLabel;
	@FXML private Label totalAmountLabel;
	@FXML private ProgressIndicator progressIndicator;

	private String typeValue;
	private String itemValue;
	private LocalDate fromDateValue;
	private LocalDate toDateValue;
	private String monthValue;
	private String yearValue;
	private String paymentModeValue;

	private final DashboardRepository dashboardRepository = new DashboardRepository();
	private static final Map<String, Integer> KANNADA_MONTHS_MAP = Map.ofEntries(
			Map.entry("ಜನವರಿ", 1), Map.entry("ಫೆಬ್ರುವರಿ", 2), Map.entry("ಮಾರ್ಚ್", 3),
			Map.entry("ಏಪ್ರಿಲ್", 4), Map.entry("ಮೇ", 5), Map.entry("ಜೂನ್", 6),
			Map.entry("ಜುಲೈ", 7), Map.entry("ಆಗಸ್ಟ್", 8), Map.entry("ಸೆಪ್ಟೆಂಬರ್", 9),
			Map.entry("ಅಕ್ಟೋಬರ್", 10), Map.entry("ನವೆಂಬರ್", 11), Map.entry("ಡಿಸೆಂಬರ್", 12)
	);

	@FXML
	public void initialize() {
		clearAllFiltersAndRunReport();
		setupTableColumns();
		generateReport();
	}

	@FXML
	private void generateReport() {
		progressIndicator.setVisible(true);
		dashboardTable.setDisable(true);
		dashboardTable.setItems(FXCollections.observableArrayList());

		Task<List<DashboardStats>> loadReportTask = new Task<>() {
			@Override
			protected List<DashboardStats> call() {
				List<DashboardStats> allStats = new ArrayList<>();
				if ("ಎಲ್ಲಾ".equals(typeValue) || "ಸೇವೆ".equals(typeValue)) {
					allStats.addAll(dashboardRepository.getSevaStatistics(fromDateValue, toDateValue, paymentModeValue, getIdFromName(itemValue, dashboardRepository.getAllSevaNames())));
				}
				if ("ಎಲ್ಲಾ".equals(typeValue) || "ದೇಣಿಗೆ".equals(typeValue)) {
					allStats.addAll(dashboardRepository.getDonationStatistics(fromDateValue, toDateValue, paymentModeValue, getIdFromName(itemValue, dashboardRepository.getAllDonationNames())));
				}
				if ("ಎಲ್ಲಾ".equals(typeValue) || "ಶಾಶ್ವತ ಪೂಜೆ".equals(typeValue)) {
					allStats.addAll(dashboardRepository.getShashwathaPoojaStatistics(fromDateValue, toDateValue));
				}
				if ("ಎಲ್ಲಾ".equals(typeValue) || "ವಿಶೇಷ ಪೂಜೆ".equals(typeValue)) {
					allStats.addAll(dashboardRepository.getVisheshaPoojaStatistics(fromDateValue, toDateValue, paymentModeValue, getIdFromName(itemValue, dashboardRepository.getAllVisheshaPoojaNames())));
				}
				if ("ಎಲ್ಲಾ".equals(typeValue) || "ಕಾರ್ಯಕ್ರಮ".equals(typeValue)) {
					allStats.addAll(dashboardRepository.getKaryakramaStatistics(fromDateValue, toDateValue, paymentModeValue, getIdFromName(itemValue, dashboardRepository.getAllKaryakramaNames())));
				}
				return allStats;
			}
		};

		loadReportTask.setOnSucceeded(_ -> {
			List<DashboardStats> result = loadReportTask.getValue();
			dashboardTable.setItems(FXCollections.observableArrayList(result));
			updateSummaryLabels(result);
			progressIndicator.setVisible(false);
			dashboardTable.setDisable(false);
		});
		loadReportTask.setOnFailed(_ -> {
			progressIndicator.setVisible(false);
			dashboardTable.setDisable(false);
			showAlert("Error", "Failed to load dashboard report.");
			logger.error("Failed to load dashboard report", loadReportTask.getException());
		});

		new Thread(loadReportTask).start();
	}

	@FXML
	public void openFilterWindow() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/DashboardView/FilterWindow.fxml"));
			Stage filterStage = new Stage();
			filterStage.setTitle("ಫಿಲ್ಟರ್ ಆಯ್ಕೆಗಳು");
			filterStage.initModality(Modality.WINDOW_MODAL);
			filterStage.initOwner(dashboardTable.getScene().getWindow());
			filterStage.setResizable(false);
			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/modern-dashboard.css")).toExternalForm());
			filterStage.setScene(scene);
			FilterPopupController filterController = loader.getController();
			filterController.initializeWithCurrentFilters(
					typeValue, itemValue, fromDateValue, toDateValue,
					monthValue, yearValue, paymentModeValue
			);
			filterController.setFilterApplyHandler(() -> {
				this.typeValue = filterController.getTypeValue();
				this.itemValue = filterController.getItemValue();
				this.paymentModeValue = filterController.getPaymentModeValue();
				this.monthValue = filterController.getMonthValue();
				this.yearValue = filterController.getYearValue();

				// --- MODIFIED LOGIC START ---
				// Prioritize month/year selection over the single date picker
				this.fromDateValue = null;
				this.toDateValue = null;

				if (monthValue != null && !monthValue.equals("All")) {
					int monthNum = KANNADA_MONTHS_MAP.getOrDefault(monthValue, -1);
					if (monthNum != -1) {
						int year = (yearValue != null && !yearValue.isEmpty()) ? Integer.parseInt(yearValue) : LocalDate.now().getYear();
						this.fromDateValue = LocalDate.of(year, monthNum, 1);
						this.toDateValue = this.fromDateValue.withDayOfMonth(this.fromDateValue.lengthOfMonth());
					}
				} else if (yearValue != null && !yearValue.isEmpty()) {
					int year = Integer.parseInt(yearValue);
					this.fromDateValue = LocalDate.of(year, Month.JANUARY, 1);
					this.toDateValue = LocalDate.of(year, Month.DECEMBER, 31);
				} else {
					// Fallback to the single date picker if no month/year is selected
					this.fromDateValue = filterController.getFromDateValue();
					this.toDateValue = filterController.getFromDateValue(); // Filter for a single day
				}
				// --- MODIFIED LOGIC END ---

				generateReport();
				filterStage.close();
			});


			filterStage.showAndWait();
		} catch (IOException e) {
			logger.error("Failed to open filter window", e);
			showAlert("Error", "Failed to open filter window: " + e.getMessage());
		}
	}

	public void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		if (dashboardTable != null && dashboardTable.getScene() != null) {
			alert.initOwner(dashboardTable.getScene().getWindow());
		}
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void setupTableColumns() {
		itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
		itemTypeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
				switch (cellData.getValue().getItemType()) {
					case "SEVA" -> "ಸೇವೆ";
					case "DONATION" -> "ದೇಣಿಗೆ";
					case "VISHESHA_POOJA" -> "ವಿಶೇಷ ಪೂಜೆ";
					case "SHASHWATHA_POOJA" -> "ಶಾಶ್ವತ ಪೂಜೆ";
					case "KARYAKRAMA" -> "ಕಾರ್ಯಕ್ರಮ";
					default -> cellData.getValue().getItemType();
				}
		));
		totalCountColumn.setCellValueFactory(new PropertyValueFactory<>("totalCount"));
		cashCountColumn.setCellValueFactory(new PropertyValueFactory<>("cashCount"));
		onlineCountColumn.setCellValueFactory(new PropertyValueFactory<>("onlineCount"));
		totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

		totalCountColumn.setCellFactory(this::createNumericCell);
		cashCountColumn.setCellFactory(this::createNumericCell);
		onlineCountColumn.setCellFactory(this::createNumericCell);
		totalAmountColumn.setCellFactory(this::createNumericCell);
	}

	private <T extends Number> TableCell<DashboardStats, T> createNumericCell(TableColumn<DashboardStats, T> column) {
		return new TableCell<>() {
			@Override
			protected void updateItem(T item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					if (item instanceof Double) {
						setText(String.format("₹%.2f", item.doubleValue()));
					} else {
						setText(item.toString());
					}
					setAlignment(Pos.CENTER);
				}
			}
		};
	}

	private void updateSummaryLabels(List<DashboardStats> stats) {
		int totalRecords = stats.stream().mapToInt(DashboardStats::getTotalCount).sum();
		double totalAmount = stats.stream().mapToDouble(DashboardStats::getTotalAmount).sum();
		totalRecordsLabel.setText("📋 ಒಟ್ಟು ದಾಖಲೆಗಳು: " + totalRecords);
		totalAmountLabel.setText("💰 ಒಟ್ಟು ಮೊತ್ತ: ₹" + String.format("%.2f", totalAmount));
	}

	private String getIdFromName(String itemName, List<String> fullList) {
		if (itemName == null || "ಎಲ್ಲಾ".equals(itemName)) return null;
		return fullList.stream()
				.filter(item -> item.split(":")[1].equals(itemName))
				.map(item -> item.split(":")[0])
				.findFirst().orElse(null);
	}

	private void clearAllFiltersAndRunReport() {
		typeValue = "ಎಲ್ಲಾ";
		itemValue = "ಎಲ್ಲಾ";
		fromDateValue = null;
		toDateValue = null;
		monthValue = "All";
		yearValue = "";
		paymentModeValue = "All";
	}

	@FXML
	private void handleExportToExcel() {
		List<DashboardStats> data = dashboardTable.getItems();
		if (data.isEmpty()) {
			showAlert("No Data", "There is no data to export.");
			return;
		}
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Excel File");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
		File file = fileChooser.showSaveDialog(dashboardTable.getScene().getWindow());
		if (file != null) {
			try (Workbook workbook = new XSSFWorkbook()) {
				Sheet sheet = workbook.createSheet("Dashboard Report");
				Row headerRow = sheet.createRow(0);
				String[] headers = {"Item Name", "Item Type", "Total Count", "Cash Count", "Online Count", "Total Amount"};
				for (int i = 0; i < headers.length; i++) {
					headerRow.createCell(i).setCellValue(headers[i]);
				}
				int rowNum = 1;
				for (DashboardStats stats : data) {
					Row row = sheet.createRow(rowNum++);
					row.createCell(0).setCellValue(stats.getItemName());
					row.createCell(1).setCellValue(stats.getItemType());
					row.createCell(2).setCellValue(stats.getTotalCount());
					row.createCell(3).setCellValue(stats.getCashCount());
					row.createCell(4).setCellValue(stats.getOnlineCount());
					row.createCell(5).setCellValue(stats.getTotalAmount());
				}
				try (FileOutputStream fileOut = new FileOutputStream(file)) {
					workbook.write(fileOut);
				}
				showAlert("Success", "Data exported successfully to " + file.getName());
			} catch (IOException e) {
				showAlert("Error", "Failed to write the Excel file: " + e.getMessage());
				logger.error("Failed to write Excel file", e);
			}
		}
	}

	public void closeWindow() {
		Stage stage = (Stage) dashboardTable.getScene().getWindow();
		if (stage != null) stage.close();
	}
}

