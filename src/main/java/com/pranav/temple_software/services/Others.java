package com.pranav.temple_software.services;
import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.OthersRepository;

import java.util.List;
import java.util.Optional;

public class Others {
	MainController controller ;

	public Others(MainController mainController) {
		this.controller = mainController;
	}

	public void handleAddOtherSeva() {
		String selected = controller.othersComboBox.getValue();

		// **Step 1: Check if the user selected the default option**
		if (selected == null || selected.equals("ಆಯ್ಕೆ")) {
			controller.showAlert("Invalid Selection", "Please select a valid Other Seva.");
			return;
		}

		// Extract name if combo box format includes "Name - ₹Amount"
		String nameOnly = selected.contains(" - ₹") ? selected.split(" - ₹")[0].trim() : selected;

		// **Step 2: Check if the selected Other Seva already exists in the TableView**
		boolean exists = controller.selectedSevas.stream()
				.anyMatch(entry -> entry.getName().equals(nameOnly));

		if (exists) {
			// **Show alert instead of modifying the amount**
			controller.showAlert("Duplicate Entry", "The selected Other Seva is already added.");
			return;
		}

		// Always fetch the latest data from repository
		List<SevaEntry> currentOtherSevas = OthersRepository.getAllOthers();
		Optional<SevaEntry> matched = currentOtherSevas.stream()
				.filter(entry -> entry.getName().equals(nameOnly))
				.findFirst();

		matched.ifPresent(seva -> {
			SevaEntry newEntry = new SevaEntry(seva.getName(), seva.getAmount());
			controller.selectedSevas.add(newEntry); // Add to TableView
			controller.sevaTableView.refresh(); // Ensure table updates
		});
	}

}
