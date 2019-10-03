package com.cyecize.summer.areas.scanning.models;

import com.cyecize.solet.HttpSoletResponseImpl;

import java.io.ByteArrayOutputStream;

public class HttpSummerResponse extends HttpSoletResponseImpl {
    public HttpSummerResponse() {
        super(new ByteArrayOutputStream());
    }
}
