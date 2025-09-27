package org.miniboot.app;

import org.miniboot.app.controllers.HelloController;
import org.miniboot.app.http.HttpServer;
import org.miniboot.app.router.Router;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty(AppConfig.PORT_KEY, AppConfig.HTTP_PORT));
        Router router = new Router();
        HelloController.mount(router);

        HttpServer server = new HttpServer(port, router);
        server.start();
    }
}   
