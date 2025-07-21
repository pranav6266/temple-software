package com.pranav.temple_software.listeners;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.Seva;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.repositories.SevaRepository;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckBox;
import java.util.Collection; // *** ADD Import ***


import java.util.Arrays;

public class SevaListener {
	MainController controller;
	SevaRepository sevaRepository;
	public SevaListener(MainController controller , SevaRepository sevaRepository){
		this.controller = controller;
		this.sevaRepository = sevaRepository;
	}
	public void initiateSevaListener() { //
		// Add listener to selectedSevas to sync CheckBox states
		controller.selectedSevas.addListener((ListChangeListener<SevaEntry>) change -> { //
			while (change.next()) { //
				if (change.wasRemoved()) { //
					for (SevaEntry removedEntry : change.getRemoved()) { //
						// Find the Seva ID from the removed entry's name
						// *** Use the repository's data ***
						sevaRepository.getAllSevas().stream() // Get sevas from repository
								.filter(seva -> seva.getName().equals(removedEntry.getName())) // Find matching name
								.findFirst()
								.ifPresent(seva -> {
									String sevaId = seva.getId(); // Get ID from Seva object
									CheckBox checkBox = controller.sevaCheckboxMap.get(sevaId); //
									if (checkBox != null) { //
										checkBox.setSelected(false); //
									}
								});
					}
				}
			}
		});
	}


	public void setupSevaCheckboxes() {
		controller.sevaCheckboxMap.clear();
		controller.sevaCheckboxContainer.getChildren().clear();

		// Get sevas (assuming sorted consistently, e.g., by ID from repository)
		Collection<Seva> sevas = sevaRepository.getAllSevas();

		if (sevas.isEmpty()) {
			System.out.println("No sevas found in repository to display.");
			return;
		}

		// Use loop index for display number
		int displayIndex = 1; // Start sequential numbering from 1

		for (Seva seva : sevas) {
			// *** CHANGE: Use displayIndex for the checkbox text prefix ***
			CheckBox checkBox = new CheckBox(displayIndex + ". " + seva.getName());
			checkBox.getStyleClass().add("seva-checkbox");

			// *** IMPORTANT: Use the REAL seva.getId() as the key in the map ***
			final String currentSevaId = seva.getId();
			controller.sevaCheckboxMap.put(currentSevaId, checkBox); // Use real ID as key

			// Initialize CheckBox state based on selectedSevas (logic remains the same)
			boolean isSelected = controller.selectedSevas.stream()
					.anyMatch(entry -> entry.getName().equals(seva.getName()));
			checkBox.setSelected(isSelected);


			// Update selectedSevas when CheckBox is toggled (logic remains the same)
			checkBox.selectedProperty().addListener((obs, wasSelected, isSelectedNow) -> {
				if (isSelectedNow) {
					if (controller.selectedSevas.stream().noneMatch(e -> e.getName().equals(seva.getName()))) {
						controller.selectedSevas.add(new SevaEntry(seva.getName(), seva.getAmount()));
					}
				} else {
					controller.selectedSevas.removeIf(entry ->
							entry.getName().equals(seva.getName()) &&
									entry.getAmount() == seva.getAmount()
					);
				}
			});

			controller.sevaCheckboxContainer.getChildren().add(checkBox);
			displayIndex++; // Increment for the next checkbox
		}
		System.out.println("Seva checkboxes setup complete with " + sevas.size() + " sevas.");
	}





	public void raashiNakshatraMap() {
		// Populate the Rashi-Nakshatra mapping (adjust according to your data)
		controller.rashiNakshatraMap.put("ಮೇಷ", Arrays.asList("ಅಶ್ವಿನಿ", "ಭರಣಿ", "ಕೃತಿಕ"));
		controller.rashiNakshatraMap.put("ವೃಷಭ", Arrays.asList("ರೋಹಿಣಿ", "ಮೃಗಶಿರ", "ಆರ್ದ್ರ"));
		controller.rashiNakshatraMap.put("ಮಿಥುನ", Arrays.asList("ಪುನರ್ವಸು", "ಪುಷ್ಯ", "ಆಶ್ಲೇಷ"));
		controller.rashiNakshatraMap.put("ಕರ್ಕಾಟಕ", Arrays.asList("ಮಘ", "ಪೂರ್ವ ಫಲ್ಗುಣಿ", "ಉತ್ತರ ಫಲ್ಗುಣಿ"));
		controller.rashiNakshatraMap.put("ಸಿಂಹ", Arrays.asList("ಹಸ್ತ", "ಚಿತ್ತ", "ಸ್ವಾತಿ"));
		controller.rashiNakshatraMap.put("ಕನ್ಯಾ", Arrays.asList("ವಿಶಾಖ", "ಅನೂರಾಧ", "ಜ್ಯೇಷ್ಠ"));
		controller.rashiNakshatraMap.put("ತುಲಾ", Arrays.asList("ಮೂಲ", "ಪೂರ್ವಾಷಾಢ", "ಉತ್ತರಾಷಾಢ"));
		controller.rashiNakshatraMap.put("ವೃಶ್ಚಿಕ", Arrays.asList("ಶ್ರವಣ", "ಧನಿಷ್ಠ", "ಶತಭಿಷ"));
		controller.rashiNakshatraMap.put("ಧನು", Arrays.asList("ಪೂರ್ವ ಭಾದ್ರಪದ", "ಉತ್ತರ ಭಾದ್ರಪದ", "ರೇವತಿ"));
		controller.rashiNakshatraMap.put("ಮಕರ", Arrays.asList("ಧನಿಷ್ಠ", "ಶತಭಿಷ", "ಪೂರ್ವ ಭಾದ್ರಪದ")); // Example
		controller.rashiNakshatraMap.put("ಕುಂಭ", Arrays.asList("ಉತ್ತರ ಭಾದ್ರಪದ", "ರೇವತಿ", "ಅಶ್ವಿನಿ")); // Example
		controller.rashiNakshatraMap.put("ಮೀನ", Arrays.asList("ರೇವತಿ", "ಅಶ್ವಿನಿ", "ಭರಣಿ")); // Example
	}

}
