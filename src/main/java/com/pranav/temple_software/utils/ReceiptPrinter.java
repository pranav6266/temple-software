// FILE: src/main/java/com/pranav/temple_software/utils/ReceiptPrinter.java
package com.pranav.temple_software.utils;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.DonationReceiptData;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.models.SevaReceiptData;
import com.pranav.temple_software.models.ShashwathaPoojaReceipt; // Make sure this import exists
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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
import java.util.Objects;
import java.util.function.Consumer;

public class ReceiptPrinter {

	// MODIFIED: Reduced width slightly for a safety margin on thermal printers
	private static final double RECEIPT_WIDTH_MM = 78;
	private static final double POINTS_PER_MM = 2.83465;
	private static final double RECEIPT_WIDTH_POINTS = RECEIPT_WIDTH_MM * POINTS_PER_MM;

	MainController controller;

	public ReceiptPrinter(MainController controller) {
		this.controller = controller;
	}

	public Node createReceiptNode(SevaReceiptData data) {
		VBox receiptBox = new VBox(1);
		// MODIFIED: Reduced padding
		receiptBox.setStyle("-fx-padding: 5; -fx-background-color: white;");
		receiptBox.setPrefWidth(RECEIPT_WIDTH_POINTS);
		receiptBox.setMaxWidth(RECEIPT_WIDTH_POINTS);

		// MODIFIED: Reduced font sizes for all text elements
		Text templeName = new Text("ಶ್ರೀ ಶಾಸ್ತಾರ ಸುಬ್ರಹ್ಮಣಯೇಶ್ವರ ದೇವಸ್ಥಾನ");
		templeName.setFont(Font.font("Noto Sans Kannada", 12));
		templeName.setStyle("-fx-font-weight: bold;");
		VBox heading = new VBox(templeName);
		heading.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(heading);

		VBox subHeadings = new VBox(1);
		subHeadings.setStyle("-fx-alignment: center;");
		subHeadings.getChildren().addAll(
				new Text("ಚೇರ್ಕಬೆ"),
				new Text("ಅಂಚೆ : 671552"),
				new Text("ದೂರವಾಣಿ : 6282525216, 9526431593")
		);
		subHeadings.getChildren().forEach(node -> ((Text) node).setFont(Font.font("Noto Sans Kannada", 9)));
		receiptBox.getChildren().add(subHeadings);
		receiptBox.getChildren().add(new Text(""));

		Text receiptTitle = new Text("ಸೇವಾ ರಶೀದಿ");
		receiptTitle.setFont(Font.font("Noto Sans Kannada", 10));
		receiptTitle.setStyle("-fx-font-weight: bold; -fx-underline: true;");
		VBox titleBox = new VBox(receiptTitle);
		titleBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(titleBox);
		receiptBox.getChildren().add(new Text(""));

		VBox detailsVBox = new VBox(2);
		detailsVBox.getChildren().addAll(
				new Text("ರಶೀದಿ ಸಂಖ್ಯೆ: " + data.getReceiptId()),
				new Text("ಭಕ್ತರ ಹೆಸರು: " + (data.getDevoteeName().isEmpty() ? "---" : data.getDevoteeName())),
				new Text("ದೂರವಾಣಿ: " + (data.getPhoneNumber().isEmpty() ? "---" : data.getPhoneNumber())),
				new Text("ಜನ್ಮ ನಕ್ಷತ್ರ: " + (data.getNakshatra() != null ? data.getNakshatra() : "---")),
				new Text("ಜನ್ಮ ರಾಶಿ: " + (data.getRashi() != null ? data.getRashi() : "---")),
				new Text("ದಿನಾಂಕ: " + data.getFormattedDate())
		);
		detailsVBox.getChildren().forEach(node -> ((Text) node).setFont(Font.font("Noto Sans Kannada", 9)));
		receiptBox.getChildren().add(detailsVBox);
		receiptBox.getChildren().add(new Text(""));

		// MODIFIED: Using HBox with flexible growth for the seva name column
		HBox headerRow = new HBox();
		headerRow.setPadding(new Insets(2, 0, 2, 0));
		Label sevaLabel = new Label("ಸೇವೆಯ ಹೆಸರು");
		sevaLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 9px;");
		HBox.setHgrow(sevaLabel, Priority.ALWAYS);

		Label pramanaLabel = new Label("ಪ್ರಮಾಣ");
		pramanaLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 9px; -fx-alignment: center;");
		pramanaLabel.setPrefWidth(40);

		Label mottaLabel = new Label("ಮೊತ್ತ");
		mottaLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 9px; -fx-alignment: center-right;");
		mottaLabel.setPrefWidth(55);

		headerRow.getChildren().addAll(sevaLabel, pramanaLabel, mottaLabel);
		receiptBox.getChildren().add(headerRow);
		receiptBox.getChildren().add(new Text("-".repeat(50)));

		for (SevaEntry seva : data.getSevas()) {
			HBox sevaRow = new HBox();
			Label name = new Label(seva.getName());
			name.setWrapText(true);
			name.setStyle("-fx-font-size: 9px;");
			HBox.setHgrow(name, Priority.ALWAYS);

			Label qty = new Label(String.valueOf(seva.getQuantity()));
			qty.setStyle("-fx-font-size: 9px; -fx-alignment: center;");
			qty.setPrefWidth(40);

			Label total = new Label("₹" + String.format("%.2f", seva.getTotalAmount()));
			total.setStyle("-fx-font-size: 9px; -fx-alignment: center-right;");
			total.setPrefWidth(55);

			sevaRow.getChildren().addAll(name, qty, total);
			receiptBox.getChildren().add(sevaRow);
		}
		receiptBox.getChildren().add(new Text("-".repeat(50)));
		receiptBox.getChildren().add(new Text(""));

		Text totalText = new Text("ಒಟ್ಟು ಮೊತ್ತ: ₹" + String.format("%.2f", data.getTotalAmount()));
		totalText.setFont(Font.font("Noto Sans Kannada", 11));
		totalText.setStyle("-fx-font-weight: bold;");
		HBox totalBox = new HBox(totalText);
		totalBox.setAlignment(Pos.CENTER_RIGHT);
		receiptBox.getChildren().add(totalBox);
		receiptBox.getChildren().add(new Text(""));

		Text blessing = new Text("ಶ್ರೀ ದೇವರ ಕೃಪೆ ಸದಾ ನಿಮ್ಮ ಮೇಲಿರಲಿ!");
		blessing.setFont(Font.font("Noto Sans Kannada", 10));
		blessing.setStyle("-fx-font-style: italic;");
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

		Text templeName = new Text("ಶ್ರೀ ಶಾಸ್ತಾರ ಸುಬ್ರಹ್ಮಣಯೇಶ್ವರ ದೇವಸ್ಥಾನ");
		templeName.setFont(Font.font("Noto Sans Kannada", 12));
		templeName.setStyle("-fx-font-weight: bold;");
		VBox heading = new VBox(templeName);
		heading.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(heading);

		VBox subHeadings = new VBox(1);
		subHeadings.setStyle("-fx-alignment: center;");
		subHeadings.getChildren().addAll(
				new Text("ಚೇರ್ಕಬೆ"),
				new Text("ಅಂಚೆ : 671552"),
				new Text("ದೂರವಾಣಿ : 6282525216, 9526431593")
		);
		subHeadings.getChildren().forEach(node -> ((Text) node).setFont(Font.font("Noto Sans Kannada", 9)));
		receiptBox.getChildren().add(subHeadings);
		receiptBox.getChildren().add(new Text(""));

		Text receiptTitle = new Text("ದೇಣಿಗೆ ರಶೀದಿ");
		receiptTitle.setFont(Font.font("Noto Sans Kannada", 10));
		receiptTitle.setStyle("-fx-font-weight: bold; -fx-underline: true;");
		VBox titleBox = new VBox(receiptTitle);
		titleBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(titleBox);
		receiptBox.getChildren().add(new Text(""));

		VBox detailsVBox = new VBox(2);
		detailsVBox.getChildren().addAll(
				new Text("ರಶೀದಿ ಸಂಖ್ಯೆ: " + data.getDonationReceiptId()),
				new Text("ಭಕ್ತರ ಹೆಸರು: " + (data.getDevoteeName().isEmpty() ? "---" : data.getDevoteeName())),
				new Text("ದೂರವಾಣಿ: " + (data.getPhoneNumber().isEmpty() ? "---" : data.getPhoneNumber())),
				new Text("ಜನ್ಮ ನಕ್ಷತ್ರ: " + (data.getNakshatra() != null ? data.getNakshatra() : "---")),
				new Text("ಜನ್ಮ ರಾಶಿ: " + (data.getRashi() != null && !Objects.equals(data.getRashi(), "ಆಯ್ಕೆ") ? data.getRashi() : "---")),
				new Text("ದಿನಾಂಕ: " + data.getFormattedDate())
		);
		detailsVBox.getChildren().forEach(node -> ((Text) node).setFont(Font.font("Noto Sans Kannada", 9)));
		receiptBox.getChildren().add(detailsVBox);
		receiptBox.getChildren().add(new Text(""));

		VBox donationDetailsVBox = new VBox(2);
		donationDetailsVBox.getChildren().addAll(
				new Text("ದೇಣಿಗೆ ವಿಧ: " + data.getDonationName()),
				new Text("ಪಾವತಿ ವಿಧಾನ: " + data.getPaymentMode()),
				new Text("ದೇಣಿಗೆ ಮೊತ್ತ: ₹" + String.format("%.2f", data.getDonationAmount()))
		);
		donationDetailsVBox.getChildren().forEach(node -> ((Text) node).setFont(Font.font("Noto Sans Kannada", 9)));
		receiptBox.getChildren().add(donationDetailsVBox);
		receiptBox.getChildren().add(new Text(""));


		Text blessing = new Text("ಶ್ರೀ ದೇವರ ಕೃಪೆ ಸದಾ ನಿಮ್ಮ ಮೇಲಿರಲಿ!");
		blessing.setFont(Font.font("Noto Sans Kannada", 10));
		blessing.setStyle("-fx-font-style: italic;");
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

		Text templeName = new Text(ConfigManager.getInstance().getProperty("temple.name"));
		templeName.setFont(Font.font("Noto Sans Kannada", 12));
		templeName.setStyle("-fx-font-weight: bold;");
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
		receiptBox.getChildren().add(new Text(""));

		Text receiptTitle = new Text("ಶಾಶ್ವತ ಪೂಜೆ ರಶೀದಿ");
		receiptTitle.setFont(Font.font("Noto Sans Kannada", 10));
		receiptTitle.setStyle("-fx-font-weight: bold; -fx-underline: true;");
		VBox titleBox = new VBox(receiptTitle);
		titleBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(titleBox);
		receiptBox.getChildren().add(new Text(""));

		VBox detailsVBox = new VBox(2);
		detailsVBox.getChildren().addAll(
				new Text("ರಶೀದಿ ಸಂಖ್ಯೆ: " + data.getReceiptId()),
				new Text("ಭಕ್ತರ ಹೆಸರು: " + (data.getDevoteeName().isEmpty() ? "---" : data.getDevoteeName())),
				new Text("ದೂರವಾಣಿ: " + (data.getPhoneNumber().isEmpty() ? "---" : data.getPhoneNumber())),
				new Text("ಜನ್ಮ ನಕ್ಷತ್ರ: " + (data.getNakshatra() != null ? data.getNakshatra() : "---")),
				new Text("ಜನ್ಮ ರಾಶಿ: " + (data.getRashi() != null && !Objects.equals(data.getRashi(), "ಆಯ್ಕೆ") ? data.getRashi() : "---")),
				new Text("ರಶೀದಿ ದಿನಾಂಕ: " + data.getFormattedReceiptDate()),
				new Text("ಪೂಜಾ ದಿನಾಂಕ/ವಿವರ: " + data.getPoojaDate())
		);
		detailsVBox.getChildren().forEach(node -> ((Text) node).setFont(Font.font("Noto Sans Kannada", 9)));
		receiptBox.getChildren().add(detailsVBox);
		receiptBox.getChildren().add(new Text(""));

		Text blessing = new Text("ಶ್ರೀ ದೇವರ ಕೃಪೆ ಸದಾ ನಿಮ್ಮ ಮೇಲಿರಲಿ!");
		blessing.setFont(Font.font("Noto Sans Kannada", 10));
		blessing.setStyle("-fx-font-style: italic;");
		VBox blessingBox = new VBox(blessing);
		blessingBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(blessingBox);

		return receiptBox;
	}

