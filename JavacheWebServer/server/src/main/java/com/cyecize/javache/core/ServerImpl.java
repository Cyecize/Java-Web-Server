package com.cyecize.javache.core;

import com.cyecize.javache.services.InputStreamCachingServiceImpl;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.javache.services.LoggingService;
import com.cyecize.javache.services.RequestHandlerLoadingService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerImpl implements Server {

    private static final int SOCKET_TIMEOUT_MILLISECONDS = 5000;

    private static final String LISTENING_MESSAGE_FORMAT = "http://localhost:%d";

    private int port;

    private LoggingService loggingService;

    private RequestHandlerLoadingService requestHandlerLoadingService;

    private JavacheConfigService javacheConfigService;

    public ServerImpl(int port, LoggingService loggingService, RequestHandlerLoadingService requestHandlerLoadingService, JavacheConfigService javacheConfigService) {
        this.port = port;
        this.loggingService = loggingService;
        this.requestHandlerLoadingService = requestHandlerLoadingService;
        this.javacheConfigService = javacheConfigService;
        this.initRequestHandlers();
    }

    @Override
    public void run() throws IOException {
        ServerSocket serverSocket = new ServerSocket(this.port);
        serverSocket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
        this.loggingService.info(String.format(LISTENING_MESSAGE_FORMAT, this.port));

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                Thread thread = new Thread(new ConnectionHandlerImpl(
                        clientSocket,
                        this.requestHandlerLoadingService.getRequestHandlers(),
                        new InputStreamCachingServiceImpl(), this.loggingService)
                );
                thread.start();
            } catch (SocketTimeoutException ignored) {
            }
        }
    }

    private void initRequestHandlers() {
        try {
            this.requestHandlerLoadingService.loadRequestHandlers(this.javacheConfigService.getRequestHandlerPriority());
        } catch (Exception e) {
            this.loggingService.error(e.getMessage());
            this.loggingService.printStackTrace(e.getStackTrace());
        }
    }
}
