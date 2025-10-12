package org.example.oop.Utils;

import java.io.File;

public class AppConfig {
    public static String TEST_DATA_TXT = "/TestData/inventory_9cols.txt";
    public static String STOCK_TEST_DATA_TXT = "/TestData/stock_movements.txt";

    public static File getStockDataFile() {
        String root = "c:/BTL_OOP/BTL_OOP/OOP/oop_ui"; // hoặc System.getProperty("user.dir")
        return new File(root + "/src/main/resources" + STOCK_TEST_DATA_TXT);
    }
}
