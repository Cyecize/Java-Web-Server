package com.cyecize.javache.services;

import com.cyecize.WebConstants;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.api.JavacheComponent;
import com.cyecize.javache.io.Reader;
import com.cyecize.javache.utils.PrimitiveTypeDataResolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JavacheComponent
public class JavacheConfigServiceImpl implements JavacheConfigService {

    private static final String CONFIG_FOLDER_PATH = WebConstants.WORKING_DIRECTORY + "config/";

    private static final String REQUEST_HANDLER_PRIORITY_FILE = CONFIG_FOLDER_PATH + "request-handlers.ini";

    private static final String CONFIG_FILE_PATH = CONFIG_FOLDER_PATH + "config.ini";

    private static final String REQUEST_HANDLER_PRIORITY_FILE_NOT_FOUND_FORMAT =
            "Request Handler priority configuration file does not exist for \"%s\".";

    private final PrimitiveTypeDataResolver dataResolver;

    private List<String> requestHandlers;


    protected Map<String, Object> configParameters;

    public JavacheConfigServiceImpl() {
        this.dataResolver = new PrimitiveTypeDataResolver();
        this.initConfigurations();
    }

    @Override
    public void addConfigParam(String paramName, Object value) {
        this.configParameters.put(paramName, value);
    }

    @Override
    public void addConfigParam(JavacheConfigValue paramName, Object value) {
        this.addConfigParam(paramName.name(), value);
    }

    @Override
    public <T> T getConfigParam(String paramName, Class<T> type) {
        return (T) this.configParameters.get(paramName);
    }

    @Override
    public <T> T getConfigParam(JavacheConfigValue paramName, Class<T> type) {
        return this.getConfigParam(paramName.name(), type);
    }

    @Override
    public Object getConfigParam(JavacheConfigValue paramName) {
        return this.configParameters.get(paramName.name());
    }

    @Override
    public String getConfigParamString(String paramName) {
        final Object configParam = this.getConfigParam(paramName, Object.class);
        if (configParam != null) {
            return configParam.toString();
        }

        return null;
    }

    @Override
    public String getConfigParamString(JavacheConfigValue paramName) {
        return this.getConfigParamString(paramName.name());
    }

    @Override
    public List<String> getRequestHandlerPriority() {
        return this.requestHandlers;
    }

    @Override
    public Map<String, Object> getConfigParams() {
        return this.configParameters;
    }

