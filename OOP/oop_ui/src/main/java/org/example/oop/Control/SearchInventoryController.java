package org.example.oop.Control;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.control.ProgressIndicator;
import org.example.oop.Model.Inventory.InventoryRow;
import org.example.oop.Utils.AppConfig;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

public class SearchInventoryController {
    @FXML
    private TableColumn<InventoryRow, String> categoryColumn;
    @FXML
    private TableColumn<InventoryRow, Integer> idColumn;
    @FXML
    private TableView<InventoryRow> inventoryTable;
    @FXML
    private TableColumn<InventoryRow, LocalDate> lastUpdatedColumn;
    @FXML
    private TableColumn<InventoryRow, String> nameColumn;
    @FXML
    private TableColumn<InventoryRow, Integer> priceColumn;
    @FXML
    private TableColumn<InventoryRow, Integer> quantityColumn;
    @FXML
    private Button resetButton;
    @FXML
    private Button searchButton;
    @FXML
    private TextField searchTextField;
    @FXML
    private TableColumn<InventoryRow, String> typeColumn;
    @FXML
    private TableColumn<InventoryRow, String> unitColumn;
    @FXML
    private DatePicker lastUpdatedPicker;
    @FXML
    private ComboBox<String> categoryBox;
    @FXML
    private ComboBox<String> typeBox;
    @FXML
    private Label itemName;
    @FXML
    TextArea itemDescription;
    @FXML
    private TextField skuTextField;
    @FXML
    private ComboBox<String> statusBox;
    @FXML
    private ComboBox<String> supplierBox;
    @FXML
    private Button exportButton;

    // Columns thiếu
    @FXML
    private TableColumn<InventoryRow, String> skuColumn;
    @FXML
    private TableColumn<InventoryRow, String> supplierColumn;
    @FXML
    private TableColumn<InventoryRow, String> statusColumn;

    // Quick info fields thiếu
    @FXML
    private Label quickSku;
    @FXML
    private Label quickSupplier;
    @FXML
    private Label quickStatus;
    @FXML
    private Label quickPrice;

    // UI components thiếu
    @FXML
    private ImageView itemImage;
    @FXML
    private Label messageLabel;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Label countLabel;

    private final InventoriesController inventoriesController = new InventoriesController();
    private ObservableList<InventoryRow> masterData = FXCollections.observableArrayList();
    private FilteredList<InventoryRow> rowFilteredList;

    @FXML
    public void initialize() throws IOException {
        InitTable();
        InitTypeBox();
        InitCategoryBox();
        InitStatusBox();
        InitSupplierBox();
        rowFilteredList = new FilteredList<>(masterData, predicate -> true);
        searchTextField.setOnAction(event -> updatePredicate());
        typeBox.setOnAction(event -> updatePredicate());
        categoryBox.setOnAction(event -> updatePredicate());
        lastUpdatedPicker.setOnAction(event -> updatePredicate());
        skuTextField.setOnAction(event -> updatePredicate());
        statusBox.setOnAction(event -> updatePredicate());
        supplierBox.setOnAction(event -> updatePredicate());
        updatePredicate();
    }

    private void InitSupplierBox() {
        Set<String> suppliers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (InventoryRow row : masterData) {
            if (row != null) {
                String supplier = row.getSupplier();
                if (supplier != null) {
                    supplier = supplier.trim();
                    if (!supplier.isEmpty()) {
                        suppliers.add(supplier);
                    }
                }
            }
        }
        ObservableList<String> itemTypes = FXCollections.observableArrayList();
        itemTypes.add("Suppliers");
        itemTypes.addAll(suppliers);
        supplierBox.setItems(itemTypes);
        supplierBox.getSelectionModel().selectFirst();
    }

    private void InitStatusBox() {
        ObservableList<String> statuses = FXCollections.observableArrayList();
        statuses.add("Status"); // Default value
        statuses.add("IN_STOCK");
        statuses.add("LOW_STOCK");
        statuses.add("OUT_OF_STOCK");

        statusBox.setItems(statuses);
        statusBox.getSelectionModel().selectFirst();
    }

    @FXML
    void OnClickResetButton(ActionEvent event) {
        searchTextField.clear();
        skuTextField.clear();
        typeBox.getSelectionModel().selectFirst(); // Select "Type"
        categoryBox.getSelectionModel().selectFirst(); // Select "Category"
        statusBox.getSelectionModel().selectFirst(); // Select "Status"
        supplierBox.getSelectionModel().selectFirst(); // Select "Suppliers"
        lastUpdatedPicker.setValue(null);

        updatePredicate();
    }

    @FXML
    void OnClickSearchButton(ActionEvent event) {
        updatePredicate();
    }

    @FXML
    void OnClickExportButton(ActionEvent event) {

        System.out.println("Export clicked - To be implemented");
        messageLabel.setText("Export functionality coming soon");
    }

