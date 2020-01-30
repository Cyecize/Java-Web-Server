package com.cyecize.summer.areas.startup.services;

import com.cyecize.solet.SoletConfig;
import com.cyecize.solet.SoletConstants;
import com.cyecize.solet.SoletLogger;
import com.cyecize.summer.areas.startup.util.JavacheConfigServiceUtils;
import com.cyecize.summer.constants.IocConstants;
import com.cyecize.summer.utils.PathUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class UserConfigServiceImpl implements UserConfigService {

    private final SoletConfig soletConfig;

    private final Map<String, Object> javacheConfig;

    private final SoletLogger soletLogger;

    private Map<String, String> config;

    public UserConfigServiceImpl(SoletConfig soletConfig, Map<String, Object> javacheConfig) {
        this.soletConfig = soletConfig;
        this.javacheConfig = javacheConfig;
        this.soletLogger = (SoletLogger) soletConfig.getAttribute(SoletConstants.SOLET_CONFIG_LOGGER);

        this.readConfig();
    }

    @Override
    public Map<String, String> getUserProvidedConfig() {
        return this.config;
    }

    private void readConfig() {
        this.config = new HashMap<>();

        final String configFilePath = PathUtils.appendPath(
                this.soletConfig.getAttribute(SoletConstants.SOLET_CFG_WORKING_DIR).toString(),
                IocConstants.PROPERTIES_FILE_NAME
        );

        final File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            this.soletLogger.warning("Missing config file '%s'.", configFilePath);
            return;
        }

        this.readFile(configFile);

        JavacheConfigServiceUtils.overrideJavacheConfig(this.javacheConfig, this.config);
    }

    private void readFile(File file) {
        try (final FileInputStream inputStream = new FileInputStream(file)) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            while (reader.ready()) {
                final String line = reader.readLine();
                final String[] keyValuePair = line.split("\\s*=\\s*");
                if (keyValuePair.length < 2) {
                    continue;
                }

                this.config.put(keyValuePair[0], keyValuePair[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
