package com.cyecize;

import com.cyecize.ioc.MagicInjector;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.ioc.services.DependencyContainer;
import com.cyecize.javache.ConfigConstants;
import com.cyecize.javache.core.ServerInitializer;
import com.cyecize.javache.services.JavacheConfigService;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

@Service
public class StartUp {

    public static void main(String[] args) throws Exception {
        replaceSystemClassLoader();

        final DependencyContainer dependencyContainer = MagicInjector.run(StartUp.class);
        dependencyContainer.getService(JavacheConfigService.class).addConfigParam(ConfigConstants.SERVER_STARTUP_ARGS, args);

        dependencyContainer.getService(ServerInitializer.class).initializeServer();
    }

    /*
        This is a workaround for java 9 and above.
        Since java 9 the systemClassLoader is no longer URLClassLoader
        which means that there is no method "addUrl"
        by doing this we change the default classLoader with URLClassLoader
     */
    public static void replaceSystemClassLoader() throws IllegalAccessException {
        Field scl = Arrays.stream(ClassLoader.class.getDeclaredFields())
                .filter(f -> f.getType() == ClassLoader.class && !f.getName().equals("parent"))
                .findFirst().orElse(null);

        scl.setAccessible(true);
        scl.set(null, new URLClassLoader(new URL[0]));
        Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
    }
}
