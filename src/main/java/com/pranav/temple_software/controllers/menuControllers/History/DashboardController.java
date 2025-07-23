package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.DashboardStats;
import com.pranav.temple_software.repositories.DashboardRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

	@FXML private ComboBox<String> typeComboBox;
	@FXML private ComboBox<String> itemComboBox;
	@FXML private DatePicker fromDatePicker;
	@FXML private DatePicker toDatePicker;
	@FXML private ComboBox<String> monthComboBox;
	@FXML private ComboBox<String> yearComboBox;
	@FXML private ComboBox<String> paymentModeComboBox;

	@FXML private Button clearFiltersButton;
	@FXML private TableView<DashboardStats> dashboardTable;
	@FXML private TableColumn<DashboardStats, String> itemNameColumn;
	@FXML private TableColumn<DashboardStats, String> itemTypeColumn;
	@FXML private TableColumn<DashboardStats, Integer> totalCountColumn;
	@FXML private TableColumn<DashboardStats, Integer> cashCountColumn;
	@FXML private TableColumn<DashboardStats, Integer> onlineCountColumn;
	@FXML private TableColumn<DashboardStats, Double> totalAmountColumn;

	// FXML fields for summary labels
	@FXML private Label totalRecordsLabel;
	@FXML private Label totalAmountLabel;

	private final DashboardRepository dashboardRepository = new DashboardRepository();

	@FXML
	public void initialize() {
		setupComboBoxes();
		setupTableColumns();
		setupEventHandlers();
		generateReport();
	}

	@FXML
	public void openFilterWindow() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/History/FilterWindow.fxml"));
			Stage filterStage = new Stage();
			filterStage.setTitle("ಫಿಲ್ಟರ್ ಆಯ್ಕೆಗಳು");
			filterStage.initModality(Modality.WINDOW_MODAL);
			filterStage.initOwner(dashboardTable.getScene().getWindow());
			filterStage.setResizable(false);

			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(getClass().getResource("/css/modern-dashboard.css").toExternalForm());
			filterStage.setScene(scene);

			FilterPopupController filterController = loader.getController();
			filterController.initializeWithCurrentFilters(
					typeComboBox.getValue(),
					itemComboBox.getValue(),
					fromDatePicker.getValue(),
					toDatePicker.getValue(),
					monthComboBox.getValue(),
					yearComboBox.getValue(),
					paymentModeComboBox.getValue()
			);

			filterController.setFilterApplyHandler(() -> {
				typeComboBox.setValue(filterController.getTypeValue());
				itemComboBox.setValue(filterController.getItemValue());
				fromDatePicker.setValue(filterController.getFromDateValue());
				toDatePicker.setValue(filterController.getToDateValue());
				monthComboBox.setValue(filterController.getMonthValue());
				yearComboBox.setValue(filterController.getYearValue());
				paymentModeComboBox.setValue(filterController.getPaymentModeValue());
				generateReport();
				filterStage.close();
			});

			filterStage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();
			showAlert("Error", "Failed to open filter window: " + e.getMessage());
		}
	}

	public void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void setupComboBoxes() {
		typeComboBox.setItems(FXCollections.observableArrayList("ಎಲ್ಲಾ", "ಸೇವೆ", "ಇತರೆ ಸೇವೆ", "ದೇಣಿಗೆ"));
		typeComboBox.setValue("ಎಲ್ಲಾ");
		paymentModeComboBox.setItems(FXCollections.observableArrayList("All", "Cash", "Online"));
		paymentModeComboBox.setValue("All");
		monthComboBox.setItems(FXCollections.observableArrayList("All", "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"));
		monthComboBox.setValue("All");
		List<String> years = new ArrayList<>();
		years.add("");
		int currentYear = LocalDate.now().getYear();
		for (int y = currentYear; y >= 2000; y--) {
			years.add(String.valueOf(y));
		}
		yearComboBox.setItems(FXCollections.observableArrayList(years));
		yearComboBox.setValue("");
		itemComboBox.setItems(FXCollections.observableArrayList("ಎಲ್ಲಾ"));
		itemComboBox.setValue("ಎಲ್ಲಾ");
	}

	private void setupTableColumns() {
		itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
		itemTypeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
				switch (cellData.getValue().getItemType()) {
					case "SEVA" -> "ಸೇವೆ";
					case "OTHER_SEVA" -> "ಇತರೆ ಸೇವೆ";
					case "DONATION" -> "ದೇಣಿಗೆ";
					default -> cellData.getValue().getItemType();
				}
		));
		totalCountColumn.setCellValueFactory(new PropertyValueFactory<>("totalCount"));
		cashCountColumn.setCellValueFactory(new PropertyValueFactory<>("cashCount"));
		onlineCountColumn.setCellValueFactory(new PropertyValueFactory<>("onlineCount"));
		totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

		for (TableColumn<DashboardStats, ?> col : List.of(totalCountColumn, cashCountColumn, onlineCountColumn, totalAmountColumn)) {
			col.setCellFactory(column -> new TableCell() {
				@Override
				protected void updateItem(Object item, boolean empty) {
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
					} else {
						if (item instanceof Double) {
							setText(String.format("₹%.2f", (Double) item));
						} else {
							setText(item.toString());
						}
						setAlignment(Pos.CENTER);
					}
				}
			});
		}
	}

	private void setupEventHandlers() {
		typeComboBox.setOnAction(e -> updateItemComboBox());
		clearFiltersButton.setOnAction(e -> clearAllFilters());
	}

	private void updateItemComboBox() {
		String selectedType = typeComboBox.getValue();
		List<String> items = new ArrayList<>();
		items.add("ಎಲ್ಲಾ");

		switch (selectedType) {
			case "ಸೇವೆ" -> items.addAll(dashboardRepository.getAllSevaNames().stream().map(s -> s.split(":")[1]).collect(Collectors.toList()));
			case "ಇತರೆ ಸೇವೆ" -> items.addAll(dashboardRepository.getAllOtherSevaNames().stream().map(s -> s.split(":")[1]).collect(Collectors.toList()));
			case "ದೇಣಿಗೆ" -> items.addAll(dashboardRepository.getAllDonationNames().stream().map(s -> s.split(":")[1]).collect(Collectors.toList()));
		}

		itemComboBox.setItems(FXCollections.observableArrayList(items));
		itemComboBox.setValue("ಎಲ್ಲಾ");
	}

	@FXML
	private void generateReport() {
		String selectedType = typeComboBox.getValue();
		String selectedItem = itemComboBox.getValue();
		LocalDate fromDate = fromDatePicker.getValue();
		LocalDate toDate = toDatePicker.getValue();
		String selectedMonth = monthComboBox.getValue();
		String selectedYear = yearComboBox.getValue();
		String paymentMode = paymentModeComboBox.getValue();

		if (fromDate == null && selectedMonth != null && !selectedMonth.equals("All")) {
			int year = (selectedYear != null && !selectedYear.isEmpty()) ? Integer.parseInt(selectedYear) : LocalDate.now().getYear();
			int monthValue = getMonthValue(selectedMonth);
			fromDate = LocalDate.of(year, monthValue, 1);
			toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
		} else if (fromDate == null && selectedYear != null && !selectedYear.isEmpty()) {
			int year = Integer.parseInt(selectedYear);
			fromDate = LocalDate.of(year, 1, 1);
			toDate = LocalDate.of(year, 12, 31);
		}

		List<DashboardStats> allStats = new ArrayList<>();
		if ("ಎಲ್ಲಾ".equals(selectedType) || "ಸೇವೆ".equals(selectedType)) {
			allStats.addAll(dashboardRepository.getSevaStatistics(fromDate, toDate, paymentMode, getIdFromName(selectedItem, dashboardRepository.getAllSevaNames())));
		}
		if ("ಎಲ್ಲಾ".equals(selectedType) || "ಇತರೆ ಸೇವೆ".equals(selectedType)) {
			allStats.addAll(dashboardRepository.getOtherSevaStatistics(fromDate, toDate, paymentMode, getIdFromName(selectedItem, dashboardRepository.getAllOtherSevaNames())));
		}
		if ("ಎಲ್ಲಾ".equals(selectedType) || "ದೇಣಿಗೆ".equals(selectedType)) {
			allStats.addAll(dashboardRepository.getDonationStatistics(fromDate, toDate, paymentMode, getIdFromName(selectedItem, dashboardRepository.getAllDonationNames())));
		}

		dashboardTable.setItems(FXCollections.observableArrayList(allStats));
		updateSummaryLabels(allStats);
	}

	private void updateSummaryLabels(List<DashboardStats> stats) {
		int totalRecords = stats.stream().mapToInt(DashboardStats::getTotalCount).sum();
		double totalAmount = stats.stream().mapToDouble(DashboardStats::getTotalAmount).sum();

		totalRecordsLabel.setText("ಒಟ್ಟು ದಾಖಲೆಗಳು: " + totalRecords);
		totalAmountLabel.setText("ಒಟ್ಟು ಮೊತ್ತ: ₹" + String.format("%.2f", totalAmount));
	}

	private String getIdFromName(String itemName, List<String> fullList) {
		if (itemName == null || "ಎಲ್ಲಾ".equals(itemName)) return null;
		return fullList.stream()
				.filter(item -> item.split(":")[1].equals(itemName))
				.map(item -> item.split(":")[0])
				.findFirst().orElse(null);
	}

	private int getMonthValue(String monthName) {
		return switch (monthName) {
			case "JANUARY" -> 1; case "FEBRUARY" -> 2; case "MARCH" -> 3;
			case "APRIL" -> 4; case "MAY" -> 5; case "JUNE" -> 6;
			case "JULY" -> 7; case "AUGUST" -> 8; case "SEPTEMBER" -> 9;
			case "OCTOBER" -> 10; case "NOVEMBER" -> 11; case "DECEMBER" -> 12;
			default -> 1;
		};
	}

	@FXML
	private void clearAllFilters() {
		typeComboBox.setValue("ಎಲ್ಲಾ");
		itemComboBox.setValue("ಎಲ್ಲಾ");
		fromDatePicker.setValue(null);
		toDatePicker.setValue(null);
		monthComboBox.setValue("All");
		yearComboBox.setValue("");
		paymentModeComboBox.setValue("All");
		generateReport();
	}

	public void closeWindow() {
		Stage stage = (Stage) dashboardTable.getScene().getWindow();
		if (stage != null) stage.close();
	}
}
