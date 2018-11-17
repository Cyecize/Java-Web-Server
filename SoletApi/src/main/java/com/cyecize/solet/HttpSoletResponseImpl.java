package com.cyecize.solet;

import com.cyecize.http.HttpResponseImpl;
import com.cyecize.http.HttpStatus;

import java.io.OutputStream;

public class HttpSoletResponseImpl extends HttpResponseImpl implements HttpSoletResponse {

    private OutputStream outputStream;

    public HttpSoletResponseImpl(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void sendRedirect(String location) {
        super.setStatusCode(HttpStatus.SEE_OTHER);
        super.setContent(location);
        super.addHeader("Location", location);
    }

    @Override
    public OutputStream getOutputStream() {
        return this.outputStream;
    }
}
