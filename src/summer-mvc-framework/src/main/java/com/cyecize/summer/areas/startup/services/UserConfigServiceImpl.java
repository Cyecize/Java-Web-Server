package com.cyecize.summer.areas.startup.services;

import com.cyecize.solet.SoletConfig;
import com.cyecize.solet.SoletConstants;
import com.cyecize.solet.SoletLogger;
import com.cyecize.summer.areas.startup.exceptions.ConfigurationMissingException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

                final int delimiterIndex = line.indexOf('=');
                //If there is no '=' sign.
                if (delimiterIndex == -1) {
                    continue;
                }

                final String key = line.substring(0, delimiterIndex).trim();

                final String value = delimiterIndex == line.length() - 1
                        ? null //there is no value after '', in that case read null.
                        : line.substring(delimiterIndex + 1).trim();

                this.config.put(key, this.parseValue(value));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check the value for any placeholders and if there are, parse them.
     * //TODO extract into it's own service if the logic gets too complex.
     *
     * @param value - the value provided by the user.
     * @return parsed string.
     */
    private String parseValue(String value) {
        if (value == null) {
            return null;
        }
        final String placeholdersRegex = "\\$\\{(?<source>\\w+)\\.(?<key>\\w+)\\}";
        final Pattern placeholdersPattern = Pattern.compile(placeholdersRegex);

        final Matcher matcher = placeholdersPattern.matcher(value);

        String formattedValue = value;
        while (matcher.find()) {
            final String source = matcher.group("source");
            final String key = matcher.group("key");

            if (source.equalsIgnoreCase("env")) {
                final String envValue = System.getenv(key);
                if (envValue == null) {
                    throw new ConfigurationMissingException(String.format(
                            "Could not find environment variable with name '%s'.", key
                    ));
                }

                formattedValue = formattedValue.replaceFirst(placeholdersRegex, envValue);

            } else {
                throw new ConfigurationMissingException(String.format(
                        "Unable to parse value '%s'! Source '%s' is incorrect!", value, source
                ));
            }
        }

        return formattedValue;
    }
}
