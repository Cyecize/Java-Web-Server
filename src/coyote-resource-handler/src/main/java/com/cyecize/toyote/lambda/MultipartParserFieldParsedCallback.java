package com.cyecize.toyote.lambda;

import com.cyecize.toyote.models.MultipartEntry;

@FunctionalInterface
public interface MultipartParserFieldParsedCallback {

    void onFieldParsed(MultipartEntry multipartEntry);
}
