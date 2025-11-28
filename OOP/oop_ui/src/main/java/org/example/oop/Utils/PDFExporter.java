package org.example.oop.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.miniboot.app.domain.models.Payment.Payment;
import org.miniboot.app.domain.models.Payment.PaymentItem;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

/**
 * Utility class for exporting data to PDF files
 */
public class PDFExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Export data to PDF file
     *
     * @param filePath     Full path to save the file
     * @param title        Document title
     * @param headers      Column headers
     * @param data         Data rows
     * @param columnWidths Relative column widths (optional, can be null)
     * @throws IOException If file cannot be written
     */
    public static void exportToFile(String filePath, String title, List<String> headers,
            List<List<Object>> data, float[] columnWidths) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            PdfWriter writer = new PdfWriter(fos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title
            Paragraph titleParagraph = new Paragraph(title)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(titleParagraph);

            // Add timestamp
            String timestamp = "Ngày xuất: " + LocalDateTime.now().format(DATETIME_FORMATTER);
            Paragraph timestampParagraph = new Paragraph(timestamp)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(10);
            document.add(timestampParagraph);

            // Create table
            Table table;
            if (columnWidths != null && columnWidths.length == headers.size()) {
                table = new Table(columnWidths);
            } else {
                table = new Table(UnitValue.createPercentArray(headers.size()));
            }
            table.setWidth(UnitValue.createPercentValue(100));

            // Add headers
            DeviceRgb headerColor = new DeviceRgb(33, 150, 243); // Blue color
            for (String header : headers) {
                Cell cell = new Cell()
                        .add(new Paragraph(header).setBold())
                        .setBackgroundColor(headerColor)
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(cell);
            }

            // Add data rows
            for (List<Object> rowData : data) {
                for (Object value : rowData) {
                    String cellValue = formatValue(value);
                    Cell cell = new Cell().add(new Paragraph(cellValue));
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
        }
    }

    /**
     * Export data to PDF file with default column widths
     */
    public static void exportToFile(String filePath, String title, List<String> headers, List<List<Object>> data)
            throws IOException {
        exportToFile(filePath, title, headers, data, null);
    }

    /**
     * Export data to PDF file with simple String array inputs
     * Returns true if successful, false otherwise
     *
     * @param title    Document title (can include multi-line header info)
     * @param headers  Column headers as String array
     * @param data     Data rows as List of String arrays
     * @param fileName Base filename without extension
     * @return true if export successful, false otherwise
     */
    public static boolean exportToFile(String title, String[] headers, List<String[]> data, String fileName) {
        try {
            // Ensure directory exists
            String documentsPath = getDocumentsPath();
            ensureDirectoryExists(documentsPath);

            // Create full file path
            String fullPath = documentsPath + fileName + ".pdf";

            // Convert String[] headers to List<String>
            List<String> headerList = java.util.Arrays.asList(headers);

            // Convert List<String[]> data to List<List<Object>>
            List<List<Object>> dataList = new java.util.ArrayList<>();
            for (String[] row : data) {
                List<Object> rowList = new java.util.ArrayList<>();
                for (String cell : row) {
                    rowList.add(cell);
                }
                dataList.add(rowList);
            }

            // Call the main export method
            exportToFile(fullPath, title, headerList, dataList, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Format value for display in PDF
     */
    private static String formatValue(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DATE_FORMATTER);
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DATETIME_FORMATTER);
        } else if (value instanceof Boolean) {
            return (Boolean) value ? "Có" : "Không";
        } else if (value instanceof Number) {
            return String.format("%,d", ((Number) value).longValue());
        } else {
            return value.toString();
        }
    }

    /**
     * Generate a default filename with timestamp
     */
    public static String generateFileName(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return prefix + "_" + timestamp + ".pdf";
    }

    /**
     * Get user's Documents folder path
     */
    public static String getDocumentsPath() {
        String userHome = System.getProperty("user.home");
        return userHome + "/Documents/EyeClinic/";
    }

    /**
     * Ensure the export directory exists
     */
    public static void ensureDirectoryExists(String directoryPath) {
        java.io.File directory = new java.io.File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Export receipt/invoice to PDF with beautiful formatting and Vietnamese font
     * support
     * 
     * @param payment  Payment object containing all payment information
     * @param items    List of payment items
     * @param fileName Base filename without extension
     * @return true if successful, false otherwise
     */
    public static boolean exportReceipt(Payment payment, List<PaymentItem> items, String fileName) {
        try {
            // Ensure directory exists
            String documentsPath = getDocumentsPath();
            ensureDirectoryExists(documentsPath);

            // Create full file path
            String fullPath = documentsPath + fileName + ".pdf";

            // Create PDF writer and document
            PdfWriter writer = new PdfWriter(fullPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Load Vietnamese-supported font
            PdfFont font = null;
            PdfFont boldFont = null;

            // Get Windows font directory
            String windir = System.getenv("WINDIR");
            if (windir == null) {
                windir = "C:/Windows"; // Fallback
            }

            // Try to load system fonts (supports Vietnamese)
            String[] fontPaths = {
                    // Linux paths
                    "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                    "/usr/share/fonts/dejavu/DejaVuSans.ttf",
                    System.getProperty("user.home") + "/.fonts/DejaVuSans.ttf",
                    // Windows paths - Arial (best for Vietnamese)
                    "C:/Windows/Fonts/arial.ttf",
                    windir + "/Fonts/arial.ttf",
                    // Windows paths - Times New Roman
                    "C:/Windows/Fonts/times.ttf",
                    windir + "/Fonts/times.ttf"
            };

            String[] boldFontPaths = {
                    // Linux paths
                    "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
                    "/usr/share/fonts/dejavu/DejaVuSans-Bold.ttf",
                    System.getProperty("user.home") + "/.fonts/DejaVuSans-Bold.ttf",
                    // Windows paths - Arial Bold (best for Vietnamese)
                    "C:/Windows/Fonts/arialbd.ttf",
                    windir + "/Fonts/arialbd.ttf",
                    // Windows paths - Times New Roman Bold
                    "C:/Windows/Fonts/timesbd.ttf",
                    windir + "/Fonts/timesbd.ttf"
            };

            // Try to find and load fonts
            for (String fontPath : fontPaths) {
                if (fontPath == null)
                    continue; // Skip null paths
                File fontFile = new File(fontPath);
                if (fontFile.exists()) {
                    try {
                        font = PdfFontFactory.createFont(fontPath, "Identity-H",
                                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                        System.out.println("✅ PDF: Loaded font from: " + fontPath);
                        break;
                    } catch (Exception e) {
                        System.out.println("⚠️ PDF: Failed to load font: " + fontPath + " - " + e.getMessage());
                    }
                }
            }

            for (String fontPath : boldFontPaths) {
                if (fontPath == null)
                    continue; // Skip null paths
                File fontFile = new File(fontPath);
                if (fontFile.exists()) {
                    try {
                        boldFont = PdfFontFactory.createFont(fontPath, "Identity-H",
                                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                        System.out.println("✅ PDF: Loaded bold font from: " + fontPath);
                        break;
                    } catch (Exception e) {
                        System.out.println("⚠️ PDF: Failed to load bold font: " + fontPath + " - " + e.getMessage());
                    }
                }
            }

            // Fallback to standard fonts if custom fonts not found
            if (font == null) {
                System.out.println(
                        "⚠️ PDF: No Vietnamese font found, using HELVETICA (may not display Vietnamese correctly)");
                font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            }
            if (boldFont == null) {
                System.out.println("⚠️ PDF: No Vietnamese bold font found, using HELVETICA_BOLD");
                boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            }

            document.setFont(font);

            // Set page margins
            document.setMargins(40, 40, 40, 40);

            // HEADER - CLINIC INFO
            Paragraph clinicName = new Paragraph("PHÒNG KHÁM MẮT")
                    .setFont(boldFont)
                    .setFontSize(24)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(clinicName);

            Paragraph address = new Paragraph("Địa chỉ: Phòng 304-A2, Km10, đường Nguyễn Trãi, Hà Đông, Hà Nội")
                    .setFont(font)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(3);
            document.add(address);

            Paragraph contact = new Paragraph("Hotline: 0966668888 | Email: Nguhotuongd23@gmail.com")
                    .setFont(font)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(contact);

            // Title
            Paragraph title = new Paragraph("HÓA ĐƠN THANH TOÁN")
                    .setFont(boldFont)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(39, 174, 96))
                    .setMarginTop(10)
                    .setMarginBottom(15);
            document.add(title);

            // Separator line
            SolidLine line1 = new SolidLine(1f);
            line1.setColor(new DeviceRgb(189, 195, 199));
            LineSeparator separator1 = new LineSeparator(line1);
            document.add(separator1);

            // RECEIPT INFO - 2x2 Grid
            float[] columnWidths = { 120, 180, 120, 180 };
            Table infoTable = new Table(UnitValue.createPointArray(columnWidths));
            infoTable.setWidth(UnitValue.createPercentValue(100));
            infoTable.setMarginTop(15);
            infoTable.setMarginBottom(15);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // Row 1
            infoTable.addCell(createInfoLabelCell("Số phiếu:", boldFont));
            infoTable.addCell(createInfoValueCell(payment.getCode(), font));
            infoTable.addCell(createInfoLabelCell("Ngày:", boldFont));
            infoTable.addCell(createInfoValueCell(payment.getIssuedAt().format(dateFormatter), font));

            // Row 2
            infoTable.addCell(createInfoLabelCell("Thu ngân:", boldFont));
            infoTable.addCell(createInfoValueCell("NV" + payment.getCashierId(), font));
            infoTable.addCell(createInfoLabelCell("Khách hàng:", boldFont));
            String customerName = payment.getCustomerId() == null ? "Khách lẻ" : "KH" + payment.getCustomerId();
            infoTable.addCell(createInfoValueCell(customerName, font));

            document.add(infoTable);

            // Separator line
            SolidLine line2 = new SolidLine(1f);
            line2.setColor(new DeviceRgb(189, 195, 199));
            LineSeparator separator2 = new LineSeparator(line2);
            document.add(separator2);

            // ITEMS TABLE
            Paragraph itemsTitle = new Paragraph("CHI TIẾT HÓA ĐƠN")
                    .setFont(boldFont)
                    .setFontSize(15)
                    .setFontColor(new DeviceRgb(52, 73, 94))
                    .setMarginTop(15)
                    .setMarginBottom(10);
            document.add(itemsTitle);

            // Create items table
            float[] itemColumnWidths = { 40, 250, 60, 100, 100 };
            Table itemsTable = new Table(UnitValue.createPointArray(itemColumnWidths));
            itemsTable.setWidth(UnitValue.createPercentValue(100));

            // Table headers
            DeviceRgb headerColor = new DeviceRgb(52, 152, 219);
            String[] headers = { "STT", "Mặt hàng/Dịch vụ", "SL", "Đơn giá", "Thành tiền" };

            for (String header : headers) {
                Cell cell = new Cell()
                        .add(new Paragraph(header).setFont(boldFont).setFontSize(11))
                        .setBackgroundColor(headerColor)
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setPadding(8)
                        .setBorder(new SolidBorder(ColorConstants.WHITE, 1));
                itemsTable.addHeaderCell(cell);
            }

            // Table data
            int index = 1;
            for (PaymentItem item : items) {
                // STT
                itemsTable.addCell(createTableCell(String.valueOf(index++), font, TextAlignment.CENTER));
                // Item name
                itemsTable.addCell(createTableCell(item.getDescription(), font, TextAlignment.LEFT));
                // Quantity
                itemsTable.addCell(createTableCell(String.valueOf(item.getQty()), font, TextAlignment.CENTER));
                // Unit price
                itemsTable
                        .addCell(createTableCell(String.format("%,d", item.getUnitPrice()), font, TextAlignment.RIGHT));
                // Total
                itemsTable
                        .addCell(createTableCell(String.format("%,d", item.getTotalLine()), font, TextAlignment.RIGHT));
            }

            document.add(itemsTable);

            // Separator line
            SolidLine line3 = new SolidLine(1f);
            line3.setColor(new DeviceRgb(189, 195, 199));
            LineSeparator separator3 = new LineSeparator(line3);
            separator3.setMarginTop(15);
            separator3.setMarginBottom(15);
            document.add(separator3);

            // SUMMARY
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[] { 70, 30 }));
            summaryTable.setWidth(UnitValue.createPercentValue(100));

            // Total
            summaryTable.addCell(createSummaryLabelCell("Tổng tiền:", boldFont));
            summaryTable.addCell(createSummaryValueCell(
                    String.format("%,d d", payment.getGrandTotal()),
                    boldFont,
                    new DeviceRgb(231, 76, 60),
                    18));

            // Amount paid
            summaryTable.addCell(createSummaryLabelCell("Khách trả:", boldFont));
            summaryTable.addCell(createSummaryValueCell(
                    String.format("%,d d", payment.getAmountPaid()),
                    boldFont,
                    new DeviceRgb(39, 174, 96),
                    16));

            // Change
            Integer change = payment.getAmountPaid() - payment.getGrandTotal();
            summaryTable.addCell(createSummaryLabelCell("Tiền thừa:", boldFont));
            summaryTable.addCell(createSummaryValueCell(
                    String.format("%,d d", change),
                    boldFont,
                    new DeviceRgb(52, 152, 219),
                    16));

            document.add(summaryTable);

            // Separator line
            SolidLine line4 = new SolidLine(1f);
            line4.setColor(new DeviceRgb(189, 195, 199));
            LineSeparator separator4 = new LineSeparator(line4);
            separator4.setMarginTop(15);
            separator4.setMarginBottom(15);
            document.add(separator4);

            // FOOTER
            Paragraph thanks = new Paragraph("XIN CẢM ƠN QUÝ KHÁCH!")
                    .setFont(boldFont)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(39, 174, 96))
                    .setMarginTop(10)
                    .setMarginBottom(10);
            document.add(thanks);

            Paragraph note1 = new Paragraph("Quý khách vui lòng giữ hóa đơn để đổi trả hoặc bảo hành dịch vụ")
                    .setFont(font)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(127, 140, 141))
                    .setMarginBottom(3);
            document.add(note1);

            Paragraph note2 = new Paragraph("Quet ma QR de nhan tu van va ho tro truc tuyen")
                    .setFont(font)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(127, 140, 141))
                    .setMarginBottom(10);
            document.add(note2);

            // Add QR Code Image
            try {
                // Load QR code from resources
                InputStream qrStream = PDFExporter.class.getResourceAsStream("/Image/qrcode.png");
                if (qrStream != null) {
                    byte[] qrBytes = qrStream.readAllBytes();
                    Image qrImage = new Image(ImageDataFactory.create(qrBytes));
                    qrImage.setWidth(120);
                    qrImage.setHeight(120);
                    qrImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
                    qrImage.setMarginTop(10);
                    qrImage.setMarginBottom(10);
                    document.add(qrImage);

                    Paragraph qrLabel = new Paragraph("Quet ma de thanh toan")
                            .setFont(font)
                            .setFontSize(11)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontColor(new DeviceRgb(127, 140, 141))
                            .setMarginBottom(10);
                    document.add(qrLabel);
                    qrStream.close();
                }
            } catch (Exception e) {
                System.err.println("Could not load QR code image: " + e.getMessage());
                // Continue without QR code if image not found
            }

            Paragraph farewell = new Paragraph("Hen gap lai Quy khach!")
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(149, 165, 166))
                    .setItalic()
                    .setMarginTop(10);
            document.add(farewell);

            // Close document
            document.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper methods for creating cells
    private static Cell createInfoLabelCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(11).setFontColor(new DeviceRgb(52, 73, 94)))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPaddingRight(10)
                .setPaddingTop(5)
                .setPaddingBottom(5);
    }

    private static Cell createInfoValueCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(11).setFontColor(new DeviceRgb(44, 62, 80)))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.LEFT)
                .setPaddingLeft(10)
                .setPaddingTop(5)
                .setPaddingBottom(5);
    }

    private static Cell createTableCell(String text, PdfFont font, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(10))
                .setTextAlignment(alignment)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(6)
                .setBorder(new SolidBorder(new DeviceRgb(224, 224, 224), 0.5f));
    }

    private static Cell createSummaryLabelCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(14).setFontColor(new DeviceRgb(52, 73, 94)))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPaddingRight(20)
                .setPaddingTop(5)
                .setPaddingBottom(5);
    }

    private static Cell createSummaryValueCell(String text, PdfFont font, DeviceRgb color, int fontSize) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(fontSize).setFontColor(color))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPaddingTop(5)
                .setPaddingBottom(5);
    }
}
