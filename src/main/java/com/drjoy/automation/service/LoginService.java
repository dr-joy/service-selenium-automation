package com.drjoy.automation.service;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.utils.WebElementUtils;
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

    public static void login(String username, String password) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        driver.get(Configuration.getBaseUrl());
        driver.manage().window().maximize();

        if (username != null && password != null) {
            driver.findElement(By.xpath("//input[@name='login-id']")).sendKeys(username);
            driver.findElement(By.xpath("//input[@name='password']")).sendKeys(password);

            driver.findElement(By.xpath("//button[@type='submit']")).click(); // Thay bằng ID thực tế nếu khác

            try {
                Thread.sleep(5000); // Chờ dialog nếu có

                By btnApprove = By.xpath("//button[@class='btn btn-success' and @type='submit' and text()='同意する']");
                if (WebElementUtils.waitForElementClickable(driver, btnApprove, 5) != null) {
                    driver.findElement(By.xpath("//label[@class='custom-control custom-checkbox m-0']")).click();
                    driver.findElement(btnApprove).click();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Tái gián đoạn luồng
                logger.error("Luồng bị gián đoạn khi chờ nút xác nhận: {}", e.getMessage());
            } catch (Exception e) {
                logger.error("Nút xác nhận không tồn tại hoặc không thể nhấn: {}", e.getMessage());
            }

            WebElementUtils.waitForElementPresent(driver, By.xpath("//a[@class='navbar-brand']"), 20);
        }
    }
}
