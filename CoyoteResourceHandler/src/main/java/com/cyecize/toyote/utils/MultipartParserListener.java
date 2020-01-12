package com.cyecize.toyote.utils;

import com.cyecize.toyote.MultipartConstants;
import com.cyecize.toyote.lambda.MultipartParserErrorCallback;
import com.cyecize.toyote.lambda.MultipartParserFieldParsedCallback;
import com.cyecize.toyote.models.MultipartEntry;
import org.synchronoss.cloud.nio.multipart.NioMultipartParserListener;
import org.synchronoss.cloud.nio.stream.storage.StreamStorage;

import java.util.List;
import java.util.Map;

public class MultipartParserListener implements NioMultipartParserListener {

    private final MultipartParserErrorCallback errorCallback;

    private final MultipartParserFieldParsedCallback fieldParsedCallback;

    public MultipartParserListener(MultipartParserErrorCallback errorCallback,
                                   MultipartParserFieldParsedCallback fieldParsedCallback) {
        this.errorCallback = errorCallback;
        this.fieldParsedCallback = fieldParsedCallback;
    }

    @Override
    public void onPartFinished(StreamStorage partBodyStreamStorage, Map<String, List<String>> headersFromPart) {
        String contentType = null;
        if (headersFromPart.containsKey(MultipartConstants.NIO_CONTENT_TYPE_PARAM_NAME)) {
            contentType = headersFromPart.get(MultipartConstants.NIO_CONTENT_TYPE_PARAM_NAME).get(0);
        }

        final MultipartEntry multipartMetadata = new MultipartEntry(
                MultipartUtils.parseContentDispositionString(
                        headersFromPart.get(MultipartConstants.NIO_CONTENT_DISPOSITION_PARAM_NAME).get(0)
                ),
                contentType,
                partBodyStreamStorage.getInputStream()
        );

        this.fieldParsedCallback.onFieldParsed(multipartMetadata);
    }

    @Override
    public void onAllPartsFinished() {

    }

    @Override
    public void onNestedPartStarted(Map<String, List<String>> headersFromParentPart) {

    }

    @Override
    public void onNestedPartFinished() {

    }

    @Override
    public void onError(String message, Throwable cause) {
        this.errorCallback.onError(cause);
    }
}
