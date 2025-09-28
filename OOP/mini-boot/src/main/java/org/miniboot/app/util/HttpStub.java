package org.miniboot.app.util;

import org.miniboot.app.AppConfig;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpStub {
    private final Router router;

    public HttpStub(Router router) { this.router = router; }

    public Result get(String path)  { return call(AppConfig.GET_KEY, path, new byte[0]); }
    public Result post(String path, byte[] body) { return call(AppConfig.POST_KEY, path, body); }

    public Result call(String method, String path, byte[] body) {
        Map<String,String> headers = new LinkedHashMap<>();
        headers.put("host","localhost");
        headers.put(AppConfig.RES_CONTENT_LENGTH_KEY, String.valueOf(body.length));
        HttpRequest req = HttpRequest.of(method, path, AppConfig.HTTP_TYPE, headers, body);
        HttpResponse res = router.dispatch(req);
        return new Result(res.status, new String(res.body, StandardCharsets.UTF_8), res.contentType);
    }

    public record Result(int status, String body, String contentType) {}
}
