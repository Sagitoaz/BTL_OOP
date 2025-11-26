package org.example.oop.Utils;

/**
 * Payment API Configuration
 * Định nghĩa các endpoint cho Payment API
 * ĐỒNG BỘ VỚI BACKEND - DO NOT CHANGE
 */
public class PaymentConfig {
    // Payment endpoints (backend: /payments)
    public static final String GET_PAYMENT_ENDPOINT = "/payments";
    public static final String POST_PAYMENT_ENDPOINT = "/payments";
    public static final String PUT_PAYMENT_ENDPOINT = "/payments";
    public static final String GET_PAYMENT_WITH_STATUS_ENDPOINT = "/payments/with-status";
    
    // Payment Item endpoints (backend: /payment-items)
    public static final String GET_PAYMENT_ITEM_ENDPOINT = "/payment-items";
    public static final String POST_PAYMENT_ITEM_ENDPOINT = "/payment-items";
    public static final String PUT_PAYMENT_ITEM_ENDPOINT = "/payment-items";
    public static final String DELETE_PAYMENT_ITEM_ENDPOINT = "/payment-items";
    
    // Payment Status Log endpoints (backend: /payment-status)
    public static final String GET_PAYMENT_STATUS_LOG_ENDPOINT = "/payment-status";
    public static final String POST_PAYMENT_STATUS_LOG_ENDPOINT = "/payment-status";
    public static final String PUT_PAYMENT_STATUS_LOG_ENDPOINT = "/payment-status";
    
    // Stock Movement endpoints (backend: /stockMovements)
    public static final String GET_STOCK_MOVEMENT_ENDPOINT = "/stockMovements";
    public static final String POST_STOCK_MOVEMENT_ENDPOINT = "/stockMovements";
    public static final String PUT_STOCK_MOVEMENT_ENDPOINT = "/stockMovements";
    public static final String DELETE_STOCK_MOVEMENT_ENDPOINT = "/stockMovements";
    
    private PaymentConfig() {
        // Private constructor to prevent instantiation
    }
}
