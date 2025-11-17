package org.miniboot.app.config;

/**
 * DatabaseConstants: Chứa các hằng số liên quan đến Database
 */
public final class DatabaseConstants {

    // Private constructor để ngăn khởi tạo
    private DatabaseConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ========== DATABASE CONFIGURATION KEYS ==========
    public static final String CONFIG_DB_URL = "DB_URL";
    public static final String CONFIG_DB_USER = "DB_USER";
    public static final String CONFIG_DB_PASSWORD = "DB_PASSWORD";
    public static final String CONFIG_DB_DRIVER = "DB_DRIVER";
    public static final String CONFIG_DB_POOL_SIZE = "DB_POOL_SIZE";
    public static final String CONFIG_DB_TIMEOUT = "DB_TIMEOUT";

    // ========== DATABASE DEFAULT VALUES ==========
    public static final String DEFAULT_DB_URL = "jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:6543/postgres";
    public static final String DEFAULT_DB_USER = "postgres.dwcpuomioxgqznusjewq";
    public static final String DEFAULT_DB_PASSWORD = "Nguhotuongd23@";
    public static final String DEFAULT_DB_DRIVER = "org.postgresql.Driver";
    public static final int DEFAULT_DB_POOL_SIZE = 10;
    public static final int DEFAULT_DB_TIMEOUT = 10; // seconds

    // ========== DATABASE DRIVERS ==========
    public static final String DRIVER_POSTGRESQL = "org.postgresql.Driver";
    public static final String DRIVER_MYSQL = "com.mysql.cj.jdbc.Driver";
    public static final String DRIVER_SQLITE = "org.sqlite.JDBC";
    public static final String DRIVER_H2 = "org.h2.Driver";

    // ========== COMMON TABLE NAMES ==========
    public static final String TABLE_USERS = "users";
    public static final String TABLE_CUSTOMERS = "customers";
    public static final String TABLE_EMPLOYEES = "employees";
    public static final String TABLE_DOCTORS = "doctors";
    public static final String TABLE_APPOINTMENTS = "appointments";
    public static final String TABLE_PRESCRIPTIONS = "prescriptions";
    public static final String TABLE_PRODUCTS = "products";
    public static final String TABLE_INVENTORY = "inventory";
    public static final String TABLE_STOCK_MOVEMENTS = "stock_movements";
    public static final String TABLE_PAYMENTS = "payments";
    public static final String TABLE_PAYMENT_ITEMS = "payment_items";
    public static final String TABLE_PAYMENT_STATUS_LOG = "payment_status_log";

    // ========== COMMON COLUMN NAMES ==========
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_UUID = "uuid";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";
    public static final String COLUMN_DELETED_AT = "deleted_at";
    public static final String COLUMN_IS_ACTIVE = "is_active";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_FIRST_NAME = "first_name";
    public static final String COLUMN_LAST_NAME = "last_name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_ROLE = "role";
    public static final String COLUMN_STATUS = "status";

    // ========== SQL KEYWORDS ==========
    public static final String SQL_SELECT = "SELECT";
    public static final String SQL_FROM = "FROM";
    public static final String SQL_WHERE = "WHERE";
    public static final String SQL_AND = "AND";
    public static final String SQL_OR = "OR";
    public static final String SQL_ORDER_BY = "ORDER BY";
    public static final String SQL_GROUP_BY = "GROUP BY";
    public static final String SQL_HAVING = "HAVING";
    public static final String SQL_LIMIT = "LIMIT";
    public static final String SQL_OFFSET = "OFFSET";
    public static final String SQL_INSERT_INTO = "INSERT INTO";
    public static final String SQL_VALUES = "VALUES";
    public static final String SQL_UPDATE = "UPDATE";
    public static final String SQL_SET = "SET";
    public static final String SQL_DELETE_FROM = "DELETE FROM";
    public static final String SQL_ASC = "ASC";
    public static final String SQL_DESC = "DESC";

    // ========== DATABASE ERROR MESSAGES ==========
    public static final String ERROR_CONNECTION_FAILED = "Failed to connect to database";
    public static final String ERROR_QUERY_FAILED = "Failed to execute query";
    public static final String ERROR_DRIVER_NOT_FOUND = "Database driver not found";
    public static final String ERROR_DUPLICATE_KEY = "Duplicate key violation";
    public static final String ERROR_FOREIGN_KEY = "Foreign key constraint violation";
    public static final String ERROR_TIMEOUT = "Database operation timeout";
    public static final String ERROR_TRANSACTION_FAILED = "Transaction failed";

    // ========== DATABASE SUCCESS MESSAGES ==========
    public static final String SUCCESS_CONNECTION = "Database connection established successfully";
    public static final String SUCCESS_QUERY = "Query executed successfully";
    public static final String SUCCESS_INSERT = "Record inserted successfully";
    public static final String SUCCESS_UPDATE = "Record updated successfully";
    public static final String SUCCESS_DELETE = "Record deleted successfully";

    // ========== QUERY PARAMETERS ==========
    public static final String PARAM_LIMIT = "limit";
    public static final String PARAM_OFFSET = "offset";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PAGE_SIZE = "pageSize";
    public static final String PARAM_SORT = "sort";
    public static final String PARAM_ORDER = "order";
    public static final String PARAM_SEARCH = "search";
    public static final String PARAM_FILTER = "filter";
}

