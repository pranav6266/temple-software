package com.pranav.temple_software.controllers.menuControllers.KaryakramaManager;

import com.pranav.temple_software.Launcher;
import com.pranav.temple_software.models.Karyakrama;
import com.pranav.temple_software.repositories.KaryakramaRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class KaryakramaManagerController {

	@FXML private TableView<Karyakrama> karyakramaTable;
	@FXML private TableColumn<Karyakrama, String> nameColumn;

	private final KaryakramaRepository repository = new KaryakramaRepository();
	private final ObservableList<Karyakrama> karyakramaList = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		karyakramaTable.setItems(karyakramaList);
		loadKaryakramas();
	}

	private void loadKaryakramas() {
		karyakramaList.setAll(repository.getAllKaryakramagalu());
	}

	@FXML
	private void handleAddKaryakrama() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Add New Karyakrama");
		dialog.setHeaderText("Enter the name for the new event.");
		dialog.setContentText("Name:");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(name -> {
			if (name.trim().isEmpty()) {
				showAlert(Alert.AlertType.ERROR, "Input Error", "Karyakrama name cannot be empty.");
				return;
			}
			if (repository.addKaryakrama(name)) {
				loadKaryakramas();
			} else {
				showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add the new Karyakrama.");
			}
		});
	}

	@FXML
	private void handleEditKaryakrama() {
		Karyakrama selected = karyakramaTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a Karyakrama to edit.");
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("/fxml/MenuViews/KaryakramaManager/KaryakramaEditView.fxml"));
			Stage stage = new Stage();
			stage.setTitle("Edit Karyakrama and Sevas");
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(karyakramaTable.getScene().getWindow());
			stage.setScene(new Scene(loader.load()));

			KaryakramaEditController controller = loader.getController();
			controller.initData(selected);

			stage.showAndWait();
			loadKaryakramas(); // Refresh the list after the edit window is closed
		} catch (IOException e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to open the edit window.");
		}
	}

	@FXML
	private void handleDeleteKaryakrama() {
		Karyakrama selected = karyakramaTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a Karyakrama to delete.");
			return;
		}

		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
				"Are you sure you want to delete '" + selected.getName() + "'? This will also delete all sevas associated with it. This action cannot be undone.",
				ButtonType.YES, ButtonType.CANCEL);
		confirmation.setHeaderText("Confirm Deletion");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.YES) {
			if (repository.deleteKaryakrama(selected.getId())) {
				loadKaryakramas();
			} else {
				showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete the Karyakrama.");
			}
		}
	}

	@FXML
	private void closeWindow() {
		Stage stage = (Stage) karyakramaTable.getScene().getWindow();
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