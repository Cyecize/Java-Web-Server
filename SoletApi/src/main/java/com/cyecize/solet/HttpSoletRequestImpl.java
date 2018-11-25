package com.cyecize.solet;

import com.cyecize.http.HttpRequestImpl;
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

    private InputStream inputStream;

    private String contextPath;

    private Map<String, MemoryFile> uploadedFiles;

    public HttpSoletRequestImpl(String requestContent, InputStream requestStream) {
        super(requestContent);
        this.inputStream = requestStream;
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
        return this.inputStream;
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
            FileItemIterator parsedBodyParams = MultipartFileUpload.parse(this.readInputStream(this.getInputStream()), this.getHeaders().get(CONTENT_TYPE));

            while (parsedBodyParams.hasNext()) {
                FileItemStream bodyParam = parsedBodyParams.next();
                InputStream in = bodyParam.openStream();
                byte[] paramContent = this.readInputStream(in);
                boolean isParamNull = this.isMultipartFileNull(paramContent);

                if (bodyParam.isFormField()) {
                    if (!isParamNull) {
                        super.addBodyParameter(bodyParam.getFieldName(), new String(paramContent, StandardCharsets.UTF_8));
                    } else {
                        super.addBodyParameter(bodyParam.getFieldName(), null);
                    }
                } else {
                    MemoryFile memoryFile = null;
                    if (!isParamNull) {
                        memoryFile = new MultipartMemoryFile(bodyParam.getName(), bodyParam.getFieldName(), paramContent, bodyParam.getContentType());
                    }
                    this.uploadedFiles.put(bodyParam.getFieldName(), memoryFile);
                }

                in.close();
            }
        } catch (FileUploadException | IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readInputStream(InputStream inputStream) throws IOException {
        return inputStream.readAllBytes();
    }

    private boolean isMultipartFileNull(final byte[] fileBytes) {
        return fileBytes.length <= 0;
    }
}
