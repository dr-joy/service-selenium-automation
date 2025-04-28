package com.drjoy.automation.service;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.utils.WebUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginService {
    private static final Logger logger = LogManager.getLogger(LoginService.class);

    private LoginService() {}

    public boolean isLoginAlready() {
        try {
            String currentUrl = DriverFactory.getDriver().getCurrentUrl();
            if (currentUrl != null && !currentUrl.contains("/re/re0022")) {
                return true; // đã đăng nhập
            }
        } catch (Exception e) {
            // ignore error
        }

        return false;
    }

    public static void login(String username, String password) {
        WebDriver driver = DriverFactory.getDriver();
        driver.get(Configuration.getBaseUrl());
        driver.manage().window().maximize();

        if (username != null && password != null) {
            WebUI.findWebElementIfVisible(By.xpath("//input[@name='login-id']")).sendKeys(username);
            WebUI.findWebElementIfPresent(By.xpath("//input[@name='password']")).sendKeys(password);

            WebUI.findWebElementIfPresent(By.xpath("//button[@type='submit']")).click(); // Thay bằng ID thực tế nếu khác

            try {
                WebUI.sleep(5000);

                By btnApprove = By.xpath("//button[@class='btn btn-success' and @type='submit' and text()='同意する']");
                if (WebUI.waitForElementClickable(btnApprove, 5) != null) {
                    driver.findElement(By.xpath("//label[@class='custom-control custom-checkbox m-0']")).click();
                    driver.findElement(btnApprove).click();
                }
            } catch (Exception e) {
                logger.error("Nút xác nhận không tồn tại hoặc không thể nhấn: {}", e.getMessage());
            }
        }
    }

    public static void logout() {
        WebUI.findWebElementIfVisible(By.xpath("//a[@id='header-btn-settings-panel' and @title='設定']")).click();

        WebUI.findWebElementIfVisible(By.xpath("//a[@class='nav-link fs13 cursor-pointer' and text()='ログアウト']")).click();
    }
}