    private void initConfigurations() {
        try {
            this.loadRequestHandlerConfig();
            this.initDefaultConfigParams();
            this.initConfigParams();
            this.applyEnvironmentVariables();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Looks for file that contains request handler names and priority.
     */
    protected void loadRequestHandlerConfig() throws IOException {
        final File priorityConfigFile = new File(REQUEST_HANDLER_PRIORITY_FILE);

        if (!priorityConfigFile.exists() || !priorityConfigFile.isFile()) {
            throw new IllegalArgumentException(
                    String.format(REQUEST_HANDLER_PRIORITY_FILE_NOT_FOUND_FORMAT, CONFIG_FOLDER_PATH)
            );
        }

        final String configFileContent = new Reader().readAllLines(new FileInputStream(priorityConfigFile));

        this.requestHandlers = Arrays.stream(configFileContent.split(",\\s+"))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Adds the default parameters that javache web server will use.
     */
    private void initDefaultConfigParams() {
        this.configParameters = new HashMap<>();
        this.configParameters.put(JavacheConfigValue.MAX_REQUEST_SIZE.name(), Integer.MAX_VALUE);
        this.configParameters.put(JavacheConfigValue.SHOW_REQUEST_LOG.name(), false);
        this.configParameters.put(JavacheConfigValue.ASSETS_DIR_NAME.name(), "assets/");
        this.configParameters.put(JavacheConfigValue.WEB_APPS_DIR_NAME.name(), "webapps/");
        this.configParameters.put(JavacheConfigValue.APP_COMPILE_OUTPUT_DIR_NAME.name(), "classes");
        this.configParameters.put(JavacheConfigValue.APP_RESOURCES_DIR_NAME.name(), "webapp");
        this.configParameters.put(JavacheConfigValue.MAIN_APP_JAR_NAME.name(), "ROOT");
        this.configParameters.put(JavacheConfigValue.APPLICATION_DEPENDENCIES_FOLDER_NAME.name(), "lib");
        this.configParameters.put(JavacheConfigValue.BROCOLLINA_SKIP_EXTRACTING_IF_FOLDER_EXISTS.name(), false);
        this.configParameters.put(JavacheConfigValue.BROCCOLINA_FORCE_OVERWRITE_FILES.name(), true);
        this.configParameters.put(JavacheConfigValue.BROCCOLINA_TRACK_RESOURCES.name(), true);
        this.configParameters.put(JavacheConfigValue.SERVER_PORT.name(), WebConstants.JAVACHE_CONFIG_EMPTY_PORT);
        this.configParameters.put(JavacheConfigValue.SERVER_STARTUP_ARGS.name(), new String[0]);
        this.configParameters.put(JavacheConfigValue.JAVACHE_WORKING_DIRECTORY.name(), WebConstants.WORKING_DIRECTORY);
        this.configParameters.put(JavacheConfigValue.LIB_DIR_NAME.name(), "lib/");
        this.configParameters.put(JavacheConfigValue.API_DIR_NAME.name(), "api/");
        this.configParameters.put(JavacheConfigValue.LOGS_DIR_NAME.name(), "logs/");
        this.configParameters.put(JavacheConfigValue.JAVACHE_PRINT_EXCEPTIONS.name(), true);
        this.configParameters.put(JavacheConfigValue.TOYOTE_RESOURCE_HANDLER_ORDER.name(), 1);
        this.configParameters.put(JavacheConfigValue.BROCCOLINA_SOLET_DISPATCHER_ORDER.name(), 2);
        this.configParameters.put(JavacheConfigValue.ENABLE_RESOURCE_CACHING.name(), true);
        this.configParameters.put(JavacheConfigValue.RESOURCE_CACHING_EXPRESSION.name(), WebConstants.DEFAULT_CACHING_EXPRESSION);
    }

    /**
     * Reads the config.ini file and filters those settings that are
     * available as default (set in initDefaultConfigParams)
     * Then gets the current param type and tries to convert the value to that type.
     */
    private void initConfigParams() throws IOException {
        final File configFile = new File(CONFIG_FILE_PATH);
        if (!configFile.exists() || !configFile.isFile()) {
            return;
        }

        final BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(new FileInputStream(CONFIG_FILE_PATH)));
        //TODO: refactor the parsing -> case when user has ':' in his value.
        while (bufferedReader.ready()) {
            final String line = bufferedReader.readLine();
            final String[] keyValuePair = line.trim().split(":\\s+");

            if (keyValuePair.length != 2) {
                continue;
            }

            keyValuePair[0] = keyValuePair[0].toUpperCase();
            if (!this.configParameters.containsKey(keyValuePair[0])) {
                continue;
            }

            this.configParameters.put(keyValuePair[0], this.dataResolver.resolve(
                    this.configParameters.get(keyValuePair[0]).getClass(), keyValuePair[1]
            ));
        }
    }

    /**
     * As a last step, Javache will check for any environment variables with names that match {@link JavacheConfigValue}
     * Since environment variables are applied last, they have the highest priority.
     */
    private void applyEnvironmentVariables() {
        for (JavacheConfigValue cfg : JavacheConfigValue.values()) {
            final String val = System.getenv(cfg.name());
            if (val == null) {
                continue;
            }

            this.configParameters.put(
                    cfg.name(),
                    this.dataResolver.resolve(
                            this.configParameters.get(cfg.name()).getClass(),
                            val
                    )
            );
        }
    }
}
