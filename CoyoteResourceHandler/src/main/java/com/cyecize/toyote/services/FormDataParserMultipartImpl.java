package com.cyecize.toyote.services;

import com.cyecize.http.HttpRequest;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.toyote.MultipartConstants;
import com.cyecize.toyote.exceptions.CannotParseRequestException;
import com.cyecize.toyote.models.MultipartEntry;
import com.cyecize.toyote.models.MultipartFileImpl;
import com.cyecize.toyote.utils.MultipartParserListener;
import org.synchronoss.cloud.nio.multipart.Multipart;
import org.synchronoss.cloud.nio.multipart.MultipartContext;
import org.synchronoss.cloud.nio.multipart.NioMultipartParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class FormDataParserMultipartImpl implements FormDataParser {

    private final boolean showRequestLog;

    @Autowired
    public FormDataParserMultipartImpl(JavacheConfigService configService) {
        this.showRequestLog = configService.getConfigParam(JavacheConfigValue.SHOW_REQUEST_LOG, boolean.class);
    }

    @Override
    public void parseBodyParams(InputStream inputStream, HttpRequest request) throws CannotParseRequestException {
        try {
            this.parseMultipartBody(inputStream, request);
        } catch (IOException e) {
            throw new CannotParseRequestException(e.getMessage(), e);
        }
    }

    private void parseMultipartBody(InputStream inputStream, HttpRequest request) throws IOException {
        final int contentLength = request.getContentLength();

        final MultipartContext context = new MultipartContext(
                request.getContentType(),
                contentLength,
                StandardCharsets.UTF_8.name()
        );

        final NioMultipartParser parser = Multipart.multipart(context).forNIO(
                new MultipartParserListener(this::errorCallback, multipartEntry -> this.successCallback(multipartEntry, request))
        );

        byte[] buffer = new byte[0];
        int leftToRead = contentLength;
        int bytesRead = Math.min(2048, inputStream.available());

        while (leftToRead > 0) {
            buffer = inputStream.readNBytes(bytesRead);
            parser.write(buffer);
            leftToRead -= bytesRead;
            bytesRead = Math.min(2048, inputStream.available());
        }

        if (this.showRequestLog) {
            //TODO javache log service
            System.out.println(new String(buffer, StandardCharsets.UTF_8));
        }
    }

    private void errorCallback(Throwable cause) {
        throw new RuntimeException(cause);
    }

    private void successCallback(MultipartEntry multipartEntry, HttpRequest request) {
        try {
            final Map<String, String> contentDispositionData = multipartEntry.getContentDispositionData();

            if (multipartEntry.getContentType() == null) {
                final String fieldVal = new String(multipartEntry.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                final String fieldName = contentDispositionData.get(MultipartConstants.NIO_FIELD_PARAM_NAME);

                request.addBodyParameter(fieldName, fieldVal);
            } else {
                request.addMultipartFile(new MultipartFileImpl(
                        multipartEntry.getInputStream().available(),
                        multipartEntry.getContentType(),
                        contentDispositionData.get(MultipartConstants.NIO_FILE_PARAM_NAME),
                        contentDispositionData.get(MultipartConstants.NIO_FIELD_PARAM_NAME),
                        multipartEntry.getInputStream()
                ));
            }
        } catch (IOException ex) {
            throw new CannotParseRequestException(ex.getMessage(), ex);
        }
    }
}
