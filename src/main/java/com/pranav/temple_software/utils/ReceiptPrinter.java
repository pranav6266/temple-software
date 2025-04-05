package com.pranav.temple_software.utils;

import com.pranav.temple_software.controllers.MainController.SevaEntry;
import com.pranav.temple_software.models.ReceiptData;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;

public class ReceiptPrinter {

	// --- Constants for Thermal Printer (Approximate - Adjust based on testing) ---
	private static final double RECEIPT_WIDTH_MM = 50;
	private static final double POINTS_PER_MM = 2.83464; // 72 points per inch / 25.4 mm per inch
	private static final double RECEIPT_WIDTH_POINTS = RECEIPT_WIDTH_MM * POINTS_PER_MM;
	private static final Font KANNADA_FONT_SMALL = Font.font("Noto Sans Kannada", FontWeight.NORMAL, 8); // Adjust font size
	private static final Font KANNADA_FONT_BOLD = Font.font("Noto Sans Kannada", FontWeight.BOLD, 9); // Adjust font size
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	// --- Method to Create the Receipt Layout as a JavaFX Node ---
	private Node createReceiptNode(ReceiptData data) {
		VBox receiptLayout = new VBox(5); // Spacing between elements
		receiptLayout.setPadding(new Insets(7,5,5,5));
		receiptLayout.setPrefWidth(RECEIPT_WIDTH_POINTS); // Set width for thermal paper
		receiptLayout.setAlignment(Pos.CENTER); // Center headings

		// 1. Heading & Subheading
		Label heading = new Label(data.getHeading());
		heading.setFont(KANNADA_FONT_BOLD);
		heading.setTextAlignment(TextAlignment.CENTER);


		Label subHeading = new Label(data.getSubHeading());
		subHeading.setFont(KANNADA_FONT_SMALL);
		subHeading.setTextAlignment(TextAlignment.CENTER);

		receiptLayout.getChildren().addAll(heading, subHeading, new Region() {
			{ VBox.setVgrow(this, Priority.ALWAYS);setMinHeight(5);}
		}); // Add some space

		// --- Left Align Details ---
		VBox detailsBox = new VBox(2);
		detailsBox.setAlignment(Pos.CENTER_LEFT);

		detailsBox.getChildren().add(new Text("ರಶೀದಿ ಸಂಖ್ಯೆ : " + data.getReceiptId()));

		// 2. Devotee Info
		detailsBox.getChildren().add(new Text("ಭಕ್ತರ ಹೆಸರು: " + data.getDevoteeName()));
		detailsBox.getChildren().add(new Text("ಭಕ್ತರ ರಾಶಿ : " + data.getRaashi()));
		detailsBox.getChildren().add(new Text("ಭಕ್ತರ ನಕ್ಷತ್ರ :" + data.getNakshatra()));

		// 3. Seva Date
		detailsBox.getChildren().add(new Text("ಸೇವಾ ದಿನಾಂಕ: " + data.getSevaDate().format(DATE_FORMATTER)));

		detailsBox.getChildren().forEach(node -> { if (node instanceof Text) ((Text)node).setFont(KANNADA_FONT_SMALL);});

		receiptLayout.getChildren().add(detailsBox);
		receiptLayout.getChildren().add(new Region() {{ VBox.setVgrow(this, Priority.ALWAYS);setMinHeight(10);}}); // Add space


		// 4. List of Sevas
		GridPane sevaGrid = new GridPane();
		sevaGrid.setHgap(10);
		sevaGrid.setVgap(2);
//		sevaGrid.add(new Text("ಸೇವೆ"), 0, 0); // Header Optional
//		sevaGrid.add(new Text("ಮೊತ್ತ"), 1, 0); // Header Optional

		int rowIndex = 0; // Start from row 0 or 1 if headers are used
		for (SevaEntry seva : data.getSevas()) {
			Text sevaName = new Text(seva.getName());
			sevaName.setFont(KANNADA_FONT_SMALL);
			Text sevaAmount = new Text(String.format("₹%.2f", seva.getAmount()));
			sevaAmount.setFont(KANNADA_FONT_SMALL);

			sevaGrid.add(sevaName, 0, rowIndex);
			GridPane.setHalignment(sevaAmount, javafx.geometry.HPos.RIGHT);
			sevaGrid.add(sevaAmount, 1, rowIndex);
			rowIndex++;
		}
		// Make amount column take remaining space
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(100); // Adjust percentage
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPercentWidth(20); // Adjust percentage
		col2.setHalignment(javafx.geometry.HPos.RIGHT);
		sevaGrid.getColumnConstraints().addAll(col1, col2);


		receiptLayout.getChildren().add(sevaGrid);
		receiptLayout.getChildren().add(new Region() {{ VBox.setVgrow(this, Priority.ALWAYS);setMinHeight(10);}}); // Add space


		// 5. Total Amount
		Label totalLabel = new Label(String.format("ಒಟ್ಟು ಮೊತ್ತ: ₹%.2f", data.getTotalAmount()));
		totalLabel.setFont(KANNADA_FONT_BOLD);
		totalLabel.setAlignment(Pos.CENTER_RIGHT);
		receiptLayout.getChildren().add(totalLabel);


		receiptLayout.getChildren().add(new Region() {{ VBox.setVgrow(this, Priority.ALWAYS);setMinHeight(15);}}); // Add space


		// 6. Final Line
		Label finalLine = new Label(data.getFinalLine());
		finalLine.setFont(KANNADA_FONT_SMALL);
		finalLine.setTextAlignment(TextAlignment.CENTER);
		receiptLayout.getChildren().add(finalLine);

		// Set max width to ensure it fits 50mm
		receiptLayout.setMaxWidth(RECEIPT_WIDTH_POINTS);


		return receiptLayout;
	}


