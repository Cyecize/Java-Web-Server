package com.cyecize.toyote.services;

import com.cyecize.http.HttpRequest;
import com.cyecize.toyote.exceptions.CannotParseRequestException;

import java.io.InputStream;

public interface FormDataParser {

    void parseBodyParams(InputStream inputStream, HttpRequest request) throws CannotParseRequestException;
}
