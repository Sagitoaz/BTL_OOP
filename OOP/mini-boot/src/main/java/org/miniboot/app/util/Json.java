package org.miniboot.app.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.AppConfig;

public class Json {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        // cho phép in đẹp khi debug: bật/tắt qua env
        boolean pretty = Boolean.parseBoolean(System.getProperty("JSON_PRETTY",
                System.getenv().getOrDefault("JSON_PRETTY","false")));
        MAPPER.configure(SerializationFeature.INDENT_OUTPUT, pretty);
    }

    public static HttpResponse ok(Object data)     { return json(200, data); }
    public static HttpResponse created(Object data){ return json(201, data); }

    public static HttpResponse json(int status, Object data) {
        try {
            byte[] body = MAPPER.writeValueAsBytes(data);
            return HttpResponse.of(status, AppConfig.JSON_UTF_8_TYPE, body);
        } catch (JsonProcessingException e) {
            byte[] body = ("{\"error\":\"json-serialize-failed\"}").getBytes();
            return HttpResponse.of(500, AppConfig.JSON_UTF_8_TYPE, body);
        }
    }
}
