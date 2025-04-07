// HistoryController.java
package com.pranav.temple_software.controllers;

import com.pranav.temple_software.models.ReceiptData;
import com.pranav.temple_software.repositories.ReceiptRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import java.util.List;

public class HistoryController {
	@FXML private TableView<ReceiptData> historyTable;

	private final ReceiptRepository receiptRepository = new ReceiptRepository();

	@FXML
	public void initialize() {
		loadHistory();
	}

	private void loadHistory() {
		List<ReceiptData> receipts = receiptRepository.getAllReceipts();
		historyTable.setItems(FXCollections.observableArrayList(receipts));
	}
}