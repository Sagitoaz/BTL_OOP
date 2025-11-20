package org.miniboot.app.util;

/**
 * Payment API Configuration
 * Định nghĩa các endpoint cho Payment API
 */
public class PaymentConfig {

    // Payment endpoints
    public static final String GET_PAYMENT_ENDPOINT = "/payments";
    public static final String POST_PAYMENT_ENDPOINT = "/payments";
    public static final String PUT_PAYMENT_ENDPOINT = "/payments";
    public static final String GET_PAYMENT_WITH_STATUS_ENDPOINT = "/payments/with-status";

    // Payment Item endpoints
    public static final String GET_PAYMENT_ITEM_ENDPOINT = "/payment-items";
    public static final String POST_PAYMENT_ITEM_ENDPOINT = "/payment-items";
    public static final String PUT_PAYMENT_ITEM_ENDPOINT = "/payment-items";
    public static final String DELETE_PAYMENT_ITEM_ENDPOINT = "/payment-items";

    // Payment Status Log endpoints
    public static final String GET_PAYMENT_STATUS_LOG_ENDPOINT = "/payment-status";
    public static final String POST_PAYMENT_STATUS_LOG_ENDPOINT = "/payment-status";
    public static final String PUT_PAYMENT_STATUS_LOG_ENDPOINT = "/payment-status";

    // Stock Movement endpoints
    public static final String GET_STOCK_MOVEMENT_ENDPOINT = "/stockMovements";
    public static final String POST_STOCK_MOVEMENT_ENDPOINT = "/stockMovements";
    public static final String PUT_STOCK_MOVEMENT_ENDPOINT = "/stockMovements";
    public static final String DELETE_STOCK_MOVEMENT_ENDPOINT = "/stockMovements";

    private PaymentConfig() {
        // Private constructor to prevent instantiation
    }
}
