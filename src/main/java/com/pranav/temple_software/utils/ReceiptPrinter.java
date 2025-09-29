// I will provide the full file as it's cleaner than patching.
// The new methods are createKaryakramaReceiptNode, showKaryakramaPrintPreview, and saveKaryakramaReceiptAsPdf.

package com.pranav.temple_software.utils;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.*;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.printing.PDFPageable;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class ReceiptPrinter {

	private static final double RECEIPT_WIDTH_MM = 78;
	private static final double POINTS_PER_MM = 2.83465;
	private static final double RECEIPT_WIDTH_POINTS = RECEIPT_WIDTH_MM * POINTS_PER_MM;

	MainController controller;

	public ReceiptPrinter(MainController controller) {
		this.controller = controller;
	}

	private HBox createDetailRow(String label, String value) {
		HBox row = new HBox(4);
		Text labelText = new Text(label);
		labelText.setStyle("-fx-font-family: 'Noto Sans Kannada'; -fx-font-size: 9px; -fx-font-weight: bold; -fx-stroke: black; -fx-stroke-width: 0.15;");
		Text valueText = new Text(value);
		valueText.setFont(Font.font("Noto Sans Kannada", FontWeight.NORMAL, 9));
		row.getChildren().addAll(labelText, valueText);
		return row;
	}

	/**
	 * FULLY REVISED METHOD: Prints a JavaFX node by creating an in-memory PDF
	 * and sending it directly to the default printer using PDFBox and the Java Print Service.
	 * This method is self-contained and does not rely on OS file associations.
	 *
	 * @param nodeToPrint     The JavaFX node to be printed.
	 * @param ownerStage      The parent stage for any alerts.
	 * @param onPrintComplete A callback to report whether the print job was successful.
	 */
	public void printNode(Node nodeToPrint, Stage ownerStage, Consumer<Boolean> onPrintComplete) {
		PDDocument document = null;
		try {
			// Step 1: Find the default system printer
			PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();
			if (defaultPrinter == null) {
				showAlert(ownerStage, "Printing Error", "No default printer found. Please configure a printer in your system settings.");
				onPrintComplete.accept(false);
				return;
			}
			System.out.println("✅ Found default printer: " + defaultPrinter.getName());

			// Step 2: Create the PDF document from the JavaFX node in memory
			if (nodeToPrint.getScene() == null) {
				new Scene(new Group(nodeToPrint)); // Node must be in a scene to be rendered
			}
			final int scale = 3; // Use a higher scale for better quality
			SnapshotParameters params = new SnapshotParameters();
			params.setFill(Color.WHITE);
			params.setTransform(javafx.scene.transform.Transform.scale(scale, scale));
			WritableImage image = nodeToPrint.snapshot(params, null);
			BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

			document = new PDDocument();
			float width = (float) image.getWidth();
			float height = (float) image.getHeight();
			PDPage page = new PDPage(new PDRectangle(width, height));
			document.addPage(page);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "png", baos);
			byte[] imageInByte = baos.toByteArray();

			PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageInByte, "receipt");
			try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
				contentStream.drawImage(pdImage, 0, 0, width, height);
			}

			// Step 3: Create a Java PrinterJob and set the PDF document as printable
			PrinterJob job = PrinterJob.getPrinterJob();
			job.setPrintService(defaultPrinter);
			job.setPageable(new PDFPageable(document));

			// Step 4: Print the job
			job.print();
			System.out.println("✅ Print job sent to printer successfully.");
			onPrintComplete.accept(true);

		} catch (PrinterException e) {
			showAlert(ownerStage, "Printing Error", "Failed to print document: " + e.getMessage());
			e.printStackTrace();
			onPrintComplete.accept(false);
		} catch (IOException e) {
			showAlert(ownerStage, "PDF Creation Error", "Failed to create PDF for printing: " + e.getMessage());
			e.printStackTrace();
			onPrintComplete.accept(false);
		} finally {
			// Step 5: Clean up the in-memory PDF document
			if (document != null) {
				try {
					document.close();
				} catch (IOException e) {
					System.err.println("Error closing PDDocument: " + e.getMessage());
				}
			}
		}
	}


	// --- NEW METHOD for Karyakrama Preview ---
	public void showKaryakramaPrintPreview(KaryakramaReceiptData data, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed) {
		Stage previewStage = new Stage();
		previewStage.initModality(Modality.WINDOW_MODAL);
		previewStage.initOwner(ownerStage);
		previewStage.setTitle("Karyakrama Receipt Preview");
		Node receiptNode = createKaryakramaReceiptNode(data);

		// Standard preview boilerplate
		VBox previewContainer = new VBox(receiptNode);
		previewContainer.setPrefWidth(RECEIPT_WIDTH_POINTS);
		previewContainer.setMaxWidth(RECEIPT_WIDTH_POINTS);
		previewContainer.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");
		double scaleFactor = 1.3;
		previewContainer.setScaleX(scaleFactor);
		previewContainer.setScaleY(scaleFactor);
		Group scaledContainer = new Group(previewContainer);
		ScrollPane scrollPane = new ScrollPane(scaledContainer);
		scrollPane.setPrefViewportWidth(RECEIPT_WIDTH_POINTS * scaleFactor + 20);
		scrollPane.setPrefViewportHeight(450);

		Button printButton = new Button("Print");
		printButton.setOnAction(e -> {
			printNode(receiptNode, previewStage, success -> {
				if (onPrintComplete != null) Platform.runLater(() -> onPrintComplete.accept(success));
			});
			previewStage.close();
		});
		Button savePdfButton = new Button("Save as PDF");
		savePdfButton.setOnAction(e -> {
			saveKaryakramaReceiptAsPdf(data, success -> {
				if (onPrintComplete != null) Platform.runLater(() -> onPrintComplete.accept(success));
			});
			previewStage.close();
		});
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(e -> previewStage.close());
		previewStage.setOnCloseRequest(e -> {
			if (onDialogClosed != null) onDialogClosed.run();
		});

		HBox buttonBox = new HBox(10, printButton, savePdfButton, cancelButton);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setPadding(new Insets(10));
		VBox layout = new VBox(10, scrollPane, buttonBox);
		layout.setAlignment(Pos.CENTER);
		Scene scene = new Scene(layout, 450, 600);
		previewStage.setScene(scene);
		previewStage.show();
	}

	// Other show...Preview methods remain the same...
	public void showPrintPreview(SevaReceiptData data, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed) {
		Stage previewStage = new Stage();
		previewStage.initModality(Modality.WINDOW_MODAL);
		previewStage.initOwner(ownerStage);
		previewStage.setTitle("Print Preview");
		Node receiptNode = createReceiptNode(data);

		VBox previewContainer = new VBox(receiptNode);
		previewContainer.setPrefWidth(RECEIPT_WIDTH_POINTS);
		previewContainer.setMaxWidth(RECEIPT_WIDTH_POINTS);
		previewContainer.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");
		double scaleFactor = 1.3;
		previewContainer.setScaleX(scaleFactor);
		previewContainer.setScaleY(scaleFactor);

		Group scaledContainer = new Group(previewContainer);
		scaledContainer.setAutoSizeChildren(true);
		ScrollPane scrollPane = new ScrollPane(scaledContainer);
		scrollPane.setFitToWidth(false);
		scrollPane.setFitToHeight(false);
		scrollPane.setPrefViewportWidth(RECEIPT_WIDTH_POINTS * scaleFactor + 20);
		scrollPane.setPrefViewportHeight(450);

		Button printButton = new Button("Print");
		printButton.setOnAction(e -> {
			printNode(receiptNode, previewStage, success -> {
				if (onPrintComplete != null) {
					Platform.runLater(() -> onPrintComplete.accept(success));
				}
			});
			previewStage.close();
		});
		Button savePdfButton = new Button("Save as PDF");
		savePdfButton.setOnAction(e -> {
			saveSevaReceiptAsPdf(data, success -> {
				if (onPrintComplete != null) {
					Platform.runLater(() -> onPrintComplete.accept(success));
				}
			});
			previewStage.close();
		});
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(e -> previewStage.close());
		previewStage.setOnCloseRequest(e -> {
			if (onDialogClosed != null) {
				onDialogClosed.run();
			}
		});
		HBox buttonBox = new HBox(10, printButton, savePdfButton, cancelButton);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setPadding(new Insets(10));
		VBox layout = new VBox(10, scrollPane, buttonBox);
		layout.setAlignment(Pos.CENTER);
		scrollPane.setPrefViewportHeight(800);
		Scene scene = new Scene(layout, 450, 600);
		previewStage.setScene(scene);
		previewStage.show();
	}

	public void showDonationPrintPreview(DonationReceiptData data, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed) {
		Stage previewStage = new Stage();
		previewStage.initModality(Modality.WINDOW_MODAL);
		previewStage.initOwner(ownerStage);
		previewStage.setTitle("Donation Print Preview");
		Node receiptNode = createDonationReceiptNode(data);

		VBox previewContainer = new VBox(receiptNode);
		previewContainer.setPrefWidth(RECEIPT_WIDTH_POINTS);
		previewContainer.setMaxWidth(RECEIPT_WIDTH_POINTS);
		previewContainer.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");
		double scaleFactor = 1.3;
		previewContainer.setScaleX(scaleFactor);
		previewContainer.setScaleY(scaleFactor);

		Group scaledContainer = new Group(previewContainer);
		scaledContainer.setAutoSizeChildren(true);
		ScrollPane scrollPane = new ScrollPane(scaledContainer);
		scrollPane.setFitToWidth(false);
		scrollPane.setFitToHeight(false);
		scrollPane.setPrefViewportWidth(RECEIPT_WIDTH_POINTS * scaleFactor + 20);
		scrollPane.setPrefViewportHeight(450);

		Button printButton = new Button("Print");
		printButton.setOnAction(e -> {
			printNode(receiptNode, previewStage, success -> {
				if (onPrintComplete != null) {
					Platform.runLater(() -> onPrintComplete.accept(success));
				}
			});
			previewStage.close();
		});
		Button savePdfButton = new Button("Save as PDF");
		savePdfButton.setOnAction(e -> {
			saveDonationReceiptAsPdf(data, success -> {
				if (onPrintComplete != null) {
					Platform.runLater(() -> onPrintComplete.accept(success));
				}
			});
			previewStage.close();
		});
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(e -> previewStage.close());
		previewStage.setOnCloseRequest(e -> {
			if (onDialogClosed != null) {
				onDialogClosed.run();
			}
		});
		HBox buttonBox = new HBox(10, printButton, savePdfButton, cancelButton);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setPadding(new Insets(10));
		VBox layout = new VBox(10, scrollPane, buttonBox);
		layout.setAlignment(Pos.CENTER);
		scrollPane.setPrefViewportHeight(800);
		Scene scene = new Scene(layout, 450, 600);
		previewStage.setScene(scene);
		previewStage.show();
	}

	public void showShashwathaPoojaPrintPreview(ShashwathaPoojaReceipt data, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed) {
		Stage previewStage = new Stage();
		previewStage.initModality(Modality.WINDOW_MODAL);
		previewStage.initOwner(ownerStage);
		previewStage.setTitle("Shashwatha Pooja Print Preview");

		Node receiptNode = createShashwathaPoojaReceiptNode(data);

		VBox previewContainer = new VBox(receiptNode);
		previewContainer.setPrefWidth(RECEIPT_WIDTH_POINTS);
		previewContainer.setMaxWidth(RECEIPT_WIDTH_POINTS);
		previewContainer.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");

		double scaleFactor = 1.3;
		previewContainer.setScaleX(scaleFactor);
		previewContainer.setScaleY(scaleFactor);

		Group scaledContainer = new Group(previewContainer);
		scaledContainer.setAutoSizeChildren(true);
		ScrollPane scrollPane = new ScrollPane(scaledContainer);
		scrollPane.setFitToWidth(false);
		scrollPane.setFitToHeight(false);
		scrollPane.setPrefViewportWidth(RECEIPT_WIDTH_POINTS * scaleFactor + 20);
		scrollPane.setPrefViewportHeight(450);

		Button printButton = new Button("Print");
		printButton.setOnAction(e -> {
			printNode(receiptNode, previewStage, success -> {
				if (onPrintComplete != null) {
					Platform.runLater(() -> onPrintComplete.accept(success));
				}
			});
			previewStage.close();
		});
		Button savePdfButton = new Button("Save as PDF");
		savePdfButton.setOnAction(e -> {
			saveShashwathaPoojaReceiptAsPdf(data, success -> {
				if (onPrintComplete != null) {
					Platform.runLater(() -> onPrintComplete.accept(success));
				}
			});
			previewStage.close();
		});
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(e -> previewStage.close());
		previewStage.setOnCloseRequest(e -> {
			if (onDialogClosed != null) {
				onDialogClosed.run();
			}
		});
		HBox buttonBox = new HBox(10, printButton, savePdfButton, cancelButton);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setPadding(new Insets(10));
		VBox layout = new VBox(10, scrollPane, buttonBox);
		layout.setAlignment(Pos.CENTER);
		scrollPane.setPrefViewportHeight(800);
		Scene scene = new Scene(layout, 450, 600);
		previewStage.setScene(scene);
		previewStage.show();
	}

	public void showInKindDonationPrintPreview(InKindDonation data, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed) {
		Stage previewStage = new Stage();
		previewStage.initModality(Modality.WINDOW_MODAL);
		previewStage.initOwner(ownerStage);
		previewStage.setTitle("In-Kind Donation Print Preview");

		Node receiptNode = createInKindDonationReceiptNode(data);

		VBox previewContainer = new VBox(receiptNode);
		previewContainer.setPrefWidth(RECEIPT_WIDTH_POINTS);
		previewContainer.setMaxWidth(RECEIPT_WIDTH_POINTS);
		previewContainer.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");

		double scaleFactor = 1.3;
		previewContainer.setScaleX(scaleFactor);
		previewContainer.setScaleY(scaleFactor);
		Group scaledContainer = new Group(previewContainer);
		scaledContainer.setAutoSizeChildren(true);
		ScrollPane scrollPane = new ScrollPane(scaledContainer);
		scrollPane.setFitToWidth(false);
		scrollPane.setFitToHeight(false);
		scrollPane.setPrefViewportWidth(RECEIPT_WIDTH_POINTS * scaleFactor + 20);
		scrollPane.setPrefViewportHeight(450);

		Button printButton = new Button("Print");
		printButton.setOnAction(e -> {
			printNode(receiptNode, previewStage, success -> {
				if (onPrintComplete != null) {
					Platform.runLater(() -> onPrintComplete.accept(success));
				}
			});
			previewStage.close();
		});
		Button savePdfButton = new Button("Save as PDF");
		savePdfButton.setOnAction(e -> {
			saveInKindDonationReceiptAsPdf(data, success -> {
				if (onPrintComplete != null) {
					Platform.runLater(() -> onPrintComplete.accept(success));
				}
			});
			previewStage.close();
		});
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(e -> previewStage.close());
		previewStage.setOnCloseRequest(e -> {
			if (onDialogClosed != null) {
				onDialogClosed.run();
			}
		});
		HBox buttonBox = new HBox(10, printButton, savePdfButton, cancelButton);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setPadding(new Insets(10));
		VBox layout = new VBox(10, scrollPane, buttonBox);
		layout.setAlignment(Pos.CENTER);
		scrollPane.setPrefViewportHeight(800);
		Scene scene = new Scene(layout, 450, 600);
		previewStage.setScene(scene);
		previewStage.show();
	}

	// ... save...AsPdf methods remain the same ...
	private void saveNodeAsPdf(Node nodeToSave, File outputFile, Consumer<Boolean> onSaveComplete) {
		try {
			if (nodeToSave.getScene() == null) {
				new Scene(new Group(nodeToSave));
			}
			final int scale = 3;
			SnapshotParameters params = new SnapshotParameters();
			params.setFill(Color.WHITE);
			params.setTransform(javafx.scene.transform.Transform.scale(scale, scale));
			WritableImage image = nodeToSave.snapshot(params, null);
			BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
			try (PDDocument document = new PDDocument()) {
				float width = (float) image.getWidth();
				float height = (float) image.getHeight();
				PDPage page = new PDPage(new PDRectangle(width, height));
				document.addPage(page);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(bufferedImage, "png", baos);
				byte[] imageInByte = baos.toByteArray();

				PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageInByte, outputFile.getName());
				try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
					contentStream.drawImage(pdImage, 0, 0, width, height);
				}

				document.save(outputFile);
				System.out.println("PDF snapshot saved to: " + outputFile.getAbsolutePath());
				onSaveComplete.accept(true);
			}

		} catch (IOException e) {
			e.printStackTrace();
			showAlert(controller.mainStage, "PDF Snapshot Error", "Failed to save receipt as PDF: " + e.getMessage());
			onSaveComplete.accept(false);
		}
	}

	public void saveSevaReceiptAsPdf(SevaReceiptData data, Consumer<Boolean> onSaveComplete) {
		try {
			String userDesktop = System.getProperty("user.home") + File.separator + "Desktop";
			Path directoryPath = Paths.get(userDesktop, "CHERKABE_SEVAS", "SEVA_RECEIPTS");
			Files.createDirectories(directoryPath);

			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			String fileName = String.format("Seva-%d-%s.pdf", data.getReceiptId(), timestamp);
			File file = new File(directoryPath.toFile(), fileName);

			Node receiptNode = createReceiptNode(data);
			saveNodeAsPdf(receiptNode, file, onSaveComplete);

		} catch (IOException e) {
			e.printStackTrace();
			showAlert(controller.mainStage, "File Error", "Could not create directory for PDF: " + e.getMessage());
			onSaveComplete.accept(false);
		}
	}

	// --- NEW METHOD ---
	public void saveKaryakramaReceiptAsPdf(KaryakramaReceiptData data, Consumer<Boolean> onSaveComplete) {
		try {
			String userDesktop = System.getProperty("user.home") + File.separator + "Desktop";
			Path directoryPath = Paths.get(userDesktop, "CHERKABE_SEVAS", "KARYAKRAMA_RECEIPTS");
			Files.createDirectories(directoryPath);

			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			String fileName = String.format("Karyakrama-%d-%s.pdf", data.getReceiptId(), timestamp);
			File file = new File(directoryPath.toFile(), fileName);

			Node receiptNode = createKaryakramaReceiptNode(data);
			saveNodeAsPdf(receiptNode, file, onSaveComplete);

		} catch (IOException e) {
			e.printStackTrace();
			showAlert(controller.mainStage, "File Error", "Could not create directory for PDF: " + e.getMessage());
			onSaveComplete.accept(false);
		}
	}

	// Other save...AsPdf methods...
	public void saveDonationReceiptAsPdf(DonationReceiptData data, Consumer<Boolean> onSaveComplete) {
		try {
			String userDesktop = System.getProperty("user.home") + File.separator + "Desktop";
			Path directoryPath = Paths.get(userDesktop, "CHERKABE_SEVAS", "DONATION_RECEIPTS");
			Files.createDirectories(directoryPath);

			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			String fileName = String.format("Donation-%s-%d-%s.pdf", data.getDonationName().replaceAll("[^a-zA-Z0-9]", ""), data.getDonationReceiptId(), timestamp);
			File file = new File(directoryPath.toFile(), fileName);

			Node receiptNode = createDonationReceiptNode(data);
			saveNodeAsPdf(receiptNode, file, onSaveComplete);

		} catch (IOException e) {
			e.printStackTrace();
			showAlert(controller.mainStage, "File Error", "Could not create directory for PDF: " + e.getMessage());
		}
	}

	public void saveInKindDonationReceiptAsPdf(InKindDonation data, Consumer<Boolean> onSaveComplete) {
		try {
			String userDesktop = System.getProperty("user.home") + File.separator + "Desktop";
			Path directoryPath = Paths.get(userDesktop, "CHERKABE_SEVAS", "INKIND_DONATION_RECEIPTS");
			Files.createDirectories(directoryPath);

			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			String fileName = String.format("InKind-%d-%s.pdf", data.getInKindReceiptId(), timestamp);
			File file = new File(directoryPath.toFile(), fileName);

			Node receiptNode = createInKindDonationReceiptNode(data);
			saveNodeAsPdf(receiptNode, file, onSaveComplete);

		} catch (IOException e) {
			e.printStackTrace();
			showAlert(controller.mainStage, "File Error", "Could not create directory for PDF: " + e.getMessage());
			onSaveComplete.accept(false);
		}
	}

	public void saveShashwathaPoojaReceiptAsPdf(ShashwathaPoojaReceipt data, Consumer<Boolean> onSaveComplete) {
		try {
			String userDesktop = System.getProperty("user.home") + File.separator + "Desktop";
			Path directoryPath = Paths.get(userDesktop, "CHERKABE_SEVAS", "SHASHWATHA_RECEIPTS");
			Files.createDirectories(directoryPath);

			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			String fileName = String.format("Shashwatha-%d-%s.pdf", data.getReceiptId(), timestamp);
			File file = new File(directoryPath.toFile(), fileName);

			Node receiptNode = createShashwathaPoojaReceiptNode(data);
			saveNodeAsPdf(receiptNode, file, onSaveComplete);

		} catch (IOException e) {
			e.printStackTrace();
			showAlert(controller.mainStage, "File Error", "Could not create directory for PDF: " + e.getMessage());
			onSaveComplete.accept(false);
		}
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

	// --- NEW METHOD ---
	public Node createKaryakramaReceiptNode(KaryakramaReceiptData data) {
		// This is essentially the same as the main Seva Receipt, but with a different title.
		VBox receiptBox = new VBox(1);
		receiptBox.setStyle("-fx-padding: 5; -fx-background-color: white;");
		receiptBox.setPrefWidth(RECEIPT_WIDTH_POINTS);
		receiptBox.setMaxWidth(RECEIPT_WIDTH_POINTS);
		// Header (temple name, address, etc)
		Text templeName = new Text(ConfigManager.getInstance().getProperty("temple.name"));
		templeName.setFont(Font.font("Noto Sans Kannada", FontWeight.BOLD, 12));
		VBox heading = new VBox(templeName);
		heading.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(heading);

		VBox subHeadings = new VBox(1);
		subHeadings.setStyle("-fx-alignment: center;");
		subHeadings.getChildren().addAll(
				new Text(ConfigManager.getInstance().getProperty("temple.location")),
				new Text(ConfigManager.getInstance().getProperty("temple.postal")),
				new Text(ConfigManager.getInstance().getProperty("temple.phone"))
		);
		subHeadings.getChildren().forEach(node -> ((Text) node).setFont(Font.font("Noto Sans Kannada", 9)));
		receiptBox.getChildren().add(subHeadings);
		receiptBox.getChildren().add(new VBox(new Text("****************************************")) {{ setAlignment(Pos.CENTER); }});
		// Title
		Text receiptTitle = new Text("ಕಾರ್ಯಕ್ರಮದ ಇತರೆ ರಶೀದಿ");
		receiptTitle.setFont(Font.font("Noto Sans Kannada", FontWeight.BOLD, 10));
		receiptTitle.setUnderline(true);
		VBox titleBox = new VBox(receiptTitle);
		titleBox.setStyle("-fx-alignment: center; -fx-padding: 5 0;");
		receiptBox.getChildren().add(titleBox);

		// Devotee Details
		VBox detailsVBox = new VBox(2);
		detailsVBox.getChildren().add(createDetailRow("ರಶೀದಿ ಸಂಖ್ಯೆ: ", String.valueOf(data.getReceiptId())));
		detailsVBox.getChildren().add(createDetailRow("ಭಕ್ತರ ಹೆಸರು: ", data.getDevoteeName()));
		if (data.getPhoneNumber() != null && !data.getPhoneNumber().isEmpty()) {
			detailsVBox.getChildren().add(createDetailRow("ದೂರವಾಣಿ: ", data.getPhoneNumber()));
		}
		detailsVBox.getChildren().add(createDetailRow("ದಿನಾಂಕ: ", data.getFormattedReceiptDate()));
		receiptBox.getChildren().add(detailsVBox);
		receiptBox.getChildren().add(new Text(""));
		// Seva Table
		GridPane headerGrid = new GridPane();
		headerGrid.setPrefWidth(RECEIPT_WIDTH_POINTS - 10);
		ColumnConstraints col1 = new ColumnConstraints() {{ setPercentWidth(60); setHalignment(HPos.LEFT); }};
		ColumnConstraints col2 = new ColumnConstraints() {{ setPercentWidth(20); setHalignment(HPos.CENTER); }};
		ColumnConstraints col3 = new ColumnConstraints() {{ setPercentWidth(20); setHalignment(HPos.RIGHT); }};
		headerGrid.getColumnConstraints().addAll(col1, col2, col3);
		Label sevaLabel = new Label("ಇತರೆಯ ಹೆಸರು") {{ setStyle("-fx-font-family: 'Noto Sans Kannada'; -fx-font-size: 9px; -fx-font-weight: bold;"); }};
		Label pramanaLabel = new Label("ಪ್ರಮಾಣ") {{ setStyle("-fx-font-family: 'Noto Sans Kannada'; -fx-font-size: 9px; -fx-font-weight: bold;"); }};
		Label mottaLabel = new Label("ಮೊತ್ತ") {{ setStyle("-fx-font-family: 'Noto Sans Kannada'; -fx-font-size: 9px; -fx-font-weight: bold;"); }};
		headerGrid.add(sevaLabel, 0, 0);
		headerGrid.add(pramanaLabel, 1, 0);
		headerGrid.add(mottaLabel, 2, 0);

		GridPane dataGrid = new GridPane();
		dataGrid.setPrefWidth(RECEIPT_WIDTH_POINTS - 10);
		dataGrid.getColumnConstraints().addAll(col1, col2, col3);
		for (SevaEntry seva : data.getSevas()) {
			dataGrid.addRow(dataGrid.getRowCount(),
					new Label(seva.getName()) {{ setWrapText(true); setStyle("-fx-font-size: 9px;"); }},
					new Label(String.valueOf(seva.getQuantity())) {{ setStyle("-fx-font-size: 9px;"); }},
					new Label("₹" + String.format("%.2f", seva.getTotalAmount())) {{ setStyle("-fx-font-size: 9px;"); }}
			);
		}

		receiptBox.getChildren().addAll(headerGrid, new Text("-".repeat(50)), dataGrid, new Text("-".repeat(50)), new Text(""));

		// Total and Footer
		Text totalText = new Text("ಒಟ್ಟು ಮೊತ್ತ: ₹" + String.format("%.2f", data.getTotalAmount()));
		totalText.setFont(Font.font("Noto Sans Kannada", FontWeight.BOLD, 11));
		HBox totalBox = new HBox(totalText) {{ setAlignment(Pos.CENTER); }};
		receiptBox.getChildren().add(totalBox);
		receiptBox.getChildren().add(new Text(""));
		Text blessing = new Text("ಶ್ರೀ ದೇವರ ಕೃಪೆ ಸದಾ ನಿಮ್ಮ ಮೇಲಿರಲಿ!");
		blessing.setFont(Font.font("Noto Sans Kannada", FontPosture.ITALIC, 10));
		VBox blessingBox = new VBox(blessing) {{ setStyle("-fx-alignment: center;"); }};
		receiptBox.getChildren().add(blessingBox);

		return receiptBox;
	}

	// Other create...Node methods remain the same...
	public Node createReceiptNode(SevaReceiptData data) {
		VBox receiptBox = new VBox(1);
		receiptBox.setStyle("-fx-padding: 5; -fx-background-color: white;");
		receiptBox.setPrefWidth(RECEIPT_WIDTH_POINTS);
		receiptBox.setMaxWidth(RECEIPT_WIDTH_POINTS);

		Text asteriskLine = new Text("****************************************");
		asteriskLine.setFont(Font.font("Noto Sans Kannada", 9));
		receiptBox.getChildren().add(new VBox(asteriskLine) {{ setAlignment(Pos.CENTER); }});

		Text templeName = new Text(ConfigManager.getInstance().getProperty("temple.name"));
		templeName.setFont(Font.font("Noto Sans Kannada", FontWeight.BOLD, 12));
		VBox heading = new VBox(templeName);
		heading.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(heading);

		VBox subHeadings = new VBox(1);
		subHeadings.setStyle("-fx-alignment: center;");
		subHeadings.getChildren().addAll(
				new Text(ConfigManager.getInstance().getProperty("temple.location")),
				new Text(ConfigManager.getInstance().getProperty("temple.postal")),
				new Text(ConfigManager.getInstance().getProperty("temple.phone"))
		);
		subHeadings.getChildren().forEach(node -> ((Text) node).setFont(Font.font("Noto Sans Kannada", 9)));
		receiptBox.getChildren().add(subHeadings);
		receiptBox.getChildren().add(new VBox(new Text("****************************************")) {{ setAlignment(Pos.CENTER); }});

		receiptBox.getChildren().add(new Text(""));
		Text receiptTitle = new Text("ಸೇವಾ ರಶೀದಿ");
		receiptTitle.setFont(Font.font("Noto Sans Kannada", FontWeight.BOLD, 10));
		receiptTitle.setUnderline(true);
		VBox titleBox = new VBox(receiptTitle);
		titleBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(titleBox);
		receiptBox.getChildren().add(new Text(""));

		VBox detailsVBox = new VBox(2);
		detailsVBox.getChildren().add(createDetailRow("ರಶೀದಿ ಸಂಖ್ಯೆ: ", String.valueOf(data.getReceiptId())));
		detailsVBox.getChildren().add(createDetailRow("ಭಕ್ತರ ಹೆಸರು: ", (data.getDevoteeName().isEmpty() ? "---" : data.getDevoteeName())));
		if (data.getPhoneNumber() != null && !data.getPhoneNumber().isEmpty()) {
			detailsVBox.getChildren().add(createDetailRow("ದೂರವಾಣಿ: ", data.getPhoneNumber()));
		}
		if (data.getNakshatra() != null && !data.getNakshatra().isEmpty()) {
			detailsVBox.getChildren().add(createDetailRow("ಜನ್ಮ ನಕ್ಷತ್ರ: ", data.getNakshatra()));
		}
		if (data.getRashi() != null && !data.getRashi().isEmpty() && !data.getRashi().equals("ಆಯ್ಕೆ")) {
			detailsVBox.getChildren().add(createDetailRow("ಜನ್ಮ ರಾಶಿ: ", data.getRashi()));
		}
		detailsVBox.getChildren().add(createDetailRow("ದಿನಾಂಕ: ", data.getFormattedDate()));
		receiptBox.getChildren().add(detailsVBox);
		receiptBox.getChildren().add(new Text(""));
		GridPane headerGrid = new GridPane();
		headerGrid.setPrefWidth(RECEIPT_WIDTH_POINTS - 10);
		headerGrid.setMaxWidth(RECEIPT_WIDTH_POINTS - 10);

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(60);
		col1.setHalignment(HPos.LEFT);
		col1.setHgrow(Priority.ALWAYS);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPercentWidth(20);
		col2.setHalignment(HPos.CENTER);

		ColumnConstraints col3 = new ColumnConstraints();
		col3.setPercentWidth(20);
		col3.setHalignment(HPos.RIGHT);

		headerGrid.getColumnConstraints().addAll(col1, col2, col3);
		Label sevaLabel = new Label("ಸೇವೆಯ ಹೆಸರು");
		sevaLabel.setStyle("-fx-font-family: 'Noto Sans Kannada'; -fx-font-size: 9px; -fx-font-weight: bold; -fx-stroke: black; -fx-stroke-width: 0.15;");
		Label pramanaLabel = new Label("ಪ್ರಮಾಣ");
		pramanaLabel.setStyle("-fx-font-family: 'Noto Sans Kannada'; -fx-font-size: 9px; -fx-font-weight: bold; -fx-stroke: black; -fx-stroke-width: 0.15;");
		Label mottaLabel = new Label("ಮೊತ್ತ");
		mottaLabel.setStyle("-fx-font-family: 'Noto Sans Kannada'; -fx-font-size: 9px; -fx-font-weight: bold; -fx-stroke: black; -fx-stroke-width: 0.15;");

		headerGrid.add(sevaLabel, 0, 0);
		headerGrid.add(pramanaLabel, 1, 0);
		headerGrid.add(mottaLabel, 2, 0);

		GridPane dataGrid = new GridPane();
		dataGrid.setPrefWidth(RECEIPT_WIDTH_POINTS - 10);
		dataGrid.setMaxWidth(RECEIPT_WIDTH_POINTS - 10);
		dataGrid.getColumnConstraints().addAll(col1, col2, col3);
		for (SevaEntry seva : data.getSevas()) {
			Label name = new Label(seva.getName());
			name.setWrapText(true);
			name.setStyle("-fx-font-size: 9px;");

			Label qty = new Label(String.valueOf(seva.getQuantity()));
			qty.setStyle("-fx-font-size: 9px;");
			Label total = new Label("₹" + String.format("%.2f", seva.getTotalAmount()));
			total.setStyle("-fx-font-size: 9px;");
			dataGrid.addRow(dataGrid.getRowCount(), name, qty, total);
		}

		receiptBox.getChildren().add(headerGrid);
		receiptBox.getChildren().add(new Text("-".repeat(50)));
		receiptBox.getChildren().add(dataGrid);
		receiptBox.getChildren().add(new Text("-".repeat(50)));
		receiptBox.getChildren().add(new Text(""));

		Text totalText = new Text("ಒಟ್ಟು ಮೊತ್ತ: ₹" + String.format("%.2f", data.getTotalAmount()));
		totalText.setFont(Font.font("Noto Sans Kannada", FontWeight.BOLD, 11));
		HBox totalBox = new HBox(totalText);
		totalBox.setAlignment(Pos.CENTER);
		receiptBox.getChildren().add(totalBox);
		receiptBox.getChildren().add(new Text(""));

		Text blessing = new Text("ಶ್ರೀ ದೇವರ ಕೃಪೆ ಸದಾ ನಿಮ್ಮ ಮೇಲಿರಲಿ!");
		blessing.setFont(Font.font("Noto Sans Kannada", FontPosture.ITALIC, 10));
		VBox blessingBox = new VBox(blessing);
		blessingBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(blessingBox);

		return receiptBox;
	}

	public Node createDonationReceiptNode(DonationReceiptData data) {
		VBox receiptBox = new VBox(1);
		receiptBox.setStyle("-fx-padding: 5; -fx-background-color: white;");
		receiptBox.setMaxWidth(RECEIPT_WIDTH_POINTS);
		receiptBox.setPrefWidth(RECEIPT_WIDTH_POINTS);

		Text asteriskLine = new Text("****************************************");
		asteriskLine.setFont(Font.font("Noto Sans Kannada", 9));
		receiptBox.getChildren().add(new VBox(asteriskLine) {{ setAlignment(Pos.CENTER); }});

		Text templeName = new Text(ConfigManager.getInstance().getProperty("temple.name"));
		templeName.setFont(Font.font("Noto Sans Kannada", FontWeight.BOLD, 12));
		VBox heading = new VBox(templeName);
		heading.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(heading);

		VBox subHeadings = new VBox(1);
		subHeadings.setStyle("-fx-alignment: center;");
		subHeadings.getChildren().addAll(
				new Text(ConfigManager.getInstance().getProperty("temple.location")),
				new Text(ConfigManager.getInstance().getProperty("temple.postal")),
				new Text(ConfigManager.getInstance().getProperty("temple.phone"))
		);
		subHeadings.getChildren().forEach(node -> ((Text) node).setFont(Font.font("Noto Sans Kannada", 9)));
		receiptBox.getChildren().add(subHeadings);
		receiptBox.getChildren().add(new VBox(new Text("****************************************")) {{ setAlignment(Pos.CENTER); }});

		receiptBox.getChildren().add(new Text(""));
		Text receiptTitle = new Text("ದೇಣಿಗೆ ರಶೀದಿ");
		receiptTitle.setFont(Font.font("Noto Sans Kannada", FontWeight.BOLD, 10));
		receiptTitle.setUnderline(true);
		VBox titleBox = new VBox(receiptTitle);
		titleBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(titleBox);
		receiptBox.getChildren().add(new Text(""));

		VBox detailsVBox = new VBox(2);
		detailsVBox.getChildren().add(createDetailRow("ರಶೀದಿ ಸಂಖ್ಯೆ: ", String.valueOf(data.getDonationReceiptId())));
		detailsVBox.getChildren().add(createDetailRow("ಭಕ್ತರ ಹೆಸರು: ", (data.getDevoteeName().isEmpty() ? "---" : data.getDevoteeName())));
		if (data.getPhoneNumber() != null && !data.getPhoneNumber().isEmpty()) {
			detailsVBox.getChildren().add(createDetailRow("ದೂರವಾಣಿ: ", data.getPhoneNumber()));
		}
		if (data.getNakshatra() != null && !data.getNakshatra().isEmpty()) {
			detailsVBox.getChildren().add(createDetailRow("ಜನ್ಮ ನಕ್ಷತ್ರ: ", data.getNakshatra()));
		}
		if (data.getRashi() != null && !data.getRashi().isEmpty() && !data.getRashi().equals("ಆಯ್ಕೆ")) {
			detailsVBox.getChildren().add(createDetailRow("ಜನ್ಮ ರಾಶಿ: ", data.getRashi()));
		}
		detailsVBox.getChildren().add(createDetailRow("ದಿನಾಂಕ: ", data.getFormattedDate()));
		receiptBox.getChildren().add(detailsVBox);
		receiptBox.getChildren().add(new Text(""));
		VBox donationDetailsVBox = new VBox(2);
		donationDetailsVBox.getChildren().addAll(
				createDetailRow("ದೇಣಿಗೆ ವಿಧ: ", data.getDonationName()),
				createDetailRow("ಪಾವತಿ ವಿಧಾನ: ", data.getPaymentMode()),
				createDetailRow("ದೇಣಿಗೆ ಮೊತ್ತ: ", "₹" + String.format("%.2f", data.getDonationAmount()))
		);
		receiptBox.getChildren().add(donationDetailsVBox);
		receiptBox.getChildren().add(new Text(""));
		Text blessing = new Text("ಶ್ರೀ ದೇವರ ಕೃಪೆ ಸದಾ ನಿಮ್ಮ ಮೇಲಿರಲಿ!");
		blessing.setFont(Font.font("Noto Sans Kannada", FontPosture.ITALIC, 10));
		VBox blessingBox = new VBox(blessing);
		blessingBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(blessingBox);

		return receiptBox;
	}

	public Node createShashwathaPoojaReceiptNode(ShashwathaPoojaReceipt data) {
		VBox receiptBox = new VBox(1);
		receiptBox.setStyle("-fx-padding: 5; -fx-background-color: white;");
		receiptBox.setPrefWidth(RECEIPT_WIDTH_POINTS);
		receiptBox.setMaxWidth(RECEIPT_WIDTH_POINTS);
		Text asteriskLine = new Text("****************************************");
		asteriskLine.setFont(Font.font("Noto Sans Kannada", 9));
		receiptBox.getChildren().add(new VBox(asteriskLine) {{ setAlignment(Pos.CENTER); }});
		Text templeName = new Text(ConfigManager.getInstance().getProperty("temple.name"));
		templeName.setFont(Font.font("Noto Sans Kannada", FontWeight.BOLD, 12));
		VBox heading = new VBox(templeName);
		heading.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(heading);
		VBox subHeadings = new VBox(1);
		subHeadings.setStyle("-fx-alignment: center;");
		subHeadings.getChildren().addAll(
				new Text(ConfigManager.getInstance().getProperty("temple.location")),
				new Text(ConfigManager.getInstance().getProperty("temple.postal")),
				new Text(ConfigManager.getInstance().getProperty("temple.phone"))
		);
		subHeadings.getChildren().forEach(node -> ((Text) node).setFont(Font.font("Noto Sans Kannada", 9)));
		receiptBox.getChildren().add(subHeadings);
		receiptBox.getChildren().add(new VBox(new Text("****************************************")) {{ setAlignment(Pos.CENTER); }});

		receiptBox.getChildren().add(new Text(""));
		Text receiptTitle = new Text("ಶಾಶ್ವತ ಪೂಜೆ ರಶೀದಿ");
		receiptTitle.setFont(Font.font("Noto Sans Kannada", FontWeight.BOLD, 10));
		receiptTitle.setUnderline(true);
		VBox titleBox = new VBox(receiptTitle);
		titleBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(titleBox);
		receiptBox.getChildren().add(new Text(""));

		VBox detailsVBox = new VBox(2);
		detailsVBox.getChildren().add(createDetailRow("ರಶೀದಿ ಸಂಖ್ಯೆ: ", String.valueOf(data.getReceiptId())));
		detailsVBox.getChildren().add(createDetailRow("ಭಕ್ತರ ಹೆಸರು: ", (data.getDevoteeName().isEmpty() ? "---" : data.getDevoteeName())));
		if (data.getPhoneNumber() != null && !data.getPhoneNumber().isEmpty()) {
			detailsVBox.getChildren().add(createDetailRow("ದೂರವಾಣಿ: ", data.getPhoneNumber()));
		}
		if (data.getNakshatra() != null && !data.getNakshatra().isEmpty()) {
			detailsVBox.getChildren().add(createDetailRow("ಜನ್ಮ ನಕ್ಷತ್ರ: ", data.getNakshatra()));
		}
		if (data.getRashi() != null && !data.getRashi().isEmpty() && !data.getRashi().equals("ಆಯ್ಕೆ")) {
			detailsVBox.getChildren().add(createDetailRow("ಜನ್ಮ ರಾಶಿ: ", data.getRashi()));
		}
		detailsVBox.getChildren().add(createDetailRow("ರಶೀದಿ ದಿನಾಂಕ: ", data.getFormattedReceiptDate()));
		detailsVBox.getChildren().add(createDetailRow("ಪೂಜಾ ದಿನಾಂಕ/ವಿವರ: ", data.getPoojaDate()));
		receiptBox.getChildren().add(detailsVBox);
		receiptBox.getChildren().add(new Text(""));

		Text blessing = new Text("ಶ್ರೀ ದೇವರ ಕೃಪೆ ಸದಾ ನಿಮ್ಮ ಮೇಲಿರಲಿ!");
		blessing.setFont(Font.font("Noto Sans Kannada", FontPosture.ITALIC, 10));
		VBox blessingBox = new VBox(blessing);
		blessingBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(blessingBox);

		return receiptBox;
	}

	public Node createInKindDonationReceiptNode(InKindDonation data) {
		VBox receiptBox = new VBox(1);
		receiptBox.setStyle("-fx-padding: 5; -fx-background-color: white;");
		receiptBox.setPrefWidth(RECEIPT_WIDTH_POINTS);
		receiptBox.setMaxWidth(RECEIPT_WIDTH_POINTS);
		Text asteriskLine = new Text("****************************************");
		asteriskLine.setFont(Font.font("Noto Sans Kannada", 9));
		receiptBox.getChildren().add(new VBox(asteriskLine) {{ setAlignment(Pos.CENTER); }});
		Text templeName = new Text(ConfigManager.getInstance().getProperty("temple.name"));
		templeName.setFont(Font.font("Noto Sans Kannada", FontWeight.BOLD, 12));
		VBox heading = new VBox(templeName);
		heading.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(heading);
		VBox subHeadings = new VBox(1);
		subHeadings.setStyle("-fx-alignment: center;");
		subHeadings.getChildren().addAll(
				new Text(ConfigManager.getInstance().getProperty("temple.location")),
				new Text(ConfigManager.getInstance().getProperty("temple.postal")),
				new Text(ConfigManager.getInstance().getProperty("temple.phone"))
		);
		subHeadings.getChildren().forEach(node -> ((Text) node).setFont(Font.font("Noto Sans Kannada", 9)));
		receiptBox.getChildren().add(subHeadings);
		receiptBox.getChildren().add(new VBox(new Text("****************************************")) {{ setAlignment(Pos.CENTER); }});

		receiptBox.getChildren().add(new Text(""));
		Text receiptTitle = new Text("ವಸ್ತು ದೇಣಿಗೆ ರಶೀದಿ");
		receiptTitle.setFont(Font.font("Noto Sans Kannada", FontWeight.BOLD, 10));
		receiptTitle.setUnderline(true);
		VBox titleBox = new VBox(receiptTitle);
		titleBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(titleBox);
		receiptBox.getChildren().add(new Text(""));

		VBox detailsVBox = new VBox(2);
		detailsVBox.getChildren().add(createDetailRow("ರಶೀದಿ ಸಂಖ್ಯೆ: ", String.valueOf(data.getInKindReceiptId())));
		detailsVBox.getChildren().add(createDetailRow("ಭಕ್ತರ ಹೆಸರು: ", (data.getDevoteeName().isEmpty() ? "---" : data.getDevoteeName())));
		if (data.getPhoneNumber() != null && !data.getPhoneNumber().isEmpty()) {
			detailsVBox.getChildren().add(createDetailRow("ದೂರವಾಣಿ: ", data.getPhoneNumber()));
		}
		if (data.getNakshatra() != null && !data.getNakshatra().isEmpty()) {
			detailsVBox.getChildren().add(createDetailRow("ಜನ್ಮ ನಕ್ಷತ್ರ: ", data.getNakshatra()));
		}
		if (data.getRashi() != null && !data.getRashi().isEmpty() && !data.getRashi().equals("ಆಯ್ಕೆ")) {
			detailsVBox.getChildren().add(createDetailRow("ಜನ್ಮ ರಾಶಿ: ", data.getRashi()));
		}
		detailsVBox.getChildren().add(createDetailRow("ದಿನಾಂಕ: ", data.getFormattedDate()));
		receiptBox.getChildren().add(detailsVBox);

		receiptBox.getChildren().add(new Text(""));
		Text itemHeader = new Text("ವಸ್ತು ವಿವರಣೆ:");
		itemHeader.setFont(Font.font("Noto Sans Kannada", FontWeight.BOLD, 10));
		receiptBox.getChildren().add(itemHeader);
		receiptBox.getChildren().add(new Text("-".repeat(50)));

		Text itemDescription = new Text(data.getItemDescription());
		itemDescription.setFont(Font.font("Noto Sans Kannada", 9));
		itemDescription.setWrappingWidth(RECEIPT_WIDTH_POINTS - 20);
		receiptBox.getChildren().add(itemDescription);
		receiptBox.getChildren().add(new Text(""));
		receiptBox.getChildren().add(new Text("-".repeat(50)));
		receiptBox.getChildren().add(new Text(""));
		Text blessing = new Text("ಶ್ರೀ ದೇವರ ಕೃಪೆ ಸದಾ ನಿಮ್ಮ ಮೇಲಿರಲಿ!");
		blessing.setFont(Font.font("Noto Sans Kannada", FontPosture.ITALIC, 10));
		VBox blessingBox = new VBox(blessing);
		blessingBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(blessingBox);

		return receiptBox;
	}
}