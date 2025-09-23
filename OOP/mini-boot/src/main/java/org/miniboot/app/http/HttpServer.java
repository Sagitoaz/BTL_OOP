package org.miniboot.app.http;

import org.miniboot.app.AppConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private final int port;
    private final Router router;
    private volatile boolean running = false;
    private ServerSocket socket;
    private ExecutorService pool = Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() * 2));

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
            client.setSoTimeout(10000);
            pool.submit(() -> handle(client));
        }
    }

    private void handle(Socket client) {
        try (client; InputStream in = client.getInputStream(); OutputStream out = client.getOutputStream()) {
            HttpRequest request = HttpRequest.parse(in, 1000000);
            HttpResponse response = router.dispatch(request);
            response.writeTo(out);
        } catch (BadRequest e) {
            writeError(client, 400, AppConfig.RESPONSE_400);
        } catch (NotFound e) {
            writeError(client, 404, AppConfig.RESPONSE_404);
        } catch (MethodNotAllowed e) {
            writeError(client, 405, AppConfig.RESPONSE_405);
        } catch (Throwable t) {
            writeError(client, 500, AppConfig.RESPONSE_500);
        }
    }

    private void writeError(Socket client, int status, String message) {
        try (OutputStream out = client.getOutputStream()) {
            HttpResponse.of(status, "application/json",
                            ("{\"error\":{\"code\":" + status + ",\"message\":\"" + message + "\"}}").getBytes())
                    .writeTo(out);
        } catch (IOException ignored) {}
    }

    public static class BadRequest extends RuntimeException {}
    public static class NotFound extends RuntimeException {}
    public static class MethodNotAllowed extends RuntimeException {}
}