	// --- Method for Print Preview ---
	public void showPrintPreview(ReceiptData data, Stage ownerStage) {
		Stage previewStage = new Stage();
		previewStage.initModality(Modality.WINDOW_MODAL);
		previewStage.initOwner(ownerStage);
		previewStage.setTitle("ಮುದ್ರಣ ಪೂರ್ವದರ್ಶನ"); // Print Preview Title

		Node receiptNode = createReceiptNode(data);


		double scaleFactor = 3; // Adjust this value to control preview size
		receiptNode.setScaleX(scaleFactor);
		receiptNode.setScaleY(scaleFactor);


		// Wrap in a non-clipping container (e.g., Group/Pane)
		Group scaledContainer = new Group(receiptNode);
		scaledContainer.setAutoSizeChildren(true);

		ScrollPane scrollPane = new ScrollPane(scaledContainer);
		scrollPane.setFitToWidth(false); // Disable width fitting
		scrollPane.setFitToHeight(false);
		scrollPane.setPrefViewportWidth(RECEIPT_WIDTH_POINTS * scaleFactor + 20); // Add padding
		scrollPane.setPrefViewportHeight(600); // Adjust height

		Button printButton = new Button("ಮುದ್ರಿಸು"); // Print Button
		printButton.setOnAction(e -> {
//			// Reset scale before printing
			receiptNode.setScaleX(1.0);
			receiptNode.setScaleY(1.0);
			printReceipt(receiptNode, ownerStage);
			previewStage.close();
		});

//		Button savePdfButton = new Button("PDF ಉಳಿಸು"); // Save PDF Button
//		savePdfButton.setOnAction(e -> {
//			try {
//				saveReceiptAsPdf(data, ownerStage); // Call the PDF saving method
//			} catch (IOException ioException) {
//				// Handle IO Exception (e.g., show error alert)
//				ioException.printStackTrace();
//				showAlert(ownerStage, "Error", "Failed to save PDF: " + ioException.getMessage());
//			}
//			// Optionally close preview after saving
//			// previewStage.close();
//		});


		HBox buttonBox = new HBox(10, printButton); // Add save button
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setPadding(new Insets(10));

		VBox layout = new VBox(10, scrollPane, buttonBox);
		layout.setAlignment(Pos.CENTER);
		scrollPane.setPrefViewportHeight(800);

		Scene scene = new Scene(layout, 700,1000); // Adjust preview window size
		previewStage.setScene(scene);
		previewStage.show();
	}

