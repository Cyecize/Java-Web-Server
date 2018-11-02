package com.cyecize.solet;

import com.cyecize.http.HttpResponse;

import java.io.OutputStream;

public interface HttpSoletResponse extends HttpResponse {
    OutputStream getOutputStream();
}
