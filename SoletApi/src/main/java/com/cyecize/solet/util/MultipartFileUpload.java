package com.cyecize.solet.util;

import delight.fileupload.MockHttpServletRequest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.xtext.xbase.lib.Exceptions;

import javax.servlet.http.HttpServletRequest;

public class MultipartFileUpload {
    public static FileItemIterator parse(byte[] data, String contentType) {
        try {
            ServletFileUpload upload = new ServletFileUpload();
            HttpServletRequest request = new MockedHttpServletRequest(data, contentType);
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (!isMultipart) {
                throw new Exception("Illegal request for uploading files. Multipart request expected.");
            } else {
                return upload.getItemIterator(request);
            }
        } catch (Throwable var6) {
            throw Exceptions.sneakyThrow(var6);
        }
    }
}
