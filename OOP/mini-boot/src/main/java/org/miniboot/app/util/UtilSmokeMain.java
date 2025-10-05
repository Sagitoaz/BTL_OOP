package org.miniboot.app.util;

import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.controllers.HelloController;
import org.miniboot.app.util.HttpStub;

import java.nio.charset.StandardCharsets;

public class UtilSmokeMain {
    public static void main(String[] args) {
        // lắp router + controller
        Router router = new Router();
        HelloController.mount(router);

        // dùng HttpStub gọi như client
        HttpStub stub = new HttpStub(router);

        var h = stub.get("/health");
        System.out.println("GET /health -> " + h.status() + " " + h.contentType());
        System.out.println(h.body());

        var hello = stub.get("/hello");
        System.out.println("GET /hello -> " + hello.status() + " " + hello.contentType());
        System.out.println(hello.body());

        String jsonBody = "{\"name\":\"book\"}";
        var tmp = stub.post("/items", jsonBody.getBytes(StandardCharsets.UTF_8));
        System.out.println("POST /items -> " + tmp.status() + " " + tmp.contentType());
        System.out.println(tmp.body());
    }
}
