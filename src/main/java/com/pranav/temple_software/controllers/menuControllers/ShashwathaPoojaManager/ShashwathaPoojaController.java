// FILE: src/main/java/com/pranav/temple_software/controllers/menuControllers/ShashwathaPoojaManager/ShashwathaPoojaController.java
package com.pranav.temple_software.controllers.menuControllers.ShashwathaPoojaManager;

import com.pranav.temple_software.models.ShashwathaPoojaReceipt;
import com.pranav.temple_software.repositories.ShashwathaPoojaRepository;
import com.pranav.temple_software.utils.ReceiptPrinter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ShashwathaPoojaController {

	@FXML private TextField devoteeNameField;
	@FXML private TextField contactField;
	@FXML private DatePicker receiptDatePicker;
	@FXML private TextField poojaDateField; // For the String-based date
	@FXML private ComboBox<String> raashiComboBox;
	@FXML private ComboBox<String> nakshatraComboBox;
	@FXML private TextArea addressField;
	@FXML private TextField panNumberField;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;

	private final ShashwathaPoojaRepository repository = new ShashwathaPoojaRepository();
	private final Map<String, List<String>> rashiNakshatraMap = new HashMap<>();
	private ReceiptPrinter receiptPrinter; // Will be set

	public void setReceiptPrinter(ReceiptPrinter printer) {
		this.receiptPrinter = printer;
	}

	@FXML
	public void initialize() {
		receiptDatePicker.setValue(LocalDate.now());
		populateRashiComboBox();
		setupRashiNakshatraListener();
		devoteeNameField.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
		panNumberField.setTextFormatter(new TextFormatter<>(change -> {
			change.setText(change.getText().toUpperCase());
			return change;
		}));
		validatePanRequirement();
	}

	@FXML
	private void handleSaveAndPrint() {
		if (!validateInput()) {
			return;
		}

		ShashwathaPoojaReceipt newReceipt = new ShashwathaPoojaReceipt(
				0, // ID is auto-generated
				devoteeNameField.getText(),
				contactField.getText(),
				addressField.getText(),
				panNumberField.getText(),
				raashiComboBox.getValue(),
				nakshatraComboBox.getValue(),
				receiptDatePicker.getValue(),
				poojaDateField.getText()
		);

		boolean success = repository.saveShashwathaPooja(newReceipt);

		if (success) {
			// Re-fetch from DB to get the auto-generated ID for the receipt
			List<ShashwathaPoojaReceipt> latest = repository.getAllShashwathaPoojaReceipts();
			if (!latest.isEmpty()) {
				ShashwathaPoojaReceipt savedReceipt = latest.get(0); // First item is latest

				// Check if receiptPrinter is null and handle appropriately
				if (receiptPrinter != null) {
					Consumer<Boolean> onPrintComplete = (printSuccess) -> {
						if (printSuccess) {
							Platform.runLater(() -> {
								showAlert(Alert.AlertType.INFORMATION, "Success",
										"Shashwatha Pooja receipt printed successfully!");
								closeWindow();
							});
						} else {
							Platform.runLater(() -> {
								showAlert(Alert.AlertType.WARNING, "Print Cancelled",
										"Receipt was saved but printing was cancelled.");
								closeWindow();
							});
						}
					};

					Runnable onDialogClosed = () -> {
						Platform.runLater(() -> closeWindow());
					};

					try {
						Stage ownerStage = (Stage) saveButton.getScene().getWindow();
						receiptPrinter.showShashwathaPoojaPrintPreview(savedReceipt, ownerStage, onPrintComplete, onDialogClosed);
					} catch (Exception e) {
						e.printStackTrace();
						showAlert(Alert.AlertType.ERROR, "Print Error",
								"Failed to open print preview: " + e.getMessage());
						closeWindow();
					}
				} else {
					showAlert(Alert.AlertType.ERROR, "Print Error",
							"Receipt printer is not initialized. Please contact support.");
					closeWindow();
				}
			} else {
				showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve saved receipt for printing.");
				closeWindow();
			}
		} else {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save the Pooja. Please check the logs.");
		}
	}

	private boolean validateInput() {
		if (devoteeNameField.getText() == null || devoteeNameField.getText().trim().isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Devotee Name is required.");
			return false;
		}
		if (poojaDateField.getText() == null || poojaDateField.getText().trim().isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Pooja Date/Detail is required.");
			return false;
		}
		if (receiptDatePicker.getValue() == null) {
			showAlert(Alert.AlertType.WARNING, "Validation Error", "Receipt Date is required.");
			return false;
		}
		return true;
	}

	private boolean validatePanRequirement() {
		String panNumber = panNumberField.getText();
		// Basic PAN format validation
		if (!isValidPanFormat(panNumber.trim())) {
			showAlert(Alert.AlertType.INFORMATION,"Invalid PAN",
					"Please enter a valid PAN number format (e.g., AAAPL1234C)");
			Platform.runLater(() -> panNumberField.requestFocus());
			return false;
		}
		return true;
	}

	private boolean isValidPanFormat(String pan) {
		if (pan == null || pan.length() != 10) {
			return false;
		}
		// PAN format: 5 letters, 4 digits, 1 letter
		return pan.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}");
	}

	@FXML
	private void handleCancel() {
		closeWindow();
	}

	private void closeWindow() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}

	private void populateRashiComboBox() {
		ObservableList<String> rashiOptions = FXCollections.observableArrayList();
		rashiOptions.add("ಆಯ್ಕೆ");
		rashiOptions.addAll(
				"ಮೇಷ", "ವೃಷಭ", "ಮಿಥುನ", "ಕರ್ಕಾಟಕ", "ಸಿಂಹ", "ಕನ್ಯಾ",
				"ತುಲಾ", "ವೃಶ್ಚಿಕ", "ಧನು", "ಮಕರ", "ಕುಂಭ", "ಮೀನ"
		);
		raashiComboBox.setItems(rashiOptions);
		raashiComboBox.getSelectionModel().selectFirst();
	}

	private void setupRashiNakshatraListener() {
		rashiNakshatraMap.put("ಮೇಷ", Arrays.asList("ಅಶ್ವಿನಿ", "ಭರಣಿ", "ಕೃತಿಕ"));
		rashiNakshatraMap.put("ವೃಷಭ", Arrays.asList("ಕೃತಿಕ", "ರೋಹಿಣಿ", "ಮೃಗಶಿರ"));
		rashiNakshatraMap.put("ಮಿಥುನ", Arrays.asList("ಮೃಗಶಿರ", "ಆರ್ದ್ರ", "ಪುನರ್ವಸು"));
		rashiNakshatraMap.put("ಕರ್ಕ", Arrays.asList("ಪುನರ್ವಸು", "ಪುಷ್ಯ", "ಆಶ್ಲೇಷ"));
		rashiNakshatraMap.put("ಸಿಂಹ", Arrays.asList("ಮಘ", "ಪೂರ್ವ ಫಲ್ಗುನಿ", "ಉತ್ತರ ಫಲ್ಗುನಿ"));
		rashiNakshatraMap.put("ಕನ್ಯಾ", Arrays.asList("ಉತ್ತರ ಫಲ್ಗುನಿ", "ಹಸ್ತ", "ಚಿತ್ರ"));
		rashiNakshatraMap.put("ತುಲಾ", Arrays.asList("ಚಿತ್ರ", "ಸ್ವಾತಿ", "ವಿಶಾಖ"));
		rashiNakshatraMap.put("ವೃಶ್ಚಿಕ", Arrays.asList("ವಿಶಾಖ", "ಅನುರಾಧ", "ಜ್ಯೇಷ್ಠ"));
		rashiNakshatraMap.put("ಧನುಸ್", Arrays.asList("ಮೂಲ", "ಪೂರ್ವ ಆಷಾಢ", "ಉತ್ತರ ఆಷాಢ"));
		rashiNakshatraMap.put("ಮಕರ", Arrays.asList("ಉತ್ತರ ಆಷಾಢ", "ಶ್ರವಣ", "ಧನಿಷ್ಠ"));
		rashiNakshatraMap.put("ಕುಂಭ", Arrays.asList("ಧನಿಷ್ಠ", "ಶತಭಿಷ", "ಪೂರ್ವ ಭಾದ್ರಪದ"));
		rashiNakshatraMap.put("ಮೀನ", Arrays.asList("ಪೂರ್ವ ಭಾದ್ರಪದ", "ಉತ್ತರ ಭಾದ್ರಪದ", "ರೇವತಿ"));

		nakshatraComboBox.setDisable(true);
		raashiComboBox.getSelectionModel().selectedItemProperty().addListener(
				(obs, oldVal, newVal) -> {
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

	private void showAlert(Alert.AlertType alertType, String title, String message) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}