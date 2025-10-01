package org.miniboot.app;

import org.miniboot.app.controllers.HelloController;
import org.miniboot.app.http.HttpServer;
import org.miniboot.app.router.PathPattern;
import org.miniboot.app.router.Router;
import org.miniboot.app.router.middleware.AuthMiddlewareStub;
import org.miniboot.app.router.middleware.CorsMiddleware;
import org.miniboot.app.router.middleware.LoggingMiddleware;

import java.util.Map;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty(AppConfig.PORT_KEY, AppConfig.HTTP_PORT));
        Router router = new Router();

        // mount các controller
        HelloController.mount(router);

        HttpServer server = new HttpServer(port, router);
        server.start();
    }
}
