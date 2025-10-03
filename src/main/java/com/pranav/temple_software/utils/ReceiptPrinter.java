package com.pranav.temple_software.utils;

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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

public class ReceiptPrinter {
	private String getPrinterName() {
		String printerName = ConfigManager.getInstance().getProperty("printer.name");
		if (printerName == null || printerName.isEmpty()) {
			System.err.println("WARNING: Printer name is not configured in settings.");
			return "BOXP-BR 80"; // Fallback to a default if nothing is saved
		}
		return printerName;
	}

	public ReceiptPrinter() {
	}

	public void showPrintPreview(SevaReceiptData data, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed) {
		try {
			BufferedImage receiptBufferedImage = new EscPosPrinterService(null).generateSevaReceiptImage(data);
			Image receiptFxImage = SwingFXUtils.toFXImage(receiptBufferedImage, null);
			ImageView receiptView = new ImageView(receiptFxImage);

			showPreviewDialog(receiptView, "Seva Receipt Preview", ownerStage, onPrintComplete, onDialogClosed,
					() -> { // Print Action
						try {
							new EscPosPrinterService(getPrinterName()).printSevaReceipt(data);
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
							Path savePath = createSavePath("Seva Receipts", fileName);
							ImageIO.write(receiptBufferedImage, "png", savePath.toFile());
							showAlert(ownerStage, "Preview Saved", "File saved successfully to:\n" + savePath.toString());
							return true;
						} catch (Exception e) {
							showAlert(ownerStage, "File Error", "Could not save PNG preview: " + e.getMessage());
							return false;
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
							new EscPosPrinterService(getPrinterName()).printDonationReceipt(data);
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
							Path savePath = createSavePath("Donation Receipts", fileName);
							ImageIO.write(receiptBufferedImage, "png", savePath.toFile());
							showAlert(ownerStage, "Preview Saved", "File saved successfully to:\n" + savePath.toString());
							return true;
						} catch (Exception e) {
							showAlert(ownerStage, "File Error", "Could not save PNG preview: " + e.getMessage());
							return false;
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
							new EscPosPrinterService(getPrinterName()).printShashwathaPoojaReceipt(data);
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
							Path savePath = createSavePath("Shashwatha Pooja Receipts", fileName);
							ImageIO.write(receiptBufferedImage, "png", savePath.toFile());
							showAlert(ownerStage, "Preview Saved", "File saved successfully to:\n" + savePath.toString());
							return true;
						} catch (Exception e) {
							showAlert(ownerStage, "File Error", "Could not save PNG preview: " + e.getMessage());
							return false;
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
							new EscPosPrinterService(getPrinterName()).printInKindDonationReceipt(data);
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
							Path savePath = createSavePath("In-Kind Donation Receipts", fileName);
							ImageIO.write(receiptBufferedImage, "png", savePath.toFile());
							showAlert(ownerStage, "Preview Saved", "File saved successfully to:\n" + savePath.toString());
							return true;
						} catch (Exception e) {
							showAlert(ownerStage, "File Error", "Could not save PNG preview: " + e.getMessage());
							return false;
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
							new EscPosPrinterService(getPrinterName()).printKaryakramaReceipt(data);
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
							Path savePath = createSavePath("Karyakrama Receipts", fileName);
							ImageIO.write(receiptBufferedImage, "png", savePath.toFile());
							showAlert(ownerStage, "Preview Saved", "File saved successfully to:\n" + savePath.toString());
							return true;
						} catch (Exception e) {
							showAlert(ownerStage, "File Error", "Could not save PNG preview: " + e.getMessage());
							return false;
						}
					}
			);
		} catch (Exception e) {
			showAlert(ownerStage, "Preview Error", "Could not generate receipt preview: " + e.getMessage());
		}
	}

	private void showPreviewDialog(Node receiptNode, String title, Stage ownerStage, Consumer<Boolean> afterActionCallback, Runnable onDialogClosed, FailableRunnable onPrintAction, FailableRunnable onSavePreviewAction) {
		Stage previewStage = new Stage();
		previewStage.initModality(Modality.WINDOW_MODAL);
		previewStage.initOwner(ownerStage);
		previewStage.setTitle(title);

		VBox previewContainer = new VBox(receiptNode);
		previewContainer.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");
		ScrollPane scrollPane = new ScrollPane(new Group(previewContainer));
		scrollPane.setPrefViewportHeight(450);

		Button printButton = new Button("Print");
		printButton.setOnAction(_ -> {
			boolean success = onPrintAction.run();
			if (afterActionCallback != null) Platform.runLater(() -> afterActionCallback.accept(success));
			previewStage.close();
		});

		Button savePreviewButton = new Button("Save PNG Preview");
		savePreviewButton.setOnAction(_ -> {
			Optional<ButtonType> result = showSaveConfirmationDialog(ownerStage);
			if (result.isPresent() && result.get() == ButtonType.OK) {
				boolean success = onSavePreviewAction.run();
				if (afterActionCallback != null) {
					Platform.runLater(() -> afterActionCallback.accept(success));
				}
				previewStage.close();
			}
		});

		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(_ -> {
			if (onDialogClosed != null) onDialogClosed.run();
			previewStage.close();
		});

		previewStage.setOnCloseRequest(_ -> {
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

	private Optional<ButtonType> showSaveConfirmationDialog(Stage owner) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.initOwner(owner);
		alert.setTitle("Confirm Save");
		alert.setHeaderText("Save PNG Preview");
		alert.setContentText("Are you sure you want to save this receipt preview as a PNG file?");
		return alert.showAndWait();
	}

	private Path createSavePath(String subfolder, String fileName) throws IOException {
		String userDesktop = System.getProperty("user.home") + File.separator + "Desktop";
		Path mainDirPath = Paths.get(userDesktop, "CHERKABE_RECEIPTS");
		Path subDirPath = mainDirPath.resolve(subfolder);
		Files.createDirectories(subDirPath); // Create main and sub folders if they don't exist
		return subDirPath.resolve(fileName);
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
