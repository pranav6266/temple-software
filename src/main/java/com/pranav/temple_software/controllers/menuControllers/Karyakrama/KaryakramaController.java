package com.pranav.temple_software.controllers.menuControllers.Karyakrama;
import com.pranav.temple_software.models.*;
import com.pranav.temple_software.repositories.DevoteeRepository;
import com.pranav.temple_software.repositories.KaryakramaReceiptRepository;
import com.pranav.temple_software.repositories.KaryakramaRepository;
import com.pranav.temple_software.repositories.OthersRepository;
import com.pranav.temple_software.utils.DatabaseManager;
import com.pranav.temple_software.utils.ReceiptPrinter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class KaryakramaController {

	@FXML private TextField devoteeNameField;
	@FXML private TextField contactField;
	@FXML private TextArea addressField;
	@FXML private TextField panNumberField;
	@FXML private DatePicker receiptDatePicker;
	@FXML private ComboBox<Karyakrama> karyakramaComboBox;
	@FXML private ComboBox<Others> othersComboBox;
	@FXML private TextField amountField;
	@FXML private TableView<SevaEntry> othersTableView;
	@FXML private TableColumn<SevaEntry, String> otherNameColumn;
	@FXML private TableColumn<SevaEntry, Number> totalColumn;
	@FXML private Label totalAmountLabel;
	@FXML private RadioButton cashRadio;
	@FXML private RadioButton onlineRadio;
	@FXML private Button saveButton;

	// --- NEW FIELDS ---
	@FXML private ComboBox<String> raashiComboBox;
	@FXML private ComboBox<String> nakshatraComboBox;

	private final KaryakramaRepository karyakramaRepository = new KaryakramaRepository();
	private final OthersRepository othersRepository = OthersRepository.getInstance();
	private final KaryakramaReceiptRepository receiptRepository = new KaryakramaReceiptRepository();
	private final DevoteeRepository devoteeRepository = new DevoteeRepository();
	private final ObservableList<SevaEntry> selectedOthers = FXCollections.observableArrayList();
	private final ReceiptPrinter receiptPrinter = new ReceiptPrinter();
	// --- NEW MAP ---
	private final Map<String, List<String>> rashiNakshatraMap = new HashMap<>();

	@FXML
	public void initialize() {
		receiptDatePicker.setValue(LocalDate.now());
		setupDevoteeFields();
		setupSelections();
		setupTableView();
		updateTotal();

		// --- NEW METHOD CALLS ---
		populateRashiComboBox();
		setupRashiNakshatraListener();

		devoteeNameField.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
		addressField.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
	}

	private void setupDevoteeFields() {
		contactField.textProperty().addListener((_, _, newValue) -> {
			if (newValue != null) {
				String digitsOnly = newValue.replaceAll("\\D", "");
				if (digitsOnly.length() > 10) {
					digitsOnly = digitsOnly.substring(0, 10);
				}

				if (!digitsOnly.equals(newValue)) {
					contactField.setText(digitsOnly);
				}

				if (digitsOnly.length() == 10) {
					devoteeRepository.findLatestDevoteeDetailsByPhone(digitsOnly)
							.ifPresent(this::populateDevoteeDetails);
				}
			}
		});
	}

	private void populateDevoteeDetails(DevoteeDetails details) {
		devoteeNameField.setText(details.getName());
		addressField.setText(details.getAddress());
		panNumberField.setText(details.getPanNumber());

		// --- NEW LOGIC for Raashi/Nakshatra ---
		if (details.getRashi() != null && !details.getRashi().isEmpty()) {
			raashiComboBox.setValue(details.getRashi());
		} else {
			raashiComboBox.getSelectionModel().selectFirst();
		}
		Platform.runLater(() -> {
			if (details.getNakshatra() != null && !details.getNakshatra().isEmpty()) {
				if (nakshatraComboBox.getItems().contains(details.getNakshatra())) {
					nakshatraComboBox.setValue(details.getNakshatra());
				}
			}
		});
	}

	private void setupSelections() {
		karyakramaComboBox.setItems(FXCollections.observableArrayList(karyakramaRepository.getAllKaryakramagalu()));
		othersComboBox.setItems(FXCollections.observableArrayList(othersRepository.getAllOthers()));
	}

	private void setupTableView() {
		otherNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
		othersTableView.setItems(selectedOthers);
	}

	@FXML
	private void handleAddOther() {
		Others selectedOther = othersComboBox.getValue();
		String amountStr = amountField.getText();
		if (selectedOther == null) {
			showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please select an 'Other' item.");
			return;
		}

		double amount;
		try {
			amount = Double.parseDouble(amountStr);
			if (amount <= 0) {
				showAlert(Alert.AlertType.WARNING, "Invalid Amount", "Amount must be greater than zero.");
				return;
			}
		} catch (NumberFormatException e) {
			showAlert(Alert.AlertType.WARNING, "Invalid Amount", "Please enter a valid number for the amount.");
			return;
		}

		SevaEntry newEntry = new SevaEntry(selectedOther.getName(), amount);
		newEntry.setQuantity(1);
		selectedOthers.add(newEntry);

		othersTableView.refresh();
		updateTotal();

		othersComboBox.getSelectionModel().clearSelection();
		amountField.clear();
	}

	@FXML
	private void handleSaveAndPrint() {
		if (!validateInput()) return;
		Karyakrama selectedKaryakrama = karyakramaComboBox.getValue();
		double totalAmount = selectedOthers.stream().mapToDouble(SevaEntry::getTotalAmount).sum();
		if (totalAmount > 2000 && (panNumberField.getText() == null || panNumberField.getText().trim().isEmpty())) {
			showAlert(Alert.AlertType.ERROR, "Validation Error", "PAN number is mandatory for transactions above ₹2000.");
			return;
		}

		// --- MODIFIED: Get Raashi and Nakshatra values ---
		String rashiValue = raashiComboBox.getValue();
		String finalRashi = (rashiValue != null && rashiValue.equals("ಆಯ್ಕೆ")) ? "" : rashiValue;
		String nakshatra = nakshatraComboBox.getValue();

		KaryakramaReceiptData receiptData = new KaryakramaReceiptData(
				0, devoteeNameField.getText(), contactField.getText(), addressField.getText(),
				panNumberField.getText(),
				finalRashi, // <-- Pass Rashi
				nakshatra, // <-- Pass Nakshatra
				selectedKaryakrama.getName(), receiptDatePicker.getValue(),
				new ArrayList<>(selectedOthers),
				totalAmount, cashRadio.isSelected() ? "Cash" : "Online"
		);

		// DB logic
		int actualSavedId = -1;
		Connection conn = null;
		try {
			conn = DatabaseManager.getConnection();
			conn.setAutoCommit(false);

			actualSavedId = receiptRepository.saveReceipt(conn, receiptData);

			if (actualSavedId != -1) {
				boolean itemsSaved = receiptRepository.saveReceiptItems(conn, actualSavedId, receiptData.getSevas());
				if (itemsSaved) {
					conn.commit();
				} else {
					conn.rollback();
					showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save receipt items.");
					return;
				}
			} else {
				conn.rollback();
				showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save the receipt.");
				return;
			}
		} catch (SQLException e) {
			if (conn != null) {
				try { conn.rollback(); } catch (SQLException ex) { /* Log error */ }
			}
			showAlert(Alert.AlertType.ERROR, "Database Error", "A database error occurred: " + e.getMessage());
			return;
		} finally {
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { /* Log error */ }
			}
		}

		Consumer<Boolean> afterActionCallback = (success) -> {
			Platform.runLater(this::closeWindow);
		};

		Runnable onDialogClosed = this::closeWindow;
		Stage ownerStage = (Stage) saveButton.getScene().getWindow();

		// Create preview data with the *actual* saved ID
		KaryakramaReceiptData previewData = new KaryakramaReceiptData(
				actualSavedId, receiptData.getDevoteeName(), receiptData.getPhoneNumber(),
				receiptData.getAddress(), receiptData.getPanNumber(), receiptData.getRashi(),
				receiptData.getNakshatra(), receiptData.getKaryakramaName(), receiptData.getReceiptDate(),
				receiptData.getSevas(), receiptData.getTotalAmount(), receiptData.getPaymentMode()
		);
		receiptPrinter.showKaryakramaPrintPreview(previewData, ownerStage, afterActionCallback, onDialogClosed);
	}

	private boolean validateInput() {
		if (karyakramaComboBox.getValue() == null) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a Karyakrama.");
			return false;
		}
		if (devoteeNameField.getText().trim().isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Devotee Name is required.");
			return false;
		}
		if (selectedOthers.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Please add at least one 'other' item to the receipt.");
			return false;
		}
		if (!cashRadio.isSelected() && !onlineRadio.isSelected()) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a payment method.");
			return false;
		}
		return true;
	}

	private void updateTotal() {
		double total = selectedOthers.stream().mapToDouble(SevaEntry::getTotalAmount).sum();
		totalAmountLabel.setText(String.format("₹%.2f", total));
	}

	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		if (saveButton != null && saveButton.getScene() != null) {
			alert.initOwner(saveButton.getScene().getWindow());
		}
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	@FXML
	private void closeWindow() {
		Stage stage = (Stage) saveButton.getScene().getWindow();
		stage.close();
	}

	// --- NEW HELPER METHODS ---

	private void populateRashiComboBox() {
		ObservableList<String> rashiOptions = FXCollections.observableArrayList();
		rashiOptions.add("ಆಯ್ಕೆ");
		rashiOptions.addAll("ಮೇಷ", "ವೃಷಭ", "ಮಿಥುನ", "ಕರ್ಕಾಟಕ", "ಸಿಂಹ", "ಕನ್ಯಾ", "ತುಲಾ", "ವೃಶ್ಚಿಕ", "ಧನು", "ಮಕರ", "ಕುಂಭ", "ಮೀನ");
		raashiComboBox.setItems(rashiOptions);
		raashiComboBox.getSelectionModel().selectFirst();
	}

	private void setupRashiNakshatraListener() {
		rashiNakshatraMap.put("ಮೇಷ", Arrays.asList("ಅಶ್ವಿನಿ", "ಭರಣಿ", "ಕೃತ್ತಿಕ"));
		rashiNakshatraMap.put("ವೃಷಭ", Arrays.asList("ಕೃತ್ತಿಕ", "ರೋಹಿಣಿ", "ಮೃಗಶಿರ"));
		rashiNakshatraMap.put("ಮಿಥುನ", Arrays.asList("ಮೃಗಶಿರ", "ಆರ್ದ್ರ", "ಪುನರ್ವಸು"));
		rashiNakshatraMap.put("ಕರ್ಕಾಟಕ", Arrays.asList("ಪುನರ್ವಸು", "ಪುಷ್ಯ", "ಆಶ್ಲೇಷ"));
		rashiNakshatraMap.put("ಸಿಂಹ", Arrays.asList("ಮಘ", "ಪೂರ್ವ", "ಉತ್ತರ"));
		rashiNakshatraMap.put("ಕನ್ಯಾ", Arrays.asList("ಉತ್ತರ", "ಹಸ್ತ", "ಚಿತ್ರ"));
		rashiNakshatraMap.put("ತುಲಾ", Arrays.asList("ಚಿತ್ರ", "ಸ್ವಾತಿ", "ವಿಶಾಖ"));
		rashiNakshatraMap.put("ವೃಶ್ಚಿಕ", Arrays.asList("ವಿಶಾಖ", "ಅನುರಾಧ", "ಜೇಷ್ಠ"));
		rashiNakshatraMap.put("ಧನು", Arrays.asList("ಮೂಲ", "ಪೂರ್ವಾಷಾಢ", "ಉತ್ತರಾಷಾಢ"));
		rashiNakshatraMap.put("ಮಕರ", Arrays.asList("ಉತ್ತರಾಷಾಢ", "ಶ್ರವಣ", "ಧನಿಷ್ಠ"));
		rashiNakshatraMap.put("ಕುಂಭ", Arrays.asList("ಧನಿಷ್ಠ", "ಶತಭಿಷ", "ಪೂರ್ವಾಭಾದ್ರ"));
		rashiNakshatraMap.put("ಮೀನ", Arrays.asList("ಪೂರ್ವಾಭಾದ", "ಉತ್ತರಾಭಾದ್ರ", "ರೇವತಿ"));

		nakshatraComboBox.setDisable(true);
		raashiComboBox.getSelectionModel().selectedItemProperty().addListener((
				_, _, newVal) -> {
			if (newVal == null || newVal.equals("ಆಯ್ಕೆ")) {
				nakshatraComboBox.setDisable(true);
				nakshatraComboBox.getItems().clear();
			} else {
				List<String> nakshatras = rashiNakshatraMap.get(newVal);
				if (nakshatras != null) {
					nakshatraComboBox.setItems(FXCollections.observableArrayList(nakshatras));
					nakshatraComboBox.setDisable(false);
				}
			}
		});
	}
}