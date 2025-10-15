package com.pranav.temple_software.services;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.DevoteeRepository;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TextFormatter;

import java.time.LocalDate;
import java.util.List;

public class ValidationServices {
	MainController controller;
	private final DevoteeRepository devoteeRepository = new DevoteeRepository();
	private double devoteeDailyCashTotal = 0.0;
	public ValidationServices(MainController controller) {
		this.controller = controller;
	}

	public double getDevoteeDailyCashTotal() {
		return devoteeDailyCashTotal;
	}

	public void checkAndEnforceCashLimit() {
		double currentCartTotal = controller.selectedSevas.stream()
				.mapToDouble(SevaEntry::getTotalAmount)
				.sum();
		double grandTotal = devoteeDailyCashTotal + currentCartTotal;
		if (grandTotal > 2000.0) {
			if (!controller.onlineRadio.isSelected()) {
				Platform.runLater(() -> {
					controller.cashRadio.setSelected(false);
					controller.onlineRadio.setSelected(true);
					showAlert(
							String.format("Today's cash total for this devotee (₹%.2f) plus the current cart total (₹%.2f) exceeds ₹2000.\nPayment must be made online.",
									devoteeDailyCashTotal, currentCartTotal));
				});
			}
			controller.cashRadio.setDisable(true);
		} else {
			controller.cashRadio.setDisable(false);
		}
	}

	private void fetchPastTransactionsAndValidate() {
		String phoneNumber = controller.contactField.getText();
		if (phoneNumber != null && phoneNumber.length() == 10) {
			this.devoteeDailyCashTotal = devoteeRepository.getTodaysCashTotalByPhone(phoneNumber);
			checkAndEnforceCashLimit();
		} else {
			this.devoteeDailyCashTotal = 0.0;
			checkAndEnforceCashLimit();
		}
	}

	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		if (controller.mainStage != null) {
			alert.initOwner(controller.mainStage);
		}
		alert.setTitle("Cash Limit Exceeded");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public void setupPhoneValidation() {
		controller.contactField.textProperty().addListener((_, _, newValue) -> {
			if (newValue != null) {
				String digitsOnly = newValue.replaceAll("\\D", "");
				if (digitsOnly.length() > 10) {
					digitsOnly = digitsOnly.substring(0, 10);
				}

				if (!digitsOnly.equals(newValue)) {
					controller.contactField.setText(digitsOnly);
					return;
				}

				if (digitsOnly.length() == 10) {
					devoteeRepository.findLatestDevoteeDetailsByPhone(digitsOnly)
							.ifPresent(details -> Platform.runLater(() -> controller.populateDevoteeDetails(details)));
					fetchPastTransactionsAndValidate();
				} else {
					this.devoteeDailyCashTotal = 0.0;
					checkAndEnforceCashLimit();
				}
			}
		});
		controller.contactField.focusedProperty().addListener((_, _, newVal) -> {
			if (!newVal) {
				validatePhoneNumber();
			}
		});
	}

	private void validatePhoneNumber() {
		String phone = controller.contactField.getText();
		if (phone != null && !phone.isEmpty() && phone.length() < 10) {
			controller.showAlert("Invalid Phone Number", "Phone number must contain at least 10 digits");
		}
	}

	public void setupPanValidation() {
		controller.panNumberField.textProperty().addListener((_, _, newValue) -> {
			if (newValue != null) {
				String upperCase = newValue.toUpperCase();
				if (upperCase.length() > 10) {
					upperCase = upperCase.substring(0, 10);
				}
				if (!upperCase.equals(newValue)) {
					controller.panNumberField.setText(upperCase);
				}
			}
		});
		controller.panNumberField.focusedProperty().addListener((_, _, newVal) -> {
			if (!newVal) {
				String pan = controller.panNumberField.getText();
				if (pan != null && !pan.trim().isEmpty()) {
					if (!isValidPanFormat(pan.trim())) {
						controller.showAlert("Invalid PAN Format",
								"PAN should be in format: AAAPL1234C\n" +
										"(5 letters + 4 numbers + 1 letter)");
					}
				}
			}
		});
	}

	private boolean isValidPanFormat(String pan) {
		if (pan == null || pan.length() != 10) {
			return false;
		}
		return pan.matches("[A-Z]{5}[0-9]{4}[A-Z]");
	}

	public void setupNameValidation() {
		TextFormatter<String> formatter = new TextFormatter<>(change -> {
			String newText = change.getControlNewText();
			if (newText.matches("[\\p{L} ]*")) {
				return change;
			} else {
				return null;
			}
		});
		controller.devoteeNameField.setTextFormatter(formatter);
	}

	public void radioCheck(){
		controller.cashRadio.selectedProperty().addListener(
				(_, _, newValue) -> {
					if (newValue) {
						controller.onlineRadio.setSelected(false);
					}
				});
		controller.onlineRadio.selectedProperty().addListener(
				(_, _, newValue) -> {
					if (newValue) {
						controller.cashRadio.setSelected(false);
					}
				});
	}

	public void calenderChecker() {
		controller.sevaDatePicker.getEditor().textProperty().addListener((_, _, newText) -> {
			if (newText == null || newText.isEmpty()) {
				controller.sevaDatePicker.setValue(LocalDate.now());
			}
		});
	}

	public void threeNakshatraForARashi() {
		controller.nakshatraComboBox.setDisable(true);
		controller.sevaListener.rashiNakshatraMap();
		controller.raashiComboBox.getSelectionModel().selectedItemProperty().addListener(
				(_, _, newVal) -> {
					if (newVal == null || newVal.equals("ಆಯ್ಕೆ")) {
						controller.nakshatraComboBox.setDisable(true);
						controller.nakshatraComboBox.getItems().clear();
						controller.nakshatraComboBox.getSelectionModel().clearSelection();
						controller.nakshatraComboBox.setPromptText("ನಕ್ಷತ್ರವನ್ನು ಆಯ್ಕೆಮಾಡಿ");
					} else {
						List<String> nakshatrasForRashi = controller.rashiNakshatraMap.get(newVal);
						if (nakshatrasForRashi != null) {
							ObservableList<String> nakshatraItems = FXCollections.observableArrayList(nakshatrasForRashi);
							nakshatraItems.addFirst("ಆಯ್ಕೆ");
							controller.nakshatraComboBox.setItems(nakshatraItems);
							controller.nakshatraComboBox.setDisable(false);
							controller.nakshatraComboBox.setPromptText("ನಕ್ಷತ್ರವನ್ನು ಆಯ್ಕೆಮಾಡಿ");
						}
					}
				});
	}

	public void initializeTotalCalculation() {
		DoubleBinding totalBinding = Bindings.createDoubleBinding(() ->
						controller.selectedSevas.stream()
								.mapToDouble(SevaEntry::getTotalAmount)
								.sum(),
				controller.selectedSevas
		);
		totalBinding.addListener((_, _, _) -> checkAndEnforceCashLimit());
		controller.totalLabel.textProperty().bind(Bindings.createStringBinding(() ->
						String.format("₹%.2f", totalBinding.get()),
				totalBinding
		));
	}
}