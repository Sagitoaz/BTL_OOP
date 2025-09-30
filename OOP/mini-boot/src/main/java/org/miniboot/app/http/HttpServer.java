package org.miniboot.app.http;

import org.miniboot.app.AppConfig;
import org.miniboot.app.router.Router;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private static final int CLIENT_SO_TIMEOUT_MS = 10_000;

    private final int port;
    private final Router router;
    private volatile boolean running = false;
    private ServerSocket socket;
    private final ExecutorService pool =
            Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() * 2));

    public HttpServer(int port, Router router) {
        this.port = port;
        this.router = router;
    }

    public void start() throws IOException {
        socket = new ServerSocket();
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(port));
        running = true;
        System.out.println("[mini-boot] HTTP listening on: " + port);
        while (running) {
            Socket client = socket.accept();
            client.setSoTimeout(CLIENT_SO_TIMEOUT_MS);
            pool.execute(() -> handle(client));
        }
    }

    public void stop() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignore) {}
        pool.shutdownNow();
        System.out.println("[mini-boot] HTTP stopped.");
    }

    private void handle(Socket client) {
        try (client;
             InputStream in = client.getInputStream();
             OutputStream out = client.getOutputStream()) {

            HttpRequest req = HttpRequestParser.parse(in);
            HttpResponse res = router.dispatch(req);
            res.writeTo(out);

        } catch (HttpServer.BadRequest e) {
            writeJsonError(client, 400, AppConfig.RESPONSE_400);
        } catch (HttpServer.NotFound e) {
            writeJsonError(client, 404, AppConfig.RESPONSE_404);
        } catch (HttpServer.MethodNotAllowed e) {
            writeJsonError(client, 405, AppConfig.RESPONSE_405);
        } catch (Exception e) {
            writeJsonError(client, 500, AppConfig.RESPONSE_500);
        }
    }

    private void writeJsonError(Socket client, int status, String message) {
        try {
            OutputStream out = client.getOutputStream();
            String json = "{\"error\":{\"code\":" + status + ",\"message\":\"" + message + "\"}}";
            HttpResponse.json(status, json).writeTo(out);
        } catch (IOException ignore) {}
    }

    public static class BadRequest extends RuntimeException {}
    public static class NotFound extends RuntimeException {}
    public static class MethodNotAllowed extends RuntimeException {}
}
