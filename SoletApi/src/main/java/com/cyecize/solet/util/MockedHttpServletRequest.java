package com.cyecize.solet.util;

import delight.fileupload.MockHttpServletRequest;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MockedHttpServletRequest extends MockHttpServletRequest {

    public MockedHttpServletRequest(byte[] requestData, String strContentType) {
        super(requestData, strContentType);
    }

    public MockedHttpServletRequest(InputStream requestData, long requestLength, String strContentType) {
        super(requestData, requestLength, strContentType);
    }

    @Override
    public String getCharacterEncoding() {
        return StandardCharsets.UTF_8.name();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return super.getInputStream();
    }
}
