package com.cyecize.javache.embedded;

import com.cyecize.StartUp;
import com.cyecize.WebConstants;

import java.util.HashMap;
import java.util.Map;

public class JavacheEmbedded {

    public static void startServer(int port) {
        startServer(port, new HashMap<>());
    }

    public static void startServer(int port, Map<String, Object> config) {

        System.out.println(WebConstants.WORKING_DIRECTORY);
    }

    public static void main(String[] args) throws Exception {
        StartUp.main(new String[] { 8000 + ""});
    }
}
