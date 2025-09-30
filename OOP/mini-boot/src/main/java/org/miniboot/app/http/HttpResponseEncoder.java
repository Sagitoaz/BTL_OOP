package org.miniboot.app.http;

import org.miniboot.app.AppConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

public final class HttpResponseEncoder {
    private static final DateTimeFormatter RFC_1123 =
            DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT"));

    private static final Set<String> RESERVED = Set.of(
            "date","server","content-type","content-length","connection","transfer-encoding"
    );

    private HttpResponseEncoder() {}

    public static void write(OutputStream out, HttpResponse res) throws IOException {
        write(out, res.status, res.contentType, res.body == null ? new byte[0] : res.body, res.headers);
    }

    public static void write(OutputStream out, int status, String contentType, byte[] body, Map<String, String> headers) throws IOException {
        if (body == null) body = new byte[0];
        if (contentType == null || contentType.isBlank()) contentType = AppConfig.TEXT_UTF_8_TYPE;
        String reason = AppConfig.RESPONSE_REASON.getOrDefault(status, "OK");

        StringBuilder head = new StringBuilder(256);
        head.append(AppConfig.HTTP_TYPE).append(' ').append(status).append(' ').append(reason).append("\r\n");
        head.append(AppConfig.RES_DATE_KEY).append(": ").append(RFC_1123.format(Instant.now())).append("\r\n");
        head.append(AppConfig.RES_SERVER_KEY).append(": ").append(AppConfig.SERVER_NAME).append("\r\n");
        head.append(AppConfig.RES_CONTENT_TYPE_KEY).append(": ").append(contentType).append("\r\n");
        head.append(AppConfig.RES_CONTENT_LENGTH_KEY).append(": ").append(body.length).append("\r\n");
        head.append("Connection: close\r\n");

        if (headers != null) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                if (k == null || v == null) continue;
                if (RESERVED.contains(k.toLowerCase())) continue;
                head.append(k).append(": ").append(v).append("\r\n");
            }
        }
        head.append("\r\n");
        out.write(head.toString().getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }
}
