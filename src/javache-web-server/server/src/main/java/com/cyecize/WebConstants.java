package com.cyecize;

import com.cyecize.ioc.config.MagicConfiguration;
import com.cyecize.javache.api.JavacheComponent;
import com.cyecize.javache.common.PathUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

public final class WebConstants {

    public static final int DEFAULT_SERVER_PORT = 8000;

    public static final int JAVACHE_CONFIG_EMPTY_PORT = -1;

    public static final String WORKING_DIRECTORY;

    static {
        try {
            final URI uri = StartUp.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            final Path path = Path.of(PathUtils.appendPath(Path.of(uri).toString(), "../")).normalize();
            WORKING_DIRECTORY = PathUtils.appendPath(path.toString(), "");
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static final MagicConfiguration JAVACHE_IOC_CONFIGURATION = new MagicConfiguration()
            .scanning().addCustomServiceAnnotation(JavacheComponent.class)
            .and()
            .build();

    public static final String DEFAULT_CACHING_EXPRESSION = "image/png, image/gif, image/jpeg @ max-age=120 " +
            "& text/css @ max-age=84600, public " +
            "& application/javascript @ max-age=7200";

    private WebConstants() {
        throw new RuntimeException();
    }
}