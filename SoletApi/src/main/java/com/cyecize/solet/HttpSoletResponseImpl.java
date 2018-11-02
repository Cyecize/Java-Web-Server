package com.cyecize.solet;

import com.cyecize.http.HttpResponseImpl;

import java.io.OutputStream;

public class HttpSoletResponseImpl extends HttpResponseImpl implements HttpSoletResponse {

    private OutputStream outputStream;

    public HttpSoletResponseImpl(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return this.outputStream;
    }
}