    private void InitTable() throws IOException {
        idColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        nameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getName()));
        typeColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getType()));
        categoryColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCategory()));
        quantityColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQuantity()));
        unitColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getUnit()));
        priceColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getUnitPrice()));
        lastUpdatedColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getLastUpdated()));
        skuColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSku()));
        supplierColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSupplier()));
        statusColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getStockStatus()));

        masterData = inventoriesController.loadInventory(AppConfig.TEST_DATA_TXT);

        inventoryTable.setItems(masterData);
        inventoryTable.getSortOrder().clear();
        inventoryTable.getSortOrder().add(idColumn);
        inventoryTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        inventoryTable.getSelectionModel().selectedItemProperty()
                .addListener((item, oldRow, newRow) -> updateDetail(newRow));
    }

    private void InitCategoryBox() {
        Set<String> categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (InventoryRow row : masterData) {
            if (row != null) {
                String category = row.getCategory();
                if (category != null) {
                    category = category.trim();
                    if (!category.isEmpty()) {
                        categories.add(category);
                    }
                }
            }
        }
        ObservableList<String> itemTypes = FXCollections.observableArrayList();
        itemTypes.add("Category");
        itemTypes.addAll(categories);
        categoryBox.setItems(itemTypes);
        categoryBox.getSelectionModel().selectFirst();
    }

    private void InitTypeBox() {
        Set<String> types = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (InventoryRow row : masterData) {
            if (row != null) {
                String type = row.getType();
                if (type != null) {
                    type = type.trim();
                    if (!type.isEmpty()) {
                        types.add(type);
                    }
                }
            }
        }
        ObservableList<String> itemTypes = FXCollections.observableArrayList();
        itemTypes.add("Type");
        itemTypes.addAll(types);
        typeBox.setItems(itemTypes);
        typeBox.getSelectionModel().selectFirst();
    }

    // Sau dùng database sẽ sửa ở hàm này thành hàm gọi liên kết đến db t
    // InventoriesController
    private void updatePredicate() {
        String supplierSelected = supplierBox.getSelectionModel().getSelectedItem();
        boolean allSuppliers = (supplierSelected == null || supplierSelected.equalsIgnoreCase("Suppliers")); // ✅ SỬA:

        String keyword = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String skuKeyword = skuTextField.getText() == null ? "" : skuTextField.getText().trim().toLowerCase();

        String typeSelected = typeBox.getSelectionModel().getSelectedItem();
        boolean allTypes = (typeSelected == null || typeSelected.equalsIgnoreCase("Type"));

        String categorySelected = categoryBox.getSelectionModel().getSelectedItem();
        boolean allCategories = (categorySelected == null || categorySelected.equalsIgnoreCase("Category")); // ✅ SỬA

        LocalDate dateSelected = lastUpdatedPicker.getValue();

        String statusSelected = statusBox.getSelectionModel().getSelectedItem();
        boolean allStatuses = (statusSelected == null || statusSelected.equalsIgnoreCase("Status"));

        rowFilteredList.setPredicate(row -> {
            boolean matchSupplier = allSuppliers ||
                    (row.getSupplier() != null && row.getSupplier().equalsIgnoreCase(supplierSelected));

            boolean matchKeyword = keyword.isEmpty() ||
                    (row.getName() != null && row.getName().toLowerCase().contains(keyword));

            // ✅ SỬA: Search by SKU (không phải ID!)
            boolean matchSku = skuKeyword.isEmpty() ||
                    (row.getSku() != null && row.getSku().toLowerCase().contains(skuKeyword));

            boolean matchType = allTypes ||
                    (row.getType() != null && row.getType().equalsIgnoreCase(typeSelected));

            boolean matchCategory = allCategories ||
                    (row.getCategory() != null && row.getCategory().equalsIgnoreCase(categorySelected));

            boolean matchDate = (dateSelected == null) ||
                    (row.getLastUpdated() != null && row.getLastUpdated().equals(dateSelected));

            boolean matchStatus = allStatuses ||
                    (row.getStockStatus() != null && row.getStockStatus().equalsIgnoreCase(statusSelected));

            // ✅ SỬA: matchSku (không phải mathSku!)
            return matchSku && matchType && matchKeyword && matchCategory && matchDate && matchStatus && matchSupplier;
        });

        inventoryTable.setItems(rowFilteredList);
        updateCountLabel();
    }

    private void updateDetail(InventoryRow row) {
        if (row == null) {
            itemName.setText("");
            itemDescription.setText("");

            // ✅ SỬA: Dùng setText() cho Label
            quickSku.setText("");
            quickSupplier.setText("");
            quickStatus.setText("");
            quickPrice.setText("");
            return;
        }

        itemName.setText(row.getName());
        itemDescription.setText("Nothing to describe");

        // ✅ SỬA: Dùng setText() cho Label
        quickSku.setText(row.getSku() != null ? row.getSku() : "N/A");
        quickSupplier.setText(row.getSupplier() != null ? row.getSupplier() : "N/A");
        quickStatus.setText(row.getStockStatus() != null ? row.getStockStatus() : "N/A");

        if (row.getUnitPrice() != null) {
            quickPrice.setText(String.format("%,d VND", row.getUnitPrice()));
        } else {
            quickPrice.setText("0 VND");
        }
    }

    private void updateCountLabel() {
        int count = rowFilteredList.size();
        countLabel.setText(count + " items");

        if (count == 0) {
            messageLabel.setText("No items found");
        } else {
            messageLabel.setText("Ready");
        }
    }
}
