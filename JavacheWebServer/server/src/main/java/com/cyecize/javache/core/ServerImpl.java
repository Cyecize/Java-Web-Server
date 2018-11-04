package com.cyecize.javache.core;

import com.cyecize.http.*;
import com.cyecize.javache.services.InputStreamCachingServiceImpl;
import com.cyecize.javache.services.LoggingService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.FutureTask;

public class ServerImpl implements Server {

    private static final int SOCKET_TIMEOUT_MILLISECONDS = 5000;

    private static final String LISTENING_MESSAGE_FORMAT = "http://localhost:%d";

    private int port;

    private LoggingService loggingService;

    private ServerSocket serverSocket;

    public ServerImpl(int port, LoggingService loggingService) {
        this.port = port;
        this.loggingService = loggingService;
    }

    @Override
    public void run() throws IOException {
        this.serverSocket = new ServerSocket(this.port);
        this.loggingService.info(String.format(LISTENING_MESSAGE_FORMAT, this.port));

        this.serverSocket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);

        while (true) {
            try (Socket clientSocket = this.serverSocket.accept()) {
                clientSocket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);

                var cacheService = new InputStreamCachingServiceImpl();
                String content = cacheService.getOrCacheRequestContent(clientSocket.getInputStream());
                HttpRequest request = new HttpRequestImpl(content);
                //clientSocket.get

                HttpResponse response = new HttpResponseImpl();
                response.setStatusCode(HttpStatus.NOT_FOUND);
                response.addCookie(new HttpCookieImpl("lang", "bg"));
                response.addCookie(new HttpCookieImpl("ignat", "goran"));
                if (request.getRequestURL().equals("/"))
                    response.setContent("<h1>Hello</h1>".getBytes());
                else
                    response.setContent("<h1>How are u?</h1>".getBytes());

                var outputStream = clientSocket.getOutputStream();
                outputStream.write(response.getBytes());

                outputStream.close();

//                ConnectionHandler connectionHandler
//                        = new ConnectionHandler(clientSocket,
//                        this.requestHandlerLoadingService
//                                .getRequestHandlers(),
//                        new InputStreamCachingServiceImpl(),
//                        this.loggingService);
//
//                FutureTask<?> task = new FutureTask<>(connectionHandler, null);
//                task.run();
            } catch (SocketTimeoutException ignored) {
            }
        }
    }
}
