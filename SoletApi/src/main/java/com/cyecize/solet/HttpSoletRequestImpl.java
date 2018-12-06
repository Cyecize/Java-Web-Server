package com.cyecize.solet;

import com.cyecize.http.HttpRequestImpl;
import com.cyecize.solet.service.TemporaryStorageService;
import com.cyecize.solet.util.MultipartFileUpload;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpSoletRequestImpl extends HttpRequestImpl implements HttpSoletRequest {

    private static final String CONTENT_TYPE = "Content-Type";

    private byte[] bytes;

    private String contextPath;

    private final TemporaryStorageService temporaryStorageService;

    private Map<String, MemoryFile> uploadedFiles;

    public HttpSoletRequestImpl(String requestContent, byte[] bytes, TemporaryStorageService temporaryStorageService) {
        super(requestContent);
        this.bytes = bytes;
        this.temporaryStorageService = temporaryStorageService;
        this.uploadedFiles = new HashMap<>();
        this.setContextPath("");
        this.initMultipartRequest(requestContent);
    }

    @Override
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    @Override
    public String getRelativeRequestURL() {
        return super.getRequestURL().replaceFirst(this.contextPath, "");
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(this.bytes);
    }

    @Override
    public Map<String, MemoryFile> getUploadedFiles() {
        return this.uploadedFiles;
    }

    private void initMultipartRequest(String requestContent) {
        if (!this.getMethod().equalsIgnoreCase("POST") || !this.getHeaders().containsKey(CONTENT_TYPE) || !this.getHeaders().get(CONTENT_TYPE).startsWith("multipart/")) {
            return;
        }

        try {
            FileItemIterator parsedBodyParams = MultipartFileUpload.parse(this.bytes, this.getHeaders().get(CONTENT_TYPE));

            while (parsedBodyParams.hasNext()) {
                FileItemStream bodyParam = parsedBodyParams.next();

                if (bodyParam.isFormField()) {
                    InputStream in = bodyParam.openStream();
                    byte[] paramContent = in.readAllBytes();

                    if (paramContent.length > 0) {
                        super.addBodyParameter(bodyParam.getFieldName(), new String(paramContent, StandardCharsets.UTF_8));
                    } else {
                        super.addBodyParameter(bodyParam.getFieldName(), null);
                    }
                    in.close();
                } else {
                    this.uploadedFiles.put(bodyParam.getFieldName(), this.temporaryStorageService.saveMultipartFile(bodyParam));
                }
            }
        } catch (FileUploadException | IOException e) {
            e.printStackTrace();
        }
    }
}
