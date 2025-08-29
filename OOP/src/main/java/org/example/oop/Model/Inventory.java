package org.example.oop.Model;

import java.time.LocalDate;
import java.util.Date;

public class Inventory {
    private int id;
    private String name;
    private String type;
    private String category;
    private int quantity;
    private String unit;
    private int unitPrice;
    private LocalDate lastUpdated;
    private String desciption;

    public Inventory(int id, String name, String type, String category, int quantity, String unit, int unitPrice, LocalDate lastUpdated) {
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

    public int getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public String getCategory() {
        return category;
    }

    public String getUnit() {
        return unit;
    }

    public void setDesciption(String desciption) {
        this.desciption = desciption;
    }
}
