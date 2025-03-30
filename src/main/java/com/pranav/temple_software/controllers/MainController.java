package com.pranav.temple_software.controllers;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {
	private Map<String, List<String>> rashiNakshatraMap = new HashMap<>();
	@FXML
	private ComboBox<String> sevaComboBox;
	@FXML
	private ComboBox<String> raashiComboBox;
	@FXML
	private ComboBox<String> nakshatraComboBox;
	@FXML
	private ComboBox<String> otherServicesComboBox;
	@FXML
	private ComboBox<String> donationComboBox;
	@FXML
	private TextField donationField;
	@FXML
	private CheckBox donationCheck;
	@FXML
	private RadioButton cashRadio;
	@FXML
	private RadioButton onlineRadio;
	@FXML
	private TextField devoteeNameField;
	@FXML
	private TextField contactField;
	@FXML
	private DatePicker sevaDatePicker;


	@FXML
	public void initialize() {
		sevaDatePicker.setValue(LocalDate.now());
		ObservableList<String> items = FXCollections.observableArrayList(
				"ಆಯ್ಕೆ",
				"1. ಬಲಿವಾಡು",
				"2. ಪಂಚಾಮೃತಾಭಿಷೇಕ ",
				"3. ರುದ್ರಾಭಿಷೇಕ ",
				"4. ಏಕಾದಶ ರುದ್ರಾಭಿಷೇಕ",
				"5. ಕ್ಷೀರಾಭಿಷೇಕ",
				"6. ಅಷ್ಟೋತ್ತರ ಕುಂಕುಮಾರ್ಚನೆ",
				"7. ಸಹಸ್ರನಾಮ ಕುಂಕುಮಾರ್ಚನೆ ",
				"8. ಕಾರ್ತಿಕ ಪೂಜೆ",
				"9. ತ್ರಿಮಧುರ",
				"10. ಪುಷ್ಪಾಂಜಲಿ",
				"11. ಹಣ್ಣುಕಾಯಿ",
				"12. ಶಾಸ್ತಾರ ದೇವರಿಗೆ ಕಾಯಿ",
				"13. ಪಂಚಕಜ್ಜಾಯ",
				"14. ಅಪ್ಪಕಜ್ಜಾಯ (1 ಕುಡ್ತೆ )",
				"15. ಮಂಗಳಾರತಿ",
				"16. ಕರ್ಪೂರಾರತಿ",
				"17. ತುಪ್ಪದ ನಂದಾದೀಪ",
				"18. ಎಳ್ಳೆಣ್ಣೆ ನಂದಾದೀಪ",
				"19. ಒಂದು ದಿನದ ಪೂಜೆ ",
				"20. ಸರ್ವಸೇವೆ ",
				"21. ಗಣಪತಿ ಹವನ",
				"22. ದೂರ್ವಾಹೋಮ ",
				"23. ಶನಿ ಪೂಜೆ",
				"24. ಶನಿ ಜಪ",
				"25. ರಾಹು ಜಪ",
				"26. ತುಲಾಭಾರ ",
				"27. ದೀಪಾರಾಧನೆ ",
				"28. ನೈವೇದ್ಯ ಸಮರ್ಪಣೆ ",
				"29. ಹಾಲು ಪಾಯಸ",
				"30. ಪಿಂಡಿ ಪಾಯಸ",
				"31. ಕಠಿಣ ಪಾಯಸ",
				"32. 2 ಕಾಯಿ ಪಾಯಸ",
				"33. 5 ಕಾಯಿ ಪಾಯಸ",
				"34. ಹೆಸರುಬೇಳೆ ಪಾಯಸ",
				"35. ನಾಗನಿಗೆ ಹಾಲು ಸಮರ್ಪಣೆ",
				"36. ನಾಗ ಪೂಜೆ",
				"37. ನಾಗ ತಂಬಿಲ",
				"38. ಪವಮಾನ ಅಭಿಷೇಕ",
				"39. ಶತ ರುದ್ರಾಭಿಷೇಕ",
				"40. ಆಶ್ಲೇಷ ಬಲಿ",
				"41. ವರಮಹಾಲಕ್ಷ್ಮೀ ಪೂಜೆ"

		);

		ObservableList<String> rashis = FXCollections.observableArrayList(
				"ಆಯ್ಕೆ", "ಮೇಷ", "ವೃಷಭ", "ಮಿಥುನ", "ಕರ್ಕಾಟಕ", "ಸಿಂಹ", "ಕನ್ಯಾ",
				"ತುಲಾ", "ವೃಶ್ಚಿಕ", "ಧನು", "ಮಕರ", "ಕುಂಭ", "ಮೀನ"
		);

		ObservableList<String> nakshatras = FXCollections.observableArrayList(
				"ಆಯ್ಕೆ", "ಅಶ್ವಿನಿ", "ಭರಣಿ", "ಕೃತಿಕ", "ರೋಹಿಣಿ", "ಮೃಗಶಿರ", "ಆರ್ದ್ರ",
				"ಪುನರ್ವಸು", "ಪುಷ್ಯ", "ಆಶ್ಲೇಷ", "ಮಘ", "ಪೂರ್ವ ಫಲ್ಗುಣಿ", "ಉತ್ತರ ಫಲ್ಗುಣಿ",
				"ಹಸ್ತ", "ಚಿತ್ತ", "ಸ್ವಾತಿ", "ವಿಶಾಖ", "ಅನೂರಾಧ", "ಜ್ಯೇಷ್ಠ",
				"ಮೂಲ", "ಪೂರ್ವಾಷಾಢ", "ಉತ್ತರಾಷಾಢ", "ಶ್ರವಣ", "ಧನಿಷ್ಠ", "ಶತಭಿಷ",
				"ಪೂರ್ವ ಭಾದ್ರಪದ", "ಉತ್ತರ ಭಾದ್ರಪದ", "ರೇವತಿ"
		);


		ObservableList<String> donations = FXCollections.observableArrayList(
				"ಸ್ಥಳ ಕಾಣಿಕ",
				"ಪಾತ್ರೆ ಬಾಡಿಗೆ",
				"ವಿದ್ಯುತ್",
				"ಜನರೇಟರ್", "ಕಟ್ಟಿಗೆ", "ತೆಂಗಿನಕಾಯಿ", "ಅರ್ಚಕರ ದಕ್ಷಿಣೆ", "ಅಡಿಗೆಯವರಿಗೆ", "ಕೂಲಿ", "ಊಟೋಪಚಾರದ ಬಗ್ಗೆ", "ಇತರ ಖರ್ಚಿನ ಬಾಬ್ತು"
		);

		ObservableList<String> otherSevaReciepts = FXCollections.observableArrayList(
				"ಶತ ರುದ್ರಾಭಿಷೇಕ",
				"ಸಾಮೂಹಿಕ ಆಶ್ಲೇಷ ಬಲಿ",
				"ಶ್ರೀಕೃಷ್ಣ ಜನ್ಮಾಷ್ಟಮಿ",
				"ವರಮಹಾಲಕ್ಷ್ಮೀ  ಪೂಜೆ",
				"ಪ್ರತಿಷ್ಠಾ ದಿನ (ಕಳಭ)",
				"ಸಮಾಜ ಸೇವಾ ಕಾರ್ಯಗಳು",
				"ನಿತ್ಯ-ನೈಮಿತ್ತಿಕ ಕಾರ್ಯಗಳು",
				"ಜೀರ್ಣೋದ್ಧಾರ ಕಾರ್ಯಗಳು",
				"ಅಭಿವೃದ್ಧಿ ಕಾರ್ಯಗಳು",
				"ಅನ್ನದಾನ"
		);


		sevaComboBox.setItems(items);
		raashiComboBox.setItems(rashis);
		otherServicesComboBox.setItems(otherSevaReciepts);
		donationComboBox.setItems(donations);

		//This below is to select only the 3 nakshatras for a given raashi
		nakshatraComboBox.setDisable(true);
		initializeRashiNakshatraMap();
		raashiComboBox.getSelectionModel().selectedItemProperty().addListener(
				(obs, oldVal, newVal) -> {
			if (newVal == null || newVal.equals("ಆಯ್ಕೆ")) {
				nakshatraComboBox.setDisable(true);
				nakshatraComboBox.getItems().clear();
				nakshatraComboBox.getSelectionModel().clearSelection();
				nakshatraComboBox.setPromptText("ನಕ್ಷತ್ರವನ್ನು ಆಯ್ಕೆಮಾಡಿ");
			} else {
				List<String> nakshatrasForRashi = rashiNakshatraMap.get(newVal);
				if (nakshatrasForRashi != null) {
					ObservableList<String> nakshatraItems = FXCollections.observableArrayList(nakshatrasForRashi);
					nakshatraItems.add(0, "ಆಯ್ಕೆ");
					nakshatraComboBox.setItems(nakshatraItems);
					nakshatraComboBox.setDisable(false);
					nakshatraComboBox.setPromptText("ನಕ್ಷತ್ರವನ್ನು ಆಯ್ಕೆಮಾಡಿ");
				}
			}
		});


		//This is the code to disable all donation fields till the checkbox is not checked.
		{
			// Initially disable donation field and combo box
			donationField.setDisable(true);
			donationComboBox.setDisable(true);

			// Add listener to donation checkbox
			donationCheck.selectedProperty().addListener(
					(observable, oldValue, newValue) -> {
						// Enable/disable donation field and combo box based on checkbox state
						donationField.setDisable(!newValue);
						donationComboBox.setDisable(!newValue);
					});
		}


		//This is the code to check only one of the radio buttons in cash and online
		{
			// Set up the checkboxes to act like radio buttons
			cashRadio.selectedProperty().addListener(
					(observable, oldValue, newValue) -> {
						if (newValue) {
							onlineRadio.setSelected(false);
						}
					});

			onlineRadio.selectedProperty().addListener(
					(observable, oldValue, newValue) -> {
						if (newValue) {
							cashRadio.setSelected(false);
						}
					});
		}


		// Disable past dates in calendar
		sevaDatePicker.setDayCellFactory(picker -> new DateCell() {
			@Override
			public void updateItem(LocalDate date, boolean empty) {
				super.updateItem(date, empty);
				LocalDate today = LocalDate.now();
				setDisable(date.isBefore(today));
				if (date.isBefore(today)) {
					setStyle("-fx-text-fill: #d3d3d3;"); // Gray out past dates
				}
			}
		});

		// Prevent manual entry of past dates
		sevaDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null && newVal.isBefore(LocalDate.now())) {
				sevaDatePicker.setValue(LocalDate.now());
			}
		});

		// Force today's date if field is left empty or invalid
		sevaDatePicker.getEditor().textProperty().addListener((obs, oldVal, newText) -> {
			if (newText == null || newText.isEmpty()) {
				sevaDatePicker.setValue(LocalDate.now());
			}
		});

		// Final validation on focus loss
		sevaDatePicker.focusedProperty().addListener((obs, oldVal, hasFocus) -> {
			if (!hasFocus) {
				LocalDate currentDate = sevaDatePicker.getValue();
				if (currentDate == null || currentDate.isBefore(LocalDate.now())) {
					sevaDatePicker.setValue(LocalDate.now());
				}
			}
		});


		setupNameValidation();
		setupPhoneValidation();
		setupAmountValidation();

	}


	private void initializeRashiNakshatraMap() {
		// Populate the Rashi-Nakshatra mapping (adjust according to your data)
		rashiNakshatraMap.put("ಮೇಷ", Arrays.asList("ಅಶ್ವಿನಿ", "ಭರಣಿ", "ಕೃತಿಕ"));
		rashiNakshatraMap.put("ವೃಷಭ", Arrays.asList("ರೋಹಿಣಿ", "ಮೃಗಶಿರ", "ಆರ್ದ್ರ"));
		rashiNakshatraMap.put("ಮಿಥುನ", Arrays.asList("ಪುನರ್ವಸು", "ಪುಷ್ಯ", "ಆಶ್ಲೇಷ"));
		rashiNakshatraMap.put("ಕರ್ಕಾಟಕ", Arrays.asList("ಮಘ", "ಪೂರ್ವ ಫಲ್ಗುಣಿ", "ಉತ್ತರ ಫಲ್ಗುಣಿ"));
		rashiNakshatraMap.put("ಸಿಂಹ", Arrays.asList("ಹಸ್ತ", "ಚಿತ್ತ", "ಸ್ವಾತಿ"));
		rashiNakshatraMap.put("ಕನ್ಯಾ", Arrays.asList("ವಿಶಾಖ", "ಅನೂರಾಧ", "ಜ್ಯೇಷ್ಠ"));
		rashiNakshatraMap.put("ತುಲಾ", Arrays.asList("ಮೂಲ", "ಪೂರ್ವಾಷಾಢ", "ಉತ್ತರಾಷಾಢ"));
		rashiNakshatraMap.put("ವೃಶ್ಚಿಕ", Arrays.asList("ಶ್ರವಣ", "ಧನಿಷ್ಠ", "ಶತಭಿಷ"));
		rashiNakshatraMap.put("ಧನು", Arrays.asList("ಪೂರ್ವ ಭಾದ್ರಪದ", "ಉತ್ತರ ಭಾದ್ರಪದ", "ರೇವತಿ"));
		rashiNakshatraMap.put("ಮಕರ", Arrays.asList("ಧನಿಷ್ಠ", "ಶತಭಿಷ", "ಪೂರ್ವ ಭಾದ್ರಪದ")); // Example
		rashiNakshatraMap.put("ಕುಂಭ", Arrays.asList("ಉತ್ತರ ಭಾದ್ರಪದ", "ರೇವತಿ", "ಅಶ್ವಿನಿ")); // Example
		rashiNakshatraMap.put("ಮೀನ", Arrays.asList("ರೇವತಿ", "ಅಶ್ವಿನಿ", "ಭರಣಿ")); // Example
	}


		//This code implements what values the text boxes should accept.
		private void setupNameValidation() {
			// Create a TextFormatter with a filter to allow only letters and spaces
			TextFormatter<String> formatter = new TextFormatter<>(change -> {
				String newText = change.getControlNewText();
				// Allow empty input or strings containing only letters/spaces
				if (newText.matches("[\\p{L} ]*")) {
					return change; // Accept the change
				} else {
					return null; // Reject the change
				}
			});
			devoteeNameField.setTextFormatter(formatter);
		}


	private void setupPhoneValidation() {
		contactField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				// Allow only digits and limit to 10 characters
				if (!newValue.matches("\\d*")) {
					contactField.setText(newValue.replaceAll("[^\\d]", ""));
				}
				if (newValue.length() > 10) {
					contactField.setText(newValue.substring(0, 10));
				}
			}
		});
	}

	private void setupAmountValidation() {
		donationField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null && !newValue.matches("\\d*(\\.\\d*)?")) {
				// Allow only digits and at most one decimal point
				donationField.setText(oldValue);
			}
		});
	}
	//Till this, the code will restrict the input of the text fields




}

