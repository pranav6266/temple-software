package com.pranav.temple_software.controllers.menuControllers.Karyakrama;

import com.pranav.temple_software.models.*;
import com.pranav.temple_software.repositories.DevoteeRepository;
import com.pranav.temple_software.repositories.KaryakramaReceiptRepository;
import com.pranav.temple_software.repositories.KaryakramaRepository;
import com.pranav.temple_software.repositories.KaryakramaSevaRepository;
import com.pranav.temple_software.utils.ReceiptPrinter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class KaryakramaController {

	@FXML private TextField devoteeNameField;
	@FXML private TextField contactField;
	@FXML private TextArea addressField;
	@FXML private TextField panNumberField;
	@FXML private DatePicker receiptDatePicker;
	@FXML private ComboBox<Karyakrama> karyakramaComboBox;
	@FXML private ComboBox<KaryakramaSeva> sevaComboBox;
	@FXML private Spinner<Integer> quantitySpinner;
	@FXML private TableView<SevaEntry> sevasTableView;
	@FXML private TableColumn<SevaEntry, String> sevaNameColumn;
	@FXML private TableColumn<SevaEntry, Number> amountColumn;
	@FXML private TableColumn<SevaEntry, Integer> quantityColumn;
	@FXML private TableColumn<SevaEntry, Number> totalColumn;
	@FXML private Label totalAmountLabel;
	@FXML private RadioButton cashRadio;
	@FXML private RadioButton onlineRadio;
	@FXML private Button saveButton;

	private final KaryakramaRepository karyakramaRepository = new KaryakramaRepository();
	private final KaryakramaSevaRepository karyakramaSevaRepository = new KaryakramaSevaRepository();
	private final KaryakramaReceiptRepository receiptRepository = new KaryakramaReceiptRepository();
	private final DevoteeRepository devoteeRepository = new DevoteeRepository();
	private final ObservableList<SevaEntry> selectedSevas = FXCollections.observableArrayList();
	private final ReceiptPrinter receiptPrinter = new ReceiptPrinter(null);

	@FXML
	public void initialize() {
		receiptDatePicker.setValue(LocalDate.now());
		setupDevoteeFields();
		setupKaryakramaSelection();
		setupTableView();
		updateTotal();
	}

	private void setupDevoteeFields() {
		contactField.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal && contactField.getText() != null && contactField.getText().length() == 10) {
				devoteeRepository.findLatestDevoteeDetailsByPhone(contactField.getText())
						.ifPresent(this::populateDevoteeDetails);
			}
		});
	}

	private void populateDevoteeDetails(DevoteeDetails details) {
		devoteeNameField.setText(details.getName());
		addressField.setText(details.getAddress());
		panNumberField.setText(details.getPanNumber());
	}

	private void setupKaryakramaSelection() {
		karyakramaComboBox.setItems(FXCollections.observableArrayList(karyakramaRepository.getAllKaryakramagalu()));
		karyakramaComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			sevaComboBox.getItems().clear();
			if (newVal != null) {
				sevaComboBox.setItems(FXCollections.observableArrayList(karyakramaSevaRepository.getSevasForKaryakrama(newVal.getId())));
			}
		});
	}

	private void setupTableView() {
		sevaNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
		quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
		sevasTableView.setItems(selectedSevas);
	}

	@FXML
	private void handleAddSeva() {
		KaryakramaSeva selectedSeva = sevaComboBox.getValue();
		int quantity = quantitySpinner.getValue();

		if (selectedSeva == null || quantity <= 0) {
			showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please select a seva and specify a valid quantity.");
			return;
		}

		Optional<SevaEntry> existingEntry = selectedSevas.stream()
				.filter(entry -> entry.getName().equals(selectedSeva.getName()))
				.findFirst();

		if (existingEntry.isPresent()) {
			existingEntry.get().setQuantity(existingEntry.get().getQuantity() + quantity);
		} else {
			SevaEntry newEntry = new SevaEntry(selectedSeva.getName(), selectedSeva.getAmount());
			newEntry.setQuantity(quantity);
			selectedSevas.add(newEntry);
		}
		sevasTableView.refresh();
		updateTotal();

		sevaComboBox.getSelectionModel().clearSelection();
		quantitySpinner.getValueFactory().setValue(1);
	}

	@FXML
	private void handleSaveAndPrint() {
		if (!validateInput()) return;

		double totalAmount = selectedSevas.stream().mapToDouble(SevaEntry::getTotalAmount).sum();
		if (totalAmount > 2000 && (panNumberField.getText() == null || panNumberField.getText().trim().isEmpty())) {
			showAlert(Alert.AlertType.ERROR, "Validation Error", "PAN number is mandatory for transactions above ₹2000.");
			return;
		}

		KaryakramaReceiptData receiptData = new KaryakramaReceiptData(
				0, devoteeNameField.getText(), contactField.getText(), addressField.getText(),
				panNumberField.getText(), "", "", receiptDatePicker.getValue(),
				selectedSevas.stream().collect(Collectors.toList()),
				totalAmount, cashRadio.isSelected() ? "Cash" : "Online"
		);

		int savedId = receiptRepository.saveReceipt(receiptData);

		if (savedId != -1) {
			KaryakramaReceiptData savedReceiptData = new KaryakramaReceiptData(savedId,
					receiptData.getDevoteeName(), receiptData.getPhoneNumber(), receiptData.getAddress(), receiptData.getPanNumber(),
					receiptData.getRashi(), receiptData.getNakshatra(), receiptData.getReceiptDate(), receiptData.getSevas(),
					receiptData.getTotalAmount(), receiptData.getPaymentMode());

			Consumer<Boolean> onPrintComplete = (success) -> Platform.runLater(this::closeWindow);
			Runnable onDialogClosed = this::closeWindow;
			Stage ownerStage = (Stage) saveButton.getScene().getWindow();
			receiptPrinter.showKaryakramaPrintPreview(savedReceiptData, ownerStage, onPrintComplete, onDialogClosed);
		} else {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save the receipt.");
		}
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
		if (selectedSevas.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Please add at least one seva to the receipt.");
			return false;
		}
		if (!cashRadio.isSelected() && !onlineRadio.isSelected()) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a payment method.");
			return false;
		}
		return true;
	}

	private void updateTotal() {
		double total = selectedSevas.stream().mapToDouble(SevaEntry::getTotalAmount).sum();
		totalAmountLabel.setText(String.format("₹%.2f", total));
	}

	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
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