package com.drjoy.automation.utils;

import com.drjoy.automation.config.DriverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class WebUI {

    private static final Logger logger = LogManager.getLogger(WebUI.class);
    public static final int LARGE_TIMEOUT = 60;
    public static final int NORMAL_TIMEOUT = 30;
    public static final int SMALL_TIMEOUT = 10;

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static WebElement findWebElementIfVisible(By by) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(NORMAL_TIMEOUT));

        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static WebElement findWebElementIfPresent(By by) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(SMALL_TIMEOUT));

        return wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public static List<WebElement> findWebElementsIfVisible(By by) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(NORMAL_TIMEOUT));

        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(by));
    }

    public static List<WebElement> findWebElementsIfVisible(By by, int timeoutSeconds) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));

        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(by));
    }

    public static List<WebElement> findWebElementsIfPresent(By by) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(SMALL_TIMEOUT));

        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
    }

    /**
     * Chờ phần tử hiển thị rồi click
     * Timeout = 60s
     * @param by {@link By}
     */
    public static void click(By by) {
        WebElement element = waitForElementPresent(by, LARGE_TIMEOUT);
        if (element != null) element.click();
    }

    /**
     * Click vào vị trí (x, y) trên màn hình trình duyệt.
     *
     * @param x Tọa độ X (px)
     * @param y Tọa độ Y (px)
     */
    public static void clickAtCoordinates(int x, int y) {
        Actions actions = new Actions(DriverFactory.getDriver());
        actions.moveByOffset(x, y).click().perform();

        // Đưa chuột về vị trí ban đầu để tránh lệch chuột
        actions.moveByOffset(-x, -y).perform();
    }

    public static void clickByJS(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) DriverFactory.getDriver();
        js.executeScript("arguments[0].click();", element);
    }

    /**
     * Cuộn đến phần tử và click vào phần tử đó.
     *
     * @param element Phần tử cần click.
     * Sử dụng JavascriptExecutor để cuộn đến phần tử trước khi click.
     */
    public static void clickWithScrollTo(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) DriverFactory.getDriver();
        js.executeScript("arguments[0].scrollIntoView(true);", element);
        element.click();
    }

    public static void scrollToElementCenter(WebElement e) {
        JavascriptExecutor js = (JavascriptExecutor) DriverFactory.getDriver();
        js.executeScript(
            "arguments[0].scrollIntoView({block: 'center', inline: 'center', behavior: 'instant'});",
            e
        );
    }

    public static void scrollToTop() {
        JavascriptExecutor js = (JavascriptExecutor) DriverFactory.getDriver();
        js.executeScript("window.scrollTo(0, 0);");
    }

    public static void mouseOver(WebElement target) {
        mouseOver(target, 500);
    }

    public static void mouseOver(WebElement target, int delay) {
        WebDriver driver = DriverFactory.getDriver();

        Actions actions = new Actions(driver);
        actions.moveToElement(target).pause(Duration.ofMillis(delay)).perform();
    }

    /**
     * Đợi đến khi một phần tử có thể nhấn được trong khoảng thời gian chỉ định.
     *
     * @param by      Định danh phần tử (By.xpath, By.id,...)
     * @param timeout thời gian timeout (seconds)
     * @return WebElement nếu có thể nhấn được, ngược lại null
     */
    public static WebElement waitForElementClickable(By by, int timeout) {
        try {
            WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(timeout));
            return wait.until(ExpectedConditions.elementToBeClickable(by));
        } catch (Exception e) {
            logger.error("Element {} not clickable within {}s: {}", by, timeout, e);
            return null;
        }
    }

    public static WebElement waitForElementClickable(By by) {
        return waitForElementClickable(by, LARGE_TIMEOUT);
    }

    /**
     * Đợi đến khi một phần tử xuất hiện trong DOM (present), không nhất thiết là visible.
     *
     * @param by      Định danh phần tử (By.xpath, By.id,...)
     * @param timeoutSeconds thời gian timeout (seconds)
     * @return WebElement nếu có mặt, ngược lại null
     */
    public static WebElement waitForElementPresent(By by, int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(timeoutSeconds));
            return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        } catch (Exception e) {
            logger.error("Element {} not present within {}s: {}", by, timeoutSeconds, e);
            return null;
        }
    }

    public static boolean waitForElementNotPresent(By by, int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(timeoutSeconds));
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
        } catch (Exception e) {
            logger.error("Element {} not present within {}s: {}", by, timeoutSeconds, e);
            return false;
        }
    }

    public static boolean isElementPresent(By by, int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(timeoutSeconds));
            wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        } catch (Exception e) {
            logger.error("Element {} not present within {}s: {}", by, timeoutSeconds, e);
            return false;
        }
        return true;
    }
}
