package com.pranav.temple_software.utils;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.image.*;
import com.github.anastaciocintra.output.PrinterOutputStream;
import com.pranav.temple_software.models.*;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class EscPosPrinterService {
	private final String printerName;
	private static final int PRINTER_PIXEL_WIDTH = 576;
	private static final int PADDING = 15; // Left and Right padding in pixels

	private static final Font FONT_TITLE = new Font("Noto Sans Kannada", Font.BOLD, 30);
	private static final Font FONT_SUBTITLE = new Font("Noto Sans Kannada", Font.PLAIN, 20);
	private static final Font FONT_HEADER = new Font("Noto Sans Kannada", Font.BOLD, 24);
	private static final Font FONT_REGULAR = new Font("Noto Sans Kannada", Font.PLAIN, 20);
	private static final Font FONT_REGULAR_BOLD = new Font("Noto Sans Kannada", Font.BOLD, 20); // Font for detail labels
	private static final Font FONT_BOLD = new Font("Noto Sans Kannada", Font.BOLD, 22);
	private static final Font FONT_ITALIC = new Font("Noto Sans Kannada", Font.ITALIC, 20);
	private static final Font FONT_TOTAL = new Font("Noto Sans Kannada", Font.BOLD, 32);

	public EscPosPrinterService(String printerName) {
		this.printerName = printerName;
	}

	// --- Public Methods for SAVING PNG PREVIEWS ---
	public void saveSevaReceiptAsPng(SevaReceiptData data, String filename) throws IOException {
		saveImage(generateSevaReceiptImage(data), filename);
	}
	public void saveDonationReceiptAsPng(DonationReceiptData data, String filename) throws IOException {
		saveImage(generateDonationReceiptImage(data), filename);
	}
	public void saveShashwathaPoojaReceiptAsPng(ShashwathaPoojaReceipt data, String filename) throws IOException {
		saveImage(generateShashwathaPoojaReceiptImage(data), filename);
	}
	public void saveInKindDonationReceiptAsPng(InKindDonation data, String filename) throws IOException {
		saveImage(generateInKindDonationReceiptImage(data), filename);
	}
	public void saveKaryakramaReceiptAsPng(KaryakramaReceiptData data, String filename) throws IOException {
		saveImage(generateKaryakramaReceiptImage(data), filename);
	}

	// --- Public Methods for ACTUAL PRINTING ---
	public void printSevaReceipt(SevaReceiptData data) throws IOException {
		print(generateSevaReceiptImage(data));
	}
	public void printDonationReceipt(DonationReceiptData data) throws IOException {
		print(generateDonationReceiptImage(data));
	}
	public void printShashwathaPoojaReceipt(ShashwathaPoojaReceipt data) throws IOException {
		print(generateShashwathaPoojaReceiptImage(data));
	}
	public void printInKindDonationReceipt(InKindDonation data) throws IOException {
		print(generateInKindDonationReceiptImage(data));
	}
	public void printKaryakramaReceipt(KaryakramaReceiptData data) throws IOException {
		print(generateKaryakramaReceiptImage(data));
	}

	// --- Image Generation Logic for Each Receipt Type ---

	public BufferedImage generateSevaReceiptImage(SevaReceiptData data) {
		List<BufferedImage> lines = buildHeader("ಸೇವಾ ರಶೀದಿ");
		lines.add(renderDetailLine("ರಶೀದಿ ಸಂಖ್ಯೆ:", String.valueOf(data.getReceiptId())));
		lines.add(renderDetailLine("ಭಕ್ತರ ಹೆಸರು:", data.getDevoteeName()));
		if (isValid(data.getPhoneNumber())) lines.add(renderDetailLine("ದೂರವಾಣಿ:", data.getPhoneNumber()));
		if (isValid(data.getNakshatra())) lines.add(renderDetailLine("ಜನ್ಮ ನಕ್ಷತ್ರ:", data.getNakshatra()));
		if (isValid(data.getRashi()) && !data.getRashi().equals("ಆಯ್ಕೆ")) lines.add(renderDetailLine("ಜನ್ಮ ರಾಶಿ:", data.getRashi()));
		lines.add(renderDetailLine("ದಿನಾಂಕ:", data.getFormattedDate()));
		lines.add(renderDetailLine("ಪಾವತಿ ವಿಧಾನ:", data.getPaymentMode()));
		lines.add(renderBlankLine(15)); // Added gap before the table

		lines.add(renderSeparatorLine());
		lines.add(renderTableRow("ಸೇವೆಯ ಹೆಸರು", "ಪ್ರಮಾಣ", "ಮೊತ್ತ", FONT_BOLD));
		lines.add(renderSeparatorLine());
		for (SevaEntry seva : data.getSevas()) {
			lines.add(renderTableRow(seva.getName(), String.valueOf(seva.getQuantity()), String.format("₹%.2f", seva.getTotalAmount()), FONT_REGULAR));
		}
		lines.add(renderSeparatorLine());
		lines.add(renderBlankLine(10));

		lines.add(renderTextToImage(String.format("ಒಟ್ಟು ಮೊತ್ತ: ₹%.2f", data.getTotalAmount()), FONT_TOTAL, EscPos.Justification.Center));
		lines.addAll(buildFooter());
		return stitchImages(lines);
	}

	public BufferedImage generateDonationReceiptImage(DonationReceiptData data) {
		List<BufferedImage> lines = buildHeader("ದೇಣಿಗೆ ರಶೀದಿ");
		lines.add(renderDetailLine("ರಶೀದಿ ಸಂಖ್ಯೆ:", String.valueOf(data.getDonationReceiptId())));
		lines.add(renderDetailLine("ಭಕ್ತರ ಹೆಸರು:", data.getDevoteeName()));
		if (isValid(data.getPhoneNumber())) lines.add(renderDetailLine("ದೂರವಾಣಿ:", data.getPhoneNumber()));
		lines.add(renderDetailLine("ದಿನಾಂಕ:", data.getFormattedDate()));
		lines.add(renderBlankLine(15));
		lines.add(renderDetailLine("ದೇಣಿಗೆ ವಿಧ:", data.getDonationName()));
		lines.add(renderDetailLine("ಪಾವತಿ ವಿಧಾನ:", data.getPaymentMode()));
		lines.add(renderBlankLine(10));
		lines.add(renderTextToImage(String.format("ದೇಣಿಗೆ ಮೊತ್ತ: ₹%.2f", data.getDonationAmount()), FONT_TOTAL, EscPos.Justification.Center));
		lines.addAll(buildFooter());
		return stitchImages(lines);
	}

	public BufferedImage generateShashwathaPoojaReceiptImage(ShashwathaPoojaReceipt data) {
		List<BufferedImage> lines = buildHeader("ಶಾಶ್ವತ ಪೂಜೆ ರಶೀದಿ");
		lines.add(renderDetailLine("ರಶೀದಿ ಸಂಖ್ಯೆ:", String.valueOf(data.getReceiptId())));
		lines.add(renderDetailLine("ಭಕ್ತರ ಹೆಸರು:", data.getDevoteeName()));
		if (isValid(data.getPhoneNumber())) lines.add(renderDetailLine("ದೂರವಾಣಿ:", data.getPhoneNumber()));
		lines.add(renderDetailLine("ರಶೀದಿ ದಿನಾಂಕ:", data.getFormattedReceiptDate()));
		lines.add(renderDetailLine("ಪೂಜಾ ದಿನಾಂಕ/ವಿವರ:", data.getPoojaDate()));
		lines.add(renderDetailLine("ಪಾವತಿ ವಿಧಾನ:", data.getPaymentMode()));
		lines.add(renderBlankLine(10));
		lines.add(renderTextToImage(String.format("ಪೂಜಾ ಮೊತ್ತ: ₹%.2f", data.getAmount()), FONT_TOTAL, EscPos.Justification.Center));
		lines.addAll(buildFooter());
		return stitchImages(lines);
	}

	public BufferedImage generateInKindDonationReceiptImage(InKindDonation data) {
		List<BufferedImage> lines = buildHeader("ವಸ್ತು ದೇಣಿಗೆ ರಶೀದಿ");
		lines.add(renderDetailLine("ರಶೀದಿ ಸಂಖ್ಯೆ:", String.valueOf(data.getInKindReceiptId())));
		lines.add(renderDetailLine("ಭಕ್ತರ ಹೆಸರು:", data.getDevoteeName()));
		if (isValid(data.getPhoneNumber())) lines.add(renderDetailLine("ದೂರವಾಣಿ:", data.getPhoneNumber()));
		lines.add(renderDetailLine("ದಿನಾಂಕ:", data.getFormattedDate()));
		lines.add(renderBlankLine(10));
		lines.add(renderSeparatorLine());
		lines.add(renderTextToImage("ವಸ್ತು ವಿವರಣೆ:", FONT_BOLD, EscPos.Justification.Left_Default));
		lines.add(renderTextToImage(data.getItemDescription(), FONT_REGULAR, EscPos.Justification.Left_Default));
		lines.add(renderSeparatorLine());
		lines.addAll(buildFooter());
		return stitchImages(lines);
	}

	public BufferedImage generateKaryakramaReceiptImage(KaryakramaReceiptData data) {
		List<BufferedImage> lines = buildHeader("ಕಾರ್ಯಕ್ರಮದ ರಶೀದಿ");
		lines.add(renderDetailLine("ರಶೀದಿ ಸಂಖ್ಯೆ:", String.valueOf(data.getReceiptId())));
		lines.add(renderDetailLine("ಭಕ್ತರ ಹೆಸರು:", data.getDevoteeName()));
		if (isValid(data.getPhoneNumber())) lines.add(renderDetailLine("ದೂರವಾಣಿ:", data.getPhoneNumber()));
		lines.add(renderDetailLine("ಕಾರ್ಯಕ್ರಮ:", data.getKaryakramaName()));
		lines.add(renderDetailLine("ದಿನಾಂಕ:", data.getFormattedReceiptDate()));
		lines.add(renderDetailLine("ಪಾವತಿ ವಿಧಾನ:", data.getPaymentMode()));
		lines.add(renderBlankLine(15));
		lines.add(renderSeparatorLine());
		lines.add(renderTableRow("ವಿವರ", "ಪ್ರಮಾಣ", "ಮೊತ್ತ", FONT_BOLD));
		lines.add(renderSeparatorLine());
		for (SevaEntry seva : data.getSevas()) {
			lines.add(renderTableRow(seva.getName(), String.valueOf(seva.getQuantity()), String.format("₹%.2f", seva.getTotalAmount()), FONT_REGULAR));
		}
		lines.add(renderSeparatorLine());
		lines.add(renderBlankLine(10));
		lines.add(renderTextToImage(String.format("ಒಟ್ಟು ಮೊತ್ತ: ₹%.2f", data.getTotalAmount()), FONT_TOTAL, EscPos.Justification.Center));
		lines.addAll(buildFooter());
		return stitchImages(lines);
	}

	// --- Core Utility Methods ---
	private List<BufferedImage> buildHeader(String title) {
		List<BufferedImage> headerLines = new ArrayList<>();
		headerLines.add(renderTextToImage("********************************", FONT_SUBTITLE, EscPos.Justification.Center));
		headerLines.add(renderTextToImage(ConfigManager.getInstance().getProperty("temple.name"), FONT_TITLE, EscPos.Justification.Center));
		headerLines.add(renderTextToImage(ConfigManager.getInstance().getProperty("temple.location"), FONT_SUBTITLE, EscPos.Justification.Center));
		headerLines.add(renderTextToImage(ConfigManager.getInstance().getProperty("temple.postal"), FONT_SUBTITLE, EscPos.Justification.Center));
		headerLines.add(renderTextToImage(ConfigManager.getInstance().getProperty("temple.phone"), FONT_SUBTITLE, EscPos.Justification.Center));
		headerLines.add(renderTextToImage("********************************", FONT_SUBTITLE, EscPos.Justification.Center));
		headerLines.add(renderBlankLine(10));
		headerLines.add(renderTextToImage(title, FONT_HEADER, EscPos.Justification.Center));
		headerLines.add(renderBlankLine(15));
		return headerLines;
	}

	private List<BufferedImage> buildFooter() {
		List<BufferedImage> footerLines = new ArrayList<>();
		footerLines.add(renderBlankLine(15));
		footerLines.add(renderTextToImage("ಶ್ರೀ ದೇವರ ಕೃಪೆ ಸದಾ ನಿಮ್ಮ ಮೇಲಿರಲಿ!", FONT_ITALIC, EscPos.Justification.Center));
		return footerLines;
	}

	private BufferedImage stitchImages(List<BufferedImage> images) {
		images.add(0, renderBlankLine(PADDING));
		int totalHeight = images.stream().mapToInt(BufferedImage::getHeight).sum();
		BufferedImage finalReceipt = new BufferedImage(PRINTER_PIXEL_WIDTH, totalHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = finalReceipt.createGraphics();
		g2d.setColor(java.awt.Color.WHITE);
		g2d.fillRect(0, 0, PRINTER_PIXEL_WIDTH, totalHeight);
		int currentY = 0;
		for (BufferedImage line : images) {
			g2d.drawImage(line, 0, currentY, null);
			currentY += line.getHeight();
		}
		g2d.dispose();
		return finalReceipt;
	}

	private void print(BufferedImage receiptImage) throws IOException {
		try (OutputStream outputStream = getOutputStream(); EscPos escpos = new EscPos(outputStream)) {
			CoffeeImage coffeeImage = new CoffeeImageImpl(receiptImage);
			Bitonal algorithm = new BitonalThreshold();
			EscPosImage escposImage = new EscPosImage(coffeeImage, algorithm);
			BitImageWrapper imageWrapper = new BitImageWrapper();
			escpos.write(imageWrapper, escposImage);
			escpos.feed(5);
			escpos.cut(EscPos.CutMode.FULL);
		}
	}

	private void saveImage(BufferedImage image, String filename) throws IOException {
		String userDesktop = System.getProperty("user.home") + "/Desktop/";
		File outputFile = new File(userDesktop + filename);
		ImageIO.write(image, "png", outputFile);
		System.out.println("PNG preview saved to: " + outputFile.getAbsolutePath());
	}

	private OutputStream getOutputStream() throws IOException {
		PrintService printService = PrinterOutputStream.getPrintServiceByName(this.printerName);
		if (printService == null) { throw new IOException("Printer not found: '" + this.printerName + "'."); }
		return new PrinterOutputStream(printService);
	}

	private BufferedImage renderTextToImage(String text, Font font, EscPos.Justification justification) {
		BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D tempG2d = tempImg.createGraphics();
		tempG2d.setFont(font);
		int textWidth = tempG2d.getFontMetrics().stringWidth(text);
		int textHeight = tempG2d.getFontMetrics().getHeight();
		tempG2d.dispose();
		BufferedImage image = new BufferedImage(PRINTER_PIXEL_WIDTH, textHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(java.awt.Color.WHITE);
		g2d.fillRect(0,0, PRINTER_PIXEL_WIDTH, textHeight);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setFont(font);
		g2d.setColor(java.awt.Color.BLACK);
		int x;
		if (justification == EscPos.Justification.Right) { x = PRINTER_PIXEL_WIDTH - textWidth - PADDING; }
		else if (justification == EscPos.Justification.Center) { x = (PRINTER_PIXEL_WIDTH - textWidth) / 2; }
		else { x = PADDING; }
		g2d.drawString(text, x, g2d.getFontMetrics().getAscent());
		g2d.dispose();
		return image;
	}

	private BufferedImage renderTableRow(String col1, String col2, String col3, Font font) {
		int height = (int)(font.getSize() * 1.5);
		BufferedImage rowImage = new BufferedImage(PRINTER_PIXEL_WIDTH, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = rowImage.createGraphics();
		g2d.setColor(java.awt.Color.WHITE);
		g2d.fillRect(0, 0, PRINTER_PIXEL_WIDTH, height);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setFont(font);
		g2d.setColor(java.awt.Color.BLACK);
		int y = g2d.getFontMetrics().getAscent();

		g2d.drawString(col1, PADDING, y);
		int col2X = 350;
		int textWidthCol2 = g2d.getFontMetrics().stringWidth(col2);
		g2d.drawString(col2, col2X + (100 - textWidthCol2) / 2, y);
		int textWidthCol3 = g2d.getFontMetrics().stringWidth(col3);
		g2d.drawString(col3, PRINTER_PIXEL_WIDTH - textWidthCol3 - PADDING, y);
		g2d.dispose();
		return rowImage;
	}

	private BufferedImage renderSeparatorLine() {
		BufferedImage lineImage = new BufferedImage(PRINTER_PIXEL_WIDTH, 5, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = lineImage.createGraphics();
		g2d.setColor(java.awt.Color.WHITE);
		g2d.fillRect(0, 0, PRINTER_PIXEL_WIDTH, 5);
		g2d.setColor(java.awt.Color.BLACK);
		float[] dash = {2.0f};
		g2d.setStroke(new java.awt.BasicStroke(1, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 5.0f, dash, 0.0f));
		g2d.drawLine(PADDING, 2, PRINTER_PIXEL_WIDTH - PADDING, 2);
		g2d.dispose();
		return lineImage;
	}

	private BufferedImage renderBlankLine(int height) {
		BufferedImage blankImage = new BufferedImage(PRINTER_PIXEL_WIDTH, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = blankImage.createGraphics();
		g2d.setColor(java.awt.Color.WHITE);
		g2d.fillRect(0, 0, PRINTER_PIXEL_WIDTH, height);
		g2d.dispose();
		return blankImage;
	}

	private BufferedImage renderDetailLine(String label, String value) {
		int height = (int)(FONT_REGULAR.getSize() * 1.5);
		BufferedImage lineImage = new BufferedImage(PRINTER_PIXEL_WIDTH, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = lineImage.createGraphics();
		g2d.setColor(java.awt.Color.WHITE);
		g2d.fillRect(0, 0, PRINTER_PIXEL_WIDTH, height);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setColor(java.awt.Color.BLACK);

		g2d.setFont(FONT_REGULAR_BOLD);
		int y = g2d.getFontMetrics().getAscent() + 2;
		g2d.drawString(label, PADDING, y);

		g2d.setFont(FONT_REGULAR);
		// Measure the bold label width with the BOLD font to know where to start the regular text
		int labelWidth = g2d.getFontMetrics(FONT_REGULAR_BOLD).stringWidth(label + " ");
		g2d.drawString(value, PADDING + labelWidth, y);

		g2d.dispose();
		return lineImage;
	}

	private boolean isValid(String str) { return str != null && !str.trim().isEmpty(); }
}