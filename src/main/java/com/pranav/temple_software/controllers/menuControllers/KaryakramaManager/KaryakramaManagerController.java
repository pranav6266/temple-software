// FILE: src/main/java/com/pranav/temple_software/controllers/menuControllers/KaryakramaManager/KaryakramaManagerController.java
package com.pranav.temple_software.controllers.menuControllers.KaryakramaManager;

import com.pranav.temple_software.controllers.menuControllers.BaseManagerController;
import com.pranav.temple_software.models.Karyakrama;
import com.pranav.temple_software.repositories.KaryakramaRepository;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.Optional;

public class KaryakramaManagerController extends BaseManagerController<Karyakrama> {

	@FXML private TableView<Karyakrama> karyakramaTable;
	@FXML private TableColumn<Karyakrama, Integer> slNoColumn;
	@FXML private TableColumn<Karyakrama, String> nameColumn;

	private final KaryakramaRepository repository = new KaryakramaRepository();

	@FXML
	@Override
	public void initialize() {
		super.initialize();
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		slNoColumn.setCellFactory(_ -> new TableCell<>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? null : String.valueOf(getIndex() + 1));
			}
		});
		refreshGridPane();
	}

	@Override
	protected void loadData() {
		tempItemList.setAll(repository.getAllKaryakramagalu());
	}

	@Override
	protected void storeOriginalState() {
		originalState.clear();
		for (Karyakrama item : tempItemList) {
			originalState.put(String.valueOf(item.getId()), new Karyakrama(item.getId(), item.getName(), item.isActive()));
		}
	}

	@Override
	protected String getItemId(Karyakrama item) {
		return String.valueOf(item.getId());
	}

	@Override
	protected void refreshGridPane() {
		karyakramaTable.setItems(FXCollections.observableArrayList(tempItemList));
		karyakramaTable.refresh();
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
			tempItemList.add(new Karyakrama(-1, name, true));
			refreshGridPane();
		});
	}

	@FXML
	private void handleDeleteKaryakrama() {
		Karyakrama selected = karyakramaTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a Karyakrama to delete.");
			return;
		}

		if (selected.getId() != -1) {
			itemsMarkedForDeletion.add(selected);
		}
		tempItemList.remove(selected);
		refreshGridPane();
	}

	@FXML
	@Override
	public void handleSave(ActionEvent event) {
		for (Karyakrama itemToDelete : itemsMarkedForDeletion) {
			repository.deleteKaryakrama(itemToDelete.getId());
		}

		for (Karyakrama item : tempItemList) {
			if (item.getId() == -1) {
				repository.addKaryakrama(item.getName());
			}
		}
		showAlert(Alert.AlertType.INFORMATION, "Success", "All changes have been saved.");
		closeWindow();
	}

	// Unused abstract methods from BaseManagerController
	@Override
	@FXML
	protected void openAddPopup(ActionEvent event) { handleAddKaryakrama(); }
	@Override
	@FXML
	protected void openEditPopup(ActionEvent event) { /* No Edit functionality */ }
	@Override
	@FXML
	protected void openDeletePopup(ActionEvent event) { handleDeleteKaryakrama(); }
}