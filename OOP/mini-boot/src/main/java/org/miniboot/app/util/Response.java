package org.miniboot.app.util;

import org.miniboot.app.config.HttpConstants;
import org.miniboot.app.http.HttpResponse;

import java.nio.charset.StandardCharsets;

public class Response {
    public static HttpResponse text(String s) {
        return HttpResponse.of(Types.Status.OK.code)
                .header(HttpConstants.HEADER_CONTENT_TYPE, HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8)
                .body(s);
    }

    public static HttpResponse json(Object data) {
        String json = Json.stringify(data);
        return text(json);
    }

    public static HttpResponse created(String location, Object data) {
        byte[] body;
        if (data != null) {
            String json = Json.stringify(data);
            body = json.getBytes(StandardCharsets.UTF_8);
        } else {
            body = new byte[0];
        }
        HttpResponse response = HttpResponse.of(Types.Status.CREATED.code)
                .header(HttpConstants.HEADER_LOCATION, location);
        if (body.length > 0) {
            response.header(HttpConstants.HEADER_CONTENT_TYPE, HttpConstants.CONTENT_TYPE_JSON_UTF8)
                    .body(body);
        } else {
            response.header(HttpConstants.HEADER_CONTENT_LENGTH, "0")
                    .body(body);
        }
        return response;
    }
}
