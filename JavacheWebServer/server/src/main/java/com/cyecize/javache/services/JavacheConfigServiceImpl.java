package com.cyecize.javache.services;

import com.cyecize.WebConstants;
import com.cyecize.javache.io.Reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class JavacheConfigServiceImpl implements JavacheConfigService {

    private static final String CONFIG_FOLDER_PATH = WebConstants.WORKING_DIRECTORY + "config/";

    private static final String REQUEST_HANDLER_PRIORITY_FILE = CONFIG_FOLDER_PATH + "request-handlers.ini";

    private static final String REQUEST_HANDLER_PRIORITY_FILE_NOT_FOUND_FORMAT = "Request Handler priority configuration file does not exist for \"%s\".";

    private List<String> requestHandlers;

    public JavacheConfigServiceImpl() {
        this.initConfigurations();
    }

    @Override
    public List<String> getRequestHandlerPriority() {
        return this.requestHandlers;
    }

    private void initConfigurations() {
        try {
            this.loadRequestHandlerConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRequestHandlerConfig() throws IOException {
        File priorityConfigFile = new File(REQUEST_HANDLER_PRIORITY_FILE);

        if (!priorityConfigFile.exists() || !priorityConfigFile.isFile()) {
            throw new IllegalArgumentException(String.format(REQUEST_HANDLER_PRIORITY_FILE_NOT_FOUND_FORMAT, CONFIG_FOLDER_PATH));
        }

        String configFileContent = new Reader().readAllLines(new FileInputStream(priorityConfigFile));

        this.requestHandlers = Arrays.stream(configFileContent.split(",\\s+"))
                .collect(Collectors.toCollection(LinkedList::new));
    }


}
