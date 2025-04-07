package com.pranav.temple_software.listeners;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.Seva;
import com.pranav.temple_software.models.SevaEntry;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckBox;

import java.util.Arrays;

public class SevaListener {
	MainController controller;
	public SevaListener(MainController controller){
		this.controller = controller;
	}
	public void initiateSevaListener() {
		// Add listener to selectedSevas to sync CheckBox states
		controller.selectedSevas.addListener((ListChangeListener<SevaEntry>) change -> {
			while (change.next()) {
				if (change.wasRemoved()) {
					for (SevaEntry removedEntry : change.getRemoved()) {
						// Find the Seva ID from the removed entry's name
						controller.sevaRepository.sevaMap.entrySet().stream()
								.filter(entry -> entry.getValue().getName().equals(removedEntry.getName()))
								.findFirst()
								.ifPresent(entry -> {
									String sevaId = entry.getKey();
									CheckBox checkBox = controller.sevaCheckboxMap.get(sevaId);
									if (checkBox != null) {
										checkBox.setSelected(false);
									}
								});
					}
				}
			}
		});
	}

	public void setupSevaCheckboxes() {
		controller.sevaRepository.initializeSevaData();
		controller.sevaCheckboxMap.clear(); // Clear existing entries

		for (Seva seva : controller.sevaRepository.sevaMap.values()) {
			CheckBox checkBox = new CheckBox(seva.getId() + ". " + seva.getName());
			checkBox.getStyleClass().add("seva-checkbox");
			String sevaId = seva.getId(); // Unique identifier for the Seva

			// Add CheckBox to the map
			controller.sevaCheckboxMap.put(sevaId, checkBox);

			{// Initialize CheckBox state based on selectedSevas
				boolean isSelected = controller.selectedSevas.stream()
						.anyMatch(entry -> entry.getName().equals(seva.getName()));
				checkBox.setSelected(isSelected);
			}

			// Update selectedSevas when CheckBox is toggled
			checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
				if (isSelected) {
					controller.selectedSevas.add(new SevaEntry(seva.getName(), seva.getAmount()));
				} else {
					controller.selectedSevas.removeIf(entry ->
							entry.getName().equals(seva.getName()) &&
									entry.getAmount() == seva.getAmount()
					);
				}
			});

			controller.sevaCheckboxContainer.getChildren().add(checkBox);
		}
		raashiNakshatraMap();
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
