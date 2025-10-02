// FILE: src/main/java/com/pranav/temple_software/controllers/menuControllers/KaryakramaManager/OthersManagerController.java
package com.pranav.temple_software.controllers.menuControllers.KaryakramaManager;

import com.pranav.temple_software.controllers.menuControllers.BaseManagerController;
import com.pranav.temple_software.models.Others;
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
	protected void refreshGridPane() {
		othersTable.setItems(FXCollections.observableArrayList(tempItemList));
		othersTable.refresh();
	}

	@FXML
	private void handleAdd() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.initOwner(othersTable.getScene().getWindow());
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

	/**
	 * MODIFIED: This method no longer checks for deletion failures.
	 * It processes all additions and deletions and then updates the display order.
	 * This makes its behavior consistent with the other manager modules.
	 */
	@FXML
	@Override
	public void handleSave(ActionEvent event) {
		// Process deletions
		for (Others itemToDelete : itemsMarkedForDeletion) {
			repository.deleteOtherFromDB(itemToDelete.getId());
		}

		// Process additions
		for (Others item : tempItemList) {
			if (item.getId() == -1) { // -1 indicates a new, unsaved item
				repository.addOtherToDB(item.getName());
			}
		}

		// After all adds/deletes, reload from DB to get correct IDs for reordering
		repository.loadOthersFromDB();
		List<Others> dbList = repository.getAllOthers();
		List<Others> finalListForReordering = new ArrayList<>();

		// Create a list that reflects the final UI order but contains the actual DB items
		for (Others uiItem : tempItemList) {
			dbList.stream()
					.filter(dbItem -> dbItem.getName().equals(uiItem.getName()))
					.findFirst()
					.ifPresent(finalListForReordering::add);
		}
		repository.updateDisplayOrder(finalListForReordering);

		showAlert(Alert.AlertType.INFORMATION, "Success", "All changes have been saved.");
		closeWindow();
	}

	// Unused abstract methods from BaseManagerController that are now linked to the simpler handleAdd/handleDelete methods
	@Override
	protected void openAddPopup(ActionEvent event) { handleAdd(); }
	@Override
	protected void openEditPopup(ActionEvent event) { /* No Edit functionality for this manager */ }
	@Override
	protected void openDeletePopup(ActionEvent event) { handleDelete(); }
}