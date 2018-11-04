package com.cyecize;

public class WebConstants {

    private final static String START_UP_PACKAGE_PATH = StartUp.class.getName()
            .replace(StartUp.class.getSimpleName(), "")
            .replaceAll("\\.", "/");

    public static final int DEFAULT_SERVER_PORT = 8000;

    public static final String WORKING_DIRECTORY = StartUp.class.getResource("").toString()
            .replace("file:/", "")
            .replace(START_UP_PACKAGE_PATH, "");

    private WebConstants() {

    }
}
