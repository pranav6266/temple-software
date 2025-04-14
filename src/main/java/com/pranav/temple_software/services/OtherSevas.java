package com.pranav.temple_software.services;
import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.OtherSevaRepository;

import java.util.List;
import java.util.Optional;

public class OtherSevas {
	MainController controller ;

	public OtherSevas(MainController mainController) {
		this.controller = mainController;
	}

	public void handleAddOtherSeva() {
		String selected = controller.otherServicesComboBox.getValue();
		if (selected == null || selected.equals("ಆಯ್ಕೆ")) return;

		// Extract name from "Name - ₹Amount" format if needed
		String nameOnly = selected.contains(" - ₹") ? selected.split(" - ₹")[0].trim() : selected;

		// Always fetch the latest data from repository
		List<SevaEntry> currentOtherSevas = OtherSevaRepository.getAllOtherSevas();
		Optional<SevaEntry> matched = currentOtherSevas.stream()
				.filter(entry -> entry.getName().equals(nameOnly))
				.findFirst();

		matched.ifPresent(seva -> {
			SevaEntry newEntry = new SevaEntry(seva.getName(), seva.getAmount());
			controller.selectedSevas.add(newEntry); // Adds to TableView
			controller.sevaTableView.refresh(); // Make sure table updates
		});
	}

}
