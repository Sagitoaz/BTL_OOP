package org.miniboot.app.util;

import java.io.IOException;
import java.util.Map;

import org.miniboot.app.AppConfig;
import org.miniboot.app.http.HttpResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Json {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // Thêm hỗ trợ Java 8 Date/Time (LocalDateTime, LocalDate, etc.)
        MAPPER.registerModule(new JavaTimeModule());
        
        // Disable writing dates as timestamps (dùng ISO-8601 format)
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // cho phép in đẹp khi debug: bật/tắt qua env
        boolean pretty = Boolean.parseBoolean(System.getProperty(AppConfig.JSON_PRETTY_KEY,
                System.getenv().getOrDefault(AppConfig.JSON_PRETTY_KEY, AppConfig.JSON_PRETTY_DEFAULT)));
        MAPPER.configure(SerializationFeature.INDENT_OUTPUT, pretty);
    }

    // parse JSON từ byte[]
    public static <T> T fromBytes(byte[] body, Class<T> clazz) throws IOException {
        return MAPPER.readValue(body, clazz);
    }

    public static <T> T fromString(String body, Class<T> clazz) throws IOException {
        return MAPPER.readValue(body, clazz);
    }

    public static HttpResponse ok(Object data) {
        return json(200, data);
    }

    public static HttpResponse created(Object data) {
        return json(201, data);
    }
    public static HttpResponse error(int status, String message) {
        return json(status, java.util.Map.of("error", message));
    }

    @SuppressWarnings("unchecked")
    public static HttpResponse json(int status, Object data) {
        try {
            byte[] body = MAPPER.writeValueAsBytes(data);
            return HttpResponse.of(status, AppConfig.JSON_UTF_8_TYPE, body);
        } catch (JsonProcessingException e) {
            byte[] body = ("{\"error\":\"json-serialize-failed\"}").getBytes();
            return HttpResponse.of(500, AppConfig.JSON_UTF_8_TYPE, body);
        }
    }

    public static String stringify(Object data) {
        if (data == null) return "null";
        if (data instanceof String s) return quote(s);
        if (data instanceof Number || data instanceof Boolean) return data.toString();

        if (data instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append(quote(String.valueOf(entry.getKey())))
                        .append(":")
                        .append(stringify(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }

        if (data instanceof Iterable<?> it) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            for (Object o : it) {
                if (!first) sb.append(",");
                sb.append(stringify(o));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        return quote(data.toString());
    }

    private static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    public static Map<String, Object> parseMap(String body) throws IOException {
        return fromString(body, Map.class);
    }
}
