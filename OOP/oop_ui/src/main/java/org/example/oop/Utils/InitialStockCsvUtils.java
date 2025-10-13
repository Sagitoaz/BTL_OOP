package org.example.oop.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.example.oop.Model.Inventory.InitialStockLine;

/**
 * Utility class for CSV operations related to Initial Stock
 */
public class InitialStockCsvUtils {

     private static final DateTimeFormatter[] DATE_FORMATTERS = {
               DateTimeFormatter.ofPattern("yyyy-MM-dd"),
               DateTimeFormatter.ofPattern("dd/MM/yyyy"),
               DateTimeFormatter.ofPattern("MM/dd/yyyy"),
               DateTimeFormatter.ofPattern("dd-MM-yyyy")
     };

     /**
      * Import initial stock lines from CSV file
      * Expected format: BatchNo,ExpiryDate,SerialNo,Qty,Note,RefId,Red
      * 
      * @param csvFile CSV file to import
      * @return List of InitialStockLine objects
      * @throws IOException if file reading fails
      */
     public static List<InitialStockLine> importFromCsv(File csvFile) throws IOException {
          List<InitialStockLine> lines = new ArrayList<>();

          try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
               String line;
               int lineNumber = 0;
               boolean isFirstLine = true;

               while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    line = line.trim();

                    // Skip empty lines
                    if (line.isEmpty()) {
                         continue;
                    }

                    // Skip header line (if contains non-numeric data in qty column)
                    if (isFirstLine) {
                         isFirstLine = false;
                         if (line.toLowerCase().contains("batch") || line.toLowerCase().contains("qty") ||
                                   line.toLowerCase().contains("refid") || line.toLowerCase().contains("red")) {
                              continue;
                         }
                    }

                    try {
                         InitialStockLine stockLine = parseCsvLine(line, lineNumber);
                         if (stockLine != null) {
                              lines.add(stockLine);
                         }
                    } catch (Exception e) {
                         System.err.println("⚠️ Error parsing line " + lineNumber + ": " + line);
                         System.err.println("Error: " + e.getMessage());
                         // Continue processing other lines
                    }
               }
          }

          return lines;
     }

     /**
      * Parse a single CSV line into InitialStockLine
      * Format: BatchNo,ExpiryDate,SerialNo,Qty,Note,RefId,Red
      */
     private static InitialStockLine parseCsvLine(String line, int lineNumber) {
          String[] parts = line.split(",", -1); // -1 to keep empty strings

          if (parts.length < 4) {
               throw new IllegalArgumentException("Line must have at least 4 columns: BatchNo,ExpiryDate,SerialNo,Qty");
          }

          InitialStockLine stockLine = new InitialStockLine();

          // Batch No (column 0)
          stockLine.setBatchNo(parts[0].trim());

          // Expiry Date (column 1)
          LocalDate expiryDate = parseDate(parts[1].trim());
          stockLine.setExpiryDate(expiryDate);

          // Serial No (column 2)
          stockLine.setSerialNo(parts[2].trim());

          // Qty (column 3) - required
          try {
               int qty = Integer.parseInt(parts[3].trim());
               stockLine.setQty(qty);
          } catch (NumberFormatException e) {
               throw new IllegalArgumentException("Invalid quantity: " + parts[3]);
          }

          // Note (column 4) - optional
          if (parts.length > 4) {
               stockLine.setNote(parts[4].trim());
          }

          // RefId (column 5) - optional
          if (parts.length > 5) {
               stockLine.setRefid(parts[5].trim());
          }

          // Red (column 6) - optional
          if (parts.length > 6) {
               stockLine.setRed(parts[6].trim());
          }

          return stockLine;
     }

     /**
      * Parse date string with multiple format attempts
      */
     private static LocalDate parseDate(String dateStr) {
          if (dateStr == null || dateStr.trim().isEmpty()) {
               return null;
          }

          dateStr = dateStr.trim();

          for (DateTimeFormatter formatter : DATE_FORMATTERS) {
               try {
                    return LocalDate.parse(dateStr, formatter);
               } catch (DateTimeParseException ignored) {
                    // Try next formatter
               }
          }

          // If all formatters fail, return null (will be handled by caller)
          System.err.println("⚠️ Could not parse date: " + dateStr + " (using null)");
          return null;
     }

     /**
      * Generate sample CSV content for user reference
      */
     public static String generateSampleCsvContent() {
          StringBuilder sb = new StringBuilder();
          sb.append("BatchNo,ExpiryDate,SerialNo,Qty,Note,RefId,Red\n");
          sb.append("BATCH001,2025-12-31,SN001,100,Sample batch 1,REF001,R001\n");
          sb.append("BATCH002,2026-06-30,SN002,50,Sample batch 2,REF002,R002\n");
          sb.append("BATCH003,2025-09-15,,25,No serial number,,\n");
          return sb.toString();
     }

     /**
      * Validate imported lines and return validation summary
      */
     public static ValidationResult validateImportedLines(List<InitialStockLine> lines) {
          ValidationResult result = new ValidationResult();

          for (int i = 0; i < lines.size(); i++) {
               InitialStockLine line = lines.get(i);

               // Check required fields
               if (line.getBatchNo() == null || line.getBatchNo().trim().isEmpty()) {
                    result.addWarning("Line " + (i + 1) + ": Empty batch number");
               }

               if (line.getQty() <= 0) {
                    result.addError("Line " + (i + 1) + ": Quantity must be > 0");
               }

               // Check duplicate batch numbers
               for (int j = i + 1; j < lines.size(); j++) {
                    if (line.getBatchNo() != null &&
                              line.getBatchNo().equals(lines.get(j).getBatchNo()) &&
                              line.getSerialNo() != null &&
                              line.getSerialNo().equals(lines.get(j).getSerialNo())) {
                         result.addWarning("Duplicate batch+serial: " + line.getBatchNo() + " + " + line.getSerialNo());
                    }
               }
          }

          result.setTotalLines(lines.size());
          return result;
     }

     /**
      * Validation result container
      */
     public static class ValidationResult {
          private final List<String> errors = new ArrayList<>();
          private final List<String> warnings = new ArrayList<>();
          private int totalLines = 0;

          public void addError(String error) {
               errors.add(error);
          }

          public void addWarning(String warning) {
               warnings.add(warning);
          }

          public List<String> getErrors() {
               return errors;
          }

          public List<String> getWarnings() {
               return warnings;
          }

          public boolean hasErrors() {
               return !errors.isEmpty();
          }

          public boolean hasWarnings() {
               return !warnings.isEmpty();
          }

          public int getTotalLines() {
               return totalLines;
          }

          public void setTotalLines(int totalLines) {
               this.totalLines = totalLines;
          }

          public String getSummary() {
               StringBuilder sb = new StringBuilder();
               sb.append("Import Summary:\n");
               sb.append("Total lines: ").append(totalLines).append("\n");
               sb.append("Errors: ").append(errors.size()).append("\n");
               sb.append("Warnings: ").append(warnings.size()).append("\n");

               if (hasErrors()) {
                    sb.append("\nErrors:\n");
                    for (String error : errors) {
                         sb.append("- ").append(error).append("\n");
                    }
               }

               if (hasWarnings()) {
                    sb.append("\nWarnings:\n");
                    for (String warning : warnings) {
                         sb.append("- ").append(warning).append("\n");
                    }
               }

               return sb.toString();
          }
     }
}