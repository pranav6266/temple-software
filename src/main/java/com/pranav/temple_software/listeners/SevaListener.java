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
			checkBox.selectedProperty().addListener((_, _, isSelectedNow) -> {
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





	public void rashiNakshatraMap() {
		// ಮೇಷ (Aries)
		controller.rashiNakshatraMap.put("ಮೇಷ", Arrays.asList("ಅಶ್ವಿನಿ", "ಭರಣಿ", "ಕೃತ್ತಿಕ"));
		// ವೃಷಭ (Taurus)
		controller.rashiNakshatraMap.put("ವೃಷಭ", Arrays.asList("ಕೃತ್ತಿಕ", "ರೋಹಿಣಿ", "ಮೃಗಶಿರ"));
		// ಮಿಥುನ (Gemini)
		controller.rashiNakshatraMap.put("ಮಿಥುನ", Arrays.asList("ಮೃಗಶಿರ", "ಆರ್ದ್ರ", "ಪುನರ್ವಸು"));
		// ಕರ್ಕ (Cancer)
		controller.rashiNakshatraMap.put("ಕರ್ಕಾಟಕ", Arrays.asList("ಪುನರ್ವಸು", "ಪುಷ್ಯ", "ಆಶ್ಲೇಷ"));
		// ಸಿಂಹ (Leo)
		controller.rashiNakshatraMap.put("ಸಿಂಹ", Arrays.asList("ಮಘ", "ಪೂರ್ವ", "ಉತ್ತರ"));
		// ಕನ್ಯಾ (Virgo)
		controller.rashiNakshatraMap.put("ಕನ್ಯಾ", Arrays.asList("ಉತ್ತರ", "ಹಸ್ತ", "ಚಿತ್ರ"));
		// ತುಲಾ (Libra)
		controller.rashiNakshatraMap.put("ತುಲಾ", Arrays.asList("ಚಿತ್ರ", "ಸ್ವಾತಿ", "ವಿಶಾಖ"));
		// ವೃಶ್ಚಿಕ (Scorpio)
		controller.rashiNakshatraMap.put("ವೃಶ್ಚಿಕ", Arrays.asList("ವಿಶಾಖ", "ಅನುರಾಧ", "ಜೇಷ್ಠ"));
		// ಧನುಸ್ (Sagittarius)
		controller.rashiNakshatraMap.put("ಧನು", Arrays.asList("ಮೂಲ", "ಪೂರ್ವಾಷಾಢ", "ಉತ್ತರಾಷಾಢ"));
		// ಮಕರ (Capricorn)
		controller.rashiNakshatraMap.put("ಮಕರ", Arrays.asList("ಉತ್ತರಾಷಾಢ", "ಶ್ರವಣ", "ಧನಿಷ್ಠ"));
		// ಕುಂಭ (Aquarius)
		controller.rashiNakshatraMap.put("ಕುಂಭ", Arrays.asList("ಧನಿಷ್ಠ", "ಶತಭಿಷ", "ಪೂರ್ವಾಭಾದ್ರ"));
		// ಮೀನ (Pisces)
		controller.rashiNakshatraMap.put("ಮೀನ", Arrays.asList("ಪೂರ್ವಾಭಾದ", "ಉತ್ತರಾಭಾದ್ರ", "ರೇವತಿ"));
	}
}
