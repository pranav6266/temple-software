package com.pranav.temple_software.controllers.menuControllers;

import com.pranav.temple_software.utils.ConfigManager;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.util.ArrayList;
import java.util.List;

public class PrinterSettingsController {

	@FXML
	private ComboBox<String> printersComboBox;

	@FXML
	private Label statusLabel;

	@FXML
	public void initialize() {
		// Get all printers on the system
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
		List<String> printerNames = new ArrayList<>();
		for (PrintService printService : printServices) {
			printerNames.add(printService.getName());
		}
		printersComboBox.setItems(FXCollections.observableArrayList(printerNames));

		// Load and select the currently saved printer
		String savedPrinter = ConfigManager.getInstance().getProperty("printer.name");
		if (savedPrinter != null && !savedPrinter.isEmpty()) {
			printersComboBox.setValue(savedPrinter);
		}
	}

	// FILE: src/main/java/com/pranav/temple_software/controllers/menuControllers/PrinterSettingsController.java
	@FXML
	void handleSave() {
		String selectedPrinter = printersComboBox.getValue();
		if (selectedPrinter == null || selectedPrinter.isEmpty()) {
			showStatus("Please select a printer before saving.", true);
			return;
		}

		ConfigManager.getInstance().saveProperty("printer.name", selectedPrinter);
		handleCancel();
	}

	@FXML
	void handleCancel() {
		Stage stage = (Stage) printersComboBox.getScene().getWindow();
		stage.close();
	}

	private void showStatus(String message, boolean isError) {
		statusLabel.setText(message);
		statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
	}
}