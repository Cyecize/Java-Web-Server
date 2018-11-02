package com.cyecize.solet;

import com.cyecize.http.HttpRequest;

import java.io.InputStream;

public interface HttpSoletRequest extends HttpRequest {
    InputStream getInputStream();
}
