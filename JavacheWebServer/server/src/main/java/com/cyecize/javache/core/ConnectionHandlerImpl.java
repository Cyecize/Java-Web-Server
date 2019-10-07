package com.cyecize.javache.core;

import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.api.RequestHandler;

import com.cyecize.javache.exceptions.RequestReadException;
import com.cyecize.javache.services.InputStreamCachingService;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.javache.services.LoggingService;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;

public class ConnectionHandlerImpl implements ConnectionHandler {

    private Socket clientSocket;

    private InputStream clientSocketInputStream;

    private OutputStream clientSocketOutputStream;

    private List<RequestHandler> requestHandlers;

    private InputStreamCachingService cachingService;

    private LoggingService loggingService;

    private JavacheConfigService configService;

    public ConnectionHandlerImpl(Socket clientSocket, List<RequestHandler> requestHandlers,
                                 InputStreamCachingService cachingService,
                                 LoggingService loggingService,
                                 JavacheConfigService configService) {

        this.initializeConnection(clientSocket);
        this.requestHandlers = requestHandlers;
        this.cachingService = cachingService;
        this.loggingService = loggingService;
        this.configService = configService;
    }

    @Override
    public void run() {
        try {
            this.processClientConnection();
            this.clientSocketInputStream.close();
            this.clientSocketOutputStream.flush();
            this.clientSocketOutputStream.close();
            this.clientSocket.close();
            this.cachingService.evictCache();
        } catch (Throwable e) {
            if (e instanceof SocketTimeoutException) {
                this.loggingService.error(e.getMessage());
                return;
            }

            if (e instanceof RequestReadException) {
                try {
                    this.handleRequestReadException((RequestReadException) e);
                    return;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            this.loggingService.error(e.getMessage());
            e.printStackTrace();
        } finally {
            this.clientSocketInputStream = null;
            this.clientSocketOutputStream = null;
            this.clientSocket = null;
            this.cachingService = null;
        }
    }

    /**
     * Reads and caches the input stream and in the request content is too large it throws exception.
     * Iterates through all request handles and executes them until one intercepts.
     * Request handlers are kept in order.
     */
    private void processClientConnection() throws IOException {
        try {
            this.cachingService.getOrCacheInputStream(this.clientSocketInputStream, this.configService.getConfigParam(JavacheConfigValue.MAX_REQUEST_SIZE, Integer.class));
        } catch (RequestReadException e) {
            this.handleRequestReadException(e);
            return;
        }

        for (RequestHandler requestHandler : this.requestHandlers) {
            requestHandler.handleRequest(
                    this.cachingService.getOrCacheInputStream(this.clientSocketInputStream),
                    this.clientSocketOutputStream
            );

            if (requestHandler.hasIntercepted()) {
                break;
            }
        }
    }

    private void handleRequestReadException(RequestReadException e) throws IOException {
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(this.clientSocketOutputStream));
        this.clientSocketInputStream.close();
        printWriter.println("HTTP/1.1 400 Bad Request");
        printWriter.println("Content-Type: text/html");
        printWriter.println();
        printWriter.println(String.format("<h4>%s</h4>", e.getMessage()));
    }

    private void initializeConnection(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.clientSocketInputStream = this.clientSocket.getInputStream();
            this.clientSocketOutputStream = this.clientSocket.getOutputStream();
        } catch (IOException e) {
            loggingService.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
