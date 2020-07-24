package com.cyecize.javache;

import com.cyecize.javache.services.JavacheConfigService;

/**
 * Enum that contains all config parameters that can be specified in {@link JavacheConfigService}
 */
public enum JavacheConfigValue {

    /**
     * Specify the max request size in bytes.
     */
    MAX_REQUEST_SIZE,

    /**
     * If true, request content will be logged.
     */
    SHOW_REQUEST_LOG,

    /**
     * Specify the folder name in which the web applications will be located, defaults to webapps.
     */
    WEB_APPS_DIR_NAME,

    /**
     * Specify the folder name in which the permanent web app assets will be located, defaults to assets.
     */
    ASSETS_DIR_NAME,

    /**
     * Specify the folder name in which the global libraries will be located, defaults to lib.
     */
    LIB_DIR_NAME,

    /**
     * Specify the folder name in which the server APIs such as HttpApi will be located, defaults to api.
     */
    API_DIR_NAME,

    /**
     * Specify the folder name in which the logs will be located, defaults to logs.
     */
    LOGS_DIR_NAME,

    /**
     * Specify the folder name within a web app JAR in which the compile output will be located, defaults to classes.
     */
    APP_COMPILE_OUTPUT_DIR_NAME,

    /**
     * Specify the folder name withing a web app JAR within the compile output dir in which
     * web resources such as html, css, js will be located, defaults to webapp.
     */
    APP_RESOURCES_DIR_NAME,

    /**
     * Specify the name of the main web app, defaults to ROOT.
     */
    MAIN_APP_JAR_NAME,

    /**
     * Specify the folder name withing a web app JAR in which the app's libraries are located, defaults to lib.
     */
    APPLICATION_DEPENDENCIES_FOLDER_NAME,

    /**
     * Setting to true will result in broccolina not deleting a web app folder and extracting the JAR again
     * if such folder exists.
     */
    BROCOLLINA_SKIP_EXTRACTING_IF_FOLDER_EXISTS,

    /**
     * Setting to false will not delete the app's folder and files that are not present in the new
     * build of the app will still be present.
     */
    BROCCOLINA_FORCE_OVERWRITE_FILES,

    /**
     * Setting to false will result in solets not accepting requests that might be resource requests (css, js, html).
     */
    BROCCOLINA_TRACK_RESOURCES,

    /**
     * Specify the server port, defaults to 8000
     */
    SERVER_PORT,

    /**
     * Specify the server's startup args as when running a main method.
     */
    SERVER_STARTUP_ARGS,

    /**
     * The context directory of Javache.
     */
    JAVACHE_WORKING_DIRECTORY,

    /**
     * Setting to false will result in javache not logging unhandled exceptions.
     */
    JAVACHE_PRINT_EXCEPTIONS,

    /**
     * Specify the order of Toyote resource handler, defaults to 1.
     */
    TOYOTE_RESOURCE_HANDLER_ORDER,

    /**
     * Specify the order of Broccolina request handler, defaults to 2.
     */
    BROCCOLINA_SOLET_DISPATCHER_ORDER,

    /**
     * Setting to false will result in resources not being cached (no Cache-Control header being sent)/
     */
    ENABLE_RESOURCE_CACHING,

    /**
     * Specify an expression for the caching type for each resource media type.
     * Format - media/type1, media/type2 @ header-value & media/type3 @ header-value
     */
    RESOURCE_CACHING_EXPRESSION,
}
