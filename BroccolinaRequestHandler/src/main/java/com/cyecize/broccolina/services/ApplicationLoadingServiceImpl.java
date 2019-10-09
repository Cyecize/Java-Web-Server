package com.cyecize.broccolina.services;

import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.api.JavacheComponent;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.solet.HttpSolet;
import com.cyecize.solet.SoletConfig;
import com.cyecize.solet.WebSolet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@JavacheComponent
public class ApplicationLoadingServiceImpl implements ApplicationLoadingService {

    private static final String MISSING_SOLET_ANNOTATION_FORMAT = "Missing solet annotation for class named %s.";

    private final ApplicationScanningService scanningService;

    private final String assetsDir;

    private String rootAppName;

    private Map<String, HttpSolet> solets;

    private SoletConfig soletConfig;

    @Autowired
    public ApplicationLoadingServiceImpl(ApplicationScanningService scanningService, JavacheConfigService configService) {
        this.scanningService = scanningService;
        this.assetsDir = configService.getConfigParam(JavacheConfigValue.JAVACHE_WORKING_DIRECTORY, String.class) +
                configService.getConfigParam(JavacheConfigValue.ASSETS_DIR_NAME, String.class);

        this.rootAppName = configService.getConfigParam(JavacheConfigValue.MAIN_APP_JAR_NAME, String.class);
        this.solets = new HashMap<>();
        this.makeAppAssetDir(this.assetsDir);
    }

    @Override
    public List<String> getApplicationNames() {
        return this.scanningService.getApplicationNames();
    }

    /**
     * Gets all available HttpSolet implementations.
     * Iterates them, creates application assets folder for each app and
     * loads solet into the solet map.
     * <p>
     * Returns a map of solet route and solet instance.
     */
    @Override
    public Map<String, HttpSolet> loadApplications(SoletConfig soletConfig) throws IOException {
        this.soletConfig = soletConfig;
        try {
            Map<String, List<Class<HttpSolet>>> soletClasses = this.scanningService.findSoletClasses();
            for (Map.Entry<String, List<Class<HttpSolet>>> entry : soletClasses.entrySet()) {
                String applicationName = entry.getKey();
                this.makeAppAssetDir(this.assetsDir + applicationName + File.separator);

                for (Class<HttpSolet> soletClass : entry.getValue()) {
                    this.loadSolet(soletClass, applicationName);
                }
            }

        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return this.solets;
    }

    /**
     * Creates an instance of the solet.
     * If the application name is different than the javache specified main jar name (ROOT.jar by default), add the appName to the route.
     * Put the solet in a solet map with a key being the soletRoute.
     */
    private void loadSolet(Class<HttpSolet> soletClass, String applicationName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        HttpSolet soletInstance = soletClass.getDeclaredConstructor().newInstance();
        soletInstance.setAssetsFolder(this.assetsDir + applicationName);

        WebSolet soletAnnotation = this.getSoletAnnotation(soletInstance.getClass());
        if (soletAnnotation == null) {
            throw new IllegalArgumentException(String.format(MISSING_SOLET_ANNOTATION_FORMAT, soletClass.getName()));
        }

        String soletRoute = soletAnnotation.value();
        if (!applicationName.equals(this.rootAppName)) {
            soletRoute = "/" + applicationName + soletRoute;
        }

        if (!soletInstance.isInitialized()) {
            soletInstance.init(this.soletConfig);
        }

        if (!applicationName.equals("") && !applicationName.equals(this.rootAppName)) {
            soletInstance.setAppNamePrefix("/" + applicationName);
        }

        this.solets.put(soletRoute, soletInstance);
    }

    /**
     * Recursive method for getting @WebSolet annotation from a given class.
     * Recursion is required since only parent class could have @WebSolet annotation
     * and not the child.
     */
    private WebSolet getSoletAnnotation(Class<?> soletClass) {
        WebSolet solet = soletClass.getAnnotation(WebSolet.class);
        if (solet == null && soletClass.getSuperclass() != null) {
            return getSoletAnnotation(soletClass.getSuperclass());
        }
        return solet;
    }

    /**
     * Creates asset directory for the current app in javache's assets directory.
     */
    private void makeAppAssetDir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdir();
        }
    }
}
