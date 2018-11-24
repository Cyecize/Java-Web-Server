package com.cyecize.solet;

import com.cyecize.http.HttpRequestImpl;
import delight.fileupload.FileUpload;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        FileItemIterator parsedBodyParams = FileUpload.parse(requestContent.getBytes(StandardCharsets.UTF_8), this.getHeaders().get(CONTENT_TYPE));
        try {
            while (parsedBodyParams.hasNext()) {
                FileItemStream bodyParam = parsedBodyParams.next();

                InputStream in = bodyParam.openStream();
                byte[] paramContent = this.readInputStream(in);
                boolean isParamNull = this.isMultipartFileNull(paramContent, 4096);

                if (bodyParam.isFormField()) {
                    String field = null;
                    if (!isParamNull) {
                        field = new String(paramContent, StandardCharsets.UTF_8);
                    }
                    super.addBodyParameter(bodyParam.getFieldName(), field);
                } else {
                    MemoryFile memoryFile = null;
                    if (!isParamNull) {
                        memoryFile = new MultipartMemoryFile(bodyParam.getName(), bodyParam.getFieldName(), paramContent);
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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedInputStream in = new BufferedInputStream(inputStream);
        byte[] buffer = new byte[4096];
        while (true) {
            int remaining = in.read(buffer);
            outputStream.write(buffer);
            if (remaining <= 0) {
                break;
            }
        }
        return outputStream.toByteArray();
    }

    private boolean isMultipartFileNull(final byte[] fileBytes, int bufferSize) {
        if (fileBytes.length > bufferSize) {
            System.out.println("buffer is more than 4096, " + fileBytes.length);
            return false;
        }
        int hits = 0;
        for (byte b : fileBytes) {
            if (b != 0) {
                hits++;
            }
        }
        System.out.println("hits are " + hits);
        return (hits == 0);
    }
}
