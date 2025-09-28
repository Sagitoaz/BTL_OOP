package org.miniboot.app.controllers;

import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.Json;

import java.util.Map;

public class HelloController {
    public static void mount(Router router) {
        router.get("/hello", HelloController::hello);
        router.get("/health", HelloController::health);
    }

    private static HttpResponse hello(HttpRequest request) {
        return Json.ok(Map.of("message","ok"));
    }

    private static HttpResponse health(HttpRequest request) {
        return Json.ok(Map.of("status","ok", "uptimeMs", String.valueOf(System.currentTimeMillis())));
    }

    private static HttpResponse loginStub(HttpRequest req) {
        return Json.ok(Map.of(
            "access_token", "fake-token-123",
            "token_type", "Bearer"
        ));
    }

    private static HttpResponse doctorsStub(HttpRequest req) {
        return Json.ok(Map.of(
            "data", java.util.List.of(
                Map.of("id", 1, "name", "BS. Lan")
            )
        ));
    }
}
