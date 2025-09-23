package org.miniboot.app;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import org.miniboot.app.controllers.HelloController;
import org.miniboot.app.http.HttpServer;
import org.miniboot.app.http.Router;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty(AppConfig.PORT_KEY, AppConfig.HTTP_PORT));
        Router router = new Router();
        HelloController.mount(router);

        HttpServer server = new HttpServer(port, router);
        server.start();
    }
}
