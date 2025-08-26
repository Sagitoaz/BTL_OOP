package org.example.oop.View;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.example.oop.Control.InventoriesController;
import org.example.oop.Model.InventoryRow;
import org.example.oop.Utils.AppConfig;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class SearchInventoryView {
    @FXML private TableColumn<InventoryRow, String> CategoryColumn;
    @FXML private TableColumn<InventoryRow, Integer> IdColumn;
    @FXML private TableView<InventoryRow> InventoryTable;
    @FXML private TableColumn<InventoryRow, LocalDate> LastUpdatedColumn;
    @FXML private TableColumn<InventoryRow, String> NameColumn;
    @FXML private TableColumn<InventoryRow, Integer> PriceColumn;
    @FXML private TableColumn<InventoryRow, Integer> QuantityColumn;
    @FXML private Button ResetButton;
    @FXML private Button SearchButton;
    @FXML private TextField SearchTextField;
    @FXML private TableColumn<InventoryRow, String> TypeColumn;
    @FXML private TableColumn<InventoryRow, String> UnitColumn;

    private InventoriesController inventoriesController = new InventoriesController();

    @FXML public void initialize() throws IOException {
        IdColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        NameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getName()));
        TypeColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getType()));
        CategoryColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCategory()));
        QuantityColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQuantity()));
        UnitColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getUnit()));
        PriceColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getUnitPrice()));
        LastUpdatedColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getLastUpdated()));

        List<InventoryRow> raw = inventoriesController.loadInventory(AppConfig.TEST_DATA_TXT);
        ObservableList<InventoryRow> data = FXCollections.observableArrayList(raw);

        InventoryTable.setItems(data);
    }

    @FXML
    void OnClickResetButton(ActionEvent event) {

    }

    @FXML
    void OnClickSearchButton(ActionEvent event) {
        String keyword = SearchTextField.getText();
    }

}
