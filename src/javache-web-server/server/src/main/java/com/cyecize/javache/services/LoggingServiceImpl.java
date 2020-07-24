package com.cyecize.javache.services;

import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.JavacheConfigValue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

@Service
public class LoggingServiceImpl implements LoggingService {

    //TODO: make this configurable.
    private static final String LOGS_FILE_NAME = "javache.log";

    private final JavacheConfigService configService;

    private String filePath;

    @Autowired
    public LoggingServiceImpl(JavacheConfigService configService) {
        this.configService = configService;
        this.initLogsFile();
    }

    @Override
    public void info(Object msg, Object... params) {
        this.print("INFO", msg, params);
    }

    @Override
    public void warning(Object msg, Object... params) {
        this.print("WARNING", msg, params);
    }

    @Override
    public void error(Object msg, Object... params) {
        this.print("ERROR", msg, params);
    }

    @Override
    public void printStackTrace(Throwable throwable) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final PrintWriter printWriter = new PrintWriter(outputStream);

        throwable.printStackTrace(printWriter);
        printWriter.flush();
        printWriter.close();

        this.error(new String(outputStream.toByteArray()));
    }

    private synchronized void print(String type, Object msg, Object... params) {
        final String formattedMsg = String.format("[%s][%s] %s", this.getDate(), type, String.format(msg + "", params));
        System.out.println(formattedMsg);
        this.writeToFile(formattedMsg);
    }

    private String getDate() {
        return new Date().toInstant().toString();
    }

    /**
     * Appends a given text to the log file.
     * Creates the log file if it doesn't exist.
     *
     * @param msg - given text.
     */
    private void writeToFile(String msg) {
        try {
            final File file = new File(this.filePath);
            if (!file.exists()) {
                file.createNewFile();
            }

            try (final PrintWriter printer = new PrintWriter(new FileWriter(this.filePath, true))) {
                printer.println(msg);
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates the log dir if it doesn't exist, sets up the path to the logs file.
     */
    private void initLogsFile() {
        final String logsDir = this.configService.getConfigParamString(JavacheConfigValue.JAVACHE_WORKING_DIRECTORY) +
                this.configService.getConfigParamString(JavacheConfigValue.LOGS_DIR_NAME);

        new File(logsDir).mkdir();

        this.filePath = logsDir + LOGS_FILE_NAME;
    }
}
