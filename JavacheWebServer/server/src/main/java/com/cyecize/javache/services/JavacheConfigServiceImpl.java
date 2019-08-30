package com.cyecize.javache.services;

import com.cyecize.WebConstants;
import com.cyecize.javache.ConfigConstants;
import com.cyecize.javache.io.Reader;
import com.cyecize.javache.utils.PrimitiveTypeDataResolver;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class JavacheConfigServiceImpl implements JavacheConfigService {

    private static final String CONFIG_FOLDER_PATH = WebConstants.WORKING_DIRECTORY + "config/";

    private static final String REQUEST_HANDLER_PRIORITY_FILE = CONFIG_FOLDER_PATH + "request-handlers.ini";

    private static final String CONFIG_FILE_PATH = CONFIG_FOLDER_PATH + "config.ini";

    private static final String REQUEST_HANDLER_PRIORITY_FILE_NOT_FOUND_FORMAT = "Request Handler priority configuration file does not exist for \"%s\".";

    private List<String> requestHandlers;

    private PrimitiveTypeDataResolver dataResolver;

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
    public <T> T getConfigParam(String paramName, Class<T> type) {
        return (T) this.configParameters.get(paramName);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Looks for file that contains request handler names and priority.
     */
    protected void loadRequestHandlerConfig() throws IOException {
        File priorityConfigFile = new File(REQUEST_HANDLER_PRIORITY_FILE);

        if (!priorityConfigFile.exists() || !priorityConfigFile.isFile()) {
            throw new IllegalArgumentException(String.format(REQUEST_HANDLER_PRIORITY_FILE_NOT_FOUND_FORMAT, CONFIG_FOLDER_PATH));
        }

        String configFileContent = new Reader().readAllLines(new FileInputStream(priorityConfigFile));

        this.requestHandlers = Arrays.stream(configFileContent.split(",\\s+"))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Adds the default parameters that javache web server will use.
     */
    private void initDefaultConfigParams() {
        this.configParameters = new HashMap<>();
        this.configParameters.put(ConfigConstants.MAX_REQUEST_SIZE, Integer.MAX_VALUE);
        this.configParameters.put(ConfigConstants.SHOW_REQUEST_LOG, false);
        this.configParameters.put(ConfigConstants.ASSETS_DIR_NAME, "assets/");
        this.configParameters.put(ConfigConstants.WEB_APPS_DIR_NAME, "webapps/");
        this.configParameters.put(ConfigConstants.APP_COMPILE_OUTPUT_DIR_NAME, "classes");
        this.configParameters.put(ConfigConstants.MAIN_APP_JAR_NAME, "ROOT");
        this.configParameters.put(ConfigConstants.APPLICATION_DEPENDENCIES_FOLDER_NAME, "lib");
        this.configParameters.put(ConfigConstants.BROCOLLINA_SKIP_EXTRACTING_IF_FOLDER_EXISTS, false);
        this.configParameters.put(ConfigConstants.BROCCOLINA_FORCE_OVERWRITE_FILES, true);
        this.configParameters.put(ConfigConstants.BROCCOLINA_TRACK_RESOURCES, true);
        this.configParameters.put(ConfigConstants.TOYOTE_MAX_NUMBER_OF_CACHED_FILES, 50);
        this.configParameters.put(ConfigConstants.TOYOYE_CACHED_FILE_MAX_SIZE, 1000000);
        this.configParameters.put(ConfigConstants.SERVER_PORT, WebConstants.DEFAULT_SERVER_PORT);
        this.configParameters.put(ConfigConstants.SERVER_STARTUP_ARGS, new String[0]);
    }

    /**
     * Reads the config.ini file and filters those settings that are
     * available as default (set in initDefaultConfigParams)
     * Then gets the current param type and tries to convert the value to that type.
     */
    private void initConfigParams() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);
        if (!configFile.exists() || !configFile.isFile()) {
            return;
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(CONFIG_FILE_PATH)));
        while (bufferedReader.ready()) {
            String line = bufferedReader.readLine();
            String[] keyValuePair = line.trim().split(":\\s+");
            if (keyValuePair.length != 2) {
                continue;
            }

            keyValuePair[0] = keyValuePair[0].toUpperCase();
            if (!this.configParameters.containsKey(keyValuePair[0])) {
                continue;
            }

            this.configParameters.put(keyValuePair[0], this.dataResolver.resolve(this.configParameters.get(keyValuePair[0]).getClass(), keyValuePair[1]));
        }
    }

}
