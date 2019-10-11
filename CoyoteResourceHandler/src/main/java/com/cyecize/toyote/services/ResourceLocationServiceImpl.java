package com.cyecize.toyote.services;

import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.PostConstruct;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.toyote.exceptions.ResourceNotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ResourceLocationServiceImpl implements ResourceLocationService {

    private static final String RESOURCE_NOT_FOUND_FORMAT = "Resource \"%s\" not found!";

    private final JavacheConfigService configService;

    private final AppNameCollectService appNameCollectService;

    private final List<String> appNames;

    private String pathToAssets;

    private String pathToWebappsFormat;

    @Autowired
    public ResourceLocationServiceImpl(AppNameCollectService appNameCollectService, JavacheConfigService configService) {
        this.configService = configService;
        this.appNameCollectService = appNameCollectService;
        this.appNames = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        this.appNames.addAll(this.appNameCollectService.getApplicationNames());
        this.initDirectories();
    }

    @Override
    public InputStream locateResource(String requestURL) throws ResourceNotFoundException, FileNotFoundException {
        final String currentRequestAppName = this.getAppNameForRequest(requestURL);
        requestURL = requestURL.replaceFirst(Pattern.quote("/" + currentRequestAppName), "");

        File file = new File(this.createWebappsResourceDir(requestURL, currentRequestAppName));
        if (!file.exists() || file.isDirectory()) {
            file = new File(this.createAssetsResourceDir(requestURL, currentRequestAppName));
        }

        if (file.exists() && !file.isDirectory()) {
            return new FileInputStream(file);
        }

        throw new ResourceNotFoundException(String.format(RESOURCE_NOT_FOUND_FORMAT, requestURL));
    }

    private String getAppNameForRequest(String requestURL) {
        for (String appName : this.appNames) {
            if (requestURL.startsWith("/" + appName)) {
                return appName;
            }
        }

        return this.configService.getConfigParam(JavacheConfigValue.MAIN_APP_JAR_NAME, String.class);
    }

    private String createWebappsResourceDir(String requestURL, String appName) {
        return String.format(this.pathToWebappsFormat, appName, requestURL);
    }

    private String createAssetsResourceDir(String requestURL, String appName) {
        return this.pathToAssets + appName + requestURL;
    }

    private void initDirectories() {
        final String workingDir = this.configService.getConfigParam(JavacheConfigValue.JAVACHE_WORKING_DIRECTORY, String.class);

        this.pathToAssets = workingDir + this.configService.getConfigParam(JavacheConfigValue.ASSETS_DIR_NAME, String.class);

        this.pathToWebappsFormat = workingDir +
                this.configService.getConfigParam(JavacheConfigValue.WEB_APPS_DIR_NAME) +
                "%s" + File.separator +
                this.configService.getConfigParam(JavacheConfigValue.APP_COMPILE_OUTPUT_DIR_NAME) +
                "%s";
    }
}
