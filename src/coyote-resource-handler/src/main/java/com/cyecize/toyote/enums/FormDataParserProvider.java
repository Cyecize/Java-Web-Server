package com.cyecize.toyote.enums;

import com.cyecize.toyote.services.FormDataParser;
import com.cyecize.toyote.services.FormDataParserDefaultImpl;
import com.cyecize.toyote.services.FormDataParserMultipartImpl;

import java.util.Arrays;

import static com.cyecize.toyote.ToyoteConstants.MULTIPART_FORM_DATA;
import static com.cyecize.toyote.ToyoteConstants.TEXT_PLAIN;

public enum FormDataParserProvider {
    DEFAULT(TEXT_PLAIN, FormDataParserDefaultImpl.class),
    MULTIPART(MULTIPART_FORM_DATA, FormDataParserMultipartImpl.class);

    private final String contentType;
    private final Class<? extends FormDataParser> parserType;

    FormDataParserProvider(String contentType, Class<? extends FormDataParser> parserType) {
        this.contentType = contentType;
        this.parserType = parserType;
    }

    public Class<? extends FormDataParser> getParserType() {
        return this.parserType;
    }

    public static FormDataParserProvider findByContentType(String contentType) {
        if (contentType == null) {
            return DEFAULT;
        }

        return Arrays.stream(values())
                .filter(provider -> contentType.startsWith(provider.contentType))
                .findFirst().orElse(DEFAULT);
    }

}
