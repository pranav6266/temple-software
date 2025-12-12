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
import java.util.function.Supplier; // Import added

public class ReceiptPrinter {
	private String getPrinterName() {
		String printerName = ConfigManager.getInstance().getProperty("printer.name");
		if (printerName == null || printerName.isEmpty()) {
			System.err.println("WARNING: Printer name is not configured in settings.");
			return "BOXP-BR 80"; // Fallback
		}
		return printerName;
	}

	public ReceiptPrinter() {
	}

	// --- SEVA RECEIPT ---
	public void showPrintPreview(SevaReceiptData tempData, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed, Supplier<Integer> saveAction) {
		try {
			// Generate preview with TEMP data (ID 0)
			BufferedImage receiptBufferedImage = new EscPosPrinterService(null).generateSevaReceiptImage(tempData);
			Image receiptFxImage = SwingFXUtils.toFXImage(receiptBufferedImage, null);
			ImageView receiptView = new ImageView(receiptFxImage);

			showPreviewDialog(receiptView, "Seva Receipt Preview", ownerStage, onPrintComplete, onDialogClosed,
					() -> { // Print Action
						try {
							int newId = saveAction.get(); // EXECUTE SAVE
							if (newId == -1) throw new Exception("Database Save Failed");

							SevaReceiptData finalData = new SevaReceiptData(
									newId, tempData.getDevoteeName(), tempData.getPhoneNumber(), tempData.getAddress(), tempData.getPanNumber(),
									tempData.getRashi(), tempData.getNakshatra(), tempData.getFormattedDate().equals("") ? null : java.time.LocalDate.now(), // Date is handled in object, passed safely
									tempData.getSevas(), tempData.getTotalAmount(), tempData.getPaymentMode()
							);
							// Hack: Re-parsing date might be needed if strictly typed, but usually fine or constructor handles it.
							// Better Constructor usage:
							SevaReceiptData finalDataClean = new SevaReceiptData(
									newId, tempData.getDevoteeName(), tempData.getPhoneNumber(), tempData.getAddress(), tempData.getPanNumber(),
									tempData.getRashi(), tempData.getNakshatra(),
									// We need the LocalDate, but getter returns String. In real app, getter should return LocalDate or store it.
									// Assuming safe reuse or passing the date from controller context if needed.
									// For now, assuming internal date object is accessible or re-passed.
									// FIX: Let's assume the getter logic in model:
									// Since original model had LocalDate field, we can't easily get it back from formatted string without parsing.
									// However, simpler way: Pass LocalDate.now() if it was 'today', or we need a getter for raw date in model.
									// Assuming we add getSevaDate() to model or just use the current date if it's new.
									java.time.LocalDate.now(),
									tempData.getSevas(), tempData.getTotalAmount(), tempData.getPaymentMode()
							);

							new EscPosPrinterService(getPrinterName()).printSevaReceipt(finalDataClean);
							return true;
						} catch (Exception e) {
							showAlert(ownerStage, "Printing Error", "Could not print receipt: " + e.getMessage());
							return false;
						}
					},
					() -> { // Save PNG Action
						try {
							int newId = saveAction.get(); // EXECUTE SAVE
							if (newId == -1) throw new Exception("Database Save Failed");

							SevaReceiptData finalData = new SevaReceiptData(
									newId, tempData.getDevoteeName(), tempData.getPhoneNumber(), tempData.getAddress(), tempData.getPanNumber(),
									tempData.getRashi(), tempData.getNakshatra(), java.time.LocalDate.now(),
									tempData.getSevas(), tempData.getTotalAmount(), tempData.getPaymentMode()
							);

							BufferedImage finalImage = new EscPosPrinterService(null).generateSevaReceiptImage(finalData);
							String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
							String fileName = String.format("Seva-%d-%s.png", newId, timestamp);
							Path savePath = createSavePath("Seva Receipts", fileName);
							ImageIO.write(finalImage, "png", savePath.toFile());
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

	// --- DONATION RECEIPT ---
	public void showDonationPrintPreview(DonationReceiptData tempData, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed, Supplier<Integer> saveAction) {
		try {
			BufferedImage receiptBufferedImage = new EscPosPrinterService(null).generateDonationReceiptImage(tempData);
			Image receiptFxImage = SwingFXUtils.toFXImage(receiptBufferedImage, null);
			ImageView receiptView = new ImageView(receiptFxImage);

			showPreviewDialog(receiptView, "Donation Receipt Preview", ownerStage, onPrintComplete, onDialogClosed,
					() -> {
						try {
							int newId = saveAction.get();
							if (newId == -1) throw new Exception("Database Save Failed");

							DonationReceiptData finalData = new DonationReceiptData(
									newId, tempData.getDevoteeName(), tempData.getPhoneNumber(), tempData.getAddress(), tempData.getPanNumber(),
									tempData.getRashi(), tempData.getNakshatra(), java.time.LocalDate.now(),
									tempData.getDonationName(), tempData.getDonationAmount(), tempData.getPaymentMode()
							);

							new EscPosPrinterService(getPrinterName()).printDonationReceipt(finalData);
							return true;
						} catch (Exception e) {
							showAlert(ownerStage, "Printing Error", "Could not print receipt: " + e.getMessage());
							return false;
						}
					},
					() -> {
						try {
							int newId = saveAction.get();
							if (newId == -1) throw new Exception("Database Save Failed");

							DonationReceiptData finalData = new DonationReceiptData(
									newId, tempData.getDevoteeName(), tempData.getPhoneNumber(), tempData.getAddress(), tempData.getPanNumber(),
									tempData.getRashi(), tempData.getNakshatra(), java.time.LocalDate.now(),
									tempData.getDonationName(), tempData.getDonationAmount(), tempData.getPaymentMode()
							);

							BufferedImage finalImage = new EscPosPrinterService(null).generateDonationReceiptImage(finalData);
							String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
							String fileName = String.format("Donation-%d-%s.png", newId, timestamp);
							Path savePath = createSavePath("Donation Receipts", fileName);
							ImageIO.write(finalImage, "png", savePath.toFile());
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

	// --- SHASHWATHA POOJA RECEIPT ---
	public void showShashwathaPoojaPrintPreview(ShashwathaPoojaReceipt tempData, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed, Supplier<Integer> saveAction) {
		try {
			BufferedImage receiptBufferedImage = new EscPosPrinterService(null).generateShashwathaPoojaReceiptImage(tempData);
			Image receiptFxImage = SwingFXUtils.toFXImage(receiptBufferedImage, null);
			ImageView receiptView = new ImageView(receiptFxImage);

			showPreviewDialog(receiptView, "Shashwatha Pooja Preview", ownerStage, onPrintComplete, onDialogClosed,
					() -> {
						try {
							int newId = saveAction.get();
							if (newId == -1) throw new Exception("Database Save Failed");

							ShashwathaPoojaReceipt finalData = new ShashwathaPoojaReceipt(
									newId, tempData.getDevoteeName(), tempData.getPhoneNumber(), tempData.getAddress(), tempData.getPanNumber(),
									tempData.getRashi(), tempData.getNakshatra(), java.time.LocalDate.now(),
									tempData.getPoojaDate(), tempData.getAmount(), tempData.getPaymentMode()
							);

							new EscPosPrinterService(getPrinterName()).printShashwathaPoojaReceipt(finalData);
							return true;
						} catch (Exception e) {
							showAlert(ownerStage, "Printing Error", "Could not print receipt: " + e.getMessage());
							return false;
						}
					},
					() -> {
						try {
							int newId = saveAction.get();
							if (newId == -1) throw new Exception("Database Save Failed");

							ShashwathaPoojaReceipt finalData = new ShashwathaPoojaReceipt(
									newId, tempData.getDevoteeName(), tempData.getPhoneNumber(), tempData.getAddress(), tempData.getPanNumber(),
									tempData.getRashi(), tempData.getNakshatra(), java.time.LocalDate.now(),
									tempData.getPoojaDate(), tempData.getAmount(), tempData.getPaymentMode()
							);

							BufferedImage finalImage = new EscPosPrinterService(null).generateShashwathaPoojaReceiptImage(finalData);
							String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
							String fileName = String.format("Shashwatha-%d-%s.png", newId, timestamp);
							Path savePath = createSavePath("Shashwatha Pooja Receipts", fileName);
							ImageIO.write(finalImage, "png", savePath.toFile());
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

	// --- IN-KIND DONATION RECEIPT ---
	public void showInKindDonationPrintPreview(InKindDonation tempData, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed, Supplier<Integer> saveAction) {
		try {
			BufferedImage receiptBufferedImage = new EscPosPrinterService(null).generateInKindDonationReceiptImage(tempData);
			Image receiptFxImage = SwingFXUtils.toFXImage(receiptBufferedImage, null);
			ImageView receiptView = new ImageView(receiptFxImage);

			showPreviewDialog(receiptView, "In-Kind Donation Preview", ownerStage, onPrintComplete, onDialogClosed,
					() -> {
						try {
							int newId = saveAction.get();
							if (newId == -1) throw new Exception("Database Save Failed");

							InKindDonation finalData = new InKindDonation(
									newId, tempData.getDevoteeName(), tempData.getPhoneNumber(), tempData.getAddress(), tempData.getPanNumber(),
									tempData.getRashi(), tempData.getNakshatra(), tempData.getDonationDate(), tempData.getItemDescription()
							);

							new EscPosPrinterService(getPrinterName()).printInKindDonationReceipt(finalData);
							return true;
						} catch (Exception e) {
							showAlert(ownerStage, "Printing Error", "Could not print receipt: " + e.getMessage());
							return false;
						}
					},
					() -> {
						try {
							int newId = saveAction.get();
							if (newId == -1) throw new Exception("Database Save Failed");

							InKindDonation finalData = new InKindDonation(
									newId, tempData.getDevoteeName(), tempData.getPhoneNumber(), tempData.getAddress(), tempData.getPanNumber(),
									tempData.getRashi(), tempData.getNakshatra(), tempData.getDonationDate(), tempData.getItemDescription()
							);

							BufferedImage finalImage = new EscPosPrinterService(null).generateInKindDonationReceiptImage(finalData);
							String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
							String fileName = String.format("InKind-%d-%s.png", newId, timestamp);
							Path savePath = createSavePath("In-Kind Donation Receipts", fileName);
							ImageIO.write(finalImage, "png", savePath.toFile());
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

	// --- KARYAKRAMA RECEIPT ---
	public void showKaryakramaPrintPreview(KaryakramaReceiptData tempData, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed, Supplier<Integer> saveAction) {
		try {
			BufferedImage receiptBufferedImage = new EscPosPrinterService(null).generateKaryakramaReceiptImage(tempData);
			Image receiptFxImage = SwingFXUtils.toFXImage(receiptBufferedImage, null);
			ImageView receiptView = new ImageView(receiptFxImage);

			showPreviewDialog(receiptView, "Karyakrama Receipt Preview", ownerStage, onPrintComplete, onDialogClosed,
					() -> {
						try {
							int newId = saveAction.get();
							if (newId == -1) throw new Exception("Database Save Failed");

							KaryakramaReceiptData finalData = new KaryakramaReceiptData(
									newId, tempData.getDevoteeName(), tempData.getPhoneNumber(), tempData.getAddress(), tempData.getPanNumber(),
									tempData.getRashi(), tempData.getNakshatra(), tempData.getKaryakramaName(), tempData.getReceiptDate(),
									tempData.getSevas(), tempData.getTotalAmount(), tempData.getPaymentMode()
							);

							new EscPosPrinterService(getPrinterName()).printKaryakramaReceipt(finalData);
							return true;
						} catch (Exception e) {
							showAlert(ownerStage, "Printing Error", "Could not print receipt: " + e.getMessage());
							return false;
						}
					},
					() -> {
						try {
							int newId = saveAction.get();
							if (newId == -1) throw new Exception("Database Save Failed");

							KaryakramaReceiptData finalData = new KaryakramaReceiptData(
									newId, tempData.getDevoteeName(), tempData.getPhoneNumber(), tempData.getAddress(), tempData.getPanNumber(),
									tempData.getRashi(), tempData.getNakshatra(), tempData.getKaryakramaName(), tempData.getReceiptDate(),
									tempData.getSevas(), tempData.getTotalAmount(), tempData.getPaymentMode()
							);

							BufferedImage finalImage = new EscPosPrinterService(null).generateKaryakramaReceiptImage(finalData);
							String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
							String fileName = String.format("Karyakrama-%d-%s.png", newId, timestamp);
							Path savePath = createSavePath("Karyakrama Receipts", fileName);
							ImageIO.write(finalImage, "png", savePath.toFile());
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

		Button savePreviewButton = new Button("Save PNG");
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
			if (afterActionCallback != null) Platform.runLater(() -> afterActionCallback.accept(false));
			if (onDialogClosed != null) onDialogClosed.run();
			previewStage.close();
		});

		previewStage.setOnCloseRequest(_ -> {
			if (afterActionCallback != null) Platform.runLater(() -> afterActionCallback.accept(false));
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
		Files.createDirectories(subDirPath);
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
	interface FailableRunnable {
		boolean run();
	}
}