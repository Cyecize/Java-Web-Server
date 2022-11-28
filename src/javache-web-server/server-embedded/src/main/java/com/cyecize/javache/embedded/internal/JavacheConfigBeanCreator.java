package com.cyecize.javache.embedded.internal;

import com.cyecize.WebConstants;
import com.cyecize.broccolina.services.JarFileUnzipService;
import com.cyecize.broccolina.services.JarFileUnzipServiceImpl;
import com.cyecize.ioc.annotations.Bean;
import com.cyecize.ioc.annotations.PostConstruct;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.embedded.services.EmbeddedJavacheConfigService;
import com.cyecize.javache.services.JavacheConfigService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Utility class that creates {@link Bean} for {@link JavacheConfigService}.
 * The reason this is required is because {@link JavacheConfigService} has two implementations - one
 * for the standalone server and another for the embedded server.
 */
@JavacheEmbeddedComponent
public class JavacheConfigBeanCreator {

    public static Integer port;

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

        if (configService.getConfigParam(JavacheConfigValue.SERVER_PORT, int.class) == WebConstants.JAVACHE_CONFIG_EMPTY_PORT) {
            int port = WebConstants.DEFAULT_SERVER_PORT;
            if (JavacheConfigBeanCreator.port != null) {
                port = JavacheConfigBeanCreator.port;
            }

            configService.addConfigParam(JavacheConfigValue.SERVER_PORT, port);
        }

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

    /**
     * Gets the server's working directory.
     * If the app is in a jar file, it will extract it and return the directory to that folder.
     *
     * @return working directory.
     */
    private String getWorkingDir() throws IOException {
        String workingDir;
        try {
            final URI uri = mainClass.getProtectionDomain().getCodeSource().getLocation().toURI();
            workingDir = Path.of(uri).toString();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        if (workingDir.endsWith(".jar")) {
            final JarFileUnzipService unzipService = new JarFileUnzipServiceImpl();
            unzipService.unzipJar(new File(workingDir), false, workingDir.replace(".jar", ""));
            workingDir = workingDir.replace(".jar", "");
        }

        System.out.println(String.format("Working Directory: %s", workingDir));

        return workingDir;
    }
}