	// --- Method to Handle Actual Printing ---
	public void printReceipt(Node nodeToPrint, Stage ownerStage) {
		PrinterJob job = PrinterJob.createPrinterJob();

		if (job == null) {
			showAlert(ownerStage, "Printing Error", "Could not create printer job.");
			return;
		}

		// Show Print Dialog
		boolean proceed = job.showPrintDialog(ownerStage);

		if (proceed) {
			Printer printer = job.getPrinter();
			// --- Attempt to set paper size (might depend on driver) ---
			// You might need to experiment here. Standard sizes are often Letter/A4.
			// Thermal printers might require specific driver settings to handle 50mm.
			// Create a custom paper if needed, though support varies.
			Paper customPaper = Paper.NA_LETTER;  // Example height
			PageLayout pageLayout = printer.createPageLayout(customPaper, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);

			// If custom paper fails, try a default and rely on driver scaling
			if (pageLayout == null || pageLayout.getPrintableWidth() < RECEIPT_WIDTH_POINTS * 0.8) { // Check if width seems too small
				pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);
				// Scale node to fit - this might distort if aspect ratio isn't maintained
				double scaleX = pageLayout.getPrintableWidth() / nodeToPrint.getBoundsInParent().getWidth();
				// Consider scaling height proportionally if needed, or let it break across pages/cut
				nodeToPrint.setScaleX(scaleX);
				nodeToPrint.setScaleY(scaleX); // Maintain aspect ratio
			}


			boolean printed = job.printPage(pageLayout, nodeToPrint);
			if (printed) {
				job.endJob();
				// Reset scale if it was changed
				nodeToPrint.setScaleX(1.0);
				nodeToPrint.setScaleY(1.0);
			} else {
				showAlert(ownerStage, "Printing Failed", "Failed to print the page.");
				job.cancelJob(); // Attempt to cancel if printing fails
			}
		}
	}

	// --- Method to Save as PDF ---
