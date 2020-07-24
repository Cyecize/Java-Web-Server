package com.cyecize.toyote.lambda;

import com.cyecize.toyote.exceptions.CannotParseRequestException;

@FunctionalInterface
public interface MultipartParserErrorCallback {

    void onError(Throwable throwable) throws CannotParseRequestException;
}
