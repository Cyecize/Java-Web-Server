package com.cyecize.javache.core;

import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.api.RequestHandlerSharedData;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ConnectionHandlerImpl implements ConnectionHandler {

    private final Socket clientSocket;

    private final List<RequestHandler> requestHandlers;

    public ConnectionHandlerImpl(Socket clientSocket, List<RequestHandler> requestHandlers) {
        this.clientSocket = clientSocket;
        this.requestHandlers = requestHandlers;
    }

    @Override
    public void run() {
        try {
            this.processClientConnection();
            this.clientSocket.getInputStream().close();
            this.clientSocket.getOutputStream().flush();
            this.clientSocket.getOutputStream().close();
            this.clientSocket.close();
        } catch (Throwable e) {
            //TODO: log
            e.printStackTrace();
        }
    }

    /**
     * Iterates through all request handles and executes them until one intercepts.
     * Request handlers are kept in order.
     */
    private void processClientConnection() throws IOException {
        final RequestHandlerSharedData sharedData = new RequestHandlerSharedData();

        for (RequestHandler requestHandler : this.requestHandlers) {
            if (requestHandler.handleRequest(this.clientSocket.getInputStream(), this.clientSocket.getOutputStream(), sharedData)) {
                break;
            }
        }
    }
}
