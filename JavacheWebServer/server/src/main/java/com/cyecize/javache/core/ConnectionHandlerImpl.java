package com.cyecize.javache.core;

import com.cyecize.javache.api.RequestDestroyHandler;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.api.RequestHandlerSharedData;
import com.cyecize.javache.services.LoggingService;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ConnectionHandlerImpl implements ConnectionHandler {

    private final Socket clientSocket;

    private final List<RequestHandler> requestHandlers;

    private final List<RequestDestroyHandler> requestDestroyHandlers;

    private final LoggingService loggingService;

    public ConnectionHandlerImpl(Socket clientSocket, List<RequestHandler> requestHandlers,
                                 List<RequestDestroyHandler> requestDestroyHandlers,
                                 LoggingService loggingService) {
        this.clientSocket = clientSocket;
        this.requestHandlers = requestHandlers;
        this.requestDestroyHandlers = requestDestroyHandlers;
        this.loggingService = loggingService;
    }

    @Override
    public void run() {
        try {
            this.processClientConnection();
            this.clientSocket.close();
        } catch (Throwable e) {
            this.loggingService.printStackTrace(e);
        }
    }

    /**
     * Iterates through all request handles and executes them until one intercepts.
     * Request handlers are kept in order.
     */
    private void processClientConnection() throws IOException {
        final RequestHandlerSharedData sharedData = new RequestHandlerSharedData();

        for (RequestHandler requestHandler : this.requestHandlers) {
            boolean requestHandled = requestHandler.handleRequest(
                    this.clientSocket.getInputStream(),
                    this.clientSocket.getOutputStream(),
                    sharedData
            );

            if (requestHandled) {
                break;
            }
        }

        for (RequestDestroyHandler requestDestroyHandler : this.requestDestroyHandlers) {
            requestDestroyHandler.destroy(sharedData);
        }
    }
}
