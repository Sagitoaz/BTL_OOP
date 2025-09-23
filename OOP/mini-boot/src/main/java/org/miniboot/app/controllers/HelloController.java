package org.miniboot.app.controllers;

import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.http.Router;
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
}
