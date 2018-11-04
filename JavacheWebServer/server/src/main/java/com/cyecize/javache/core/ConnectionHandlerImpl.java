package com.cyecize.javache.core;

import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.services.InputStreamCachingService;
import com.cyecize.javache.services.LoggingService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;

public class ConnectionHandlerImpl implements ConnectionHandler {

    private Socket clientSocket;

    private InputStream clientSocketInputStream;

    private OutputStream clientSocketOutputStream;

    private Set<RequestHandler> requestHandlers;

    private InputStreamCachingService cachingService;

    private LoggingService loggingService;

    public ConnectionHandlerImpl(Socket clientSocket, Set<RequestHandler> requestHandlers, InputStreamCachingService cachingService, LoggingService loggingService) {
        this.initializeConnection(clientSocket);
        this.requestHandlers = requestHandlers;
        this.cachingService = cachingService;
        this.loggingService = loggingService;
    }

    @Override
    public void run() {
        try {
            this.processClientConnection();
            this.clientSocketInputStream.close();
            this.clientSocketOutputStream.close();
            this.clientSocket.close();
            this.cachingService.evictCache();
        } catch (IOException e) {
            this.loggingService.error(e.getMessage());
        }
    }

    private void processClientConnection() throws IOException {
        for (RequestHandler requestHandler : this.requestHandlers) {
            requestHandler.handleRequest(this.cachingService.getOrCacheRequestContent(this.clientSocketInputStream), this.clientSocketOutputStream);
            if (requestHandler.hasIntercepted()) {
                break;
            }
        }
    }

    private void initializeConnection(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.clientSocketInputStream = this.clientSocket.getInputStream();
            this.clientSocketOutputStream = this.clientSocket.getOutputStream();
        } catch (IOException e) {
            loggingService.error(e.getMessage());
            loggingService.printStackTrace(e.getStackTrace());
        }
    }
}
