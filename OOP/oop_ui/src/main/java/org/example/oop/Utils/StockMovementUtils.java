package org.example.oop.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class cho Stock Movement operations
 */
public class StockMovementUtils {
     private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

     public static String formatMovementDate(LocalDateTime dateTime) {
          return dateTime != null ? dateTime.format(FORMATTER) : "";
     }
}
