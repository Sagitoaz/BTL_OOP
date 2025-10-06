package org.example.oop.Control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import org.example.oop.Model.Inventory.InventoryRow;
import org.example.oop.Utils.AppConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public class InventoriesController {
    public ObservableList<InventoryRow> loadInventory(String path) throws IOException {
        InputStream input = getClass().getResourceAsStream(AppConfig.TEST_DATA_TXT);
        if (input == null) {
            throw new IllegalStateException("Not Found " + AppConfig.TEST_DATA_TXT);
        }
        ObservableList<InventoryRow> inventoryList = FXCollections.observableArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.trim().split(",");
            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            String type = parts[2].trim();
            String category = parts[3].trim();
            int quantity = Integer.parseInt(parts[4].trim());
            String unit = parts[5].trim();
            int price = Integer.parseInt(parts[6].trim());
            LocalDate lastUpdated = LocalDate.parse(parts[7].trim());
            inventoryList.add(new InventoryRow(id, name, type, category, quantity, unit, price, lastUpdated));
        }
        return inventoryList;
    }
}