package org.example.oop.Control;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

import org.example.oop.Model.Inventory.Inventory;
import org.example.oop.Repository.InventoryRepository;
import org.example.oop.Utils.AppConfig;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class SearchInventoryController {
    @FXML
    private TableColumn<Inventory, String> categoryColumn;
    @FXML
    private TableColumn<Inventory, Integer> idColumn;
    @FXML
    private TableView<Inventory> inventoryTable;
    @FXML
    private TableColumn<Inventory, LocalDate> lastUpdatedColumn;
    @FXML
    private TableColumn<Inventory, String> nameColumn;
    @FXML
    private TableColumn<Inventory, Integer> priceColumn;
    @FXML
    private TableColumn<Inventory, Integer> quantityColumn;
    @FXML
    private Button resetButton;
    @FXML
    private Button searchButton;
    @FXML
    private TextField searchTextField;
    @FXML
    private TableColumn<Inventory, String> typeColumn;
    @FXML
    private TableColumn<Inventory, String> unitColumn;
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
    private Button exportButton;

    // Columns thiếu
    @FXML
    private TableColumn<Inventory, String> skuColumn;
    @FXML
    private TableColumn<Inventory, String> statusColumn;

    // Quick info fields thiếu
    @FXML
    private Label quickSku;
    @FXML
    private Label quickStatus;
    @FXML
    private Label quickPrice;
    @FXML
    private Label quickType;
    @FXML
    private Label quickCategory;
    @FXML
    private Label quickCostPrice;
    @FXML
    private Label quickQuantity;
    @FXML
    private Label quickUnit;

    // UI components thiếu
    @FXML
    private ImageView itemImage;
    @FXML
    private Label messageLabel;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Label countLabel;

    private final InventoryRepository inventoriesController = new InventoryRepository();
    private ObservableList<Inventory> masterData = FXCollections.observableArrayList();
    private FilteredList<Inventory> rowFilteredList;

    @FXML
    public void initialize() throws IOException {
        InitTable();
        InitTypeBox();
        InitCategoryBox();
        InitStatusBox();
        rowFilteredList = new FilteredList<>(masterData, predicate -> true);
        searchTextField.setOnAction(event -> updatePredicate());
        typeBox.setOnAction(event -> updatePredicate());
        categoryBox.setOnAction(event -> updatePredicate());
        lastUpdatedPicker.setOnAction(event -> updatePredicate());
        skuTextField.setOnAction(event -> updatePredicate());
        statusBox.setOnAction(event -> updatePredicate());
        updatePredicate();
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
        for (Inventory row : masterData) {
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
        for (Inventory row : masterData) {
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
            boolean matchKeyword = keyword.isEmpty() ||
                    (row.getName() != null && row.getName().toLowerCase().contains(keyword));

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
            return matchSku && matchType && matchKeyword && matchCategory && matchDate && matchStatus;
        });

        inventoryTable.setItems(rowFilteredList);
        updateCountLabel();
    }

    private void updateDetail(Inventory row) {
        if (row == null) {
            itemName.setText("");
            itemDescription.setText("");

            quickSku.setText("");
            quickStatus.setText("");
            quickPrice.setText("");
            quickType.setText("");
            quickCategory.setText("");
            quickCostPrice.setText("");
            quickQuantity.setText("");
            quickUnit.setText("");
            return;
        }

        itemName.setText(row.getName());
        itemDescription.setText("Nothing to describe");

        quickSku.setText(row.getSku() != null ? row.getSku() : "N/A");
        quickStatus.setText(row.getStockStatus() != null ? row.getStockStatus() : "N/A");
        quickType.setText(row.getType() != null ? row.getType() : "N/A");
        quickCategory.setText(row.getCategory() != null ? row.getCategory() : "N/A");
        quickQuantity.setText(row.getQuantity() >= 0 ? String.valueOf(row.getQuantity()) : "0");
        quickUnit.setText(row.getUnit() != null ? row.getUnit() : "N/A");

        if (row.getUnitPrice() != null) {
            quickPrice.setText(String.format("%,d VND", row.getUnitPrice()));
        } else {
            quickPrice.setText("0 VND");
        }

        if (row.getPriceCost() != null) {
            quickCostPrice.setText(String.format("%,d VND", row.getPriceCost()));
        } else {
            quickCostPrice.setText("N/A");
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
