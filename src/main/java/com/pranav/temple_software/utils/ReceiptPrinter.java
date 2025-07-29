package com.pranav.temple_software.utils;

import com.pranav.temple_software.controllers.MainController;
import com.pranav.temple_software.models.SevaEntry;
import com.pranav.temple_software.models.SevaReceiptData;
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
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.function.Consumer;
import com.pranav.temple_software.models.DonationReceiptData;


public class ReceiptPrinter {

	// --- Constants for Thermal Printer (Approximate - Adjust based on testing) ---
	private static final double RECEIPT_WIDTH_MM = 80;
	private static final double POINTS_PER_MM = 2.83465;
	private static final double RECEIPT_WIDTH_POINTS = RECEIPT_WIDTH_MM * POINTS_PER_MM; // ≈ 227


	MainController controller;
	public ReceiptPrinter(MainController controller){
		this.controller = controller;
	}
	// --- Method to Create the Receipt Layout as a JavaFX Node ---
	public Node createReceiptNode(SevaReceiptData data) {
		VBox receiptBox = new VBox(1);
		receiptBox.setStyle("-fx-padding: 7;");  // Left margin (adds padding to whole receipt)
		receiptBox.setPrefWidth(RECEIPT_WIDTH_POINTS);
		receiptBox.setMaxWidth(RECEIPT_WIDTH_POINTS);

		// Temple Name (Main Heading) - Left Aligned
		Text templeName = new Text("ಶ್ರೀ ಶಾಸ್ತಾರ ಸುಬ್ರಹ್ಮಣ್ಯೇಶ್ವರ ದೇವಸ್ಥಾನ");
		templeName.setFont(Font.font("Noto Sans Kannada", 16));
		templeName.setStyle("-fx-font-weight: bold; -fx-underline: true;");
		VBox heading =  new VBox(templeName);
		heading.setStyle("-fx-alignment: center; -fx-underline: true;");
		receiptBox.getChildren().add(heading);

		// Subheadings - Center Aligned
		VBox subHeadings = new VBox(2);
		subHeadings.setStyle("-fx-alignment: center;");
		subHeadings.getChildren().addAll(
				new Text("ಚೇರ್ಕಬೆ"),
				new Text("ಅಂಚೆ : 671552"),
				new Text("ದೂರವಾಣಿ: 6282525216, 9526431593")
		);
		receiptBox.getChildren().add(subHeadings);

		receiptBox.getChildren().add(new Text("")); // Spacer

		// Receipt Title - Center Aligned
		Text receiptTitle = new Text("ಸೇವಾ ರಶೀದಿ");
		receiptTitle.setFont(Font.font("Noto Sans Kannada", 14));
		receiptTitle.setStyle("-fx-underline: true;");
		VBox titleBox = new VBox(receiptTitle);
		titleBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(titleBox);

		receiptBox.getChildren().add(new Text("")); // Spacer

		// Devotee Details - Left Aligned
		receiptBox.getChildren().addAll(
				new Text("ರಶೀದಿ ಸಂಖ್ಯೆ: " + data.getReceiptId()),
				new Text("ಭಕ್ತರ ಹೆಸರು: " + (data.getDevoteeName().isEmpty() ?  "---" : data.getDevoteeName() )),
				new Text("ದೂರವಾಣಿ: " + (data.getPhoneNumber().isEmpty() ? "---" : data.getPhoneNumber())),
				new Text("ಜನ್ಮ ನಕ್ಷತ್ರ: " + (data.getNakshatra() != null ? data.getNakshatra() : "---")),
				new Text("ಜನ್ಮ ರಾಶಿ: " + (data.getRashi() != null ? data.getRashi() : "---")),
				new Text("ದಿನಾಂಕ: " + data.getFormattedDate())
		);

		receiptBox.getChildren().add(new Text("")); // Spacer

		// Seva Header Row (Fixed Column Widths + Bold Labels)
		HBox headerRow = new HBox(8); // spacing 20

		Label sevaLabel = new Label("ಸೇವೆಯ ಹೆಸರು"); sevaLabel.setPrefWidth(105);
		sevaLabel.setStyle("-fx-font-weight: bold;");

		Label pramanaLabel = new Label("ಪ್ರಮಾಣ");
		pramanaLabel.setPrefWidth(65);
		pramanaLabel.setStyle("-fx-font-weight: bold;");

		Label mottaLabel = new Label("ಮೊತ್ತ");
		mottaLabel.setPrefWidth(60);
		mottaLabel.setStyle("-fx-font-weight: bold;");

		headerRow.getChildren().addAll(sevaLabel, pramanaLabel, mottaLabel);

		receiptBox.getChildren().add(headerRow);


		// Seva Items
		for (SevaEntry seva : data.getSevas()) {
			HBox sevaRow = new HBox(20);
			sevaRow.getChildren().addAll(
					createLabel(seva.getName(), 150),
					createLabel(String.valueOf(seva.getQuantity()), 50),
					createLabel("₹" + String.format("%.2f", seva.getTotalAmount()), 70)
			);
			receiptBox.getChildren().add(sevaRow);
		}

		receiptBox.getChildren().add(new Text("")); // Spacer

		// Total Amount Row
		Text totalText = new Text("ಒಟ್ಟು ಮೊತ್ತ: ₹" + String.format("%.2f", data.getTotalAmount()));
		totalText.setFont(Font.font("Noto Sans Kannada", 14));
		totalText.setStyle("-fx-font-weight: bold;");
		receiptBox.getChildren().add(totalText);

		receiptBox.getChildren().add(new Text("")); // Spacer

		// Blessing Line - Center Aligned
		Text blessing = new Text("ಶ್ರೀ ದೇವರ ಕೃಪೆ ಸದಾ ನಿಮ್ಮ ಮೇಲಿರಲಿ!");
		blessing.setFont(Font.font("Noto Sans Kannada", 12));
		blessing.setStyle("-fx-font-style: italic;");
		VBox blessingBox = new VBox(blessing);
		blessingBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(blessingBox);

		return receiptBox;
	}

