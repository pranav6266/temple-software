package com.pranav.temple_software.utils;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.*;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class ReceiptPrinter {
	private static final String PRINTER_NAME_PLACEHOLDER = "BOXP-BR 80";
	private final MainController controller;

	public ReceiptPrinter(MainController controller) {
		this.controller = controller;
	}

	public void showPrintPreview(SevaReceiptData data, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed) {
		try {
			BufferedImage receiptBufferedImage = new EscPosPrinterService(null).generateSevaReceiptImage(data);
			Image receiptFxImage = SwingFXUtils.toFXImage(receiptBufferedImage, null);
			ImageView receiptView = new ImageView(receiptFxImage);

			showPreviewDialog(receiptView, "Seva Receipt Preview", ownerStage, onPrintComplete, onDialogClosed,
					() -> { // Print Action
						try {
							new EscPosPrinterService(PRINTER_NAME_PLACEHOLDER).printSevaReceipt(data);
							return true;
						} catch (Exception e) {
							showAlert(ownerStage, "Printing Error", "Could not print receipt: " + e.getMessage());
							return false;
						}
					},
					() -> { // Save PNG Action
						try {
							String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
							String fileName = String.format("Seva-%d-%s.png", data.getReceiptId(), timestamp);
							new EscPosPrinterService(null).saveSevaReceiptAsPng(data, fileName);
							showAlert(ownerStage, "Preview Saved", fileName + " has been saved to your Desktop.");
						} catch (Exception e) {
							showAlert(ownerStage, "File Error", "Could not save PNG preview: " + e.getMessage());
						}
					}
			);
		} catch (Exception e) {
			showAlert(ownerStage, "Preview Error", "Could not generate receipt preview: " + e.getMessage());
		}
	}

	public void showDonationPrintPreview(DonationReceiptData data, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed) {
		try {
			BufferedImage receiptBufferedImage = new EscPosPrinterService(null).generateDonationReceiptImage(data);
			Image receiptFxImage = SwingFXUtils.toFXImage(receiptBufferedImage, null);
			ImageView receiptView = new ImageView(receiptFxImage);

			showPreviewDialog(receiptView, "Donation Receipt Preview", ownerStage, onPrintComplete, onDialogClosed,
					() -> {
						try {
							new EscPosPrinterService(PRINTER_NAME_PLACEHOLDER).printDonationReceipt(data);
							return true;
						} catch (Exception e) {
							showAlert(ownerStage, "Printing Error", "Could not print receipt: " + e.getMessage());
							return false;
						}
					},
					() -> {
						try {
							String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
							String fileName = String.format("Donation-%d-%s.png", data.getDonationReceiptId(), timestamp);
							new EscPosPrinterService(null).saveDonationReceiptAsPng(data, fileName);
							showAlert(ownerStage, "Preview Saved", fileName + " has been saved to your Desktop.");
						} catch (Exception e) {
							showAlert(ownerStage, "File Error", "Could not save PNG preview: " + e.getMessage());
						}
					}
			);
		} catch (Exception e) {
			showAlert(ownerStage, "Preview Error", "Could not generate receipt preview: " + e.getMessage());
		}
	}

	public void showShashwathaPoojaPrintPreview(ShashwathaPoojaReceipt data, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed) {
		try {
			BufferedImage receiptBufferedImage = new EscPosPrinterService(null).generateShashwathaPoojaReceiptImage(data);
			Image receiptFxImage = SwingFXUtils.toFXImage(receiptBufferedImage, null);
			ImageView receiptView = new ImageView(receiptFxImage);

			showPreviewDialog(receiptView, "Shashwatha Pooja Preview", ownerStage, onPrintComplete, onDialogClosed,
					() -> {
						try {
							new EscPosPrinterService(PRINTER_NAME_PLACEHOLDER).printShashwathaPoojaReceipt(data);
							return true;
						} catch (Exception e) {
							showAlert(ownerStage, "Printing Error", "Could not print receipt: " + e.getMessage());
							return false;
						}
					},
					() -> {
						try {
							String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
							String fileName = String.format("Shashwatha-%d-%s.png", data.getReceiptId(), timestamp);
							new EscPosPrinterService(null).saveShashwathaPoojaReceiptAsPng(data, fileName);
							showAlert(ownerStage, "Preview Saved", fileName + " has been saved to your Desktop.");
						} catch (Exception e) {
							showAlert(ownerStage, "File Error", "Could not save PNG preview: " + e.getMessage());
						}
					}
			);
		} catch (Exception e) {
			showAlert(ownerStage, "Preview Error", "Could not generate receipt preview: " + e.getMessage());
		}
	}

	public void showInKindDonationPrintPreview(InKindDonation data, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed) {
		try {
			BufferedImage receiptBufferedImage = new EscPosPrinterService(null).generateInKindDonationReceiptImage(data);
			Image receiptFxImage = SwingFXUtils.toFXImage(receiptBufferedImage, null);
			ImageView receiptView = new ImageView(receiptFxImage);

			showPreviewDialog(receiptView, "In-Kind Donation Preview", ownerStage, onPrintComplete, onDialogClosed,
					() -> {
						try {
							new EscPosPrinterService(PRINTER_NAME_PLACEHOLDER).printInKindDonationReceipt(data);
							return true;
						} catch (Exception e) {
							showAlert(ownerStage, "Printing Error", "Could not print receipt: " + e.getMessage());
							return false;
						}
					},
					() -> {
						try {
							String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
							String fileName = String.format("InKind-%d-%s.png", data.getInKindReceiptId(), timestamp);
							new EscPosPrinterService(null).saveInKindDonationReceiptAsPng(data, fileName);
							showAlert(ownerStage, "Preview Saved", fileName + " has been saved to your Desktop.");
						} catch (Exception e) {
							showAlert(ownerStage, "File Error", "Could not save PNG preview: " + e.getMessage());
						}
					}
			);
		} catch (Exception e) {
			showAlert(ownerStage, "Preview Error", "Could not generate receipt preview: " + e.getMessage());
		}
	}

	public void showKaryakramaPrintPreview(KaryakramaReceiptData data, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed) {
		try {
			BufferedImage receiptBufferedImage = new EscPosPrinterService(null).generateKaryakramaReceiptImage(data);
			Image receiptFxImage = SwingFXUtils.toFXImage(receiptBufferedImage, null);
			ImageView receiptView = new ImageView(receiptFxImage);

			showPreviewDialog(receiptView, "Karyakrama Receipt Preview", ownerStage, onPrintComplete, onDialogClosed,
					() -> {
						try {
							new EscPosPrinterService(PRINTER_NAME_PLACEHOLDER).printKaryakramaReceipt(data);
							return true;
						} catch (Exception e) {
							showAlert(ownerStage, "Printing Error", "Could not print receipt: " + e.getMessage());
							return false;
						}
					},
					() -> {
						try {
							String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
							String fileName = String.format("Karyakrama-%d-%s.png", data.getReceiptId(), timestamp);
							new EscPosPrinterService(null).saveKaryakramaReceiptAsPng(data, fileName);
							showAlert(ownerStage, "Preview Saved", fileName + " has been saved to your Desktop.");
						} catch (Exception e) {
							showAlert(ownerStage, "File Error", "Could not save PNG preview: " + e.getMessage());
						}
					}
			);
		} catch (Exception e) {
			showAlert(ownerStage, "Preview Error", "Could not generate receipt preview: " + e.getMessage());
		}
	}

	private void showPreviewDialog(Node receiptNode, String title, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed, FailableRunnable onPrintAction, Runnable onSavePreviewAction) {
		Stage previewStage = new Stage();
		previewStage.initModality(Modality.WINDOW_MODAL);
		previewStage.initOwner(ownerStage);
		previewStage.setTitle(title);

		VBox previewContainer = new VBox(receiptNode);
		previewContainer.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");
		ScrollPane scrollPane = new ScrollPane(new Group(previewContainer));
		scrollPane.setPrefViewportHeight(450);

		Button printButton = new Button("Print");
		printButton.setOnAction(e -> {
			boolean success = onPrintAction.run();
			if (onPrintComplete != null) Platform.runLater(() -> onPrintComplete.accept(success));
			previewStage.close();
		});

		Button savePreviewButton = new Button("Save PNG Preview");
		savePreviewButton.setOnAction(e -> onSavePreviewAction.run());

		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(e -> previewStage.close());
		previewStage.setOnCloseRequest(e -> {
			if (onDialogClosed != null) onDialogClosed.run();
		});

		HBox buttonBox = new HBox(10, printButton, savePreviewButton, cancelButton);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setPadding(new Insets(10));

		VBox layout = new VBox(10, scrollPane, buttonBox);
		layout.setAlignment(Pos.CENTER);
		Scene scene = new Scene(layout, 620, 600);
		previewStage.setScene(scene);
		previewStage.show();
	}

	private void showAlert(Stage owner, String title, String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.initOwner(owner);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(message);
			alert.showAndWait();
		});
	}

	@FunctionalInterface
	interface FailableRunnable { boolean run(); }
}