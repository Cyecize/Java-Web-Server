package com.cyecize;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class WebConstants {

    private final static String START_UP_PACKAGE_PATH = StartUp.class.getName()
            .replace(StartUp.class.getSimpleName(), "")
            .replaceAll("\\.", "/");

    public static final int DEFAULT_SERVER_PORT = 8000;

    public static final String WORKING_DIRECTORY = URLDecoder.decode(StartUp.class.getResource("").toString()
            .replace("file:/", "")
            .replace(START_UP_PACKAGE_PATH, ""), StandardCharsets.UTF_8)
            + "../";

    private WebConstants() {
        throw new RuntimeException();
    }
}
