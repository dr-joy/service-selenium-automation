package com.drjoy.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WebElementUtils {

    private static final Logger logger = LogManager.getLogger(WebElementUtils.class);

    /**
     * Đợi đến khi một phần tử có thể nhấn được trong khoảng thời gian chỉ định.
     *
     * @param driver  WebDriver hiện tại
     * @param by      Định danh phần tử (By.xpath, By.id,...)
     * @param timeout thời gian timeout (seconds)
     * @return WebElement nếu có thể nhấn được, ngược lại null
     */
    public static WebElement waitForElementClickable(WebDriver driver, By by, int timeout) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, timeout);
            return wait.until(ExpectedConditions.elementToBeClickable(by));
        } catch (Exception e) {
            logger.error("Element {} not clickable within {}s: {}", by, timeout, e);
            return null;
        }
    }

    /**
     * Đợi đến khi một phần tử xuất hiện trong DOM (present), không nhất thiết là visible.
     *
     * @param driver  WebDriver hiện tại
     * @param by      Định danh phần tử (By.xpath, By.id,...)
     * @param timeout thời gian timeout (seconds)
     * @return WebElement nếu có mặt, ngược lại null
     */
    public static WebElement waitForElementPresent(WebDriver driver, By by, int timeout) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, timeout);
            return wait.until(ExpectedConditions.presenceOfElementLocated(by));
        } catch (Exception e) {
            logger.error("Element {} not present within {}s: {}", by, timeout, e);
            return null;
        }
    }
}
