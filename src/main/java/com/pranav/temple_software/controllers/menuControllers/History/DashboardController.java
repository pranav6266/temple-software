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
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuViews/DashboardView/FilterWindow.fxml"));
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
		typeComboBox.setItems(FXCollections.observableArrayList(
				"ಎಲ್ಲಾ", "ಸೇವೆ", "ಇತರೆ ಸೇವೆ", "ದೇಣಿಗೆ", "ವಸ್ತು ದೇಣಿಗೆ", "ಶಾಶ್ವತ ಪೂಜೆ", "ವಿಶೇಷ ಪೂಜೆ"
		));
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
					case "OTHERS" -> "ಇತರೆ";
					case "DONATION" -> "ದೇಣಿಗೆ";
					case "VISHESHA_POOJA" -> "ವಿಶೇಷ ಪೂಜೆ";
					case "SHASHWATHA_POOJA" -> "ಶಾಶ್ವತ ಪೂಜೆ";
					default -> cellData.getValue().getItemType();
				}
		));
		totalCountColumn.setCellValueFactory(new PropertyValueFactory<>("totalCount"));
		cashCountColumn.setCellValueFactory(new PropertyValueFactory<>("cashCount"));
		onlineCountColumn.setCellValueFactory(new PropertyValueFactory<>("onlineCount"));
		totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

		// Use the helper method to create a type-safe cell factory for each numeric column
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
					// Check if the item is a Double to format it as currency
					if (item instanceof Double) {
						setText(String.format("₹%.2f", item.doubleValue()));
					} else {
						// Otherwise, just convert it to a string (for Integers)
						setText(item.toString());
					}
					setAlignment(Pos.CENTER);
				}
			}
		};
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
			case "ಸೇವೆ" -> items.addAll(dashboardRepository.getAllSevaNames().stream()
					.map(s -> s.split(":")[1]).collect(Collectors.toList()));
			case "ಇತರೆ ಸೇವೆ" -> items.addAll(dashboardRepository.getAllOtherSevaNames().stream()
					.map(s -> s.split(":")[1]).collect(Collectors.toList()));
			case "ದೇಣಿಗೆ" -> items.addAll(dashboardRepository.getAllDonationNames().stream()
					.map(s -> s.split(":")[1]).collect(Collectors.toList()));
			case "ವಸ್ತು ದೇಣಿಗೆ" -> items.add("ವಸ್ತು ದೇಣಿಗೆ"); // Only one type
			case "ಶಾಶ್ವತ ಪೂಜೆ" -> items.addAll(dashboardRepository.getAllShashwathaPoojaNames().stream()
					.map(s -> s.split(":")[1]).collect(Collectors.toList()));
			case "ವಿಶೇಷ ಪೂಜೆ" -> items.addAll(dashboardRepository.getAllVisheshaPoojaNames().stream()
					.map(s -> s.split(":")[1]).collect(Collectors.toList()));
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

		// Date handling logic remains the same...

		List<DashboardStats> allStats = new ArrayList<>();
		if ("ಎಲ್ಲಾ".equals(selectedType) || "ಸೇವೆ".equals(selectedType)) {
			allStats.addAll(dashboardRepository.getSevaStatistics(fromDate, toDate, paymentMode,
					getIdFromName(selectedItem, dashboardRepository.getAllSevaNames())));
		}
		if ("ಎಲ್ಲಾ".equals(selectedType) || "ಇತರೆ ಸೇವೆ".equals(selectedType)) {
			allStats.addAll(dashboardRepository.getOtherSevaStatistics(fromDate, toDate, paymentMode,
					getIdFromName(selectedItem, dashboardRepository.getAllOtherSevaNames())));
		}
		if ("ಎಲ್ಲಾ".equals(selectedType) || "ದೇಣಿಗೆ".equals(selectedType)) {
			allStats.addAll(dashboardRepository.getDonationStatistics(fromDate, toDate, paymentMode,
					getIdFromName(selectedItem, dashboardRepository.getAllDonationNames())));
		}
		// Add new statistics
		if ("ಎಲ್ಲಾ".equals(selectedType) || "ಶಾಶ್ವತ ಪೂಜೆ".equals(selectedType)) {
			allStats.addAll(dashboardRepository.getShashwathaPoojaStatistics(fromDate, toDate));
		}
		if ("ಎಲ್ಲಾ".equals(selectedType) || "ವಿಶೇಷ ಪೂಜೆ".equals(selectedType)) {
			allStats.addAll(dashboardRepository.getVisheshaPoojaStatistics(fromDate, toDate, paymentMode,
					getIdFromName(selectedItem, dashboardRepository.getAllVisheshaPoojaNames())));
		}

		dashboardTable.setItems(FXCollections.observableArrayList(allStats));
		updateSummaryLabels(allStats);
	}

	private void updateSummaryLabels(List<DashboardStats> stats) {
		int totalRecords = stats.stream().mapToInt(DashboardStats::getTotalCount).sum();
		double totalAmount = stats.stream().mapToDouble(DashboardStats::getTotalAmount).sum();

		// Added Unicode icons for better visual appeal
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