	// ... (Keep the rest of the file from saveNodeAsPdf onwards unchanged)
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

	public boolean printReceipt(Node nodeToPrint, Stage ownerStage) {
		PrinterJob job = PrinterJob.createPrinterJob();
		if (job == null) {
			showAlert(ownerStage, "Printing Error", "Could not create printer job.");
			return false;
		}
		boolean proceed = job.showPrintDialog(ownerStage);
		boolean printSucceeded = false;
		if (proceed) {
			Printer printer = job.getPrinter();
			Paper customPaper = Paper.NA_LETTER;
			PageLayout pageLayout = printer.createPageLayout(customPaper, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);
			if (pageLayout.getPrintableWidth() < RECEIPT_WIDTH_POINTS * 0.8) {
				pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);
				double scaleX = pageLayout.getPrintableWidth() / nodeToPrint.getBoundsInParent().getWidth();
				nodeToPrint.setScaleX(scaleX);
				nodeToPrint.setScaleY(scaleX);
			}
			boolean printed = job.printPage(pageLayout, nodeToPrint);
			if (printed) {
				printSucceeded = job.endJob();
				nodeToPrint.setScaleX(1.0);
				nodeToPrint.setScaleY(1.0);
				if (!printSucceeded) {
					showAlert(ownerStage, "Printing Failed", "Failed to finalize the print job.");
				}
			} else {
				showAlert(ownerStage, "Printing Failed", "Failed to print the page.");
				job.cancelJob();
			}
		} else {
			job.cancelJob();
		}
		return printSucceeded;
	}

	public void showPrintPreviewWithCancelCallback(SevaReceiptData data, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed) {
		Stage previewStage = new Stage();
		previewStage.initModality(Modality.WINDOW_MODAL);
		previewStage.initOwner(ownerStage);
		previewStage.setTitle("ಮುದ್ರಣ ಪೂರ್ವದರ್ಶನ");
		Node receiptNode = createReceiptNode(data);
		double scaleFactor = 1.3;
		receiptNode.setScaleX(scaleFactor);
		receiptNode.setScaleY(scaleFactor);
		Group scaledContainer = new Group(receiptNode);
		scaledContainer.setAutoSizeChildren(true);
		ScrollPane scrollPane = new ScrollPane(scaledContainer);
		scrollPane.setFitToWidth(false);
		scrollPane.setFitToHeight(false);
		scrollPane.setPrefViewportWidth(RECEIPT_WIDTH_POINTS * scaleFactor + 20);
		scrollPane.setPrefViewportHeight(450);
		Button printButton = new Button("ಮುದ್ರಿಸು");
		printButton.setOnAction(e -> {
			receiptNode.setScaleX(1.0);
			receiptNode.setScaleY(1.0);
			boolean success = printReceipt(receiptNode, ownerStage);
			if (onPrintComplete != null) {
				onPrintComplete.accept(success);
			}
			previewStage.close();
		});
		previewStage.setOnCloseRequest(e -> {
			if (onDialogClosed != null) {
				onDialogClosed.run();
			}
		});
		HBox buttonBox = new HBox(10, printButton);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setPadding(new Insets(10));
		VBox layout = new VBox(10, scrollPane, buttonBox);
		layout.setAlignment(Pos.CENTER);
		scrollPane.setPrefViewportHeight(800);
		Scene scene = new Scene(layout, 450, 600);
		previewStage.setScene(scene);
		previewStage.show();
	}

	public void showDonationPrintPreviewWithCancelCallback(DonationReceiptData data, Stage ownerStage, Consumer<Boolean> onPrintComplete, Runnable onDialogClosed) {
		Stage previewStage = new Stage();
		previewStage.initModality(Modality.WINDOW_MODAL);
		previewStage.initOwner(ownerStage);
		previewStage.setTitle("ದೇಣಿಗೆ ರಶೀದಿ ಮುದ್ರಣ ಪೂರ್ವದರ್ಶನ");
		Node receiptNode = createDonationReceiptNode(data);
		double scaleFactor = 1.3;
		receiptNode.setScaleX(scaleFactor);
		receiptNode.setScaleY(scaleFactor);
		Group scaledContainer = new Group(receiptNode);
		scaledContainer.setAutoSizeChildren(true);
		ScrollPane scrollPane = new ScrollPane(scaledContainer);
		scrollPane.setFitToWidth(false);
		scrollPane.setFitToHeight(false);
		scrollPane.setPrefViewportWidth(RECEIPT_WIDTH_POINTS * scaleFactor + 20);
		scrollPane.setPrefViewportHeight(450);
		Button printButton = new Button("ಮುದ್ರಿಸು");
		printButton.setOnAction(e -> {
			receiptNode.setScaleX(1.0);
			receiptNode.setScaleY(1.0);
			boolean success = printReceipt(receiptNode, ownerStage);
			if (onPrintComplete != null) {
				onPrintComplete.accept(success);
			}
			previewStage.close();
		});
		previewStage.setOnCloseRequest(e -> {
			if (onDialogClosed != null) {
				onDialogClosed.run();
			}
		});
		HBox buttonBox = new HBox(10, printButton);
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
		previewStage.setTitle("ಶಾಶ್ವತ ಪೂಜೆ ಮುದ್ರಣ ಪೂರ್ವದರ್ಶನ");

		Node receiptNode = createShashwathaPoojaReceiptNode(data);
		double scaleFactor = 1.3;
		receiptNode.setScaleX(scaleFactor);
		receiptNode.setScaleY(scaleFactor);

		Group scaledContainer = new Group(receiptNode);
		scaledContainer.setAutoSizeChildren(true);

		ScrollPane scrollPane = new ScrollPane(scaledContainer);
		scrollPane.setFitToWidth(false);
		scrollPane.setFitToHeight(false);
		scrollPane.setPrefViewportWidth(RECEIPT_WIDTH_POINTS * scaleFactor + 20);
		scrollPane.setPrefViewportHeight(450);

		Button printButton = new Button("ಮುದ್ರಿಸು");
		printButton.setOnAction(e -> {
			receiptNode.setScaleX(1.0);
			receiptNode.setScaleY(1.0);
			boolean success = printReceipt(receiptNode, ownerStage);
			if (onPrintComplete != null) {
				onPrintComplete.accept(success);
			}
			previewStage.close();
		});

		previewStage.setOnCloseRequest(e -> {
			if (onDialogClosed != null) {
				onDialogClosed.run();
			}
		});

		HBox buttonBox = new HBox(10, printButton);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setPadding(new Insets(10));

		VBox layout = new VBox(10, scrollPane, buttonBox);
		layout.setAlignment(Pos.CENTER);
		scrollPane.setPrefViewportHeight(800);
		Scene scene = new Scene(layout, 450, 600);
		previewStage.setScene(scene);
		previewStage.show();
	}

	private void showAlert(Stage owner, String title, String message) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}