package com.pranav.temple_software.controllers.menuControllers.KaryakramaManager;

import com.pranav.temple_software.models.Karyakrama;
import com.pranav.temple_software.models.KaryakramaSeva;
import com.pranav.temple_software.repositories.KaryakramaRepository;
import com.pranav.temple_software.repositories.KaryakramaSevaRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class KaryakramaEditController {

	@FXML private Label titleLabel;
	@FXML private TextField karyakramaNameField;
	@FXML private TableView<KaryakramaSeva> sevasTable;
	@FXML private TableColumn<KaryakramaSeva, String> sevaNameColumn;
	@FXML private TableColumn<KaryakramaSeva, Double> sevaAmountColumn;

	private Karyakrama currentKaryakrama;
	private final KaryakramaRepository karyakramaRepository = new KaryakramaRepository();
	private final KaryakramaSevaRepository sevaRepository = new KaryakramaSevaRepository();
	private final ObservableList<KaryakramaSeva> sevasList = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		sevaNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		sevaAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
		sevasTable.setItems(sevasList);
	}

	public void initData(Karyakrama karyakrama) {
		this.currentKaryakrama = karyakrama;
		titleLabel.setText("Editing: " + karyakrama.getName());
		karyakramaNameField.setText(karyakrama.getName());
		loadSevas();
	}

	private void loadSevas() {
		sevasList.setAll(sevaRepository.getSevasForKaryakrama(currentKaryakrama.getId()));
	}

	@FXML
	private void handleAddSeva() {
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("Add New Seva");
		dialog.setHeaderText("Enter details for the new seva for '" + currentKaryakrama.getName() + "'");

		ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		TextField nameField = new TextField();
		nameField.setPromptText("Seva Name");
		TextField amountField = new TextField();
		amountField.setPromptText("Amount");

		grid.add(new Label("Name:"), 0, 0);
		grid.add(nameField, 1, 0);
		grid.add(new Label("Amount:"), 0, 1);
		grid.add(amountField, 1, 1);
		dialog.getDialogPane().setContent(grid);

		Platform.runLater(nameField::requestFocus);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == saveButtonType) {
				return new Pair<>(nameField.getText(), amountField.getText());
			}
			return null;
		});

		Optional<Pair<String, String>> result = dialog.showAndWait();
		result.ifPresent(details -> {
			try {
				String name = details.getKey();
				double amount = Double.parseDouble(details.getValue());
				if (name.trim().isEmpty() || amount < 0) {
					throw new NumberFormatException();
				}
				if (sevaRepository.addSevaToKaryakrama(currentKaryakrama.getId(), name, amount)) {
					loadSevas();
				} else {
					showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add seva.");
				}
			} catch (NumberFormatException e) {
				showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid name and a non-negative amount.");
			}
		});
	}

	@FXML
	private void handleDeleteSeva() {
		KaryakramaSeva selected = sevasTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a seva to delete.");
			return;
		}
		if (sevaRepository.deleteSeva(selected.getId())) {
			loadSevas();
		} else {
			showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete seva.");
		}
	}

	@FXML
	private void handleSaveAll() {
		String newKaryakramaName = karyakramaNameField.getText().trim();
		if (newKaryakramaName.isEmpty()) {
			showAlert(Alert.AlertType.ERROR, "Input Error", "Karyakrama name cannot be empty.");
			return;
		}

		if (!newKaryakramaName.equals(currentKaryakrama.getName())) {
			karyakramaRepository.updateKaryakrama(currentKaryakrama.getId(), newKaryakramaName);
		}

		closeWindow();
	}

	@FXML
	private void closeWindow() {
		Stage stage = (Stage) karyakramaNameField.getScene().getWindow();
		stage.close();
	}

	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}