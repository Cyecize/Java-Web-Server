package com.cyecize.toyote.utils;

import com.cyecize.toyote.exceptions.CannotParseExpressionException;

import java.util.HashMap;
import java.util.Map;

public final class CachingExpressingParser {

    public static Map<String, String> parseExpression(String expressionString) {

        final Map<String, String> mediaTypeCacheMap = new HashMap<>();
        final String[] expressions = expressionString.split("\\s*&\\s*");

        try {
            for (String expression : expressions) {
                final String[] tokens = expression.split("\\s*@\\s*");

                final String headerValue = tokens[1].trim();
                final String[] mediaTypes = tokens[0].split(",\\s*");

                for (String mediaType : mediaTypes) {
                    mediaTypeCacheMap.put(mediaType.trim(), headerValue);
                }
            }
        } catch (Exception ex) {
            throw new CannotParseExpressionException(
                    String.format("Cannot parse caching expression '%s', check the syntax.", expressionString),
                    ex
            );
        }

        return mediaTypeCacheMap;
    }
}
