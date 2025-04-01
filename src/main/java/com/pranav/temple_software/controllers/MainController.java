package com.pranav.temple_software.controllers;


import com.pranav.temple_software.models.ReceiptData;
import com.pranav.temple_software.utils.ReceiptPrinter;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.*;

public class MainController {
	private Map<String, List<String>> rashiNakshatraMap = new HashMap<>();

	private ReceiptPrinter receiptPrinter = new ReceiptPrinter();
	private Stage mainStage;
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
	private VBox sevaCheckboxContainer;
	@FXML
	private TableView<SevaEntry> sevaTableView;
	@FXML
	private TableColumn<SevaEntry, String> slNoColumn;
	@FXML
	private TableColumn<SevaEntry, String> sevaNameColumn;
	@FXML private Button addDonationButton;
	@FXML private Button addDonationButton1;
	@FXML Label totalLabel;
	@FXML
	private Button printPreviewButton;

	private final Map<String, CheckBox> sevaCheckboxMap = new HashMap<>();
	private ObservableList<SevaEntry> selectedSevas = FXCollections.observableArrayList();

	public void setMainStage(Stage stage) {
		this.mainStage = stage;
	}



	private void handlePrintPreview() {
		// 1. Gather Data
		String devoteeName = devoteeNameField.getText();
		String phone = contactField.getText();
		LocalDate date = sevaDatePicker.getValue();
		// Ensure selectedSevas list is up-to-date (it should be based on your existing code)
		ObservableList<SevaEntry> currentSevas = FXCollections.observableArrayList(sevaTableView.getItems());

		// Get total amount (parse from label or recalculate)
		double total = 0.0;
		try {
			// Assuming totalLabel text is like "₹123.45"
			String totalText = totalLabel.getText().replaceAll("[^\\d.]", "");
			total = Double.parseDouble(totalText);
		} catch (NumberFormatException | NullPointerException ex) {
			// Fallback: Recalculate if label parsing fails
			total = currentSevas.stream().mapToDouble(SevaEntry::getAmount).sum();
			System.err.println("Could not parse total from label, recalculating.");
		}


		// Basic Validation (Add more as needed)
		List<String> errors = new ArrayList<>();

		if (devoteeNameField.getText() == null || devoteeNameField.getText().trim().isEmpty()) {
			errors.add("Please enter devotee name");
		}

		if (sevaDatePicker.getValue() == null) {
			errors.add("Please select a seva date");
		}

		if (selectedSevas.isEmpty()) {
			errors.add("Please add at least one seva or donation");
		}

		// New radio button validation
		if (!cashRadio.isSelected() && !onlineRadio.isSelected()) {
			errors.add("Please select payment mode (Cash/Online)");
		}

		// Phone number validation (only if not empty)
		String phoneNumber = contactField.getText();
		if (phoneNumber != null && !phoneNumber.isEmpty() && phoneNumber.length() < 10) {
			errors.add("Phone number must contain at least 10 digits");
		}

		if (!errors.isEmpty()) {
			showAlert("Validation Error", String.join("\n", errors));
			return;
		}


		// 2. Create ReceiptData object
		ReceiptData receiptData = new ReceiptData(devoteeName, phoneNumber, date, currentSevas, total);

		// 3. Call the preview method from ReceiptPrinter
		receiptPrinter.showPrintPreview(receiptData, mainStage); // Pass mainStage as owner
	}
	public static class SevaEntry {
		private final StringProperty name;
		private final DoubleProperty amount;

		public SevaEntry( String name, double amount) {
			this.name = new SimpleStringProperty(name);
			this.amount = new SimpleDoubleProperty(amount);
		}

		// Getters and property methods
		public String getName() { return name.get(); }
		public double getAmount() { return amount.get(); }
		public StringProperty nameProperty() { return name; }
		public DoubleProperty amountProperty() { return amount; }
	}



		public class Seva {
			private final String id;
			private final String name;
			private final double amount;

			public Seva(String id, String name, double amount) {
				this.id = id;
				this.name = name;
				this.amount = amount;
			}

			// Getters
			public String getId() {
				return id;
			}

			public String getName() {
				return name;
			}

			public double getAmount() {
				return amount;
			}
		}


