package org.miniboot.app.http;

import org.miniboot.app.AppConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {
    public final int status;
    public final String contentType;
    public final byte[] body;
    public final Map<String, String> headers = new LinkedHashMap<>();

    public HttpResponse(int status, String contentType, byte[] body) {
        this.status = status;
        this.contentType = contentType;
        this.body = body != null ? body : new byte[0];
        headers.put(AppConfig.RES_CONTENT_TYPE_KEY, contentType);
        headers.put(AppConfig.RES_CONTENT_LENGTH_KEY, String.valueOf(this.body.length));
        headers.put(AppConfig.RES_CONNECTION_KEY, AppConfig.CONNECTION_CLOSE_KEY);
        headers.put(AppConfig.RES_DATE_KEY, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()));
        headers.put(AppConfig.RES_SERVER_KEY, AppConfig.SERVER_NAME);
    }

    public static HttpResponse json(int status, String json) {
        return new HttpResponse(status, AppConfig.JSON_UTF_8_TYPE, json.getBytes(StandardCharsets.UTF_8));
    }

    public static HttpResponse of(int status, String contentType, byte[] body) {
        return new HttpResponse(status, contentType, body);
    }

    public void writeTo(OutputStream out) throws IOException {
        String reason = switch (status) {
            case 200 -> AppConfig.RESPONSE_200;
            case 201 -> AppConfig.RESPONSE_201;
            case 400 -> AppConfig.RESPONSE_400;
            case 401 -> AppConfig.RESPONSE_401;
            case 403 -> AppConfig.RESPONSE_403;
            case 404 -> AppConfig.RESPONSE_404;
            case 405 -> AppConfig.RESPONSE_405;
            default -> AppConfig.RESPONSE_500;
        };
        StringBuilder sb = new StringBuilder();
        sb.append(AppConfig.HTTP_TYPE + " ").append(status + " ").append(reason + "\r\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }
        sb.append("\r\n");
        out.write(sb.toString().getBytes(StandardCharsets.US_ASCII));
        out.write(body);
        out.flush();
    }
}
