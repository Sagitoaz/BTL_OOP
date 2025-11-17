package org.miniboot.app;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import org.miniboot.app.config.HttpConstants;

public class AppConfig {
    // Defaults
    public static String HTTP_PORT = "8080";
    public static int MAX_BODY_BYTES = 1_000_000; // 1MB
    public static int WORKER_THREADS = 64;

    // Keys
    public static final String PORT_KEY = "PORT";
    public static final String MAX_BODY_BYTES_KEY = "MAX_BODY_BYTES";
    public static final String WORKER_THREADS_KEY = "WORKER_THREADS";

    // HTTP constants - sử dụng từ HttpConstants
    @Deprecated public static final String POST_KEY = HttpConstants.METHOD_POST;
    @Deprecated public static final String GET_KEY = HttpConstants.METHOD_GET;
    @Deprecated public static final String PUT_KEY = HttpConstants.METHOD_PUT;
    @Deprecated public static final String DELETE_KEY = HttpConstants.METHOD_DELETE;
    @Deprecated public static final String RES_CONTENT_TYPE_KEY = HttpConstants.HEADER_CONTENT_TYPE;
    @Deprecated public static final String RES_CONTENT_LENGTH_KEY = HttpConstants.HEADER_CONTENT_LENGTH;
    @Deprecated public static final String RES_CONNECTION_KEY = HttpConstants.HEADER_CONNECTION;
    @Deprecated public static final String RES_DATE_KEY = HttpConstants.HEADER_DATE;
    @Deprecated public static final String RES_SERVER_KEY = HttpConstants.HEADER_SERVER;
    @Deprecated public static final String LOCATION_KEY = HttpConstants.HEADER_LOCATION;
    @Deprecated public static final String CONNECTION_CLOSE_KEY = HttpConstants.CONNECTION_CLOSE;
    @Deprecated public static final String SERVER_NAME = HttpConstants.SERVER_NAME;
    @Deprecated public static final String JSON_UTF_8_TYPE = HttpConstants.CONTENT_TYPE_JSON_UTF8;
    @Deprecated public static final String TEXT_UTF_8_TYPE = HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8;
    @Deprecated public static final String RESPONSE_200 = HttpConstants.REASON_OK;
    @Deprecated public static final String RESPONSE_201 = HttpConstants.REASON_CREATED;
    @Deprecated public static final String RESPONSE_400 = HttpConstants.REASON_BAD_REQUEST;
    @Deprecated public static final String RESPONSE_401 = HttpConstants.REASON_UNAUTHORIZED;
    @Deprecated public static final String RESPONSE_403 = HttpConstants.REASON_FORBIDDEN;
    @Deprecated public static final String RESPONSE_404 = HttpConstants.REASON_NOT_FOUND;
    @Deprecated public static final String RESPONSE_405 = HttpConstants.REASON_METHOD_NOT_ALLOWED;
    @Deprecated public static final String RESPONSE_500 = HttpConstants.REASON_INTERNAL_SERVER_ERROR;
    @Deprecated public static final String HTTP_TYPE = HttpConstants.HTTP_VERSION_1_1;

    // Map Errors
    public static final Map<Integer, String> RESPONSE_REASON = Map.ofEntries(
            Map.entry(HttpConstants.STATUS_OK, HttpConstants.REASON_OK),
            Map.entry(HttpConstants.STATUS_CREATED, HttpConstants.REASON_CREATED),
            Map.entry(HttpConstants.STATUS_NO_CONTENT, HttpConstants.REASON_NO_CONTENT),
            Map.entry(HttpConstants.STATUS_MOVED_PERMANENTLY, HttpConstants.REASON_MOVED_PERMANENTLY),
            Map.entry(HttpConstants.STATUS_FOUND, HttpConstants.REASON_FOUND),
            Map.entry(HttpConstants.STATUS_NOT_MODIFIED, HttpConstants.REASON_NOT_MODIFIED),
            Map.entry(HttpConstants.STATUS_BAD_REQUEST, HttpConstants.REASON_BAD_REQUEST),
            Map.entry(HttpConstants.STATUS_UNAUTHORIZED, HttpConstants.REASON_UNAUTHORIZED),
            Map.entry(HttpConstants.STATUS_FORBIDDEN, HttpConstants.REASON_FORBIDDEN),
            Map.entry(HttpConstants.STATUS_NOT_FOUND, HttpConstants.REASON_NOT_FOUND),
            Map.entry(HttpConstants.STATUS_METHOD_NOT_ALLOWED, HttpConstants.REASON_METHOD_NOT_ALLOWED),
            Map.entry(HttpConstants.STATUS_PAYLOAD_TOO_LARGE, HttpConstants.REASON_PAYLOAD_TOO_LARGE),
            Map.entry(HttpConstants.STATUS_INTERNAL_SERVER_ERROR, HttpConstants.REASON_INTERNAL_SERVER_ERROR),
            Map.entry(HttpConstants.STATUS_NOT_IMPLEMENTED, HttpConstants.REASON_NOT_IMPLEMENTED),
            Map.entry(HttpConstants.STATUS_SERVICE_UNAVAILABLE, HttpConstants.REASON_SERVICE_UNAVAILABLE));

    // Utils
    public static final String JSON_PRETTY_KEY = "JSON_PRETTY";
    public static final String JSON_PRETTY_DEFAULT = "false";
    public static final String LOG_LEVEL_KEY = "LOG_LEVEL";
    public static final String LOG_LEVEL_DEFAULT = "INFO";

    // Database Config Keys (used by DatabaseConfig)
    public static final String DB_URL_KEY = "DB_URL";
    public static final String DB_USER_KEY = "DB_USER";
    public static final String DB_PASSWORD_KEY = "DB_PASSWORD";

    // Math
    public static final int MAX_INTEGER_VALUE = 10000000;

    // Data file paths
    public static final String STOCK_TEST_DATA_TXT = "/TestData/stock_movements.txt";
    public static final String INVENTORY_TEST_DATA_TXT = "/TestData/inventory_9cols.txt";

    public static java.io.File getStockDataFile() {
        return new java.io.File("data/stock_movements.txt");
    }

    public static java.io.File getInventoryDataFile() {
        return new java.io.File("data/inventory.txt");
    }

    // load tại startup
    public static void load() {
        Properties p = new Properties();

        // 1) file properties (tùy chọn)
        String path = System.getProperty("APP_PROPS", System.getenv().getOrDefault("APP_PROPS", ""));
        if (!path.isBlank()) {
            try (FileInputStream fis = new FileInputStream(path)) {
                p.load(fis);
            } catch (Exception ignored) {
            }
        }

        // helper
        java.util.function.BiFunction<String, String, String> pick = (key, defVal) -> {
            String sys = System.getProperty(key);
            if (sys != null)
                return sys;
            String env = System.getenv(key);
            if (env != null)
                return env;
            String file = p.getProperty(key);
            return file != null ? file : defVal;
        };

        HTTP_PORT = pick.apply(PORT_KEY, HTTP_PORT);
        MAX_BODY_BYTES = Integer.parseInt(pick.apply(MAX_BODY_BYTES_KEY, String.valueOf(MAX_BODY_BYTES)));
        WORKER_THREADS = Integer.parseInt(pick.apply(WORKER_THREADS_KEY, String.valueOf(WORKER_THREADS)));
    }
}
