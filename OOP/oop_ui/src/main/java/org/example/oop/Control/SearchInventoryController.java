package org.example.oop.Control;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.example.oop.Model.Inventory.InventoryRow;
import org.example.oop.Utils.AppConfig;

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

    private final InventoriesController inventoriesController = new InventoriesController();
    private ObservableList<InventoryRow> masterData = FXCollections.observableArrayList();
    private FilteredList<InventoryRow> rowFilteredList;

    @FXML
    public void initialize() throws IOException {
        InitTable();
        InitTypeBox();
        InitCategoryBox();

        rowFilteredList = new FilteredList<>(masterData, predicate -> true);
        searchTextField.setOnAction(event -> updatePredicate());
        typeBox.setOnAction(event -> updatePredicate());
        categoryBox.setOnAction(event -> updatePredicate());
        lastUpdatedPicker.setOnAction(event -> updatePredicate());
        updatePredicate();
    }

    @FXML
    void OnClickResetButton(ActionEvent event) {
        searchTextField.clear();
        typeBox.getSelectionModel().selectFirst();
        categoryBox.getSelectionModel().selectFirst();
        lastUpdatedPicker.setValue(null);
        updatePredicate();
    }

    @FXML
    void OnClickSearchButton(ActionEvent event) {
        updatePredicate();
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

        masterData = inventoriesController.loadInventory(AppConfig.TEST_DATA_TXT);

        inventoryTable.setItems(masterData);
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
        String keyword = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();

        String typeSelected = typeBox.getSelectionModel().getSelectedItem();
        boolean allTypes = (typeSelected == null || typeSelected.equalsIgnoreCase("Type"));

        String categorySelected = categoryBox.getSelectionModel().getSelectedItem();
        boolean allCagories = (categorySelected == null || categorySelected.equalsIgnoreCase("Category"));

        LocalDate dateSelected = lastUpdatedPicker.getValue();

        rowFilteredList.setPredicate(row -> {
            boolean matchKeyword = keyword.isEmpty() ||
                    (row.getName() != null && row.getName().toLowerCase().contains(keyword));

            boolean matchType = allTypes ||
                    (row.getType() != null && row.getType().equalsIgnoreCase(typeSelected));

            boolean matchCategory = allCagories ||
                    (row.getCategory() != null && row.getCategory().equalsIgnoreCase(categorySelected));

            boolean matchDate = (dateSelected == null) ||
                    (row.getLastUpdated() != null && row.getLastUpdated().equals(dateSelected));

            return matchType && matchKeyword && matchCategory && matchDate;
        });
        inventoryTable.setItems(rowFilteredList);
    }

    private void updateDetail(InventoryRow row) {
        if (row == null) {
            itemName.setText("");
            itemDescription.setText("");
            return;
        }
        itemName.setText(row.getName());
        itemDescription.setText("Nothing to describe");
    }
}