	private Label createLabel(String text, double width) {
		Label label = new Label(text);
		label.setFont(Font.font("Noto Sans Kannada", 12));
		label.setPrefWidth(width);
		label.setWrapText(true);
		return label;
	}


	// --- Method for Print Preview ---
	public void showPrintPreview(SevaReceiptData data, Stage ownerStage, Consumer<Boolean> onPrintComplete) {
		Stage previewStage = new Stage();
		previewStage.initModality(Modality.WINDOW_MODAL);
		previewStage.initOwner(ownerStage);
		previewStage.setTitle("ಮುದ್ರಣ ಪೂರ್ವದರ್ಶನ"); // Print Preview Title

		Node receiptNode = createReceiptNode(data);


		double scaleFactor = 2; // Adjust this value to control preview size
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

		Button printButton = new Button("ಮುದ್ರಿಸು");
		printButton.setOnAction(e -> {
			// Reset scale before printing
			receiptNode.setScaleX(1.0);
			receiptNode.setScaleY(1.0);
			// Call the modified printReceipt and get the result
			boolean success = printReceipt(receiptNode, ownerStage);
			// Execute the callback with the result
			if (onPrintComplete != null) {
				onPrintComplete.accept(success);
			}
			previewStage.close(); // Close preview regardless of success
		});


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


	// Method for donation receipt preview
	public void showDonationPrintPreview(DonationReceiptData data, Stage ownerStage, Consumer<Boolean> onPrintComplete) {
		Stage previewStage = new Stage();
		previewStage.initModality(Modality.WINDOW_MODAL);
		previewStage.initOwner(ownerStage);
		previewStage.setTitle("ದೇಣಿಗೆ ರಶೀದಿ ಮುದ್ರಣ ಪೂರ್ವದರ್ಶನ"); // Donation Receipt Print Preview

		Node receiptNode = createDonationReceiptNode(data);

		double scaleFactor = 2;
		receiptNode.setScaleX(scaleFactor);
		receiptNode.setScaleY(scaleFactor);

		Group scaledContainer = new Group(receiptNode);
		scaledContainer.setAutoSizeChildren(true);

		ScrollPane scrollPane = new ScrollPane(scaledContainer);
		scrollPane.setFitToWidth(false);
		scrollPane.setFitToHeight(false);
		scrollPane.setPrefViewportWidth(RECEIPT_WIDTH_POINTS * scaleFactor + 20);
		scrollPane.setPrefViewportHeight(600);

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

		HBox buttonBox = new HBox(10, printButton);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setPadding(new Insets(10));

		VBox layout = new VBox(10, scrollPane, buttonBox);
		layout.setAlignment(Pos.CENTER);
		scrollPane.setPrefViewportHeight(800);

		Scene scene = new Scene(layout, 700, 1000);
		previewStage.setScene(scene);
		previewStage.show();
	}

	// Method to create donation receipt layout
	public Node createDonationReceiptNode(DonationReceiptData data) {
		VBox receiptBox = new VBox(1);
		receiptBox.setStyle("-fx-padding: 10;");
		receiptBox.setMaxWidth(10);

		// Temple Name (Main Heading)
		Text templeName = new Text("ಶ್ರೀ ಶಾಸ್ತಾರ ಸುಬ್ರಹ್ಮಣ್ಯೇಶ್ವರ ದೇವಸ್ಥಾನ");
		templeName.setFont(Font.font("Noto Sans Kannada", 16));
		templeName.setStyle("-fx-font-weight: bold; -fx-underline: true;");
		VBox heading = new VBox(templeName);
		heading.setStyle("-fx-alignment: center; -fx-underline: true;");
		receiptBox.getChildren().add(heading);

		// Subheadings
		VBox subHeadings = new VBox(2);
		subHeadings.setStyle("-fx-alignment: center;");
		subHeadings.getChildren().addAll(
				new Text("ಚೇರ್ಕಬೆ"),
				new Text("ಅಂಚೆ : 671552"),
				new Text("ದೂರವಾಣಿ: 6282525216, 9526431593")
		);
		receiptBox.getChildren().add(subHeadings);

		receiptBox.getChildren().add(new Text(""));

		// Receipt Title - Donation Specific
		Text receiptTitle = new Text("ದೇಣಿಗೆ ರಶೀದಿ");
		receiptTitle.setFont(Font.font("Noto Sans Kannada", 14));
		receiptTitle.setStyle("-fx-underline: true;");
		VBox titleBox = new VBox(receiptTitle);
		titleBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(titleBox);

		receiptBox.getChildren().add(new Text(""));

		// Receipt ID
		receiptBox.getChildren().add(new Text("ರಶೀದಿ ಸಂಖ್ಯೆ: " + data.getDonationReceiptId()));

		// Devotee Details
		receiptBox.getChildren().addAll(
				new Text("ಭಕ್ತರ ಹೆಸರು: " + (data.getDevoteeName().isEmpty() ? "---" : data.getDevoteeName())),
				new Text("ದೂರವಾಣಿ: " + (data.getPhoneNumber().isEmpty() ? "---" : data.getPhoneNumber())),
				new Text("ಜನ್ಮ ನಕ್ಷತ್ರ: " + (data.getNakshatra() != null ? data.getNakshatra() : "---")),
				new Text("ಜನ್ಮ ರಾಶಿ: " + (data.getRashi() != null && !Objects.equals(data.getRashi(), "ಆಯ್ಕೆ") ? data.getRashi() : "---")),
				new Text("ದಿನಾಂಕ: " + data.getFormattedDate())
		);

		receiptBox.getChildren().add(new Text(""));

		// Donation Details
		receiptBox.getChildren().addAll(
				new Text("ದೇಣಿಗೆ ವಿಧ: " + data.getDonationName()),
				new Text("ಪಾವತಿ ವಿಧಾನ: " + data.getPaymentMode()),
				new Text("ದೇಣಿಗೆ ಮೊತ್ತ: ₹" + String.format("%.2f", data.getDonationAmount()))
		);

		receiptBox.getChildren().add(new Text(""));

		// Blessing Line
		Text blessing = new Text("ಶ್ರೀ ದೇವರ ಕೃಪೆ ಸದಾ ನಿಮ್ಮ ಮೇಲಿರಲಿ!");
		blessing.setFont(Font.font("Noto Sans Kannada", 12));
		blessing.setStyle("-fx-font-style: italic;");
		VBox blessingBox = new VBox(blessing);
		blessingBox.setStyle("-fx-alignment: center;");
		receiptBox.getChildren().add(blessingBox);

		return receiptBox;
	}


	// --- Method to Handle Actual Printing ---
// Inside ReceiptPrinter.java
	public boolean printReceipt(Node nodeToPrint, Stage ownerStage) { // Changed return type
		PrinterJob job = PrinterJob.createPrinterJob();

		if (job == null) {
			showAlert(ownerStage, "Printing Error", "Could not create printer job.");
			return false; // Indicate failure
		}

		boolean proceed = job.showPrintDialog(ownerStage);
		boolean printSucceeded = false; // Flag for success

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
				printSucceeded = job.endJob(); // endJob returns true on success
				// Reset scale if it was changed
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
			// User cancelled the print dialog
			job.cancelJob(); // Good practice to cancel if dialog is closed
		}
		return printSucceeded; // Return the final status
	}
	// --- Method to Save as PDF ---
//	public void saveReceiptAsPdf(SevaReceiptData data, Stage ownerStage) throws IOException {
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

		// **KEY FIX: Handle window close event**
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

		// **KEY FIX: Handle window close event**
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