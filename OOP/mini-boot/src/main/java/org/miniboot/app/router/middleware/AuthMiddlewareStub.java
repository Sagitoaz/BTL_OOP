package org.miniboot.app.router.middleware;

import org.miniboot.app.auth.AuthService;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Handler;
import org.miniboot.app.router.Middleware;

import java.nio.charset.StandardCharsets;

public class AuthMiddlewareStub implements Middleware {
    public Handler apply(Handler next) {

        // Kiem tra xem response co duoc author chua
        return req -> {

            // Kiem tra xem request nay co protected ko
            boolean prot = Boolean.parseBoolean(req.tags.getOrDefault("protected", "false"));

            if (!prot) {

                return next.handle(req);
            } else {

                // Kiem tra xem request nay da duocc author chua
                String auth = req.headers.get("authorization");
                if (auth == null || auth.isEmpty()) {
                    return new HttpResponse(
                            401,
                            "application/json",
                            "{\"error\":\"missing Authorization\"}".getBytes(StandardCharsets.UTF_8));
                } else {
                    // Validate token using AuthService
                    try {
                        AuthService.validateToken(auth);
                        return next.handle(req);
                    } catch (Exception e) {
                        return new HttpResponse(
                                401,
                                "application/json",
                                "{\"error\":\"invalid token\"}".getBytes(StandardCharsets.UTF_8));
                    }
                }
            }

        };
    }
}