//	public void saveReceiptAsPdf(ReceiptData data, Stage ownerStage) throws IOException {
//		javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
//		fileChooser.setTitle("Save Receipt as PDF");
//		fileChooser.setInitialFileName("Receipt_" + data.getDevoteeName().replace(" ", "_") + ".pdf");
//		fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
//
//		File file = fileChooser.showSaveDialog(ownerStage);
//
//		if (file != null) {
//			try (PDDocument document = new PDDocument()) {
//				// --- Load Kannada Font for PDFBox ---
//				// Ensure you have a suitable Kannada font file (e.g., NotoSansKannada-Regular.ttf) accessible
//				// Place it in your resources folder or provide a path
//				File fontFile = new File(getClass().getResource("/fonts/NotoSansKannada-Regular.ttf").toURI()); // Adjust path
//				PDType0Font kannadaFont = PDType0Font.load(document, fontFile);
//
//				// --- Define Page Size (approximate 50mm width) ---
//				// PDF uses points (1/72 inch). 50mm is approx 141.7 points.
//				// Height can be variable, adjust as needed or calculate based on content.
//				PDPage page = new PDPage(new org.apache.pdfbox.pdmodel.common.PDRectangle( (float)RECEIPT_WIDTH_POINTS, 600)); // Width, Height in points
//				document.addPage(page);
//
//				PDPageContentStream contentStream = new PDPageContentStream(document, page);
//
//				float yPosition = page.getMediaBox().getHeight() - 30; // Start near top
//				float leftMargin = 10;
//				float contentWidth = page.getMediaBox().getWidth() - 2 * leftMargin;
//				float lineSpacing = 12; // Adjust line spacing
//				float smallFontSize = 8f; // Adjust font size
//				float boldFontSize = 9f; // Adjust font size
//
//
//				// --- Write Content (Similar structure to createReceiptNode) ---
//
//				// Center alignment helper
//				float center = page.getMediaBox().getWidth() / 2;
//
//
//				// 1. Heading
//				contentStream.beginText();
//				contentStream.setFont(kannadaFont, boldFontSize);
//				float headingWidth = kannadaFont.getStringWidth(data.getHeading()) / 1000 * boldFontSize;
//				contentStream.newLineAtOffset(center - headingWidth / 2, yPosition);
//				contentStream.showText(data.getHeading());
//				contentStream.endText();
//				yPosition -= lineSpacing * 1.2; // Extra space after heading
//
//
//				// 2. Subheading
//				contentStream.beginText();
//				contentStream.setFont(kannadaFont, smallFontSize);
//				float subheadingWidth = kannadaFont.getStringWidth(data.getSubHeading()) / 1000 * smallFontSize;
//				contentStream.newLineAtOffset(center - subheadingWidth / 2, yPosition);
//				contentStream.showText(data.getSubHeading());
//				contentStream.endText();
//				yPosition -= lineSpacing * 1.5;
//
//				// 3. Devotee Details & Date (Left Align)
//				contentStream.beginText();
//				contentStream.setFont(kannadaFont, smallFontSize);
//				contentStream.newLineAtOffset(leftMargin, yPosition);
//				contentStream.showText("ಭಕ್ತರ ಹೆಸರು: " + data.getDevoteeName());
//				contentStream.newLineAtOffset(0, -lineSpacing); // Move down
//				contentStream.showText("ದೂರವಾಣಿ: " + data.getPhoneNumber());
//				contentStream.newLineAtOffset(0, -lineSpacing); // Move down
//				contentStream.showText("ಸೇವಾ ದಿನಾಂಕ: " + data.getSevaDate().format(DATE_FORMATTER));
//				contentStream.endText();
//				yPosition -= lineSpacing * 4; // Adjust space after details
//
//
//				// 4. Sevas List
//				float sevaNameX = leftMargin;
//				float amountX = page.getMediaBox().getWidth() - leftMargin; // Right align amount
//
//
//				for (SevaEntry seva : data.getSevas()) {
//					contentStream.beginText();
//					contentStream.setFont(kannadaFont, smallFontSize);
//					contentStream.newLineAtOffset(sevaNameX, yPosition);
//					contentStream.showText(seva.getName());
//					contentStream.endText();
//
//
//					String amountText = String.format("₹%.2f", seva.getAmount());
//					float amountWidth = kannadaFont.getStringWidth(amountText) / 1000 * smallFontSize;
//
//
//					contentStream.beginText();
//					contentStream.setFont(kannadaFont, smallFontSize);
//					contentStream.newLineAtOffset(amountX - amountWidth, yPosition); // Position right aligned
//					contentStream.showText(amountText);
//					contentStream.endText();
//
//
//					yPosition -= lineSpacing;
//				}
//				yPosition -= lineSpacing; // Extra space
//
//
//				// 5. Total Amount (Right Align)
//				String totalText = String.format("ಒಟ್ಟು ಮೊತ್ತ: ₹%.2f", data.getTotalAmount());
//				float totalWidth = kannadaFont.getStringWidth(totalText) / 1000 * boldFontSize;
//				contentStream.beginText();
//				contentStream.setFont(kannadaFont, boldFontSize);
//				contentStream.newLineAtOffset(page.getMediaBox().getWidth() - leftMargin - totalWidth, yPosition);
//				contentStream.showText(totalText);
//				contentStream.endText();
//				yPosition -= lineSpacing * 2; // Extra space
//
//
//				// 6. Final Line (Center)
//				contentStream.beginText();
//				contentStream.setFont(kannadaFont, smallFontSize);
//				float finalLineWidth = kannadaFont.getStringWidth(data.getFinalLine()) / 1000 * smallFontSize;
//				contentStream.newLineAtOffset(center - finalLineWidth / 2, yPosition);
//				contentStream.showText(data.getFinalLine());
//				contentStream.endText();
//
//
//				contentStream.close();
//				document.save(file);
//				showAlert(ownerStage, "Success", "Receipt saved as PDF:\n" + file.getAbsolutePath());
//
//
//			} catch (Exception e) { // Catch broader exceptions during PDF creation/font loading
//				throw new IOException("Error generating PDF: " + e.getMessage(), e);
//			}
//		}
//	}


	// --- Helper Alert Method ---
	private void showAlert(Stage owner, String title, String message) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}