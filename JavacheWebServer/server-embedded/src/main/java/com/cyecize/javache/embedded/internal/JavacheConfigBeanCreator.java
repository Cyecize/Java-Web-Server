package com.cyecize.javache.embedded.internal;

import com.cyecize.broccolina.services.JarFileUnzipService;
import com.cyecize.broccolina.services.JarFileUnzipServiceImpl;
import com.cyecize.ioc.annotations.Bean;
import com.cyecize.ioc.annotations.PostConstruct;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.embedded.services.EmbeddedJavacheConfigService;
import com.cyecize.javache.services.JavacheConfigService;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@JavacheEmbeddedComponent
public class JavacheConfigBeanCreator {

    public static int port;

    public static Map<String, Object> config;

    public static Class<?> mainClass;

    public JavacheConfigBeanCreator() {

    }

    @PostConstruct
    public void init() {
        this.initConfig();
    }

    @Bean
    public JavacheConfigService configService() throws IOException {
        final JavacheConfigService configService = new EmbeddedJavacheConfigService(config);

        configService.addConfigParam(JavacheConfigValue.SERVER_PORT, port);
        configService.addConfigParam(JavacheConfigValue.JAVACHE_WORKING_DIRECTORY, this.getWorkingDir());

        return configService;
    }

    private void initConfig() {
        //Since there is not app output directory.
        config.putIfAbsent(JavacheConfigValue.MAIN_APP_JAR_NAME.name(), "");

        //There is no "classes" folder inside the jar file so we set it to empty.
        config.put(JavacheConfigValue.APP_COMPILE_OUTPUT_DIR_NAME.name(), "");

        //We want to stay on the same level.
        config.put(JavacheConfigValue.WEB_APPS_DIR_NAME.name(), "./");
    }

    private String getWorkingDir() throws IOException {
        String workingDir = mainClass.getProtectionDomain().getCodeSource().getLocation().getFile().substring(1);

        if (workingDir.endsWith(".jar")) {
            JarFileUnzipService unzipService = new JarFileUnzipServiceImpl();
            unzipService.unzipJar(new File(workingDir), false, workingDir.replace(".jar", ""));
            workingDir = workingDir.replace(".jar", "");
        }

        System.out.println(String.format("Working Directory: %s", workingDir));

        return workingDir;
    }
}
