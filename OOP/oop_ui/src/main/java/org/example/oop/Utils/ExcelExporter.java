package org.example.oop.Utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for exporting data to Excel files
 */
public class ExcelExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Create a basic Excel workbook with headers and data
     *
     * @param sheetName Name of the sheet
     * @param headers   Column headers
     * @param data      Data rows (each row is a list of objects)
     * @return Workbook object
     */
    public static Workbook createWorkbook(String sheetName, List<String> headers, List<List<Object>> data) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);

        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);

        // Create header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        int rowNum = 1;
        for (List<Object> rowData : data) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < rowData.size(); i++) {
                Cell cell = row.createCell(i);
                Object value = rowData.get(i);

                if (value == null) {
                    cell.setCellValue("");
                    cell.setCellStyle(dataStyle);
                } else if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                    cell.setCellStyle(numberStyle);
                } else if (value instanceof LocalDate) {
                    cell.setCellValue(((LocalDate) value).format(DATE_FORMATTER));
                    cell.setCellStyle(dateStyle);
                } else if (value instanceof LocalDateTime) {
                    cell.setCellValue(((LocalDateTime) value).format(DATETIME_FORMATTER));
                    cell.setCellStyle(dateStyle);
                } else if (value instanceof Boolean) {
                    cell.setCellValue((Boolean) value ? "Có" : "Không");
                    cell.setCellStyle(dataStyle);
                } else {
                    cell.setCellValue(value.toString());
                    cell.setCellStyle(dataStyle);
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
            // Add some padding
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }

        return workbook;
    }

    /**
     * Export data to Excel file
     *
     * @param filePath  Full path to save the file
     * @param sheetName Name of the sheet
     * @param headers   Column headers
     * @param data      Data rows
     * @throws IOException If file cannot be written
     */
    public static void exportToFile(String filePath, String sheetName, List<String> headers, List<List<Object>> data) throws IOException {
        Workbook workbook = createWorkbook(sheetName, headers, data);
        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }
    }

    /**
     * Create header style (bold, colored background)
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * Create data cell style
     */
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * Create date cell style
     */
    private static CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Create number cell style
     */
    private static CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    /**
     * Generate a default filename with timestamp
     *
     * @param prefix File name prefix
     * @return Filename with timestamp
     */
    public static String generateFileName(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return prefix + "_" + timestamp + ".xlsx";
    }

    /**
     * Get user's Documents folder path
     *
     * @return Path to Documents folder
     */
    public static String getDocumentsPath() {
        String userHome = System.getProperty("user.home");
        return userHome + "/Documents/EyeClinic/";
    }

    /**
     * Ensure the export directory exists
     *
     * @param directoryPath Path to directory
     */
    public static void ensureDirectoryExists(String directoryPath) {
        java.io.File directory = new java.io.File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}
