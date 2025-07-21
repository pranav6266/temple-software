package com.pranav.temple_software.controllers.menuControllers.History;

import com.pranav.temple_software.models.DashboardStats;
import com.pranav.temple_software.repositories.DashboardRepository;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardController {

	@FXML private ComboBox<String> typeComboBox;
	@FXML private ComboBox<String> itemComboBox;
	@FXML private DatePicker fromDatePicker;
	@FXML private DatePicker toDatePicker;
	@FXML private ComboBox<String> monthComboBox;
	@FXML private ComboBox<String> yearComboBox;
	@FXML private ComboBox<String> paymentModeComboBox;
	@FXML private Button generateReportButton;
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

		// Load initial data
		generateReport();
	}

	private void setupComboBoxes() {
		// Type ComboBox
		typeComboBox.setItems(FXCollections.observableArrayList(
				"ಎಲ್ಲಾ", "ಸೇವೆ", "ಇತರೆ ಸೇವೆ", "ದೇಣಿಗೆ"
		));
		typeComboBox.setValue("ಎಲ್ಲಾ");

		// Payment Mode ComboBox
		paymentModeComboBox.setItems(FXCollections.observableArrayList(
				"All", "Cash", "Online"
		));
		paymentModeComboBox.setValue("All");

		// Month ComboBox
		monthComboBox.setItems(FXCollections.observableArrayList(
				"All", "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
				"JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
		));
		monthComboBox.setValue("All");

		// Year ComboBox
		List<String> years = new ArrayList<>();
		years.add("");
		int currentYear = LocalDate.now().getYear();
		for (int y = currentYear; y >= 2000; y--) {
			years.add(String.valueOf(y));
		}
		yearComboBox.setItems(FXCollections.observableArrayList(years));
		yearComboBox.setValue("");

		// Item ComboBox (initially empty, populated based on type selection)
		itemComboBox.setItems(FXCollections.observableArrayList("ಎಲ್ಲಾ"));
		itemComboBox.setValue("ಎಲ್ಲಾ");
	}

	private void setupTableColumns() {
		itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
		itemTypeColumn.setCellValueFactory(cellData -> {
			String type = cellData.getValue().getItemType();
			String kannada = switch (type) {
				case "SEVA" -> "ಸೇವೆ";
				case "OTHER_SEVA" -> "ಇತರೆ ಸೇವೆ";
				case "DONATION" -> "ದೇಣಿಗೆ";
				default -> type;
			};
			return new SimpleStringProperty(kannada);
		});

		totalCountColumn.setCellValueFactory(new PropertyValueFactory<>("totalCount"));
		totalCountColumn.setCellFactory(column -> new TableCell<DashboardStats, Integer>() {
			@Override
			protected void updateItem(Integer count, boolean empty) {
				super.updateItem(count, empty);
				if (empty || count == null) {
					setText(null);
				} else {
					setText(count.toString());
					setAlignment(Pos.CENTER);
				}
			}
		});

		cashCountColumn.setCellValueFactory(new PropertyValueFactory<>("cashCount"));
		cashCountColumn.setCellFactory(column -> new TableCell<DashboardStats, Integer>() {
			@Override
			protected void updateItem(Integer count, boolean empty) {
				super.updateItem(count, empty);
				if (empty || count == null) {
					setText(null);
				} else {
					setText(count.toString());
					setAlignment(Pos.CENTER);
				}
			}
		});

		onlineCountColumn.setCellValueFactory(new PropertyValueFactory<>("onlineCount"));
		onlineCountColumn.setCellFactory(column -> new TableCell<DashboardStats, Integer>() {
			@Override
			protected void updateItem(Integer count, boolean empty) {
				super.updateItem(count, empty);
				if (empty || count == null) {
					setText(null);
				} else {
					setText(count.toString());
					setAlignment(Pos.CENTER);
				}
			}
		});

		totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
		totalAmountColumn.setCellFactory(column -> new TableCell<DashboardStats, Double>() {
			@Override
			protected void updateItem(Double amount, boolean empty) {
				super.updateItem(amount, empty);
				if (empty || amount == null) {
					setText(null);
				} else {
					setText(String.format("₹%.2f", amount));
					setAlignment(Pos.CENTER_RIGHT);
				}
			}
		});
	}

	private void setupEventHandlers() {
		// Type selection change handler
		typeComboBox.setOnAction(e -> updateItemComboBox());

		generateReportButton.setOnAction(e -> generateReport());
		clearFiltersButton.setOnAction(e -> clearAllFilters());
	}

	private void updateItemComboBox() {
		String selectedType = typeComboBox.getValue();
		List<String> items = new ArrayList<>();
		items.add("ಎಲ್ಲಾ");

		switch (selectedType) {
			case "ಸೇವೆ":
				List<String> sevaNames = dashboardRepository.getAllSevaNames();
				for (String seva : sevaNames) {
					String[] parts = seva.split(":");
					if (parts.length == 2) {
						items.add(parts[1]); // Add name only
					}
				}
				break;
			case "ಇತರೆ ಸೇವೆ":
				List<String> otherSevaNames = dashboardRepository.getAllOtherSevaNames();
				for (String otherSeva : otherSevaNames) {
					String[] parts = otherSeva.split(":");
					if (parts.length == 2) {
						items.add(parts[1]); // Add name only
					}
				}
				break;
			case "ದೇಣಿಗೆ":
				List<String> donationNames = dashboardRepository.getAllDonationNames();
				for (String donation : donationNames) {
					String[] parts = donation.split(":");
					if (parts.length == 2) {
						items.add(parts[1]); // Add name only
					}
				}
				break;
		}

		itemComboBox.setItems(FXCollections.observableArrayList(items));
		itemComboBox.setValue("ಎಲ್ಲಾ");
	}

	@FXML
	private void generateReport() {
		// Get filter values
		String selectedType = typeComboBox.getValue();
		String selectedItem = itemComboBox.getValue();
		LocalDate fromDate = fromDatePicker.getValue();
		LocalDate toDate = toDatePicker.getValue();
		String selectedMonth = monthComboBox.getValue();
		String selectedYear = yearComboBox.getValue();
		String paymentMode = paymentModeComboBox.getValue();

		// Handle date/month/year logic
		if (fromDate == null && selectedMonth != null && !selectedMonth.equals("All")) {
			int year = selectedYear != null && !selectedYear.isEmpty() ?
					Integer.parseInt(selectedYear) : LocalDate.now().getYear();
			int monthValue = getMonthValue(selectedMonth);
			fromDate = LocalDate.of(year, monthValue, 1);
			toDate = fromDate.plusMonths(1).minusDays(1);
		} else if (fromDate == null && selectedYear != null && !selectedYear.isEmpty()) {
			int year = Integer.parseInt(selectedYear);
			fromDate = LocalDate.of(year, 1, 1);
			toDate = LocalDate.of(year, 12, 31);
		}

		// Fetch data based on type
		List<DashboardStats> allStats = new ArrayList<>();

		if (selectedType.equals("ಎಲ್ಲಾ") || selectedType.equals("ಸೇವೆ")) {
			String sevaId = getIdFromName(selectedItem, dashboardRepository.getAllSevaNames());
			allStats.addAll(dashboardRepository.getSevaStatistics(fromDate, toDate, paymentMode, sevaId));
		}

		if (selectedType.equals("ಎಲ್ಲಾ") || selectedType.equals("ಇತರೆ ಸೇವೆ")) {
			String otherSevaId = getIdFromName(selectedItem, dashboardRepository.getAllOtherSevaNames());
			allStats.addAll(dashboardRepository.getOtherSevaStatistics(fromDate, toDate, paymentMode, otherSevaId));
		}

		if (selectedType.equals("ಎಲ್ಲಾ") || selectedType.equals("ದೇಣಿಗೆ")) {
			String donationId = getIdFromName(selectedItem, dashboardRepository.getAllDonationNames());
			allStats.addAll(dashboardRepository.getDonationStatistics(fromDate, toDate, paymentMode, donationId));
		}

		// Update table
		dashboardTable.setItems(FXCollections.observableArrayList(allStats));

		// Update summary labels
		updateSummaryLabels(allStats);
	}

	private String getIdFromName(String itemName, List<String> fullList) {
		if (itemName == null || itemName.equals("ಎಲ್ಲಾ")) {
			return null;
		}

		for (String item : fullList) {
			String[] parts = item.split(":");
			if (parts.length == 2 && parts[1].equals(itemName)) {
				return parts[0];
			}
		}
		return null;
	}

	private int getMonthValue(String monthName) {
		return switch (monthName) {
			case "JANUARY" -> 1;
			case "FEBRUARY" -> 2;
			case "MARCH" -> 3;
			case "APRIL" -> 4;
			case "MAY" -> 5;
			case "JUNE" -> 6;
			case "JULY" -> 7;
			case "AUGUST" -> 8;
			case "SEPTEMBER" -> 9;
			case "OCTOBER" -> 10;
			case "NOVEMBER" -> 11;
			case "DECEMBER" -> 12;
			default -> 1;
		};
	}

	private void updateSummaryLabels(List<DashboardStats> stats) {
		int totalRecords = stats.stream().mapToInt(DashboardStats::getTotalCount).sum();
		double totalAmount = stats.stream().mapToDouble(DashboardStats::getTotalAmount).sum();

		totalRecordsLabel.setText("ಒಟ್ಟು ದಾಖಲೆಗಳು: " + totalRecords);
		totalAmountLabel.setText("ಒಟ್ಟು ಮೊತ್ತ: ₹" + String.format("%.2f", totalAmount));
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

		// Regenerate report with cleared filters
		generateReport();
	}

	public void closeWindow() {
		Stage stage = (Stage) dashboardTable.getScene().getWindow();
		if (stage != null) {
			stage.close();
		}
	}
}
