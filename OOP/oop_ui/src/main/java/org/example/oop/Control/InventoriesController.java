package org.example.oop.Control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.example.oop.Model.Inventory.Inventory;
import org.example.oop.Model.Inventory.InventoryRow;
import org.example.oop.Utils.AppConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class InventoriesController {
    public ObservableList<InventoryRow> loadInventory(String path) throws IOException {
        InputStream input = getClass().getResourceAsStream(path);
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

    public void saveInventory(ObservableList<InventoryRow> inventoryrow, String path) throws Exception {
        File file = new File(path);
        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
            for (InventoryRow row : inventoryrow) {
                writer.printf("%d,%s,%s,%s,%d,%s,%d",
                        row.getId(),
                        row.getName(),
                        row.getType(),
                        row.getCategory(),
                        row.getQuantity(),
                        row.getUnit(),
                        row.getUnitPrice(),
                        row.getLastUpdated());
            }
        }
    }

    public void AddInventory(ObservableList<InventoryRow> inventoryList, InventoryRow row) {
        if (row == null) {
            throw new IllegalArgumentException("Inventory row cannot be null");
        }
        int max_id = inventoryList.stream()
                .mapToInt(InventoryRow::getId)
                .max().orElse(0);
        row.setId(max_id + 1);
        inventoryList.add(row);
    }

    public boolean updateInventory(ObservableList<InventoryRow> inventoryList, int id, InventoryRow updatedRow) {
        for (int i = 0; i < inventoryList.size(); i++) {
            InventoryRow row = inventoryList.get(i);
            if (row.getId() == id) {
                row.setName(updatedRow.getName());
                row.setType(updatedRow.getType());
                row.setCategory(updatedRow.getCategory());
                row.setQuantity(updatedRow.getQuantity());
                row.setUnit(updatedRow.getUnit());
                row.setUnitPrice(updatedRow.getUnitPrice());
                row.setLastUpdated(updatedRow.getLastUpdated());
                return true;
            }
        }
        return false;
    }

    public boolean deleteInventory(ObservableList<InventoryRow> inventoryList, int id) {
        return inventoryList.removeIf(row -> row.getId() == id);
    }

    public ObservableList<InventoryRow> getLowStockItems(ObservableList<InventoryRow> list) {
        return list.stream().filter(row -> "LOW_STOCK".equalsIgnoreCase(row.getStockStatus()) ||
                "OUT_OF_STOCK".equalsIgnoreCase(row.getStockStatus()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public InventoryRow searchBySku(ObservableList<InventoryRow> inventoryList, String sku) {
        return inventoryList.stream()
                .filter(row -> sku.equalsIgnoreCase(row.getSku()))
                .findFirst()
                .orElse(null);
    }
}