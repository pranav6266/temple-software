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

	private final KaryakramaRepository karyakramaRepository = new KaryakramaRepository();
	private final OthersRepository othersRepository = OthersRepository.getInstance();
	private final KaryakramaReceiptRepository receiptRepository = new KaryakramaReceiptRepository();
	private final DevoteeRepository devoteeRepository = new DevoteeRepository();
	private final ObservableList<SevaEntry> selectedOthers = FXCollections.observableArrayList();
	private final ReceiptPrinter receiptPrinter = new ReceiptPrinter();

	@FXML
	public void initialize() {
		receiptDatePicker.setValue(LocalDate.now());
		setupDevoteeFields();
		setupSelections();
		setupTableView();
		updateTotal();

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

		KaryakramaReceiptData receiptData = new KaryakramaReceiptData(
				0, devoteeNameField.getText(), contactField.getText(), addressField.getText(),
				panNumberField.getText(), "", "", selectedKaryakrama.getName(), receiptDatePicker.getValue(),
				new ArrayList<>(selectedOthers),
				totalAmount, cashRadio.isSelected() ? "Cash" : "Online"
		);

		// MODIFICATION START: The database logic is now wrapped in a transaction here.
		Consumer<Boolean> afterActionCallback = (success) -> {
			if (success) {
				Connection conn = null;
				try {
					conn = DatabaseManager.getConnection();
					conn.setAutoCommit(false); // Start transaction

					int savedId = receiptRepository.saveReceipt(conn, receiptData);

					if (savedId != -1) {
						boolean itemsSaved = receiptRepository.saveReceiptItems(conn, savedId, receiptData.getSevas());
						if (itemsSaved) {
							conn.commit(); // All good, commit transaction
						} else {
							conn.rollback();
							Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save receipt items."));
						}
					} else {
						conn.rollback();
						Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save the receipt."));
					}
				} catch (SQLException e) {
					if (conn != null) {
						try { conn.rollback(); } catch (SQLException ex) { /* Log error */ }
					}
					Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Database Error", "A database error occurred: " + e.getMessage()));
				} finally {
					if (conn != null) {
						try { conn.close(); } catch (SQLException e) { /* Log error */ }
					}
				}
			}
			Platform.runLater(this::closeWindow);
		};
		// MODIFICATION END

		Runnable onDialogClosed = this::closeWindow;
		Stage ownerStage = (Stage) saveButton.getScene().getWindow();
		int provisionalId = receiptRepository.getNextReceiptId();
		KaryakramaReceiptData previewData = new KaryakramaReceiptData(provisionalId, receiptData.getDevoteeName(), receiptData.getPhoneNumber(), receiptData.getAddress(), receiptData.getPanNumber(), receiptData.getRashi(), receiptData.getNakshatra(), receiptData.getKaryakramaName(), receiptData.getReceiptDate(), receiptData.getSevas(), receiptData.getTotalAmount(), receiptData.getPaymentMode());
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
}