package com.cyecize;

import com.cyecize.ioc.config.MagicConfiguration;
import com.cyecize.javache.api.JavacheComponent;

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

    public static final MagicConfiguration JAVACHE_IOC_CONFIGURATION = new MagicConfiguration()
            .scanning().addCustomServiceAnnotation(JavacheComponent.class)
            .and()
            .build();

    private WebConstants() {
        throw new RuntimeException();
    }
}
