package com.drjoy.automation.config;

import com.drjoy.automation.exception.ConfigLoadException;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
    private static final Properties properties = new Properties();

    private static String env;
    private static String dataBasePath;
    private static String browser;
    @Getter
    private static boolean headless;

    private Configuration() {}

    static {
        env = System.getenv("env");

        browser = System.getenv("browser");

        headless = Boolean.parseBoolean(System.getenv("headless"));

        dataBasePath = System.getenv("dataBasePath");

        try (InputStream input = Configuration.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new ConfigLoadException("Lỗi khi load file cấu hình:" );
            }
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigLoadException("Lỗi khi load file cấu hình: ", e);
        }
    }

    public static String getBaseUrl() {
        if (env == null || env.isEmpty()) {
            env = get("app.url.jackfruit");
        }
        return get(String.format("app.url.%s", env));
    }

    public static String getBrowser() {
        if (browser == null || browser.isEmpty()) {
            browser = get("app.default.browser");
        }
        return browser;
    }

    public static String getDataBasePath() {
        if (dataBasePath == null || dataBasePath.isEmpty()) {
            dataBasePath = get("app.database.path");
        }
        return dataBasePath;
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
