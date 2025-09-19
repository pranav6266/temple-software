// FILE: src/main/java/com/pranav/temple_software/utils/ReceiptPrinter.java
package com.pranav.temple_software.utils;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.*;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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

	/**
	 * Creates a row for the details section with a bold label and a normal value.
	 * @param label The bolded heading text.
	 * @param value The normal font data text.
	 * @return An HBox containing the formatted label and value.
	 */
	private HBox createDetailRow(String label, String value) {
		HBox row = new HBox(4); // spacing
		Text labelText = new Text(label);
		// Apply a thin stroke to simulate a bold effect that renders in PDF
		labelText.setStyle("-fx-font-family: 'Noto Sans Kannada'; -fx-font-size: 9px; -fx-font-weight: bold; -fx-stroke: black; -fx-stroke-width: 0.15;");
		Text valueText = new Text(value);
		valueText.setFont(Font.font("Noto Sans Kannada", FontWeight.NORMAL, 9));
		row.getChildren().addAll(labelText, valueText);
		return row;
	}

	public void printNode(Node nodeToPrint, Stage ownerStage, Consumer<Boolean> onPrintComplete) {
		PrinterJob job = PrinterJob.createPrinterJob();
		if (job == null) {
			showAlert(ownerStage, "Printing Error", "Could not create a printer job.");
			onPrintComplete.accept(false);
			return;
		}

		// *** MODIFICATION START ***
		// Take a snapshot of the node. This converts the complex JavaFX node into a simple image.
		// Thermal printers can print images much more reliably than complex layouts.
		SnapshotParameters params = new SnapshotParameters();
		params.setFill(Color.WHITE); // Ensure a non-transparent background
		WritableImage image = nodeToPrint.snapshot(params, null);
		ImageView imageViewToPrint = new ImageView(image);
		// *** MODIFICATION END ***

		if (job.showPrintDialog(ownerStage)) {
			PageLayout pageLayout = job.getPrinter().getDefaultPageLayout();

			// *** MODIFICATION ***
			// We now print the 'ImageView' instead of the original 'nodeToPrint'.
			boolean printed = job.printPage(pageLayout, imageViewToPrint);
			if (printed) {
				boolean success = job.endJob();
				onPrintComplete.accept(success);
			} else {
				showAlert(ownerStage, "Printing Failed", "The print command failed.");
				job.cancelJob();
				onPrintComplete.accept(false);
			}
		} else {
			job.cancelJob();
			onPrintComplete.accept(false);
		}
	}


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
		// --- MODIFIED: The following block handles the table layout as requested ---

		// 1. Create a GridPane for just the header
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

		// 2. Create a separate GridPane for the data rows
		GridPane dataGrid = new GridPane();
		dataGrid.setPrefWidth(RECEIPT_WIDTH_POINTS - 10);
		dataGrid.setMaxWidth(RECEIPT_WIDTH_POINTS - 10);
		dataGrid.getColumnConstraints().addAll(col1, col2, col3); // Use same constraints

		for (SevaEntry seva : data.getSevas()) {
			Label name = new Label(seva.getName());
			name.setWrapText(true);
			name.setStyle("-fx-font-size: 9px;");

			Label qty = new Label(String.valueOf(seva.getQuantity()));
			qty.setStyle("-fx-font-size: 9px;");

			Label total = new Label("₹" + String.format("%.2f", seva.getTotalAmount()));
			total.setStyle("-fx-font-size: 9px;");
			// Add row to dataGrid. The row index is relative to this grid.
			dataGrid.addRow(dataGrid.getRowCount(), name, qty, total);
		}

		// 3. Add elements to the main receipt VBox in the correct order
		receiptBox.getChildren().add(headerGrid);
		receiptBox.getChildren().add(new Text("-".repeat(50)));
		receiptBox.getChildren().add(dataGrid);
		receiptBox.getChildren().add(new Text("-".repeat(50)));
		receiptBox.getChildren().add(new Text(""));
		// --- END OF MODIFICATION ---

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