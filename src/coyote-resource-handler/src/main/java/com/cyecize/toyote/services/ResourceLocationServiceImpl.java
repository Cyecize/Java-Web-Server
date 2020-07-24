package com.cyecize.toyote.services;

import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.PostConstruct;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.common.PathUtils;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.toyote.exceptions.ResourceNotFoundException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ResourceLocationServiceImpl implements ResourceLocationService {

    private static final String RESOURCE_NOT_FOUND_FORMAT = "Resource \"%s\" not found!";

    private final JavacheConfigService configService;

    private final AppNameCollectService appNameCollectService;

    private final List<String> appNames;

    private String pathToAssetsFormat;

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

    /**
     * Looks for a resource in the webapps or in the assets directory.
     *
     * @param requestURL - path to resource.
     * @return file which name matches the request url.
     * @throws ResourceNotFoundException if the resource file cannot be found.
     */
    @Override
    public File locateResource(String requestURL) throws ResourceNotFoundException {
        final String currentRequestAppName = this.getAppNameForRequest(requestURL);
        requestURL = requestURL.replaceFirst(Pattern.quote("/" + currentRequestAppName), "");

        File file = new File(this.createWebappsResourceDir(requestURL, currentRequestAppName));
        if (!file.exists() || file.isDirectory()) {
            file = new File(this.createAssetsResourceDir(requestURL, currentRequestAppName));
        }

        if (file.exists() && !file.isDirectory()) {
            return file;
        }

        throw new ResourceNotFoundException(String.format(RESOURCE_NOT_FOUND_FORMAT, requestURL));
    }

    private String getAppNameForRequest(String requestURL) {
        for (String appName : this.appNames) {
            if (requestURL.startsWith("/" + appName)) {
                return appName;
            }
        }

        return this.configService.getConfigParamString(JavacheConfigValue.MAIN_APP_JAR_NAME);
    }

    private String createWebappsResourceDir(String requestURL, String appName) {
        return String.format(
                this.pathToWebappsFormat,
                PathUtils.trimAllSlashes(appName),
                PathUtils.trimAllSlashes(requestURL)
        );
    }

    private String createAssetsResourceDir(String requestURL, String appName) {
        return String.format(
                this.pathToAssetsFormat,
                PathUtils.trimAllSlashes(appName),
                PathUtils.trimAllSlashes(requestURL)
        );
    }

    private void initDirectories() {
        final String workingDir = this.configService.getConfigParamString(JavacheConfigValue.JAVACHE_WORKING_DIRECTORY);

        String pathToAssets = PathUtils.appendPath(
                workingDir,
                this.configService.getConfigParamString(JavacheConfigValue.ASSETS_DIR_NAME)
        );

        pathToAssets = PathUtils.appendPath(pathToAssets, "%s");
        pathToAssets = PathUtils.appendPath(pathToAssets, "%s");

        this.pathToAssetsFormat = pathToAssets;

        String pathToWebApps = PathUtils.appendPath(
                workingDir,
                this.configService.getConfigParamString(JavacheConfigValue.WEB_APPS_DIR_NAME)
        );

        pathToWebApps = PathUtils.appendPath(
                pathToWebApps,
                "%s"
        );

        pathToWebApps = PathUtils.appendPath(
                pathToWebApps,
                this.configService.getConfigParamString(JavacheConfigValue.APP_COMPILE_OUTPUT_DIR_NAME)
        );

        pathToWebApps = PathUtils.appendPath(
                pathToWebApps,
                this.configService.getConfigParamString(JavacheConfigValue.APP_RESOURCES_DIR_NAME)
        );

        pathToWebApps = PathUtils.appendPath(
                pathToWebApps,
                "%s"
        );

        this.pathToWebappsFormat = pathToWebApps;
    }
}
