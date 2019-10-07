package com.cyecize.javache.core;

import com.cyecize.javache.services.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerImpl implements Server {

    private static final int SOCKET_TIMEOUT_MILLISECONDS = 60000;

    private static final String LISTENING_MESSAGE_FORMAT = "http://localhost:%d";

    private int port;

    private final LoggingService loggingService;

    private final RequestHandlerLoadingService requestHandlerLoadingService;

    private final JavacheConfigService javacheConfigService;

    public ServerImpl(int port, LoggingService loggingService, RequestHandlerLoadingService requestHandlerLoadingService, JavacheConfigService javacheConfigService) {
        this.port = port;
        this.loggingService = loggingService;
        this.requestHandlerLoadingService = requestHandlerLoadingService;
        this.javacheConfigService = javacheConfigService;
    }

    @Override
    public void run() throws IOException {
        ServerSocket serverSocket = new ServerSocket(this.port);
        serverSocket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
        this.loggingService.info(String.format(LISTENING_MESSAGE_FORMAT, this.port));

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
                clientSocket.setReceiveBufferSize(131072);

                Thread thread = new Thread(new ConnectionHandlerImpl(
                        clientSocket,
                        this.requestHandlerLoadingService.getRequestHandlers(),
                        new InputStreamCachingServiceImpl(),
                        this.loggingService,
                        this.javacheConfigService
                ));

                thread.start();
            } catch (SocketTimeoutException ignored) {
            }
        }
    }
}