	private void handleAddDonation() {
		String donationType = donationComboBox.getValue();
		String amountText = donationField.getText();

		if (donationType == null || donationType.equals("ಆಯ್ಕೆ") || amountText.isEmpty()) {
			showAlert("Invalid Input", "Please select a donation type and enter amount");
			return;
		}

		try {
			double amount = Double.parseDouble(amountText);
			if (amount <= 0) throw new NumberFormatException();

			String entryName = "ದೇಣಿಗೆ : " + donationType;

			// Find existing donation entry
			Optional<SevaEntry> existingEntry = selectedSevas.stream()
					.filter(entry -> entry.getName().equals(entryName))
					.findFirst();

			if (existingEntry.isPresent()) {
				// Update existing amount
				existingEntry.get().amountProperty().set(amount);
			} else {
				// Add new entry
				selectedSevas.add(new SevaEntry(entryName, amount));
			}

			donationField.clear();
		} catch (NumberFormatException ex) {
			showAlert("Invalid Amount", "Please enter a valid positive number");
		}
	}

	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}


	private void handleAddOtherSeva() {
		String sevaType = otherServicesComboBox.getValue();

		if (sevaType == null || sevaType.equals("ಆಯ್ಕೆ") || sevaType.isEmpty()) {
			showAlert("Invalid Input", "Please select an other service type");
			return;
		}

		// Check for existing service
		String entryName = "ಇತರೆ ಸೇವೆಗಳು : " + sevaType;
		boolean exists = selectedSevas.stream()
				.anyMatch(entry -> entry.getName().equals(entryName));

		if (exists) {
			showAlert("Duplicate Service", "This service already exists in the list");
			return;
		}

		selectedSevas.add(new SevaEntry(entryName, 0.00));
	}



	private void initializeTotalCalculation() {
		// Create binding for total amount
		DoubleBinding totalBinding = Bindings.createDoubleBinding(() ->
						selectedSevas.stream()
								.mapToDouble(SevaEntry::getAmount)
								.sum(),
				selectedSevas
		);

		// Update total label with currency format
		totalLabel.textProperty().bind(Bindings.createStringBinding(() ->
						String.format("₹%.2f", totalBinding.get()),
				totalBinding
		));
	}

	private void validatePhoneNumber() {
		String phone = contactField.getText();
		if (phone != null && !phone.isEmpty() && phone.length() < 10) {
			showAlert("Invalid Phone Number", "Phone number must contain at least 10 digits");
		}
	}



	@FXML
	public void initialize() {
		sevaDatePicker.setValue(LocalDate.now());
		sevaNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		sevaTableView.setItems(selectedSevas);
		setupTableView();
		initializeTotalCalculation();


		printPreviewButton.setOnAction(e -> handlePrintPreview());

		addDonationButton1.setOnAction(e -> handleAddOtherSeva());

		donationField.setDisable(true);
		donationComboBox.setDisable(true);
		addDonationButton.setDisable(true);


		// Add phone number validation on focus loss
		contactField.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal) { // When focus is lost
				validatePhoneNumber();
			}
		});

		// Add listener to selectedSevas to sync CheckBox states
		selectedSevas.addListener((ListChangeListener<SevaEntry>) change -> {
			while (change.next()) {
				if (change.wasRemoved()) {
					for (SevaEntry removedEntry : change.getRemoved()) {
						// Find the Seva ID from the removed entry's name
						sevaMap.entrySet().stream()
								.filter(entry -> entry.getValue().getName().equals(removedEntry.getName()))
								.findFirst()
								.ifPresent(entry -> {
									String sevaId = entry.getKey();
									CheckBox checkBox = sevaCheckboxMap.get(sevaId);
									if (checkBox != null) {
										checkBox.setSelected(false);
									}
								});
					}
				}
			}
		});

		//Donation checkbox listener to put it inside the table view
		donationCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
			donationField.setDisable(!newVal);
			donationComboBox.setDisable(!newVal);
			addDonationButton.setDisable(!newVal);
		});

		// Add Donation button handler
		addDonationButton.setOnAction(e -> handleAddDonation());



		// Add a new TableColumn for actions
		TableColumn<SevaEntry, Void> actionColumn = new TableColumn<>("Action");
		actionColumn.setCellFactory(col -> new TableCell<>() {
			private final Button removeButton = new Button("Remove");

			{
				removeButton.setOnAction(event -> {
					SevaEntry entry = getTableView().getItems().get(getIndex());
					selectedSevas.remove(entry);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				setGraphic(empty ? null : removeButton);
			}
		});

		// Add the column to your TableView
		sevaTableView.getColumns().add(actionColumn);

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
				"ಆಯ್ಕೆ",
				"ಸ್ಥಳ ಕಾಣಿಕ",
				"ಪಾತ್ರೆ ಬಾಡಿಗೆ",
				"ವಿದ್ಯುತ್",
				"ಜನರೇಟರ್", "ಕಟ್ಟಿಗೆ", "ತೆಂಗಿನಕಾಯಿ", "ಅರ್ಚಕರ ದಕ್ಷಿಣೆ", "ಅಡಿಗೆಯವರಿಗೆ", "ಕೂಲಿ", "ಊಟೋಪಚಾರದ ಬಗ್ಗೆ", "ಇತರ ಖರ್ಚಿನ ಬಾಬ್ತು"
		);

		ObservableList<String> otherSevaReciepts = FXCollections.observableArrayList(
				"ಆಯ್ಕೆ",
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


		setupSevaCheckboxes();
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



		// Force today's date if field is left empty or invalid
		sevaDatePicker.getEditor().textProperty().addListener((obs, oldVal, newText) -> {
			if (newText == null || newText.isEmpty()) {
				sevaDatePicker.setValue(LocalDate.now());
			}
		});



		setupNameValidation();
		setupPhoneValidation();
		setupAmountValidation();

	}

	private void setupTableView() {
		// Serial number column
		slNoColumn.setCellFactory(col -> new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? null : String.valueOf(getIndex() + 1));
				setAlignment(Pos.CENTER);
			}
		});

		// Seva name column
		sevaNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());


		// Amount column
		TableColumn<SevaEntry, Number> amountColumn = (TableColumn<SevaEntry, Number>) sevaTableView.getColumns().get(2);
		amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty());

		// Format amount as currency
		amountColumn.setCellFactory(tc -> new TableCell<>() {
			@Override
			protected void updateItem(Number amount, boolean empty) {
				super.updateItem(amount, empty);
				if (empty || amount == null) {
					setText(null);
				} else {
					setText(String.format("₹%.2f", amount.doubleValue()));
					setAlignment(Pos.CENTER_RIGHT);
				}
			}
		});

	}

	private TableColumn<SevaEntry, Void> getSevaEntryVoidTableColumn() {
		TableColumn<SevaEntry, Void> actionColumn = new TableColumn<>("Action");
		actionColumn.setCellFactory(param -> new TableCell<>() {
			private final Button removeButton = new Button("Remove");

			{
				removeButton.setOnAction(event -> {
					SevaEntry entry = getTableView().getItems().get(getIndex());
					selectedSevas.remove(entry);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				setGraphic(empty ? null : removeButton);
			}
		});
		return actionColumn;
	}


	private void setupSevaCheckboxes() {
		initializeSevaData();
		sevaCheckboxMap.clear(); // Clear existing entries

		for (Seva seva : sevaMap.values()) {
			CheckBox checkBox = new CheckBox(seva.getId() + ". " + seva.getName());
			checkBox.getStyleClass().add("seva-checkbox");
			String sevaId = seva.getId(); // Unique identifier for the Seva

			// Add CheckBox to the map
			sevaCheckboxMap.put(sevaId, checkBox);

			{// Initialize CheckBox state based on selectedSevas
			boolean isSelected = selectedSevas.stream()
					.anyMatch(entry -> entry.getName().equals(seva.getName()));
			checkBox.setSelected(isSelected);
			}

			// Update selectedSevas when CheckBox is toggled
			checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
				if (isSelected) {
					selectedSevas.add(new SevaEntry(seva.getName(), seva.getAmount()));
				} else {
					selectedSevas.removeIf(entry ->
							entry.getName().equals(seva.getName()) &&
									entry.getAmount() == seva.getAmount()
					);
				}
			});

			sevaCheckboxContainer.getChildren().add(checkBox);
		}
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
				} else if (newValue.length() < 10) {
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


	private final Map<String, Seva> sevaMap = new LinkedHashMap<>();

	private void initializeSevaData() {
		// First 19 sevas (using first image amounts)
		double[] first19 = {
				50.00, 30.00, 50.00, 1000.00, 20.00, 10.00, 200.00, 50.00, 30.00,
				30.00, 25.00, 20.00, 100.00, 20.00, 20.00, 100.00, 50.00, 300.00
		};

		// Second 19 sevas (using second image amounts)
		double[] second18 = {
				200.00, 250.00, 350.00, 300.00, 250.00, 250.00, 100.00, 1000.00,
				100.00, 50.00, 100.00, 125.00, 250.00, 400.00, 400.00, 30.00,
				200.00, 300.00, 500.00
		};

		// First 19 sevas (items 1-19)
		sevaMap.put("1", new Seva("1", "ಬಲಿವಾಡು", first19[0]));
		sevaMap.put("2", new Seva("2", "ಪಂಚಾಮೃತಾಭಿಷೇಕ", first19[1]));
		sevaMap.put("3", new Seva("3", "ರುದ್ರಾಭಿಷೇಕ", first19[2]));
		sevaMap.put("4", new Seva("4", "ಏಕಾದಶ ರುದ್ರಾಭಿಷೇಕ", first19[3]));
		sevaMap.put("5", new Seva("5", "ಕ್ಷೀರಾಭಿಷೇಕ", first19[4]));
		sevaMap.put("6", new Seva("6", "ಅಷ್ಟೋತ್ತರ ಕುಂಕುಮಾರ್ಚನೆ", first19[5]));
		sevaMap.put("7", new Seva("7", "ಸಹಸ್ರನಾಮ ಕುಂಕುಮಾರ್ಚನೆ", first19[6]));
		sevaMap.put("8", new Seva("8", "ಕಾರ್ತಿಕ ಪೂಜೆ", first19[7]));
		sevaMap.put("9", new Seva("9", "ತ್ರಿಮಧುರ", first19[8]));
		sevaMap.put("10", new Seva("10", "ಪುಷ್ಪಾಂಜಲಿ", first19[9]));
		sevaMap.put("11", new Seva("11", "ಹಣ್ಣುಕಾಯಿ", first19[10]));
		sevaMap.put("12", new Seva("12", "ಶಾಸ್ತಾರ ದೇವರಿಗೆ ಕಾಯಿ", first19[11]));
		sevaMap.put("13", new Seva("13", "ಪಂಚಕಜ್ಜಾಯ", first19[12]));
		sevaMap.put("14", new Seva("14", "ಅಪ್ಪಕಜ್ಜಾಯ (1 ಕುಡ್ತೆ )", first19[13]));
		sevaMap.put("15", new Seva("15", "ಮಂಗಳಾರತಿ", first19[14]));
		sevaMap.put("16", new Seva("16", "ಕರ್ಪೂರಾರತಿ", first19[15]));
		sevaMap.put("17", new Seva("17", "ತುಪ್ಪದ ನಂದಾದೀಪ", first19[16]));
		sevaMap.put("18", new Seva("18", "ಎಳ್ಳೆಣ್ಣೆ ನಂದಾದೀಪ", first19[17]));

		// Last seva from first image (19th)
		sevaMap.put("19", new Seva("19", "ಒಂದು ದಿನದ ಪೂಜೆ", 300)); // Using last value from first image

		// Next 19 sevas (items 20-38)
		sevaMap.put("20", new Seva("20", "ಸರ್ವಸೇವೆ", second18[0]));
		sevaMap.put("21", new Seva("21", "ಗಣಪತಿ ಹವನ", second18[1]));
		sevaMap.put("22", new Seva("22", "ದೂರ್ವಾಹೋಮ", second18[2]));
		sevaMap.put("23", new Seva("23", "ಶನಿ ಪೂಜೆ", second18[3]));
		sevaMap.put("24", new Seva("24", "ಶನಿ ಜಪ", second18[4]));
		sevaMap.put("25", new Seva("25", "ರಾಹು ಜಪ", second18[5]));
		sevaMap.put("26", new Seva("26", "ತುಲಾಭಾರ", second18[6]));
		sevaMap.put("27", new Seva("27", "ದೀಪಾರಾಧನೆ", second18[7]));
		sevaMap.put("28", new Seva("28", "ನೈವೇದ್ಯ ಸಮರ್ಪಣೆ", second18[8]));
		sevaMap.put("29", new Seva("29", "ಹಾಲು ಪಾಯಸ", second18[9]));
		sevaMap.put("30", new Seva("30", "ಪಿಂಡಿ ಪಾಯಸ", second18[10]));
		sevaMap.put("31", new Seva("31", "ಕಠಿಣ ಪಾಯಸ", second18[11]));
		sevaMap.put("32", new Seva("32", "2 ಕಾಯಿ ಪಾಯಸ", second18[12]));
		sevaMap.put("33", new Seva("33", "5 ಕಾಯಿ ಪಾಯಸ", second18[13]));
		sevaMap.put("34", new Seva("34", "ಹೆಸರುಬೇಳೆ ಪಾಯಸ", second18[14]));
		sevaMap.put("35", new Seva("35", "ನಾಗನಿಗೆ ಹಾಲು ಸಮರ್ಪಣೆ", second18[15]));
		sevaMap.put("36", new Seva("36", "ನಾಗ ಪೂಜೆ", second18[16]));
		sevaMap.put("37", new Seva("37", "ನಾಗ ತಂಬಿಲ", second18[17]));
		sevaMap.put("38", new Seva("38", "ಪವಮಾನ ಅಭಿಷೇಕ", second18[18]));
	}
}

