package com.pranav.temple_software.services;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.DevoteeDetails;
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
import java.util.Optional;

public class ValidationServices {
	MainController controller;
	private final DevoteeRepository devoteeRepository = new DevoteeRepository();
	private double devoteeDailyCashTotal = 0.0;
	public ValidationServices(MainController controller) {
		this.controller = controller;
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
					showAlert("Cash Limit Exceeded",
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

	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public void setupPhoneValidation() {
		controller.contactField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				if (!newValue.matches("\\d*")) {
					controller.contactField.setText(newValue.replaceAll("[^\\d]", ""));
				}
				if (newValue.length() > 10) {
					controller.contactField.setText(newValue.substring(0, 10));
				}
			}
		});
		controller.contactField.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal) { // When focus is lost
				String phoneNumber = controller.contactField.getText();
				if (phoneNumber != null && phoneNumber.length() == 10) {
					Optional<DevoteeDetails> detailsOpt = devoteeRepository.findLatestDevoteeDetailsByPhone(phoneNumber);
					detailsOpt.ifPresent(details -> {
						Platform.runLater(() -> controller.populateDevoteeDetails(details));
					});
					fetchPastTransactionsAndValidate();
				} else if (phoneNumber != null && !phoneNumber.isEmpty()) {
					validatePhoneNumber();
				} else {
					fetchPastTransactionsAndValidate();
				}
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
		controller.panNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				String upperCase = newValue.toUpperCase();
				if (upperCase.length() > 10) {
					upperCase = upperCase.substring(0, 10);
				}
				upperCase = upperCase.replaceAll("[^A-Z0-9]", "");
				controller.panNumberField.setText(upperCase);
			}
		});
		controller.panNumberField.focusedProperty().addListener((obs, oldVal, newVal) -> {
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
		return pan.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}");
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
				(observable, oldValue, newValue) -> {
					if (newValue) {
						controller.onlineRadio.setSelected(false);
					}
				});
		controller.onlineRadio.selectedProperty().addListener(
				(observable, oldValue, newValue) -> {
					if (newValue) {
						controller.cashRadio.setSelected(false);
					}
				});
	}

	public void calenderChecker() {
		controller.sevaDatePicker.getEditor().textProperty().addListener((obs, oldVal, newText) -> {
			if (newText == null || newText.isEmpty()) {
				controller.sevaDatePicker.setValue(LocalDate.now());
			}
		});
	}

	public void threeNakshatraForARashi() {
		controller.nakshatraComboBox.setDisable(true);
		controller.sevaListener.rashiNakshatraMap();
		controller.raashiComboBox.getSelectionModel().selectedItemProperty().addListener(
				(obs, oldVal, newVal) -> {
					if (newVal == null || newVal.equals("ಆಯ್ಕೆ")) {
						controller.nakshatraComboBox.setDisable(true);
						controller.nakshatraComboBox.getItems().clear();
						controller.nakshatraComboBox.getSelectionModel().clearSelection();
						controller.nakshatraComboBox.setPromptText("ನಕ್ಷತ್ರವನ್ನು ಆಯ್ಕೆಮಾಡಿ");
					} else {
						List<String> nakshatrasForRashi = controller.rashiNakshatraMap.get(newVal);
						if (nakshatrasForRashi != null) {
							ObservableList<String> nakshatraItems = FXCollections.observableArrayList(nakshatrasForRashi);
							nakshatraItems.add(0, "ಆಯ್ಕೆ");
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
		totalBinding.addListener((obs, oldVal, newVal) -> {
			checkAndEnforceCashLimit();
		});
		controller.totalLabel.textProperty().bind(Bindings.createStringBinding(() ->
						String.format("₹%.2f", totalBinding.get()),
				totalBinding
		));
	}
}