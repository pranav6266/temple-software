package com.pranav.temple_software.services;
import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.SevaEntry;
import javafx.scene.control.Alert;

public class OtherSevas {
	MainController controller ;

	public OtherSevas(MainController mainController) {
		this.controller = mainController;
	}

	public void handleAddOtherSeva() {
		String sevaType = controller.otherServicesComboBox.getValue();

		if (sevaType == null || sevaType.equals("ಆಯ್ಕೆ") || sevaType.isEmpty()) {
			controller.showAlert(Alert.AlertType.INFORMATION, "Invalid Input", "Please select an other service type");
			return;
		}

		// Check for existing service
		String entryName = "ಇತರೆ ಸೇವೆಗಳು : " + sevaType;
		boolean exists = controller.selectedSevas.stream()
				.anyMatch(entry -> entry.getName().equals(entryName));

		if (exists) {
			controller.showAlert(Alert.AlertType.INFORMATION, "Duplicate Service", "This service already exists in the list");
			return;
		}

		controller.selectedSevas.add(new SevaEntry(entryName, 0.00));
	}
}
