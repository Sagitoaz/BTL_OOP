package org.miniboot.app.util;

import org.miniboot.app.router.Router;
import org.miniboot.app.controllers.HelloController;
import org.miniboot.app.router.middleware.AuthMiddlewareStub;
import org.miniboot.app.router.middleware.CorsMiddleware;
import org.miniboot.app.router.middleware.LoggingMiddleware;
import org.miniboot.app.util.HttpStub;

public class UtilSmokeMain {
    public static void main(String[] args) throws Exception {
        // lắp router + controller
        Router router = new Router();
        router.use(new LoggingMiddleware());
        router.use(new CorsMiddleware());
        router.use(new AuthMiddlewareStub());
        HelloController.mount(router);

        // dùng HttpStub gọi như client
        HttpStub stub = new HttpStub(router);

        var h = stub.get("/health");
        System.out.println("GET /health -> " + h.status() );



        var hello = stub.get("/hello");

    }
}
