package org.miniboot.app.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.miniboot.app.AppConfig;
import org.miniboot.app.config.ErrorMessages;
import org.miniboot.app.config.HttpConstants;
import org.miniboot.app.http.HttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    @SuppressWarnings("unchecked")
    public static <T> List<T> fromBytesToList(byte[] body, Class<T> clazz) throws IOException {
        // Sử dụng TypeReference để đọc thành List<T>
        // Warning: This cast is necessary due to type erasure in Java generics
        return MAPPER.readValue(body, MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    public static <T> T fromString(String body, Class<T> clazz) throws IOException {
        return MAPPER.readValue(body, clazz);
    }

    public static HttpResponse ok(Object data) {
        return json(HttpConstants.STATUS_OK, data);
    }

    public static HttpResponse created(Object data) {
        return json(HttpConstants.STATUS_CREATED, data);
    }

    public static HttpResponse error(int status, String message) {
        return json(status, java.util.Map.of("error", message));
    }

    public static HttpResponse json(int status, Object data) {
        try {
            System.out.println("🔄 Serializing to JSON: " + (data != null ? data.getClass().getName() : "null"));
            byte[] body = MAPPER.writeValueAsBytes(data);
            System.out.println("✅ JSON serialized: " + body.length + " bytes");
            return HttpResponse.of(status, HttpConstants.CONTENT_TYPE_JSON_UTF8, body);
        } catch (JsonProcessingException e) {
            System.err.println("❌ JSON SERIALIZATION FAILED!");
            System.err.println("   Data type: " + (data != null ? data.getClass().getName() : "null"));
            System.err.println("   Error: " + e.getMessage());
            System.err.println("   Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "none"));
            e.printStackTrace();
            byte[] body = ("{\"error\":\"" + ErrorMessages.ERROR_JSON_PARSE + "\"}").getBytes();
            return HttpResponse.of(HttpConstants.STATUS_INTERNAL_SERVER_ERROR,
                    HttpConstants.CONTENT_TYPE_JSON_UTF8, body);
        }
    }

    public static String stringify(Object data) {
        if (data == null)
            return "null";
        if (data instanceof String s)
            return quote(s);
        if (data instanceof Number || data instanceof Boolean)
            return data.toString();

        if (data instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first)
                    sb.append(",");
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
            for (Object item : it) {
                if (!first)
                    sb.append(",");
                sb.append(stringify(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }

        // fallback
        return "\"" + data + "\"";
    }

    private static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseMap(String body) throws IOException {
        // Warning: Raw type usage is necessary here due to runtime type erasure
        return MAPPER.readValue(body, new TypeReference<Map<String, Object>>() {});
    }
}
