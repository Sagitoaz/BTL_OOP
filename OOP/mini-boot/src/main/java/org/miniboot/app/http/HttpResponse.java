package org.miniboot.app.http;

import org.miniboot.app.AppConfig;
import org.miniboot.app.util.Types;

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
    public byte[] body;
    public final Map<String, String> headers = new LinkedHashMap<>();

    public HttpResponse(int status, String contentType, byte[] body) {
        this.status = status;
        this.contentType = (contentType == null || contentType.isBlank()) ? AppConfig.TEXT_UTF_8_TYPE : contentType;
        this.body = body != null ? body : new byte[0];
        headers.put(AppConfig.RES_CONTENT_TYPE_KEY, this.contentType);
        headers.put(AppConfig.RES_CONTENT_LENGTH_KEY, String.valueOf(this.body.length));
        headers.put(AppConfig.RES_CONNECTION_KEY, AppConfig.CONNECTION_CLOSE_KEY);
        headers.put(AppConfig.RES_DATE_KEY, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()));
        headers.put(AppConfig.RES_SERVER_KEY, AppConfig.SERVER_NAME);
    }

    public static HttpResponse json(int status, String json) {
        return new HttpResponse(status, AppConfig.JSON_UTF_8_TYPE,
                json == null ? new byte[0] : json.getBytes(StandardCharsets.UTF_8));
    }

    public static HttpResponse of(int status, String contentType, byte[] body) {
        return new HttpResponse(status, contentType, body);
    }

    public static HttpResponse of(Types.Status status) {
        return new HttpResponse(status.code, AppConfig.TEXT_UTF_8_TYPE, new byte[0]);
    }

    public HttpResponse header(String key, String value) {
        if (key != null && value != null) {
            this.headers.put(key, value);
        }
        return this;
    }

    public HttpResponse body(byte[] body) {
        this.body = body != null ? body : new byte[0];
        this.headers.put(AppConfig.RES_CONTENT_LENGTH_KEY, String.valueOf(this.body.length));
        return this;
    }

    public HttpResponse body(String text) {
        return body(text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8));
    }

    public void writeTo(OutputStream out) throws IOException {
        HttpResponseEncoder.write(out, this);
    }
}
