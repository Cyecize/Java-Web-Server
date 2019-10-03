package com.cyecize.summer.areas.scanning.models;

import com.cyecize.solet.HttpSoletRequestImpl;


public class HttpSummerRequest extends HttpSoletRequestImpl {
    public HttpSummerRequest() {
        super(
                "GET /hello.htm HTTP/1.1\n" +
                        "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\n" +
                        "Host: www.tutorialspoint.com\n" +
                        "Accept-Language: en-us\n" +
                        "Accept-Encoding: gzip, deflate\n" +
                        "Connection: Keep-Alive",
                new byte[0],
                null
        );
    }
}
