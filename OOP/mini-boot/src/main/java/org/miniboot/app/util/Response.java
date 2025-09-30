package org.miniboot.app.util;

import org.miniboot.app.AppConfig;
import org.miniboot.app.http.HttpResponse;

import java.nio.charset.StandardCharsets;

public class Response {
    public static HttpResponse text(String s) {
        return HttpResponse.of(Types.Status.OK)
                .header(AppConfig.RES_CONTENT_TYPE_KEY, AppConfig.TEXT_UTF_8_TYPE)
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
        HttpResponse response = HttpResponse.of(Types.Status.CREATED).header(AppConfig.LOCATION_KEY, location);
        if (body.length > 0) {
            response.header(AppConfig.RES_CONTENT_TYPE_KEY, AppConfig.JSON_UTF_8_TYPE).body(body);
        } else {
            response.header(AppConfig.RES_CONTENT_TYPE_KEY, "0").body(body);
        }
        return response;
    }
}
