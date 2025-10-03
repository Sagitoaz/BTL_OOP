// EchoController.java
package org.miniboot.app.controllers;

import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class EchoController {
    public static void mount(Router router) {
        router.post("/echo", echo());
    }

    private static Function<HttpRequest, HttpResponse> echo() {
        return (HttpRequest req) -> {
            byte[] body = req.body == null ? new byte[0] : req.body;
            // trả lại đúng body đã nhận
            //return HttpResponse.okJson(new String(body, StandardCharsets.UTF_8));
            // nếu bạn chưa có okJson, tạm dùng:
            return new HttpResponse(200, "application/json; charset=utf-8", body);
        };
    }
}
