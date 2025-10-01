package org.miniboot.app;

import java.io.FileInputStream;
import java.util.Properties;

public class AppConfig {
    // Defaults
    public static String HTTP_PORT = "8080";
    public static int MAX_BODY_BYTES = 1_000_000;   // 1MB
    public static int WORKER_THREADS = 64;

    // Keys
    public static String PORT_KEY = "PORT";

    // HTTP constants (giữ nguyên)
    public static String POST_KEY = "POST";
    public static String GET_KEY = "GET";
    public static String RES_CONTENT_TYPE_KEY = "content-type";
    public static String RES_CONTENT_LENGTH_KEY = "content-length";
    public static String RES_CONNECTION_KEY = "connection";
    public static String RES_DATE_KEY = "date";
    public static String RES_SERVER_KEY = "server";
    public static String CONNECTION_CLOSE_KEY = "close";
    public static String SERVER_NAME = "mini-boot";
    public static String JSON_UTF_8_TYPE = "application/json; charset=utf-8";
    public static String RESPONSE_200 = "OK";
    public static String RESPONSE_201 = "Created";
    public static String RESPONSE_400 = "Bad Request";
    public static String RESPONSE_401 = "Unauthorized";
    public static String RESPONSE_403 = "Forbidden";
    public static String RESPONSE_404 = "Not Found";
    public static String RESPONSE_405 = "Method Not Allowed";
    public static String RESPONSE_500 = "Internal Server Error";
    public static String HTTP_TYPE = "HTTP/1.1";

    //Utils
    public static String JSON_PRETTY_KEY = "JSON_PRETTY";
    public static String JSON_PRETTY_DEFAULT = "false";
    public static String LOG_LEVEL_KEY = "LOG_LEVEL";
    public static String LOG_LEVEL_DEFAULT = "INFO";

    //Math
    public static int MAX_INTEGER_VALUE = 10000000;
    // load tại startup
    public static void load() {
        Properties p = new Properties();

        // 1) file properties (tùy chọn)
        String path = System.getProperty("APP_PROPS", System.getenv().getOrDefault("APP_PROPS",""));
        if (!path.isBlank()) {
            try (FileInputStream fis = new FileInputStream(path)) {
                p.load(fis);
            } catch (Exception ignored) {}
        }

        // helper
        java.util.function.BiFunction<String,String,String> pick = (key, defVal) -> {
            String sys = System.getProperty(key);
            if (sys != null) return sys;
            String env = System.getenv(key);
            if (env != null) return env;
            String file = p.getProperty(key);
            return file != null ? file : defVal;
        };

        HTTP_PORT      = pick.apply("PORT", HTTP_PORT);
        MAX_BODY_BYTES = Integer.parseInt(pick.apply("MAX_BODY_BYTES", String.valueOf(MAX_BODY_BYTES)));
        WORKER_THREADS = Integer.parseInt(pick.apply("WORKER_THREADS", String.valueOf(WORKER_THREADS)));
    }


}
