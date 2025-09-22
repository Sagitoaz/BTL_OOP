package org.example.oop.Model;

import java.time.LocalDate;
import java.util.Date;

public class InventoryRow {
    private Integer id;
    private String name;
    private String type;
    private String category;
    private Integer quantity;
    private String unit;
    private Integer unitPrice;
    private LocalDate lastUpdated;

    public InventoryRow(int id, String name, String type, String category, int quantity, String unit, int unitPrice, LocalDate lastUpdated) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.lastUpdated = lastUpdated;
    }
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    public Integer getId() {
        return id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getUnitPrice() {
        return unitPrice;
    }

    public String getCategory() {
        return category;
    }

    public String getUnit() {
        return unit;
    }
}
