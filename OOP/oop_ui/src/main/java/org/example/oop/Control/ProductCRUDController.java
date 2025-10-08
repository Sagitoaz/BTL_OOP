package org.example.oop.Control;

import java.io.IOException;
import java.time.LocalDate;

import org.example.oop.Model.Inventory.InventoryRow;
import org.example.oop.Utils.AppConfig;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ProductCRUDController {
     // === FXML Fields - Table ===
     @FXML
     private TableView<InventoryRow> productTable;
     @FXML
     private TableColumn<InventoryRow, Integer> idColumn;
     @FXML
     private TableColumn<InventoryRow, String> skuColumn;
     @FXML
     private TableColumn<InventoryRow, String> nameColumn;
     @FXML // ✅ THÊM @FXML
     private TableColumn<InventoryRow, String> categoryColumn;
     @FXML // ✅ THÊM @FXML
     private TableColumn<InventoryRow, Integer> quantityColumn; // ✅ SỬA thành Integer
     @FXML // ✅ THÊM @FXML
     private TableColumn<InventoryRow, Integer> priceColumn; // ✅ SỬA thành Integer

     // === FXML Fields - Form ===
     @FXML
     private TextField skuField;
     @FXML
     private TextField nameField;
     @FXML // ✅ THÊM @FXML
     private TextField quantityField;
     @FXML // ✅ THÊM @FXML
     private TextField unitField;
     @FXML // ✅ THÊM @FXML
     private TextField priceField;
     @FXML // ✅ THÊM @FXML - MỚI
     private TextField supplierField;
     @FXML // ✅ THÊM @FXML - MỚI
     private TextArea descriptionArea;

     @FXML
     private ComboBox<String> typeBox;
     @FXML
     private ComboBox<String> categoryBox;

     // === FXML Fields - Buttons & Status ===
     @FXML
     private Label statusLabel;
     @FXML
     private Button saveButton;
     @FXML
     private Button deleteButton;
     @FXML
     private Button clearButton;

     // === Data ===
     private ObservableList<InventoryRow> productList = FXCollections.observableArrayList();
     private InventoriesController inventoriesController = new InventoriesController();
     private InventoryRow selectedProduct = null;

     @FXML // ✅ THÊM @FXML cho initialize
     public void initialize() throws IOException {
          initTable();
          initFormBoxes();
          loadData();
     }

     // === Event Handlers ===
     @FXML // ✅ THÊM @FXML
     void onSaveButton(ActionEvent event) throws Exception {
          if (!validateForm()) {
               showStatus("Please fill all required fields", true);
               return;
          }
          InventoryRow product = getFormData();
          if (selectedProduct == null) {
               inventoriesController.AddInventory(productList, product);
               showStatus("Product added successfully", false); // ✅ SỬA typo "successdully"
          } else {
               boolean success = inventoriesController.updateInventory(productList, selectedProduct.getId(), product);
               if (success) {
                    showStatus("Product updated successfully", false);
               } else {
                    showStatus("Failed to update product", true);
               }
          }
          inventoriesController.saveInventory(productList, AppConfig.TEST_DATA_TXT);
          clearForm();
          productTable.refresh();
     }

     @FXML
     void onDeleteButton(ActionEvent event) throws Exception {
          if (selectedProduct == null) {
               showStatus("Please select a product to delete", true);
               return;
          }

          Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
          confirmAlert.setTitle("Confirm Delete");
          confirmAlert.setHeaderText("Delete Product?");
          confirmAlert.setContentText("Are you sure you want to delete: " + selectedProduct.getName() + "?");

          if (confirmAlert.showAndWait().get() == ButtonType.OK) {
               boolean success = inventoriesController.deleteInventory(productList, selectedProduct.getId());

               if (success) {
                    inventoriesController.saveInventory(productList, AppConfig.TEST_DATA_TXT);
                    clearForm();
                    productTable.refresh();
                    showStatus("Product deleted successfully", false);
               } else {
                    showStatus("Failed to delete product", true);
               }
          }
     }

     @FXML
     void onClearButton(ActionEvent event) {
          clearForm();
     }

     // === Helper Methods ===
     private void initTable() {
          // ✅ SỬA: Xóa dòng lặp và fix logic
          idColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
          skuColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getSku()));
          nameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getName()));
          categoryColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCategory())); // ✅
                                                                                                                  // SỬA
          quantityColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getQuantity())); // ✅
                                                                                                                  // SỬA
          priceColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getUnitPrice())); // ✅ SỬA

          productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
               if (newSelection != null) {
                    selectedProduct = newSelection;
                    populateForm(newSelection);
               }
          });

          productTable.setItems(productList);
     }

     private void initFormBoxes() {
          ObservableList<String> types = FXCollections.observableArrayList(
                    "Medicine", "Equipment", "Material", "Other");
          typeBox.setItems(types);

          ObservableList<String> categories = FXCollections.observableArrayList(
                    "Drug", "Lens", "Frame", "Tool", "Supply");
          categoryBox.setItems(categories);
     }

     private void loadData() throws IOException {
          productList = inventoriesController.loadInventory(AppConfig.TEST_DATA_TXT);
     }

     private void populateForm(InventoryRow product) {
          if (product == null)
               return;

          skuField.setText(product.getSku());
          nameField.setText(product.getName());
          typeBox.setValue(product.getType());
          categoryBox.setValue(product.getCategory());
          quantityField.setText(String.valueOf(product.getQuantity()));
          unitField.setText(product.getUnit());
          priceField.setText(String.valueOf(product.getUnitPrice()));

          // ✅ SỬA: Bỏ comment nếu có supplier
          if (product.getSupplier() != null) {
               supplierField.setText(product.getSupplier());
          }

          // ✅ THÊM: Clear description (hoặc load nếu có)
          descriptionArea.clear();
     }

     private void clearForm() {
          skuField.clear();
          nameField.clear();
          typeBox.getSelectionModel().clearSelection();
          categoryBox.getSelectionModel().clearSelection();
          quantityField.clear();
          unitField.clear();
          priceField.clear();
          supplierField.clear(); // ✅ THÊM
          descriptionArea.clear(); // ✅ THÊM

          selectedProduct = null;
     }

     private InventoryRow getFormData() {
          InventoryRow row = new InventoryRow();

          row.setSku(skuField.getText());
          row.setName(nameField.getText());
          row.setType(typeBox.getValue());
          row.setCategory(categoryBox.getValue());
          row.setQuantity(Integer.parseInt(quantityField.getText()));
          row.setUnit(unitField.getText());
          row.setUnitPrice(Integer.parseInt(priceField.getText()));
          row.setLastUpdated(LocalDate.now());

          // ✅ SỬA: Thêm supplier nếu có
          if (supplierField.getText() != null && !supplierField.getText().trim().isEmpty()) {
               row.setSupplier(supplierField.getText());
          }

          return row;
     }

     private boolean validateForm() {
          if (skuField.getText() == null || skuField.getText().trim().isEmpty()) {
               return false;
          }
          if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
               return false;
          }
          if (typeBox.getValue() == null || typeBox.getValue().isEmpty()) {
               return false;
          }
          if (categoryBox.getValue() == null || categoryBox.getValue().isEmpty()) {
               return false;
          }

          try {
               Integer.parseInt(quantityField.getText());
               Integer.parseInt(priceField.getText());
          } catch (NumberFormatException e) {
               return false;
          }

          return true;
     }

     private void showStatus(String message, boolean isError) {
          if (statusLabel != null) {
               statusLabel.setText(message);
               statusLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
          }
     }
}
