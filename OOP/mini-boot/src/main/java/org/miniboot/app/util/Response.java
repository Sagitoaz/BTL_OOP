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


}
