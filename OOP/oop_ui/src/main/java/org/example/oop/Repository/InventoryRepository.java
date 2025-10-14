package org.example.oop.Repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.example.oop.Model.Inventory.Inventory;
import org.example.oop.Utils.AppConfig;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InventoryRepository {
    public ObservableList<Inventory> loadInventory(String path) throws IOException {
        InputStream input = getClass().getResourceAsStream(path);
        if (input == null) {
            throw new IllegalStateException("Not Found " + AppConfig.TEST_DATA_TXT);
        }
        ObservableList<Inventory> inventoryList = FXCollections.observableArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        String line;
        boolean isFirstLine = true; // ✅ FIX: Track first line

        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }

            // ✅ FIX: Skip header line
            if (isFirstLine) {
                isFirstLine = false;
                // Check if this is a header (starts with "id,name,...")
                if (line.trim().toLowerCase().startsWith("id,")) {
                    continue;
                }
            }

            String[] parts = line.trim().split(",");
            // Format:
            // id,name,type,category,quantity,unit,unitPrice,priceCost,lastUpdated,sku
            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            String type = parts[2].trim();
            String category = parts[3].trim();
            int quantity = Integer.parseInt(parts[4].trim());
            String unit = parts[5].trim();
            int unitPrice = Integer.parseInt(parts[6].trim());
            int priceCost = Integer.parseInt(parts[7].trim());
            LocalDate lastUpdated = LocalDate.parse(parts[8].trim());
            String sku = parts[9].trim();

            Inventory inventory = new Inventory(id, name, type, category, quantity, unit, unitPrice, priceCost,
                    lastUpdated);
            inventory.setSku(sku);
            inventory.setStockStatus();
            inventoryList.add(inventory);
        }
        return inventoryList;
    }

    public void saveInventory(ObservableList<Inventory> inventoryList, String path) throws Exception {
        // ✅ FIX: Get absolute path to resources folder
        String absolutePath = "c:/BTL_OOP/BTL_OOP/OOP/oop_ui/src/main/resources" + path;
        File file = new File(absolutePath);

        System.out.println("💾 Saving to: " + absolutePath);

        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
            // ✅ FIX: Write header line
            writer.println("id,name,type,category,quantity,unit,unitPrice,priceCost,lastUpdated,sku");

            // ✅ FIX: Include priceCost in format (10 columns)
            for (Inventory inventory : inventoryList) {
                writer.printf("%d,%s,%s,%s,%d,%s,%d,%d,%s,%s%n",
                        inventory.getId(),
                        inventory.getName(),
                        inventory.getType(),
                        inventory.getCategory(),
                        inventory.getQuantity(),
                        inventory.getUnit(),
                        inventory.getUnitPrice(),
                        inventory.getPriceCost() != null ? inventory.getPriceCost() : 0,
                        inventory.getLastUpdated(),
                        inventory.getSku());
            }
        }
        System.out.println("✅ Saved " + inventoryList.size() + " items successfully!");
    }

    public void AddInventory(ObservableList<Inventory> inventoryList, Inventory inventory) {
        if (inventory == null) {
            throw new IllegalArgumentException("Inventory cannot be null");
        }
        int max_id = inventoryList.stream()
                .mapToInt(Inventory::getId)
                .max().orElse(0);
        inventory.setId(max_id + 1);
        inventoryList.add(inventory);
    }

    public boolean updateInventory(ObservableList<Inventory> inventoryList, int id, Inventory updatedInventory) {
        for (int i = 0; i < inventoryList.size(); i++) {
            Inventory inventory = inventoryList.get(i);
            if (inventory.getId() == id) {
                inventory.setName(updatedInventory.getName());
                inventory.setType(updatedInventory.getType());
                inventory.setCategory(updatedInventory.getCategory());
                inventory.setQuantity(updatedInventory.getQuantity());
                inventory.setUnit(updatedInventory.getUnit());
                inventory.setUnitPrice(updatedInventory.getUnitPrice());
                inventory.setLastUpdated(updatedInventory.getLastUpdated());
                inventory.setSku(updatedInventory.getSku());
                return true;
            }
        }
        return false;
    }

    public boolean deleteInventory(ObservableList<Inventory> inventoryList, int id) {
        return inventoryList.removeIf(inventory -> inventory.getId() == id);
    }

    public ObservableList<Inventory> getLowStockItems(ObservableList<Inventory> list) {
        return list.stream().filter(inventory -> "LOW_STOCK".equalsIgnoreCase(inventory.getStockStatus()) ||
                "OUT_OF_STOCK".equalsIgnoreCase(inventory.getStockStatus()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public Inventory searchBySku(ObservableList<Inventory> inventoryList, String sku) {
        return inventoryList.stream()
                .filter(inventory -> sku.equalsIgnoreCase(inventory.getSku()))
                .findFirst()
                .orElse(null);
    }

    public Inventory searchByName(ObservableList<Inventory> inventoryList, String name) {
        return inventoryList.stream()
                .filter(inventory -> name.toLowerCase().equalsIgnoreCase(inventory.getName().toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    public void upsertQty(int productId, int qtyChange) throws IOException {
        String absolutePath = "c:/BTL_OOP/BTL_OOP/OOP/oop_ui/src/main/resources" + AppConfig.TEST_DATA_TXT;
        File file = new File(absolutePath);

        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        List<String> newLines = new ArrayList<>();

        if (lines.isEmpty())
            return;
        newLines.add(lines.get(0)); // dòng header

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split(",", -1);
            if (parts.length < 10)
                continue;

            int id = Integer.parseInt(parts[0].trim());
            int quantity = Integer.parseInt(parts[4].trim());

            if (id == productId) {
                quantity += qtyChange;
                parts[4] = String.valueOf(quantity);
            }
            newLines.add(String.join(",", parts));
        }

        Files.write(file.toPath(), newLines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public synchronized int getQty(int productId) {
        // ✅ FIX: Sử dụng đường dẫn tuyệt đối như các phương thức khác
        String absolutePath = "c:/BTL_OOP/BTL_OOP/OOP/oop_ui/src/main/resources" + AppConfig.TEST_DATA_TXT;
        File f = new File(absolutePath);
        if (!f.exists())
            return 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {

            String line;
            int idIdx = 0; // mặc định: id ở cột 0
            Integer qtyIdx = null; // sẽ dò: onHand/qty/quantity/stock
            boolean headerScanned = false;

            // ---- Đọc dòng đầu để dò header (nếu có) ----
            br.mark(1 << 16);
            String first = br.readLine();
            while (first != null && first.isBlank())
                first = br.readLine();

            if (first == null)
                return 0;

            // ✅ FIX: Sử dụng dấu phẩy thay vì |
            String[] firstCols = first.split(",", -1);
            boolean firstIdIsNumber = tryParseInt(safe(firstCols, 0)) != null;

            if (!firstIdIsNumber) {
                // Có header
                headerScanned = true;
                for (int i = 0; i < firstCols.length; i++) {
                    String h = safe(firstCols, i).toLowerCase();
                    if (h.equals("id") || h.equals("productid") || h.equals("product_id"))
                        idIdx = i;
                }
                for (int i = 0; i < firstCols.length; i++) {
                    String h = safe(firstCols, i).toLowerCase();
                    if (h.equals("onhand") || h.equals("qty") || h.equals("quantity") ||
                            h.equals("stock") || h.equals("stockonhand") || h.equals("on_hand")) {
                        qtyIdx = i;
                        break;
                    }
                }
            }

            // Nếu không có header → quay về đầu file
            if (!headerScanned)
                br.reset();

            // ---- Duyệt từng dòng dữ liệu ----
            while ((line = br.readLine()) != null) {
                if (line.isBlank())
                    continue;
                // ✅ FIX: Sử dụng dấu phẩy thay vì |
                String[] p = line.split(",", -1);

                Integer idVal = tryParseInt(safe(p, idIdx));
                if (idVal == null)
                    continue; // bỏ dòng rác/tiêu đề
                if (idVal != productId)
                    continue;

                Integer q;
                if (qtyIdx != null && qtyIdx < p.length) {
                    q = tryParseInt(safe(p, qtyIdx));
                    if (q != null)
                        return q;
                }

                // Fallback (không header hoặc header không có cột qty):
                // 1) thử cột 4 như bạn đang dùng (quantity column)
                if (p.length > 4) {
                    q = tryParseInt(safe(p, 4));
                    if (q != null)
                        return q;
                }
                // 2) nếu vẫn fail, lấy "cột số cuối cùng" trong dòng làm qty
                for (int i = p.length - 1; i >= 0; i--) {
                    q = tryParseInt(safe(p, i));
                    if (q != null)
                        return q;
                }
                return 0;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading " + absolutePath, e);
        }
        return 0;
    }

    private static String safe(String[] arr, int i) {
        return i >= 0 && i < arr.length ? arr[i].trim().replace("\uFEFF", "") : "";
    }

    private static Integer tryParseInt(String s) {
        if (s == null || s.isEmpty())
            return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}