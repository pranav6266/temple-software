// FILE: src/main/java/com/pranav/temple_software/controllers/menuControllers/KaryakramaManager/OthersManagerController.java
package com.pranav.temple_software.controllers.menuControllers.KaryakramaManager;

import com.pranav.temple_software.controllers.menuControllers.BaseManagerController;
import com.pranav.temple_software.models.Others;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.OthersRepository;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OthersManagerController extends BaseManagerController<Others> {

	@FXML private TableView<Others> othersTable;
	@FXML private TableColumn<Others, Integer> slNoColumn;
	@FXML private TableColumn<Others, String> nameColumn;

	private final OthersRepository repository = OthersRepository.getInstance();

	@FXML
	@Override
	public void initialize() {
		super.initialize();
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		slNoColumn.setCellFactory(col -> new TableCell<>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? null : String.valueOf(getIndex() + 1));
			}
		});
		refreshGridPane();
	}

	@Override
	protected String getItemId(SevaEntry item) {
		return "";
	}

	@Override
	protected String getItemName(SevaEntry item) {
		return "";
	}

	@Override
	protected void loadData() {
		repository.loadOthersFromDB();
		tempItemList.setAll(repository.getAllOthers());
	}

	@Override
	protected void storeOriginalState() {
		originalState.clear();
		for(Others item : tempItemList) {
			originalState.put(String.valueOf(item.getId()), new Others(item.getId(), item.getName(), item.getDisplayOrder()));
		}
	}

	@Override
	protected String getItemId(Others item) {
		return String.valueOf(item.getId());
	}

	@Override
	protected String getItemName(Others item) {
		return item.getName();
	}

	@Override
	protected void refreshGridPane() {
		othersTable.setItems(FXCollections.observableArrayList(tempItemList));
		othersTable.refresh();
	}

	@FXML
	private void handleAdd() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Add New 'Other' Item");
		dialog.setHeaderText("Enter the name for the new item.");
		dialog.setContentText("Name:");
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(name -> {
			if (name.trim().isEmpty()) {
				showAlert(Alert.AlertType.ERROR, "Input Error", "Name cannot be empty.");
				return;
			}
			tempItemList.add(new Others(-1, name, 0));
			refreshGridPane();
		});
	}

	@FXML
	private void handleDelete() {
		Others selected = othersTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item to delete.");
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
		StringBuilder summary = new StringBuilder("Changes saved successfully.");
		List<String> deletionFailures = new ArrayList<>();

		for (Others itemToDelete : itemsMarkedForDeletion) {
			boolean success = repository.deleteOtherFromDB(itemToDelete.getId());
			if (!success) {
				deletionFailures.add(itemToDelete.getName());
			}
		}

		for (Others item : tempItemList) {
			if (item.getId() == -1) {
				repository.addOtherToDB(item.getName());
			}
		}

		// After all adds/deletes, reload from DB and set the final order
		repository.loadOthersFromDB();
		List<Others> dbList = repository.getAllOthers();
		List<Others> finalListForReordering = new ArrayList<>();
		for (Others uiItem : tempItemList) {
			dbList.stream()
					.filter(dbItem -> dbItem.getName().equals(uiItem.getName()))
					.findFirst()
					.ifPresent(finalListForReordering::add);
		}
		repository.updateDisplayOrder(finalListForReordering);

		if (!deletionFailures.isEmpty()) {
			summary.append("\n\nWarning: Could not delete the following items because they are in use:\n");
			summary.append(String.join("\n", deletionFailures));
			showAlert(Alert.AlertType.WARNING, "Partial Success", summary.toString());
		} else {
			showAlert(Alert.AlertType.INFORMATION, "Success", summary.toString());
		}

		closeWindow();
	}

	// Unused abstract methods
	@Override
	protected void openAddPopup(ActionEvent event) { handleAdd(); }
	@Override
	protected void openEditPopup(ActionEvent event) { /* No Edit functionality */ }
	@Override
	protected void openDeletePopup(ActionEvent event) { handleDelete(); }
}