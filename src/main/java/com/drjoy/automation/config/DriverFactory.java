package com.drjoy.automation.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DriverFactory {
    @Getter
    private static final String CACHE_PATH = "C:/selenium-app-setting/drivers/caches";

    private static WebDriver driver;

    private DriverFactory() {}

    public static synchronized WebDriver getDriver() {
        if (driver == null || isDriverInvalid(driver)) {
            createNewDriver();
        }
        return driver;
    }

    public static void stopDriver() {
        if (driver != null) {
            driver.quit();
            driver = null; // rất quan trọng
        }
    }

    private static void createNewDriver() {
        String browser = Configuration.getBrowser();
        boolean headless = Configuration.isHeadless();

        switch (browser) {
            case "firefox":
                WebDriverManager.firefoxdriver().cachePath(CACHE_PATH).setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) firefoxOptions.addArguments("--headless");
                driver = new FirefoxDriver(firefoxOptions);
                break;

            case "edge":
                WebDriverManager.edgedriver().cachePath(CACHE_PATH).setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                driver = new EdgeDriver(edgeOptions);
                break;

            case "chrome":
            default:
                WebDriverManager.chromedriver().cachePath(CACHE_PATH).setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) chromeOptions.addArguments("--headless=new", "--disable-gpu");
                driver = new RemoteWebDriver(chromeOptions);
                break;
        }

        driver.manage().window().maximize();
    }

    private static boolean isDriverInvalid(WebDriver driver) {
        try {
            return ((RemoteWebDriver) driver).getSessionId() == null;
        } catch (Exception e) {
            return true;
        }
    }
}
